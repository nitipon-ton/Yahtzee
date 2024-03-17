import java.util.Scanner;
import java.lang.Exception;
public class DiceMain
{
	public static void main(String[] args) 
	{
		Scanner ask = new Scanner(System.in);
		System.out.println("Type the number of players");
		int numPlayer = ask.nextInt();
		while (numPlayer <= 0) {
			numPlayer = ask.nextInt();
		}
		Player[] players = new Player[numPlayer];
		int[] ranks = new int[numPlayer];
		String[] usernames = new String[numPlayer];
		boolean[] isBot = new boolean[numPlayer];
		int botCount = 0;
		for (int i = 0; i < numPlayer; i++) {
			players[i] = new Player();
			System.out.println("type 1 to let bot play character #" + (i + 1));
			isBot[i] = ask.nextInt() == 1;
			ask.nextLine(); // Consume the leftover newline character
			if (isBot[i]) {
				usernames[i] = "bot " + ++botCount;
				players[i].bot = true;
				players[i].cheat = true;
			} else {
				System.out.println("enter player name:");
				usernames[i] = ask.nextLine(); // Now waits for actual input
			}
		}
		for (int a = 1; a <= 13; a++) {
			for (int i = 0; i < numPlayer; i++) {
				System.out.print(usernames[i] + "'s turn ");
				for (int j = 0; j < 3; j++)
				{
					players[i].rolldice();
					players[i].chooseScore();
				}
				players[i].resetfornextround();
			}
			System.out.println("------------------------------------------");
			System.out.println("END OF ROUND (" + a + ")");
			for (int i = 0; i < numPlayer; i++) {
				System.out.print("-----------------------\n" + usernames[i] + "'s score ");
				players[i].checkScoreCard();
				if (players[i].life == 2) {
					a = 99;
				}
			}
            java.util.Arrays.fill(ranks, 1);
			for (int i = 0; i < numPlayer; i++) {
				for (int j = 0; j < numPlayer; j++) {
					if (players[j].totalscore > players[i].totalscore) {
						ranks[i]++;
					}
				}
			}
			for (int rank = 1; rank <= numPlayer; rank++) {
				for (int i = 0; i < numPlayer; i++) {
					if (ranks[i] == rank) {
						System.out.println("RANK " + rank + ": " + usernames[i] + ": SCORE = " + players[i].totalscore);
					}
				}
			}
		}
		int[] scoreInRange = new int[30];
		//scoreInRange[0] = No. of player scoring 0-19
		for (int rank = 1; rank <= numPlayer; rank++) {
			for (int i = 0; i < numPlayer; i++) {
				if (ranks[i] == rank) {
					scoreInRange[players[i].totalscore / 20]++;
				}
			}
		}
		String fig = "(I)";
		int figSize = 1;
		if (numPlayer > 300) {
			fig = "(X)";
			figSize = 10;
		}
		if (numPlayer > 3000) {
			fig = "(C)";
			figSize = 100;
		}
		if (numPlayer > 30000) {
			fig = "(M)";
			figSize = 1000;
		}
		for (int i = 4; i <= 16; i++) {
			System.out.print("score" + 20 * i + "-" + (20 * i + 19) + "  ");
			for (int j = 0; j < scoreInRange[i] / figSize; j++) {
				System.out.print(fig);
			}
			System.out.print("\n");
		}
		System.out.println("GOOD GAME, WELL PLAYED!");
		ask.close();
	}
}
