package student40006741.recommendation.service;

import org.junit.jupiter.api.Test;
import student40006741.recommendation.dto.AiRecommendedTypeDto;
import student40006741.recommendation.dto.AiStructuredResponseDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecommendationCatalogServiceTest {

    @Test
    void buildSuggestions_mapsKnownDomainsToSubscriptions() {
        RecommendationCatalogService service = new RecommendationCatalogService();
        AiStructuredResponseDto aiResponse = new AiStructuredResponseDto();
        aiResponse.setTopDomain("music");
        aiResponse.setRecommendedTypes(List.of(
                new AiRecommendedTypeDto("music", "high", "fort intérêt pour la musique")
        ));

        var suggestions = service.buildSuggestions(aiResponse);

        assertEquals(2, suggestions.size());
        assertEquals("Spotify Premium", suggestions.get(0).getTitle());
        assertEquals("music", suggestions.get(0).getDomain());
        assertEquals("high", suggestions.get(0).getUtilityLevel());
        assertEquals("fort intérêt pour la musique", suggestions.get(0).getReasonText());
    }

    @Test
    void buildSuggestions_fallsBackToTopDomain() {
        RecommendationCatalogService service = new RecommendationCatalogService();
        AiStructuredResponseDto aiResponse = new AiStructuredResponseDto();
        aiResponse.setTopDomain("books");
        aiResponse.setNeedLevel("medium");

        var suggestions = service.buildSuggestions(aiResponse);

        assertEquals(1, suggestions.size());
        assertEquals("Kindle Unlimited", suggestions.get(0).getTitle());
        assertTrue(suggestions.get(0).getReasonText().contains("domaine principal"));
    }
}
