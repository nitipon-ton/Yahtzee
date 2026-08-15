// Checks every hardcoded probability branch against brute-force enumeration.
//
//   node analysis/audit-probabilities.js
//   node analysis/audit-probabilities.js --verbose
//
// The six prob* functions answer "what are the odds of hitting this pattern if
// I reroll these dice?" with closed-form branches instead of enumerating
// outcomes. That is much faster, but a hand-derived branch can be wrong in a
// way no amount of playtesting would reveal, so this compares all of them
// against the truth.
//
// The whole input space is only 210 states: the answer depends on the multiset
// of KEPT dice and how many dice are rerolled, never on which slots they sit
// in (this script verifies that assumption too). So it can be checked
// exhaustively rather than sampled.
//
// Exits non-zero if anything disagrees, so it works as a pre-commit check.

const { loadGame, parseArgs, DEFAULT_SEED } = require('./harness');

const opts = parseArgs(process.argv.slice(2), { verbose: '' });
const verbose = opts.verbose !== '';

const PATTERNS = ['toak', 'foak', 'fh', 'smstr', 'lgstr', 'yaht'];
const FN = {
  toak: 'probTOAK', foak: 'probFOAK', fh: 'probFH',
  smstr: 'probSmStr', lgstr: 'probLgStr', yaht: 'probYaht',
};

// The game's own pattern tests, so the audit measures what the game rewards.
const counts = [0, 0, 0, 0, 0, 0];
function classify(hand) {
  counts.fill(0);
  for (const v of hand) counts[v - 1] += 1;
  let max = 0, sumSquares = 0;
  for (let i = 0; i < 6; i += 1) {
    if (counts[i] > max) max = counts[i];
    sumSquares += counts[i] * counts[i];
  }
  const straight4 = counts[0] * counts[1] * counts[2] * counts[3]
    + counts[1] * counts[2] * counts[3] * counts[4]
    + counts[2] * counts[3] * counts[4] * counts[5];
  return {
    toak: max >= 3,
    foak: max >= 4,
    yaht: max === 5,
    fh: sumSquares === 13,
    smstr: straight4 > 0,
    lgstr: counts[1] === 1 && counts[2] === 1 && counts[3] === 1 && counts[4] === 1,
  };
}

// Exact probability: enumerate all 6^k outcomes of rerolling k dice.
function enumerate(kept, k) {
  const total = 6 ** k;
  const hits = { toak: 0, foak: 0, fh: 0, smstr: 0, lgstr: 0, yaht: 0 };
  const hand = kept.concat(new Array(k).fill(0));
  const recurse = (i) => {
    if (i === k) {
      const got = classify(hand);
      for (const p of PATTERNS) if (got[p]) hits[p] += 1;
      return;
    }
    for (let v = 1; v <= 6; v += 1) { hand[kept.length + i] = v; recurse(i + 1); }
  };
  recurse(0);
  const out = {};
  for (const p of PATTERNS) out[p] = hits[p] / total;
  return out;
}

// All non-decreasing sequences of length m over 1..6 — i.e. every distinct
// multiset of kept dice.
function multisets(m) {
  const out = [];
  const build = (start, current) => {
    if (current.length === m) { out.push(current.slice()); return; }
    for (let v = start; v <= 6; v += 1) { current.push(v); build(v, current); current.pop(); }
  };
  build(1, []);
  return out;
}

const g = loadGame(DEFAULT_SEED);
const probe = g.newPlayer();

// --- assumption check: does the answer depend on WHICH slots are kept? ---
let placementChecks = 0, placementDisagreements = 0;
for (const kept of multisets(3)) {
  const layouts = [[0, 1, 2], [4, 3, 2], [2, 0, 4]];
  let reference = null;
  for (const slots of layouts) {
    const hand = [1, 1, 1, 1, 1];
    kept.forEach((v, i) => { hand[slots[i]] = v; });
    let mask = 0;
    for (let i = 0; i < 5; i += 1) if (!slots.includes(i)) mask += 10 * 2 ** i;
    const values = PATTERNS.map((p) => probe[FN[p]](hand, mask));
    if (reference === null) reference = values;
    else {
      placementChecks += 1;
      if (values.some((v, i) => Math.abs(v - reference[i]) > 1e-12)) placementDisagreements += 1;
    }
  }
}

// --- exhaustive audit over the full 210-state space ---
const report = {};
for (const p of PATTERNS) report[p] = { states: 0, exact: 0, wrong: [], maxError: 0 };

for (let kept = 0; kept <= 4; kept += 1) {
  const rerolled = 5 - kept;
  let mask = 0;
  for (let i = kept; i <= 4; i += 1) mask += 10 * 2 ** i;
  for (const values of multisets(kept)) {
    const hand = values.concat(new Array(rerolled).fill(1));
    const truth = enumerate(values, rerolled);
    for (const p of PATTERNS) {
      const got = probe[FN[p]](hand, mask);
      const want = truth[p];
      const err = Math.abs(got - want);
      const r = report[p];
      r.states += 1;
      if (err < 1e-12) r.exact += 1;
      else {
        r.wrong.push({ kept: values.join('') || '(none)', rerolled, got, want });
        if (err > r.maxError) r.maxError = err;
      }
    }
  }
}

const totalStates = report.toak.states;
console.log(`Exhaustive audit: ${totalStates} keep/reroll states x ${PATTERNS.length} functions `
  + `= ${totalStates * PATTERNS.length} values\n`);
console.log(`placement independence: ${placementChecks} permuted layouts checked, `
  + `${placementDisagreements} disagreed`
  + `${placementDisagreements === 0 ? '  (so 210 states really is the whole space)' : '  *** ASSUMPTION BROKEN ***'}`);
console.log('');
console.log('  function      states   exact   wrong   max error');
let failures = placementDisagreements;
for (const p of PATTERNS) {
  const r = report[p];
  failures += r.wrong.length;
  console.log(`  ${FN[p].padEnd(12)}${String(r.states).padStart(8)}${String(r.exact).padStart(8)}`
    + `${String(r.wrong.length).padStart(8)}${(r.wrong.length ? r.maxError.toFixed(4) : '-').padStart(12)}`);
}

if (verbose || failures) {
  for (const p of PATTERNS) {
    for (const w of report[p].wrong) {
      console.log(`    ${FN[p]}  keep [${w.kept}] reroll ${w.rerolled}: `
        + `table ${w.got.toFixed(4)}, truth ${w.want.toFixed(4)}`);
    }
  }
}

console.log(failures === 0
  ? '\nAll probability branches match brute-force enumeration exactly.'
  : `\n${failures} MISMATCHES`);
process.exit(failures === 0 ? 0 : 1);
