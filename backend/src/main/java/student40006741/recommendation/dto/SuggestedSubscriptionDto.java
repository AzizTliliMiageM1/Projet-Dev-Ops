package student40006741.recommendation.dto;

public class SuggestedSubscriptionDto {
    private String title;
    private String domain;
    private String utilityLevel;
    private String reasonText;

    public SuggestedSubscriptionDto() {
    }

    public SuggestedSubscriptionDto(String title, String domain, String utilityLevel, String reasonText) {
        this.title = title;
        this.domain = domain;
        this.utilityLevel = utilityLevel;
        this.reasonText = reasonText;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public String getReasonText() {
        return reasonText;
    }

    public void setReasonText(String reasonText) {
        this.reasonText = reasonText;
    }
}
