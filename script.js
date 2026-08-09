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
const showBotDetailsToggle = document.getElementById('showBotDetailsToggle');
const roundBotLogPanel = document.getElementById('roundBotLogPanel');
const roundBotLogContent = document.getElementById('roundBotLogContent');
const scoreboardTable = document.getElementById('scoreboardTable');
const finalSummary = document.getElementById('finalSummary');
const backButtons = [backToSetupButton, newGameButton];

let game = null;
let selectedDice = [false, false, false, false, false];
let botTimeout = null;
let showBotDetails = false;
let roundBotLog = [];

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

const DICE_LABELS = ['A', 'B', 'C', 'D', 'E'];

class Dice {
  roll() {
    return Math.floor(Math.random() * 6) + 1;
  }
}

const Helper = {
  sumArr(arr) {
    return arr.reduce((sum, value) => sum + value, 0);
  },
  maxArr(arr) {
    return arr.reduce((max, value) => Math.max(max, value), 0);
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
    this.userChoose = 0;
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
    this.simulProb = new Array(31).fill(0);
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
    if (this.yaht !== 100) {
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
    if (advDone && !this.chanAvail && basicDone && this.yaht <= 0) {
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
    for (let i = 1; i <= 6; i += 1) {
      options.push({
        id: i,
        label: CATEGORY_LABELS[i],
        points: this.isAvailBasic[i - 1] ? i * this.faceCounter[i - 1] : 0,
        available: this.isAvailBasic[i - 1],
      });
    }

    const yahtScore = this.isYahtzee() ? (this.yaht > 0 ? 50 : 100) : 0;
    options.push({
      id: CATEGORY.YAHTZEE,
      label: CATEGORY_LABELS[7],
      points: this.yaht !== 100 ? yahtScore : 0,
      available: this.yaht !== 100,
    });

    options.push({
      id: CATEGORY.FOUR_KIND,
      label: CATEGORY_LABELS[8],
      points:
        this.isAvailAdv[1] > 0 && Helper.maxArr(this.faceCounter) >= 4
          ? Helper.sumArr(this.arrVal)
          : 0,
      available: this.isAvailAdv[1] > 0,
    });

    options.push({
      id: CATEGORY.THREE_KIND,
      label: CATEGORY_LABELS[9],
      points:
        this.isAvailAdv[0] > 0 && Helper.maxArr(this.faceCounter) >= 3
          ? Helper.sumArr(this.arrVal)
          : 0,
      available: this.isAvailAdv[0] > 0,
    });

    options.push({
      id: CATEGORY.CHANCE,
      label: CATEGORY_LABELS[11],
      points: this.chanAvail ? Helper.sumArr(this.arrVal) : 0,
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
    return options;
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
      if (this.isYahtzee()) {
        if (this.yaht > 0) {
          this.score += 50;
        } else {
          this.score += 100;
          this.yahtBo += 100;
        }
        this.yaht -= 1;
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
    const arr = arrVal.slice();
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
    return { arr, count };
  }

  probLgStr(a, userSimChoose) {
    let prob = 0;
    const info = this.computeDecode(a, userSimChoose);
    const arr = info.arr;
    const numberofReroll = info.count;
    const count = [0, 0, 0, 0, 0, 0];
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
        } else if ([5, 10, 17, 26, 40, 45, 52, 61].includes(sumofsquare)) {
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
      } else {
        prob = 1.0 / 6.0;
      }
    }
    return prob;
  }

  probFOAK(a, userSimChoose) {
    let prob = 0;
    const info = this.computeDecode(a, userSimChoose);
    const arr = info.arr;
    const numberofReroll = info.count;
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
    const info = this.computeDecode(a, userSimChoose);
    const arr = info.arr;
    const numberofReroll = info.count;
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
    const info = this.computeDecode(a, userSimChoose);
    const arr = info.arr;
    const numberofReroll = info.count;
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
    const info = this.computeDecode(a, userSimChoose);
    const arr = info.arr;
    const numberofReroll = info.count;
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
    const info = this.computeDecode(a, userSimChoose);
    const arr = info.arr;
    const numberofReroll = info.count;
    const count = [0, 0, 0, 0, 0, 0];
    let sum = 0;
    let sumofsquare = 0;
    let diffpair = 0;
    let forbash = 0;
    for (let i = 0; i <= 4; i += 1) {
      if (arr[i] === 1) count[0] += 1;
      if (arr[i] === 2) count[1] += 1;
      if (arr[i] === 3) count[2] += 1;
      if (arr[i] === 4) count[3] += 1;
      if (arr[i] === 5) count[4] += 1;
      if (arr[i] === 6) count[5] += 1;
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
      if ([2, 72].includes(sumofsquare)) {
        prob = 6.0 / 216.0;
      } else if ([8, 50, 40, 26, 37].includes(sumofsquare)) {
        prob = 12.0 / 216.0;
      } else if ([18, 32].includes(sumofsquare)) {
        prob = 18.0 / 216.0;
      } else if ([61, 5, 29].includes(sumofsquare)) {
        prob = 30.0 / 216.0;
      } else if ([52, 10, 45, 17].includes(sumofsquare)) {
        prob = 36.0 / 216.0;
      } else if ([41, 13, 34, 20].includes(sumofsquare)) {
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
        } else if (count[3] * count[4] > 0 || count[3] * count[4] > 0 || count[1] * count[3] > 0 || count[2] * count[4] > 0) {
          prob = 4.0 / 36.0;
        } else if (count[1] * count[4] > 0) {
          prob = 2.0 / 36.0;
        } else if (count[2] * count[3] > 0) {
          prob = 6.0 / 36.0;
        } else {
          prob = 0.0;
        }
      } else if ([14, 77, 21, 70, 38, 45, 38, 45].includes(sumofsquare)) {
        prob = 11.0 / 36.0;
      } else if ([35, 42, 49, 56, 46, 53].includes(sumofsquare)) {
        prob = 4.0 / 36.0;
      } else if ([26, 61].includes(sumofsquare)) {
        prob = 13.0 / 36.0;
      } else if ([30, 65, 41, 62].includes(sumofsquare)) {
        prob = 2.0 / 36.0;
      } else if ([29, 50].includes(sumofsquare)) {
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

    const humanLabel = {
      '3 of a kind': 'Three of a Kind',
      '4 of a kind': 'Four of a Kind',
      'Full house': 'Full House',
      'Small straight': 'Small Straight',
      'Large straight': 'Large Straight',
      Yahtzee: 'Yahtzee',
    };

    const tryCategory = (available, probFn, label) => {
      if (!available) return;
      let bestCategoryProb = 0;
      const bestMasks = [];
      for (let i = 1; i <= 31; i += 1) {
        const mask = this.getProbabilityMask(i);
        const probability = probFn(this.arrVal, mask);
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
          category: humanLabel[label] || label,
          probability: bestCategoryProb,
          letters,
        });
      }
    };

    tryCategory(this.isAvailAdv[0] > 0, this.probTOAK.bind(this), '3 of a kind');
    tryCategory(this.isAvailAdv[1] > 0, this.probFOAK.bind(this), '4 of a kind');
    tryCategory(this.isAvailAdv[2] > 0, this.probFH.bind(this), 'Full house');
    tryCategory(this.isAvailAdv[3] > 0, this.probSmStr.bind(this), 'Small straight');
    tryCategory(this.isAvailAdv[4] > 0, this.probLgStr.bind(this), 'Large straight');
    tryCategory(this.yaht !== 100, this.probYaht.bind(this), 'Yahtzee');

    return { suggestions, bestMask, bestMaskProb };
  }

  getBestMaskForCategory(probFn) {
    let bestMask = 0;
    let bestProb = 0;
    for (let i = 1; i <= 31; i += 1) {
      const mask = this.getProbabilityMask(i);
      const probability = probFn(this.arrVal, mask);
      if (probability > bestProb) {
        bestProb = probability;
        bestMask = mask;
      }
    }
    return { bestMask, bestProb };
  }

  getBotAction() {
    const options = this.getAvailableOptions();
    let maxPoint = -1;
    let bestChoice = CATEGORY.END_TURN;

    for (const option of options) {
      if (option.id !== CATEGORY.END_TURN && !option.available) {
        continue;
      }
      if (option.id !== CATEGORY.END_TURN && option.points > maxPoint) {
        maxPoint = option.points;
        bestChoice = option.id;
      }
      if (option.id === CATEGORY.END_TURN && bestChoice === CATEGORY.END_TURN) {
        bestChoice = CATEGORY.END_TURN;
      }
    }

    if (this.roll_left >= 1) {
      if (this.isAvailAdv[4] > 0 && this.smallStraightPresent() && !this.largeStraightPresent()) {
        const largeStraightAdvice = this.getBestMaskForCategory(this.probLgStr.bind(this));
        if (largeStraightAdvice.bestMask > 0) {
          return largeStraightAdvice.bestMask;
        }
      }

      let rerollMask = 0;
      // Prefer basic-category reroll when best static choice is a basic or no positive static choice
      if (maxPoint === 0 || (bestChoice >= 1 && bestChoice <= 6)) {
        for (let j = 5; j >= 1; j -= 1) {
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
        name: player.name,
        score: player.totalscore + player.score,
      }))
      .sort((a, b) => b.score - a.score);
    return ranks;
  }
}

function renderSetupOptions() {
  playerOptions.innerHTML = '';
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
    playerOptions.append(option);
  }
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
    setTimeout(playBotTurn, 450);
  } else {
    rollButton.textContent = player.roll_left === 3 ? 'Start turn' : 'Roll selected dice';
  }
}

