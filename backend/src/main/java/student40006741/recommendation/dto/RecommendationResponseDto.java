package student40006741.recommendation.dto;

import java.util.ArrayList;
import java.util.List;

public class RecommendationResponseDto {
    private String mainNeed;
    private String needLevel;
    private String topDomain;
    private double confidence;
    private List<SuggestedSubscriptionDto> suggestions = new ArrayList<>();

    public RecommendationResponseDto() {
    }

    public RecommendationResponseDto(
            String mainNeed,
            String needLevel,
            String topDomain,
            double confidence,
            List<SuggestedSubscriptionDto> suggestions
    ) {
        this.mainNeed = mainNeed;
        this.needLevel = needLevel;
        this.topDomain = topDomain;
        this.confidence = confidence;
        this.suggestions = suggestions == null ? new ArrayList<>() : suggestions;
    }

    public String getMainNeed() {
        return mainNeed;
    }

    public void setMainNeed(String mainNeed) {
        this.mainNeed = mainNeed;
    }

    public String getNeedLevel() {
        return needLevel;
    }

    public void setNeedLevel(String needLevel) {
        this.needLevel = needLevel;
    }

    public String getTopDomain() {
        return topDomain;
    }

    public void setTopDomain(String topDomain) {
        this.topDomain = topDomain;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public List<SuggestedSubscriptionDto> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<SuggestedSubscriptionDto> suggestions) {
        this.suggestions = suggestions == null ? new ArrayList<>() : suggestions;
    }
}
