package com.projet.analytics.anomaly;

import com.projet.backend.domain.Abonnement;
import java.util.List;
import java.util.Map;

/**
 * Represents a report of detected anomalies in subscriptions.
 */
public class AnomalyReport {

    private final List<Abonnement> duplicateSubscriptions;
    private final List<Abonnement> underutilizedSubscriptions;
    private final Map<String, List<Abonnement>> inconsistentSubscriptions;

    public AnomalyReport(List<Abonnement> duplicateSubscriptions, List<Abonnement> underutilizedSubscriptions, Map<String, List<Abonnement>> inconsistentSubscriptions) {
        this.duplicateSubscriptions = duplicateSubscriptions;
        this.underutilizedSubscriptions = underutilizedSubscriptions;
        this.inconsistentSubscriptions = inconsistentSubscriptions;
    }

    // Getters
    public List<Abonnement> getDuplicateSubscriptions() {
        return duplicateSubscriptions;
    }

    public List<Abonnement> getUnderutilizedSubscriptions() {
        return underutilizedSubscriptions;
    }

    public Map<String, List<Abonnement>> getInconsistentSubscriptions() {
        return inconsistentSubscriptions;
    }
}
