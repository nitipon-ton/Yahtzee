import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.net.URI;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class WebServer {

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newFixedThreadPool(2));

        // favicon
        server.createContext("/favicon.ico", exchange -> {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
        });

        // health
        server.createContext("/health", exchange -> {
            byte[] response = "Yahtzee server running".getBytes();
            exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });

        // play
        server.createContext("/play", exchange -> {
            if (!exchange.getRequestMethod().equals("GET")) {
                exchange.sendResponseHeaders(405, -1);
                exchange.close();
                return;
            }

            int bots = parseBots(exchange.getRequestURI());
            bots = Math.max(1, Math.min(100, bots));

            // --- CHANGE: capture all System.out prints from DiceGame/Player ---
            GameResult result;
            String gameLog;

            synchronized (WebServer.class) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintStream oldOut = System.out;
                System.setOut(new PrintStream(baos));

                try {
                    result = DiceGame.playBots(bots);
                } finally {
                    System.out.flush();
                    System.setOut(oldOut);
                }
                gameLog = baos.toString();
            }


            // Build HTML
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html><html><head>")
                .append("<title>Yahtzee Bot Simulation</title>")
                .append("<style>")
                .append("body { font-family: monospace; background:#111; color:#eee; padding:20px; }")
                .append("h1 { color:#7dd3fc; }")
                .append(".meta { margin-bottom:15px; color:#aaa; }")
                .append("table { border-collapse: collapse; width: 100%; margin-bottom:20px; }")
                .append("th, td { border: 1px solid #555; padding: 8px; text-align: left; }")
                .append("th { background:#222; } td { background:#111; }")
                .append("pre { background:#000; padding:15px; border-radius:8px; overflow:auto; max-height:50vh; }")
                .append("</style></head><body>")
                .append("<h1>Yahtzee Bot Simulation</h1>")
                .append("<div class='meta'>Bots: ").append(bots).append("</div>")
                .append("<div class='meta'>Average Score: ").append(String.format("%.2f", result.averageScore))
                .append(" | Max Score: ").append(result.maxScore)
                .append(" | Min Score: ").append(result.minScore)
                .append("</div>");

            // Leaderboard table
            html.append("<table><tr><th>Rank</th><th>Name</th><th>Score</th></tr>");
            for (GameResult.PlayerSummary ps : result.leaderboard) {
                html.append("<tr><td>").append(ps.rank)
                    .append("</td><td>").append(ps.name)
                    .append("</td><td>").append(ps.score)
                    .append("</td></tr>");
            }
            html.append("</table>");

            html.append("<h2>Bot Game Logs (Including Full Probabilities Analysis!)</h2><pre>").append(gameLog).append("</pre>");

            html.append("</body></html>");

            byte[] response = html.toString().getBytes();
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });

        server.start();
        System.out.println("Server running:");
        System.out.println("  http://localhost:8080/play?bots=10");
        System.out.println("  http://localhost:8080/health");
        Thread.currentThread().join();
    }

    // -------- helpers --------
    private static int parseBots(URI uri) {
        String query = uri.getQuery();
        if (query == null) return 10;

        for (String part : query.split("&")) {
            String[] kv = part.split("=");
            if (kv.length == 2 && kv[0].equals("bots")) {
                try { return Integer.parseInt(kv[1]); }
                catch (NumberFormatException ignored) {}
            }
        }
        return 10;
    }
}