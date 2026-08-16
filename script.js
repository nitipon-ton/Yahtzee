const playerCountInput = document.getElementById('playerCount');
const playerOptions = document.getElementById('playerOptions');
const startGameButton = document.getElementById('startGameButton');
const backToSetupButton = document.getElementById('backToSetup');
const newGameButton = document.getElementById('newGameButton');
const setupScreen = document.getElementById('setupScreen');
const gameScreen = document.getElementById('gameScreen');
const gameOverScreen = document.getElementById('gameOverScreen');
const turnTitle = document.getElementById('turnTitle');
const gameStatus = document.getElementById('gameStatus');
const playerType = document.getElementById('playerType');
const playerScore = document.getElementById('playerScore');
const playerTotal = document.getElementById('playerTotal');
const diceRow = document.getElementById('diceRow');
const rollButton = document.getElementById('rollButton');
const forfeitButton = document.getElementById('forfeitButton');
const optionsList = document.getElementById('optionsList');
const suggestionText = document.getElementById('suggestionText');
const botModeSelect = document.getElementById('botModeSelect');
const botModeHint = document.getElementById('botModeHint');
const difficultySelect = document.getElementById('difficultySelect');
const difficultyHint = document.getElementById('difficultyHint');
const roundBotLogPanel = document.getElementById('roundBotLogPanel');
const roundBotLogContent = document.getElementById('roundBotLogContent');
const scoreboardTable = document.getElementById('scoreboardTable');
const finalSummary = document.getElementById('finalSummary');
const helpButton = document.getElementById('helpButton');
const closeHelpButton = document.getElementById('closeHelpButton');
const helpPanel = document.getElementById('helpPanel');
const backButtons = [backToSetupButton, newGameButton];

const BOT_MODE = {
  // Pause between every single bot action, so each roll and reroll is readable.
  WATCH: 'watch',
  // Pause once per whole bot turn: quick, but you still see the turns land.
  FAST: 'fast',
  // Same pacing as FAST, but nothing is logged and no log is shown afterwards.
  SILENT: 'silent',
};

const BOT_STEP_DELAY_MS = 400;
// Per-turn beat, tuned for a 3-bot table and scaled down for bigger ones so a
// full round takes about the same time no matter how many bots are playing.
const BOT_TURN_DELAY_MS = 140;
const BOT_TURN_MIN_DELAY_MS = 45;

const BOT_MODE_HINTS = {
  [BOT_MODE.WATCH]: 'Every roll and reroll is shown as it happens. Slowest, but you can follow the reasoning.',
  [BOT_MODE.FAST]: 'Each bot turn lands in one beat. The full decision log is available once the game ends.',
  [BOT_MODE.SILENT]: 'Same speed as above, with no decision log recorded.',
};

const DIFFICULTY = {
  // The original coursework logic: probability tables and pattern chasing.
  EASY: 'easy',
  // hard-bot.js: expectimax over rerolls, categories priced by opportunity cost.
  HARD: 'hard',
};

const DIFFICULTY_HINTS = {
  [DIFFICULTY.EASY]: 'Chases whichever scoring pattern is most likely, using the hand-derived probability tables.',
  [DIFFICULTY.HARD]: 'Searches every reroll two rolls deep and values a box by what it would be worth if saved. Averages about 27 points more per game.',
};

let game = null;
let selectedDice = [false, false, false, false, false];
let difficulty = DIFFICULTY.EASY;
let botMode = BOT_MODE.FAST;
let botTimeout = null;
let roundBotLog = [];

// A bot turn is at most 3 steps; this is a runaway guard, not a real limit.
const MAX_BOT_STEPS = 1000;

const CATEGORY = {
  BASIC_1: 1,
  BASIC_2: 2,
  BASIC_3: 3,
  BASIC_4: 4,
  BASIC_5: 5,
  BASIC_6: 6,
  YAHTZEE: 7,
  FOUR_KIND: 8,
  THREE_KIND: 9,
  CHANCE: 11,
  SMALL_STRAIGHT: 12,
  LARGE_STRAIGHT: 13,
  FULL_HOUSE: 14,
  END_TURN: 15,
  FORFEIT: 99,
};

const CATEGORY_LABELS = {
  1: 'Ones',
  2: 'Twos',
  3: 'Threes',
  4: 'Fours',
  5: 'Fives',
  6: 'Sixes',
  7: 'Yahtzee',
  8: '4 of a kind',
  9: '3 of a kind',
  11: 'Chance',
  12: 'Small straight',
  13: 'Large straight',
  14: 'Full house',
  15: 'End turn',
};

// Awarded once the upper section reaches 63. checkScoreCard applies it; the bot
// also has to price it in when choosing a category, or it will take a 30-point
// straight over the 18 points that would have unlocked 35.
const UPPER_BONUS = 35;

const DICE_LABELS = ['A', 'B', 'C', 'D', 'E'];
const DICE_EMOJI = ['1️⃣', '2️⃣', '3️⃣', '4️⃣', '5️⃣', '6️⃣'];

const HUMAN_LABELS = {
  '3 of a kind': 'Three of a Kind',
  '4 of a kind': 'Four of a Kind',
  'Full house': 'Full House',
  'Small straight': 'Small Straight',
  'Large straight': 'Large Straight',
  Yahtzee: 'Yahtzee',
};

const LGSTR_SUMSQ_6 = new Set([5, 10, 17, 26, 40, 45, 52, 61]);
const SMSTR_SUMSQ_6 = new Set([2, 72]);
const SMSTR_SUMSQ_12 = new Set([8, 50, 40, 26, 37]);
const SMSTR_SUMSQ_18 = new Set([18, 32]);
const SMSTR_SUMSQ_30 = new Set([61, 5, 29]);
const SMSTR_SUMSQ_36 = new Set([52, 10, 45, 17]);
const SMSTR_SUMSQ_54 = new Set([41, 13, 34, 20]);
const SMSTR_R2_11 = new Set([14, 77, 21, 70, 38, 45]);
const SMSTR_R2_4 = new Set([35, 42, 49, 56, 46, 53]);
const SMSTR_R2_13 = new Set([26, 61]);
const SMSTR_R2_2 = new Set([30, 65, 41, 62]);
const SMSTR_R2_20 = new Set([29, 50]);

// Reused across probability evaluations to avoid per-call allocations; never
// held across calls, so a single shared buffer is safe.
const decodeScratch = [0, 0, 0, 0, 0];
const countScratch = [0, 0, 0, 0, 0, 0];

class Dice {
  roll() {
    return Math.floor(Math.random() * 6) + 1;
  }
}

const Helper = {
  sumArr(arr) {
    let sum = 0;
    for (let i = 0; i < arr.length; i += 1) {
      sum += arr[i];
    }
    return sum;
  },
  maxArr(arr) {
    let max = 0;
    for (let i = 0; i < arr.length; i += 1) {
      if (arr[i] > max) {
        max = arr[i];
      }
    }
    return max;
  },
};

class Player {
  constructor(name, isBot) {
    this.name = name;
    this.bot = isBot;
    this.cheat = true;
    this.totalscore = 0;
    this.life = 1;
    this.score = 0;
    this.roll_left = 3;
    this.yaht = 1;
    this.isAvailAdv = [1, 1, 1, 1, 1];
    this.chanAvail = true;
    this.isAvailBasic = [true, true, true, true, true, true];
    this.pntsToak = 0;
    this.pntsFoak = 0;
    this.pntsChan = 0;
    this.bonus = 0;
    this.yahtBo = 0;
    this.pntsBasic = [0, 0, 0, 0, 0, 0];
    this.arrVal = [0, 0, 0, 0, 0];
    this.faceCounter = [0, 0, 0, 0, 0, 0];
    this.rollDecision = 0;
    this.botLog = [];
    this.dice = new Dice();
  }

  logBot(message) {
    this.botLog.push(message);
  }

  updateFaceCounter() {
    this.faceCounter.fill(0);
    for (let i = 0; i < 5; i += 1) {
      if (this.arrVal[i] > 0) {
        this.faceCounter[this.arrVal[i] - 1] += 1;
      }
    }
  }

  resetForNextRound() {
    if (this.life === 1 || this.life === 2) {
      this.totalscore += this.score;
      this.score = 0;
      this.roll_left = 3;
      this.arrVal = [0, 0, 0, 0, 0];
      this.faceCounter = [0, 0, 0, 0, 0, 0];
      this.rollDecision = 0;
      selectedDice = [false, false, false, false, false];
    }
  }

