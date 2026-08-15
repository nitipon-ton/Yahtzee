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
| `index.html`, `styles.css` | UI. |
| `vercel.json` | Static deploy config. |

### Running it

**Console (Java):**

```
javac *.java
java DiceMain
```

**Web:** open `index.html` directly, or serve the folder with any static server.

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

The consequence is that every turn fills exactly one box, so all 13 boxes are used in all 13 rounds — verified across 36,000 simulated scorecards, every one of which ended with 13 of 13 filled.

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

## And they are now exact

Because the functions depend only on the *multiset* of kept dice and the number of dice rerolled — verified by re-running 672 permuted placements with zero disagreements — the entire input space is just **210 states**. That's small enough to audit exhaustively against ground truth computed by enumeration, using the game's own pattern predicates.

| Function | States | Exact | Wrong |
|---|---|---|---|
| `probTOAK` | 210 | 210 | 0 |
| `probFOAK` | 210 | 210 | 0 |
| `probFH` | 210 | 210 | 0 |
| `probYaht` | 210 | 210 | 0 |
| `probSmStr` | 210 | 210 | 0 |
| `probLgStr` | 210 | 210 | 0 |

**210 of 210 exact, in both the Java and the JavaScript.** Cross-checking the two implementations against each other and against enumeration — 1,260 values across 210 states — gives zero disagreements anywhere.

### Accuracy is not the same as strength

The audit turned up a handful of incorrect entries in the two straight functions, since corrected in both implementations. Replaying 8,000 games × 3 bots before and after the correction:

| Variant | Mean | Median | SD | Lg straight filled |
|---|---|---|---|---|
| Before | 208.4 | 197 | 46.4 | 89.4% |
| After | 208.4 | 197 | 46.4 | 89.4% |

**+0.00 points (±0.83 at 95% confidence)** across 24,000 scorecards, and no measurable change in speed.

That is not a surprise once you look at how the numbers are used. The bot only ever *ranks* with them — it picks the mask with the highest probability, then compares the best-per-category across categories. A probability that is wrong in magnitude but still the largest in its comparison changes nothing. Getting the tables exact is worth doing because a probability function should be right, not because it wins games.

## If this is ever revisited

A precomputed exact table is the best of both: **462 entries** covering every keep-multiset × reroll-count combination, built at startup with ~23,000 enumerations in about 2 ms, then O(1) lookups. It benchmarks at 1.10× the current cost — statistically the same speed — while being exact by construction and replacing roughly 400 lines of hand-derived branching. The hand-derived tables were the right call for a 2022 coursework project; a lookup table is the easier thing to trust now.

---

# Bot performance diagnostic

**Method.** 12,000 games × 3 bots = **36,000 completed scorecards**, plus separate 6,000-game runs per experiment. All variants share one seeded PRNG and the identical scoring engine, so comparisons are like-for-like. Category statistics come from an instrumented `performScore` (a move log), not from end-of-game state — end-state is unreliable for 3-/4-of-a-kind for the reason described above. Measured with a headless simulation harness, which is not included in this repo.

## Headline

| | |
|---|---|
| mean | **209.4** |
| median | 198 |
| standard deviation | 49.5 |
| min / max | 75 / 715 |
| p5 / p25 / p75 / p95 / p99 | 152 / 180 / 231 / 318 / 391 |

```
175-199 ###################################################################  33.7%
200-224 ####################################                  18.1%
225-249 ###############################                       15.8%
150-174 ############################                          13.9%
250-274 ############                                           6.3%
125-149 ########                                               3.8%
```

The mean sits 11 points above the median — a right skew driven entirely by multi-Yahtzee games. **51.8% of all games land in the 175–225 band**, and only 4.5% finish below 150.

## Where it stands

| Strategy | Mean | Median | SD | Max |
|---|---|---|---|---|
| Take best category, never reroll | 115.4 | 114 | 28.9 | 231 |
| Keep the most common face, reroll the rest | 152.0 | 135 | 67.7 | 597 |
| **This bot** | **209.7** | 198 | 50.0 | 729 |
| Published optimal solitaire Yahtzee | ~254.6 | — | — | — |

+82% over never rerolling, +38% over the classic keep-the-common-face heuristic, and **82.4% of optimal**. For hand-derived probability tables, that's respectable.

The 254.6 benchmark assumes standard rules including the joker rule, which the web version now implements, so this is a like-for-like comparison.

## Where the points come from

Average contribution per card:

```
Upper section     47.6  ################################################ 22.7%
Large straight    35.9  #################################### 17.1%
Small straight    29.9  ############################## 14.3%
Full house        24.4  ######################## 11.6%
Chance            18.1  ################## 8.6%
3 of a kind       16.1  ################ 7.7%
Yahtzee box       15.7  ################ 7.5%
4 of a kind       13.2  ############# 6.3%
Yahtzee bonus      6.1  ###### 2.9%
Upper bonus        2.8  ### 1.3%
```

## Per category

Every box is now taken exactly once per card, so "picks" is 36,000 across the board. Points exclude the joker bonus, which is credited separately.

