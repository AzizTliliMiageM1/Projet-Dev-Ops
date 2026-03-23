package com.projet.analytics.anomaly;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.projet.backend.domain.Abonnement;

class AnomalyDetectorTest {

    private AnomalyDetector anomalyDetector;

    @BeforeEach
    void setUp() {
        anomalyDetector = new AnomalyDetectorImpl();
    }

    @Test
    @DisplayName("Doit détecter les doublons, les abonnements sous-utilisés et vérifier les incohérences")
    void shouldDetectAnomaliesCorrectly() {
        // Arrange
        LocalDate fixedDate = LocalDate.of(2026, 3, 23);

        Abonnement duplicate1 = new Abonnement(
                "Netflix",
                fixedDate.minusMonths(1),
                fixedDate.plusYears(1),
                15.0,
                "user1"
        );

        Abonnement duplicate2 = new Abonnement(
                "Netflix",
                fixedDate.minusMonths(1),
                fixedDate.plusYears(1),
                15.0,
                "user1"
        );

        Abonnement underutilized = new Abonnement(
                "Free Mobile",
                fixedDate.minusMonths(1),
                fixedDate.plusYears(1),
                0.5,
                "user1"
        );

        Abonnement expired = new Abonnement(
                "Old Service",
                fixedDate.minusYears(2),
                fixedDate.minusYears(1),
                25.0,
                "user1"
        );

        List<Abonnement> abonnements = List.of(
                duplicate1,
                duplicate2,
                underutilized,
                expired
        );

        // Act
        AnomalyReport report = anomalyDetector.detectAnomalies(abonnements);

        // Assert
        assertAll("Vérification du rapport d'anomalies",
                () -> assertNotNull(report, "Le rapport ne doit pas être null"),
                () -> assertNotNull(report.getDuplicateSubscriptions(), "La liste des doublons ne doit pas être null"),
                () -> assertNotNull(report.getUnderutilizedSubscriptions(), "La liste des abonnements sous-utilisés ne doit pas être null"),
                () -> assertNotNull(report.getInconsistentSubscriptions(), "La map des incohérences ne doit pas être null"),

                () -> assertEquals(2, report.getDuplicateSubscriptions().size(),
                        "Il devrait y avoir 2 abonnements détectés comme doublons"),

                () -> assertEquals(1, report.getUnderutilizedSubscriptions().size(),
                        "Il devrait y avoir 1 abonnement sous-utilisé"),

                () -> assertTrue(
                        report.getUnderutilizedSubscriptions().stream()
                                .anyMatch(a -> "Free Mobile".equals(a.getNomService())),
                        "L'abonnement 'Free Mobile' devrait être marqué comme sous-utilisé"
                ),

                () -> assertTrue(
                        report.getInconsistentSubscriptions().containsKey("InactiveWithFutureRenewal"),
                        "La clé 'InactiveWithFutureRenewal' devrait exister dans les incohérences"
                ),

                () -> assertEquals(0,
                        report.getInconsistentSubscriptions().get("InactiveWithFutureRenewal").size(),
                        "Il ne devrait pas y avoir d'abonnement incohérent de type 'InactiveWithFutureRenewal'")
        );
    }
}