  rollDice() {
    if (this.life !== 1) {
      return;
    }

    if (this.roll_left < 3 && this.roll_left >= 0) {
      this.faceCounter.fill(0);
      let rollDeciTemp = this.rollDecision;
      let digit = 160;
      for (let i = 4; i >= 0; i -= 1) {
        if (rollDeciTemp >= digit) {
          rollDeciTemp -= digit;
          this.arrVal[i] = this.dice.roll();
        }
        digit /= 2;
      }
      this.updateFaceCounter();
    } else if (this.roll_left === 3) {
      this.roll_left -= 1;
      for (let i = 0; i < 5; i += 1) {
        this.arrVal[i] = this.dice.roll();
      }
      this.updateFaceCounter();
    }
  }

  hasChoice() {
    if (this.chanAvail) {
      return true;
    }
    if (this.yaht === 1) {
      return true;
    }
    for (let i = 0; i < 6; i += 1) {
      if (this.isAvailBasic[i]) {
        return true;
      }
    }
    for (let i = 0; i < 5; i += 1) {
      if (this.isAvailAdv[i] > 0) {
        return true;
      }
    }
    return false;
  }

  isYahtzee() {
    return this.arrVal.every((value) => value === this.arrVal[0]);
  }

  // isYahtzee() also reports true for the unrolled [0,0,0,0,0] hand, which is
  // harmless where it is already used but would wrongly trigger the joker path.
  isYahtzeeHand() {
    return this.arrVal[0] > 0 && this.isYahtzee();
  }

  // A joker turn is a Yahtzee rolled when the Yahtzee box is already filled —
  // with 50 (bonus applies) or with a scratched 0 (no bonus, joker still does).
  isJokerTurn() {
    return this.isYahtzeeHand() && this.yaht !== 1;
  }

  // Official joker placement, in priority order:
  //   'upper-forced' - the matching upper box is open, so it must be used
  //   'lower-free'   - that box is taken; any open lower box accepts the joker
  //   'upper-zero'   - lower section full too, so a zero goes in an upper box
  jokerStage() {
    if (this.isAvailBasic[this.arrVal[0] - 1]) {
      return 'upper-forced';
    }
    if (this.chanAvail || this.isAvailAdv.some((value) => value > 0)) {
      return 'lower-free';
    }
    return 'upper-zero';
  }

  // Rewrites the option list for a joker turn: restricts it to legal placements
  // and prices the lower boxes at their joker value rather than their pattern.
  applyJokerRules(options) {
    const face = this.arrVal[0];
    const stage = this.jokerStage();
    const sum = Helper.sumArr(this.arrVal);
    const jokerValue = {
      [CATEGORY.THREE_KIND]: sum,
      [CATEGORY.FOUR_KIND]: sum,
      [CATEGORY.FULL_HOUSE]: 25,
      [CATEGORY.SMALL_STRAIGHT]: 30,
      [CATEGORY.LARGE_STRAIGHT]: 40,
      [CATEGORY.CHANCE]: sum,
    };

    for (const option of options) {
      if (option.id === CATEGORY.END_TURN) {
        continue;
      }
      if (option.id === CATEGORY.YAHTZEE) {
        option.available = false;
        option.points = 0;
      } else if (stage === 'upper-forced') {
        option.available = option.id === face;
        option.points = option.id === face ? sum : 0;
      } else if (stage === 'lower-free') {
        const value = jokerValue[option.id];
        if (value === undefined) {
          option.available = false;
          option.points = 0;
        } else {
          option.points = option.available ? value : 0;
        }
      } else {
        option.available = option.id >= 1 && option.id <= 6 && this.isAvailBasic[option.id - 1];
        option.points = 0;
      }
    }
    return options;
  }

  // Scores a joker turn. The bonus is awarded for the turn itself, then the
  // dice are placed in the chosen box at joker value.
  scoreJoker(choice) {
    const face = this.arrVal[0];
    const stage = this.jokerStage();
    const sum = Helper.sumArr(this.arrVal);

    // Only a Yahtzee box holding 50 earns the bonus; a scratched 0 earns nothing.
    if (this.yaht === 0) {
      this.score += 100;
      this.yahtBo += 100;
    }

    if (stage === 'upper-forced') {
      if (choice === face) {
        this.pntsBasic[face - 1] = sum;
        this.score += sum;
        this.isAvailBasic[face - 1] = false;
      }
    } else if (stage === 'lower-free') {
      if (choice === CATEGORY.THREE_KIND && this.isAvailAdv[0] > 0) {
        this.pntsToak = sum;
        this.score += sum;
        this.isAvailAdv[0] -= 1;
      } else if (choice === CATEGORY.FOUR_KIND && this.isAvailAdv[1] > 0) {
        this.pntsFoak = sum;
        this.score += sum;
        this.isAvailAdv[1] -= 1;
      } else if (choice === CATEGORY.FULL_HOUSE && this.isAvailAdv[2] > 0) {
        this.score += 25;
        this.isAvailAdv[2] -= 1;
      } else if (choice === CATEGORY.SMALL_STRAIGHT && this.isAvailAdv[3] > 0) {
        this.score += 30;
        this.isAvailAdv[3] -= 1;
      } else if (choice === CATEGORY.LARGE_STRAIGHT && this.isAvailAdv[4] > 0) {
        this.score += 40;
        this.isAvailAdv[4] -= 1;
      } else if (choice === CATEGORY.CHANCE && this.chanAvail) {
        this.pntsChan = sum;
        this.score += sum;
        this.chanAvail = false;
      }
    } else if (choice >= 1 && choice <= 6 && this.isAvailBasic[choice - 1]) {
      this.isAvailBasic[choice - 1] = false;
    }

    this.roll_left = -1;
  }

  smallStraightPresent() {
    return (
      this.faceCounter[0] * this.faceCounter[1] * this.faceCounter[2] * this.faceCounter[3] +
      this.faceCounter[1] * this.faceCounter[2] * this.faceCounter[3] * this.faceCounter[4] +
      this.faceCounter[2] * this.faceCounter[3] * this.faceCounter[4] * this.faceCounter[5] >
      0
    );
  }

  largeStraightPresent() {
    return (
      this.faceCounter[1] === 1 &&
      this.faceCounter[2] === 1 &&
      this.faceCounter[3] === 1 &&
      this.faceCounter[4] === 1
    );
  }

  fullHousePresent() {
    return (
      this.faceCounter[0] * this.faceCounter[0] +
      this.faceCounter[1] * this.faceCounter[1] +
      this.faceCounter[2] * this.faceCounter[2] +
      this.faceCounter[3] * this.faceCounter[3] +
      this.faceCounter[4] * this.faceCounter[4] +
      this.faceCounter[5] * this.faceCounter[5] ===
      13
    );
  }

  checkScoreCard() {
    if (Helper.sumArr(this.pntsBasic) >= 63 && !this.gotbonus) {
      this.bonus = 35;
      this.totalscore += 35;
      this.gotbonus = true;
    }
    const basicDone = this.pntsBasic.every((value) => value !== 0);
    const advDone = this.isAvailAdv.every((value) => value <= 0);
    if (advDone && !this.chanAvail && basicDone && this.yaht !== 1) {
      this.life = 2;
    }
  }

  calcRerollMask() {
    this.rollDecision = selectedDice.reduce((mask, selected, index) => {
      return selected ? mask + 10 * 2 ** index : mask;
    }, 0);
  }

  maskToDiceLetters(mask) {
    const letters = [];
    let digit = 160;
    for (let i = 4; i >= 0; i -= 1) {
      if (mask >= digit) {
        letters.unshift(DICE_LABELS[i]);
        mask -= digit;
      }
      digit /= 2;
    }
    return letters;
  }

