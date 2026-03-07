package com.projet.analytics.optimization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class ObjectiveFunctionTest {

    @Test
    void combineScoresAccordingToWeights() {
        OptimizationConstraint constraint = OptimizationConstraint.of(100.0, 4.0, 3.0, 3.0);
        ObjectiveFunction objective = new ObjectiveFunction(constraint);

        SubscriptionScore components = SubscriptionScore.of(80.0, 20.0, 60.0);
        double score = objective.computeScore(components);

        // Normalized weights: value=0.4, risk=0.3, comfort=0.3
        double expected = (0.4 * 80.0) - (0.3 * 20.0) + (0.3 * 60.0);
        assertEquals(expected, score, 1e-6);
    }

    @Test
    void clampsScoreWithinBounds() {
        OptimizationConstraint constraint = OptimizationConstraint.of(100.0, 1.0, 0.0, 0.0);
        ObjectiveFunction objective = new ObjectiveFunction(constraint);

        SubscriptionScore veryHigh = SubscriptionScore.of(500.0, 0.0, 0.0);
        assertEquals(100.0, objective.computeScore(veryHigh), 1e-6);

        SubscriptionScore veryLow = SubscriptionScore.of(0.0, 500.0, 0.0);
        assertEquals(0.0, objective.computeScore(veryLow), 1e-6);
    }

    @Test
    void normalizeToRatioReturnsValueBetweenZeroAndOne() {
        OptimizationConstraint constraint = OptimizationConstraint.of(100.0, 2.0, 1.0, 1.0);
        ObjectiveFunction objective = new ObjectiveFunction(constraint);
        SubscriptionScore components = SubscriptionScore.of(70.0, 10.0, 30.0);

        double score = objective.computeScore(components);
        double ratio = objective.normalizeToRatio(score);

        assertEquals(score / 100.0, ratio, 1e-6);
    }
}
