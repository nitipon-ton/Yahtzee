import java.util.Scanner;
public class Player
{
	public int rollDecision; //decide to take point in which way or re-roll which dice
	public int haveChoice=0; //to skip play when no play available
	public int totalscore=0; //total score during the game
	public int life=1; //to cut players out of game that is less than 5(default)people
	public int score=0; //score from a single move
	public int roll_left=3; //roll left
	public int bot=0; //auto move for bot when bot=1
	
	public int yaht=1; public int toak=1; public int foak=1; public int fh=1; public int lgstr=1; public int smstr=1;
	public int chan=1; public int ace=1; public int two=1; public int three=1; public int four=1; public int five=1; public int six=1;
	double maxProb=0; double []simulProb= new double[31]; public int botDeci=15; public int gotbonus=0;
	
	public int pntsToak=0; public int pntsFoak=0; public int pntsChan=0; public int bonus=0; public int yahtBo=0;
	public int pnts1=0; public int pnts2=0; public int pnts3=0; public int pnts4=0; public int pnts5=0; public int pnts6=0; 
	
	public int valA = 0; public int valB = 0; public int valC = 0; public int valD = 0; public int valE = 0; public int []arrVal= new int[5];
	public int count1=0; public int count2=0; public int count3=0; public int count4=0; public int count5=0; public int count6=0;
	Dice a = new Dice(6); Dice b = new Dice(6); Dice c = new Dice(6); Dice d = new Dice(6); Dice e = new Dice(6);
	public int cheat=0;
	
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
	
	public int maxofSix(int a, int b, int c, int d, int e, int f)
	{
		int max=0;
		if(a>max) {max=a;}
		if(b>max) {max=b;}
		if(c>max) {max=c;}
		if(d>max) {max=d;}
		if(e>max) {max=e;}
		if(f>max) {max=f;}
		return max;
	}
	///////////////////////////////////////////////////////////////////
	public double probLgStr(int a,int b,int c,int d,int e,int userSimChoose)
	{
		double prob=0; double numberofReroll=0.0; int sum=0; int sumofsquare=0; int diffpair=0; int []arr= new int[5]; int []count= new int[6];
		arr[0]=a;arr[1]=b;arr[2]=c;arr[3]=d;arr[4]=e;
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
	
	public double probFOAK(int a,int b,int c,int d,int e, int userSimChoose)
	{
		double prob=0; double numberofReroll=0.0; int sum=0; int sumofsquare=0; int diffpair=0; int []arr= new int[5];
		arr[0]=a;arr[1]=b;arr[2]=c;arr[3]=d;arr[4]=e;
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
	
	public double probYaht(int a,int b,int c,int d,int e, int userSimChoose)
	{
		double prob=0; double numberofReroll=0.0; int sum=0; int sumofsquare=0; int diffpair=0; int []arr= new int[5];
		arr[0]=a;arr[1]=b;arr[2]=c;arr[3]=d;arr[4]=e;
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
		
		if(numberofReroll==4||numberofReroll==5)
		{
			prob=1.0/1296.0;
		}
		else
		{
			if(diffpair==0)
			{
				prob=Math.pow(1.0/6.0, numberofReroll);
			}
			else
			{
				prob=0;
			}
		}
		return prob;
	}
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
	public double probTOAK(int a,int b,int c,int d,int e, int userSimChoose)
	{
		double prob=0; double numberofReroll=0.0; int sum=0; int sumofsquare=0; int diffpair=0; int []arr= new int[5];
		arr[0]=a;arr[1]=b;arr[2]=c;arr[3]=d;arr[4]=e;
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
			prob=46.0/216.0;
		}
		if(numberofReroll==3)
		{
			if(diffpair==0)
			{
				prob=96.0/216.0;
			}
			else if(diffpair==2)
			{
				prob=36.0/216.0;
			}
		}
		if(numberofReroll==2)
		{
			if(diffpair==0)
			{
				prob=1.0;
			}
			else if(diffpair==4)
			{
				prob=12.0/36.0;
			}
			else if(diffpair==6)
			{
				prob=3.0/36.0;
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
				prob=1.0;
			}
			else if(diffpair==8)
			{
				prob=2.0/6.0;
			}
			else if(diffpair==10)
			{
				prob=1.0/6.0;
			}
			else if(diffpair==12)
			{
				prob=0.0;
			}
		}
		
		return prob;
	}
	
	public double probFH(int a,int b,int c,int d,int e, int userSimChoose)
	{
		double prob=0; double numberofReroll=0.0; int sum=0; int sumofsquare=0; int diffpair=0; int []arr= new int[5];
		arr[0]=a;arr[1]=b;arr[2]=c;arr[3]=d;arr[4]=e;
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
			prob=50.0/1296.0;
		}
		if(numberofReroll==3)
		{
			if(diffpair==0)
			{
				prob=20.0/216.0;
			}
			else if(diffpair==2)
			{
				prob=6.0/216.0;
			}
		}
		if(numberofReroll==2)
		{
			if(diffpair==0)
			{
				prob=5.0/36.0;
			}
			else if(diffpair==4)
			{
				prob=3.0/36.0;
			}
			else if(diffpair==6)
			{
				prob=0.0;
			}
		}
		if(numberofReroll==1)
		{
			if(diffpair==0||diffpair==10|diffpair==12)
			{
				prob=0.0;
			}
			else if(diffpair==6)
			{
				prob=1.0/6.0;
			}
			else if(diffpair==8)
			{
				prob=2.0/6.0;
			}
		}
		return prob;
	}
	