  getAvailableOptions() {
    const options = [];
    const diceSum = Helper.sumArr(this.arrVal);
    const maxDup = Helper.maxArr(this.faceCounter);
    for (let i = 1; i <= 6; i += 1) {
      options.push({
        id: i,
        label: CATEGORY_LABELS[i],
        points: this.isAvailBasic[i - 1] ? i * this.faceCounter[i - 1] : 0,
        available: this.isAvailBasic[i - 1],
      });
    }

    // The box itself is a one-shot: 50 or a scratched 0, then closed for good.
    // Further Yahtzees are worth 100 through the joker path, not through here.
    options.push({
      id: CATEGORY.YAHTZEE,
      label: CATEGORY_LABELS[7],
      points: this.yaht === 1 && this.isYahtzeeHand() ? 50 : 0,
      available: this.yaht === 1,
    });

    options.push({
      id: CATEGORY.FOUR_KIND,
      label: CATEGORY_LABELS[8],
      points: this.isAvailAdv[1] > 0 && maxDup >= 4 ? diceSum : 0,
      available: this.isAvailAdv[1] > 0,
    });

    options.push({
      id: CATEGORY.THREE_KIND,
      label: CATEGORY_LABELS[9],
      points: this.isAvailAdv[0] > 0 && maxDup >= 3 ? diceSum : 0,
      available: this.isAvailAdv[0] > 0,
    });

    options.push({
      id: CATEGORY.CHANCE,
      label: CATEGORY_LABELS[11],
      points: this.chanAvail ? diceSum : 0,
      available: this.chanAvail,
    });

    options.push({
      id: CATEGORY.SMALL_STRAIGHT,
      label: CATEGORY_LABELS[12],
      points: this.isAvailAdv[3] > 0 && this.smallStraightPresent() ? 30 : 0,
      available: this.isAvailAdv[3] > 0,
    });

    options.push({
      id: CATEGORY.LARGE_STRAIGHT,
      label: CATEGORY_LABELS[13],
      points: this.isAvailAdv[4] > 0 && this.largeStraightPresent() ? 40 : 0,
      available: this.isAvailAdv[4] > 0,
    });

    options.push({
      id: CATEGORY.FULL_HOUSE,
      label: CATEGORY_LABELS[14],
      points: this.isAvailAdv[2] > 0 && this.fullHousePresent() ? 25 : 0,
      available: this.isAvailAdv[2] > 0,
    });

    if (!this.hasChoice() && this.roll_left === 0) {
      options.push({
        id: CATEGORY.END_TURN,
        label: 'Finish turn with no choice',
        points: 0,
        available: true,
      });
    }
    return this.isJokerTurn() ? this.applyJokerRules(options) : options;
  }

  performScore(choice) {
    if (choice === CATEGORY.FORFEIT) {
      this.life -= 1;
      return;
    }

    if (choice === CATEGORY.END_TURN) {
      this.roll_left = -1;
      return;
    }

    if (this.isJokerTurn()) {
      this.scoreJoker(choice);
      return;
    }

    if (choice >= 1 && choice <= 6) {
      if (this.isAvailBasic[choice - 1]) {
        this.pntsBasic[choice - 1] = choice * this.faceCounter[choice - 1];
        this.score += this.pntsBasic[choice - 1];
        this.isAvailBasic[choice - 1] = false;
      }
      this.roll_left = -1;
      return;
    }

    if (choice === CATEGORY.YAHTZEE) {
      // The box is filled exactly once. Reaching here with it already filled
      // would be a caller bug, so refuse rather than score it twice.
      if (this.yaht !== 1) {
        return;
      }
      if (this.isYahtzeeHand()) {
        this.score += 50;
        this.yaht = 0;
      } else {
        this.yaht = 100;
      }
      this.roll_left = -1;
      return;
    }

    if (choice === CATEGORY.FOUR_KIND) {
      if (Helper.maxArr(this.faceCounter) >= 4 && this.isAvailAdv[1] > 0) {
        this.pntsFoak = Helper.sumArr(this.arrVal);
        this.score += this.pntsFoak;
        this.isAvailAdv[1] -= 1;
      } else if (this.isAvailAdv[1] > 0) {
        this.isAvailAdv[1] = Math.max(0, this.isAvailAdv[1] - 2);
      }
      this.roll_left = -1;
      return;
    }

    if (choice === CATEGORY.THREE_KIND) {
      if (Helper.maxArr(this.faceCounter) >= 3 && this.isAvailAdv[0] > 0) {
        this.pntsToak = Helper.sumArr(this.arrVal);
        this.score += this.pntsToak;
        this.isAvailAdv[0] -= 1;
      } else if (this.isAvailAdv[0] > 0) {
        this.isAvailAdv[0] = Math.max(0, this.isAvailAdv[0] - 2);
      }
      this.roll_left = -1;
      return;
    }

    if (choice === CATEGORY.LARGE_STRAIGHT) {
      if (this.largeStraightPresent() && this.isAvailAdv[4] > 0) {
        this.score += 40;
        this.isAvailAdv[4] -= 1;
      } else if (this.isAvailAdv[4] > 0) {
        this.isAvailAdv[4] -= 2;
      }
      this.roll_left = -1;
      return;
    }

    if (choice === CATEGORY.SMALL_STRAIGHT) {
      if (this.smallStraightPresent() && this.isAvailAdv[3] > 0) {
        this.score += 30;
        this.isAvailAdv[3] -= 1;
      } else if (this.isAvailAdv[3] > 0) {
        this.isAvailAdv[3] -= 2;
      }
      this.roll_left = -1;
      return;
    }

    if (choice === CATEGORY.FULL_HOUSE) {
      if (this.fullHousePresent() && this.isAvailAdv[2] > 0) {
        this.score += 25;
        this.isAvailAdv[2] -= 1;
      } else if (this.isAvailAdv[2] > 0) {
        this.isAvailAdv[2] -= 2;
      }
      this.roll_left = -1;
      return;
    }

    if (choice === CATEGORY.CHANCE) {
      if (this.chanAvail) {
        this.pntsChan = Helper.sumArr(this.arrVal);
        this.score += this.pntsChan;
        this.chanAvail = false;
      }
      this.roll_left = -1;
    }
  }

  getProbabilityMask(index) {
    return 10 * index;
  }

  computeDecode(arrVal, userSimChoose) {
    const arr = decodeScratch;
    for (let i = 0; i < 5; i += 1) {
      arr[i] = arrVal[i];
    }
    let digit = 160;
    let count = 0;
    for (let i = 4; i >= 0; i -= 1) {
      if (userSimChoose >= digit) {
        userSimChoose -= digit;
        arr[i] = 0;
        count += 1;
      }
      digit /= 2;
    }
    return count;
  }

  probLgStr(a, userSimChoose) {
    let prob = 0;
    const numberofReroll = this.computeDecode(a, userSimChoose);
    const arr = decodeScratch;
    const count = countScratch;
    count.fill(0);
    let sum = 0;
    let sumofsquare = 0;
    let diffpair = 0;
    for (let i = 0; i <= 4; i += 1) {
      if (arr[i] > 0) {
        count[arr[i] - 1] += 1;
      }
    }
    for (let i = 0; i <= 4; i += 1) {
      sum += arr[i];
      sumofsquare += arr[i] * arr[i];
      for (let j = 0; j <= 4; j += 1) {
        if (arr[i] * arr[j] > 0 && arr[i] !== arr[j]) {
          diffpair += 1;
        }
      }
    }
    if (numberofReroll === 5) {
      prob = 40.0 / 1296.0;
    } else if (numberofReroll === 4) {
      if (sum === 1 || sum === 6) {
        prob = 4.0 / 216.0;
      } else {
        prob = 8.0 / 216.0;
      }
    } else if (numberofReroll === 3) {
      if (diffpair === 2) {
        if (sumofsquare === 37) {
          prob = 0.0;
        } else if (LGSTR_SUMSQ_6.has(sumofsquare)) {
          prob = 6.0 / 216.0;
        } else {
          prob = 12.0 / 216.0;
        }
      } else {
        prob = 0;
      }
    } else if (numberofReroll === 2) {
      if ((count[0] > 0 && count[5] > 0) || diffpair < 6) {
        prob = 0.0;
      } else if (count[0] + count[5] === 1) {
        prob = 2.0 / 36.0;
      } else {
        prob = 4.0 / 36.0;
      }
    } else if (numberofReroll === 1) {
      if (diffpair < 12 || (count[0] > 0 && count[5] > 0)) {
        prob = 0.0;
      } else if (count[0] === 0 && count[5] === 0) {
        // Holding 2-3-4-5: either a 1 or a 6 completes the straight, not just one of them.
        prob = 2.0 / 6.0;
      } else {
        prob = 1.0 / 6.0;
      }
    }
    return prob;
  }

