package com.projet.analytics.lifecycle;

import java.util.List;

/**
 * Représente le plan de cycle de vie global pour N mois.
 */
public class SubscriptionLifecyclePlan {
    private final int months;
    private final double budgetTarget;
    private final List<MonthlyPlan> monthlyPlans;
    private final double totalCost;
    private final double globalObjectiveScore;

    public SubscriptionLifecyclePlan(
        int months,
        double budgetTarget,
        List<MonthlyPlan> monthlyPlans,
        double totalCost,
        double globalObjectiveScore
    ) {
        this.months = months;
        this.budgetTarget = budgetTarget;
        this.monthlyPlans = monthlyPlans;
        this.totalCost = totalCost;
        this.globalObjectiveScore = globalObjectiveScore;
    }

    public int getMonths() {
        return months;
    }

    public double getBudgetTarget() {
        return budgetTarget;
    }

    public List<MonthlyPlan> getMonthlyPlans() {
        return monthlyPlans;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public double getGlobalObjectiveScore() {
        return globalObjectiveScore;
    }
}
