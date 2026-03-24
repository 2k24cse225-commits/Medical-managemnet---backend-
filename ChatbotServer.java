import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.*;
import java.net.http.*;

public class ChatbotServer {

    private static final String API_URL = "https://api.sambanova.ai/v1/chat/completions";

    public static void main(String[] args) throws Exception {

        String apiKey = System.getenv("SAMBANOVA_API_KEY");

        if (apiKey == null) {
            System.out.println("API Key not found!");
            return;
        }

        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/chat", (HttpExchange exchange) -> {

            // ✅ CORS HEADERS (for all requests)
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

            // ✅ Handle OPTIONS (preflight)
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                try {
                    exchange.sendResponseHeaders(204, -1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {

                try {
                    // Read user input
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(exchange.getRequestBody(), "utf-8"));
                    String userMessage = br.readLine();

                    if (userMessage == null) userMessage = "Hello";

                    // JSON request
                    String jsonRequest = "{"
                            + "\"model\": \"Llama-4-Maverick-17B-128E-Instruct\","
                            + "\"messages\": ["
                            + "{\"role\":\"system\",\"content\":\"You are a helpful assistant\"},"
                            + "{\"role\":\"user\",\"content\":\"" + userMessage + "\"}"
                            + "],"
                            + "\"temperature\": 0.1,"
                            + "\"top_p\": 0.1"
                            + "}";

                    HttpClient client = HttpClient.newHttpClient();

                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(API_URL))
                            .header("Authorization", "Bearer " + apiKey)
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                            .build();

                    HttpResponse<String> response = client.send(
                            request, HttpResponse.BodyHandlers.ofString());

                    byte[] resp = response.body().getBytes();

                    exchange.sendResponseHeaders(200, resp.length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(resp);
                    os.close();

                } catch (Exception e) {
                    e.printStackTrace();
                    String error = "Error occurred";
                    try {
                        exchange.sendResponseHeaders(500, error.length());
                        OutputStream os = exchange.getResponseBody();
                        os.write(error.getBytes());
                        os.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }

            } else {
                try {
                    exchange.sendResponseHeaders(405, -1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        server.start();
        System.out.println("Server running on port " + port);
    }
}
