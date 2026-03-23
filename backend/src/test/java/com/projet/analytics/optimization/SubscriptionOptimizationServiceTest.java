package com.projet.analytics.optimization;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.projet.backend.domain.Abonnement;

@DisplayName("Tests du service d'optimisation des abonnements")
class SubscriptionOptimizationServiceImplTest {

    private static final LocalDate FIXED_DATE = LocalDate.of(2026, 3, 23);
    private static final double SEUIL_OPTIMISER = 60.0;

    private SubscriptionOptimizationService service;

    @BeforeEach
    void setUp() {
        service = new SubscriptionOptimizationServiceImpl();
    }

    @Test
    @DisplayName("Doit conserver un abonnement rentable et récemment utilisé")
    void shouldKeepProfitableSubscription() {
        Abonnement sub = createSubscription(
            "Netflix",
            FIXED_DATE.minusMonths(6),
            FIXED_DATE.plusMonths(6),
            15.0,
            FIXED_DATE.minusDays(5),
            "Loisir"
        );

        OptimizationResult result = service.analyze(List.of(sub));
        OptimizationSuggestion suggestion = getFirstSuggestion(result);

        assertAll(
            () -> assertValidResult(result),
            () -> assertEquals(OptimizationAction.CONSERVER, suggestion.getAction())
        );
    }

    @Test
    @DisplayName("Doit résilier un abonnement ancien et inutilisé")
    void shouldCancelUnusedSubscription() {
        Abonnement sub = createSubscription(
            "Ancien VPN",
            FIXED_DATE.minusYears(2),
            FIXED_DATE.plusMonths(1),
            10.0,
            FIXED_DATE.minusMonths(8),
            "Logiciel"
        );

        OptimizationResult result = service.analyze(List.of(sub));
        OptimizationSuggestion suggestion = getFirstSuggestion(result);

        assertAll(
            () -> assertValidResult(result),
            () -> assertEquals(OptimizationAction.RESILIER, suggestion.getAction())
        );
    }

    @Test
    @DisplayName("Doit proposer des actions adaptées sur un portefeuille mixte")
    void shouldAnalyzeMixedPortfolioCorrectly() {
        Abonnement spotify = createSubscription(
            "Spotify",
            FIXED_DATE.minusMonths(12),
            FIXED_DATE.plusMonths(1),
            9.99,
            FIXED_DATE.minusDays(1),
            "Musique"
        );

        Abonnement adobe = createSubscription(
            "Adobe",
            FIXED_DATE.minusMonths(3),
            FIXED_DATE.plusMonths(9),
            50.0,
            FIXED_DATE.minusDays(40),
            "Logiciel"
        );

        Abonnement gym = createSubscription(
            "Gym",
            FIXED_DATE.minusMonths(5),
            FIXED_DATE.plusMonths(7),
            25.0,
            FIXED_DATE.minusDays(100),
            "Sport"
        );

        OptimizationResult result = service.analyze(List.of(spotify, adobe, gym));

        assertAll(
            () -> assertValidResult(result),
            () -> assertEquals(
                OptimizationAction.CONSERVER,
                findSuggestionFor(result, "Spotify").getAction()
            ),
            () -> assertEquals(
                OptimizationAction.OPTIMISER,
                findSuggestionFor(result, "Adobe").getAction()
            ),
            () -> assertEquals(
                OptimizationAction.RESILIER,
                findSuggestionFor(result, "Gym").getAction()
            )
        );
    }

    @Test
    @DisplayName("Doit conserver un abonnement gratuit même jamais utilisé")
    void shouldKeepFreeSubscriptionEvenIfNeverUsed() {
        Abonnement sub = createSubscription(
            "Service Gratuit",
            FIXED_DATE.minusYears(1),
            FIXED_DATE.plusYears(1),
            0.0,
            null,
            "Utilitaire"
        );

        OptimizationResult result = service.analyze(List.of(sub));
        OptimizationSuggestion suggestion = getFirstSuggestion(result);

        assertAll(
            () -> assertValidResult(result),
            () -> assertEquals(OptimizationAction.CONSERVER, suggestion.getAction()),
            () -> assertTrue(
                suggestion.getScore() > SEUIL_OPTIMISER,
                "Le score devrait être supérieur au seuil d'optimisation"
            )
        );
    }

    // =========================
    // Helpers - Assertions
    // =========================

    private void assertValidResult(OptimizationResult result) {
        assertNotNull(result, "Le résultat ne doit pas être null");
        assertNotNull(result.getSuggestions(), "La liste des suggestions ne doit pas être null");
        assertTrue(!result.getSuggestions().isEmpty(), "La liste des suggestions ne doit pas être vide");
    }

    private OptimizationSuggestion getFirstSuggestion(OptimizationResult result) {
        return result.getSuggestions().get(0);
    }

    // =========================
    // Helpers - Recherche
    // =========================

    private OptimizationSuggestion findSuggestionFor(OptimizationResult result, String serviceName) {
        return result.getSuggestions().stream()
            .filter(s -> s.getAbonnement().getNomService().equals(serviceName))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Suggestion for " + serviceName + " not found"));
    }

    // =========================
    // Helpers - Data builders
    // =========================

    private Abonnement createSubscription(
        String serviceName,
        LocalDate startDate,
        LocalDate endDate,
        double price,
        LocalDate lastUsed,
        String category
    ) {
        return new Abonnement(
            serviceName,
            startDate,
            endDate,
            price,
            "UserA",
            lastUsed,
            category
        );
    }
}