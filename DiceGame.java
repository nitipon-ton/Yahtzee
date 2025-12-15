public class DiceGame {

    public static GameResult playBots(int numBots) {

        Player[] players = new Player[numBots];
        String[] usernames = new String[numBots];

        for (int i = 0; i < numBots; i++) {
            players[i] = new Player();
            players[i].bot = true;
            players[i].cheat = true;
            usernames[i] = "BOT " + (i + 1);
        }

        // Standard Yahtzee = 13 rounds
        for (int round = 1; round <= 13; round++) {
            players[0].printNormal("\n\n***************** ROUND " + round + " *****************\n\n");
            for (int i = 0; i < numBots; i++) {
                players[i].printNormal("----------------" + usernames[i] + "'s turn ");
                for (int roll = 0; roll < 3; roll++) {
                    players[i].rollDice();
                    players[i].chooseScore();
                }
                players[i].resetfornextround();
            }

            if (round == 13) {
                players[0].printNormal("------------------------------------------\nEND OF GAME SUMMARY\n");
				for (int i = 0; i < numBots; i++) {
                    players[0].printNormal("-----------------------\n" + usernames[i] + "'s score ");
                	players[i].checkScoreCard();
				}
			}

            int[] ranks = new int[numBots];
            java.util.Arrays.fill(ranks, 1);
            for (int i = 0; i < numBots; i++) {
                for (int j = 0; j < numBots; j++) {
                    if (players[j].totalscore > players[i].totalscore) {
                        ranks[i]++;
                    }
                }
            }

            for (int rank = 1; rank <= numBots; rank++) {
                for (int i = 0; i < numBots; i++) {
                    if (ranks[i] == rank) {
                        players[0].printNormal("RANK " + rank + ": " + usernames[i] + ": SCORE = " + players[i].totalscore + "\n");
                    }
                }
            }
        }

        return GameResult.fromPlayers(players, usernames);
    }
}