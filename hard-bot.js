// Hard-mode bot.
//
// The easy bot (in script.js) is the original coursework logic: it matches the
// hand against hand-derived probability tables and chases whichever named
// pattern is most likely. This one plays differently in two ways.
//
//  1. EXPECTIMAX WITHIN THE TURN. It evaluates all 32 keep-sets by their exact
//     expected value two rerolls deep, using precomputed dice-transition
//     distributions. Same information as the probability tables, but it
//     optimises points directly instead of the odds of one named pattern.
//
//  2. OPPORTUNITY COST. A box is not worth its points, it is worth its points
//     minus what that box would have been worth if saved for later. Scoring 18
//     in Sixes is good; scoring 18 in Chance is bad, because Chance averages
//     about 22. This is what makes it willing to scratch a Full House.
//
// The upper bonus is priced continuously rather than only on completion, so
// progress toward 63 is worth something before the box that finishes it.
//
// Exposes globalThis.HardBot.decide(player, round) -> category id or reroll
// mask, in the same encoding getBotAction uses. Tables are built on first use.

(function (root) {
  'use strict';

  var CAT = { YAHTZEE: 7, FOUR: 8, THREE: 9, CHANCE: 11, SMALL: 12, LARGE: 13, FULL: 14 };

  // What each box tends to be worth if kept for later. Derived by self-play:
  // play, measure what each box actually realises, feed back, repeat. These
  // converged after two rounds and barely move afterwards.
  var OPP = {
    1: 1.7, 2: 4.6, 3: 7.9, 4: 11.4, 5: 14.6, 6: 17.5,
    7: 15.3, 8: 15.2, 9: 22.3, 11: 22.4, 12: 29.4, 13: 32.2, 14: 21.4,
  };

  var T = null; // built lazily

  function buildTables() {
    var dice = [], diceIndex = {};
    (function enumerate(cur) {
      if (cur.length === 5) { diceIndex[cur.join('')] = dice.length; dice.push(cur.slice()); return; }
      for (var v = cur.length ? cur[cur.length - 1] : 1; v <= 6; v += 1) { cur.push(v); enumerate(cur); cur.pop(); }
    }([]));
    var nd = dice.length; // 252

    var counts = [], sum = new Int32Array(nd), max = new Int32Array(nd),
      sq = new Int32Array(nd), small = new Uint8Array(nd), large = new Uint8Array(nd);
    for (var i = 0; i < nd; i += 1) {
      var c = [0, 0, 0, 0, 0, 0], s = 0;
      for (var j = 0; j < 5; j += 1) { c[dice[i][j] - 1] += 1; s += dice[i][j]; }
      counts.push(c); sum[i] = s;
      max[i] = Math.max(c[0], c[1], c[2], c[3], c[4], c[5]);
      sq[i] = c[0] * c[0] + c[1] * c[1] + c[2] * c[2] + c[3] * c[3] + c[4] * c[4] + c[5] * c[5];
      small[i] = (c[0] * c[1] * c[2] * c[3] + c[1] * c[2] * c[3] * c[4] + c[2] * c[3] * c[4] * c[5]) > 0 ? 1 : 0;
      large[i] = (c[1] === 1 && c[2] === 1 && c[3] === 1 && c[4] === 1) ? 1 : 0;
    }

    // every sub-multiset that can be kept, and where a reroll from it leads
    var keptIndex = {}, kept = [];
    function keptId(vals) {
      var k = vals.join('');
      if (keptIndex[k] === undefined) { keptIndex[k] = kept.length; kept.push(vals.slice()); }
      return keptIndex[k];
    }
    var handMaskKept = new Int32Array(nd * 32);
    for (var d = 0; d < nd; d += 1) {
      for (var m = 0; m < 32; m += 1) {
        var sub = [];
        for (var b = 0; b < 5; b += 1) if (m & (1 << b)) sub.push(dice[d][b]);
        handMaskKept[d * 32 + m] = keptId(sub);
      }
    }
    var nk = kept.length; // 462
    var tDest = [], tProb = [];
    for (var k = 0; k < nk; k += 1) {
      var base = kept[k], roll = 5 - base.length, total = Math.pow(6, roll), acc = {};
      var hand = base.slice();
      (function rec(depth) {
        if (depth === roll) {
          var key = hand.slice().sort(function (a, b) { return a - b; }).join('');
          var idx = diceIndex[key];
          acc[idx] = (acc[idx] || 0) + 1;
          return;
        }
        for (var v = 1; v <= 6; v += 1) { hand.push(v); rec(depth + 1); hand.pop(); }
      }(0));
      var keys = Object.keys(acc);
      var dest = new Int32Array(keys.length), prob = new Float64Array(keys.length);
      for (var q = 0; q < keys.length; q += 1) { dest[q] = Number(keys[q]); prob[q] = acc[keys[q]] / total; }
      tDest.push(dest); tProb.push(prob);
    }

    T = {
      dice: dice, diceIndex: diceIndex, nd: nd, nk: nk,
      counts: counts, sum: sum, max: max, sq: sq, small: small, large: large,
      keptIndex: keptIndex, handMaskKept: handMaskKept, tDest: tDest, tProb: tProb,
      V0: new Float64Array(nd), V1: new Float64Array(nd),
      E0: new Float64Array(nk), E1: new Float64Array(nk),
    };
  }

  function bonusCredit(total) {
    if (total >= 63) return 35;
    var r = total / 63;
    return 35 * r * r;
  }

  // Best value obtainable by stopping now with hand `d`, given scorecard `s`.
  function terminalValue(d, s, wantChoice) {
    var c = T.counts[d], sum = T.sum[d], mx = T.max[d];
    var best = -1e9, bestCat = 0;
    function consider(cat, pts, bonusDelta) {
      var v = pts + bonusDelta - OPP[cat] * s.oppScale;
      if (v > best) { best = v; bestCat = cat; }
    }
    if (mx === 5 && s.yaht !== 1) {
      // Joker turn: the bonus is automatic, but the dice must still be placed.
      var face = T.dice[d][0];
      var extra = s.yaht === 0 ? 100 : 0;
      if (s.basic[face - 1]) {
        var pts = face * 5;
        consider(face, pts + extra, bonusCredit(s.upper + pts) - bonusCredit(s.upper));
      } else {
        if (s.adv[0] > 0) consider(CAT.THREE, sum + extra, 0);
        if (s.adv[1] > 0) consider(CAT.FOUR, sum + extra, 0);
        if (s.adv[2] > 0) consider(CAT.FULL, 25 + extra, 0);
        if (s.adv[3] > 0) consider(CAT.SMALL, 30 + extra, 0);
        if (s.adv[4] > 0) consider(CAT.LARGE, 40 + extra, 0);
        if (s.chance) consider(CAT.CHANCE, sum + extra, 0);
        if (best < -1e8) {
          for (var f2 = 1; f2 <= 6; f2 += 1) if (s.basic[f2 - 1]) consider(f2, extra, 0);
        }
      }
      return wantChoice ? { v: best, cat: bestCat } : best;
    }
    for (var f = 1; f <= 6; f += 1) {
      if (!s.basic[f - 1]) continue;
      var p = f * c[f - 1];
      consider(f, p, s.upper < 63 ? bonusCredit(s.upper + p) - bonusCredit(s.upper) : 0);
    }
    if (s.adv[0] > 0) consider(CAT.THREE, mx >= 3 ? sum : 0, 0);
    if (s.adv[1] > 0) consider(CAT.FOUR, mx >= 4 ? sum : 0, 0);
    if (s.adv[2] > 0) consider(CAT.FULL, T.sq[d] === 13 ? 25 : 0, 0);
    if (s.adv[3] > 0) consider(CAT.SMALL, T.small[d] ? 30 : 0, 0);
    if (s.adv[4] > 0) consider(CAT.LARGE, T.large[d] ? 40 : 0, 0);
    if (s.yaht === 1) consider(CAT.YAHTZEE, mx === 5 ? 50 : 0, 0);
    if (s.chance) consider(CAT.CHANCE, sum, 0);
    return wantChoice ? { v: best, cat: bestCat } : best;
  }

  function expectation(src, dst) {
    for (var k = 0; k < T.nk; k += 1) {
      var dest = T.tDest[k], prob = T.tProb[k], acc = 0;
      for (var j = 0; j < dest.length; j += 1) acc += prob[j] * src[dest[j]];
      dst[k] = acc;
    }
  }

  function decide(player, round) {
    if (!T) buildTables();
    var upper = 0;
    for (var u = 0; u < 6; u += 1) upper += player.pntsBasic[u];
    var s = {
      basic: player.isAvailBasic, adv: player.isAvailAdv, chance: player.chanAvail,
      yaht: player.yaht, upper: upper,
      // On the final turn there is no "later", so a box costs nothing to spend.
      oppScale: Math.max(0, 13 - round) / 12,
    };
    var sorted = player.arrVal.slice().sort(function (a, b) { return a - b; }).join('');
    var d = T.diceIndex[sorted];
    if (d === undefined) return 0; // unrolled hand; let the caller fall back

    if (player.roll_left <= 0) return terminalValue(d, s, true).cat;

    for (var i = 0; i < T.nd; i += 1) T.V0[i] = terminalValue(i, s, false);
    expectation(T.V0, T.E0);
    var table = T.E0;
    if (player.roll_left >= 2) {
      for (var h = 0; h < T.nd; h += 1) {
        var b = T.V0[h];
        for (var m = 0; m < 32; m += 1) {
          var e = T.E0[T.handMaskKept[h * 32 + m]];
          if (e > b) b = e;
        }
        T.V1[h] = b;
      }
      expectation(T.V1, T.E1);
      table = T.E1;
    }

    // Search keep-sets over the real dice positions so the mask maps back.
    var bestMask = -1, bestVal = -1e9;
    for (var mm = 0; mm < 32; mm += 1) {
      var sub = [];
      for (var bit = 0; bit < 5; bit += 1) if (mm & (1 << bit)) sub.push(player.arrVal[bit]);
      sub.sort(function (a, b2) { return a - b2; });
      var val = table[T.keptIndex[sub.join('')]];
      if (val > bestVal) { bestVal = val; bestMask = mm; }
    }
    var stop = terminalValue(d, s, true);
    if (stop.v >= bestVal || bestMask === 31) return stop.cat;
    var rerollMask = 0;
    for (var r = 0; r < 5; r += 1) if (!(bestMask & (1 << r))) rerollMask += 10 * Math.pow(2, r);
    return rerollMask === 0 ? stop.cat : rerollMask;
  }

  root.HardBot = { decide: decide };
}(typeof globalThis !== 'undefined' ? globalThis : this));
