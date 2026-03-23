package com.projet.analytics.lifecycle;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Tests de l'évaluation des plans mensuels")
class PlanEvaluatorTest {

    private PlanEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new PlanEvaluator();
    }

    @Test
    @DisplayName("Le score global doit être compris entre 0 et 100")
    void shouldReturnScoreBetween0And100() {
        List<MonthlyPlan> plans = createSamplePlans();

        double score = evaluator.evaluate(plans);

        assertTrue(
            score >= 0 && score <= 100,
            String.format("Le score %.2f doit être compris entre 0 et 100", score)
        );
    }

    @Test
    @DisplayName("Une liste vide doit retourner un score nul")
    void shouldReturnZeroForEmptyPlanList() {
        double score = evaluator.evaluate(List.of());

        assertEquals(0.0, score, 1e-6);
    }

    @Test
    @DisplayName("Un seul plan doit retourner son propre score")
    void shouldReturnSinglePlanScore() {
        List<MonthlyPlan> plans = List.of(
            createMonthlyPlan("January", 25.0, 75.0)
        );

        double score = evaluator.evaluate(plans);

        assertEquals(75.0, score, 1e-6);
    }

    @Test
    @DisplayName("Le coût total doit correspondre à la somme des coûts mensuels")
    void shouldCalculateTotalCostCorrectly() {
        List<MonthlyPlan> plans = createSamplePlans();

        double totalCost = evaluator.calculateTotalCost(plans);

        assertEquals(73.0, totalCost, 1e-6);
    }

    @Test
    @DisplayName("Une liste vide doit retourner un coût total nul")
    void shouldReturnZeroTotalCostForEmptyList() {
        double totalCost = evaluator.calculateTotalCost(List.of());

        assertEquals(0.0, totalCost, 1e-6);
    }

    @Test
    @DisplayName("L'efficacité budgétaire doit être calculée comme score total / coût total")
    void shouldCalculateBudgetEfficiency() {
        List<MonthlyPlan> plans = List.of(
            createMonthlyPlan("January", 20.0, 80.0)
        );

        double efficiency = evaluator.calculateBudgetEfficiency(plans);

        assertEquals(4.0, efficiency, 1e-6);
    }

    @Test
    @DisplayName("Une efficacité budgétaire avec score nul doit retourner 0")
    void shouldReturnZeroEfficiencyWhenScoreIsZero() {
        List<MonthlyPlan> plans = List.of(
            createMonthlyPlan("January", 20.0, 0.0)
        );

        double efficiency = evaluator.calculateBudgetEfficiency(plans);

        assertEquals(0.0, efficiency, 1e-6);
    }

    @Test
    @DisplayName("Les méthodes doivent retourner des valeurs cohérentes sur un même jeu de données")
    void shouldReturnConsistentMetrics() {
        List<MonthlyPlan> plans = createSamplePlans();

        double score = evaluator.evaluate(plans);
        double totalCost = evaluator.calculateTotalCost(plans);
        double efficiency = evaluator.calculateBudgetEfficiency(plans);

        assertAll(
            () -> assertTrue(score >= 0 && score <= 100, "Le score doit rester dans l'intervalle [0,100]"),
            () -> assertEquals(73.0, totalCost, 1e-6, "Le coût total calculé est incorrect"),
            () -> assertTrue(efficiency > 0, "L'efficacité budgétaire doit être positive")
        );
    }

    // =========================
    // Helpers - Data builders
    // =========================

    private List<MonthlyPlan> createSamplePlans() {
        return List.of(
            createMonthlyPlan("January", 25.0, 65.5),
            createMonthlyPlan("February", 28.0, 70.0),
            createMonthlyPlan("March", 20.0, 80.0)
        );
    }

    private MonthlyPlan createMonthlyPlan(String month, double cost, double score) {
        return new MonthlyPlan(
            month,
            List.of("Netflix"),
            createDecisions(),
            cost,
            score
        );
    }

    private Map<String, LifecycleDecision> createDecisions() {
        Map<String, LifecycleDecision> decisions = new LinkedHashMap<>();
        decisions.put("Netflix", LifecycleDecision.KEEP);
        decisions.put("Spotify", LifecycleDecision.PAUSE);
        return decisions;
    }
}