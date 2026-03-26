package student40006741.recommendation.client;

import student40006741.recommendation.dto.AiStructuredResponseDto;

public interface RecommendationAiClient {
    AiStructuredResponseDto analyzeText(String text);
}
