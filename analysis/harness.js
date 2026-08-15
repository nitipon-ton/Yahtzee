// Shared plumbing for the analysis scripts.
//
// script.js is a browser file: it grabs DOM nodes at load time and wires up
// event listeners. To run it under Node we load it into a vm context with a
// stub document, then reach in and drive the game classes directly.
//
// No dependencies. Node 14+.

const fs = require('fs');
const path = require('path');
const vm = require('vm');

const REPO_ROOT = path.join(__dirname, '..');
const DEFAULT_SCRIPT = path.join(REPO_ROOT, 'script.js');

// Every getElementById/createElement call gets one of these. Nothing renders;
// the properties just have to exist so script.js can assign to them.
function stubElement() {
  return {
    className: '', textContent: '', innerHTML: '', value: '', type: '',
    checked: false, disabled: false, open: false, style: {},
    classList: { add() {}, remove() {}, contains() { return false; } },
    append() {}, addEventListener() {},
  };
}

// xorshift32. Seeded so every run is reproducible — the numbers in README.md
// come from DEFAULT_SEED, and re-running with it should reproduce them exactly.
function seededMath(seed) {
  let s = seed >>> 0;
  if (s === 0) s = 1;
  const M = Object.create(Math);
  M.random = () => {
    s ^= s << 13; s >>>= 0;
    s ^= s >>> 17;
    s ^= s << 5; s >>>= 0;
    return s / 4294967296;
  };
  return M;
}

// Loads script.js into a sandbox and installs the instrumentation the
// analysis needs. `scriptPath` lets you point at a modified copy to compare
// two versions of the algorithm against each other.
function loadGame(seed, scriptPath = DEFAULT_SCRIPT) {
  const source = fs.readFileSync(scriptPath, 'utf8');
  const document = {
    getElementById: () => stubElement(),
    createElement: () => stubElement(),
    createDocumentFragment: () => stubElement(),
  };
  const context = vm.createContext({
    document,
    Math: seededMath(seed),
    console,
    setTimeout: () => 0,
    clearTimeout: () => {},
  });
  vm.runInContext(source, context, { filename: path.basename(scriptPath) });

  // Wrap performScore so every scoring decision is recorded. The joker bonus
  // is subtracted out, otherwise it would be credited to whichever box
  // happened to receive the dice on a bonus-Yahtzee turn.
  vm.runInContext(`
    globalThis.__moves = [];
    const __performScore = Player.prototype.performScore;
    Player.prototype.performScore = function (choice) {
      const scoreBefore = this.score;
      const bonusBefore = this.yahtBo;
      const result = __performScore.call(this, choice);
      __moves.push({
        round: game ? game.round : 0,
        choice,
        gained: this.score - scoreBefore - (this.yahtBo - bonusBefore),
        name: this.name,
      });
      return result;
    };

    // Plays one complete all-bot game and returns the finished scorecards.
    globalThis.__playGame = function (playerCount) {
      __moves.length = 0;
      botMode = 'silent';
      game = new Game(Array.from({ length: playerCount }, (_, i) => new Player('P' + i, true)));
      advanceBotsAndRender();
      return {
        finished: game.finished,
        names: game.players.map((p) => p.name),
        cards: game.players.map((p) => ({
          total: p.totalscore + p.score,
          upper: p.pntsBasic.reduce((a, b) => a + b, 0),
          basic: p.pntsBasic.slice(),
          upperBonus: p.bonus,
          jokerBonus: p.yahtBo,
          yaht: p.yaht,
          // Every box should be filled by the end: 6 upper + 5 lower + chance + yahtzee.
          boxesFilled: p.isAvailBasic.filter((x) => !x).length
            + p.isAvailAdv.filter((v) => v !== 1).length
            + (p.chanAvail ? 0 : 1)
            + (p.yaht !== 1 ? 1 : 0),
        })),
        moves: __moves.slice(),
      };
    };

    // Reference strategies, scored by the same engine so the comparison is fair.
    //   'none' - never reroll, just take the best category on the opening roll
    //   'mode' - keep whichever face appears most, reroll the rest, then score
    globalThis.__playBaseline = function (kind) {
      const p = new Player('X', false);
      game = { round: 1, players: [p], currentPlayer() { return p; } };
      for (let round = 1; round <= 13; round += 1) {
        game.round = round;
        p.roll_left = 3;
        p.rollDice();
        if (kind === 'mode') {
          while (p.roll_left > 0) {
            let best = 0;
            for (let i = 1; i < 6; i += 1) if (p.faceCounter[i] > p.faceCounter[best]) best = i;
            let mask = 0;
            for (let i = 0; i < 5; i += 1) if (p.arrVal[i] !== best + 1) mask += 10 * 2 ** i;
            if (mask === 0) break;
            p.rollDecision = mask;
            p.roll_left -= 1;
            p.rollDice();
          }
        }
        const open = p.getAvailableOptions().filter((o) => o.available && o.id !== 15);
        if (!open.length) break;
        const pick = open.reduce((a, b) => (b.points > a.points ? b : a), open[0]);
        p.performScore(pick.id);
        p.checkScoreCard();
        p.totalscore += p.score;
        p.score = 0;
        p.arrVal = [0, 0, 0, 0, 0];
        p.faceCounter = [0, 0, 0, 0, 0, 0];
        p.rollDecision = 0;
      }
      return p.totalscore;
    };
  `, context);

  return {
    context,
    playGame: vm.runInContext('__playGame', context),
    playBaseline: vm.runInContext('__playBaseline', context),
    newPlayer: () => vm.runInContext('new Player("probe", true)', context),
    run: (code) => vm.runInContext(code, context),
  };
}

