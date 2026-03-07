package com.projet.analytics.lifecycle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.projet.backend.domain.Abonnement;

/**
 * Sélectionne la meilleure combinaison d'abonnements pour un mois donné
 * en respectant la contrainte de budget.
 * 
 * Algorithme : Greedy Selection (sélection gloutonne)
 * 
 * 1. Calculer l'utilité pour chaque abonnement
 * 2. Trier par utilité décroissante
 * 3. Sélectionner les abonnements tant que le budget le permet
 * 4. Retourner la sélection avec les décisions et le coût
 */
public class MonthlyOptimizer {
    private final SubscriptionUtilityCalculator utilityCalculator;

    public MonthlyOptimizer() {
        this.utilityCalculator = new SubscriptionUtilityCalculator();
    }

    /**
     * Classe interne pour stocker abonnement + utilité
     */
    private static class UtilityScore {
        final Abonnement subscription;
        final double utility;

        UtilityScore(Abonnement subscription, double utility) {
            this.subscription = subscription;
            this.utility = utility;
        }
    }

    /**
     * Optimise pour un mois donné.
     * 
     * @param subscriptions Liste des abonnements disponibles
     * @param budgetMax Budget maximum pour ce mois
     * @param monthIndex Index du mois (pour prédictiond'utilisation)
     * @return MonthlyOptimizationResult avec sélection, décisions et coût
     */
    public MonthlyOptimizationResult optimize(
        List<Abonnement> subscriptions,
        double budgetMax,
        int monthIndex
    ) {
        if (subscriptions == null || subscriptions.isEmpty()) {
            return new MonthlyOptimizationResult(List.of(), new HashMap<>(), 0.0, 0.0);
        }

        // Calculer l'utilité de chaque abonnement
        double maxPrice = subscriptions.stream()
            .mapToDouble(Abonnement::getPrixMensuel)
            .max()
            .orElse(1.0);

        List<UtilityScore> scored = subscriptions.stream()
            .map(sub -> new UtilityScore(
                sub,
                utilityCalculator.calculateUtility(sub, monthIndex, maxPrice)
            ))
            .sorted((a, b) -> Double.compare(b.utility, a.utility)) // Décroissant
            .collect(Collectors.toList());

        // Sélection gloutonne sous contrainte budgétaire
        List<Abonnement> selected = new ArrayList<>();
        double currentCost = 0.0;
        double currentScore = 0.0;

        for (UtilityScore item : scored) {
            double price = Math.max(0.0, item.subscription.getPrixMensuel());
            if (currentCost + price <= budgetMax) {
                selected.add(item.subscription);
                currentCost += price;
                currentScore += item.utility;
            }
        }

        // Construire les décisions
        Map<String, LifecycleDecision> decisions = buildDecisions(subscriptions, selected);

        return new MonthlyOptimizationResult(selected, decisions, currentCost, currentScore);
    }

    /**
     * Construit la map des décisions (KEEP ou PAUSE) pour chaque abonnement.
     */
    private Map<String, LifecycleDecision> buildDecisions(
        List<Abonnement> all,
        List<Abonnement> selected
    ) {
        Map<String, LifecycleDecision> decisions = new HashMap<>();
        List<String> selectedIds = selected.stream()
            .map(Abonnement::getId)
            .collect(Collectors.toSet())
            .stream()
            .collect(Collectors.toList());

        for (Abonnement sub : all) {
            if (selectedIds.contains(sub.getId())) {
                decisions.put(sub.getNomService(), LifecycleDecision.KEEP);
            } else {
                decisions.put(sub.getNomService(), LifecycleDecision.PAUSE);
            }
        }

        return decisions;
    }

    /**
     * Résultat de l'optimisation pour un mois.
     */
    public static class MonthlyOptimizationResult {
        public final List<Abonnement> selected;
        public final Map<String, LifecycleDecision> decisions;
        public final double monthlyCost;
        public final double monthlyScore;

        public MonthlyOptimizationResult(
            List<Abonnement> selected,
            Map<String, LifecycleDecision> decisions,
            double monthlyCost,
            double monthlyScore
        ) {
            this.selected = selected;
            this.decisions = decisions;
            this.monthlyCost = monthlyCost;
            this.monthlyScore = monthlyScore;
        }
    }
}
