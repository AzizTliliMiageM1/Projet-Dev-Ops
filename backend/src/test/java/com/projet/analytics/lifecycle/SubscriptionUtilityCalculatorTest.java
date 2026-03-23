package com.projet.analytics.lifecycle;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.projet.backend.domain.Abonnement;

@DisplayName("Tests du calculateur d'utilité des abonnements")
class SubscriptionUtilityCalculatorTest {

    private static final LocalDate FIXED_DATE = LocalDate.of(2026, 3, 23);

    private SubscriptionUtilityCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new SubscriptionUtilityCalculator();
    }

    @Test
    @DisplayName("Un abonnement récemment utilisé doit avoir une utilité plus élevée qu'un abonnement dormant")
    void shouldGiveHigherUtilityToRecentlyUsedSubscription() {
        Abonnement recent = createSubscription("Recent", 15.0, FIXED_DATE.minusDays(2));
        Abonnement dormant = createSubscription("Dormant", 15.0, FIXED_DATE.minusDays(120));

        double recentUtility = calculator.calculateUtility(recent, 0, 25.0);
        double dormantUtility = calculator.calculateUtility(dormant, 0, 25.0);

        assertAll(
            () -> assertValidUtility(recentUtility),
            () -> assertValidUtility(dormantUtility),
            () -> assertTrue(
                recentUtility > dormantUtility,
                String.format(
                    "Recent (%.2f) should be higher than dormant (%.2f)",
                    recentUtility, dormantUtility
                )
            )
        );
    }

    @Test
    @DisplayName("Un abonnement moins cher doit avoir une utilité plus élevée qu'un abonnement cher à usage égal")
    void shouldGiveHigherUtilityToCheaperSubscription() {
        Abonnement cheap = createSubscription("Cheap", 5.0, FIXED_DATE.minusDays(5));
        Abonnement expensive = createSubscription("Expensive", 50.0, FIXED_DATE.minusDays(5));

        double cheapUtility = calculator.calculateUtility(cheap, 0, 50.0);
        double expensiveUtility = calculator.calculateUtility(expensive, 0, 50.0);

        assertAll(
            () -> assertValidUtility(cheapUtility),
            () -> assertValidUtility(expensiveUtility),
            () -> assertTrue(
                cheapUtility > expensiveUtility,
                String.format(
                    "Cheap (%.2f) should be higher than expensive (%.2f)",
                    cheapUtility, expensiveUtility
                )
            )
        );
    }

    @Test
    @DisplayName("L'utilité doit décroître avec le temps")
    void shouldDecayUtilityOverMonths() {
        Abonnement subscription = createSubscription("Test", 15.0, FIXED_DATE.minusDays(100));

        double month0 = calculator.calculateUtility(subscription, 0, 25.0);
        double month3 = calculator.calculateUtility(subscription, 3, 25.0);

        assertAll(
            () -> assertValidUtility(month0),
            () -> assertValidUtility(month3),
            () -> assertTrue(
                month0 > month3,
                String.format(
                    "Month 0 (%.2f) should be higher than month 3 (%.2f)",
                    month0, month3
                )
            )
        );
    }

    @Test
    @DisplayName("Le score d'utilité doit toujours être entre 0 et 100")
    void shouldReturnUtilityBetweenZeroAndOneHundred() {
        Abonnement subscription = createSubscription("Test", 15.0, FIXED_DATE.minusDays(5));

        double utility = calculator.calculateUtility(subscription, 0, 25.0);

        assertValidUtility(utility);
    }

    @Test
    @DisplayName("Un abonnement jamais utilisé doit avoir une faible utilité")
    void shouldReturnLowUtilityForNeverUsedSubscription() {
        Abonnement neverUsed = createSubscription("NeverUsed", 20.0, null);

        double utility = calculator.calculateUtility(neverUsed, 0, 25.0);

        assertAll(
            () -> assertValidUtility(utility),
            () -> assertTrue(
                utility < 50,
                String.format("Never used should have low utility, got %.2f", utility)
            )
        );
    }

    // =========================
    // Helpers - Assertions
    // =========================

    private void assertValidUtility(double utility) {
        assertTrue(
            utility >= 0 && utility <= 100,
            String.format("Utility %.2f should be between 0 and 100", utility)
        );
    }

    // =========================
    // Helpers - Data builders
    // =========================

    private Abonnement createSubscription(String name, double price, LocalDate lastUsed) {
        Abonnement abo = new Abonnement(
            name,
            FIXED_DATE.minusMonths(3),
            FIXED_DATE.plusMonths(9),
            price,
            "TestUser",
            lastUsed,
            "Entertainment"
        );
        abo.setPriorite("Important");
        return abo;
    }
}