import java.util.Scanner;
public class DiceMain {
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		System.out.println("Type the number of players");
		int numPlayer = scanner.nextInt();
		while (numPlayer <= 0) {
			numPlayer = scanner.nextInt();
		}
		Player[] players = new Player[numPlayer];
		int[] ranks = new int[numPlayer];
		String[] usernames = new String[numPlayer];
		boolean[] isBot = new boolean[numPlayer];
		int botCount = 0;
		for (int i = 0; i < numPlayer; i++) {
			players[i] = new Player();
			System.out.println("Type 1 to let bot play character #" + (i + 1));
			System.out.println("Type 0 to let human play character #" + (i + 1));
			isBot[i] = scanner.nextInt() == 1;
			scanner.nextLine(); // Consume the leftover newline character
			if (isBot[i]) {
				usernames[i] = "bot " + ++botCount;
				players[i].bot = true;
				players[i].cheat = true;
			} else {
				players[i].cheat = true; // Enable cheat mode for human players
				System.out.println("Enter player name:");
				usernames[i] = scanner.nextLine(); // Now waits for actual input
			}
		}
		for (int a = 1; a <= 13; a++) {
			for (int i = 0; i < numPlayer; i++) {
				System.out.print(usernames[i] + "'s turn ");
				for (int j = 0; j < 3; j++) {
					players[i].rollDice();
					players[i].chooseScore();
				}
				players[i].resetfornextround();
			}
			System.out.println("------------------------------------------\nEND OF ROUND (" + a + ")");
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
		for (int i = 4; i <= 18; i++) {
			System.out.print("score " + 20 * i + "-" + (20 * i + 19) + "  ");
			for (int j = 0; j < scoreInRange[i] / figSize; j++) {
				System.out.print(fig);
			}
			System.out.print("\n");
		}
		int totalScore = 0;
		for (Player p: players) {
			totalScore += p.totalscore;
		}
		System.out.println("The average score is " + totalScore / (numPlayer + 0.0));

		System.out.println("GOOD GAME, WELL PLAYED!");
		scanner.close();
	}
}