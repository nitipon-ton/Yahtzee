import java.util.Scanner;
public class Player {
	public int rollDecision; //decide to take point in which way or re-roll which dice
	public boolean haveChoice = false; //to skip play when no play available
	public int totalscore=0; //total score during the game
	public int life=1; //to cut players out of game that is less than 5(default)people
	public int score=0; //score from a single move
	public int roll_left=3; //roll left
	public boolean bot = false; //auto move for bot when bot = true
	public boolean cheat = false;
	public int yaht=1;
	public int toak=1; public int foak=1; public int fh=1; public int lgstr=1; public int smstr=1;public int chan=1;
	public boolean isFreeBasic[] = {true, true, true, true, true, true};
	double maxProb = 0; double[] simulProb= new double[31]; public int botDeci = 15;
	public boolean gotbonus = false;
	public int pntsToak=0;
	public int pntsFoak=0;
	public int pntsChan=0;
	public int bonus=0;
	public int yahtBo=0;
	public int[] pntsBasic = {0, 0, 0, 0, 0, 0};
	public int[] arrVal = new int[5];
	public int count1=0; public int count2=0; public int count3=0; public int count4=0; public int count5=0; public int count6=0;
	Dice dice = new Dice(6);
	public int userChoose;
	public double maxofArray(double[] arr)
	{
		double max=0;
		for(int i=0;i<arr.length;i++)
		{
			if(arr[i]>max) {max=arr[i];}
		}
		return max;
	}
	public int maxOfArr(int[] arr) {
		int max = 0;
		for (int i: arr) {
			if (i > max) {
				max = i;
			}
		}
		return max;
	}
	public int sumOfArr(int[] arr) {
		int sum = 0;
		for (int i: arr) {
			sum += i;
		}
		return sum;
	}
	public double probLgStr(int[] a, int userSimChoose) {
		double prob=0; double numberofReroll=0.0; int sum=0; int sumofsquare=0; int diffpair=0;
		int[] count= new int[6];
		int[] arr = new int[5];
		for (int i = 0; i < 5; i++) {
			arr[i] = a[i];
		}
		if(Math.floorDiv(userSimChoose,10)%2==1){arr[0]=0;numberofReroll++;}
		if(Math.floorDiv(userSimChoose,20)%2==1){arr[1]=0;numberofReroll++;}
		if(Math.floorDiv(userSimChoose,40)%2==1){arr[2]=0;numberofReroll++;}
		if(Math.floorDiv(userSimChoose,80)%2==1){arr[3]=0;numberofReroll++;}
		if(Math.floorDiv(userSimChoose,160)%2==1){arr[4]=0;numberofReroll++;}
		
		for(int i=0;i<=4;i++)
		{
			for(int j=0;j<=5;j++)
			{
				if(arr[i]==j+1)
				{
					count[j]++; //count[0] = count number of 1
				}
			}
		}
		
		for(int i=0;i<=4;i++)
		{
			sum+=arr[i]; sumofsquare+=arr[i]*arr[i];
			for(int j=0;j<=4;j++)
			{
				if(arr[i]*arr[j]>0&&arr[i]!=arr[j])
				{
					diffpair++;
				}
			}
		}
		
		if(numberofReroll==5)
		{
			prob=40.0/1296.0;
		}
		else if(numberofReroll==4)
		{
			if(sum==1|sum==6)
			{
				prob=4.0/216.0;
			}
			else
			{
				prob=8.0/216.0;
			}
		}
		else if(numberofReroll==3)
		{
			if(diffpair==2)
			{
				if(sumofsquare==37)
				{
					prob=0.0;
				}
				else if(sumofsquare==5||sumofsquare==10||sumofsquare==17||sumofsquare==26||sumofsquare==40||sumofsquare==45||sumofsquare==52||sumofsquare==61)
				{
					prob=6.0/216.0;
				}
				else
				{
					prob=12.0/216.0;
				}
			}
			else
			{
				prob=0;
			}
		}
		else if(numberofReroll==2)
		{
			if(count[0]*count[5]>0)
			{
				prob=0.0;
			}
			else if(diffpair<6)
			{
				prob=0.0;
			}
			else if(count[0]+count[5]==1)
			{
				prob=2.0/36.0;
			}
			else
			{
				prob=4.0/36.0;
			}
		}
		
		return prob;
	}
	public double probFOAK(int[] a, int userSimChoose) {
		double prob=0; double numberofReroll=0.0; int sum=0; int sumofsquare=0; int diffpair=0;
		int[] arr = new int[5];
		for (int i = 0; i < 5; i++) {
			arr[i] = a[i];
		}
		if(Math.floorDiv(userSimChoose,10)%2==1){arr[0]=0;numberofReroll++;}
		if(Math.floorDiv(userSimChoose,20)%2==1){arr[1]=0;numberofReroll++;}
		if(Math.floorDiv(userSimChoose,40)%2==1){arr[2]=0;numberofReroll++;}
		if(Math.floorDiv(userSimChoose,80)%2==1){arr[3]=0;numberofReroll++;}
		if(Math.floorDiv(userSimChoose,160)%2==1){arr[4]=0;numberofReroll++;}
		for(int i=0;i<=4;i++)
		{
			sum+=arr[i]; sumofsquare+=arr[i]*arr[i];
			for(int j=0;j<=4;j++)
			{
				if(arr[i]*arr[j]>0&&arr[i]!=arr[j])
				{
					diffpair++;
				}
			}
		}
		if(numberofReroll==5||numberofReroll==4)
		{
			prob=26.0/1296.0;
		}
		if(numberofReroll==3)
		{
			if(sum*sum==2*sumofsquare)
			{
				prob=16.0/216.0;
			}
			else
			{
				prob=2.0/216.0;
			}
		}
		if(numberofReroll==2)
		{
			if(diffpair==0)
			{
				prob=11.0/36.0;
			}
			else if(diffpair==4)
			{
				prob=1.0/36.0;
			}
			else if(diffpair==6)
			{
				prob=0.0;
			}
		}
		if(numberofReroll==1)
		{
			if(diffpair==0)
			{
				prob=1.0;
			}
			else if(diffpair==6)
			{
				prob=1.0/6.0;
			}
			else if(diffpair>6)
			{
				prob=0;
			}
		}
		return prob;
	}
	public double probYaht(int[] a, int userSimChoose) {
		double prob=0; double numberofReroll=0.0; int sum=0; int sumofsquare=0; int diffpair=0;
		int[] arr = new int[5];
		for (int i = 0; i < 5; i++) {
			arr[i] = a[i];
		}
		if(Math.floorDiv(userSimChoose,10)%2==1){arr[0]=0;numberofReroll++;}
		if(Math.floorDiv(userSimChoose,20)%2==1){arr[1]=0;numberofReroll++;}
		if(Math.floorDiv(userSimChoose,40)%2==1){arr[2]=0;numberofReroll++;}
		if(Math.floorDiv(userSimChoose,80)%2==1){arr[3]=0;numberofReroll++;}
		if(Math.floorDiv(userSimChoose,160)%2==1){arr[4]=0;numberofReroll++;}
		for(int i=0;i<=4;i++)
		{
			sum+=arr[i]; sumofsquare+=arr[i]*arr[i];
			for(int j=0;j<=4;j++)
			{
				if(arr[i]*arr[j]>0&&arr[i]!=arr[j])
				{
					diffpair++;
				}
			}
		}
		if(numberofReroll==4||numberofReroll==5) {
			prob=1.0/1296.0;
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
		double prob=0; double numberofReroll=0.0; int sum=0; int sumofsquare=0; int diffpair=0;
		int[] arr = new int[5];
		for (int i = 0; i < 5; i++) {
			arr[i] = a[i];
		}
		if(Math.floorDiv(userSimChoose,10)%2==1){arr[0]=0;numberofReroll++;}
		if(Math.floorDiv(userSimChoose,20)%2==1){arr[1]=0;numberofReroll++;}
		if(Math.floorDiv(userSimChoose,40)%2==1){arr[2]=0;numberofReroll++;}
		if(Math.floorDiv(userSimChoose,80)%2==1){arr[3]=0;numberofReroll++;}
		if(Math.floorDiv(userSimChoose,160)%2==1){arr[4]=0;numberofReroll++;}
		for(int i=0;i<=4;i++) {
			sum+=arr[i]; sumofsquare+=arr[i]*arr[i];
			for(int j=0;j<=4;j++) {
				if(arr[i]*arr[j]>0&&arr[i]!=arr[j]) {
					diffpair++;
				}
			}
		}
		if(numberofReroll==5||numberofReroll==4) {
			prob=46.0/216.0;
		}
		if(numberofReroll==3) {
			if(diffpair==0) {
				prob=96.0/216.0;
			} else if(diffpair==2) {
				prob=36.0/216.0;
			}
		}
		if(numberofReroll==2) {
			if(diffpair==0) {
				prob=1.0;
			} else if(diffpair==4) {
				prob=12.0/36.0;
			} else if(diffpair==6) {
				prob=3.0/36.0;
			}
		}
		if(numberofReroll==1) {
			if(diffpair==0) {
				prob=1.0;
			} else if(diffpair==6) {
				prob=1.0;
			} else if(diffpair==8) {
				prob=2.0/6.0;
			} else if(diffpair==10) {
				prob=1.0/6.0;
			} else if(diffpair==12) {
				prob=0.0;
			}
		}
		return prob;
	}
	public double probFH(int[] a, int userSimChoose) {
		double prob=0; double numberofReroll=0.0; int sum=0; int sumofsquare=0; int diffpair=0;
		int[] arr = new int[5];
		for (int i = 0; i < 5; i++) {
			arr[i] = a[i];
		}
		if(Math.floorDiv(userSimChoose,10)%2==1){arr[0]=0;numberofReroll++;}
		if(Math.floorDiv(userSimChoose,20)%2==1){arr[1]=0;numberofReroll++;}
		if(Math.floorDiv(userSimChoose,40)%2==1){arr[2]=0;numberofReroll++;}
		if(Math.floorDiv(userSimChoose,80)%2==1){arr[3]=0;numberofReroll++;}
		if(Math.floorDiv(userSimChoose,160)%2==1){arr[4]=0;numberofReroll++;}
		for(int i=0;i<=4;i++) {
			sum+=arr[i]; sumofsquare+=arr[i]*arr[i];
			for(int j=0;j<=4;j++) {
				if(arr[i]*arr[j]>0&&arr[i]!=arr[j]) {
					diffpair++;
				}
			}
		}
		if(numberofReroll==5||numberofReroll==4) {
			prob=50.0/1296.0;
		}
		if(numberofReroll==3) {
			if(diffpair==0) {
				prob=20.0/216.0;
			} else if(diffpair==2) {
				prob=6.0/216.0;
			}
		}
		if(numberofReroll==2) {
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
		int sumofsquareofCount=0; double forbash=0;
		if(Math.floorDiv(userSimChoose,10)%2==1){arr[0]=0;numberofReroll++;}
		if(Math.floorDiv(userSimChoose,20)%2==1){arr[1]=0;numberofReroll++;}
		if(Math.floorDiv(userSimChoose,40)%2==1){arr[2]=0;numberofReroll++;}
		if(Math.floorDiv(userSimChoose,80)%2==1){arr[3]=0;numberofReroll++;}
		if(Math.floorDiv(userSimChoose,160)%2==1){arr[4]=0;numberofReroll++;}
		
		for(int i=0;i<=4;i++)
		{
			for(int j=0;j<=5;j++)
			{
				if(arr[i]==j+1)
				{
					count[j]++; //count[0] = count number of 1
				}
			}
		}
		
		for(int i=0;i<=4;i++)
		{
			sum+=arr[i]; sumofsquare+=arr[i]*arr[i]; sumofsquareofCount+=count[0]*count[0];
			for(int j=0;j<=4;j++)
			{
				if(arr[i]*arr[j]>0&&arr[i]!=arr[j])
				{
					diffpair++;
				}
			}
		}
		
		if(numberofReroll==5)
		{
			prob=200.0/1296.0;
		}
		else if(numberofReroll==4)
		{
			if(sum==1||sum==6)
			{
				prob=132.0/1296.0;
			}
			else if(sum==2||sum==5)
			{
				prob=192.0/1296.0;
			}
			else
			{
				prob=276.0/1296.0;
			}
		}
		else if(numberofReroll==3)
		{
			if(sumofsquare==2||sumofsquare==72)
			{
				prob=6.0/216.0;
			}
			else if(sumofsquare==8||sumofsquare==50||sumofsquare==40||sumofsquare==26||sumofsquare==37)
			{
				prob=12.0/216.0;
			}
			else if(sumofsquare==18||sumofsquare==32)
			{
				prob=18.0/216.0;
			}
			else if(sumofsquare==61||sumofsquare==5||sumofsquare==29)
			{
				prob=30.0/216.0;
			}
			else if(sumofsquare==52||sumofsquare==10||sumofsquare==45||sumofsquare==17)
			{
				prob=36.0/216.0;
			}
			else if(sumofsquare==41||sumofsquare==13||sumofsquare==34||sumofsquare==20)
			{
				prob=54.0/216.0;
			}
			else if(sumofsquare==25)
			{
				prob=78.0/216.0;
			}
		}
		else if(numberofReroll==2)
		{
			if(diffpair==0)
			{
				prob=0.0;
			}
			else if(diffpair==4)
			{
				if(count[0]>0&&count[4]==0&&count[5]==0) //count[0] counts the number of 1 (not included those that are intended to re-roll)
				{
					prob=2.0/36.0;
				}
				else if(count[5]>0&&count[1]==0&&count[0]==0)
				{
					prob=2.0/36.0;
				}
				else if(count[3]*count[4]>0||count[3]*count[4]>0||count[1]*count[3]>0||count[2]*count[4]>0)
				{
					prob=4.0/36.0;
				}
				else if(count[1]*count[4]>0)
				{
					prob=2.0/36.0;
				}
				else if(count[2]*count[3]>0)
				{
					prob=6.0/36.0;
				}
				else
				{
					prob=0.0;
				}
			}
			else if(sumofsquare==14||sumofsquare==77||sumofsquare==21||sumofsquare==70||sumofsquare==38||sumofsquare==45||sumofsquare==38||sumofsquare==45)
			{
				prob=11.0/36.0;
			}
			else if(sumofsquare==35||sumofsquare==42||sumofsquare==49||sumofsquare==56||sumofsquare==46||sumofsquare==53)
			{
				prob=4.0/36.0;
			}
			else if(sumofsquare==26||sumofsquare==61)
			{
				prob=13.0/36.0;
			}
			else if(sumofsquare==30||sumofsquare==65||sumofsquare==41||sumofsquare==62)
			{
				prob=2.0/36.0;
			}
			else if(sumofsquare==29||sumofsquare==50)
			{
				prob=20.0/36.0;
			}
		} else if(numberofReroll==1) {
			for(int i=1;i<=6;i++) {
				for (int j=0;j<=4;j++) {
					if(arr[j]==0) {
						arr[j]=i;
						count[i-1]++;
						if(count[0]*count[1]*count[2]*count[3]+count[1]*count[2]*count[3]*count[4]+count[2]*count[3]*count[4]*count[5]>0) {
							forbash++;
						}
						count[i-1]--;
						arr[j]=0;
					}
				}
			}
			prob=forbash/6.0;
		}
		return prob;
	}
	public void checkScoreCard() {
		if (sumOfArr(pntsBasic) >= 63 && !gotbonus) {
			bonus=35;
			totalscore+=35;
			gotbonus = true;
		}
		System.out.print("\n");
		for (int i = 0; i < 6; i++) {
			System.out.print("______________\n|  " + (i + 1) + "s  |  "+ pntsBasic[i] + "  |\n");
		}
		System.out.print("______________\n|Bonus |  " + bonus + "  |\n______________\n______________\n");
		String[] titles = {"3ofAK ", "4ofAK ", "FullHS", "SmStr ", "LgStr ", "YahtZ ", "Chance", "YahtBo"};
		int pntsYaht = 0;
		if (yaht <= 0) {
			pntsYaht = 50;
		}
		int[] pnts = {pntsToak, pntsFoak, 25 * (1 - fh), 30 * (1 - smstr), 40 * (1 - lgstr), pntsYaht, pntsChan, yahtBo};
		for (int i = 0; i < 8; i++) {
			System.out.println("|" + titles[i] + "|  " + pnts[i] + "  |\n______________");
		}
		boolean basicDone = true;
		for (boolean b: isFreeBasic) {
			if (b) {
				basicDone = false;
				break;
			}
		}
		if(toak + foak + fh + lgstr + smstr + chan == 0 && basicDone && yaht <= 0) {
			System.out.println("SCORE CARD COMPLETED\nTotal score = " + totalscore + "\n");
			life = 2;
		} else {
			System.out.println("total score = " + totalscore + "\n");
		}
	}
	public void resetfornextround() {
		if (life == 1) {
			totalscore+=score;
			System.out.println("");
			score=0;
			haveChoice = false;
			roll_left=3;
			arrVal[0] = 0; arrVal[1] = 0; arrVal[2] = 0;  arrVal[3] = 0; arrVal[4] = 0;
			count1=0; count2=0; count3=0; count4=0; count5=0; count6=0;
		} else if (life==0) {
			System.out.print("(gone) --> ");
		}
	}
	public void rolldice() {
		if(life==1) {
			if(roll_left==-1) {
				roll_left=-2;
			} else if(roll_left<3&&roll_left>=0) {
				count1=0; count2=0; count3=0; count4=0; count5=0; count6=0;
				if(Math.floorDiv(rollDecision,10)%2==1) {
					arrVal[0] = dice.roll();
				}
				if(Math.floorDiv(rollDecision,20)%2==1) {
					arrVal[1] = dice.roll();
				}
				if(Math.floorDiv(rollDecision,40)%2==1) {
					arrVal[2] = dice.roll();
				}
				if(Math.floorDiv(rollDecision,80)%2==1) {
					arrVal[3] = dice.roll();
				}
				if(Math.floorDiv(rollDecision,160)%2==1) {
					arrVal[4] = dice.roll();
				}
				if(arrVal[0]==1){count1++;}if(arrVal[0]==2){count2++;}if(arrVal[0]==3){count3++;}if(arrVal[0]==4){count4++;}if(arrVal[0]==5){count5++;}if(arrVal[0]==6){count6++;}
				if(arrVal[1]==1){count1++;}if(arrVal[1]==2){count2++;}if(arrVal[1]==3){count3++;}if(arrVal[1]==4){count4++;}if(arrVal[1]==5){count5++;}if(arrVal[1]==6){count6++;}
				if(arrVal[2]==1){count1++;}if(arrVal[2]==2){count2++;}if(arrVal[2]==3){count3++;}if(arrVal[2]==4){count4++;}if(arrVal[2]==5){count5++;}if(arrVal[2]==6){count6++;}
				if(arrVal[3]==1){count1++;}if(arrVal[3]==2){count2++;}if(arrVal[3]==3){count3++;}if(arrVal[3]==4){count4++;}if(arrVal[3]==5){count5++;}if(arrVal[3]==6){count6++;}
				if(arrVal[4]==1){count1++;}if(arrVal[4]==2){count2++;}if(arrVal[4]==3){count3++;}if(arrVal[4]==4){count4++;}if(arrVal[4]==5){count5++;}if(arrVal[4]==6){count6++;}
			} else if(roll_left==3) {
				arrVal[0] = dice.roll(); arrVal[1] = dice.roll(); arrVal[2] = dice.roll(); arrVal[3] = dice.roll(); arrVal[4] = dice.roll();
				roll_left--;
				if(arrVal[0]==1){count1++;}if(arrVal[0]==2){count2++;}if(arrVal[0]==3){count3++;}if(arrVal[0]==4){count4++;}if(arrVal[0]==5){count5++;}if(arrVal[0]==6){count6++;}
				if(arrVal[1]==1){count1++;}if(arrVal[1]==2){count2++;}if(arrVal[1]==3){count3++;}if(arrVal[1]==4){count4++;}if(arrVal[1]==5){count5++;}if(arrVal[1]==6){count6++;}
				if(arrVal[2]==1){count1++;}if(arrVal[2]==2){count2++;}if(arrVal[2]==3){count3++;}if(arrVal[2]==4){count4++;}if(arrVal[2]==5){count5++;}if(arrVal[2]==6){count6++;}
				if(arrVal[3]==1){count1++;}if(arrVal[3]==2){count2++;}if(arrVal[3]==3){count3++;}if(arrVal[3]==4){count4++;}if(arrVal[3]==5){count5++;}if(arrVal[3]==6){count6++;}
				if(arrVal[4]==1){count1++;}if(arrVal[4]==2){count2++;}if(arrVal[4]==3){count3++;}if(arrVal[4]==4){count4++;}if(arrVal[4]==5){count5++;}if(arrVal[4]==6){count6++;}
			}
		}
	}
	public void chooseScore() {
		if(roll_left>=0&&life==1) {
			System.out.println("-------------------------------\n A  B  C  D  E");
			for (int i = 0; i < 5; i++) {
				System.out.print("[" + arrVal[i] + "]");
			}
			if (roll_left == 0) {
				System.out.print("  No More Re-Roll Left!!!");
			}
			System.out.print("\nScoring(s) available this game: ");
			if(toak==1) {System.out.print("3ofAK   ");}
			if(foak==1) {System.out.print("4ofAK   ");}
			if(fh==1) {System.out.print("FullHS   ");}
			if(smstr==1) {System.out.print("SmStrg   ");}
			if(lgstr==1) {System.out.print("LgStrg   ");}
			System.out.print("Yahtz   ");
			for (int i = 1; i <=6; i++) {
				if (isFreeBasic[i - 1]) {
					System.out.print(i + "s   ");
				}
			}
			if(chan==1) {System.out.print("Chance   ");}
			System.out.println("\nScoring(s) available this move");
			//code for each type of scoring
			int maxPoint=0;botDeci=15;
			if(count1 > 0 && isFreeBasic[0]) {
				System.out.print("Aces  : "); System.out.print(1*count1); System.out.print(" points ");
				System.out.println(": type 1 to select");
				haveChoice = true;
				if(1*count1>maxPoint) {
					maxPoint=1*count1;botDeci=1;
				}	
			}
			if(count2 > 0 && isFreeBasic[1]) {
				System.out.print("Twos  : "); System.out.print(2*count2); System.out.print(" points ");
				System.out.println(": type 2 to select");
				haveChoice = true;
				if(2*count2>maxPoint) {
					maxPoint=2*count2;botDeci=2;
				}
			}
			if(count3 > 0 && isFreeBasic[2]) {
				System.out.print("Threes: "); System.out.print(3*count3); System.out.print(" points ");
				System.out.println(": type 3 to select");
				haveChoice = true;
				if(3*count3>maxPoint) {
					maxPoint=3*count3;botDeci=3;
				}
			}
			if(count4 > 0 && isFreeBasic[3]) {
				System.out.print("Fours : "); System.out.print(4*count4); System.out.print(" points ");
				System.out.println(": type 4 to select");
				haveChoice = true;
				if(4*count4>maxPoint) {
					maxPoint=4*count4;botDeci=4;
				}
			}
			if(count5 > 0 && isFreeBasic[4]) {
				System.out.print("Fives : "); System.out.print(5*count5); System.out.print(" points ");
				System.out.println(": type 5 to select");
				haveChoice = true;
				if(5*count5>maxPoint) {
					maxPoint=5*count5;botDeci=5;
				}
			}
			if(count6 > 0 && isFreeBasic[5]) {
				System.out.print("Sixes : "); System.out.print(6*count6); System.out.print(" points ");
				System.out.println(": type 6 to select");
				haveChoice = true;
				if(6*count6>maxPoint) {
					maxPoint=6*count6;botDeci=6;
				}
			}
			int maxDup = maxOfArr(new int[]{count1,count2,count3,count4,count5,count6});
			if(maxDup == 5) {
				if(yaht>0) {
					System.out.print("Yahtz : 50 points ");System.out.println(": type 7 to select");
					haveChoice = true;
					if(50>maxPoint)
					{
						maxPoint=50;botDeci=7;
					}
				} else {
					System.out.print("Yahtz : 100 points ");System.out.println(": type 7 to select");
					haveChoice = true;
					if(100>maxPoint) {
						maxPoint=100;botDeci=7;
					}
				}
			}
			if(maxDup >= 4 && foak > 0) {
				System.out.print("4ofAK : "); System.out.print(arrVal[0]+arrVal[1]+arrVal[2]+arrVal[3]+arrVal[4]); System.out.print(" points ");
				System.out.println(": type 8 to select");
				haveChoice = true;
				if(arrVal[0]+arrVal[1]+arrVal[2]+arrVal[3]+arrVal[4]>maxPoint) {
					maxPoint=arrVal[0]+arrVal[1]+arrVal[2]+arrVal[3]+arrVal[4];botDeci=8;
				}
				
			}
			if(maxDup >= 3 && toak>0) {
				System.out.print("3ofAK : "); System.out.print(arrVal[0]+arrVal[1]+arrVal[2]+arrVal[3]+arrVal[4]); System.out.print(" points ");
				System.out.println(": type 9 to select");
				haveChoice = true;
				if(arrVal[0]+arrVal[1]+arrVal[2]+arrVal[3]+arrVal[4]>maxPoint) {
					maxPoint=arrVal[0]+arrVal[1]+arrVal[2]+arrVal[3]+arrVal[4];
					botDeci=9;
				}
				
			}
			if(chan>0) {
				System.out.print("Chance: "); System.out.print(arrVal[0]+arrVal[1]+arrVal[2]+arrVal[3]+arrVal[4]); System.out.print(" points ");
				System.out.println(": type 11 to select");
				haveChoice = true;
				if(arrVal[0]+arrVal[1]+arrVal[2]+arrVal[3]+arrVal[4]>maxPoint)
				{
					maxPoint=arrVal[0]+arrVal[1]+arrVal[2]+arrVal[3]+arrVal[4];
					botDeci=11;
				}
			}
			if(count1*count2*count3*count4+count2*count3*count4*count5+count3*count4*count5*count6>0&&smstr>0) {
				System.out.print("SmStrg: 30 points ");System.out.println(": type 12 to select");
				haveChoice = true;
				if(30>maxPoint) {
					maxPoint=30;
					botDeci=12;
				}
			}
			if(1==count2&&count2==count3&&count3==count4&&count4==count5&&lgstr>0) {
				System.out.print("LgStrg: 40 points ");System.out.println(": type 13 to select");
				haveChoice = true;
				if(40>maxPoint) {
					maxPoint=40;
					botDeci=13;
				}
			}
			if(count1*count1+count2*count2+count3*count3+count4*count4+count5*count5+count6*count6==13&&fh>0) {
				System.out.print("FullHS: 25 points ");System.out.println(": type 14 to select");
				haveChoice = true;
				if(25>maxPoint) {
					maxPoint=25;
					botDeci=14;
				}
			}
			if((botDeci==15||botDeci==6||botDeci==5||botDeci==4||botDeci==3||botDeci==2||botDeci==1)&&roll_left>=1) {
				boolean done = false;
				for (int j = 5; j >= 0; j--) {
					if(isFreeBasic[j] && !done) {
						botDeci=0;
						for(int i = 0; i <= 4; i++) {
							if (arrVal[i] != j + 1) {
								botDeci += 10 * Math.pow(2, i);
							}
						}
						done = true;
					}
				}
			}
			if(!haveChoice && roll_left == 0) {
				System.out.print("Skip! : 0 point ");System.out.println(": type 15 to select");
			}
			System.out.println("type 99 to delete this player");
			if(roll_left>0) {
				System.out.println("");
				System.out.println("FOR REROLL");
				System.out.println("THIS IS DICE VALUE");
				System.out.println("A=10_B=20_C=40_D=80_E=160");
				System.out.println("Insert the SUM OF DICE VALUE you want TO REROLL");
				///start of cheat code
				double maxProbAll=0;
				if(cheat) {
					System.out.println("SUGGESTION: ");
					if(toak>0) {
						System.out.print("max chance 3ofAK ---> "); maxProb=0;
						for(int i=1;i<=31;i++)
						{simulProb[i-1]=probTOAK(arrVal, (10*i)); maxProb = maxofArray(simulProb);}
						if(maxProb>0) {
							for(int i=1;i<=31;i++) {
								if(simulProb[i-1]==maxProb) {
									System.out.print(10*i);System.out.print(" ");
									if(maxProb>=maxProbAll&&maxPoint<24) {
										botDeci=10*i;
										maxProbAll=maxProb;
									}
								}
						    }
							System.out.println("\nprobability = " + maxProb);
						}
					}
					if(foak>0) {
						System.out.print("max chance 4ofAK ---> "); maxProb=0;
						for(int i=1;i<=31;i++)
						{simulProb[i-1]=probFOAK(arrVal, (10*i)); maxProb = maxofArray(simulProb);}
						if(maxProb>0) {
							for(int i=1;i<=31;i++) {
								if(simulProb[i-1]==maxProb) {
									System.out.print(10*i);System.out.print(" ");
									if(maxProb>=maxProbAll&&maxPoint<24) {
										botDeci=10*i;
										maxProbAll=maxProb;
									}
								}
						    }
							System.out.println("\nprobability = " + maxProb);
						}
					}
					if(fh>0) {
						System.out.print("max chance FullHS ---> "); maxProb=0;
						for(int i=1;i<=31;i++)
						{simulProb[i-1]=probFH(arrVal, (10*i)); maxProb = maxofArray(simulProb);}
						if(maxProb>0) {
							for(int i=1;i<=31;i++) {
								if(simulProb[i-1]==maxProb) {
									System.out.print(10*i);System.out.print(" ");
									if(maxProb>=maxProbAll&&maxPoint<24) {
										botDeci=10*i;
										maxProbAll=maxProb;
									}
								}
							}
							System.out.println("\nprobability = " + maxProb);
						}
					}
					if(smstr>0) {
						System.out.print("max chance SmStr ---> "); maxProb=0;
						for(int i=1;i<=31;i++)
						{simulProb[i-1]=probSmStr(arrVal, (10*i)); maxProb = maxofArray(simulProb);}
						if(maxProb>0) {
							for(int i=1;i<=31;i++) {
								if(simulProb[i-1]==maxProb) {
									System.out.print(10*i);System.out.print(" ");
									if(maxProb>=maxProbAll&&maxPoint<24) {
										botDeci=10*i;
										maxProbAll=maxProb;
									}
								}
							}
							System.out.println("\nprobability = " + maxProb);
						}
					}
					if(lgstr>0) {
						System.out.print("max chance LgStr ---> "); maxProb=0;
						for(int i=1;i<=31;i++)
						{simulProb[i-1]=probLgStr(arrVal, (10*i)); maxProb = maxofArray(simulProb);}
						if(maxProb>0) {
							for(int i=1;i<=31;i++) {
								if(simulProb[i-1]==maxProb) {
									System.out.print(10*i);System.out.print(" ");
									if(maxProb>=maxProbAll&&maxPoint<24) {
										botDeci=10*i;
										maxProbAll=maxProb;
									}
								}
							}
							System.out.println("\nprobability = " + maxProb);
						}
					}
					if(yaht>0) {
						System.out.print("max chance Yahtzee ---> "); maxProb=0;
						for(int i=1;i<=31;i++)
						{simulProb[i-1]=probYaht(arrVal, (10*i)); maxProb = maxofArray(simulProb);}
						if(maxProb>0) {
							for(int i=1;i<=31;i++) {
								if(simulProb[i-1]==maxProb) {
									System.out.print(10*i);System.out.print(" ");
									if(maxProb>=maxProbAll&&maxPoint<24) {
										botDeci=10*i;
										maxProbAll=maxProb;
									}
								}
						    }
							System.out.println("\nprobability = " + maxProb);
						}
					} else {
						System.out.print("max chance Yahtzee ---> "); maxProb=0;
						for(int i=1;i<=31;i++)
						{simulProb[i-1]=probYaht(arrVal, (10*i)); maxProb = maxofArray(simulProb);}
						if(maxProb>0) {
							for(int i=1;i<=31;i++) {
								if(simulProb[i-1]==maxProb) {
									System.out.print(10*i);System.out.print(" ");
									if(maxProb>=maxProbAll&&maxProb>=1.0/36.0&&maxPoint<24) {
										botDeci=10*i;
										maxProbAll=maxProb;
									}
								}
						    }
							System.out.println("\nprobability = " + maxProb);
						}
					}
						
				}
				///end of cheat code
			}
			Scanner kboard = new Scanner(System.in);
			for(int i=0;i<100;i++) {
				int userSim;
				if(!bot) {
					userSim = kboard.nextInt();
				} else {
					userSim=botDeci;
				}
				if (userSim%10==0) {
					int userDecideReroll=1;
					if(cheat) {
						{System.out.print("Yahtzee prob with this re-roll= ");System.out.println(probYaht(arrVal, userSim));}
						if(foak>0){System.out.print("4ofAK prob with this re-roll= ");System.out.println(probFOAK(arrVal, userSim));}
						if(toak>0){System.out.print("3ofAK prob with this re-roll= ");System.out.println(probTOAK(arrVal, userSim));}
						if(fh>0){System.out.print("FullHS prob with this re-roll= ");System.out.println(probFH(arrVal, userSim));}
						if(smstr>0){System.out.print("SmStr prob with this re-roll= ");System.out.println(probSmStr(arrVal, userSim));}
						if(lgstr>0){System.out.print("LgStr prob with this re-roll= ");System.out.println(probLgStr(arrVal, userSim));}
						System.out.print("type 1 to confirm decision----");System.out.println("type other number to change decision");
						Scanner rerollDecision = new Scanner(System.in);
						if (bot) {
							userDecideReroll=1;
						} else {
							userDecideReroll = rerollDecision.nextInt();
						}
					}
					if(userDecideReroll==1) {
						userChoose=userSim;
						i=100;
					} else {
						System.out.println("choose the re-roll again");
						userChoose=userSim;
					}
				} else {
					i=100;
					userChoose=userSim;
				}
			}
			if(roll_left>0&&userChoose%10==0&&userChoose>0) {
				rollDecision=userChoose;
				roll_left--;
			} else {
				if(count1*count2*count3*count4+count2*count3*count4*count5+count3*count4*count5*count6>0&&smstr>0&&userChoose==12) {
					score+=30;
					smstr--;
					System.out.println("Score: " + score);
					roll_left=-1;
				}
				if(arrVal[0]==arrVal[1]&&arrVal[1]==arrVal[2]&&arrVal[2]==arrVal[3]&&arrVal[3]==arrVal[4]&&yaht>0&&userChoose==7) {
					if(yaht>0) {
						score+=50;
						yaht--;
						System.out.println("Score: " + score);
						roll_left=-1;
					} else {
						score+=100;
						yahtBo+=100;
						yaht--;
						System.out.println("Score: " + score);
						roll_left=-1;
					}
				}
				if(Math.max(count6,Math.max(count5,Math.max(count4,Math.max(count3,Math.max(count2,count1)))))>=4&&foak>0&&userChoose==8) {
					pntsFoak=arrVal[0]+arrVal[1]+arrVal[2]+arrVal[3]+arrVal[4];
					score+=arrVal[0]+arrVal[1]+arrVal[2]+arrVal[3]+arrVal[4];
					foak--;
					System.out.println("Score: " + score);
					roll_left=-1;
				}
				if(Math.max(count6,Math.max(count5,Math.max(count4,Math.max(count3,Math.max(count2,count1)))))>=3&&toak>0&&userChoose==9) {
					pntsToak=arrVal[0]+arrVal[1]+arrVal[2]+arrVal[3]+arrVal[4];
					score+=arrVal[0]+arrVal[1]+arrVal[2]+arrVal[3]+arrVal[4];
					toak--;
					System.out.println("Score: " + score);
					roll_left=-1;
				}
				if(count2==count3&&count3==count4&&count4==count5&&lgstr>0&&userChoose==13) {
					score+=40;
					lgstr--;
					System.out.println("Score: " + score);
					roll_left=-1;
				}
				if(count1>0&&isFreeBasic[0]&&userChoose==1) {
					pntsBasic[0]=1*count1;
					score+=1*count1;
					isFreeBasic[0] = false;
					System.out.println("Score: " + score);
					roll_left=-1;
				}
				if(count2>0&&isFreeBasic[1]&&userChoose==2) {
					pntsBasic[1]=2*count2;
					score+=2*count2;
					isFreeBasic[1] = false;
					System.out.println("Score: " + score);
					roll_left=-1;
				}
				if(count3>0&&isFreeBasic[2]&&userChoose==3) {
					pntsBasic[2]=3*count3;
					score+=3*count3;
					isFreeBasic[2] = false;
					System.out.println("Score: " + score);
					roll_left=-1;
				}
				if(count4>0&&isFreeBasic[3]&&userChoose==4) {
					pntsBasic[3]=4*count4;
					score+=4*count4;
					isFreeBasic[3] = false;
					System.out.println("Score: " + score);
					roll_left=-1;
				}
				if(count5>0&&isFreeBasic[4]&&userChoose==5) {
					pntsBasic[4]=5*count5;
					score+=5*count5;
					isFreeBasic[4] = false;
					System.out.println("Score: " + score);
					roll_left=-1;
				}
				if(count6>0&&isFreeBasic[5]&&userChoose==6) {
					pntsBasic[5]=6*count6;
					score+=6*count6;
					isFreeBasic[5] = false;
					System.out.println("Score: " + score);
					roll_left=-1;
				}
				if(count1*count2*count3*count4+count2*count3*count4*count5+count3*count4*count5*count6>0&&smstr>0&&userChoose==12) {
					score+=30;
					smstr--;
					System.out.print("Score: "); System.out.println(score);
					roll_left=-1;
				}
				if(chan>0&&userChoose==11) {
					pntsChan=arrVal[0]+arrVal[1]+arrVal[2]+arrVal[3]+arrVal[4];
					score+=arrVal[0]+arrVal[1]+arrVal[2]+arrVal[3]+arrVal[4];
					chan--;
					System.out.print("Score: "); System.out.println(score);
					roll_left=-1;
				}
				if(count1*count1+count2*count2+count3*count3+count4*count4+count5*count5+count6*count6==13&&fh>0&&userChoose==14) {
					score+=25;
					fh--;
					System.out.print("Score: "); System.out.println(score);
					roll_left=-1;
				}
				if(!haveChoice && roll_left == 0 && userChoose == 15) {
					System.out.print("Score: "); System.out.println(score);
					roll_left=-1;
				}
				if(userChoose==99) {
					life--;
				}
			}
		}
	}
}