| Category | Scored % | Avg points when scored | Avg round taken |
|---|---|---|---|
| Chance | 100.0% | 18.1 | 3.9 |
| 3 of a kind | 99.9% | 16.1 | 4.8 |
| Small straight | 99.8% | 30.0 | 4.2 |
| Sixes | 98.2% | 16.4 | 5.2 |
| Full house | 97.7% | 25.0 | 5.0 |
| Fives | 95.8% | 11.7 | 6.7 |
| Fours | 94.6% | 9.6 | 7.1 |
| Threes | 92.6% | 7.4 | 7.3 |
| Large straight | 89.8% | 40.0 | 6.6 |
| 4 of a kind | 81.2% | 16.2 | 9.0 |
| Twos | 80.7% | 3.9 | 9.9 |
| Ones | 61.3% | 1.9 | 10.4 |
| Yahtzee | 31.4% | 50.0 | 10.8 |

---

## Strengths

**It almost never wastes a guaranteed-value box.** Small Straight 99.8%, Full House 97.7%, 3-of-a-kind 99.9%, Chance 100%. Large Straight — the hardest fixed-value box in the game — lands 89.8% of the time. This is the bot's real strength: when a fixed-value combo is achievable, it spots it and banks it. Plenty of casual human players scratch Large Straight far more often than 10%.

**The probability engine is real, and its key constant is genuinely well-tuned.** The most important magic number in the bot is `maxPoint < 24` — the threshold that decides whether to abandon a guaranteed score and chase a probabilistic combo instead. Sweeping it:

| Threshold | Mean | vs current |
|---|---|---|
| 12 | 201.9 | −7.3 |
| 18 | 205.6 | −3.6 |
| **24 (current)** | **209.2** | base |
| 30 | 203.2 | −5.9 |
| 40 | 204.1 | −5.1 |
| 60 | 189.8 | −19.4 |

That is a clean optimum. Moving it 6 in *either* direction costs 4–6 points. Not luck.

**It's consistent.** p5 is 151, so blow-ups are rare. For a multiplayer party game, that reliability is arguably worth more than a higher ceiling.

## Weaknesses

**1. The upper bonus is a structural blind spot — the single biggest leak.**

The bot earns the 35-point bonus in **7.9% of games**. Mean upper section is 47.6 against the 63 required, and it is below par on *every single box*:

| Box | Avg | Par (three of that face) | Gap |
|---|---|---|---|
| Ones | 1.2 | 3 | −1.8 |
| Twos | 3.1 | 6 | −2.9 |
| Threes | 6.9 | 9 | −2.1 |
| Fours | 9.1 | 12 | −2.9 |
| Fives | 11.2 | 15 | −3.8 |
| Sixes | 16.0 | 18 | −2.0 |

Only 11.9% of games get within 6 points of the bonus. Cards that earn it average **275.1**; cards that don't average **204.0**.

The cause is that the bot chooses categories **greedily by raw points**, and upper boxes almost always lose that comparison — three sixes is 18, but Small Straight is 30 and Full House is 25. The upper section perpetually comes second, and nothing in the decision logic ever asks "how close am I to 63?"

**2. Ones and Twos are dump boxes, which kills the bonus before it starts.** Ones scores zero in 38.7% of games (average round 10.4); Twos in 19.4%. Dumping in Ones is normal Yahtzee practice — but conceding ~2 points in Ones and ~3 in Twos means every remaining box has to run *above* par to reach 63, and this bot runs below par everywhere. The bonus was never reachable.

**3. Yahtzee is the second dump box.** The box is scratched to zero in **68.6%** of games, at an average round of 10.8. Together with Ones, that produces **1.77 zero-scoring turns per card** (13.6% of all turns). Only 7.3% of cards escape with no zeros at all.

**4. It fills boxes in descending point order, not by scarcity.** Chance goes at round **3.9** for **18.1 points** — barely above the 17.5 expected from five cold dice, and that's *after* rerolls. Chance is the one box that absorbs any hand; spending it in round 4 on a league-average roll throws away its insurance value. 3-of-a-kind goes at round 4.8 for 16.1, also roughly a random hand's sum. There is no notion of "which box will be hardest to fill later."

**5. 4-of-a-kind underperforms.** Only 81.2% filled — the most-scratched box after Yahtzee, at 18.8% — and just 16.2 points when it does hit, about four 3s.

**6. Safe but capped.** The distribution is tight and the tail is thin. Nothing in the logic responds to the score situation — the bot plays identically whether it's 80 points ahead or behind.

## Things that were tested and did *not* help

Both of these were plausible hypotheses that the data refuted:

| Experiment | Result |
|---|---|
| Remove the "give up on the upper section after round 9" gate | **+0.4** — nothing |
| Hold Chance until round 10 unless the roll is ≥23 | **−0.5** — nothing |

The first was the leading suspect for the upper-bonus problem. It isn't. That gate — `basicTotal > 50 + 0 * (game.round - 10)` — is effectively inert: the `0 *` neutralises what was clearly meant to be a sliding threshold, reducing it to a flat `> 50`, and removing the condition entirely changes nothing measurable.

The Chance result is the more interesting negative: the greedy ordering isn't costing points *given the rest of the strategy*, because the bot has no plan to exploit a saved Chance box either. The symptom can't be fixed without fixing the model.

## Summary

The bot is a **solid combo-hunter with no long game**. Its probability tables are accurate and its risk threshold sits on a real optimum — that's what earns the 82% of optimal. What it lacks is any state beyond the current roll: no bonus tracking, no box-scarcity valuation, no endgame planning. Every decision is "what's worth the most points right now."

The one change with real headroom is upper-bonus awareness — a bonus-progress premium on upper boxes when the card is on pace for 63. The ceiling on that alone is roughly **+15 to +30 points per game**, which would put the bot in the 225–240 range. Everything else probed is already at or near its local optimum.
