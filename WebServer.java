import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

public class WebServer {

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Health check
        server.createContext("/health", exchange -> {
            String response = "Yahtzee server running";
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.close();
        });

        // Play endpoint (placeholder)
        server.createContext("/play", exchange -> {
            String response = DiceGame.play();
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.close();
        });

        server.start();
        System.out.println("Server running at http://localhost:8080/play and http://localhost:8080/health");
    }
}