  probFOAK(a, userSimChoose) {
    let prob = 0;
    const numberofReroll = this.computeDecode(a, userSimChoose);
    const arr = decodeScratch;
    let sum = 0;
    let sumofsquare = 0;
    let diffpair = 0;
    for (let i = 0; i <= 4; i += 1) {
      sum += arr[i];
      sumofsquare += arr[i] * arr[i];
      for (let j = 0; j <= 4; j += 1) {
        if (arr[i] * arr[j] > 0 && arr[i] !== arr[j]) {
          diffpair += 1;
        }
      }
    }
    if (numberofReroll === 5 || numberofReroll === 4) {
      prob = 26.0 / 1296.0;
    }
    if (numberofReroll === 3) {
      if (sum * sum === 2 * sumofsquare) {
        prob = 16.0 / 216.0;
      } else {
        prob = 2.0 / 216.0;
      }
    }
    if (numberofReroll === 2) {
      if (diffpair === 0) {
        prob = 11.0 / 36.0;
      } else if (diffpair === 4) {
        prob = 1.0 / 36.0;
      } else if (diffpair === 6) {
        prob = 0.0;
      }
    }
    if (numberofReroll === 1) {
      if (diffpair === 0) {
        prob = 1.0;
      } else if (diffpair === 6) {
        prob = 1.0 / 6.0;
      } else if (diffpair > 6) {
        prob = 0.0;
      }
    }
    return prob;
  }

  probYaht(a, userSimChoose) {
    let prob = 0.0;
    const numberofReroll = this.computeDecode(a, userSimChoose);
    const arr = decodeScratch;
    let diffpair = 0;
    for (let i = 0; i <= 4; i += 1) {
      for (let j = 0; j <= 4; j += 1) {
        if (arr[i] * arr[j] > 0 && arr[i] !== arr[j]) {
          diffpair += 1;
        }
      }
    }
    if (numberofReroll === 4 || numberofReroll === 5) {
      prob = 1.0 / 1296.0;
    } else {
      if (diffpair === 0) {
        prob = Math.pow(1.0 / 6.0, numberofReroll);
      } else {
        prob = 0;
      }
    }
    return prob;
  }

  probTOAK(a, userSimChoose) {
    let prob = 0;
    const numberofReroll = this.computeDecode(a, userSimChoose);
    const arr = decodeScratch;
    let diffpair = 0;
    for (let i = 0; i <= 4; i += 1) {
      for (let j = 0; j <= 4; j += 1) {
        if (arr[i] * arr[j] > 0 && arr[i] !== arr[j]) {
          diffpair += 1;
        }
      }
    }
    if (numberofReroll === 5 || numberofReroll === 4) {
      prob = 46.0 / 216.0;
    }
    if (numberofReroll === 3) {
      if (diffpair === 0) {
        prob = 96.0 / 216.0;
      } else if (diffpair === 2) {
        prob = 36.0 / 216.0;
      }
    }
    if (numberofReroll === 2) {
      if (diffpair === 0) {
        prob = 1.0;
      } else if (diffpair === 4) {
        prob = 12.0 / 36.0;
      } else if (diffpair === 6) {
        prob = 3.0 / 36.0;
      }
    }
    if (numberofReroll === 1) {
      if (diffpair === 0) {
        prob = 1.0;
      } else if (diffpair === 6) {
        prob = 1.0;
      } else if (diffpair === 8) {
        prob = 2.0 / 6.0;
      } else if (diffpair === 10) {
        prob = 1.0 / 6.0;
      } else if (diffpair === 12) {
        prob = 0.0;
      }
    }
    return prob;
  }

  probFH(a, userSimChoose) {
    let prob = 0;
    const numberofReroll = this.computeDecode(a, userSimChoose);
    const arr = decodeScratch;
    let diffpair = 0;
    for (let i = 0; i <= 4; i += 1) {
      for (let j = 0; j <= 4; j += 1) {
        if (arr[i] > 0 && arr[j] > 0 && arr[i] !== arr[j]) {
          diffpair += 1;
        }
      }
    }
    if (numberofReroll === 5 || numberofReroll === 4) {
      prob = 50.0 / 1296.0;
    }
    if (numberofReroll === 3) {
      if (diffpair === 0) {
        prob = 20.0 / 216.0;
      } else if (diffpair === 2) {
        prob = 6.0 / 216.0;
      }
    }
    if (numberofReroll === 2) {
      if (diffpair === 0) {
        prob = 5.0 / 36.0;
      } else if (diffpair === 4) {
        prob = 3.0 / 36.0;
      } else if (diffpair === 6) {
        prob = 0.0;
      }
    }
    if (numberofReroll === 1) {
      if (diffpair === 0 || diffpair === 10 || diffpair === 12) {
        prob = 0.0;
      } else if (diffpair === 6) {
        prob = 1.0 / 6.0;
      } else if (diffpair === 8) {
        prob = 2.0 / 6.0;
      }
    }
    return prob;
  }

  probSmStr(a, userSimChoose) {
    let prob = 0;
    const numberofReroll = this.computeDecode(a, userSimChoose);
    const arr = decodeScratch;
    const count = countScratch;
    count.fill(0);
    let sum = 0;
    let sumofsquare = 0;
    let diffpair = 0;
    let forbash = 0;
    for (let i = 0; i <= 4; i += 1) {
      if (arr[i] > 0) {
        count[arr[i] - 1] += 1;
      }
    }
    for (let i = 0; i <= 4; i += 1) {
      sum += arr[i];
      sumofsquare += arr[i] * arr[i];
      for (let j = 0; j <= 4; j += 1) {
        if (arr[i] > 0 && arr[j] > 0 && arr[i] !== arr[j]) {
          diffpair += 1;
        }
      }
    }
    if (numberofReroll === 5) {
      prob = 200.0 / 1296.0;
    } else if (numberofReroll === 4) {
      if (sum === 1 || sum === 6) {
        prob = 132.0 / 1296.0;
      } else if (sum === 2 || sum === 5) {
        prob = 192.0 / 1296.0;
      } else {
        prob = 276.0 / 1296.0;
      }
    } else if (numberofReroll === 3) {
      if (SMSTR_SUMSQ_6.has(sumofsquare)) {
        prob = 6.0 / 216.0;
      } else if (SMSTR_SUMSQ_12.has(sumofsquare)) {
        prob = 12.0 / 216.0;
      } else if (SMSTR_SUMSQ_18.has(sumofsquare)) {
        prob = 18.0 / 216.0;
      } else if (SMSTR_SUMSQ_30.has(sumofsquare)) {
        prob = 30.0 / 216.0;
      } else if (SMSTR_SUMSQ_36.has(sumofsquare)) {
        prob = 36.0 / 216.0;
      } else if (SMSTR_SUMSQ_54.has(sumofsquare)) {
        prob = 54.0 / 216.0;
      } else if (sumofsquare === 25) {
        prob = 78.0 / 216.0;
      }
    } else if (numberofReroll === 2) {
      if (diffpair === 0) {
        prob = 0.0;
      } else if (diffpair === 4) {
        if (count[0] > 0 && count[4] === 0 && count[5] === 0) {
          prob = 2.0 / 36.0;
        } else if (count[5] > 0 && count[1] === 0 && count[0] === 0) {
          prob = 2.0 / 36.0;
        } else if (count[3] * count[4] > 0 || count[1] * count[2] > 0 || count[1] * count[3] > 0 || count[2] * count[4] > 0) {
          prob = 4.0 / 36.0;
        } else if (count[1] * count[4] > 0) {
          prob = 2.0 / 36.0;
        } else if (count[2] * count[3] > 0) {
          prob = 6.0 / 36.0;
        } else {
          prob = 0.0;
        }
      } else if (SMSTR_R2_11.has(sumofsquare)) {
        prob = 11.0 / 36.0;
      } else if (SMSTR_R2_4.has(sumofsquare)) {
        prob = 4.0 / 36.0;
      } else if (SMSTR_R2_13.has(sumofsquare)) {
        prob = 13.0 / 36.0;
      } else if (SMSTR_R2_2.has(sumofsquare)) {
        prob = 2.0 / 36.0;
      } else if (SMSTR_R2_20.has(sumofsquare)) {
        prob = 20.0 / 36.0;
      }
    } else if (numberofReroll === 1) {
      for (let i = 1; i <= 6; i += 1) {
        for (let j = 0; j <= 4; j += 1) {
          if (arr[j] === 0) {
            arr[j] = i;
            count[i - 1] += 1;
            if (
              count[0] * count[1] * count[2] * count[3] +
              count[1] * count[2] * count[3] * count[4] +
              count[2] * count[3] * count[4] * count[5] >
              0
            ) {
              forbash += 1;
            }
            count[i - 1] -= 1;
            arr[j] = 0;
          }
        }
      }
      prob = forbash / 6.0;
    }
    return prob;
  }

