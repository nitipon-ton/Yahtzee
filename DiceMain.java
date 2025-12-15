public class DiceMain {

    // ================= ORIGINAL CONSOLE ENTRY =================
	/* 
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
            scanner.nextLine();

            if (isBot[i]) {
                usernames[i] = "bot " + ++botCount;
                players[i].bot = true;
                players[i].cheat = true;
            } else {
                players[i].cheat = true;
                System.out.println("Enter player name:");
                usernames[i] = scanner.nextLine();
            }
        }

        runGame(players, usernames, ranks);
        scanner.close();
    }
	*/

	public static void main(String[] args) {
    	GameResult result = DiceGame.playBots(5);

    	System.out.println("Average score: " + result.averageScore);
    	System.out.println("Top score: " + result.maxScore);

    	for (int i = 0; i < 5; i++) {
        	GameResult.PlayerSummary p = result.leaderboard.get(i);
        	System.out.println(p.rank + " " + p.name + " " + p.score);
    	}
	}

    // ================= NEW BOT-ONLY ENTRY =================
    public static void runBotsOnly(int numPlayer) {
        Player[] players = new Player[numPlayer];
        int[] ranks = new int[numPlayer];
        String[] usernames = new String[numPlayer];

        for (int i = 0; i < numPlayer; i++) {
            players[i] = new Player();
            players[i].bot = true;
            players[i].cheat = true;
            usernames[i] = "BOT " + (i + 1);
        }

        runGame(players, usernames, ranks);
    }

    // ================= SHARED GAME LOGIC =================
    private static void runGame(Player[] players, String[] usernames, int[] ranks) {
        int numPlayer = players.length;

        for (int a = 1; a <= 13; a++) {
			players[0].printNormal("\n\n************* ROUND " + a + " *************\n\n");
            for (int i = 0; i < numPlayer; i++) {
				players[i].printNormal("----------------" + usernames[i] + "'s turn ");
                for (int j = 0; j < 3; j++) {
                    players[i].rollDice();
                    players[i].chooseScore();
                }
                players[i].resetfornextround();
            }

			if (a == 13) {
				System.out.println("------------------------------------------\nEND OF GAME SUMMARY");
				for (int i = 0; i < numPlayer; i++) {
					System.out.print("-----------------------\n" + usernames[i] + "'s score ");
                	players[i].checkScoreCard();
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
                        System.out.println("RANK " + rank + ": " + usernames[i]
                                + ": SCORE = " + players[i].totalscore);
                    }
                }
            }
        }

        int[] scoreInRange = new int[30];
        for (int rank = 1; rank <= numPlayer; rank++) {
            for (int i = 0; i < numPlayer; i++) {
                if (ranks[i] == rank) {
                    scoreInRange[players[i].totalscore / 20]++;
                }
            }
        }

        int totalScore = 0;
        for (Player p : players) {
            totalScore += p.totalscore;
        }

        System.out.println("The average score is " + totalScore / (numPlayer + 0.0));
    }
}