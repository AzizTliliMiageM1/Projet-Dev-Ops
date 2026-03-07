package com.projet.analytics.lifecycle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.projet.backend.domain.Abonnement;

public class LifecyclePlannerTest {

    private final LifecyclePlanner planner = new LifecyclePlanner();

    @Test
    void generatePlan_createsValidPlanFor6Months() {
        List<Abonnement> subscriptions = createTestSubscriptions();

        LifecyclePlanResult result = planner.generatePlan(subscriptions, 6, 40.0);

        assertTrue(result.isSuccess());
        assertNotNull(result.getPlan());
        assertEquals(6, result.getPlan().getMonths());
        assertEquals(40.0, result.getPlan().getBudgetTarget());
    }

    @Test
    void generatePlan_respectsBudgetConstraint() {
        List<Abonnement> subscriptions = createTestSubscriptions();
        double budgetMax = 30.0;

        LifecyclePlanResult result = planner.generatePlan(subscriptions, 3, budgetMax);

        assertTrue(result.isSuccess());
        for (MonthlyPlan month : result.getPlan().getMonthlyPlans()) {
            assertTrue(month.getMonthlyCost() <= budgetMax + 1e-6,
                String.format("Month %s exceeds budget: %.2f > %.2f",
                    month.getMonth(), month.getMonthlyCost(), budgetMax));
        }
    }

    @Test
    void generatePlan_returnsNonZeroObjectiveScore() {
        List<Abonnement> subscriptions = createTestSubscriptions();

        LifecyclePlanResult result = planner.generatePlan(subscriptions, 6, 50.0);

        assertTrue(result.isSuccess());
        assertTrue(result.getPlan().getGlobalObjectiveScore() > 0);
        assertTrue(result.getPlan().getGlobalObjectiveScore() <= 100);
    }

    @Test
    void generatePlan_returnsCorrectTotalCost() {
        List<Abonnement> subscriptions = createTestSubscriptions();

        LifecyclePlanResult result = planner.generatePlan(subscriptions, 2, 40.0);

        assertTrue(result.isSuccess());
        double expectedRange = 2 * 40.0; // Max possible cost
        assertTrue(result.getPlan().getTotalCost() <= expectedRange);
    }

    @Test
    void generatePlan_failsWithEmptySubscriptions() {
        LifecyclePlanResult result = planner.generatePlan(List.of(), 6, 40.0);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("No subscriptions"));
    }

    @Test
    void generatePlan_failsWithNegativeBudget() {
        List<Abonnement> subscriptions = createTestSubscriptions();

        LifecyclePlanResult result = planner.generatePlan(subscriptions, 6, -10.0);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("negative"));
    }

    @Test
    void generatePlan_monthlyPlansContainDecisions() {
        List<Abonnement> subscriptions = createTestSubscriptions();

        LifecyclePlanResult result = planner.generatePlan(subscriptions, 3, 40.0);

        assertTrue(result.isSuccess());
        for (MonthlyPlan month : result.getPlan().getMonthlyPlans()) {
            assertNotNull(month.getDecisions());
            assertFalse(month.getDecisions().isEmpty());
            // Chaque abonnement doit avoir une décision (KEEP ou PAUSE)
            for (String subName : subscriptions.stream()
                .map(Abonnement::getNomService)
                .toList()) {
                assertTrue(month.getDecisions().containsKey(subName));
            }
        }
    }

    @Test
    void generatePlan_preservesSubscriptionNames() {
        List<Abonnement> subscriptions = createTestSubscriptions();

        LifecyclePlanResult result = planner.generatePlan(subscriptions, 2, 40.0);

        assertTrue(result.isSuccess());
        List<String> expectedNames = subscriptions.stream()
            .map(Abonnement::getNomService)
            .toList();

        for (MonthlyPlan month : result.getPlan().getMonthlyPlans()) {
            for (String name : month.getActiveSubscriptions()) {
                assertTrue(expectedNames.contains(name));
            }
        }
    }

    // ===== Helpers =====

    private List<Abonnement> createTestSubscriptions() {
        Map<String, Double> subscriptions = new HashMap<>();
        subscriptions.put("Netflix", 15.0);
        subscriptions.put("Spotify", 11.0);
        subscriptions.put("Disney+", 9.0);
        subscriptions.put("Canal+", 25.0);

        return subscriptions.entrySet().stream()
            .map(entry -> createSubscription(entry.getKey(), entry.getValue()))
            .toList();
    }

    private Abonnement createSubscription(String name, double price) {
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
