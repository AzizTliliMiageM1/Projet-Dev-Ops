package com.projet.analytics.optimization;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Tests de la fonction objectif d'optimisation")
class ObjectiveFunctionTest {

    private ObjectiveFunction objective;

    @BeforeEach
    void setUp() {
        OptimizationConstraint constraint =
                OptimizationConstraint.of(100.0, 4.0, 3.0, 3.0);
        objective = new ObjectiveFunction(constraint);
    }

    @Test
    @DisplayName("Doit combiner correctement les scores selon les poids normalisés")
    void shouldCombineScoresAccordingToWeights() {

        SubscriptionScore components =
                SubscriptionScore.of(80.0, 20.0, 60.0);

        double score = objective.computeScore(components);

        double expected =
                (0.4 * 80.0)
              - (0.3 * 20.0)
              + (0.3 * 60.0);

        assertEquals(expected, score, 1e-6);
    }

    @Test
    @DisplayName("Doit borner le score entre 0 et le maximum autorisé")
    void shouldClampScoreWithinBounds() {

        OptimizationConstraint constraint =
                OptimizationConstraint.of(100.0, 1.0, 0.0, 0.0);

        ObjectiveFunction localObjective =
                new ObjectiveFunction(constraint);

        SubscriptionScore veryHigh =
                SubscriptionScore.of(500.0, 0.0, 0.0);

        SubscriptionScore veryLow =
                SubscriptionScore.of(0.0, 500.0, 0.0);

        assertAll(
            () -> assertEquals(
                    100.0,
                    localObjective.computeScore(veryHigh),
                    1e-6
            ),
            () -> assertEquals(
                    0.0,
                    localObjective.computeScore(veryLow),
                    1e-6
            )
        );
    }

    @Test
    @DisplayName("La normalisation doit retourner un ratio entre 0 et 1")
    void shouldNormalizeScoreToRatioBetweenZeroAndOne() {

        SubscriptionScore components =
                SubscriptionScore.of(70.0, 10.0, 30.0);

        double score = objective.computeScore(components);
        double ratio = objective.normalizeToRatio(score);

        assertAll(
            () -> assertEquals(score / 100.0, ratio, 1e-6),
            () -> assertTrue(ratio >= 0.0),
            () -> assertTrue(ratio <= 1.0)
        );
    }
}