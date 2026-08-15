// Full bot performance report — this is what produced the numbers in README.md.
//
//   node analysis/analyze.js
//   node analysis/analyze.js --games 5000
//   node analysis/analyze.js --compare /tmp/tweaked-script.js
//
// Options:
//   --games N     games to simulate            (default 40000)
//   --players N   bots per game                (default 3)
//   --seed N      PRNG seed                    (default matches README.md)
//   --compare F   also run F and print the matched-seed delta against script.js
//
// With --compare, both versions see an identical dice sequence, so the
// difference between them is the change you made and not luck. That is the
// right way to check whether an algorithm tweak actually helped.

const { loadGame, describe, parseArgs, CATEGORY_NAMES, DEFAULT_SEED, DEFAULT_SCRIPT } = require('./harness');

const OPTIMAL = 254.6; // published optimal average for solitaire Yahtzee
const opts = parseArgs(process.argv.slice(2), {
  games: 40000, players: 3, seed: DEFAULT_SEED, compare: '',
});

const f = (x, d = 1) => Number(x).toFixed(d);
const pct = (part, whole) => f(100 * part / whole);
const bar = (n) => '#'.repeat(Math.max(0, Math.round(n)));

// Plays `games` games and collects everything the report needs in one pass.
function survey(scriptPath) {
  const g = loadGame(opts.seed, scriptPath);
  const totals = [], uppers = [], upperBonuses = [], jokerBonuses = [], zerosPerCard = [];
  const perFace = [[], [], [], [], [], []];
  const byCategory = {};
  const yahtSeen = new Set();
  let cards = 0, unfinished = 0, badBoxCount = 0, yahtzeeScored = 0, turns = 0, zeroTurns = 0;

  for (let i = 0; i < opts.games; i += 1) {
    const result = g.playGame(opts.players);
    if (!result.finished) unfinished += 1;

    const zeros = {};
    for (const name of result.names) zeros[name] = 0;
    for (const move of result.moves) {
      turns += 1;
      if (move.gained === 0) { zeroTurns += 1; zeros[move.name] += 1; }
      const entry = byCategory[move.choice]
        || (byCategory[move.choice] = { picks: 0, scored: 0, points: 0, rounds: 0 });
      entry.picks += 1;
      entry.rounds += move.round;
      if (move.gained > 0) { entry.scored += 1; entry.points += move.gained; }
    }
    for (const name of result.names) zerosPerCard.push(zeros[name]);

    for (const card of result.cards) {
      cards += 1;
      totals.push(card.total);
      uppers.push(card.upper);
      upperBonuses.push(card.upperBonus);
      jokerBonuses.push(card.jokerBonus);
      card.basic.forEach((v, i2) => perFace[i2].push(v));
      if (card.boxesFilled !== 13) badBoxCount += 1;
      yahtSeen.add(card.yaht);
      if (card.yaht === 0) yahtzeeScored += 1;
    }
  }
  return {
    game: g, totals, uppers, upperBonuses, jokerBonuses, zerosPerCard, perFace,
    byCategory, yahtSeen, cards, unfinished, badBoxCount, yahtzeeScored, turns, zeroTurns,
  };
}

const s = survey(DEFAULT_SCRIPT);
const T = describe(s.totals);
const U = describe(s.uppers);

const rule = '='.repeat(76);
console.log(rule);
console.log(`${opts.games} games x ${opts.players} bots = ${s.cards} scorecards   seed 0x${opts.seed.toString(16)}`);
console.log(`integrity: ${s.unfinished} unfinished, ${s.badBoxCount} cards not filling all 13 boxes, `
  + `yaht values seen [${[...s.yahtSeen].sort((a, b) => a - b)}]`);
console.log(rule);

