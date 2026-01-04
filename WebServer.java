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

        // health (landing page)
        server.createContext("/health", exchange -> {
            if (!exchange.getRequestMethod().equals("GET")) {
                exchange.sendResponseHeaders(405, -1);
                exchange.close();
                return;
            }

            String html =
                "<!DOCTYPE html>" +
                "<html><head>" +
                "<title>Yahtzee Bot Simulator</title>" +
                "<meta charset='utf-8'>" +
                "<style>" +
                "body { font-family: system-ui, -apple-system, BlinkMacSystemFont, sans-serif;" +
                "background:#0f172a; color:#e5e7eb; padding:40px; max-width:900px; margin:auto; }" +
                "h1 { color:#7dd3fc; }" +
                "h2 { color:#93c5fd; margin-top:0px; }" +
                "p { line-height:1.6; }" +
                "input { padding:8px; font-size:16px; width:120px; }" +
                "button { padding:8px 14px; font-size:16px; margin-left:10px; cursor:pointer; }" +
                "a { color:#38bdf8; text-decoration:none; }" +
                "a:hover { text-decoration:underline; }" +
                ".box { background:#020617; padding:20px; border-radius:12px; margin-top:20px; }" +
                "</style>" +
                "</head><body>" +

                "<h1>Yahtzee Bot Simulator</h1>" +

                "<div class='box'>" +
                "<h2>What is Yahtzee?</h2>" +
                "<p>" +
                "Yahtzee is a classic dice game where players roll five dice up to three times per round " +
                "to maximize their score across 13 scoring categories such as Three-of-a-Kind, Full House, " +
                "Straights, and Yahtzee itself. Each category can only be used once per game, making " +
                "decision-making and probability trade-offs essential." +
                "</p>" +
                "</div>" +

                "<div class='box'>" +
                "<h2>What did I build?</h2>" +
                "<p>" +
                "This project simulates <strong>bots playing Yahtzee against each other</strong>. " +
                "Each bot evaluates all possible reroll combinations using probability calculations " +
                "to decide which dice to keep and which scoring category to select. " +
                "The full decision process and probability analysis are logged so the game is " +
                "completely transparent — nothing is random or hardcoded." +
                "</p>" +
                "</div>" +

                "<div class='box'>" +
                "<h2>Run a Simulation</h2>" +
                "<p>Enter the number of bots (positive integer):</p>" +

                "<input id='bots' type='number' min='1' step='1' placeholder='e.g. 10'>" +
                "<button onclick='go()'>Play</button>" +

                "</div>" +

                "<script>" +
                "function go() {" +
                "  const x = document.getElementById('bots').value;" +
                "  if (!x || x <= 0) return;" +
                "  window.location.href = " +
                "  'https://yahtzee-production.up.railway.app/play?bots=' + x;" +
                "}" +
                "</script>" +

                "</body></html>";

            byte[] response = html.getBytes();
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
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
        System.out.println("http://localhost:8080/play?bots=10");
        System.out.println("http://localhost:8080/health");
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