package shopflow.w00;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

/**
 * A tiny HTTP server built into the JDK (no frameworks). Used in Week 1 to see a request from the
 * server's side.
 *
 * Run from the repo root (single-file launch, no build needed):
 *   java core/src/main/java/shopflow/w00/TinyServer.java            # port 8080, one thread
 *   java core/src/main/java/shopflow/w00/TinyServer.java 9090 4     # port 9090, pool of 4 threads
 *
 * Endpoints:
 *   GET /hello  -> "Hello from <thread name>"
 *   GET /slow   -> sleeps 2 seconds, then answers (to see what a blocked thread does to others)
 */
public class TinyServer {

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        int threads = args.length > 1 ? Integer.parseInt(args[1]) : 0;

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/hello", exchange -> reply(exchange, "Hello from " + Thread.currentThread().getName()));
        server.createContext("/slow", exchange -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            reply(exchange, "Slow answer from " + Thread.currentThread().getName());
        });
        if (threads > 0) {
            server.setExecutor(Executors.newFixedThreadPool(threads));
        }
        server.start();
        System.out.println("Listening on http://localhost:" + port + "/hello (threads: "
                + (threads > 0 ? threads : "default single dispatcher") + "). Press Ctrl+C to stop.");
    }

    private static void reply(com.sun.net.httpserver.HttpExchange exchange, String text) throws IOException {
        byte[] body = (text + "\n").getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(200, body.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(body);
        }
    }
}
