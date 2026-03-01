package com.projet.analytics.optimization;

import com.projet.backend.domain.Abonnement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class SubscriptionOptimizationServiceImplTest {

    private SubscriptionOptimizationService service;

    @BeforeEach
    void setUp() {
        service = new SubscriptionOptimizationServiceImpl();
    }

    @Test
    void testCasAbonnementRentable() {
        Abonnement sub = new Abonnement("Netflix", LocalDate.now().minusMonths(6), LocalDate.now().plusMonths(6), 15.0, "UserA", LocalDate.now().minusDays(5), "Loisir");
        OptimizationResult result = service.analyze(List.of(sub));
        assertEquals(OptimizationAction.CONSERVER, result.getSuggestions().get(0).getAction());
    }

    @Test
    void testCasAbonnementInutile() {
        Abonnement sub = new Abonnement("Ancien VPN", LocalDate.now().minusYears(2), LocalDate.now().plusMonths(1), 10.0, "UserA", LocalDate.now().minusMonths(8), "Logiciel");
        OptimizationResult result = service.analyze(List.of(sub));
        assertEquals(OptimizationAction.RESILIER, result.getSuggestions().get(0).getAction());
    }

    @Test
    void testPortefeuilleMixte() {
        Abonnement sub1 = new Abonnement("Spotify", LocalDate.now().minusMonths(12), LocalDate.now().plusMonths(1), 9.99, "UserA", LocalDate.now().minusDays(1), "Musique");
        Abonnement sub2 = new Abonnement("Adobe", LocalDate.now().minusMonths(3), LocalDate.now().plusMonths(9), 50.0, "UserA", LocalDate.now().minusDays(40), "Logiciel");
        Abonnement sub3 = new Abonnement("Gym", LocalDate.now().minusMonths(5), LocalDate.now().plusMonths(7), 25.0, "UserA", LocalDate.now().minusDays(100), "Sport");

        OptimizationResult result = service.analyze(List.of(sub1, sub2, sub3));

        assertEquals(OptimizationAction.CONSERVER, findSuggestionFor(result, "Spotify").getAction());
        assertEquals(OptimizationAction.OPTIMISER, findSuggestionFor(result, "Adobe").getAction());
        assertEquals(OptimizationAction.RESILIER, findSuggestionFor(result, "Gym").getAction());
    }

    @Test
    void testCasLimiteAbonnementGratuitJamaisUtilise() {
        Abonnement sub = new Abonnement("Service Gratuit", LocalDate.now().minusYears(1), LocalDate.now().plusYears(1), 0.0, "UserA", null, "Utilitaire");
        sub.setDerniereUtilisation(null); // Explicitement null
        OptimizationResult result = service.analyze(List.of(sub));
        
        // Doit être conservé car gratuit, même si jamais utilisé. Le coût est le facteur dominant.
        assertEquals(OptimizationAction.CONSERVER, result.getSuggestions().get(0).getAction());
        assertTrue(result.getSuggestions().get(0).getScore() > SEUIL_OPTIMISER, "Le score doit être élevé pour un service gratuit");
    }
    
    private static final double SEUIL_OPTIMISER = 60;
    
    private OptimizationSuggestion findSuggestionFor(OptimizationResult result, String serviceName) {
        return result.getSuggestions().stream()
                .filter(s -> s.getAbonnement().getNomService().equals(serviceName))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Suggestion for " + serviceName + " not found"));
    }
}
