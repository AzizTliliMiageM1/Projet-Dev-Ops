package com.projet.analytics.lifecycle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.projet.backend.domain.Abonnement;
import com.projet.analytics.lifecycle.MonthlyOptimizer.MonthlyOptimizationResult;

public class MonthlyOptimizerTest {

    private final MonthlyOptimizer optimizer = new MonthlyOptimizer();

    @Test
    void optimize_respectsBudgetConstraint() {
        List<Abonnement> subscriptions = List.of(
            createSub("Netflix", 15.0),
            createSub("Spotify", 11.0),
            createSub("Disney+", 9.0)
        );

        MonthlyOptimizationResult result = optimizer.optimize(subscriptions, 20.0, 0);

        assertTrue(result.monthlyCost <= 20.0 + 1e-6);
        assertNotNull(result.selected);
    }

    @Test
    void optimize_selectsHighUtilitySubscriptions() {
        Abonnement netflix = createSub("Netflix", 15.0);
        Abonnement spotify = createSub("Spotify", 11.0);

        // Netflix est utilisé récemment (plus utile)
        netflix.setDerniereUtilisation(LocalDate.now().minusDays(2));
        spotify.setDerniereUtilisation(LocalDate.now().minusDays(90)); // Dormant

        List<Abonnement> subscriptions = List.of(netflix, spotify);

        MonthlyOptimizationResult result = optimizer.optimize(subscriptions, 30.0, 0);

        // Netflix devrait être sélectionné car plus utile
        assertTrue(result.selected.stream()
            .map(Abonnement::getNomService)
            .anyMatch(name -> name.equals("Netflix")));
    }

    @Test
    void optimize_createsDecisionsForAllSubscriptions() {
        List<Abonnement> subscriptions = List.of(
            createSub("Netflix", 15.0),
            createSub("Spotify", 11.0),
            createSub("Disney+", 9.0)
        );

        MonthlyOptimizationResult result = optimizer.optimize(subscriptions, 25.0, 0);

        // Devrait avoir une décision pour chaque abonnement
        assertEquals(3, result.decisions.size());

        // Chaque décision doit être KEEP ou PAUSE
        for (LifecycleDecision decision : result.decisions.values()) {
            assertTrue(decision == LifecycleDecision.KEEP || decision == LifecycleDecision.PAUSE);
        }

        // Le nombre de KEEP devrait correspondre aux sélectionnés
        long keepCount = result.decisions.values().stream()
            .filter(d -> d == LifecycleDecision.KEEP)
            .count();
        assertEquals(result.selected.size(), keepCount);
    }

    @Test
    void optimize_withZeroBudgetSelectsNothing() {
        List<Abonnement> subscriptions = List.of(
            createSub("Netflix", 15.0),
            createSub("Spotify", 11.0)
        );

        MonthlyOptimizationResult result = optimizer.optimize(subscriptions, 0.0, 0);

        assertEquals(0, result.selected.size());
        assertEquals(0.0, result.monthlyCost);
    }

    @Test
    void optimize_withHighBudgetSelectsAll() {
        List<Abonnement> subscriptions = List.of(
            createSub("Netflix", 15.0),
            createSub("Spotify", 11.0),
            createSub("Disney+", 9.0)
        );

        MonthlyOptimizationResult result = optimizer.optimize(subscriptions, 100.0, 0);

        // Avec budget suffisant, tous devraient être sélectionnés
        assertEquals(3, result.selected.size());
    }

    @Test
    void optimize_withEmptyListReturnsEmpty() {
        MonthlyOptimizationResult result = optimizer.optimize(List.of(), 50.0, 0);

        assertEquals(0, result.selected.size());
        assertEquals(0.0, result.monthlyCost);
        assertTrue(result.decisions.isEmpty());
    }

    @Test
    void optimize_monthIndexAffectsSelection() {
        List<Abonnement> subscriptions = List.of(
            createSub("Netflix", 15.0),
            createSub("Spotify", 11.0)
        );

        // Optimiser pour le mois 0 (courant)
        MonthlyOptimizationResult result0 = optimizer.optimize(subscriptions, 30.0, 0);

        // Optimiser pour le mois 3 (futur - utilisation prédite décroît)
        MonthlyOptimizationResult result3 = optimizer.optimize(subscriptions, 30.0, 3);

        // Les résultats peuvent différer due à la décroissance d'utilisation prédite
        // Voici juste un test que les deux exécutions se terminent correctement
        assertNotNull(result0);
        assertNotNull(result3);
    }

    // ===== Helpers =====

    private Abonnement createSub(String name, double price) {
        Abonnement abo = new Abonnement(
            name,
            LocalDate.now().minusMonths(3),
            LocalDate.now().plusMonths(9),
            price,
            "TestUser",
            LocalDate.now().minusDays(5),
            "Entertainment"
        );
        abo.setPriorite("Important");
        return abo;
    }
}