  getRerollSuggestion() {
    const suggestions = [];
    let bestMask = 0;
    let bestMaskProb = 0;

    const tryCategory = (available, probFn, label) => {
      if (!available) return;
      let bestCategoryProb = 0;
      const bestMasks = [];
      for (let i = 1; i <= 31; i += 1) {
        const mask = this.getProbabilityMask(i);
        const probability = probFn.call(this, this.arrVal, mask);
        if (probability > bestCategoryProb) {
          bestCategoryProb = probability;
          bestMasks.length = 0;
          bestMasks.push(mask);
        } else if (probability === bestCategoryProb) {
          bestMasks.push(mask);
        }
      }
      if (bestCategoryProb > 0) {
        if (bestCategoryProb > bestMaskProb) {
          bestMaskProb = bestCategoryProb;
          bestMask = bestMasks[0];
        }
        const letters = bestMasks
          .map((mask) => this.maskToDiceLetters(mask).join('') || 'None')
          .join(' ');
        suggestions.push({
          category: HUMAN_LABELS[label] || label,
          probability: bestCategoryProb,
          letters,
        });
      }
    };

    tryCategory(this.isAvailAdv[0] > 0, this.probTOAK, '3 of a kind');
    tryCategory(this.isAvailAdv[1] > 0, this.probFOAK, '4 of a kind');
    tryCategory(this.isAvailAdv[2] > 0, this.probFH, 'Full house');
    tryCategory(this.isAvailAdv[3] > 0, this.probSmStr, 'Small straight');
    tryCategory(this.isAvailAdv[4] > 0, this.probLgStr, 'Large straight');
    tryCategory(this.yaht !== 100, this.probYaht, 'Yahtzee');

    return { suggestions, bestMask, bestMaskProb };
  }

  getBestMaskForCategory(probFn) {
    let bestMask = 0;
    let bestProb = 0;
    for (let i = 1; i <= 31; i += 1) {
      const mask = this.getProbabilityMask(i);
      const probability = probFn.call(this, this.arrVal, mask);
      if (probability > bestProb) {
        bestProb = probability;
        bestMask = mask;
      }
    }
    return { bestMask, bestProb };
  }

  getBotAction() {
    const options = this.getAvailableOptions();
    const basicTotal = Helper.sumArr(this.pntsBasic);

    // Hard mode hands the decision to hard-bot.js. Anything it returns is
    // checked against the legal option list first, so a bad answer falls back
    // to the easy logic below rather than wasting the turn.
    if (difficulty === DIFFICULTY.HARD && typeof HardBot !== 'undefined') {
      const hard = HardBot.decide(this, typeof game !== 'undefined' && game ? game.round : 1);
      if (hard > 0) {
        const isRerollMask = hard % 10 === 0 && this.roll_left > 0;
        const isLegalCategory = options.some((o) => o.id === hard && o.available);
        if (isRerollMask || isLegalCategory) {
          return hard;
        }
      }
    }
    let maxPoint = -1;
    let bestValue = -1;
    let bestChoice = CATEGORY.END_TURN;

    // A box that pushes the upper section to 63 is worth 35 more than its own
    // points, which the raw points column cannot express. One exception: up to
    // round 11, don't let that premium outrank a Yahtzee actually in hand. The
    // Yahtzee box is the hardest to refill, and without this the bot passes on
    // enough of them to drop its Yahtzee rate. By round 12 there is little
    // "later" left, so from then on it is a straight points comparison.
    const holdingYahtzee =
      typeof game !== 'undefined' && game && game.round <= 11
      && options.some((o) => o.id === CATEGORY.YAHTZEE && o.available && o.points > 0);

    for (const option of options) {
      if (option.id !== CATEGORY.END_TURN && !option.available) {
        continue;
      }
      if (option.id !== CATEGORY.END_TURN) {
        const isUpper = option.id >= 1 && option.id <= 6;
        const completesBonus = isUpper && basicTotal < 63 && basicTotal + option.points >= 63;
        // bestValue ranks the options; maxPoint carries the same figure so the
        // reroll gate below also treats a bonus-completing box as worth taking.
        const value = option.points + (completesBonus && !holdingYahtzee ? UPPER_BONUS : 0);
        if (value > bestValue) {
          bestValue = value;
          maxPoint = value;
          bestChoice = option.id;
        }
      }
      if (option.id === CATEGORY.END_TURN && bestChoice === CATEGORY.END_TURN) {
        bestChoice = CATEGORY.END_TURN;
      }
    }

    // Holding a bonus Yahtzee is 100 guaranteed points that the option list
    // cannot express, since the bonus is the same whichever box takes the dice.
    // Rerolling would throw it away, so skip straight to placing it.
    const holdingBonusYahtzee = this.isJokerTurn() && this.yaht === 0;

    if (this.roll_left >= 1 && !holdingBonusYahtzee) {
      // Always apply: if the roll is four 5s + one 4 (any order), and Yahtzee
      // is still available then
      // reroll the single 4 to try for Yahtzee. This should run regardless of
      // round or full-house gating.
      if (
        this.faceCounter[4] === 4 &&
        this.arrVal.includes(4) &&
        this.yaht !== 100
      ) {
        let otherIndex = -1;
        for (let i = 0; i < 5; i += 1) {
          if (this.arrVal[i] !== 5) {
            otherIndex = i;
            break;
          }
        }
        if (otherIndex >= 0) {
          const mask = 10 * 2 ** otherIndex;
          return mask;
        }
      }

      // Chase the upper section whenever the bonus is still open. There used to
      // be a round/total cutoff here; measured over 180,000 scorecards, every
      // version of "give up early" scored worse, because keeping high faces
      // pays even once 63 is out of reach — three 6s is 18 points regardless.
      if (basicTotal < 63 && !(this.fullHousePresent() && this.isAvailAdv[2] > 0)) {
        const preservePriorityFace = (face) => {
          let mask = 0;
          for (let i = 0; i < 5; i += 1) {
            if (this.arrVal[i] !== face) {
              mask += 10 * 2 ** i;
            }
          }
          return mask;
        };
        // A mask of 0 means every die already shows that face, so there is
        // nothing to reroll. Fall through and let the scoring logic below pick
        // the category instead of returning a decision that means nothing.
        if (this.faceCounter[5] >= 2 && this.isAvailBasic[5] && !(this.smallStraightPresent() && this.isAvailAdv[3] > 0)) {
          const mask = preservePriorityFace(6);
          if (mask > 0) {
            return mask;
          }
        }
        for (const face of [5, 4, 3]) {
          if (this.faceCounter[face - 1] >= 3 && this.isAvailBasic[face - 1]) {
            const mask = preservePriorityFace(face);
            if (mask > 0) {
              return mask;
            }
          }
        }
      }

      if (this.isAvailAdv[4] > 0 && this.smallStraightPresent() && !this.largeStraightPresent()) {
        if (this.faceCounter[0] === 1 && this.faceCounter[1] === 1 && this.faceCounter[2] === 1 && this.faceCounter[3] === 1 && this.faceCounter[4] === 0 && this.faceCounter[5] === 1) {
          for (let i = 0; i < 5; i += 1) {
            if (this.arrVal[i] === 6) {
              return 10 * 2 ** i;
            }
          }
        }
        if (this.faceCounter[0] === 1 && this.faceCounter[1] === 0 && this.faceCounter[2] === 1 && this.faceCounter[3] === 1 && this.faceCounter[4] === 1 && this.faceCounter[5] === 1) {
          for (let i = 0; i < 5; i += 1) {
            if (this.arrVal[i] === 1) {
              return 10 * 2 ** i;
            }
          }
        }
        const largeStraightAdvice = this.getBestMaskForCategory(this.probLgStr);
        if (largeStraightAdvice.bestMask > 0) {
          return largeStraightAdvice.bestMask;
        }
      }

      // Special case: four sixes + small odd (1-3) — try for Yahtzee / 4/3 of a kind
      // If we have four sixes and the remaining die is 1/2/3 and one of 3k/4k/Yahtzee is still available,
      // reroll only that small die to maximize chance of Yahtzee on sixes.
      if (this.faceCounter[5] === 4) {
        let otherIndex = -1;
        let otherValue = 0;
        for (let i = 0; i < 5; i += 1) {
          if (this.arrVal[i] !== 6) {
            otherIndex = i;
            otherValue = this.arrVal[i];
            break;
          }
        }
        if (otherIndex >= 0 && otherValue >= 1 && otherValue <= 3) {
          if (this.isAvailAdv[0] > 0 || this.isAvailAdv[1] > 0 || this.yaht !== 100) {
            const mask = 10 * 2 ** otherIndex;
            return mask;
          }
        }
      }

      let rerollMask = 0;
      // Prefer basic-category reroll when best static choice is a basic or no positive static choice
      if (maxPoint === 0 || (bestChoice >= 1 && bestChoice <= 6)) {
        for (let j = 6; j >= 1; j -= 1) {
          if (this.isAvailBasic[j - 1]) {
            let mask = 0;
            for (let i = 0; i < 5; i += 1) {
              if (this.arrVal[i] !== j) {
                mask += 10 * 2 ** i;
              }
            }
            rerollMask = mask;
            break;
          }
        }
      }

      // Also consider the probabilistic (cheat) suggestion for any roll when worthwhile
      const advice = this.getRerollSuggestion();
      if (advice.bestMask > 0 && maxPoint < 24) {
        rerollMask = advice.bestMask;
      }

      if (rerollMask > 0) {
        return rerollMask;
      }
    }

    if (
      basicTotal < 63 &&
      this.roll_left === 0 &&
      !(this.isYahtzee() && this.yaht !== 100) &&
      !(this.fullHousePresent() && this.isAvailAdv[2] > 0)
    ) {
      if (this.faceCounter[5] >= 3 && this.isAvailBasic[5]) {
        return 6;
      }
      for (const face of [5, 4, 3]) {
        if (this.faceCounter[face - 1] >= 3 && this.isAvailBasic[face - 1]) {
          return face;
        }
      }
    }

    return bestChoice;
  }
}

