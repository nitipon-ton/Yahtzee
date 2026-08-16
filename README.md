# Yahtzee

A full Yahtzee implementation with a probability-driven bot opponent — originally a Grade 12 AP Computer Science A project, later ported to the browser.

The interesting part isn't the game. It's the bot: it evaluates every one of the 31 possible reroll combinations against hand-derived probability tables for six different scoring patterns, and picks the one with the best odds. Those tables were worked out by hand on paper, not simulated.

## History

| | |
|---|---|
| **Late 2022** | Java console version written for Grade 12 AP Computer Science A. This is where ~95% of the game and bot logic comes from, and it has barely changed since. |
| **Nov 2023** | First pushed to GitHub, once I'd figured out how to use it. The initial commit is the 2022 coursework, not new work. |
| **Aug 2026** | Browser port (`index.html` / `script.js` / `styles.css`), deployed as a static site. Same logic, translated to JavaScript, plus a UI and a bot decision log. |

Everything after 2022 has been tweaks, bug fixes and the web front-end. The core decision-making is the original coursework — so the git history starts about a year after the interesting part was written.

## What's in here

| File | |
|---|---|
| `Player.java` | The original. Scorecard state, probability tables, bot decisions, console I/O — all in one class. |
| `DiceMain.java` | Console entry point: player setup, 13-round loop, ranking, score histogram. |
| `Dice.java`, `Helper.java` | Die roll; array sum/max helpers. |
| `script.js` | The web port. Same `Player` logic plus a `Game` class, rendering, and bot scheduling. |
| `hard-bot.js` | Hard-mode bot: expectimax over rerolls, categories priced by opportunity cost. |
| `index.html`, `styles.css` | UI. |
| `analysis/` | Simulation harness that produces the report at the bottom of this file. |
| `vercel.json` | Static deploy config. |

### Running it

**Console (Java):**

```
javac *.java
java DiceMain
```

**Web:** open `index.html` directly, or serve the folder with any static server.

### Reproducing this

The analysis tooling is plain Node with no dependencies. It loads `script.js` into a sandbox with a stub DOM, so it always measures the real game code rather than a copy that can drift out of sync.

```
node analysis/analyze.js               # the full report below, ~90s
node analysis/analyze.js --games 2000  # a quick look
node analysis/audit-probabilities.js   # check the prob tables against enumeration
```

`analyze.js` defaults to the seed every number in this file was generated with, so a clean checkout reproduces the report exactly.

**After tweaking the algorithm**, the useful mode is a matched-seed comparison — both versions see an identical dice sequence, so the difference is your change rather than luck:

```
cp script.js /tmp/before.js     # stash the current version, then edit script.js
node analysis/analyze.js --compare /tmp/before.js
```

That prints the delta with a 95% confidence interval and says whether it clears the noise floor. Worth knowing: most plausible-sounding tweaks land inside the noise, so the interval matters more than the sign of the delta.

`audit-probabilities.js` exits non-zero on any mismatch, so it works as a pre-commit check if you ever touch the `prob*` functions.

## House rules

This is not quite tournament Yahtzee. Worth knowing before comparing scores to anything published:

- 13 rounds, 13 categories, three rolls per turn.
- Upper bonus: **35 points at 63+** in the upper section.
- 3-of-a-kind, 4-of-a-kind and Chance all score the **sum of all five dice**.
- Small Straight 30, Large Straight 40, Full House 25.
- The Yahtzee box is filled **exactly once**: 50 if you have one, a scratched 0 if you spend it. It then closes for good.
- Every Yahtzee rolled after that scores a **100-point bonus** and is placed as a joker, below.
- Choosing a category you don't actually have **scratches** it to zero, as normal.

### The joker rule

Rolling a second Yahtzee does two things at once. You collect the 100-point bonus, **and** you must still enter that roll somewhere on the scorecard for the turn. The five matching dice act as a wildcard — a *joker* — that can fill a box they wouldn't normally qualify for, in strict priority order:

