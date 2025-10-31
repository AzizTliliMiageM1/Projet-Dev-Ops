package com.projet.api;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import spark.Spark;

/**
 * Test d'intégration minimal qui démarre ApiServer puis exécute quelques requêtes HTTP.
 */
public class ApiServerIntegrationTest {
    private static Thread serverThread;
    private static final HttpClient client = HttpClient.newHttpClient();

    @BeforeAll
    public static void startServer() throws Exception {
        serverThread = new Thread(() -> ApiServer.main(new String[0]));
        serverThread.setDaemon(true);
        serverThread.start();

        // Attendre que Spark démarre (polling)
        long start = System.currentTimeMillis();
        boolean up = false;
        while (System.currentTimeMillis() - start < 5000) {
            try {
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:4567/api/abonnements"))
                        .timeout(Duration.ofMillis(1000))
                        .GET()
                        .build();
                HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() == 200) {
                    up = true;
                    break;
                }
            } catch (IOException | InterruptedException e) {
                Thread.sleep(200);
            }
        }
        if (!up) {
            // try one more time but fail test startup
            throw new IllegalStateException("Le serveur API n'a pas démarré dans le temps imparti");
        }
    }

    @AfterAll
    public static void stopServer() throws Exception {
        try {
            Spark.stop();
            Thread.sleep(300);
        } finally {
            // nothing
        }
    }

    @Test
    public void testGetAndPostAndDeleteFlow() throws Exception {
        // GET initial
        HttpRequest getReq = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:4567/api/abonnements"))
                .GET()
                .build();
        HttpResponse<String> getResp = client.send(getReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, getResp.statusCode());

        // POST a new abonnement
        String json = "{\"nomService\":\"ITestService\",\"dateDebut\":\"2025-10-01\",\"dateFin\":\"2026-10-01\",\"prixMensuel\":5.5,\"clientName\":\"Test\",\"derniereUtilisation\":\"2025-10-15\",\"categorie\":\"it\"}";
        HttpRequest postReq = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:4567/api/abonnements"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> postResp = client.send(postReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, postResp.statusCode());

        // GET again should return array containing posted item
        HttpResponse<String> getResp2 = client.send(getReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, getResp2.statusCode());
        assertTrue(getResp2.body().contains("ITestService"));

        // Delete last element (find last index by parsing simple heuristic)
        // We assume posted item is present; delete index 0..n-1, but for simplicity delete index 0 if present
        // Here just delete index 0 to exercise endpoint (the repository uses indices)
        HttpRequest delReq = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:4567/api/abonnements/0"))
                .DELETE()
                .build();
        HttpResponse<String> delResp = client.send(delReq, HttpResponse.BodyHandlers.ofString());
        // either 204 or 200 depending on state
        assertTrue(delResp.statusCode() == 204 || delResp.statusCode() == 200);
    }
}
