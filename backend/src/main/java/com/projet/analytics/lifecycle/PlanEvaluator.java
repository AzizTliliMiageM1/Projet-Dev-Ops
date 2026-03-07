package com.projet.analytics.lifecycle;

import java.util.List;

/**
 * Évalue la qualité globale d'un plan sur plusieurs mois.
 * 
 * Calcule:
 * - Score global (moyenne pondérée des scores mensuels)
 * - Coût total
 * - Efficacité budget (ratio value/cost)
 */
public class PlanEvaluator {

    /**
     * Évalue un plan multi-mois.
     * 
     * @param monthlyPlans Liste des plans mensuels
     * @return Score global normalisé [0-100]
     */
    public double evaluate(List<MonthlyPlan> monthlyPlans) {
        if (monthlyPlans == null || monthlyPlans.isEmpty()) {
            return 0.0;
        }

        double totalScore = monthlyPlans.stream()
            .mapToDouble(MonthlyPlan::getMonthlyScore)
            .sum();

        double avgScore = totalScore / monthlyPlans.size();

        // Normaliser à [0-100]
        return Math.min(100, Math.max(0, avgScore));
    }

    /**
     * Calcule le coût total du plan.
     */
    public double calculateTotalCost(List<MonthlyPlan> monthlyPlans) {
        return monthlyPlans.stream()
            .mapToDouble(MonthlyPlan::getMonthlyCost)
            .sum();
    }

    /**
     * Calcule l'efficacité budget (value/cost)
     */
    public double calculateBudgetEfficiency(List<MonthlyPlan> monthlyPlans) {
        double totalScore = monthlyPlans.stream()
            .mapToDouble(MonthlyPlan::getMonthlyScore)
            .sum();

        double totalCost = calculateTotalCost(monthlyPlans);

        if (totalCost == 0) {
            return totalScore > 0 ? 100 : 0;
        }

        return totalScore / totalCost;
    }
}
