package com.projet.api;

import java.io.IOException;
import java.net.URI;
import java.net.ServerSocket;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.projet.analytics.optimization.OptimizationResult;

import spark.Spark;

@DisplayName("Tests d'intégration de l'API Server")
class ApiServerIntegrationTest {

    private static String baseUrl;
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(2);
    private static int testPort;

    private static Thread serverThread;
    private static HttpClient client;
    private static ObjectMapper objectMapper;

    @BeforeAll
    static void startServer() throws Exception {
        System.setProperty("DISABLE_AUTH_FOR_TESTS", "true");
        testPort = findAvailablePort();
        System.setProperty("app.port", String.valueOf(testPort));
        baseUrl = "http://localhost:" + testPort;

        client = HttpClient.newHttpClient();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        serverThread = new Thread(() -> ApiServer.main(new String[0]));
        serverThread.setDaemon(true);
        serverThread.start();

        waitForServerStartup();
    }

    @AfterAll
    static void stopServer() throws Exception {
        try {
            Spark.stop();
            Thread.sleep(1000);
        } catch (Exception ignored) {
            // Ignore arrêt Spark en fin de test
        } finally {
            System.clearProperty("DISABLE_AUTH_FOR_TESTS");
            System.clearProperty("app.port");
        }
    }

    @Test
    @DisplayName("Doit retourner les analytics d'optimisation en JSON valide")
    void shouldReturnOptimizationAnalytics() throws Exception {
        HttpResponse<String> response = sendGet("/api/analytics/optimization");

        assertEquals(200, response.statusCode());

        OptimizationResult result =
            objectMapper.readValue(response.body(), OptimizationResult.class);

        assertAll(
            () -> assertNotNull(result, "Le résultat ne doit pas être null"),
            () -> assertNotNull(result.getSuggestions(), "Les suggestions ne doivent pas être null"),
            () -> assertTrue(
                result.getTotalEconomiePotentielle() >= 0,
                "L'économie potentielle totale doit être positive ou nulle"
            )
        );

        if (!result.getSuggestions().isEmpty()) {
            var firstSuggestion = result.getSuggestions().get(0);

            assertAll(
                () -> assertNotNull(firstSuggestion, "La première suggestion ne doit pas être null"),
                () -> assertNotNull(firstSuggestion.getAbonnement(), "L'abonnement de la suggestion ne doit pas être null"),
                () -> assertNotNull(firstSuggestion.getAction(), "L'action de la suggestion ne doit pas être null")
            );
        }
    }

    @Test
    @DisplayName("Doit permettre un cycle GET POST GET DELETE sur les abonnements")
    void shouldSupportGetPostGetDeleteFlow() throws Exception {
        // GET initial
        HttpResponse<String> initialGetResponse = sendGet("/api/abonnements");
        assertEquals(200, initialGetResponse.statusCode());

        // POST
        Map<String, Object> payload = Map.of(
            "nomService", "ITestService",
            "dateDebut", "2025-10-01",
            "dateFin", "2026-10-01",
            "prixMensuel", 5.5,
            "clientName", "Test",
            "derniereUtilisation", "2025-10-15",
            "categorie", "it"
        );

        HttpResponse<String> postResponse = sendPost("/api/abonnements", objectMapper.writeValueAsString(payload));

        assertEquals(201, postResponse.statusCode());

        Map<String, Object> createdSubscription =
            objectMapper.readValue(postResponse.body(), new TypeReference<Map<String, Object>>() {});

        String uuid = (String) createdSubscription.get("id");

        assertNotNull(uuid, "L'identifiant retourné par le POST ne doit pas être null");

        // GET après POST
        HttpResponse<String> secondGetResponse = sendGet("/api/abonnements");

        assertAll(
            () -> assertEquals(200, secondGetResponse.statusCode()),
            () -> assertTrue(
                secondGetResponse.body().contains("ITestService"),
                "La liste des abonnements devrait contenir ITestService"
            )
        );

        // DELETE
        HttpResponse<String> deleteResponse = sendDelete("/api/abonnements/" + uuid);

        assertTrue(
            deleteResponse.statusCode() == 200 || deleteResponse.statusCode() == 204,
            "Le DELETE doit retourner 200 ou 204"
        );
    }

    // =========================
    // Helpers - Startup
    // =========================

    private static void waitForServerStartup() throws Exception {
        long startTime = System.currentTimeMillis();
        boolean serverUp = false;

        while (System.currentTimeMillis() - startTime < 5000) {
            try {
                HttpResponse<String> response = sendGet("/api/abonnements");
                if (response.statusCode() == 200) {
                    serverUp = true;
                    break;
                }
            } catch (Exception ignored) {
                Thread.sleep(200);
            }
        }

        if (!serverUp) {
            throw new IllegalStateException("Le serveur API n'a pas démarré dans le temps imparti");
        }
    }

    // =========================
    // Helpers - HTTP
    // =========================

    private static HttpResponse<String> sendGet(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(buildUri(path))
            .timeout(REQUEST_TIMEOUT)
            .GET()
            .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static HttpResponse<String> sendPost(String path, String jsonBody) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(buildUri(path))
            .timeout(REQUEST_TIMEOUT)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static HttpResponse<String> sendDelete(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(buildUri(path))
            .timeout(REQUEST_TIMEOUT)
            .DELETE()
            .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static URI buildUri(String path) {
        return URI.create(baseUrl + path);
    }

    private static int findAvailablePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new IllegalStateException("Impossible de réserver un port de test", e);
        }
    }
}