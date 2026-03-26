package student40006741.recommendation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import spark.Spark;
import student40006741.recommendation.client.RecommendationAiClient;
import student40006741.recommendation.dto.AiRecommendedTypeDto;
import student40006741.recommendation.dto.AiStructuredResponseDto;
import student40006741.recommendation.service.RecommendationCatalogService;
import student40006741.recommendation.service.RecommendationService;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecommendationControllerTest {

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    @BeforeAll
    static void startServer() {
        Spark.port(4568);
        RecommendationAiClient fakeClient = text -> {
            AiStructuredResponseDto response = new AiStructuredResponseDto();
            response.setMainNeed("entertainment_and_learning");
            response.setNeedLevel("medium");
            response.setTopDomain("video");
            response.setConfidence(0.91);
            response.setRecommendedTypes(List.of(
                    new AiRecommendedTypeDto("video", "medium", "intérêt pour les films")
            ));
            return response;
        };

        RecommendationService service =
                new RecommendationService(fakeClient, new RecommendationCatalogService());
        RecommendationController controller = new RecommendationController(service, new ObjectMapper());
        controller.registerRoutes();
        Spark.awaitInitialization();
    }

    @AfterAll
    static void stopServer() {
        Spark.stop();
        Spark.awaitStop();
    }

    @Test
    void postRecommendations_returnsJsonSuggestions() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:4568/api/recommendations"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("""
                        {"text":"J'aime regarder des films."}
                        """))
                .build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"mainNeed\":\"entertainment_and_learning\""));
        assertTrue(response.body().contains("\"title\":\"Netflix Standard\""));
    }

    @Test
    void postRecommendations_withBlankText_returnsBadRequest() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:4568/api/recommendations"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("""
                        {"text":" "}
                        """))
                .build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Le texte est obligatoire"));
    }
}
