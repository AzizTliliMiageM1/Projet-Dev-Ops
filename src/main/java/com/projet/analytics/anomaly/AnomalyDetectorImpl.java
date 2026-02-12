package com.projet.analytics.anomaly;

import com.projet.backend.domain.Abonnement;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of the AnomalyDetector service.
 */
public class AnomalyDetectorImpl implements AnomalyDetector {

    private static final double UNDERUTILIZED_THRESHOLD = 1.0;

    @Override
    public AnomalyReport detectAnomalies(List<Abonnement> abonnements) {
        List<Abonnement> duplicates = detectDuplicates(abonnements);
        List<Abonnement> underutilized = detectUnderutilized(abonnements);
        Map<String, List<Abonnement>> inconsistencies = detectInconsistencies(abonnements);

        return new AnomalyReport(duplicates, underutilized, inconsistencies);
    }

    private List<Abonnement> detectDuplicates(List<Abonnement> abonnements) {
        return abonnements.stream()
                .collect(Collectors.groupingBy(sub -> sub.getNomService() + "#" + sub.getClientName()))
                .values().stream()
                .filter(list -> list.size() > 1)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    private List<Abonnement> detectUnderutilized(List<Abonnement> abonnements) {
        return abonnements.stream()
                .filter(sub -> sub.estActif())
                .filter(sub -> sub.getPrixMensuel() <= UNDERUTILIZED_THRESHOLD)
                .collect(Collectors.toList());
    }

    private Map<String, List<Abonnement>> detectInconsistencies(List<Abonnement> abonnements) {
        // Example inconsistency: inactive subscription with a future renewal date
        List<Abonnement> inactiveWithFutureRenewal = abonnements.stream()
                .filter(sub -> !sub.estActif() && sub.getDateFin() != null && sub.getDateFin().isAfter(LocalDate.now()))
                .collect(Collectors.toList());

        return Map.of("InactiveWithFutureRenewal", inactiveWithFutureRenewal);
    }
}
