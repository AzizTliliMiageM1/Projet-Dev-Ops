package com.projet.analytics.optimization;

/**
 * Fonction objectif linéaire combinant valeur, risque et confort.
 */
public final class ObjectiveFunction {

    private final double valueWeight;
    private final double riskWeight;
    private final double comfortWeight;

    public ObjectiveFunction(OptimizationConstraint constraint) {
        this.valueWeight = constraint.normalizedValueWeight();
        this.riskWeight = constraint.normalizedRiskWeight();
        this.comfortWeight = constraint.normalizedComfortWeight();
    }

    public double computeScore(SubscriptionScore score) {
        double raw = (valueWeight * score.valueScore())
            - (riskWeight * score.riskScore())
            + (comfortWeight * score.comfortScore());
        return clamp(raw);
    }

    public double normalizeToRatio(double score) {
        return clamp(score) / 100.0;
    }

    private double clamp(double value) {
        if (value < 0.0) {
            return 0.0;
        }
        if (value > 100.0) {
            return 100.0;
        }
        return value;
    }
}