class Game {
  constructor(players) {
    this.players = players;
    this.currentIndex = 0;
    this.round = 1;
    this.finished = false;
  }

  currentPlayer() {
    return this.players[this.currentIndex];
  }

  advanceTurn() {
    const player = this.currentPlayer();
    player.checkScoreCard();
    player.resetForNextRound();

    const totalPlayers = this.players.length;
    let nextIndex = this.currentIndex;
    do {
      nextIndex += 1;
      if (nextIndex >= totalPlayers) {
        nextIndex = 0;
        this.round += 1;
      }
    } while (this.players[nextIndex].life !== 1 && nextIndex !== this.currentIndex);

    if (this.players[nextIndex].life !== 1) {
      this.finished = true;
      return;
    }

    this.currentIndex = nextIndex;
    if (this.round > 13) {
      this.finished = true;
    }
  }

  getRanking() {
    const ranks = this.players
      .map((player) => ({
        player,
        name: player.name,
        score: player.totalscore + player.score,
      }))
      .sort((a, b) => b.score - a.score);
    return ranks;
  }
}

function renderSetupOptions() {
  playerOptions.innerHTML = '';
  const fragment = document.createDocumentFragment();
  const count = Number(playerCountInput.value) || 1;
  for (let i = 1; i <= count; i += 1) {
    const option = document.createElement('div');
    option.className = 'player-option';
    option.innerHTML = `
      <div class="option-row">
        <label for="playerName${i}">Player ${i} name</label>
        <input id="playerName${i}" type="text" value="Player ${i}" />
      </div>
      <div class="option-row">
        <label for="playerBot${i}">Bot player</label>
        <select id="playerBot${i}">
          <option value="0">Human</option>
          <option value="1" selected>Bot</option>
        </select>
      </div>
    `;
    fragment.append(option);
  }
  playerOptions.append(fragment);
}

function normalizePlayerCount() {
  let count = Number(playerCountInput.value) || 1;
  count = Math.min(Math.max(count, 1), 10);
  playerCountInput.value = count;
  return count;
}

function buildGame() {
  const count = normalizePlayerCount();
  const players = [];
  for (let i = 1; i <= count; i += 1) {
    const name = document.getElementById(`playerName${i}`).value.trim() || `Player ${i}`;
    const botMode = document.getElementById(`playerBot${i}`).value === '1';
    players.push(new Player(name, botMode));
  }
  return new Game(players);
}

function updateUI() {
  if (!game) {
    return;
  }

  if (game.finished) {
    setupScreen.classList.add('hidden');
    gameScreen.classList.add('hidden');
    gameOverScreen.classList.remove('hidden');
    renderFinalSummary();
    renderRoundBotLog();
    return;
  }

  const player = game.currentPlayer();
  setupScreen.classList.add('hidden');
  gameScreen.classList.remove('hidden');
  gameOverScreen.classList.add('hidden');

  turnTitle.textContent = `${player.name}'s turn`;
  playerType.textContent = player.bot ? 'BOT' : 'HUMAN';
  playerScore.textContent = String(player.score);
  playerTotal.textContent = String(player.totalscore + player.score);
  gameStatus.textContent = `Round ${game.round} • ${Math.max(player.roll_left, 0)} rolls remaining`;
  backToSetupButton.classList.remove('hidden');

  renderDice(player);
  renderOptions(player);
  renderSuggestion(player);
  renderScoreboard();
  renderRoundBotLog();

  roundBotLogPanel.classList.add('hidden');
  rollButton.disabled = player.roll_left < 0 || player.life !== 1 || player.bot;
  forfeitButton.disabled = player.life !== 1;
  if (player.bot) {
    rollButton.textContent = 'Bot is playing...';
    rollButton.disabled = true;
  } else {
    rollButton.textContent = player.roll_left === 3 ? 'Start turn' : 'Roll selected dice';
  }
}

function renderDice(player) {
  diceRow.innerHTML = '';
  const fragment = document.createDocumentFragment();
  const disabled = player.roll_left < 0 || player.life !== 1 || player.bot;
  for (let i = 0; i < 5; i += 1) {
    const tile = document.createElement('button');
    tile.type = 'button';
    tile.className = `dice-tile${selectedDice[i] ? ' selected' : ''}`;
    tile.disabled = disabled;
    const value = player.arrVal[i] || 0;
    tile.innerHTML = `
      <span class="dice-label">${DICE_LABELS[i]}</span>
      <span class="dice-value">${value ? DICE_EMOJI[value - 1] : '–'}</span>
    `;
    tile.addEventListener('click', () => {
      selectedDice[i] = !selectedDice[i];
      updateUI();
    });
    fragment.append(tile);
  }
  diceRow.append(fragment);
}

function renderOptions(player) {
  optionsList.innerHTML = '';
  const fragment = document.createDocumentFragment();
  const options = player.getAvailableOptions();
  const showNeon = player.roll_left < 3;
  const buttonsDisabled = player.roll_left === 3 || player.life !== 1 || player.bot;

  for (const option of options) {
    const card = document.createElement('div');
    const cardClasses = ['option-card'];
    if (!option.available) {
      cardClasses.push('disabled');
    } else if (showNeon && option.points > 0) {
      cardClasses.push('selectable-positive');
    } else if (showNeon && option.points === 0) {
      cardClasses.push('selectable-zero');
    }
    card.className = cardClasses.join(' ');
    card.innerHTML = `
      <h3>${option.label}</h3>
      <p>Points: <strong>${option.points}</strong></p>
    `;
    const button = document.createElement('button');
    button.type = 'button';
    button.textContent = option.available ? 'Select' : 'Used';
    button.disabled = !option.available || buttonsDisabled;
    button.addEventListener('click', () => {
      if (game.finished) return;
      player.performScore(option.id);
      player.checkScoreCard();
      game.advanceTurn();
      selectedDice = [false, false, false, false, false];
      advanceBotsAndRender();
    });
    card.append(button);
    fragment.append(card);
  }
  optionsList.append(fragment);
}

