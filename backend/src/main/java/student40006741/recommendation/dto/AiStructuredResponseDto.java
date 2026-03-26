package student40006741.recommendation.dto;

import java.util.ArrayList;
import java.util.List;

public class AiStructuredResponseDto {
    private String mainNeed;
    private String needLevel;
    private String topDomain;
    private double confidence;
    private List<String> interests = new ArrayList<>();
    private List<AiRecommendedTypeDto> recommendedTypes = new ArrayList<>();

    public AiStructuredResponseDto() {
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

    public List<String> getInterests() {
        return interests;
    }

    public void setInterests(List<String> interests) {
        this.interests = interests == null ? new ArrayList<>() : interests;
    }

    public List<AiRecommendedTypeDto> getRecommendedTypes() {
        return recommendedTypes;
    }

    public void setRecommendedTypes(List<AiRecommendedTypeDto> recommendedTypes) {
        this.recommendedTypes = recommendedTypes == null ? new ArrayList<>() : recommendedTypes;
    }
}
