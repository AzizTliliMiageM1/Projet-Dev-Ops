package com.projet.analytics.anomaly;

import com.projet.backend.domain.Abonnement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class AnomalyDetectorTest {

    private AnomalyDetector anomalyDetector;

    @BeforeEach
    void setUp() {
        anomalyDetector = new AnomalyDetectorImpl();
    }

    @Test
    void testDetectAnomalies() {
        Abonnement sub1 = new Abonnement("Netflix", LocalDate.now().minusMonths(1), LocalDate.now().plusYears(1), 15.0, "user1");
        Abonnement sub2 = new Abonnement("Netflix", LocalDate.now().minusMonths(1), LocalDate.now().plusYears(1), 15.0, "user1"); // Duplicate
        Abonnement sub3 = new Abonnement("Free Mobile", LocalDate.now().minusMonths(1), LocalDate.now().plusYears(1), 0.5, "user1"); // Underutilized
        Abonnement sub4 = new Abonnement("Old Service", LocalDate.now().minusYears(2), LocalDate.now().minusYears(1), 25.0, "user1"); 

        List<Abonnement> abonnements = Arrays.asList(sub1, sub2, sub3, sub4);

        AnomalyReport report = anomalyDetector.detectAnomalies(abonnements);

        assertNotNull(report);
        assertEquals(2, report.getDuplicateSubscriptions().size());
        assertEquals(1, report.getUnderutilizedSubscriptions().size());
        assertEquals(0, report.getInconsistentSubscriptions().get("InactiveWithFutureRenewal").size());
    }
}