function renderSuggestion(player) {
  if (!player.cheat || player.life !== 1) {
    suggestionText.textContent = 'No suggestion available.';
    return;
  }

  if (player.isJokerTurn()) {
    const face = player.arrVal[0];
    const stage = player.jokerStage();
    const bonus =
      player.yaht === 0
        ? 'It scores the 100 point Yahtzee bonus.'
        : 'The Yahtzee box was scratched, so there is no bonus.';
    let placement;
    if (stage === 'upper-forced') {
      placement = `${CATEGORY_LABELS[face]} is still open, so the dice must go there.`;
    } else if (stage === 'lower-free') {
      placement = 'Any open box in the lower section will take it as a joker, at full value.';
    } else {
      placement = 'The lower section is full, so a zero has to go in an open upper box.';
    }
    suggestionText.textContent = `Joker turn: five ${face}s with the Yahtzee box already filled.\n${bonus}\n${placement}`;
    return;
  }

  if (player.roll_left === 3) {
    suggestionText.textContent = 'Start the turn to get a tailored suggestion. Dice are labeled A B C D E.';
    return;
  }

  const advice = player.getRerollSuggestion();
  if (!advice.suggestions.length) {
    suggestionText.textContent = 'No reroll suggestions available for this roll.';
    return;
  }

  const formatDiceList = (letters) => {
    if (letters === 'None') {
      return 'keep current dice';
    }
    const dice = letters.split(' ');
    if (dice.length === 1) {
      return dice[0];
    }
    if (dice.length === 2) {
      return `${dice[0]} or ${dice[1]}`;
    }
    return `${dice.slice(0, -1).join(', ')} or ${dice.slice(-1)}`;
  };

  const lines = advice.suggestions.map((entry) => {
    const rerollText = formatDiceList(entry.letters);
    const suffix = player.roll_left === 0 ? 'No rerolls left.' : `Probability: ${(entry.probability * 100).toFixed(3)}%`;
    return `To get ${entry.category} → reroll ${rerollText}\n${suffix}`;
  });

  suggestionText.textContent = lines.join('\n\n');
}

function renderScoreboard() {
  scoreboardTable.innerHTML = '';
  const fragment = document.createDocumentFragment();
  for (const player of game.players) {
    const card = document.createElement('div');
    card.className = 'player-card';
    const statusLabel = player.life === 0 ? 'Forfeited' : player.bot ? 'Bot' : 'Human';
    const scoreLabel = player.life === 0 ? 'forfeited' : 'current total';
    card.append(
      buildLabelledBlock(player.name, statusLabel),
      buildLabelledBlock(String(player.totalscore + player.score), scoreLabel),
    );
    fragment.append(card);
  }
  scoreboardTable.append(fragment);
}

function buildLabelledBlock(strongText, spanText) {
  const block = document.createElement('div');
  const strong = document.createElement('strong');
  strong.textContent = strongText;
  const span = document.createElement('span');
  span.textContent = spanText;
  block.append(strong, span);
  return block;
}

function renderRoundBotLog() {
  if (!botLogEnabled() || !game?.finished || !roundBotLog.length) {
    roundBotLogPanel.classList.add('hidden');
    return;
  }

  roundBotLogPanel.classList.remove('hidden');
  roundBotLogContent.innerHTML = '';
  const fragment = document.createDocumentFragment();

  const rounds = new Map();
  for (const entry of roundBotLog) {
    if (!rounds.has(entry.round)) {
      rounds.set(entry.round, []);
    }
    rounds.get(entry.round).push(entry);
  }

  for (let round = 1; round <= 13; round += 1) {
    const details = document.createElement('details');
    details.className = 'bot-log-round';
    if (round === 1) details.open = true;

    const summary = document.createElement('summary');
    summary.className = 'bot-log-summary';
    summary.textContent = `Round ${round}`;
    details.append(summary);

    const content = document.createElement('div');
    content.className = 'bot-log-round-content';

    const entries = rounds.get(round) || [];
    if (!entries.length) {
      const empty = document.createElement('div');
      empty.className = 'bot-log-empty';
      empty.textContent = 'No bot actions this round.';
      content.append(empty);
    } else {
      entries.forEach((entry) => {
        const playerBlock = document.createElement('div');
        playerBlock.className = 'bot-log-player-block';

        const playerTitle = document.createElement('div');
        playerTitle.className = 'bot-log-player-title';
        playerTitle.textContent = entry.name;
        playerBlock.append(playerTitle);

        entry.lines.forEach((line) => {
          playerBlock.append(renderBotLogLine(line));
        });

        content.append(playerBlock);
      });
    }

    details.append(content);
    fragment.append(details);
  }
  roundBotLogContent.append(fragment);
}

function renderBotLogLine(line) {
  const row = document.createElement('div');
  row.className = 'bot-log-line';

  if (line.startsWith('Rolled:') || line.startsWith('New dice:')) {
    const [label, values] = line.split(':');
    const labelSpan = document.createElement('span');
    labelSpan.className = 'bot-log-label';
    labelSpan.textContent = `${label}: `;
    row.append(labelSpan);

    const diceRow = document.createElement('div');
    diceRow.className = 'bot-log-dice-row';
    values
      .trim()
      .split(' ')
      .filter(Boolean)
      .forEach((value) => {
        const die = document.createElement('span');
        die.className = 'bot-log-die';
        die.textContent = value;
        diceRow.append(die);
      });
    row.append(diceRow);
    return row;
  }

  if (line.startsWith('Rerolling:')) {
    const labelSpan = document.createElement('span');
    labelSpan.className = 'bot-log-label';
    labelSpan.textContent = 'Rerolling: ';
    row.append(labelSpan);

    const diceRow = document.createElement('div');
    diceRow.className = 'bot-log-reroll-row';
    line
      .slice('Rerolling:'.length)
      .split(',')
      .map((value) => value.trim())
      .filter(Boolean)
      .forEach((letter) => {
        const chip = document.createElement('span');
        chip.className = 'bot-log-reroll-chip';
        chip.textContent = letter;
        diceRow.append(chip);
      });
    row.append(diceRow);
    return row;
  }

  row.textContent = line;
  return row;
}

function botLogEnabled() {
  return botMode !== BOT_MODE.SILENT;
}

// Plays one step of the current bot's turn: the opening roll, a reroll, or the
// scoring decision. Returns true when that step ended the turn. Does no
// rendering — the scheduler decides when the board is drawn.
function playBotStep(player) {
  const logging = botLogEnabled();

  if (player.roll_left === 3) {
    player.botLog.length = 0;
    player.rollDice();
    if (logging) {
      player.logBot(`Rolled: ${player.arrVal.join(' ')}`);
    }
    return false;
  }

  const decision = player.getBotAction();
  if (decision > 0 && decision % 10 === 0 && player.roll_left > 0) {
    if (logging) {
      const rerollLabel = player.maskToDiceLetters(decision).join(', ') || 'none';
      player.logBot(`Rerolling: ${rerollLabel}`);
    }
    player.rollDecision = decision;
    player.roll_left -= 1;
    player.rollDice();
    if (logging) {
      player.logBot(`New dice: ${player.arrVal.join(' ')}`);
    }
    return false;
  }

  let finalBotLog = null;
  if (logging) {
    player.logBot(`Chooses: ${CATEGORY_LABELS[decision] || `Choice ${decision}`}`);
    finalBotLog = player.botLog.slice();
  }
  player.performScore(decision);
  player.checkScoreCard();
  const previousRound = game.round;
  game.advanceTurn();
  selectedDice = [false, false, false, false, false];

  if (finalBotLog && finalBotLog.length) {
    roundBotLog.push({ round: previousRound, name: player.name, lines: finalBotLog });
  }
  return true;
}

// Plays one bot's entire turn at once. The scoring branch of playBotStep always
// calls advanceTurn, so this is guaranteed to terminate.
function playBotTurn(player) {
  for (let steps = 0; steps < MAX_BOT_STEPS; steps += 1) {
    if (playBotStep(player)) {
      return;
    }
  }
}

function clearBotTimer() {
  if (botTimeout !== null) {
    clearTimeout(botTimeout);
    botTimeout = null;
  }
}

function botIsWaiting() {
  if (!game || game.finished) {
    return null;
  }
  const player = game.currentPlayer();
  if (!player.bot || player.life !== 1 || player.roll_left < 0) {
    return null;
  }
  return player;
}

// Only a human still in the game can be kept waiting, so only then is pacing
// worth anything.
function humanStillPlaying() {
  for (const player of game.players) {
    if (!player.bot && player.life === 1) {
      return true;
    }
  }
  return false;
}

