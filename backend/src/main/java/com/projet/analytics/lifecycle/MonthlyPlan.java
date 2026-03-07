package com.projet.analytics.lifecycle;

import java.util.List;
import java.util.Map;

/**
 * Représente le plan d'abonnements pour un mois donné.
 */
public class MonthlyPlan {
    private final String month;
    private final List<String> activeSubscriptions;
    private final Map<String, LifecycleDecision> decisions;
    private final double monthlyCost;
    private final double monthlyScore;

    public MonthlyPlan(
        String month,
        List<String> activeSubscriptions,
        Map<String, LifecycleDecision> decisions,
        double monthlyCost,
        double monthlyScore
    ) {
        this.month = month;
        this.activeSubscriptions = activeSubscriptions;
        this.decisions = decisions;
        this.monthlyCost = monthlyCost;
        this.monthlyScore = monthlyScore;
    }

    public String getMonth() {
        return month;
    }

    public List<String> getActiveSubscriptions() {
        return activeSubscriptions;
    }

    public Map<String, LifecycleDecision> getDecisions() {
        return decisions;
    }

    public double getMonthlyCost() {
        return monthlyCost;
    }

    public double getMonthlyScore() {
        return monthlyScore;
    }
}
