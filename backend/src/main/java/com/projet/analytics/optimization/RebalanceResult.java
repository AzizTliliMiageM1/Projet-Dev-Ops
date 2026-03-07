package com.projet.analytics.optimization;

import java.util.Collections;
import java.util.List;

import com.projet.backend.domain.Abonnement;

/**
 * Contient le résultat d'un rééquilibrage de portefeuille.
 */
public final class RebalanceResult {

    private final List<Abonnement> kept;
    private final List<Abonnement> optimized;
    private final List<Abonnement> cancelled;
    private final double finalMonthlyCost;
    private final double objectiveScore;

    public RebalanceResult(
        List<Abonnement> kept,
        List<Abonnement> optimized,
        List<Abonnement> cancelled,
        double finalMonthlyCost,
        double objectiveScore
    ) {
        this.kept = kept == null ? List.of() : List.copyOf(kept);
        this.optimized = optimized == null ? List.of() : List.copyOf(optimized);
        this.cancelled = cancelled == null ? List.of() : List.copyOf(cancelled);
        this.finalMonthlyCost = finalMonthlyCost;
        this.objectiveScore = objectiveScore;
    }

    public List<Abonnement> getKept() {
        return Collections.unmodifiableList(kept);
    }

    public List<Abonnement> getOptimized() {
        return Collections.unmodifiableList(optimized);
    }

    public List<Abonnement> getCancelled() {
        return Collections.unmodifiableList(cancelled);
    }

    public double getFinalMonthlyCost() {
        return finalMonthlyCost;
    }

    public double getObjectiveScore() {
        return objectiveScore;
    }
}
