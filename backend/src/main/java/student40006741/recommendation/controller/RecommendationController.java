package student40006741.recommendation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import student40006741.recommendation.dto.RecommendationRequestDto;
import student40006741.recommendation.exception.RecommendationException;
import student40006741.recommendation.service.RecommendationService;

import java.util.Map;

import static spark.Spark.post;

public class RecommendationController {

    private final RecommendationService recommendationService;
    private final ObjectMapper objectMapper;

    public RecommendationController(RecommendationService recommendationService, ObjectMapper objectMapper) {
        this.recommendationService = recommendationService;
        this.objectMapper = objectMapper;
    }

    public void registerRoutes() {
        post("/api/recommendations", (req, res) -> {
            res.type("application/json");
            try {
                RecommendationRequestDto requestDto =
                        objectMapper.readValue(req.body(), RecommendationRequestDto.class);
                res.status(200);
                return objectMapper.writeValueAsString(recommendationService.recommend(requestDto));
            } catch (IllegalArgumentException e) {
                res.status(400);
                return objectMapper.writeValueAsString(Map.of("error", e.getMessage()));
            } catch (RecommendationException e) {
                res.status(502);
                return objectMapper.writeValueAsString(Map.of("error", e.getMessage()));
            } catch (Exception e) {
                res.status(500);
                return objectMapper.writeValueAsString(Map.of("error", "Erreur interne lors de la recommandation"));
            }
        });
    }
}
