package com.projet.analytics.optimization;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Tests de la contrainte d'optimisation")
class OptimizationConstraintTest {

    @Test
    @DisplayName("Doit normaliser les poids proportionnellement")
    void shouldNormalizeWeightsProportionally() {
        OptimizationConstraint constraint =
                OptimizationConstraint.of(120.0, 2.0, 1.0, 1.0);

        assertAll(
            () -> assertEquals(0.5, constraint.normalizedValueWeight(), 1e-6),
            () -> assertEquals(0.25, constraint.normalizedRiskWeight(), 1e-6),
            () -> assertEquals(0.25, constraint.normalizedComfortWeight(), 1e-6),
            () -> assertEquals(120.0, constraint.budgetMax(), 1e-6)
        );
    }

    @Test
    @DisplayName("Doit lever une exception si tous les poids valent zéro")
    void shouldThrowWhenAllWeightsAreZero() {
        assertThrows(
            IllegalArgumentException.class,
            () -> OptimizationConstraint.of(50.0, 0.0, 0.0, 0.0)
        );
    }

    @Test
    @DisplayName("Doit borner les poids négatifs à zéro")
    void shouldClampNegativeInputsToZero() {
        OptimizationConstraint constraint =
                OptimizationConstraint.of(80.0, -1.0, 3.0, -2.0);

        assertAll(
            () -> assertEquals(0.0, constraint.valueWeight(), 1e-6),
            () -> assertEquals(3.0, constraint.riskWeight(), 1e-6),
            () -> assertEquals(0.0, constraint.comfortWeight(), 1e-6),
            () -> assertEquals(1.0, constraint.normalizedRiskWeight(), 1e-6)
        );
    }
}