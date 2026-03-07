package com.projet.analytics.lifecycle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class PlanEvaluatorTest {

    private final PlanEvaluator evaluator = new PlanEvaluator();

    @Test
    void evaluate_returnsScoreBetween0and100() {
        List<MonthlyPlan> plans = List.of(
            createMonthlyPlan("January", 25.0, 65.5),
            createMonthlyPlan("February", 28.0, 70.0),
            createMonthlyPlan("March", 20.0, 80.0)
        );

        double score = evaluator.evaluate(plans);

        assertTrue(score >= 0 && score <= 100,
            String.format("Score %.2f should be between 0 and 100", score));
    }

    @Test
    void evaluate_emptyListReturnsZero() {
        double score = evaluator.evaluate(List.of());

        assertEquals(0.0, score);
    }

    @Test
    void evaluate_singlePlanReturnsPlanScore() {
        List<MonthlyPlan> plans = List.of(
            createMonthlyPlan("January", 25.0, 75.0)
        );

        double score = evaluator.evaluate(plans);

        assertEquals(75.0, score);
    }

    @Test
    void calculateTotalCost_sumsMonthlyCosts() {
        List<MonthlyPlan> plans = List.of(
            createMonthlyPlan("January", 25.0, 65.5),
            createMonthlyPlan("February", 28.0, 70.0),
            createMonthlyPlan("March", 20.0, 80.0)
        );

        double totalCost = evaluator.calculateTotalCost(plans);

        assertEquals(73.0, totalCost, 1e-6);
    }

    @Test
    void calculateTotalCost_emptyListReturnsZero() {
        double totalCost = evaluator.calculateTotalCost(List.of());

        assertEquals(0.0, totalCost);
    }

    @Test
    void calculateBudgetEfficiency_valueOverCost() {
        List<MonthlyPlan> plans = List.of(
            createMonthlyPlan("January", 20.0, 80.0)
        );

        double efficiency = evaluator.calculateBudgetEfficiency(plans);

        // efficiency = 80 / 20 = 4.0
        assertEquals(4.0, efficiency, 1e-6);
    }

    @Test
    void calculateBudgetEfficiency_zeroScoreReturnsZero() {
        List<MonthlyPlan> plans = List.of(
            createMonthlyPlan("January", 20.0, 0.0)
        );

        double efficiency = evaluator.calculateBudgetEfficiency(plans);

        assertEquals(0.0, efficiency);
    }

    // ===== Helpers =====

    private MonthlyPlan createMonthlyPlan(String month, double cost, double score) {
        Map<String, LifecycleDecision> decisions = new HashMap<>();
        decisions.put("Netflix", LifecycleDecision.KEEP);
        decisions.put("Spotify", LifecycleDecision.PAUSE);

        return new MonthlyPlan(
            month,
            List.of("Netflix"),
            decisions,
            cost,
            score
        );
    }
}