function renderDice(player) {
  diceRow.innerHTML = '';
  for (let i = 0; i < 5; i += 1) {
    const tile = document.createElement('button');
    tile.type = 'button';
    tile.className = `dice-tile${selectedDice[i] ? ' selected' : ''}`;
    tile.disabled = player.roll_left < 0 || player.life !== 1 || player.bot;
    tile.innerHTML = `
      <span class="dice-label">${DICE_LABELS[i]}</span>
      <span class="dice-value">${player.arrVal[i] || '-'}</span>
    `;
    tile.addEventListener('click', () => {
      selectedDice[i] = !selectedDice[i];
      updateUI();
    });
    diceRow.append(tile);
  }
}

function renderOptions(player) {
  optionsList.innerHTML = '';
  const options = player.getAvailableOptions();
  const showNeon = player.roll_left < 3;

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
    button.disabled =
      !option.available ||
      player.roll_left === 3 ||
      player.life !== 1 ||
      player.bot;
    button.addEventListener('click', () => {
      if (game.finished) return;
        player.performScore(option.id);
      player.checkScoreCard();
      const previousRound = game.round;
      game.advanceTurn();
      selectedDice = [false, false, false, false, false];
      updateUI();
    });
    card.append(button);
    optionsList.append(card);
  }
}