1. **If the matching upper box is open, it is forced.** Five 6s must go in Sixes, for the sum of the dice — 30.
2. **If that box is already filled, any open lower box takes it at full value.** Full House 25, Small Straight 30, Large Straight 40, three/four of a kind and Chance the dice sum — even though five 6s is obviously none of those.
3. **If the entire lower section is full too, a zero goes into an open upper box.**

The bonus is only paid if the Yahtzee box holds 50. Scratch it to 0 and later Yahtzees still place as jokers, but earn nothing extra.

The web version implements all three stages. Bots pick the highest-scoring legal placement; a human is shown only the legal options, with the joker values already applied, and a note in the suggestion panel explaining which stage they're in.

The consequence is that every turn fills exactly one box, so all 13 boxes are used in all 13 rounds — verified across 120,000 simulated scorecards, every one of which ended with 13 of 13 filled.

> The Java console version predates this and still treats the Yahtzee box as re-selectable. Only `script.js` has the joker rule.

### Two encodings that look like bugs but aren't

Anyone reading the source should know these up front:

**Bot decisions are a single `int` with two meanings.** A positive multiple of 10 is a *reroll mask*, where each die contributes `10 × 2^i` — so `310` means "reroll all five", `10` means "reroll die A only". Any other value is a *category id* (1–6 upper, 7 Yahtzee, 8/9 four/three of a kind, 11 Chance, 12/13 straights, 14 full house, 15 end turn, 99 forfeit).

**`isAvailAdv` is a tri-state int, not a boolean.** `1` = still open, `0` = scored, `-1` = scratched. Converting it to a boolean would break the game. Note one inconsistency: scratching Full House / Small Straight / Large Straight sets `-1`, but scratching 3-of-a-kind or 4-of-a-kind goes through `Math.max(0, x - 2)` and lands on `0` — indistinguishable from a successful score. The category still becomes unavailable either way, so play is unaffected, but you cannot tell "scored" from "scratched" for those two by reading the final state.

---

# The probability tables

The six `prob*` functions are the heart of the bot. Rather than enumerating what could happen, each one answers *"what is the chance of hitting this pattern if I reroll these dice?"* with a tree of hardcoded branches keyed on cheap invariants of the kept dice — their sum, their sum of squares, and `diffpair` (the count of ordered pairs of kept dice with different values). These were derived by hand on paper.

## They really are much faster than enumeration

The alternative is brute force: for a given reroll mask, enumerate all 6^k outcomes and count how many produce the pattern. Measured over a complete reroll analysis (all 31 masks × 6 categories):

| Approach | Per analysis | Relative |
|---|---|---|
| **Hardcoded tables (current)** | **23.0 µs** | **1.00×** |
| Brute force, 6^k enumeration | 785.7 µs | 34.21× |
| Precomputed exact lookup | 25.2 µs | 1.10× |

A full analysis touches 186 branch evaluations in the current code, versus **16,806 enumerated outcomes** for brute force. Extrapolated to real play:

| Approach | Per scorecard (~36 analyses) | Full 10-bot game |
|---|---|---|
| Hardcoded tables | 0.83 ms | 8.3 ms |
| Brute force | 28.3 ms | 283 ms |
| Precomputed lookup | 0.91 ms | 9.1 ms |

So yes — the hardcoded approach is roughly **34× faster than enumerating rerolls**, and it is the reason the web version can play out an entire 10-bot game instantly instead of visibly stalling the browser. On a 2013-era school machine running the Java console version, the difference would have been far more pronounced.

## And they're exact

Because the functions depend only on the *multiset* of kept dice and the number of dice rerolled — verified by re-running 672 permuted placements with zero disagreements — the entire input space is just **210 states**. That's small enough to audit exhaustively against ground truth computed by enumeration, using the game's own pattern predicates.

| Function | States | Exact | Wrong |
|---|---|---|---|
| `probTOAK` | 210 | 210 | 0 |
| `probFOAK` | 210 | 210 | 0 |
| `probFH` | 210 | 210 | 0 |
| `probYaht` | 210 | 210 | 0 |
| `probSmStr` | 210 | 210 | 0 |
| `probLgStr` | 210 | 210 | 0 |

