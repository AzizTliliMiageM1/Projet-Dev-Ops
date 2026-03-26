package student40006741.recommendation.dto;

public class AiRecommendedTypeDto {
    private String domain;
    private String utilityLevel;
    private String reason;

    public AiRecommendedTypeDto() {
    }

    public AiRecommendedTypeDto(String domain, String utilityLevel, String reason) {
        this.domain = domain;
        this.utilityLevel = utilityLevel;
        this.reason = reason;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getUtilityLevel() {
        return utilityLevel;
    }

    public void setUtilityLevel(String utilityLevel) {
        this.utilityLevel = utilityLevel;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
