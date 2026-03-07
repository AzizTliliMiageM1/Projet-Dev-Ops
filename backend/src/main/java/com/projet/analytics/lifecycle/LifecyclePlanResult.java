package com.projet.analytics.lifecycle;

/**
 * Résultat de la planification du cycle de vie des abonnements.
 * 
 * Encapsule:
 * - Le plan global
 * - Métadonnées (temps exécution, algorithme, version)
 * - Statistiques d'optimisation
 */
public class LifecyclePlanResult {
    private final SubscriptionLifecyclePlan plan;
    private final long executionTimeMs;
    private final boolean success;
    private final String message;

    public LifecyclePlanResult(
        SubscriptionLifecyclePlan plan,
        long executionTimeMs,
        boolean success,
        String message
    ) {
        this.plan = plan;
        this.executionTimeMs = executionTimeMs;
        this.success = success;
        this.message = message;
    }

    public SubscriptionLifecyclePlan getPlan() {
        return plan;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    /**
     * Factory pour création rapide d'un échec.
     */
    public static LifecyclePlanResult failure(String message) {
        return new LifecyclePlanResult(null, 0, false, message);
    }

    /**
     * Factory pour création rapide d'un succès.
     */
    public static LifecyclePlanResult success(SubscriptionLifecyclePlan plan, long executionTimeMs) {
        return new LifecyclePlanResult(plan, executionTimeMs, true, "Plan generated successfully");
    }
}