	public double probSmStr(int a,int b,int c,int d,int e,int userSimChoose)
	{
		double prob=0; double numberofReroll=0.0; int sum=0; int sumofsquare=0; int diffpair=0; int []arr= new int[5]; int []count= new int[6];
		int sumofsquareofCount=0; double forbash=0;
		arr[0]=a;arr[1]=b;arr[2]=c;arr[3]=d;arr[4]=e;
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
		}
		else if(numberofReroll==1)
		{
			for(int i=1;i<=6;i++)
			{
				for (int j=0;j<=4;j++)
				{
					if(arr[j]==0)
					{
						arr[j]=i;
						count[i-1]++;
						if(count[0]*count[1]*count[2]*count[3]+count[1]*count[2]*count[3]*count[4]+count[2]*count[3]*count[4]*count[5]>0)
						{
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
	
	public void checkScoreCard()

	{
		if(pnts1+pnts2+pnts3+pnts4+pnts5+pnts6>=63&&gotbonus==0)
		{
			bonus=35;
			totalscore+=35;
			gotbonus++;
		}
		System.out.println("");System.out.println("______________");
		System.out.print("|  1s  |  ");System.out.print(pnts1);System.out.println("  |");
		System.out.println("______________");
		System.out.print("|  2s  |  ");System.out.print(pnts2);System.out.println("  |");
		System.out.println("______________");
		System.out.print("|  3s  |  ");System.out.print(pnts3);System.out.println("  |");
		System.out.println("______________");
		System.out.print("|  4s  |  ");System.out.print(pnts4);System.out.println("  |");
		System.out.println("______________");
		System.out.print("|  5s  |  ");System.out.print(pnts5);System.out.println("  |");
		System.out.println("______________");
		System.out.print("|  6s  |  ");System.out.print(pnts6);System.out.println("  |");
		System.out.println("______________");
		System.out.print("|Bonus |  ");System.out.print(bonus);System.out.println("  |");
		System.out.println("______________");
		System.out.println("______________");
		System.out.print("|3ofAK |  ");System.out.print(pntsToak);System.out.println("  |");
		System.out.println("______________");
		System.out.print("|4ofAK |  ");System.out.print(pntsFoak);System.out.println("  |");
		System.out.println("______________");
		System.out.print("|FullHS|  ");System.out.print(25*(1-fh));System.out.println("  |");
		System.out.println("______________");
		System.out.print("|SmStr |  ");System.out.print(30*(1-smstr));System.out.println("  |");
		System.out.println("______________");
		System.out.print("|LgStr |  ");System.out.print(40*(1-lgstr));System.out.println("  |");
		System.out.println("______________");
		System.out.print("|YahtZ |  ");
		if(yaht<=0)
		{
			System.out.print(50);
		}
		else
		{
			System.out.print(0);
		}
		System.out.println("  |");
		System.out.println("______________");
		System.out.print("|Chance|  ");System.out.print(pntsChan);System.out.println("  |");
		System.out.println("______________");
		System.out.print("|YahtBo|  ");System.out.print(yahtBo);System.out.println("  |");
		System.out.println("______________");


		if(toak+foak+fh+lgstr+smstr+chan+ace+two+three+four+five+six==0&&yaht<=0)
		{
			System.out.println("SCORE CARD COMPLETED");
			System.out.print("Total score = "); System.out.println(totalscore); System.out.println("");
			System.out.println("");
			life=2;
		}
		else
		{
			System.out.print("total score = "); System.out.println(totalscore); System.out.println("");
		}
		
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////
	public void resetfornextround()
	{
		if(life==1)
		{
			totalscore+=score;
			System.out.println("");
			score=0;
			haveChoice=0;
			roll_left=3;
			valA = 0; valB = 0; valC = 0;  valD = 0; valE = 0;
			count1=0; count2=0; count3=0; count4=0; count5=0; count6=0;
		}
		else if (life==0)
		{
			System.out.print("(gone) --> ");
		}
	}
	
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
	
	public void rolldice()
	{
		if(life==1)
		{
			if(roll_left==-1)
			{
				roll_left=-2;
			}
			else if(roll_left<3&&roll_left>=0)
			{
				count1=0; count2=0; count3=0; count4=0; count5=0; count6=0;
				
				if(Math.floorDiv(rollDecision,10)%2==1)
				{
					valA=a.roll();
					arrVal[0]=valA;
				}
				if(Math.floorDiv(rollDecision,20)%2==1)
				{
					valB=b.roll();
					arrVal[1]=valB;
				}
				if(Math.floorDiv(rollDecision,40)%2==1)
				{
					valC=c.roll();
					arrVal[2]=valC;
				}
				if(Math.floorDiv(rollDecision,80)%2==1)
				{
					valD=d.roll();
					arrVal[3]=valD;
				}
				if(Math.floorDiv(rollDecision,160)%2==1)
				{
					valE=e.roll();
					arrVal[4]=valE;
				}
				
				if(valA==1){count1++;}if(valA==2){count2++;}if(valA==3){count3++;}if(valA==4){count4++;}if(valA==5){count5++;}if(valA==6){count6++;}
				if(valB==1){count1++;}if(valB==2){count2++;}if(valB==3){count3++;}if(valB==4){count4++;}if(valB==5){count5++;}if(valB==6){count6++;}
				if(valC==1){count1++;}if(valC==2){count2++;}if(valC==3){count3++;}if(valC==4){count4++;}if(valC==5){count5++;}if(valC==6){count6++;}
				if(valD==1){count1++;}if(valD==2){count2++;}if(valD==3){count3++;}if(valD==4){count4++;}if(valD==5){count5++;}if(valD==6){count6++;}
				if(valE==1){count1++;}if(valE==2){count2++;}if(valE==3){count3++;}if(valE==4){count4++;}if(valE==5){count5++;}if(valE==6){count6++;}
			}
			else if(roll_left==3)
			{
				valA=a.roll(); valB=b.roll(); valC=c.roll(); valD=d.roll(); valE=e.roll(); roll_left--;
				arrVal[0]=valA; arrVal[1]=valB; arrVal[2]=valC; arrVal[3]=valD; arrVal[4]=valE;
				if(valA==1){count1++;}if(valA==2){count2++;}if(valA==3){count3++;}if(valA==4){count4++;}if(valA==5){count5++;}if(valA==6){count6++;}
				if(valB==1){count1++;}if(valB==2){count2++;}if(valB==3){count3++;}if(valB==4){count4++;}if(valB==5){count5++;}if(valB==6){count6++;}
				if(valC==1){count1++;}if(valC==2){count2++;}if(valC==3){count3++;}if(valC==4){count4++;}if(valC==5){count5++;}if(valC==6){count6++;}
				if(valD==1){count1++;}if(valD==2){count2++;}if(valD==3){count3++;}if(valD==4){count4++;}if(valD==5){count5++;}if(valD==6){count6++;}
				if(valE==1){count1++;}if(valE==2){count2++;}if(valE==3){count3++;}if(valE==4){count4++;}if(valE==5){count5++;}if(valE==6){count6++;}
			}
		}
	}
	public void chooseScore()
	{
		if(roll_left>=0&&life==1)
		{
			System.out.println("-------------------------------");
			System.out.print(" DiceA:"); System.out.print(valA);
			System.out.print(" DiceB:"); System.out.print(valB);
			System.out.print(" DiceC:"); System.out.print(valC);
			System.out.print(" DiceD:"); System.out.print(valD);
			System.out.print(" DiceE:"); System.out.print(valE);
			if(roll_left==0)
			{
				System.out.print("  No More Re-Roll Left!!!");
			}
			System.out.print("\nScoring(s) available this game: ");
			
			if(toak==1) {System.out.print("3ofAK   ");}
			if(foak==1) {System.out.print("4ofAK   ");}
			if(fh==1) {System.out.print("FullHS   ");}
			if(smstr==1) {System.out.print("SmStrg   ");}
			if(lgstr==1) {System.out.print("LgStrg   ");}
			if(yaht==1) {System.out.print("Yahtz   ");}
			if(ace==1) {System.out.print("1s   ");}
			if(two==1) {System.out.print("2s   ");}
			if(three==1) {System.out.print("3s   ");}
			if(four==1) {System.out.print("4s   ");}
			if(five==1) {System.out.print("5s   ");}
			if(six==1) {System.out.print("6s   ");}
			if(chan==1) {System.out.print("Chance   ");}
			
			System.out.println("");
			System.out.println("Scoring(s) available this move");
			
			//code for each type of scoring
			int maxPoint=0;botDeci=15;
			
			
			if(count1>0&&ace>0)
			{
				System.out.print("Aces  : "); System.out.print(1*count1); System.out.print(" points ");
				System.out.println(": type 1 to select"); haveChoice=1;
				if(1*count1>maxPoint)
				{
					maxPoint=1*count1;botDeci=1;
				}	
			}
			
			if(count2>0&&two>0)
			{
				System.out.print("Twos  : "); System.out.print(2*count2); System.out.print(" points ");
				System.out.println(": type 2 to select"); haveChoice=1;
				if(2*count2>maxPoint)
				{
					maxPoint=2*count2;botDeci=2;
				}
			}
		
			if(count3>0&&three>0)
			{
				System.out.print("Threes: "); System.out.print(3*count3); System.out.print(" points ");
				System.out.println(": type 3 to select"); haveChoice=1;
				if(3*count3>maxPoint)
				{
					maxPoint=3*count3;botDeci=3;
				}
			}
			
			if(count4>0&&four>0)
			{
				System.out.print("Fours : "); System.out.print(4*count4); System.out.print(" points ");
				System.out.println(": type 4 to select"); haveChoice=1;
				if(4*count4>maxPoint)
				{
					maxPoint=4*count4;botDeci=4;
				}
			}
			
			if(count5>0&&five>0)
			{
				System.out.print("Fives : "); System.out.print(5*count5); System.out.print(" points ");
				System.out.println(": type 5 to select"); haveChoice=1;
				if(5*count5>maxPoint)
				{
					maxPoint=5*count5;botDeci=5;
				}
			}
			
			
			if(count6>0&&six>0)
			{
				System.out.print("Sixes : "); System.out.print(6*count6); System.out.print(" points ");
				System.out.println(": type 6 to select"); haveChoice=1;
				if(6*count6>maxPoint)
				{
					maxPoint=6*count6;botDeci=6;
				}
			}
			
			if(maxofSix(count1,count2,count3,count4,count5,count6)==5)
			{
				if(yaht>0)
				{
					System.out.print("Yahtz : 50 points ");System.out.println(": type 7 to select"); haveChoice=1;
					if(50>maxPoint)
					{
						maxPoint=50;botDeci=7;
					}
				}
				else
				{
					System.out.print("Yahtz : 100 points ");System.out.println(": type 7 to select"); haveChoice=1;
					if(100>maxPoint)
					{
						maxPoint=100;botDeci=7;
					}
				}
			}
			
			if(maxofSix(count1,count2,count3,count4,count5,count6)>=4&&foak>0)
			{
				System.out.print("4ofAK : "); System.out.print(valA+valB+valC+valD+valE); System.out.print(" points ");
				System.out.println(": type 8 to select"); haveChoice=1;
				if(valA+valB+valC+valD+valE>maxPoint)
				{
					maxPoint=valA+valB+valC+valD+valE;botDeci=8;
				}
				
			}
			
			if(maxofSix(count1,count2,count3,count4,count5,count6)>=3&&toak>0)
			{
				System.out.print("3ofAK : "); System.out.print(valA+valB+valC+valD+valE); System.out.print(" points ");
				System.out.println(": type 9 to select"); haveChoice=1;
				if(valA+valB+valC+valD+valE>maxPoint)
				{
					maxPoint=valA+valB+valC+valD+valE;
					botDeci=9;
				}
				
			}
			
			
			if(chan>0)
			{
				System.out.print("Chance: "); System.out.print(valA+valB+valC+valD+valE); System.out.print(" points ");
				System.out.println(": type 11 to select"); haveChoice=1;
				if(valA+valB+valC+valD+valE>maxPoint)
				{
					maxPoint=valA+valB+valC+valD+valE;
					botDeci=11;
				}
			}
			
			if(count1*count2*count3*count4+count2*count3*count4*count5+count3*count4*count5*count6>0&&smstr>0)
			{
				System.out.print("SmStrg: 30 points ");System.out.println(": type 12 to select"); haveChoice=1;
				if(30>maxPoint)
				{
					maxPoint=30;
					botDeci=12;
				}
			}
			
			if(1==count2&&count2==count3&&count3==count4&&count4==count5&&lgstr>0)
			{
				System.out.print("LgStrg: 40 points ");System.out.println(": type 13 to select"); haveChoice=1;
				if(40>maxPoint)
				{
					maxPoint=40;
					botDeci=13;
				}
			}
			
			if(count1*count1+count2*count2+count3*count3+count4*count4+count5*count5+count6*count6==13&&fh>0)
			{
				System.out.print("FullHS: 25 points ");System.out.println(": type 14 to select"); haveChoice=1;
				if(25>maxPoint)
				{
					maxPoint=25;
					botDeci=14;
				}
			}
			
			if((botDeci==15||botDeci==6||botDeci==5||botDeci==4||botDeci==3||botDeci==2||botDeci==1)&&roll_left>=1)
			{
				if(six>0)
				{
					botDeci=0;
					for(int i=0;i<=4;i++)
					{
						if(arrVal[i]!=6)
						botDeci+=10*Math.pow(2, i);
					}
				}
				else if(five>0)
				{
					botDeci=0;
					for(int i=0;i<=4;i++)
					{
						if(arrVal[i]!=5)
						botDeci+=10*Math.pow(2, i);
					}
				}
				else if(four>0)
				{
					botDeci=0;
					for(int i=0;i<=4;i++)
					{
						if(arrVal[i]!=4)
						botDeci+=10*Math.pow(2, i);
					}
				}
				else if(three>0)
				{
					botDeci=0;
					for(int i=0;i<=4;i++)
					{
						if(arrVal[i]!=3)
						botDeci+=10*Math.pow(2, i);
					}
				}
				else if(two>0)
				{
					botDeci=0;
					for(int i=0;i<=4;i++)
					{
						if(arrVal[i]!=2)
						botDeci+=10*Math.pow(2, i);
					}
				}
				else if (ace>0)
				{
					botDeci=0;
					for(int i=0;i<=4;i++)
					{
						if(arrVal[i]!=6)
						botDeci+=10*Math.pow(2, i);
					}
				}
			}
			
			if(haveChoice!=1&&roll_left==0)
			{
				System.out.print("Skip! : 0 point ");System.out.println(": type 15 to select");
			}
			
			System.out.println("type 99 to delete this player");
			
			if(roll_left>0)
			{
				System.out.println("");
				System.out.println("FOR REROLL");
				System.out.println("THIS IS DICE VALUE");
				System.out.println("A=10_B=20_C=40_D=80_E=160");
				System.out.println("Insert the SUM OF DICE VALUE you want TO REROLL");
				
				///start of cheat code
				double maxProbAll=0;
				if(cheat==1)
				{
					System.out.println("SUGGESTION: ");
					
					///max chance 3ofAK
					if(toak>0)
					{
						System.out.print("max chance 3ofAK ---> "); maxProb=0;
						for(int i=1;i<=31;i++)
						{simulProb[i-1]=probTOAK(valA,valB,valC,valD,valE,(10*i)); maxProb = maxofArray(simulProb);}
						if(maxProb>0)
						{
							for(int i=1;i<=31;i++)
						    {
								if(simulProb[i-1]==maxProb)
								{
									System.out.print(10*i);System.out.print(" ");
									if(maxProb>=maxProbAll&&maxPoint<24)
									{
										botDeci=10*i;
										maxProbAll=maxProb;
									}
								}
						    }
							System.out.println("");
							System.out.print("probability = ");System.out.println(maxProb);
						}
						else
						{System.out.println("impossible");}
					}
					
					///max chance 4ofAK
					if(foak>0)
					{
						System.out.print("max chance 4ofAK ---> "); maxProb=0;
						for(int i=1;i<=31;i++)
						{simulProb[i-1]=probFOAK(valA,valB,valC,valD,valE,(10*i)); maxProb = maxofArray(simulProb);}
						if(maxProb>0)
						{
							for(int i=1;i<=31;i++)
						    {
								if(simulProb[i-1]==maxProb)
								{
									System.out.print(10*i);System.out.print(" ");
									if(maxProb>=maxProbAll&&maxPoint<24)
									{
										botDeci=10*i;
										maxProbAll=maxProb;
									}
								}
						    }
							System.out.println("");
							System.out.print("probability = ");System.out.println(maxProb);
						}
						else
						{System.out.println("impossible");}
					}
					
					///max chance FullHS
					if(fh>0)
					{
						System.out.print("max chance FullHS ---> "); maxProb=0;
						for(int i=1;i<=31;i++)
						{simulProb[i-1]=probFH(valA,valB,valC,valD,valE,(10*i)); maxProb = maxofArray(simulProb);}
						if(maxProb>0)
						{
							for(int i=1;i<=31;i++)
							{
								if(simulProb[i-1]==maxProb)
								{
									System.out.print(10*i);System.out.print(" ");
									if(maxProb>=maxProbAll&&maxPoint<24)
									{
										botDeci=10*i;
										maxProbAll=maxProb;
									}
								}
							}
							System.out.println("");
							System.out.print("probability = ");System.out.println(maxProb);
						}
						else
						{System.out.println("impossible");}
					}
					
					//max chance small straight
					if(smstr>0)
					{
						System.out.print("max chance SmStr ---> "); maxProb=0;
						for(int i=1;i<=31;i++)
						{simulProb[i-1]=probSmStr(valA,valB,valC,valD,valE,(10*i)); maxProb = maxofArray(simulProb);}
						if(maxProb>0)
						{
							for(int i=1;i<=31;i++)
							{
								if(simulProb[i-1]==maxProb)
								{
									System.out.print(10*i);System.out.print(" ");
									if(maxProb>=maxProbAll&&maxPoint<24)
									{
										botDeci=10*i;
										maxProbAll=maxProb;
									}
								}
							}
							System.out.println("");
							System.out.print("probability = ");System.out.println(maxProb);
						}
						else
						{System.out.println("impossible");}
					}
					//
					
					///max chance large straight
					if(lgstr>0)
					{
						System.out.print("max chance LgStr ---> "); maxProb=0;
						for(int i=1;i<=31;i++)
						{simulProb[i-1]=probLgStr(valA,valB,valC,valD,valE,(10*i)); maxProb = maxofArray(simulProb);}
						if(maxProb>0)
						{
							for(int i=1;i<=31;i++)
							{
								if(simulProb[i-1]==maxProb)
								{
									System.out.print(10*i);System.out.print(" ");
									if(maxProb>=maxProbAll&&maxPoint<24)
									{
										botDeci=10*i;
										maxProbAll=maxProb;
									}
								}
							}
							System.out.println("");
							System.out.print("probability = ");System.out.println(maxProb);
						}
						else
						{System.out.println("impossible");}
					}
					
					
					///max chance Yahtzee
					if(yaht>0)
					{
						System.out.print("max chance Yahtzee ---> "); maxProb=0;
						for(int i=1;i<=31;i++)
						{simulProb[i-1]=probYaht(valA,valB,valC,valD,valE,(10*i)); maxProb = maxofArray(simulProb);}
						if(maxProb>0)
						{
							for(int i=1;i<=31;i++)
						    {
								if(simulProb[i-1]==maxProb)
								{
									System.out.print(10*i);System.out.print(" ");
									if(maxProb>=maxProbAll&&maxPoint<24)
									{
										botDeci=10*i;
										maxProbAll=maxProb;
									}
								}
						    }
							System.out.println("");
							System.out.print("probability = ");System.out.println(maxProb);	
						}
						else
						{System.out.println("impossible");}
					}
					else
					{
						System.out.print("max chance Yahtzee ---> "); maxProb=0;
						for(int i=1;i<=31;i++)
						{simulProb[i-1]=probYaht(valA,valB,valC,valD,valE,(10*i)); maxProb = maxofArray(simulProb);}
						if(maxProb>0)
						{
							for(int i=1;i<=31;i++)
						    {
								if(simulProb[i-1]==maxProb)
								{
									System.out.print(10*i);System.out.print(" ");
									if(maxProb>=maxProbAll&&maxProb>=1.0/36.0&&maxPoint<24)
									{
										botDeci=10*i;
										maxProbAll=maxProb;
									}
								}
						    }
							System.out.println("");
							System.out.print("probability = ");System.out.println(maxProb);	
						}
						else
						{System.out.println("impossible");}
					}
						
				}
			
				
				
				///end of cheat code
			}
			Scanner kboard = new Scanner(System.in);
			for(int i=0;i<100;i++)
			{
				int userSim;
				if(bot!=1)
				{
					userSim = kboard.nextInt();
				}
				else
				{
					userSim=botDeci;
				}
				
				if (userSim%10==0)
				{
					int userDecideReroll=1;
					if(cheat==1)
					{
						{System.out.print("Yahtzee prob with this re-roll= ");System.out.println(probYaht(valA,valB,valC,valD,valE,userSim));}
						if(foak>0){System.out.print("4ofAK prob with this re-roll= ");System.out.println(probFOAK(valA,valB,valC,valD,valE,userSim));}
						if(toak>0){System.out.print("3ofAK prob with this re-roll= ");System.out.println(probTOAK(valA,valB,valC,valD,valE,userSim));}
						if(fh>0){System.out.print("FullHS prob with this re-roll= ");System.out.println(probFH(valA,valB,valC,valD,valE,userSim));}
						if(lgstr>0){System.out.print("LgStr prob with this re-roll= ");System.out.println(probLgStr(valA,valB,valC,valD,valE,userSim));}
						if(lgstr>0){System.out.print("LgStr prob with this re-roll= ");System.out.println(probLgStr(valA,valB,valC,valD,valE,userSim));}
						
						System.out.print("type 1 to confirm decision----");System.out.println("type other number to change decision");
						Scanner rerollDecision = new Scanner(System.in);
						if(bot!=1)
						{
							userDecideReroll = rerollDecision.nextInt();
						}
						else
						{
							userDecideReroll=1;
						}
					}
					
					if(userDecideReroll==1)
					{
						userChoose=userSim;
						i=100;
					}
					else
					{
						System.out.println("choose the re-roll again");
						userChoose=userSim;
					}
				}
				else
				{
					i=100;
					userChoose=userSim;
				}
			}
			
			
			if(roll_left>0&&userChoose%10==0&&userChoose>0)
			{
				
				rollDecision=userChoose;
				roll_left--;
			}
			else
			{
				if(count1*count2*count3*count4+count2*count3*count4*count5+count3*count4*count5*count6>0&&smstr>0&&userChoose==12)
				{
					score+=30;
					smstr--;
					System.out.print("Score: "); System.out.println(score);
					roll_left=-1;
				}
				
				if(valA==valB&&valB==valC&&valC==valD&&valD==valE&&yaht>0&&userChoose==7)
				{
					if(yaht>0)
					{
						score+=50;
						yaht--;
						System.out.print("Score: "); System.out.println(score);
						roll_left=-1;
					}
					else
					{
						score+=100;
						yahtBo+=100;
						yaht--;
						System.out.print("Score: "); System.out.println(score);
						roll_left=-1;
					}
				}
				
				if(Math.max(count6,Math.max(count5,Math.max(count4,Math.max(count3,Math.max(count2,count1)))))>=4&&foak>0&&userChoose==8)
				{
					pntsFoak=valA+valB+valC+valD+valE;
					score+=valA+valB+valC+valD+valE;
					foak--;
					System.out.print("Score: "); System.out.println(score);
					roll_left=-1;
				}
				
				if(Math.max(count6,Math.max(count5,Math.max(count4,Math.max(count3,Math.max(count2,count1)))))>=3&&toak>0&&userChoose==9)
				{
					pntsToak=valA+valB+valC+valD+valE;
					score+=valA+valB+valC+valD+valE;
					toak--;
					System.out.print("Score: "); System.out.println(score);
					roll_left=-1;
				}
				
				if(count2==count3&&count3==count4&&count4==count5&&lgstr>0&&userChoose==13)
				{
					score+=40;
					lgstr--;
					System.out.print("Score: "); System.out.println(score);
					roll_left=-1;
				}
				
				if(count1>0&&ace>0&&userChoose==1)
				{
					pnts1=1*count1;
					score+=1*count1;
					ace--;
					System.out.print("Score: "); System.out.println(score);
					roll_left=-1;
				}
				
				if(count2>0&&two>0&&userChoose==2)
				{
					pnts2=2*count2;
					score+=2*count2;
					two--;
					System.out.print("Score: "); System.out.println(score);
					roll_left=-1;
				}
			
				if(count3>0&&three>0&&userChoose==3)
				{
					pnts3=3*count3;
					score+=3*count3;
					three--;
					System.out.print("Score: "); System.out.println(score);
					roll_left=-1;
				}
				
				if(count4>0&&four>0&&userChoose==4)
				{
					pnts4=4*count4;
					score+=4*count4;
					four--;
					System.out.print("Score: "); System.out.println(score);
					roll_left=-1;
				}
				
				if(count5>0&&five>0&&userChoose==5)
				{
					pnts5=5*count5;
					score+=5*count5;
					five--;
					System.out.print("Score: "); System.out.println(score);
					roll_left=-1;
				}
				
				if(count6>0&&six>0&&userChoose==6)
				{
					pnts6=6*count6;
					score+=6*count6;
					six--;
					System.out.print("Score: "); System.out.println(score);
					roll_left=-1;
				}
				
				if(count1*count2*count3*count4+count2*count3*count4*count5+count3*count4*count5*count6>0&&smstr>0&&userChoose==12)
				{
					score+=30;
					smstr--;
					System.out.print("Score: "); System.out.println(score);
					roll_left=-1;
				}
				
				if(chan>0&&userChoose==11)
				{
					pntsChan=valA+valB+valC+valD+valE;
					score+=valA+valB+valC+valD+valE;
					chan--;
					System.out.print("Score: "); System.out.println(score);
					roll_left=-1;
				}
				
				if(count1*count1+count2*count2+count3*count3+count4*count4+count5*count5+count6*count6==13&&fh>0&&userChoose==14)
				{
					score+=25;
					fh--;
					System.out.print("Score: "); System.out.println(score);
					roll_left=-1;
				}
				
				if(haveChoice==0&&roll_left==0&&userChoose==15)
				{
					System.out.print("Score: "); System.out.println(score);
					roll_left=-1;
				}
				
				if(userChoose==99)
				{
					life--;
				}
			}
		}
	}
}