function describe(values) {
  const sorted = [...values].sort((a, b) => a - b);
  const n = sorted.length;
  const mean = sorted.reduce((a, b) => a + b, 0) / n;
  const variance = sorted.reduce((a, b) => a + (b - mean) * (b - mean), 0) / (n - 1);
  const sd = Math.sqrt(variance);
  const at = (p) => sorted[Math.min(n - 1, Math.floor(p * n))];
  return {
    n,
    mean,
    median: n % 2 ? sorted[(n - 1) / 2] : (sorted[n / 2 - 1] + sorted[n / 2]) / 2,
    sd,
    se: sd / Math.sqrt(n),
    min: sorted[0],
    max: sorted[n - 1],
    p5: at(0.05), p25: at(0.25), p75: at(0.75), p95: at(0.95), p99: at(0.99),
  };
}

// Minimal --flag / --flag=value parser, so the scripts stay dependency-free.
function parseArgs(argv, defaults) {
  const out = { ...defaults };
  for (let i = 0; i < argv.length; i += 1) {
    const arg = argv[i];
    if (!arg.startsWith('--')) continue;
    const eq = arg.indexOf('=');
    const key = (eq === -1 ? arg.slice(2) : arg.slice(2, eq)).replace(/-([a-z])/g, (_, c) => c.toUpperCase());
    let value = eq === -1 ? argv[i + 1] : arg.slice(eq + 1);
    if (eq === -1) i += 1;
    if (!(key in out)) {
      console.error(`unknown option --${arg.slice(2)}`);
      process.exit(2);
    }
    if (typeof out[key] === 'number') {
      value = key === 'seed' ? Number(value) : parseInt(value, 10);
      if (!Number.isFinite(value)) {
        console.error(`--${key} needs a number`);
        process.exit(2);
      }
    }
    out[key] = value;
  }
  return out;
}

const CATEGORY_NAMES = {
  1: 'Ones', 2: 'Twos', 3: 'Threes', 4: 'Fours', 5: 'Fives', 6: 'Sixes',
  7: 'Yahtzee', 8: '4 of a kind', 9: '3 of a kind', 11: 'Chance',
  12: 'Small straight', 13: 'Large straight', 14: 'Full house', 15: 'End turn',
};

// The seed behind every figure quoted in README.md.
const DEFAULT_SEED = 0x59A47233;

module.exports = {
  loadGame, describe, parseArgs, seededMath, stubElement,
  CATEGORY_NAMES, DEFAULT_SEED, DEFAULT_SCRIPT, REPO_ROOT,
};
