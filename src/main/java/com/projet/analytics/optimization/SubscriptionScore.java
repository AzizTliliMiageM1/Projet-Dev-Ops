package com.projet.analytics.optimization;

/**
 * Regroupe les composantes de score utilisées par la fonction objectif.
 */
public final class SubscriptionScore {

    private final double valueScore;
    private final double riskScore;
    private final double comfortScore;

    private SubscriptionScore(double valueScore, double riskScore, double comfortScore) {
        this.valueScore = clamp(valueScore);
        this.riskScore = clamp(riskScore);
        this.comfortScore = clamp(comfortScore);
    }

    public static SubscriptionScore of(double valueScore, double riskScore, double comfortScore) {
        return new SubscriptionScore(valueScore, riskScore, comfortScore);
    }

    private double clamp(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return 0.0;
        }
        if (value < 0.0) {
            return 0.0;
        }
        if (value > 100.0) {
            return 100.0;
        }
        return value;
    }

    public double valueScore() {
        return valueScore;
    }

    public double riskScore() {
        return riskScore;
    }

    public double comfortScore() {
        return comfortScore;
    }
}
