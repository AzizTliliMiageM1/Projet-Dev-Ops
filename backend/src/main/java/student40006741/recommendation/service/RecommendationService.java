package student40006741.recommendation.service;

import student40006741.recommendation.client.RecommendationAiClient;
import student40006741.recommendation.dto.AiStructuredResponseDto;
import student40006741.recommendation.dto.RecommendationRequestDto;
import student40006741.recommendation.dto.RecommendationResponseDto;
import student40006741.recommendation.exception.RecommendationException;

public class RecommendationService {

    private final RecommendationAiClient recommendationAiClient;
    private final RecommendationCatalogService recommendationCatalogService;

    public RecommendationService(
            RecommendationAiClient recommendationAiClient,
            RecommendationCatalogService recommendationCatalogService
    ) {
        this.recommendationAiClient = recommendationAiClient;
        this.recommendationCatalogService = recommendationCatalogService;
    }

    public RecommendationResponseDto recommend(RecommendationRequestDto requestDto) {
        validate(requestDto);

        AiStructuredResponseDto aiResponse = recommendationAiClient.analyzeText(requestDto.getText().trim());
        validateAiResponse(aiResponse);

        return new RecommendationResponseDto(
                aiResponse.getMainNeed(),
                sanitizeNeedLevel(aiResponse.getNeedLevel()),
                aiResponse.getTopDomain(),
                sanitizeConfidence(aiResponse.getConfidence()),
                recommendationCatalogService.buildSuggestions(aiResponse)
        );
    }

    private void validate(RecommendationRequestDto requestDto) {
        if (requestDto == null || requestDto.getText() == null || requestDto.getText().isBlank()) {
            throw new IllegalArgumentException("Le texte est obligatoire");
        }
    }

    private void validateAiResponse(AiStructuredResponseDto aiResponse) {
        if (aiResponse == null) {
            throw new RecommendationException("Réponse IA absente");
        }
        if (aiResponse.getMainNeed() == null || aiResponse.getMainNeed().isBlank()) {
            throw new RecommendationException("Réponse IA invalide: mainNeed manquant");
        }
        if (aiResponse.getTopDomain() == null || aiResponse.getTopDomain().isBlank()) {
            throw new RecommendationException("Réponse IA invalide: topDomain manquant");
        }
    }

    private String sanitizeNeedLevel(String needLevel) {
        if (needLevel == null || needLevel.isBlank()) {
            return "medium";
        }
        String normalized = needLevel.trim().toLowerCase();
        if ("low".equals(normalized) || "medium".equals(normalized) || "high".equals(normalized)) {
            return normalized;
        }
        return "medium";
    }

    private double sanitizeConfidence(double confidence) {
        if (confidence < 0) {
            return 0;
        }
        if (confidence > 1) {
            return 1;
        }
        return confidence;
    }
}