**210 of 210 exact, in both the Java and the JavaScript.** Cross-checking the two implementations against each other and against enumeration — 1,260 values across 210 states — gives zero disagreements anywhere. Every closed-form branch matches the true probability to the last decimal place.

## If this is ever revisited

A precomputed exact table is the best of both: **462 entries** covering every keep-multiset × reroll-count combination, built at startup with ~23,000 enumerations in about 2 ms, then O(1) lookups. It benchmarks at 1.10× the current cost — statistically the same speed — while being exact by construction and replacing roughly 400 lines of hand-derived branching. The hand-derived tables were the right call for a 2022 coursework project; a lookup table is the easier thing to trust now.

---

# Bot performance diagnostic

**Method.** This measures the **Easy** bot — the original coursework logic. Every figure below comes from a **single run of one harness with one seed** — 40,000 games × 3 bots = **120,000 completed scorecards**, standard error ±0.14. The baselines are measured in the same run against the same scoring engine. Category statistics come from an instrumented `performScore` (a move log) rather than end-of-game state, which is unreliable for 3-/4-of-a-kind for the reason given above, and which also credits the joker bonus to whichever box received the dice.

Experiments are a separate matched-seed sweep of 20,000 games per variant and are reported as **deltas only**, so there is exactly one absolute mean in this document.

Integrity checks on the main run: 0 unfinished games, 0 cards failing to fill all 13 boxes, and `yaht` only ever observed as 0 or 100.

