package student40006741.recommendation.service;

import student40006741.recommendation.dto.AiRecommendedTypeDto;
import student40006741.recommendation.dto.AiStructuredResponseDto;
import student40006741.recommendation.dto.SuggestedSubscriptionDto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RecommendationCatalogService {

    private final Map<String, List<String>> catalog = new LinkedHashMap<>();

    public RecommendationCatalogService() {
        catalog.put("music", List.of("Spotify Premium", "Deezer Premium"));
        catalog.put("video", List.of("Netflix Standard", "Disney+"));
        catalog.put("language_learning", List.of("Duolingo Super", "Babbel"));
        catalog.put("books", List.of("Kindle Unlimited"));
        catalog.put("news", List.of("Courrier international", "Mediapart"));
    }

    public List<SuggestedSubscriptionDto> buildSuggestions(AiStructuredResponseDto aiResponse) {
        List<SuggestedSubscriptionDto> suggestions = new ArrayList<>();
        Map<String, SuggestedSubscriptionDto> uniqueByTitle = new LinkedHashMap<>();

        List<AiRecommendedTypeDto> recommendedTypes = aiResponse.getRecommendedTypes();
        if (recommendedTypes != null) {
            for (AiRecommendedTypeDto recommendedType : recommendedTypes) {
                addSuggestionsForDomain(
                        uniqueByTitle,
                        normalize(recommendedType.getDomain()),
                        sanitizeLevel(recommendedType.getUtilityLevel()),
                        sanitizeReason(recommendedType.getReason(), recommendedType.getDomain())
                );
            }
        }

        if (uniqueByTitle.isEmpty()) {
            addSuggestionsForDomain(
                    uniqueByTitle,
                    normalize(aiResponse.getTopDomain()),
                    sanitizeLevel(aiResponse.getNeedLevel()),
                    sanitizeReason("domaine principal détecté", aiResponse.getTopDomain())
            );
        }

        suggestions.addAll(uniqueByTitle.values());
        return suggestions;
    }

    private void addSuggestionsForDomain(
            Map<String, SuggestedSubscriptionDto> uniqueByTitle,
            String domain,
            String utilityLevel,
            String reason
    ) {
        List<String> titles = catalog.get(domain);
        if (titles == null || titles.isEmpty()) {
            return;
        }

        for (String title : titles) {
            uniqueByTitle.putIfAbsent(
                    title,
                    new SuggestedSubscriptionDto(title, domain, utilityLevel, reason)
            );
        }
    }

    private String normalize(String domain) {
        if (domain == null) {
            return "";
        }
        return domain.trim().toLowerCase(Locale.ROOT);
    }

    private String sanitizeLevel(String level) {
        if (level == null) {
            return "medium";
        }
        String normalized = level.trim().toLowerCase(Locale.ROOT);
        if ("low".equals(normalized) || "medium".equals(normalized) || "high".equals(normalized)) {
            return normalized;
        }
        return "medium";
    }

    private String sanitizeReason(String reason, String domain) {
        if (reason != null && !reason.isBlank()) {
            return reason.trim();
        }
        return "intérêt détecté pour le domaine " + (domain == null ? "other" : domain);
    }
}
