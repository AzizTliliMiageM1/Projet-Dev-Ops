package com.projet.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import spark.Spark;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ApiServerIntegrationTest {

    private static final int TEST_PORT = 5678;
    private static final String BASE_URL = "http://localhost:" + TEST_PORT;

    private final ObjectMapper mapper = new ObjectMapper();
    private final String testEmail = "backend.integration@test.com";
    private final String testPassword = "secret";
    private String userRecord;
    private HttpClient client;

    @BeforeAll
    void setUp() throws Exception {
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        appendConfirmedUser();
        System.setProperty("app.port", String.valueOf(TEST_PORT));

        CompletableFuture.runAsync(() -> ApiServer.main(new String[0]));
        waitForServerReady();

        client = HttpClient.newBuilder()
            .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL))
            .build();
    }

    @BeforeEach
    void resetData() throws Exception {
        cleanupUserSubscriptions();
    }

    @AfterAll
    void tearDown() throws Exception {
        Spark.stop();
        Spark.awaitStop();
        System.clearProperty("app.port");
        removeConfirmedUser();
        cleanupUserSubscriptions();
    }

    @Test
    void subscriptionLifecycle_shouldReturnConsistentResponses() throws Exception {
        login();

        HttpResponse<String> initialList = sendGet("/api/abonnements");
        assertEquals(200, initialList.statusCode());
        List<Map<String, Object>> initialItems = mapper.readValue(initialList.body(), new TypeReference<List<Map<String, Object>>>() {});
        assertEquals(0, initialItems.size());

        Map<String, Object> createPayload = new HashMap<>();
        createPayload.put("nomService", "Integration Service");
        createPayload.put("dateDebut", LocalDate.now().minusMonths(1));
        createPayload.put("dateFin", LocalDate.now().plusMonths(2));
        createPayload.put("prixMensuel", 19.99);
        createPayload.put("clientName", "Test Client");
        createPayload.put("derniereUtilisation", LocalDate.now().minusDays(3));
        createPayload.put("categorie", "Tests");

        HttpResponse<String> createResponse = sendJsonPost("/api/abonnements", mapper.writeValueAsString(createPayload));
        assertEquals(201, createResponse.statusCode());
        Map<String, Object> created = mapper.readValue(createResponse.body(), new TypeReference<Map<String, Object>>() {});
        assertEquals("Integration Service", created.get("nomService"));
        String subscriptionId = (String) created.get("id");
        assertNotNull(subscriptionId);

        Map<String, Object> updatePayload = new HashMap<>(createPayload);
        updatePayload.put("id", subscriptionId);
        updatePayload.put("prixMensuel", 24.99);

        HttpResponse<String> updateResponse = sendJsonPut("/api/abonnements/" + subscriptionId, mapper.writeValueAsString(updatePayload));
        assertEquals(200, updateResponse.statusCode());
        Map<String, Object> updated = mapper.readValue(updateResponse.body(), new TypeReference<Map<String, Object>>() {});
        assertEquals(24.99, ((Number) updated.get("prixMensuel")).doubleValue(), 0.01);

        HttpResponse<String> analyticsResponse = sendGet("/api/analytics/metrics");
        assertEquals(200, analyticsResponse.statusCode());
        Map<String, Object> metrics = mapper.readValue(analyticsResponse.body(), new TypeReference<Map<String, Object>>() {});
        assertTrue(metrics.containsKey("lifetimeValue"));
        assertTrue(metrics.containsKey("averageROI"));
        assertTrue(metrics.containsKey("highRiskCount"));

        HttpResponse<String> deleteResponse = sendDelete("/api/abonnements/" + subscriptionId);
        assertEquals(204, deleteResponse.statusCode());

        HttpResponse<String> finalList = sendGet("/api/abonnements");
        assertEquals(200, finalList.statusCode());
        List<Map<String, Object>> finalItems = mapper.readValue(finalList.body(), new TypeReference<List<Map<String, Object>>>() {});
        assertEquals(0, finalItems.size());
    }

    @Test
    void createSubscription_requiresAuthentication() throws Exception {
        HttpClient anonymousClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + "/api/abonnements"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString("{}", StandardCharsets.UTF_8))
            .build();
        HttpResponse<String> response = anonymousClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(401, response.statusCode());
        assertTrue(response.body().contains("Vous devez être connecté"));
    }

    @Test
    void createSubscription_withInvalidPayloadReturnsDetailedError() throws Exception {
        login();

        Map<String, Object> invalidPayload = Map.of(
            "nomService", "Invalid",
            "prixMensuel", -3,
            "clientName", "Test"
        );

        HttpResponse<String> response = sendJsonPost("/api/abonnements", mapper.writeValueAsString(invalidPayload));

        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("error"));
    }

    @Test
    void getUnknownSubscription_returns404() throws Exception {
        login();

        HttpResponse<String> response = sendGet("/api/abonnements/" + UUID.randomUUID());

        assertEquals(404, response.statusCode());
    }

    @Test
    void importCsv_reportsMalformedLines() throws Exception {
        login();

        String csvBody = "id;nomService;dateDebut;dateFin;prixMensuel;clientName;derniereUtilisation;categorie\n"
            + ";Valid;2025-01-01;2025-12-31;9.99;Test;2025-02-01;Streaming\n"
            + "bad-line-without-enough-fields";

        HttpResponse<String> response = sendPostRaw("/api/abonnements/import/csv", csvBody, "text/csv");

        assertEquals(201, response.statusCode());
        Map<String, Object> payload = mapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});
        assertEquals(1, ((Number) payload.get("imported")).intValue());
        List<?> errors = (List<?>) payload.get("errors");
        assertFalse(errors.isEmpty());
    }

    @Test
    void budgetPlan_returnsRecommendedCancellations() throws Exception {
        login();

        Map<String, Object> expensive = new HashMap<>();
        expensive.put("nomService", "LuxService");
        expensive.put("dateDebut", LocalDate.now().minusMonths(4));
        expensive.put("dateFin", LocalDate.now().plusMonths(4));
        expensive.put("prixMensuel", 60.0);
        expensive.put("clientName", "Test");
        expensive.put("derniereUtilisation", LocalDate.now().minusDays(120));
        expensive.put("categorie", "Loisir");

        Map<String, Object> essential = new HashMap<>();
        essential.put("nomService", "Essentiel");
        essential.put("dateDebut", LocalDate.now().minusMonths(2));
        essential.put("dateFin", LocalDate.now().plusMonths(8));
        essential.put("prixMensuel", 20.0);
        essential.put("clientName", "Test");
        essential.put("derniereUtilisation", LocalDate.now().minusDays(2));
        essential.put("categorie", "Travail");

        sendJsonPost("/api/abonnements", mapper.writeValueAsString(expensive));
        sendJsonPost("/api/abonnements", mapper.writeValueAsString(essential));

        HttpResponse<String> response = sendGet("/api/analytics/budget-plan?target=30");
        assertEquals(200, response.statusCode());

        Map<String, Object> plan = mapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});
        assertTrue((Boolean) plan.get("targetFeasible"));
        List<Map<String, Object>> recommended = (List<Map<String, Object>>) plan.get("recommendedCancellations");
        assertFalse(recommended.isEmpty());
        List<String> names = recommended.stream().map(item -> (String) item.get("nomService")).toList();
        assertTrue(names.contains("LuxService"));
    }

    @Test
    void budgetPlan_requiresTargetParameter() throws Exception {
        login();

        HttpResponse<String> response = sendGet("/api/analytics/budget-plan");
        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("target"));
    }

    @Test
    void sessionResponses_shouldBeValidJsonWithBackslashCharacters() throws Exception {
        login();

        HttpResponse<String> sessionResponse = sendGet("/api/session");
        assertEquals(200, sessionResponse.statusCode());
        Map<String, Object> sessionPayload = mapper.readValue(sessionResponse.body(), new TypeReference<Map<String, Object>>() {});
        assertTrue((Boolean) sessionPayload.get("authenticated"));
        assertEquals(testEmail, sessionPayload.get("email"));
        assertEquals("integration\\user", sessionPayload.get("pseudo"));

        HttpResponse<String> currentResponse = sendGet("/api/user/current");
        assertEquals(200, currentResponse.statusCode());
        Map<String, Object> currentPayload = mapper.readValue(currentResponse.body(), new TypeReference<Map<String, Object>>() {});
        assertEquals(testEmail, currentPayload.get("email"));
        assertTrue((Boolean) currentPayload.get("connected"));
    }

    private HttpResponse<String> sendGet(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + path)).GET().build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> sendJsonPost(String path, String body) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + path))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
            .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> sendJsonPut(String path, String body) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + path))
            .header("Content-Type", "application/json")
            .PUT(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
            .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> sendDelete(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + path)).DELETE().build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> sendFormPost(String path, String formBody) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + path))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(formBody, StandardCharsets.UTF_8))
            .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> sendPostRaw(String path, String body, String contentType) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + path))
            .header("Content-Type", contentType)
            .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
            .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private void appendConfirmedUser() throws IOException {
        Path path = Path.of("users-db.txt");
        if (!Files.exists(path)) {
            Files.createFile(path);
        }
        String token = UUID.randomUUID().toString();
        userRecord = String.join(";", List.of(testEmail, testPassword, "true", token, "integration\\user"));
        Files.writeString(path, userRecord + System.lineSeparator(), StandardCharsets.UTF_8, java.nio.file.StandardOpenOption.APPEND);
    }

    private void removeConfirmedUser() throws IOException {
        Path path = Path.of("users-db.txt");
        if (!Files.exists(path) || userRecord == null) {
            return;
        }
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        lines.removeIf(line -> line.equals(userRecord));
        Files.write(path, lines, StandardCharsets.UTF_8);
    }

    private void cleanupUserSubscriptions() throws IOException {
        String sanitized = testEmail.replaceAll("[^a-zA-Z0-9@._-]", "_");
        Path path = Path.of("data", "abonnements", "abonnements_" + sanitized + ".txt");
        if (Files.exists(path)) {
            Files.delete(path);
        }
    }

    private void login() throws Exception {
        HttpResponse<String> loginResponse = sendFormPost("/api/login", "email=" + testEmail + "&password=" + testPassword);
        assertEquals(200, loginResponse.statusCode());
    }

    private void waitForServerReady() throws InterruptedException {
        HttpClient probe = HttpClient.newHttpClient();
        long deadline = System.currentTimeMillis() + 8000;
        while (System.currentTimeMillis() < deadline) {
            try {
                HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + "/api/session")).GET().build();
                HttpResponse<String> response = probe.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() < 500) {
                    return;
                }
            } catch (IOException e) {
                Thread.sleep(200);
            }
        }
        throw new IllegalStateException("API server not ready within timeout");
    }
}
