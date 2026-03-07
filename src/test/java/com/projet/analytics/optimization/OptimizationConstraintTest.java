package com.projet.analytics.optimization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class OptimizationConstraintTest {

    @Test
    void normalizesWeightsProportionally() {
        OptimizationConstraint constraint = OptimizationConstraint.of(120.0, 2.0, 1.0, 1.0);

        assertEquals(0.5, constraint.normalizedValueWeight(), 1e-6);
        assertEquals(0.25, constraint.normalizedRiskWeight(), 1e-6);
        assertEquals(0.25, constraint.normalizedComfortWeight(), 1e-6);
        assertEquals(120.0, constraint.budgetMax());
    }

    @Test
    void throwsWhenAllWeightsZero() {
        assertThrows(IllegalArgumentException.class, () -> OptimizationConstraint.of(50.0, 0.0, 0.0, 0.0));
    }

    @Test
    void ignoresNegativeInputsByClampingToZero() {
        OptimizationConstraint constraint = OptimizationConstraint.of(80.0, -1.0, 3.0, -2.0);

        assertEquals(0.0, constraint.valueWeight());
        assertEquals(3.0, constraint.riskWeight());
        assertEquals(0.0, constraint.comfortWeight());
        assertEquals(1.0, constraint.normalizedRiskWeight(), 1e-6);
    }
}