console.log(`\n## FINAL SCORE   (standard error +/- ${f(T.se, 2)})`);
console.log(`mean ${f(T.mean)} | median ${T.median} | sd ${f(T.sd)} | min ${T.min} | max ${T.max}`);
console.log(`p5 ${T.p5} | p25 ${T.p25} | p75 ${T.p75} | p95 ${T.p95} | p99 ${T.p99}`);
console.log(`mean sits ${f(T.mean - T.median)} above the median\n`);
const buckets = {};
for (const t of s.totals) { const b = Math.floor(t / 25) * 25; buckets[b] = (buckets[b] || 0) + 1; }
for (const k of Object.keys(buckets).map(Number).sort((a, b) => a - b)) {
  const share = 100 * buckets[k] / s.cards;
  if (share < 0.05) continue;
  console.log(`  ${String(k).padStart(4)}-${String(k + 24).padEnd(4)} ${bar(share * 2).padEnd(70)} ${f(share).padStart(5)}%`);
}
console.log(`  in the 175-224 band ${pct(s.totals.filter((x) => x >= 175 && x < 225).length, s.cards)}%`
  + ` | below 150 ${pct(s.totals.filter((x) => x < 150).length, s.cards)}%`);

console.log(`\n## BASELINES (same scoring engine, ${opts.games} games each)`);
const neverReroll = [], keepCommon = [];
for (let i = 0; i < opts.games; i += 1) {
  neverReroll.push(s.game.playBaseline('none'));
  keepCommon.push(s.game.playBaseline('mode'));
}
const B0 = describe(neverReroll), B1 = describe(keepCommon);
console.log('  strategy                    mean   median      sd     max');
for (const [name, d] of [['take best, never reroll', B0], ['keep the most common face', B1], ['THIS BOT', T]]) {
  console.log(`  ${name.padEnd(26)}${f(d.mean).padStart(6)} ${String(d.median).padStart(8)} `
    + `${f(d.sd).padStart(7)} ${String(d.max).padStart(7)}`);
}
console.log(`  vs never-reroll +${f(T.mean - B0.mean)} (${f(100 * (T.mean / B0.mean - 1))}%)`
  + ` | vs keep-common-face +${f(T.mean - B1.mean)} (${f(100 * (T.mean / B1.mean - 1))}%)`
  + ` | ${f(100 * T.mean / OPTIMAL)}% of the ${OPTIMAL} optimal`);

console.log('\n## PER CATEGORY (joker bonus excluded from the points column)');
console.log('  category          picks   scored%   avg when scored   avg round');
const contribution = {};
const order = Object.keys(s.byCategory).map(Number)
  .sort((a, b) => s.byCategory[b].scored / s.byCategory[b].picks - s.byCategory[a].scored / s.byCategory[a].picks);
for (const id of order) {
  const e = s.byCategory[id];
  contribution[CATEGORY_NAMES[id]] = e.points / s.cards;
  console.log(`  ${(CATEGORY_NAMES[id] || id).padEnd(16)}${String(e.picks).padStart(7)}`
    + `${pct(e.scored, e.picks).padStart(9)}%${f(e.scored ? e.points / e.scored : 0).padStart(15)}`
    + `${f(e.rounds / e.picks).padStart(12)}`);
}

console.log('\n## WHERE THE POINTS COME FROM (average per card)');
const upperBonusAvg = 35 * s.upperBonuses.filter((x) => x > 0).length / s.cards;
const jokerBonusAvg = s.jokerBonuses.reduce((a, b) => a + b, 0) / s.cards;
const sources = [
  ['Upper section', U.mean],
  ['Large straight', contribution['Large straight']],
  ['Small straight', contribution['Small straight']],
  ['Full house', contribution['Full house']],
  ['Chance', contribution.Chance],
  ['3 of a kind', contribution['3 of a kind']],
  ['Yahtzee box', contribution.Yahtzee],
  ['4 of a kind', contribution['4 of a kind']],
  ['Yahtzee bonus', jokerBonusAvg],
  ['Upper bonus', upperBonusAvg],
].sort((a, b) => b[1] - a[1]);
const sourceTotal = sources.reduce((a, r) => a + r[1], 0);
for (const [name, value] of sources) {
  console.log(`  ${name.padEnd(16)}${f(value).padStart(5)}  ${bar(value).padEnd(50)} ${f(100 * value / sourceTotal)}%`);
}
console.log(`  ${'TOTAL'.padEnd(16)}${f(sourceTotal).padStart(5)}   (should equal the mean above, ${f(T.mean)})`);

