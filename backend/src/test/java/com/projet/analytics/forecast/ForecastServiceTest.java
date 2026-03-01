package com.projet.analytics.forecast;

import com.projet.backend.domain.Abonnement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ForecastServiceTest {

    private ForecastService forecastService;

    @BeforeEach
    void setUp() {
        forecastService = new ForecastServiceImpl();
    }

    @Test
    void testProjectCosts() {
        Abonnement sub1 = new Abonnement("Netflix", LocalDate.now().minusMonths(1), LocalDate.now().plusYears(1), 15.0, "user1");
        sub1.setFrequencePaiement("Mensuel");
        Abonnement sub2 = new Abonnement("Spotify", LocalDate.now().minusMonths(2), LocalDate.now().plusYears(1), 10.0, "user1");
        sub2.setFrequencePaiement("Mensuel");
        Abonnement sub3 = new Abonnement("Office 365", LocalDate.now().plusMonths(2), LocalDate.now().plusYears(1), 120.0, "user1");
        sub3.setFrequencePaiement("Annuel");

        List<Abonnement> abonnements = Arrays.asList(sub1, sub2, sub3);

        ForecastResult result = forecastService.projectCosts(abonnements, 6);

        assertNotNull(result);
        assertEquals(6, result.getMonths());
        assertFalse(result.getProjectedCosts().isEmpty());
        
        // More detailed assertions can be added here based on expected calculations
    }
}
