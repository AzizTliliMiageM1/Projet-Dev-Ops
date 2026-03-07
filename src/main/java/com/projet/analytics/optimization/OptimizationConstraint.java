package com.projet.analytics.optimization;

/**
 * Paramètres d'optimisation fournis par l'utilisateur.
 * Garantit la cohérence des poids et du budget avant l'exécution de l'algorithme.
 */
public final class OptimizationConstraint {

    private final double budgetMax;
    private final double valueWeight;
    private final double riskWeight;
    private final double comfortWeight;
    private final double weightSum;

    private OptimizationConstraint(double budgetMax, double valueWeight, double riskWeight, double comfortWeight) {
        this.budgetMax = Math.max(0.0, budgetMax);
        this.valueWeight = Math.max(0.0, valueWeight);
        this.riskWeight = Math.max(0.0, riskWeight);
        this.comfortWeight = Math.max(0.0, comfortWeight);
        this.weightSum = this.valueWeight + this.riskWeight + this.comfortWeight;
        validate();
    }

    public static OptimizationConstraint of(double budgetMax, double valueWeight, double riskWeight, double comfortWeight) {
        return new OptimizationConstraint(budgetMax, valueWeight, riskWeight, comfortWeight);
    }

    private void validate() {
        if (weightSum <= 0.0) {
            throw new IllegalArgumentException("Au moins un poids doit être strictement positif");
        }
    }

    public double budgetMax() {
        return budgetMax;
    }

    public double valueWeight() {
        return valueWeight;
    }

    public double riskWeight() {
        return riskWeight;
    }

    public double comfortWeight() {
        return comfortWeight;
    }

    public double normalizedValueWeight() {
        return valueWeight / weightSum;
    }

    public double normalizedRiskWeight() {
        return riskWeight / weightSum;
    }

    public double normalizedComfortWeight() {
        return comfortWeight / weightSum;
    }
}
