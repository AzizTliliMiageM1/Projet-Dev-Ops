package com.projet.analytics.optimization;

import com.projet.backend.domain.Abonnement;

public class OptimizationSuggestion {
    private Abonnement abonnement;
    private OptimizationAction action;
    private double score;
    private String justification;
    private double economiePotentielle;

    public OptimizationSuggestion(Abonnement abonnement, OptimizationAction action, double score, String justification, double economiePotentielle) {
        this.abonnement = abonnement;
        this.action = action;
        this.score = score;
        this.justification = justification;
        this.economiePotentielle = economiePotentielle;
    }

    // Getters
    public Abonnement getAbonnement() {
        return abonnement;
    }

    public OptimizationAction getAction() {
        return action;
    }

    public double getScore() {
        return score;
    }

    public String getJustification() {
        return justification;
    }

    public double getEconomiePotentielle() {
        return economiePotentielle;
    }
}
