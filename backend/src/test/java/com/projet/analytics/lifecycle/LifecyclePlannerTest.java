package com.projet.analytics.lifecycle;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.projet.backend.domain.Abonnement;

@DisplayName("Tests du planificateur de cycle de vie des abonnements")
class LifecyclePlannerTest {

    private LifecyclePlanner planner;
    private List<Abonnement> subscriptions;

    @BeforeEach
    void setUp() {
        planner = new LifecyclePlanner();
        subscriptions = createTestSubscriptions();
    }

    @Test
    @DisplayName("Doit générer un plan valide sur 6 mois")
    void shouldGenerateValidPlanForSixMonths() {
        LifecyclePlanResult result = planner.generatePlan(subscriptions, 6, 40.0);

        assertSuccessfulResult(result);
        assertNotNull(result.getPlan());
        assertEquals(6, result.getPlan().getMonths());
        assertEquals(40.0, result.getPlan().getBudgetTarget());
    }

    @Test
    @DisplayName("Doit respecter la contrainte de budget mensuel")
    void shouldRespectBudgetConstraint() {
        double budgetMax = 30.0;

        LifecyclePlanResult result = planner.generatePlan(subscriptions, 3, budgetMax);

        assertSuccessfulResult(result);
        for (MonthlyPlan month : result.getPlan().getMonthlyPlans()) {
            assertTrue(
                month.getMonthlyCost() <= budgetMax + 1e-6,
                String.format(
                    "Le mois %s dépasse le budget : %.2f > %.2f",
                    month.getMonth(),
                    month.getMonthlyCost(),
                    budgetMax
                )
            );
        }
    }

    @Test
    @DisplayName("Doit retourner un score global strictement positif")
    void shouldReturnPositiveObjectiveScore() {
        LifecyclePlanResult result = planner.generatePlan(subscriptions, 6, 50.0);

        assertSuccessfulResult(result);
        double score = result.getPlan().getGlobalObjectiveScore();

        assertTrue(score > 0, "Le score global doit être positif");
        assertTrue(score <= 100, "Le score global doit être inférieur ou égal à 100");
    }

    @Test
    @DisplayName("Doit retourner un coût total cohérent")
    void shouldReturnValidTotalCost() {
        LifecyclePlanResult result = planner.generatePlan(subscriptions, 2, 40.0);

        assertSuccessfulResult(result);
        double expectedMaxCost = 2 * 40.0;

        assertTrue(
            result.getPlan().getTotalCost() <= expectedMaxCost,
            "Le coût total ne doit pas dépasser le budget global maximal"
        );
    }

    @Test
    @DisplayName("Doit échouer si la liste des abonnements est vide")
    void shouldFailWithEmptySubscriptions() {
        LifecyclePlanResult result = planner.generatePlan(List.of(), 6, 40.0);

        assertFailedResult(result);
        assertTrue(result.getMessage().contains("No subscriptions"));
    }

    @Test
    @DisplayName("Doit échouer si le budget est négatif")
    void shouldFailWithNegativeBudget() {
        LifecyclePlanResult result = planner.generatePlan(subscriptions, 6, -10.0);

        assertFailedResult(result);
        assertTrue(result.getMessage().contains("negative"));
    }

    @Test
    @DisplayName("Chaque mois doit contenir une décision pour chaque abonnement")
    void shouldGenerateDecisionsForEachSubscription() {
        LifecyclePlanResult result = planner.generatePlan(subscriptions, 3, 40.0);

        assertSuccessfulResult(result);
        List<String> expectedNames = getSubscriptionNames(subscriptions);

        for (MonthlyPlan month : result.getPlan().getMonthlyPlans()) {
            assertNotNull(month.getDecisions());
            assertFalse(month.getDecisions().isEmpty());

            for (String subName : expectedNames) {
                assertTrue(
                    month.getDecisions().containsKey(subName),
                    "La décision pour l'abonnement " + subName + " est absente"
                );
            }
        }
    }

    @Test
    @DisplayName("Les noms des abonnements actifs doivent être préservés")
    void shouldPreserveSubscriptionNames() {
        LifecyclePlanResult result = planner.generatePlan(subscriptions, 2, 40.0);

        assertSuccessfulResult(result);
        List<String> expectedNames = getSubscriptionNames(subscriptions);

        for (MonthlyPlan month : result.getPlan().getMonthlyPlans()) {
            for (String activeName : month.getActiveSubscriptions()) {
                assertTrue(
                    expectedNames.contains(activeName),
                    "Nom inattendu trouvé dans les abonnements actifs : " + activeName
                );
            }
        }
    }

    // =========================
    // Helpers - Assertions
    // =========================

    private void assertSuccessfulResult(LifecyclePlanResult result) {
        assertNotNull(result, "Le résultat ne doit pas être null");
        assertTrue(result.isSuccess(), "Le résultat devrait être un succès");
        assertNotNull(result.getPlan(), "Le plan ne doit pas être null");
    }

    private void assertFailedResult(LifecyclePlanResult result) {
        assertNotNull(result, "Le résultat ne doit pas être null");
        assertFalse(result.isSuccess(), "Le résultat devrait être un échec");
        assertNotNull(result.getMessage(), "Le message d'erreur ne doit pas être null");
    }

    private List<String> getSubscriptionNames(List<Abonnement> abonnements) {
        return abonnements.stream()
            .map(Abonnement::getNomService)
            .toList();
    }

    // =========================
    // Helpers - Data builders
    // =========================

    private List<Abonnement> createTestSubscriptions() {
        Map<String, Double> subscriptionData = new LinkedHashMap<>();
        subscriptionData.put("Netflix", 15.0);
        subscriptionData.put("Spotify", 11.0);
        subscriptionData.put("Disney+", 9.0);
        subscriptionData.put("Canal+", 25.0);

        return subscriptionData.entrySet().stream()
            .map(entry -> createSubscription(entry.getKey(), entry.getValue()))
            .toList();
    }

    private Abonnement createSubscription(String name, double price) {
        LocalDate today = LocalDate.of(2026, 3, 23);

        Abonnement abo = new Abonnement(
            name,
            today.minusMonths(3),
            today.plusMonths(9),
            price,
            "TestUser",
            today.minusDays(5),
            "Entertainment"
        );

        abo.setPriorite("Important");
        return abo;
    }
}