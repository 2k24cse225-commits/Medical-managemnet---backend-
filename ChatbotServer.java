import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.*;
import java.net.http.*;

public class ChatbotServer {

    private static final String API_URL = "https://api.sambanova.ai/v1/chat/completions";

    public static void main(String[] args) throws Exception {

        // Get API Key from environment variable
        String apiKey = System.getenv("SAMBANOVA_API_KEY");

        if (apiKey == null) {
            System.out.println("API Key not found! Set SAMBANOVA_API_KEY in environment.");
            return;
        }

        // Create server
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/chat", (HttpExchange exchange) -> {

            if ("POST".equals(exchange.getRequestMethod())) {

                // Read request body (user message)
                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
                BufferedReader br = new BufferedReader(isr);
                String userMessage = br.readLine();

                // Create JSON request
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

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                // Send response back
                byte[] resp = response.body().getBytes();
                exchange.sendResponseHeaders(200, resp.length);
                OutputStream os = exchange.getResponseBody();
                os.write(resp);
                os.close();

            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        });

        server.start();
        System.out.println("Server running on port 8080...");
    }
}