Everything here is reproducible — `node analysis/analyze.js` on the default seed regenerates these numbers exactly. See [Reproducing this](#reproducing-this).

## Headline

| | |
|---|---|
| mean | **210.3** |
| median | 198 |
| standard deviation | 50.2 |
| min / max | 82 / 696 |
| p5 / p25 / p75 / p95 / p99 | 151 / 180 / 233 / 318 / 395 |

```
175-199 ####################################################################   33.8%
200-224 ####################################                    18.0%
225-249 ################################                        16.2%
150-174 ###########################                             13.3%
250-274 #############                                            6.6%
125-149 ########                                                 3.9%
```

The mean sits 12 points above the median — a right skew driven entirely by multi-Yahtzee games. **51.8% of all games land in the 175–225 band**, and only 4.7% finish below 150.

## Where it stands

| Strategy | Mean | Median | SD | Max |
|---|---|---|---|---|
| Take best category, never reroll | 115.3 | 114 | 28.6 | 283 |
| Keep the most common face, reroll the rest | 152.6 | 135 | 69.0 | 679 |
| **This bot** | **210.3** | 198 | 50.2 | 696 |
| Published optimal solitaire Yahtzee | ~254.6 | — | — | — |

+83% over never rerolling, +38% over the classic keep-the-common-face heuristic, and **82.6% of optimal**. For hand-derived probability tables, that's respectable.

The 254.6 benchmark assumes standard rules including the joker rule, which the web version now implements, so this is a like-for-like comparison.

## Where the points come from

Average contribution per card:

```
Upper section     48.1  ################################################ 22.9%
Large straight    35.9  #################################### 17.1%
Small straight    29.9  ############################## 14.2%
Full house        24.4  ######################## 11.6%
Chance            18.0  ################## 8.6%
3 of a kind       16.0  ################ 7.6%
Yahtzee box       15.5  ################ 7.4%
4 of a kind       12.9  ############# 6.1%
Yahtzee bonus      6.0  ###### 2.8%
Upper bonus        3.6  #### 1.7%
```

These sum to 210.3, which is the headline mean exactly — a useful check that nothing is double-counted or missing.

## Per category

Every box is taken exactly once per card, so all thirteen show 120,000 picks. Points exclude the joker bonus, which is credited separately above.

| Category | Scored % | Avg points when scored | Avg round taken |
|---|---|---|---|
| Chance | 100.0% | 18.0 | 3.9 |
| 3 of a kind | 99.9% | 16.0 | 4.8 |
| Small straight | 99.7% | 30.0 | 4.2 |
| Sixes | 98.8% | 16.5 | 5.2 |
| Full house | 97.6% | 25.0 | 5.0 |
| Fives | 96.2% | 11.8 | 6.7 |
| Fours | 94.7% | 9.7 | 7.1 |
| Threes | 93.2% | 7.5 | 7.3 |
| Large straight | 89.7% | 40.0 | 6.6 |
| 4 of a kind | 80.4% | 16.0 | 9.0 |
| Twos | 81.0% | 3.9 | 9.9 |
| Ones | 61.1% | 1.9 | 10.4 |
| Yahtzee | 31.1% | 50.0 | 10.8 |

---

## Strengths

**It almost never wastes a guaranteed-value box.** Small Straight 99.7%, Full House 97.6%, 3-of-a-kind 99.9%, Chance 100%. Large Straight — the hardest fixed-value box in the game — lands 89.7% of the time. This is the bot's real strength: when a fixed-value combo is achievable, it spots it and banks it. Plenty of casual human players scratch Large Straight far more often than 10%.

**The probability engine is real, and its key constant is genuinely well-tuned.** The most important magic number in the bot is `maxPoint < 24` — the threshold that decides whether to abandon a guaranteed score and chase a probabilistic combo instead. Sweeping it, as a change against the current setting:

| Threshold | Effect |
|---|---|
| 12 | **−8.4** ±0.54 |
| 18 | **−4.2** ±0.55 |
| **24 (current)** | base |
| 30 | **−5.6** ±0.57 |
| 40 | **−5.8** ±0.57 |
| 60 | **−20.9** ±0.55 |

That is a clean optimum. Moving it 6 in *either* direction costs 4–6 points, well outside the confidence interval. Not luck.

**It's consistent.** p5 is 151, so blow-ups are rare. For a multiplayer party game, that reliability is arguably worth more than a higher ceiling.

## Weaknesses

**1. The upper bonus is a structural blind spot — the single biggest leak.**

The bot earns the 35-point bonus in **10.2% of games**. Mean upper section is 48.1 against the 63 required, and it is below par on *every single box*:

| Box | Avg | Par (three of that face) | Gap |
|---|---|---|---|
| Ones | 1.2 | 3 | −1.8 |
| Twos | 3.1 | 6 | −2.9 |
| Threes | 7.0 | 9 | −2.0 |
| Fours | 9.2 | 12 | −2.8 |
| Fives | 11.4 | 15 | −3.6 |
| Sixes | 16.3 | 18 | −1.7 |

Only 10.9% of games get within 6 points of the bonus. Cards that earn it average **271.7**; cards that don't average **203.3**.

The cause is that the bot chooses categories **greedily by raw points**, and upper boxes almost always lose that comparison — three sixes is 18, but Small Straight is 30 and Full House is 25. The upper section perpetually comes second, and nothing in the decision logic ever asks "how close am I to 63?"

**2. Ones and Twos are dump boxes, which kills the bonus before it starts.** Ones scores zero in 38.9% of games (average round 10.4); Twos in 19.0%. Dumping in Ones is normal Yahtzee practice — but conceding ~2 points in Ones and ~3 in Twos means every remaining box has to run *above* par to reach 63, and this bot runs below par everywhere. The bonus was never reachable.

**3. Yahtzee is the second dump box.** The box is scratched to zero in **68.8%** of games, at an average round of 10.8. Together with Ones, that produces **1.77 zero-scoring turns per card** (13.6% of all turns). Only 7.3% of cards escape with no zeros at all.

**4. It fills boxes in descending point order, not by scarcity.** Chance goes at round **3.9** for **18.0 points** — barely above the 17.5 expected from five cold dice, and that's *after* rerolls. Chance is the one box that absorbs any hand; spending it in round 4 on a league-average roll throws away its insurance value. 3-of-a-kind goes at round 4.8 for 16.0, also roughly a random hand's sum. There is no notion of "which box will be hardest to fill later."

**5. 4-of-a-kind underperforms.** Only 80.4% filled — the most-scratched box after Yahtzee, at 19.6% — and just 16.0 points when it does hit, about four 3s.

**6. Safe but capped.** The distribution is tight and the tail is thin. Nothing in the logic responds to the score situation — the bot plays identically whether it's 80 points ahead or behind.

## Two tuning changes that stuck

Both were found by matched-seed comparison and confirmed on independent seeds.

**1. It no longer gives up on the upper section.** The old rule stopped chasing after round 9 unless the upper total was already past 50 (`basicTotal > 50 + 0 * (game.round - 10)` — the `0 *` neutralised what was meant to be a sliding threshold). Every version of quitting early measured worse, monotonically: quit after round 5 is −2.2 to −3.6, after round 7 is −0.6 to −1.8, after round 9 is ~0, never quitting is **+0.40** ±0.33. Scorecard-based rules lose too, including "stop once par on the remaining boxes can't reach 63" at −0.78.

Reachability is simply the wrong question: chasing sixes pays even when the bonus is dead, because three 6s is 18 points regardless. For scale, the chase block as a whole is worth **8.6 points** — deciding *when* to stop moves things by well under 1. It is an on/off feature, not a tunable one.

**2. It prices in the upper bonus.** Category choice used to rank on raw points, so Small Straight (30) always beat Sixes (18) even when those 18 completed 63 and unlocked 35. A box that finishes the upper section is now worth **points + 35**. Up to round 11 that premium may not outrank a Yahtzee in hand — the hardest box to refill, and without the exception the Yahtzee rate drops 31.2% → 30.7%. Full House and Small Straight need no exception; 35 already beats them.

Worth **+0.54 to +0.69**, significant on two independent seeds. Protecting Yahtzee is never *distinguishable* from protecting nothing (+0.02, +0.05) but is positive in every run, so it is kept as the cheaper of two errors. Extending protection to 4-of-a-kind and Large Straight consistently did worse — those refill 80% and 90% of the time, so shielding them suppresses the premium too often. Bonus rate went **8.3% → 10.2%**.

## Things that were tested and did *not* help

| Experiment | Effect |
|---|---|
| Hold Chance until round 10 unless the roll is ≥23 | −0.6 ±0.55 |
| Give up on the upper once par can't reach 63 | −0.78 ±0.42 |
| Same, weighting 5s and 6s at four rather than three | −0.20 ±0.42 |
| Protect 4-of-a-kind / Large Straight from the bonus premium | −0.1 to −0.4 |

The Chance result is the interesting negative: the greedy ordering isn't costing points *given the rest of the strategy*, because the bot has no plan to exploit a saved Chance box either. The symptom can't be fixed without fixing the model.


## Summary

The bot is a **solid combo-hunter with a very short planning horizon**. Its probability tables are exact and its risk threshold sits on a real optimum — that's what earns the 82% of optimal. What it lacks is state beyond the current roll: no box-scarcity valuation, no endgame planning, no response to the score situation. Almost every decision is still "what's worth the most points right now."

**A caution about the remaining headroom.** It's tempting to read "cards with the upper bonus average 271.7, cards without average 203.3" as a 68-point prize waiting to be claimed. It isn't. That gap is overwhelmingly *selection* — cards that earn the bonus are cards that rolled well, not cards that chose better. Pricing the bonus into the category choice, which is the direct causal version of that idea, was measured at **+0.5**, not +68.

That pattern held for everything tried here. The two changes that survived are worth about **+1 point combined**, and every other plausible-sounding tweak — sliding give-up thresholds, reachability rules, Chance timing, protecting more categories — landed inside the noise or below it. The bot appears to be near a local optimum for its architecture, and the honest conclusion is that the remaining 45 points to optimal are not reachable by tuning constants. They would need a different kind of player: one that evaluates a move by what the rest of the scorecard is worth afterwards, rather than by what the move scores now.