function renderSuggestion(player) {
  if (!player.cheat || player.life !== 1) {
    suggestionText.textContent = 'No suggestion available.';
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
  for (const player of game.players) {
    const card = document.createElement('div');
    card.className = 'player-card';
    const nameBlock = document.createElement('div');
    const statusLabel = player.life === 0 ? 'Forfeited' : player.bot ? 'Bot' : 'Human';
    nameBlock.innerHTML = `<strong>${player.name}</strong><span>${statusLabel}</span>`;
    const scoreBlock = document.createElement('div');
    const scoreLabel = player.life === 0 ? 'forfeited' : 'current total';
    scoreBlock.innerHTML = `<strong>${player.totalscore + player.score}</strong><span>${scoreLabel}</span>`;
    card.append(nameBlock, scoreBlock);
    scoreboardTable.append(card);
  }
}

function renderRoundBotLog() {
  if (!showBotDetails || !game?.finished || !roundBotLog.length) {
    roundBotLogPanel.classList.add('hidden');
    return;
  }

  roundBotLogPanel.classList.remove('hidden');
  roundBotLogContent.innerHTML = '';

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
    roundBotLogContent.append(details);
  }
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

function playBotTurn() {
  if (!game || game.finished) {
    return;
  }

  const player = game.currentPlayer();
  if (!player.bot || player.life !== 1) {
    return;
  }

  if (player.roll_left === 3) {
    player.botLog = [];
    player.rollDice();
    player.logBot(`Rolled: ${player.arrVal.join(' ')}`);
    updateUI();
    botTimeout = setTimeout(playBotTurn, 350);
    return;
  }

  if (player.roll_left >= 0) {
    const decision = player.getBotAction();
    if (decision > 0 && decision % 10 === 0 && player.roll_left > 0) {
      const rerollMask = decision;
      const rerollLabel = player.maskToDiceLetters(rerollMask).join(', ') || 'none';
      player.logBot(`Rerolling: ${rerollLabel}`);
      player.rollDecision = decision;
      player.roll_left -= 1;
      player.rollDice();
      player.logBot(`New dice: ${player.arrVal.join(' ')}`);
      updateUI();
      botTimeout = setTimeout(playBotTurn, 400);
      return;
    }

    const choiceLabel = CATEGORY_LABELS[decision] || `Choice ${decision}`;
    player.logBot(`Chooses: ${choiceLabel}`);
    const finalBotLog = player.botLog.slice();
    player.performScore(decision);
    player.checkScoreCard();
    const previousRound = game.round;
    game.advanceTurn();
    selectedDice = [false, false, false, false, false];

    if (showBotDetails && finalBotLog.length) {
      roundBotLog.push({ round: previousRound, name: player.name, lines: finalBotLog });
    }

    updateUI();
  }
}

function renderFinalSummary() {
  finalSummary.innerHTML = '';
  const ranking = game.getRanking();
  const average = Math.round(
    (game.players.reduce((sum, player) => sum + player.totalscore + player.score, 0) / game.players.length) * 10,
  ) / 10;

  ranking.forEach((entry, index) => {
    const row = document.createElement('div');
    row.className = 'summary-row';
    row.innerHTML = `
      <strong>${index + 1}. ${entry.name}</strong>
      <span>${entry.score} points</span>
    `;
    finalSummary.append(row);
  });

  const averageRow = document.createElement('div');
  averageRow.className = 'summary-row';
  averageRow.innerHTML = `<strong>Average score</strong><span>${average}</span>`;
  finalSummary.append(averageRow);
}

playerCountInput.addEventListener('change', () => {
  normalizePlayerCount();
  renderSetupOptions();
});
startGameButton.addEventListener('click', () => {
  game = buildGame();
  selectedDice = [false, false, false, false, false];
  showBotDetails = showBotDetailsToggle.checked;
  showBotDetailsToggle.disabled = true;
  roundBotLog = [];
  updateUI();
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
  const previousRound = game.round;
  player.performScore(CATEGORY.FORFEIT);
  game.advanceTurn();
  selectedDice = [false, false, false, false, false];
  updateUI();
});
showBotDetailsToggle.addEventListener('change', () => {
  if (game) return;
  showBotDetails = showBotDetailsToggle.checked;
});
backButtons.forEach((button) => {

  button.addEventListener('click', () => {
    if (botTimeout) {
      clearTimeout(botTimeout);
      botTimeout = null;
    }
    setupScreen.classList.remove('hidden');
    gameScreen.classList.add('hidden');
    gameOverScreen.classList.add('hidden');
    showBotDetailsToggle.disabled = false;
    game = null;
  });
});

renderSetupOptions();
