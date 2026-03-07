package com.projet.analytics.optimization;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import com.projet.analytics.SubscriptionAnalytics;
import com.projet.backend.domain.Abonnement;

/**
 * Implémente l'algorithme glouton de rééquilibrage du portefeuille.
 */
public final class PortfolioRebalancer {

    private static final double OPTIMIZE_THRESHOLD = 60.0;
    private static final double CANCEL_THRESHOLD = 40.0;

    public RebalanceResult rebalance(List<Abonnement> abonnements, OptimizationConstraint constraint) {
        if (abonnements == null || abonnements.isEmpty()) {
            return new RebalanceResult(List.of(), List.of(), List.of(), 0.0, 0.0);
        }

        List<ScoredSubscription> scored = scoreSubscriptions(abonnements, constraint);
        if (scored.isEmpty()) {
            return new RebalanceResult(List.of(), List.of(), List.of(), 0.0, 0.0);
        }

        double budgetMax = constraint.budgetMax();
        List<Abonnement> kept = new ArrayList<>();
        List<Abonnement> optimized = new ArrayList<>();
        List<Abonnement> cancelled = new ArrayList<>();
        List<Double> keptRatios = new ArrayList<>();

        double currentCost = 0.0;
        ObjectiveFunction objective = new ObjectiveFunction(constraint);

        for (ScoredSubscription candidate : scored) {
            double price = Math.max(0.0, candidate.subscription().getPrixMensuel());
            if (currentCost + price <= budgetMax) {
                kept.add(candidate.subscription());
                currentCost += price;
                keptRatios.add(objective.normalizeToRatio(candidate.score()));
            } else {
                if (candidate.score() >= OPTIMIZE_THRESHOLD) {
                    optimized.add(candidate.subscription());
                } else if (candidate.score() >= CANCEL_THRESHOLD) {
                    optimized.add(candidate.subscription());
                } else {
                    cancelled.add(candidate.subscription());
                }
            }
        }

        double objectiveScore = keptRatios.isEmpty() ? 0.0 : keptRatios.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

        return new RebalanceResult(kept, optimized, cancelled, currentCost, objectiveScore);
    }

    private List<ScoredSubscription> scoreSubscriptions(List<Abonnement> abonnements, OptimizationConstraint constraint) {
        ObjectiveFunction objective = new ObjectiveFunction(constraint);
        List<ScoredSubscription> scored = new ArrayList<>();
        for (Abonnement abonnement : abonnements) {
            if (abonnement == null || !abonnement.estActif()) {
                continue;
            }
            SubscriptionScore score = buildScore(abonnement);
            double objectiveScore = objective.computeScore(score);
            scored.add(new ScoredSubscription(abonnement, score, objectiveScore));
        }
        scored.sort(Comparator.comparingDouble(ScoredSubscription::score).reversed());
        return scored;
    }

    private SubscriptionScore buildScore(Abonnement abonnement) {
        double value = SubscriptionAnalytics.calculateValueScore(abonnement);
        double risk = SubscriptionAnalytics.calculateChurnRisk(abonnement);
        double comfort = computeComfortScore(abonnement);
        return SubscriptionScore.of(value, risk, comfort);
    }

    private double computeComfortScore(Abonnement abonnement) {
        double comfort = 0.0;

        double usage = abonnement.getUsageFrequency();
        comfort += Math.min(usage * 5.0, 50.0);

        String priorite = abonnement.getPriorite();
        if (priorite != null) {
            switch (priorite.toLowerCase(Locale.ROOT)) {
                case "essentiel" -> comfort += 30.0;
                case "important" -> comfort += 20.0;
                case "optionnel" -> comfort += 10.0;
                default -> comfort += 5.0;
            }
        }

        if (abonnement.isPartage()) {
            comfort += 10.0;
        }

        if (hasComfortTag(abonnement)) {
            comfort += 10.0;
        }

        return Math.min(100.0, comfort);
    }

    private boolean hasComfortTag(Abonnement abonnement) {
        return abonnement.getTags().stream()
            .filter(Objects::nonNull)
            .map(tag -> tag.toLowerCase(Locale.ROOT))
            .anyMatch(tag -> tag.contains("confort") || tag.contains("comfort"));
    }

    private record ScoredSubscription(Abonnement subscription, SubscriptionScore scoreComponents, double score) {
    }
}