function botTurnDelay() {
  let bots = 0;
  for (const player of game.players) {
    if (player.bot) {
      bots += 1;
    }
  }
  const scaled = Math.round((BOT_TURN_DELAY_MS * 3) / Math.max(bots, 1));
  return Math.max(BOT_TURN_MIN_DELAY_MS, Math.min(BOT_TURN_DELAY_MS, scaled));
}

// Draws the board, then — if a bot is up — schedules its next move. Exactly one
// timer is ever pending, and it is always cleared before a new one is set.
function advanceBotsAndRender() {
  clearBotTimer();

  // Bots only, and not in watch mode: nobody is waiting for a turn, so there is
  // no one to pace for. Play the rest of the game out and draw the result once.
  if (game && botMode !== BOT_MODE.WATCH && !humanStillPlaying()) {
    let turns = 0;
    let player = botIsWaiting();
    while (player && turns < MAX_BOT_STEPS) {
      playBotTurn(player);
      player = botIsWaiting();
      turns += 1;
    }
    updateUI();
    return;
  }

  updateUI();
  if (!botIsWaiting()) {
    return;
  }
  const delay = botMode === BOT_MODE.WATCH ? BOT_STEP_DELAY_MS : botTurnDelay();
  botTimeout = setTimeout(() => {
    botTimeout = null;
    const player = botIsWaiting();
    if (!player) {
      updateUI();
      return;
    }
    if (botMode === BOT_MODE.WATCH) {
      playBotStep(player);
    } else {
      playBotTurn(player);
    }
    advanceBotsAndRender();
  }, delay);
}

function renderFinalSummary() {
  finalSummary.innerHTML = '';
  const fragment = document.createDocumentFragment();
  const ranking = game.getRanking();
  const average = Math.round(
    (game.players.reduce((sum, player) => sum + player.totalscore + player.score, 0) / game.players.length) * 10,
  ) / 10;

  ranking.forEach((entry, index) => {
    const player = entry.player;
    const details = document.createElement('details');
    details.className = 'player-scorecard';
    if (index === 0) details.open = true;

    const summary = document.createElement('summary');
    summary.className = 'player-scorecard-summary';
    const summaryName = document.createElement('strong');
    summaryName.textContent = `${index + 1}. ${entry.name}`;
    const summaryScore = document.createElement('span');
    summaryScore.textContent = `${entry.score} points`;
    summary.append(summaryName, summaryScore);
    details.append(summary);

    const content = document.createElement('div');
    content.className = 'player-scorecard-content';

    // Basic scores (Ones - Sixes)
    const basicBlock = document.createElement('div');
    basicBlock.className = 'score-section';
    const basicTitle = document.createElement('h4');
    basicTitle.textContent = 'Upper section';
    basicBlock.append(basicTitle);
    const basicTable = document.createElement('div');
    basicTable.className = 'score-table';
    for (let i = 0; i < 6; i += 1) {
      const row = document.createElement('div');
      row.className = 'score-row';
      const label = document.createElement('div');
      label.textContent = `${i + 1}s`;
      const pts = document.createElement('div');
      pts.textContent = player.pntsBasic[i] || 0;
      const state = document.createElement('div');
      state.textContent = player.isAvailBasic[i] ? 'FREE' : 'USED';
      row.append(label, pts, state);
      basicTable.append(row);
    }
    // Bonus
    const bonusRow = document.createElement('div');
    bonusRow.className = 'score-row';
    const bonusLabel = document.createElement('div');
    bonusLabel.textContent = 'Bonus';
    const bonusPts = document.createElement('div');
    bonusPts.textContent = player.bonus || 0;
    bonusRow.append(bonusLabel, bonusPts, document.createElement('div'));
    basicTable.append(bonusRow);
    basicBlock.append(basicTable);
    content.append(basicBlock);

    // Advanced section
    const advBlock = document.createElement('div');
    advBlock.className = 'score-section';
    const advTitle = document.createElement('h4');
    advTitle.textContent = 'Lower section';
    advBlock.append(advTitle);
    const advTable = document.createElement('div');
    advTable.className = 'score-table';

    const pntsToak = player.pntsToak || 0;
    const pntsFoak = player.pntsFoak || 0;
    let pntsFull = 25 * (1 - (player.isAvailAdv[2] || 0));
    if ((player.isAvailAdv[2] || 0) < 0) pntsFull = 0;
    let pntsSm = 30 * (1 - (player.isAvailAdv[3] || 0));
    if ((player.isAvailAdv[3] || 0) < 0) pntsSm = 0;
    let pntsLg = 40 * (1 - (player.isAvailAdv[4] || 0));
    if ((player.isAvailAdv[4] || 0) < 0) pntsLg = 0;
    const pntsYaht = player.yaht === 0 ? 50 : 0;
    const pntsChan = player.pntsChan || 0;
    const pntsYahtBo = player.yahtBo || 0;

    const advRows = [
      ['3 of a kind', pntsToak, player.isAvailAdv[0] > 0 ? 'FREE' : 'USED'],
      ['4 of a kind', pntsFoak, player.isAvailAdv[1] > 0 ? 'FREE' : 'USED'],
      ['Full House', pntsFull, (player.isAvailAdv[2] || 0) > 0 ? 'FREE' : 'USED'],
      ['Small Straight', pntsSm, (player.isAvailAdv[3] || 0) > 0 ? 'FREE' : 'USED'],
      ['Large Straight', pntsLg, (player.isAvailAdv[4] || 0) > 0 ? 'FREE' : 'USED'],
      ['Yahtzee', pntsYaht, player.yaht === 1 ? 'FREE' : 'USED'],
      ['Chance', pntsChan, player.chanAvail ? 'FREE' : 'USED'],
      ['Yahtz Bonus', pntsYahtBo, pntsYahtBo > 0 ? 'FREE' : 'USED'],
    ];

    advRows.forEach((r) => {
      const row = document.createElement('div');
      row.className = 'score-row';
      const label = document.createElement('div');
      label.textContent = r[0];
      const pts = document.createElement('div');
      pts.textContent = r[1];
      const state = document.createElement('div');
      state.textContent = r[2];
      row.append(label, pts, state);
      advTable.append(row);
    });

    advBlock.append(advTable);
    content.append(advBlock);

    details.append(content);
    fragment.append(details);
  });

  const averageRow = buildLabelledBlock('Average score', String(average));
  averageRow.className = 'summary-row';
  fragment.append(averageRow);
  finalSummary.append(fragment);
}

playerCountInput.addEventListener('change', () => {
  normalizePlayerCount();
  renderSetupOptions();
});
startGameButton.addEventListener('click', () => {
  game = buildGame();
  selectedDice = [false, false, false, false, false];
  botMode = botModeSelect.value;
  difficulty = difficultySelect.value;
  botModeSelect.disabled = true;
  difficultySelect.disabled = true;
  roundBotLog = [];
  advanceBotsAndRender();
});
rollButton.addEventListener('click', () => {
  if (!game) return;
  const player = game.currentPlayer();
  if (player.life !== 1) return;
  if (player.roll_left === 3) {
    player.rollDice();
  } else if (player.roll_left > 0) {
    player.calcRerollMask();
    player.roll_left -= 1;
    player.rollDice();
  }
  updateUI();
});
forfeitButton.addEventListener('click', () => {
  if (!game) return;
  const player = game.currentPlayer();
  player.performScore(CATEGORY.FORFEIT);
  game.advanceTurn();
  selectedDice = [false, false, false, false, false];
  advanceBotsAndRender();
});
function renderBotModeHint() {
  botModeHint.textContent = BOT_MODE_HINTS[botModeSelect.value] || '';
  difficultyHint.textContent = DIFFICULTY_HINTS[difficultySelect.value] || '';
}

botModeSelect.addEventListener('change', () => {
  renderBotModeHint();
  if (game) return;
  botMode = botModeSelect.value;
});

difficultySelect.addEventListener('change', () => {
  renderBotModeHint();
  if (game) return;
  difficulty = difficultySelect.value;
});

helpButton.addEventListener('click', () => {
  helpPanel.classList.remove('hidden');
});

closeHelpButton.addEventListener('click', () => {
  helpPanel.classList.add('hidden');
});

backButtons.forEach((button) => {
  button.addEventListener('click', () => {
    clearBotTimer();
    setupScreen.classList.remove('hidden');
    gameScreen.classList.add('hidden');
    gameOverScreen.classList.add('hidden');
    botModeSelect.disabled = false;
    difficultySelect.disabled = false;
    game = null;
  });
});

renderBotModeHint();
renderSetupOptions();
