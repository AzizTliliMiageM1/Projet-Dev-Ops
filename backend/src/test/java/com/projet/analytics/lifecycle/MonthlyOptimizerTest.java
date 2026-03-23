package com.projet.analytics.lifecycle;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.projet.analytics.lifecycle.MonthlyOptimizer.MonthlyOptimizationResult;
import com.projet.backend.domain.Abonnement;

@DisplayName("Tests de l'optimiseur mensuel des abonnements")
class MonthlyOptimizerTest {

    private static final LocalDate FIXED_DATE = LocalDate.of(2026, 3, 23);

    private MonthlyOptimizer optimizer;

    @BeforeEach
    void setUp() {
        optimizer = new MonthlyOptimizer();
    }

    @Test
    @DisplayName("Doit respecter la contrainte de budget")
    void shouldRespectBudgetConstraint() {
        List<Abonnement> subscriptions = createSubscriptions(
            subscription("Netflix", 15.0),
            subscription("Spotify", 11.0),
            subscription("Disney+", 9.0)
        );

        MonthlyOptimizationResult result = optimizer.optimize(subscriptions, 20.0, 0);

        assertValidResult(result);
        assertTrue(result.monthlyCost <= 20.0 + 1e-6, "Le coût mensuel dépasse le budget");
        assertNotNull(result.selected, "La liste des abonnements sélectionnés ne doit pas être null");
    }

    @Test
    @DisplayName("Doit sélectionner les abonnements à plus forte utilité")
    void shouldSelectHighUtilitySubscriptions() {
        Abonnement netflix = createSubscription("Netflix", 15.0, FIXED_DATE.minusDays(2));
        Abonnement spotify = createSubscription("Spotify", 11.0, FIXED_DATE.minusDays(90));

        List<Abonnement> subscriptions = List.of(netflix, spotify);

        MonthlyOptimizationResult result = optimizer.optimize(subscriptions, 30.0, 0);

        assertValidResult(result);
        assertTrue(
            result.selected.stream()
                .map(Abonnement::getNomService)
                .anyMatch("Netflix"::equals),
            "Netflix devrait être sélectionné car il est plus utile"
        );
    }

    @Test
    @DisplayName("Doit créer une décision pour chaque abonnement")
    void shouldCreateDecisionsForAllSubscriptions() {
        List<Abonnement> subscriptions = createSubscriptions(
            subscription("Netflix", 15.0),
            subscription("Spotify", 11.0),
            subscription("Disney+", 9.0)
        );

        MonthlyOptimizationResult result = optimizer.optimize(subscriptions, 25.0, 0);

        assertValidResult(result);
        assertEquals(3, result.decisions.size(), "Il doit y avoir une décision pour chaque abonnement");

        for (LifecycleDecision decision : result.decisions.values()) {
            assertTrue(
                decision == LifecycleDecision.KEEP || decision == LifecycleDecision.PAUSE,
                "Chaque décision doit être KEEP ou PAUSE"
            );
        }

        long keepCount = result.decisions.values().stream()
            .filter(d -> d == LifecycleDecision.KEEP)
            .count();

        assertEquals(result.selected.size(), keepCount,
            "Le nombre de décisions KEEP doit correspondre au nombre d'abonnements sélectionnés");
    }

    @Test
    @DisplayName("Avec un budget nul, ne doit rien sélectionner")
    void shouldSelectNothingWithZeroBudget() {
        List<Abonnement> subscriptions = createSubscriptions(
            subscription("Netflix", 15.0),
            subscription("Spotify", 11.0)
        );

        MonthlyOptimizationResult result = optimizer.optimize(subscriptions, 0.0, 0);

        assertValidResult(result);
        assertEquals(0, result.selected.size(), "Aucun abonnement ne doit être sélectionné");
        assertEquals(0.0, result.monthlyCost, 1e-6, "Le coût mensuel doit être nul");
    }

    @Test
    @DisplayName("Avec un budget élevé, doit sélectionner tous les abonnements")
    void shouldSelectAllWithHighBudget() {
        List<Abonnement> subscriptions = createSubscriptions(
            subscription("Netflix", 15.0),
            subscription("Spotify", 11.0),
            subscription("Disney+", 9.0)
        );

        MonthlyOptimizationResult result = optimizer.optimize(subscriptions, 100.0, 0);

        assertValidResult(result);
        assertEquals(3, result.selected.size(), "Tous les abonnements devraient être sélectionnés");
    }

    @Test
    @DisplayName("Avec une liste vide, doit retourner un résultat vide")
    void shouldReturnEmptyResultWithEmptyList() {
        MonthlyOptimizationResult result = optimizer.optimize(List.of(), 50.0, 0);

        assertValidResult(result);
        assertEquals(0, result.selected.size(), "Aucun abonnement ne doit être sélectionné");
        assertEquals(0.0, result.monthlyCost, 1e-6, "Le coût mensuel doit être nul");
        assertTrue(result.decisions.isEmpty(), "La map des décisions doit être vide");
    }

    @Test
    @DisplayName("Le mois cible ne doit pas casser l'optimisation")
    void shouldHandleDifferentMonthIndexes() {
        List<Abonnement> subscriptions = createSubscriptions(
            subscription("Netflix", 15.0),
            subscription("Spotify", 11.0)
        );

        MonthlyOptimizationResult resultMonth0 = optimizer.optimize(subscriptions, 30.0, 0);
        MonthlyOptimizationResult resultMonth3 = optimizer.optimize(subscriptions, 30.0, 3);

        assertValidResult(resultMonth0);
        assertValidResult(resultMonth3);
    }

    // =========================
    // Helpers - Assertions
    // =========================

    private void assertValidResult(MonthlyOptimizationResult result) {
        assertNotNull(result, "Le résultat ne doit pas être null");
        assertNotNull(result.selected, "La liste selected ne doit pas être null");
        assertNotNull(result.decisions, "La map decisions ne doit pas être null");
    }

    // =========================
    // Helpers - Data builders
    // =========================

    private List<Abonnement> createSubscriptions(Abonnement... abonnements) {
        return List.of(abonnements);
    }

    private Abonnement subscription(String name, double price) {
        return createSubscription(name, price, FIXED_DATE.minusDays(5));
    }

    private Abonnement createSubscription(String name, double price, LocalDate derniereUtilisation) {
        Abonnement abo = new Abonnement(
            name,
            FIXED_DATE.minusMonths(3),
            FIXED_DATE.plusMonths(9),
            price,
            "TestUser",
            derniereUtilisation,
            "Entertainment"
        );
        abo.setPriorite("Important");
        return abo;
    }
}