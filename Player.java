public class Player {
	private int rollDecision; //decide to take point in which way or re-roll which dice
	private boolean haveChoice = false; //to skip play when no play available
	public int totalscore = 0; //total score during the game
	public int life = 1; //to cut players out of the game
	private int score = 0; //score from a single move
	private int roll_left = 3;
	public boolean bot = false; //auto move for bot when bot = true
	public boolean cheat = false;
	private int yaht = 1; public int userChoose;
	private int[] isAvailAdv = {1, 1, 1, 1, 1}; //toak foak fh smstr lgstr
	private boolean chanAvail = true;
	private boolean[] isAvailBasic = {true, true, true, true, true, true};
	double maxProb = 0;
	double[] simulProb = new double[31];
	public int botDeci = 15;
	public boolean gotbonus = false;
	public int pntsToak=0; public int pntsFoak=0; public int pntsChan=0; public int bonus=0; public int yahtBo=0;
	public int[] pntsBasic = {0, 0, 0, 0, 0, 0};
	Dice dice = new Dice(6);
	public int[] arrVal = new int[5];
	public int[] faceCounter = new int[6];

	public void printNormal(String s) {
		System.out.print(s);
	}

	public void printHighlight(String s, String color) {
    	System.out.print("<span style='color:" + color + "'>" + s + "</span>");
	}
	
	public double probLgStr(int[] a, int userSimChoose) {
		double prob = 0; int numberofReroll = 0; int sum = 0; int sumofsquare = 0; int diffpair = 0;
		int[] count= new int[6];
		int[] arr = new int[5];
		for (int i = 0; i < 5; i++) {
			arr[i] = a[i];
		}
		int digit = 160;
		for (int i = 4; i >= 0; i--) {
			if (userSimChoose >= digit) {
				userSimChoose -= digit;
				arr[i] = 0;
				numberofReroll++;
			}
			digit /= 2;
		}
		for (int i = 0; i <= 4; i++) {
			if (arr[i] > 0) {
				count[arr[i]-1]++;
			}
		}
		for (int i = 0; i <= 4; i++) {
			sum += arr[i];
			sumofsquare += arr[i] * arr[i];
			for (int j = 0; j <= 4; j++) {
				if (arr[i] * arr[j] > 0 && arr[i] != arr[j]) {
					diffpair++;
				}
			}
		}
		if (numberofReroll==5) {
			prob = 40.0 / 1296.0;
		} else if (numberofReroll == 4) {
			if (sum == 1 || sum == 6) {
				prob = 4.0 / 216.0;
			} else {
				prob = 8.0 / 216.0;
			}
		} else if (numberofReroll == 3) {
			if (diffpair == 2) {
				if (sumofsquare == 37) {
					prob = 0.0;
				} else if (sumofsquare==5||sumofsquare==10||sumofsquare==17||sumofsquare==26||sumofsquare==40||sumofsquare==45||sumofsquare==52||sumofsquare==61) {
					prob = 6.0 / 216.0;
				} else {
					prob = 12.0 / 216.0;
				}
			} else {
				prob = 0;
			}
		} else if (numberofReroll == 2) {
			if ((count[0] > 0 && count[5] > 0) || diffpair < 6) {
				prob = 0.0;
			} else if (count[0] + count[5] == 1) {
				prob = 2.0 / 36.0;
			} else {
				prob = 4.0 / 36.0;
			}
		} else if (numberofReroll == 1) {
			if (diffpair < 12 || (count[0] > 0 && count[5] > 0)) {
				prob = 0.0;
			} else {
				prob = 1.0 / 6.0;
			}
		}
		return prob;
	}

	public double probFOAK(int[] a, int userSimChoose) {
		double prob = 0; int numberofReroll = 0; int sum = 0; int sumofsquare = 0; int diffpair = 0;
		int[] arr = new int[5];
		for (int i = 0; i < 5; i++) {
			arr[i] = a[i];
		}
		int digit = 160;
		for (int i = 4; i >= 0; i--) {
			if (userSimChoose >= digit) {
				userSimChoose -= digit;
				arr[i] = 0;
				numberofReroll++;
			}
			digit /= 2;
		}
		for (int i = 0; i <= 4; i++) {
			sum += arr[i];
			sumofsquare += arr[i] * arr[i];
			for (int j = 0; j <= 4; j++) {
				if (arr[i] * arr[j] > 0 && arr[i] != arr[j]) {
					diffpair++;
				}
			}
		}
		if (numberofReroll == 5 || numberofReroll == 4) {
			prob = 26.0 / 1296.0;
		}
		if (numberofReroll == 3) {
			if (sum * sum == 2 * sumofsquare) {
				prob = 16.0/216.0;
			} else {
				prob = 2.0/216.0;
			}
		}
		if (numberofReroll == 2) {
			if (diffpair == 0) {
				prob = 11.0 / 36.0;
			} else if (diffpair == 4) {
				prob = 1.0 / 36.0;
			} else if (diffpair == 6) {
				prob = 0.0;
			}
		}
		if (numberofReroll == 1) {
			if (diffpair == 0) {
				prob = 1.0;
			} else if (diffpair == 6) {
				prob = 1.0 / 6.0;
			} else if (diffpair > 6) {
				prob = 0.0;
			}
		}
		return prob;
	}

	public double probYaht(int[] a, int userSimChoose) {
		double prob = 0.0;
		double numberofReroll = 0.0;
		int diffpair = 0;
		int[] arr = new int[5];
		for (int i = 0; i < 5; i++) {
			arr[i] = a[i];
		}
		int digit = 160;
		for (int i = 4; i >= 0; i--) {
			if (userSimChoose >= digit) {
				userSimChoose -= digit;
				arr[i] = 0;
				numberofReroll++;
			}
			digit /= 2;
		}
		for (int i = 0; i <= 4; i++) {
			for (int j = 0; j <= 4; j++) {
				if (arr[i] * arr[j] > 0 && arr[i] != arr[j]) {
					diffpair++;
				}
			}
		}
		if (numberofReroll == 4 || numberofReroll == 5) {
			prob = 1.0/1296.0;
		} else {
			if(diffpair==0) {
				prob=Math.pow(1.0/6.0, numberofReroll);
			} else {
				prob=0;
			}
		}
		return prob;
	}

	public double probTOAK(int[] a, int userSimChoose) {
		double prob = 0; int numberofReroll = 0; int diffpair=0;
		int[] arr = new int[5];
		for (int i = 0; i < 5; i++) {
			arr[i] = a[i];
		}
		int digit = 160;
		for (int i = 4; i >= 0; i--) {
			if (userSimChoose >= digit) {
				userSimChoose -= digit;
				arr[i] = 0;
				numberofReroll++;
			}
			digit /= 2;
		}
		for (int i=0;i<=4;i++) {
			for(int j=0;j<=4;j++) {
				if(arr[i]*arr[j]>0&&arr[i]!=arr[j]) {
					diffpair++;
				}
			}
		}
		if (numberofReroll == 5 || numberofReroll == 4) {
			prob = 46.0 / 216.0;
		}
		if (numberofReroll == 3) {
			if(diffpair==0) {
				prob=96.0/216.0;
			} else if(diffpair==2) {
				prob=36.0/216.0;
			}
		}
		if (numberofReroll == 2) {
			if (diffpair == 0) {
				prob = 1.0;
			} else if (diffpair == 4) {
				prob = 12.0 / 36.0;
			} else if (diffpair == 6) {
				prob = 3.0 / 36.0;
			}
		}
		if (numberofReroll == 1) {
			if (diffpair == 0) {
				prob = 1.0;
			} else if (diffpair == 6) {
				prob = 1.0;
			} else if (diffpair == 8) {
				prob = 2.0/6.0;
			} else if (diffpair == 10) {
				prob = 1.0/6.0;
			} else if (diffpair == 12) {
				prob = 0.0;
			}
		}
		return prob;
	}

	public double probFH(int[] a, int userSimChoose) {
		double prob = 0; int numberofReroll = 0; int diffpair = 0;
		int[] arr = new int[5];
		for (int i = 0; i < 5; i++) {
			arr[i] = a[i];
		}
		int digit = 160;
		for (int i = 4; i >= 0; i--) {
			if (userSimChoose >= digit) {
				userSimChoose -= digit;
				arr[i] = 0;
				numberofReroll++;
			}
			digit /= 2;
		}
		for (int i = 0; i <= 4; i++) {
			for (int j = 0; j <= 4; j++) {
				if (arr[i] > 0 && arr[j] > 0 && arr[i] != arr[j]) {
					diffpair++;
				}
			}
		}
		if (numberofReroll == 5 || numberofReroll == 4) {
			prob=50.0/1296.0;
		}
		if (numberofReroll == 3) {
			if (diffpair == 0) {
				prob = 20.0/216.0;
			} else if (diffpair == 2) {
				prob = 6.0/216.0;
			}
		}
		if (numberofReroll == 2) {
			if(diffpair==0) {
				prob=5.0/36.0;
			} else if(diffpair==4) {
				prob=3.0/36.0;
			} else if(diffpair==6) {
				prob=0.0;
			}
		}
		if(numberofReroll==1) {
			if(diffpair==0||diffpair==10|diffpair==12) {
				prob=0.0;
			} else if(diffpair==6) {
				prob=1.0/6.0;
			} else if(diffpair==8) {
				prob=2.0/6.0;
			}
		}
		return prob;
	}
	public double probSmStr(int[] a, int userSimChoose) {
		double prob=0; double numberofReroll=0.0; int sum=0; int sumofsquare=0; int diffpair=0;
		int[] arr = new int[5];
		for (int i = 0; i < 5; i++) {
			arr[i] = a[i];
		}
		int[] count= new int[6];
		double forbash=0;
		int digit = 160;
		for (int i = 4; i >= 0; i--) {
			if (userSimChoose >= digit) {
				userSimChoose -= digit;
				arr[i] = 0;
				numberofReroll++;
			}
			digit /= 2;
		}
		for(int i=0;i<=4;i++) {
			for(int j=0;j<=5;j++) {
				if(arr[i]==j+1) {
					count[j]++;  // count[0] = count number of 1
				}
			}
		}
		for(int i=0;i<=4;i++) {
			sum += arr[i];
			sumofsquare += arr[i] * arr[i];
			for (int j = 0; j <= 4; j++) {
				if (arr[i] > 0 && arr[j] > 0 && arr[i] != arr[j]) {
					diffpair++;
				}
			}
		}
		if (numberofReroll == 5) {
			prob = 200.0 / 1296.0;
		} else if (numberofReroll == 4) {
			if (sum == 1 || sum == 6) {
				prob = 132.0 / 1296.0;
			} else if (sum == 2 || sum == 5) {
				prob = 192.0 / 1296.0;
			} else {
				prob = 276.0 / 1296.0;
			}
		} else if (numberofReroll==3) {
			if (sumofsquare == 2 || sumofsquare == 72) {
				prob = 6.0/216.0;
			} else if (sumofsquare == 8 || sumofsquare == 50 || sumofsquare == 40 || sumofsquare == 26 || sumofsquare == 37) {
				prob = 12.0/216.0;
			} else if (sumofsquare==18||sumofsquare==32) {
				prob = 18.0/216.0;
			} else if (sumofsquare==61||sumofsquare==5||sumofsquare==29) {
				prob = 30.0/216.0;
			} else if (sumofsquare==52||sumofsquare==10||sumofsquare==45||sumofsquare==17) {
				prob = 36.0/216.0;
			} else if (sumofsquare==41||sumofsquare==13||sumofsquare==34||sumofsquare==20) {
				prob = 54.0/216.0;
			} else if (sumofsquare==25) {
				prob = 78.0/216.0;
			}
		} else if (numberofReroll==2) {
			if (diffpair==0) {
				prob=0.0;
			} else if (diffpair==4) {
				// count[0] counts the number of 1 (not included those we want to re-roll)
				if (count[0]>0&&count[4]==0&&count[5]==0) {
					prob = 2.0/36.0;
				} else if (count[5] > 0 && count[1]==0 && count[0]==0) {
					prob = 2.0/36.0;
				} else if (count[3]*count[4]>0||count[3]*count[4]>0||count[1]*count[3]>0||count[2]*count[4]>0) {
					prob = 4.0/36.0;
				} else if (count[1] > 0 && count[4] > 0) {
					prob = 2.0/36.0;
				} else if (count[2] > 0 && count[3] > 0) {
					prob = 6.0/36.0;
				} else {
					prob = 0.0;
				}
			} else if(sumofsquare==14||sumofsquare==77||sumofsquare==21||sumofsquare==70||sumofsquare==38||sumofsquare==45||sumofsquare==38||sumofsquare==45) {
				prob=11.0/36.0;
			} else if(sumofsquare==35||sumofsquare==42||sumofsquare==49||sumofsquare==56||sumofsquare==46||sumofsquare==53) {
				prob=4.0/36.0;
			} else if(sumofsquare==26||sumofsquare==61) {
				prob=13.0/36.0;
			} else if(sumofsquare==30||sumofsquare==65||sumofsquare==41||sumofsquare==62) {
				prob=2.0/36.0;
			} else if(sumofsquare==29||sumofsquare==50) {
				prob=20.0/36.0;
			}
		} else if(numberofReroll==1) {
			for(int i=1;i<=6;i++) {
				for (int j=0;j<=4;j++) {
					if(arr[j] == 0) {
						arr[j] = i;
						count[i - 1]++;
						if(count[0]*count[1]*count[2]*count[3]+count[1]*count[2]*count[3]*count[4]+count[2]*count[3]*count[4]*count[5]>0) {
							forbash++;
						}
						count[i - 1]--;
						arr[j] = 0;
					}
				}
			}
			prob = forbash / 6.0;
		}
		return prob;
	}
	public void checkScoreCard() {
		if (Helper.sumArr(pntsBasic) >= 63 && !gotbonus) {
			bonus = 35;
			totalscore += 35;
			gotbonus = true;
		}
		System.out.print("\n");
		for (int i = 0; i < 6; i++) {
			System.out.print("______________\n|  " + (i + 1) + "s  |  "+ pntsBasic[i] + "  |");
			if (isAvailBasic[i]) {
				System.out.print(" FREE\n");
			} else {
				System.out.print(" USED\n");
			}
		}
		System.out.print("______________\n|Bonus |  " + bonus + "  |\n______________\n");
		String[] titles = {"  3 of a kind ", "  4 of a kind ", "  Full House  ", "Small Straight", "Large Straight", "   YahtZee    ", "    Chance    ", "  YahtZ Bonus "};
		int pntsYaht = 0;
		if (yaht <= 0) {
			pntsYaht = 50;
		}
		int[] pnts = {pntsToak, pntsFoak, 25 * (1 - isAvailAdv[2]), 30 * (1 - isAvailAdv[3]), 40 * (1 - isAvailAdv[4]), pntsYaht, pntsChan, yahtBo};
		for (int i = 2; i <= 4; i++) {
			if (isAvailAdv[i] < 0) {
				pnts[i] = 0;
			}
		}
		boolean[] scoredAdv = {isAvailAdv[0] > 0, isAvailAdv[1] > 0, isAvailAdv[2] > 0, isAvailAdv[3] > 0, isAvailAdv[4] > 0, yaht == 1, chanAvail, yaht != 100};
		for (int i = 0; i < 8; i++) {
			System.out.print("_____________________\n|" + titles[i] + "|  " + pnts[i] + "  |");
			if (scoredAdv[i]) {
				System.out.print(" FREE\n");
			} else {
				System.out.print(" USED\n");
			}
		}
		boolean basicDone = true;
		for (int i: pntsBasic) {
			if (i == 0) {
				basicDone = false;
				break;
			}
		}
		boolean advDone = true;
		for (int i: isAvailAdv) {
			if (i != 0) {
				advDone = false;
				break;
			}
		}
		if (advDone && !chanAvail && basicDone && yaht <= 0) {
			System.out.println("\nSCORE CARD COMPLETED\nTotal score = " + totalscore + "\n");
			life = 2;
		} else {
			System.out.println("\ntotal score = " + totalscore + "\n");
		}
	}
	public void resetfornextround() {
		if (life == 1) {
			totalscore+=score;
			System.out.println("");
			score = 0;
			haveChoice = false;
			roll_left = 3;
			arrVal[0] = 0; arrVal[1] = 0; arrVal[2] = 0;  arrVal[3] = 0; arrVal[4] = 0;
			faceCounter[0]=0; faceCounter[1]=0; faceCounter[2]=0; faceCounter[3]=0; faceCounter[4]=0; faceCounter[5]=0;
		} else if (life==0) {
			System.out.print("(gone) --> ");
		}
	}
	public void rollDice() {
		if (life == 1) {
			if (roll_left < 3 && roll_left >= 0) {
				faceCounter[0] = 0; faceCounter[1]=0; faceCounter[2]=0; faceCounter[3]=0; faceCounter[4]=0; faceCounter[5]=0;
				int rollDeciTemp = rollDecision;
				int digit = 160;
				for (int i = 4; i >= 0; i--) {
					if (rollDeciTemp >= digit) {
						rollDeciTemp -= digit;
						arrVal[i] = dice.roll();
					}
					digit /= 2;
				}
				for (int i = 0; i < 5; i++) {
					faceCounter[arrVal[i] - 1] ++;
				}
			} else if(roll_left == 3) {
				roll_left--;
				for (int i = 0; i < 5; i++) {
					arrVal[i] = dice.roll();
					faceCounter[arrVal[i] - 1] ++;
				}
			}
		}
	}

	public void chooseScore() {
		if (roll_left >= 0 && life == 1) {
			System.out.println("----------------\n\n A  B  C  D  E");
			for (int i = 0; i < 5; i++) {
				System.out.print("[" + arrVal[i] + "]");
			}
			if (roll_left == 0) {
				System.out.print("  No More Re-Roll Left!!!");
			}
			System.out.println("\n\n   Scoring(s) available\n");
			// code for each type of scoring
			int maxPoint = -1; botDeci = 15;
			for (int i = 1; i <= 6; i++) {
				if (isAvailBasic[i - 1]) {
					if (faceCounter[i - 1] > 0) {
						System.out.println("      " + i + "s      : " + i * faceCounter[i - 1] + " points");
					} else {
						System.out.println("      " + i + "s      : " + 0 + " points");
					}
					haveChoice = true;
					if(i * faceCounter[i - 1] > maxPoint) {
						maxPoint = i * faceCounter[i - 1];
						botDeci = i;
					}
				}
			}
			
			/* */
			int maxDup = Helper.maxArr(faceCounter);
			if (maxDup == 5 && yaht != 100) {
				int yahtScore = 50;
				if (yaht <= 0) {
					yahtScore = 100;
				}
				System.out.println("    YahtZee   : " + yahtScore + " points");
				haveChoice = true;
				if (yahtScore > maxPoint) {
					maxPoint = yahtScore;
					botDeci = 7;
				}
			} else if (yaht != 100) {
				System.out.println("    YahtZee   : " + 0 + " points");
				if (maxPoint == -1) {
					maxPoint = 0;
					botDeci = 7;
				}
			}

			if (maxDup >= 4 && isAvailAdv[1] > 0) {
				System.out.println(" 4 of a kind  : " + Helper.sumArr(arrVal) + " points");
				haveChoice = true;
				if(Helper.sumArr(arrVal) > maxPoint) {
					maxPoint = Helper.sumArr(arrVal);
					botDeci = 8;
				}
			} else if (isAvailAdv[1] > 0) {
				System.out.println(" 4 of a kind  : " + 0 + " points");
				if (maxPoint <= 0) {
					maxPoint = 0;
					botDeci = 8;
				}
			}

			if(maxDup >= 3 && isAvailAdv[0] > 0) {
				System.out.println(" 3 of a kind  : " + Helper.sumArr(arrVal) + " points");
				haveChoice = true;
				if(Helper.sumArr(arrVal) > maxPoint) {
					maxPoint = Helper.sumArr(arrVal);
					botDeci = 9;
				}
			} else if (isAvailAdv[0] > 0) {
				System.out.println(" 3 of a kind  : " + 0 + " points");
				if (maxPoint <= 0) {
					maxPoint = 0;
					botDeci = 9;
				}
			}

			if(chanAvail) {
				System.out.println("    Chance    : " + Helper.sumArr(arrVal) + " points");
				haveChoice = true;
				if(Helper.sumArr(arrVal) > maxPoint) {
					maxPoint = Helper.sumArr(arrVal);
					botDeci = 11;
				}
			}

			if(faceCounter[0]*faceCounter[1]*faceCounter[2]*faceCounter[3]+faceCounter[1]*faceCounter[2]*faceCounter[3]*faceCounter[4]+faceCounter[2]*faceCounter[3]*faceCounter[4]*faceCounter[5]>0&&isAvailAdv[3]>0) {
				System.out.println("Small Straight: 30 points");
				haveChoice = true;
				if (30 > maxPoint) {
					maxPoint = 30;
					botDeci = 12;
				}
			} else if (isAvailAdv[3] > 0) {
				System.out.println("Small Straight: 0 points");
				if (maxPoint <= 0) {
					maxPoint = 0;
					botDeci = 12;
				}
			}

			if(1==faceCounter[1]&&faceCounter[1]==faceCounter[2]&&faceCounter[2]==faceCounter[3]&&faceCounter[3]==faceCounter[4]&&isAvailAdv[4]>0) {
				System.out.println("Large Straight: 40 points");
				haveChoice = true;
				if(40>maxPoint) {
					maxPoint = 40;
					botDeci = 13;
				}
			} else if (isAvailAdv[4] > 0) {
				System.out.println("Large Straight: 0 points");
				if (maxPoint <= 0) {
					maxPoint = 0;
					botDeci = 13;
				}
			}

			if(faceCounter[0]*faceCounter[0]+faceCounter[1]*faceCounter[1]+faceCounter[2]*faceCounter[2]+faceCounter[3]*faceCounter[3]+faceCounter[4]*faceCounter[4]+faceCounter[5]*faceCounter[5]==13&&isAvailAdv[2]>0) {
				System.out.println("  Full House  : 25 points");
				haveChoice = true;
				if (25 > maxPoint) {
					maxPoint = 25;
					botDeci = 14;
				}
			} else if (isAvailAdv[2] > 0) {
				System.out.println("  Full House  : 0 points");
				if (maxPoint <= 0) {
					maxPoint = 0;
					botDeci = 14;
				}
			}

			if((maxPoint == 0 || (botDeci >= 1 && botDeci <= 6)) && roll_left >= 1) {
				boolean done = false;
				for (int j = 5; j >= 0; j--) {
					if (isAvailBasic[j] && !done) {
						botDeci = 0;
						for(int i = 0; i <= 4; i++) {
							if (arrVal[i] != j + 1) {
								botDeci += 10 * Math.pow(2, i);
							}
						}
						done = true;
					}
				}
			}
			if (roll_left > 0) {
				// start of cheat code
				double maxProbAll=0;
				if(cheat) {
					System.out.println("\nREROLL SUGGESTION WITH PROBABILITY:\n");
                    double maxProb = 0;
                    if(isAvailAdv[0]>0) {
						System.out.print("Max chance Three of a kind ---> ");
						maxProb =0;
						for (int i=1; i <= 31; i++) {
							simulProb[i-1] = probTOAK(arrVal, (10*i));
							maxProb = Helper.maxArr(simulProb);
						}
						if (maxProb > 0) {
							for (int i = 1; i <= 31; i++) {
								if(simulProb[i-1] == maxProb) {
									int tempI = i;
									for (int j = 0; j < 5; j++) {
										if (tempI % 2 == 1) {
											System.out.print("ABCDE".charAt(j));
										}
										tempI /= 2;
									}
									System.out.print(" ");
									if(maxProb >= maxProbAll&&maxPoint<24) {
										botDeci = 10*i;
										maxProbAll = maxProb;
									}
								}
						    }
							System.out.println("\nprobability = " + maxProb * 100 + " %\n");
						}
					}
					if (isAvailAdv[1] > 0) {
						System.out.print("Max chance Four of a kind ---> "); maxProb =0;
						for(int i=1;i<=31;i++)
						{simulProb[i-1]=probFOAK(arrVal, (10*i)); maxProb = Helper.maxArr(simulProb);}
						if(maxProb >0) {
							for(int i=1;i<=31;i++) {
								if(simulProb[i-1]== maxProb) {
									int tempI = i;
									for (int j = 0; j < 5; j++) {
										if (tempI % 2 == 1) {
											System.out.print("ABCDE".charAt(j));
										}
										tempI /= 2;
									}
									System.out.print(" ");
									if(maxProb >=maxProbAll&&maxPoint<24) {
										botDeci=10*i;
										maxProbAll= maxProb;
									}
								}
						    }
							System.out.println("\nprobability = " + maxProb * 100 + " %\n");
						}
					}
					if (isAvailAdv[2] > 0) {
						System.out.print("Max chance Full House ---> "); maxProb =0;
						for(int i=1;i<=31;i++)
						{simulProb[i-1]=probFH(arrVal, (10*i)); maxProb = Helper.maxArr(simulProb);}
						if(maxProb >0) {
							for(int i=1;i<=31;i++) {
								if(simulProb[i-1]== maxProb) {
									int tempI = i;
									for (int j = 0; j < 5; j++) {
										if (tempI % 2 == 1) {
											System.out.print("ABCDE".charAt(j));
										}
										tempI /= 2;
									}
									System.out.print(" ");
									if(maxProb >=maxProbAll&&maxPoint<24) {
										botDeci=10*i;
										maxProbAll= maxProb;
									}
								}
							}
							System.out.println("\nprobability = " + maxProb * 100 + " %\n");
						}
					}
					if (isAvailAdv[3] > 0) {
						System.out.print("Max chance Small Straight ---> "); maxProb =0;
						for(int i=1;i<=31;i++)
						{simulProb[i-1]=probSmStr(arrVal, (10*i)); maxProb = Helper.maxArr(simulProb);}
						if(maxProb >0) {
							for(int i=1;i<=31;i++) {
								if(simulProb[i-1]== maxProb) {
									int tempI = i;
									for (int j = 0; j < 5; j++) {
										if (tempI % 2 == 1) {
											System.out.print("ABCDE".charAt(j));
										}
										tempI /= 2;
									}
									System.out.print(" ");
									if(maxProb >=maxProbAll&&maxPoint<24) {
										botDeci=10*i;
										maxProbAll= maxProb;
									}
								}
							}
							System.out.println("\nprobability = " + maxProb * 100 + " %\n");
						}
					}
					if (isAvailAdv[4] > 0) {
						System.out.print("Max chance Large Straight ---> "); maxProb =0;
						for(int i=1;i<=31;i++)
						{simulProb[i-1]=probLgStr(arrVal, (10*i)); maxProb = Helper.maxArr(simulProb);}
						if(maxProb >0) {
							for(int i=1;i<=31;i++) {
								if(simulProb[i-1]== maxProb) {
									int tempI = i;
									for (int j = 0; j < 5; j++) {
										if (tempI % 2 == 1) {
											System.out.print("ABCDE".charAt(j));
										}
										tempI /= 2;
									}
									System.out.print(" ");
									if(maxProb >=maxProbAll&&maxPoint<24) {
										botDeci=10*i;
										maxProbAll= maxProb;
									}
								}
							}
							System.out.println("\nprobability = " + maxProb * 100 + " %\n");
						}
					}
					System.out.print("Max chance Yahtzee ---> "); maxProb =0;
					for (int i = 1; i <= 31; i++){	
						simulProb[i - 1] = probYaht(arrVal, (10 * i));
						maxProb = Helper.maxArr(simulProb);
					}
					if (maxProb > 0) {
						for(int i=1;i<=31;i++) {
							if(simulProb[i-1]== maxProb) {
								int tempI = i;
								for (int j = 0; j < 5; j++) {
									if (tempI % 2 == 1) {
										System.out.print("ABCDE".charAt(j));
									}
									tempI /= 2;
								}
								System.out.print(" ");
								if(maxProb >= maxProbAll && maxPoint<24) {
									botDeci=10*i;
									maxProbAll= maxProb;
								}
							}
						}
						System.out.println("\nprobability = " + maxProb * 100 + " %\n");
					}
				}
				// end of cheat code
			}
			boolean isDeciding = true;
			while (isDeciding) {
				int userSim;
				userSim = botDeci;
				if (userSim % 10 == 0) {
					System.out.print("Bot Reroll Decision: ");
					int tempI = userSim / 10;
					for (int j = 0; j < 5; j++) {
						if (tempI % 2 == 1) {
							System.out.print("ABCDE".charAt(j));
						}
						tempI /= 2;
					}
					System.out.println(" \n");
				} else {
					String[] scorings = new String[]{"1s", "2s", "3s", "4s", "5s", "6s", "YahtZee", "4 of a kind", "3 of a kind", "XXX", "Chance", "Small Straight", "Large Straight", "Full House"};
					System.out.println("\nBot Score Decision: " + scorings[userSim - 1] + "\n");
				}

				if (userSim % 10 == 0) {
					int userDecideReroll = 1;
					if (cheat) {
						System.out.println("Yahtzee prob with this re-roll = " + probYaht(arrVal, userSim) * 100 + " %");
						if(isAvailAdv[0]>0){System.out.println("3 of a kind prob with this re-roll = " + probTOAK(arrVal, userSim) * 100 + " %");}
						if(isAvailAdv[1]>0){System.out.println("4 of a kind prob with this re-roll = " + probFOAK(arrVal, userSim) * 100 + " %");}
						if(isAvailAdv[2]>0){System.out.println("Full House prob with this re-roll = " + probFH(arrVal, userSim) * 100 + " %");}
						if(isAvailAdv[3]>0){System.out.println("Small Straight prob with this re-roll = " + probSmStr(arrVal, userSim) * 100 + " %");}
						if(isAvailAdv[4]>0){System.out.println("Large Straight prob with this re-roll = " + probLgStr(arrVal, userSim) * 100 + " %");}
					}
					if (userDecideReroll == 1) {
						isDeciding = false;
					}
					userChoose = userSim;
				} else {
					isDeciding = false;
					userChoose = userSim;
				}
			}
			if (roll_left > 0 && userChoose % 10 == 0 && userChoose > 0) {
				rollDecision = userChoose;
				roll_left--;
			} else {
				if (faceCounter[0]*faceCounter[1]*faceCounter[2]*faceCounter[3]+faceCounter[1]*faceCounter[2]*faceCounter[3]*faceCounter[4]+faceCounter[2]*faceCounter[3]*faceCounter[4]*faceCounter[5]>0&&isAvailAdv[3]>0&&userChoose==12) {
					score += 30;
					isAvailAdv[3]--;
					System.out.println("Score: " + score);
					roll_left = -1;
				} else if (isAvailAdv[3] > 0 && userChoose == 12) {
					System.out.println("Score: " + score);
					isAvailAdv[3] -= 2;
					roll_left = -1;
				}

				if (arrVal[0]==arrVal[1]&&arrVal[1]==arrVal[2]&&arrVal[2]==arrVal[3]&&arrVal[3]==arrVal[4] && userChoose==7) {
					if (yaht > 0) {
						score += 50;
                    } else {
						score += 100;
						yahtBo += 100;
                    }
                    yaht--;
                    System.out.println("Score: " + score);
                    roll_left = -1;
                } else if (userChoose == 7) {
					yaht = 100; // To prevent re-choosing yahtzee again
					System.out.println("Score: " + score);
					roll_left = -1;
				}

				if(Helper.maxArr(faceCounter) >= 4 && isAvailAdv[1] > 0 && userChoose == 8) {
					pntsFoak = Helper.sumArr(arrVal);
					score += pntsFoak;
					isAvailAdv[1]--;
					System.out.println("Score: " + score);
					roll_left = -1;
				} else if (isAvailAdv[1] > 0 && userChoose == 8) {
					System.out.println("Score: " + score);
					isAvailAdv[1] -= 2;
					roll_left = -1;
				}

				if(Helper.maxArr(faceCounter) >= 3 && isAvailAdv[0] > 0 && userChoose == 9) {
					pntsToak = Helper.sumArr(arrVal);
					score += pntsToak;
					isAvailAdv[0]--;
					System.out.println("Score: " + score);
					roll_left = -1;
				} else if (isAvailAdv[0] > 0 && userChoose == 9) {
					System.out.println("Score: " + score);
					isAvailAdv[0] -= 2;
					roll_left = -1;
				}

				if(faceCounter[1]==faceCounter[2]&&faceCounter[2]==faceCounter[3]&&faceCounter[3]==faceCounter[4]&&isAvailAdv[4]>0&&userChoose==13) {
					score += 40;
					isAvailAdv[4]--;
					System.out.println("Score: " + score);
					roll_left = -1;
				} else if (isAvailAdv[4] > 0 && userChoose == 13) {
					System.out.println("Score: " + score);
					isAvailAdv[4] -= 2;
					roll_left = -1;
				}

				for (int i = 0; i < 6; i++) {
					if (faceCounter[i] > 0 && isAvailBasic[i] && userChoose == i + 1) {
						pntsBasic[i] = (i + 1) * faceCounter[i];
						score += pntsBasic[i];
						isAvailBasic[i] = false;
						System.out.println("Score: " + score);
						roll_left = -1;
					} else if (isAvailBasic[i] && userChoose == i + 1) {
						System.out.println("Score: " + score);
						isAvailBasic[i] = false;
						roll_left = -1;
					}
				}

				if (faceCounter[0]*faceCounter[1]*faceCounter[2]*faceCounter[3]+faceCounter[1]*faceCounter[2]*faceCounter[3]*faceCounter[4]+faceCounter[2]*faceCounter[3]*faceCounter[4]*faceCounter[5]>0&&isAvailAdv[3]>0&&userChoose==12) {
					score += 30;
					isAvailAdv[3]--;
					System.out.println("Score: " + score);
					roll_left = -1;
				} else if (isAvailAdv[3] > 0 && userChoose == 12) {
					System.out.println("Score: " + score);
					isAvailAdv[3] -= 2;
					roll_left = -1;
				}

				if (chanAvail && userChoose == 11) {
					pntsChan = Helper.sumArr(arrVal);
					score += pntsChan;
					chanAvail = false;
					System.out.println("Score: " + score);
					roll_left = -1;
				}

				if (faceCounter[0]*faceCounter[0]+faceCounter[1]*faceCounter[1]+faceCounter[2]*faceCounter[2]+faceCounter[3]*faceCounter[3]+faceCounter[4]*faceCounter[4]+faceCounter[5]*faceCounter[5]==13&&isAvailAdv[2]>0&&userChoose==14) {
					score += 25;
					isAvailAdv[2]--;
					System.out.println("Score: " + score);
					roll_left = -1;
				} else if (isAvailAdv[2] > 0 && userChoose == 14) {
					System.out.println("Score: " + score);
					isAvailAdv[2] -= 2;
					roll_left = -1;
				}

				if (!haveChoice && roll_left == 0 && userChoose == 15) {
					System.out.println("Score: " + score);
					roll_left = -1;
				}
			}
		}
	}
}