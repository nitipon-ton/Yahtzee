import java.util.Scanner;
public class DiceMain
{
	
	public static void main(String[] args) 
	{
		
			double roundBot=0;
			Player ton = new Player();
			Player player1 = new Player();
			Player player2 = new Player();
			Player player3 = new Player();
			Player player4 = new Player();
			
			Scanner askCheat = new Scanner(System.in);
			System.out.println("type 1 to open cheat mode, other number for normal mode");
			ton.cheat = askCheat.nextInt();
			player1.cheat = ton.cheat;
			player2.cheat = ton.cheat;
			player3.cheat = ton.cheat;
			player4.cheat = ton.cheat;
			
			Scanner askNum = new Scanner(System.in);
			Scanner askName = new Scanner(System.in);
			
			String tonUserName;
			System.out.println("type 1 to let bot play character #1");
			int bot0play = askNum.nextInt();
			if(bot0play==1)
			{
				tonUserName="bot0";
				ton.bot=1;
				ton.cheat=1;
			}
			else
			{
				System.out.println("enter player name:");
				tonUserName = askName.nextLine();
			}
			
			String p1UserName;
			System.out.println("type 1 to let bot play character #2");
			int bot1play = askNum.nextInt();
			if(bot1play==1)
			{
				p1UserName="bot1";
				player1.bot=1;
				player1.cheat=1;
			}
			else
			{
				System.out.println("enter player name:");
				p1UserName = askName.nextLine();
			}
			
			String p2UserName;
			System.out.println("type 1 to let bot play character #3");
			int bot2play = askNum.nextInt();
			if(bot2play==1)
			{
				p2UserName="bot2";
				player2.bot=1;
				player2.cheat=1;
			}
			else
			{
				System.out.println("enter player name:");
				p2UserName = askName.nextLine();
			}
			
			String p3UserName;
			System.out.println("type 1 to let bot play character #4");
			int bot3play = askNum.nextInt();
			if(bot3play==1)
			{
				p3UserName="bot3";
				player3.bot=1;
				player3.cheat=1;
			}
			else
			{
				System.out.println("enter player name:");
				p3UserName = askName.nextLine();
			}

			String p4UserName;
			System.out.println("type 1 to let bot play character #5");
			int bot4play = askNum.nextInt();
			if(bot4play==1)
			{
				p4UserName="bot4";
				player4.bot=1;
				player4.cheat=1;
			}
			else
			{
				System.out.println("enter player name:");
				p4UserName = askName.nextLine();
			}
			
			int j=1;
			while(j<=13)
			{
					System.out.print(tonUserName);System.out.print("'s turn ");
					for (int i=0;i<3;i++)
					{
						ton.rolldice();
						ton.chooseScore();
					}
					ton.resetfornextround();

					System.out.print(p1UserName);System.out.print("'s turn ");
					for (int i=0;i<3;i++)
					{
						player1.rolldice();
						player1.chooseScore();
					}
					player1.resetfornextround();
				
					System.out.print(p2UserName);System.out.print("'s turn ");
					for (int i=0;i<3;i++)
					{
						player2.rolldice();
						player2.chooseScore();
					}
					player2.resetfornextround();
				
					System.out.print(p3UserName);System.out.print("'s turn ");
					for (int i=0;i<3;i++)
					{
						player3.rolldice();
						player3.chooseScore();
					}
					player3.resetfornextround();
				
					System.out.print(p4UserName);System.out.print("'s turn ");
					for (int i=0;i<3;i++)
					{
						player4.rolldice();
						player4.chooseScore();
					}
					player4.resetfornextround();
				
				System.out.println("------------------------------------------");
				System.out.print("END OF ROUND (");System.out.print(j);System.out.println(")");
				System.out.println("-----------------------");System.out.print(tonUserName);System.out.print("'s score ");    ton.checkScoreCard();        if(ton.life==2){j=9999;}
				System.out.println("-----------------------");System.out.print(p1UserName); System.out.print("'s score ");    player1.checkScoreCard();    if(player1.life==2){j=9999;} 
				System.out.println("-----------------------");System.out.print(p2UserName); System.out.print("'s score ");    player2.checkScoreCard();    if(player2.life==2){j=9999;}
				System.out.println("-----------------------");System.out.print(p3UserName); System.out.print("'s score ");    player3.checkScoreCard();    if(player3.life==2){j=9999;}
				System.out.println("-----------------------");System.out.print(p4UserName); System.out.print("'s score ");    player4.checkScoreCard();    if(player4.life==2){j=9999;}
				int tonRank=1; int p1Rank=1; int p2Rank=1; int p3Rank=1; int p4Rank=1;
				if(ton.totalscore>ton.totalscore) {tonRank++;}
				if(player1.totalscore>ton.totalscore) {tonRank++;}
				if(player2.totalscore>ton.totalscore) {tonRank++;}
				if(player3.totalscore>ton.totalscore) {tonRank++;}
				if(player4.totalscore>ton.totalscore) {tonRank++;}
				
				if(ton.totalscore>player1.totalscore) {p1Rank++;}
				if(player1.totalscore>player1.totalscore) {p1Rank++;}
				if(player2.totalscore>player1.totalscore) {p1Rank++;}
				if(player3.totalscore>player1.totalscore) {p1Rank++;}
				if(player4.totalscore>player1.totalscore) {p1Rank++;}
				
				if(ton.totalscore>player2.totalscore) {p2Rank++;}
				if(player1.totalscore>player2.totalscore) {p2Rank++;}
				if(player2.totalscore>player2.totalscore) {p2Rank++;}
				if(player3.totalscore>player2.totalscore) {p2Rank++;}
				if(player4.totalscore>player2.totalscore) {p2Rank++;}
				
				if(ton.totalscore>player3.totalscore) {p3Rank++;}
				if(player1.totalscore>player3.totalscore) {p3Rank++;}
				if(player2.totalscore>player3.totalscore) {p3Rank++;}
				if(player3.totalscore>player3.totalscore) {p3Rank++;}
				if(player4.totalscore>player3.totalscore) {p3Rank++;}
				
				if(ton.totalscore>player4.totalscore) {p4Rank++;}
				if(player1.totalscore>player4.totalscore) {p4Rank++;}
				if(player2.totalscore>player4.totalscore) {p4Rank++;}
				if(player3.totalscore>player4.totalscore) {p4Rank++;}
				if(player4.totalscore>player4.totalscore) {p4Rank++;}
				
				System.out.print(tonUserName);System.out.print("'s RANK: ");System.out.println(tonRank);
				System.out.print(p1UserName);System.out.print("'s RANK: ");System.out.println(p1Rank);
				System.out.print(p2UserName);System.out.print("'s RANK: ");System.out.println(p2Rank);
				System.out.print(p3UserName);System.out.print("'s RANK: ");System.out.println(p3Rank);
				System.out.print(p4UserName);System.out.print("'s RANK: ");System.out.println(p4Rank);
				//only for experiment - test the bot's ability
				j++;
				
			}
			
			System.out.println("GOOD GAME, WELL PLAYED!");
	}
}
