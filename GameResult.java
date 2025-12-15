import java.util.*;

public class GameResult {

    public static class PlayerSummary {
        public String name;
        public int score;
        public int rank;
    }

    public List<PlayerSummary> leaderboard;
    public double averageScore;
    public int maxScore;
    public int minScore;

    public static GameResult fromPlayers(Player[] players, String[] names) {
        GameResult result = new GameResult();
        result.leaderboard = new ArrayList<>();

        int n = players.length;
        int[] ranks = new int[n];
        Arrays.fill(ranks, 1);

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (players[j].totalscore > players[i].totalscore) {
                    ranks[i]++;
                }
            }
        }

        int total = 0;
        int max = Integer.MIN_VALUE;
        int min = Integer.MAX_VALUE;

        for (int i = 0; i < n; i++) {
            PlayerSummary ps = new PlayerSummary();
            ps.name = names[i];
            ps.score = players[i].totalscore;
            ps.rank = ranks[i];

            result.leaderboard.add(ps);

            total += ps.score;
            max = Math.max(max, ps.score);
            min = Math.min(min, ps.score);
        }

        result.averageScore = total / (n + 0.0);
        result.maxScore = max;
        result.minScore = min;

        result.leaderboard.sort(Comparator.comparingInt(p -> p.rank));
        return result;
    }
}