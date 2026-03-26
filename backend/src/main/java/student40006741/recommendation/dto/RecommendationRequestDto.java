package student40006741.recommendation.dto;

public class RecommendationRequestDto {
    private String text;

    public RecommendationRequestDto() {
    }

    public RecommendationRequestDto(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
