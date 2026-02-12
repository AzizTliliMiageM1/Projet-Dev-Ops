package com.projet.analytics.anomaly;

import com.projet.backend.domain.Abonnement;
import java.util.List;

/**
 * Service for detecting anomalies in subscriptions.
 */
public interface AnomalyDetector {

    /**
     * Detects various anomalies in a list of subscriptions.
     *
     * @param abonnements The list of subscriptions to analyze.
     * @return An AnomalyReport containing details of any found anomalies.
     */
    AnomalyReport detectAnomalies(List<Abonnement> abonnements);
}