console.log('\n## UPPER SECTION (the bonus needs 63)');
console.log(`  mean ${f(U.mean)} | median ${U.median} | sd ${f(U.sd)}`
  + ` | bonus earned on ${pct(s.upperBonuses.filter((x) => x > 0).length, s.cards)}% of cards`
  + ` | ${f(63 - U.mean)} short on average`);
['Ones', 'Twos', 'Threes', 'Fours', 'Fives', 'Sixes'].forEach((name, i) => {
  const d = describe(s.perFace[i]);
  const par = (i + 1) * 3;
  console.log(`  ${name.padEnd(7)} avg ${f(d.mean).padStart(5)}  par ${String(par).padStart(2)}`
    + `  vs par ${((d.mean - par >= 0 ? '+' : '') + f(d.mean - par)).padStart(5)}`
    + `  zeroed ${pct(s.perFace[i].filter((v) => v === 0).length, s.cards).padStart(5)}%`);
});
console.log(`  within 6 points of the bonus: ${pct(s.uppers.filter((u) => u >= 57 && u < 63).length, s.cards)}%`);
const withBonus = s.totals.filter((_, i) => s.upperBonuses[i] > 0);
const withoutBonus = s.totals.filter((_, i) => s.upperBonuses[i] === 0);
const avg = (a) => a.reduce((x, y) => x + y, 0) / a.length;
console.log(`  cards WITH the bonus average ${f(avg(withBonus))} (${pct(withBonus.length, s.cards)}% of cards)`
  + ` | WITHOUT ${f(avg(withoutBonus))}`);

console.log('\n## TURN OUTCOMES');
console.log(`  ${s.turns} scoring turns, ${s.zeroTurns} scored nothing (${pct(s.zeroTurns, s.turns)}%)`
  + ` | ${f(s.zerosPerCard.reduce((a, b) => a + b, 0) / s.cards, 2)} zeros per card`);
const zeroHist = {};
for (const z of s.zerosPerCard) zeroHist[z] = (zeroHist[z] || 0) + 1;
for (const k of Object.keys(zeroHist).map(Number).sort((a, b) => a - b)) {
  console.log(`    ${k} zeros: ${pct(zeroHist[k], s.cards).padStart(5)}%`);
}
console.log(`  Yahtzee box scored on ${pct(s.yahtzeeScored, s.cards)}% of cards`
  + ` | joker bonus earned on ${pct(s.jokerBonuses.filter((x) => x > 0).length, s.cards)}%`);

// ---- optional matched-seed comparison against a modified copy ----
if (opts.compare) {
  console.log(`\n${rule}`);
  console.log(`COMPARISON vs ${opts.compare}   (same seed, so the dice are identical)`);
  console.log(rule);
  const other = survey(opts.compare);
  const O = describe(other.totals);
  const ci = 1.96 * Math.hypot(T.se, O.se);
  // Positive means the current script.js is ahead of the file it was compared to.
  const delta = T.mean - O.mean;
  const otherName = require('path').basename(opts.compare);
  const width = Math.max(9, otherName.length);
  console.log(`  ${'script.js'.padEnd(width)}  mean ${f(T.mean)}  median ${T.median}  sd ${f(T.sd)}`);
  console.log(`  ${otherName.padEnd(width)}  mean ${f(O.mean)}  median ${O.median}  sd ${f(O.sd)}`);
  console.log(`\n  script.js is ${(delta >= 0 ? '+' : '') + f(delta, 2)} points vs ${otherName}, 95% CI +/-${f(ci, 2)}`);
  console.log(`  ${Math.abs(delta) > ci ? (delta > 0 ? 'SIGNIFICANT GAIN' : 'SIGNIFICANT LOSS') : 'not distinguishable from noise'} at this sample size`);
  console.log(`  integrity of the compared build: ${other.unfinished} unfinished, `
    + `${other.badBoxCount} cards not filling all 13 boxes`);
}
