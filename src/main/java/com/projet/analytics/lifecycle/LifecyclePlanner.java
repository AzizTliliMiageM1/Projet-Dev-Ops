package com.projet.analytics.lifecycle;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.projet.analytics.lifecycle.MonthlyOptimizer.MonthlyOptimizationResult;
import com.projet.backend.domain.Abonnement;

/**
 * Orchestre la planification du cycle de vie des abonnements sur N mois.
 * 
 * Algorithme:
 * 1. Pour chaque mois:
 *    a. Calculer l'utilité de chaque abonnement
 *    b. Générer des combinaisons possibles sous budget
 *    c. Sélectionner la meilleure combinaison
 * 2. Agréger les résultats mensuels
 * 3. Retourner un plan complet avec évaluation
 */
public class LifecyclePlanner {
    private final MonthlyOptimizer monthlyOptimizer;
    private final PlanEvaluator planEvaluator;

    public LifecyclePlanner() {
        this.monthlyOptimizer = new MonthlyOptimizer();
        this.planEvaluator = new PlanEvaluator();
    }

    /**
     * Génère un plan de cycle de vie sur N mois.
     * 
     * @param subscriptions Liste des abonnements disponibles
     * @param months Nombre de mois à planifier
     * @param budgetTarget Budget mensuel cible
     * @return LifecyclePlanResult avec le plan généré
     */
    public LifecyclePlanResult generatePlan(
        List<Abonnement> subscriptions,
        int months,
        double budgetTarget
    ) {
        long startTime = System.currentTimeMillis();

        try {
            // Validation
            if (subscriptions == null || subscriptions.isEmpty()) {
                return LifecyclePlanResult.failure("No subscriptions provided");
            }
            if (months <= 0) {
                return LifecyclePlanResult.failure("Months must be > 0");
            }
            if (budgetTarget < 0) {
                return LifecyclePlanResult.failure("Budget target cannot be negative");
            }

            // Générer les plans mensuels
            List<MonthlyPlan> monthlyPlans = new ArrayList<>();
            LocalDate currentDate = LocalDate.now();

            for (int monthIndex = 0; monthIndex < months; monthIndex++) {
                MonthlyPlan monthlyPlan = planMonth(
                    subscriptions,
                    monthIndex,
                    budgetTarget,
                    currentDate.plusMonths(monthIndex)
                );
                monthlyPlans.add(monthlyPlan);
            }

            // Évaluer et agréger
            double globalScore = planEvaluator.evaluate(monthlyPlans);
            double totalCost = planEvaluator.calculateTotalCost(monthlyPlans);

            SubscriptionLifecyclePlan plan = new SubscriptionLifecyclePlan(
                months,
                budgetTarget,
                monthlyPlans,
                totalCost,
                globalScore
            );

            long executionTime = System.currentTimeMillis() - startTime;
            return LifecyclePlanResult.success(plan, executionTime);

        } catch (Exception e) {
            return LifecyclePlanResult.failure("Error generating plan: " + e.getMessage());
        }
    }

    /**
     * Planifie les abonnements pour un mois donné.
     */
    private MonthlyPlan planMonth(
        List<Abonnement> subscriptions,
        int monthIndex,
        double budgetTarget,
        LocalDate monthDate
    ) {
        // Optimiser pour ce mois
        MonthlyOptimizationResult result = monthlyOptimizer.optimize(
            subscriptions,
            budgetTarget,
            monthIndex
        );

        // Créer le représentation mensuelle
        List<String> activeNames = result.selected.stream()
            .map(Abonnement::getNomService)
            .collect(Collectors.toList());

        String monthLabel = getMonthLabel(monthDate);

        return new MonthlyPlan(
            monthLabel,
            activeNames,
            result.decisions,
            result.monthlyCost,
            result.monthlyScore
        );
    }

    /**
     * Retourne le label du mois (ex: "January", "February", etc.)
     */
    private String getMonthLabel(LocalDate date) {
        Month month = date.getMonth();
        return month.toString().charAt(0) +
               month.toString().substring(1).toLowerCase();
    }
}
