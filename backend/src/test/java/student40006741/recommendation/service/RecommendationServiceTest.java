package student40006741.recommendation.service;

import org.junit.jupiter.api.Test;
import student40006741.recommendation.client.RecommendationAiClient;
import student40006741.recommendation.dto.AiRecommendedTypeDto;
import student40006741.recommendation.dto.AiStructuredResponseDto;
import student40006741.recommendation.dto.RecommendationRequestDto;
import student40006741.recommendation.exception.RecommendationException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RecommendationServiceTest {

    @Test
    void recommend_returnsMappedSuggestions() {
        RecommendationAiClient fakeClient = text -> {
            AiStructuredResponseDto response = new AiStructuredResponseDto();
            response.setMainNeed("entertainment_and_learning");
            response.setNeedLevel("medium");
            response.setTopDomain("music");
            response.setConfidence(0.87);
            response.setRecommendedTypes(List.of(
                    new AiRecommendedTypeDto("music", "high", "fort intérêt pour la musique"),
                    new AiRecommendedTypeDto("language_learning", "medium", "intérêt pour les langues")
            ));
            return response;
        };

        RecommendationService service =
                new RecommendationService(fakeClient, new RecommendationCatalogService());

        var result = service.recommend(new RecommendationRequestDto("J'aime la musique et les langues."));

        assertEquals("entertainment_and_learning", result.getMainNeed());
        assertEquals("medium", result.getNeedLevel());
        assertEquals("music", result.getTopDomain());
        assertEquals(0.87, result.getConfidence());
        assertEquals(4, result.getSuggestions().size());
        assertEquals("Spotify Premium", result.getSuggestions().get(0).getTitle());
    }

    @Test
    void recommend_rejectsBlankText() {
        RecommendationService service =
                new RecommendationService(text -> new AiStructuredResponseDto(), new RecommendationCatalogService());

        assertThrows(IllegalArgumentException.class, () -> service.recommend(new RecommendationRequestDto("   ")));
    }

    @Test
    void recommend_rejectsInvalidAiResponse() {
        RecommendationService service =
                new RecommendationService(text -> new AiStructuredResponseDto(), new RecommendationCatalogService());

        assertThrows(
                RecommendationException.class,
                () -> service.recommend(new RecommendationRequestDto("texte utile"))
        );
    }
}
