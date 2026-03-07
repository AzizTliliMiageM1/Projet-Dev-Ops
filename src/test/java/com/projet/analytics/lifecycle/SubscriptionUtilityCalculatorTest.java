package com.projet.analytics.lifecycle;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.projet.backend.domain.Abonnement;

public class SubscriptionUtilityCalculatorTest {

    private final SubscriptionUtilityCalculator calculator = new SubscriptionUtilityCalculator();

    @Test
    void calculateUtility_recentlyUsedHigherThanDormant() {
        Abonnement recent = createSub("Recent", 15.0, LocalDate.now().minusDays(2));
        Abonnement dormant = createSub("Dormant", 15.0, LocalDate.now().minusDays(120));

        double recentUtility = calculator.calculateUtility(recent, 0, 25.0);
        double dormantUtility = calculator.calculateUtility(dormant, 0, 25.0);

        assertTrue(recentUtility > dormantUtility,
            String.format("Recent (%.2f) should be higher than dormant (%.2f)",
                recentUtility, dormantUtility));
    }

    @Test
    void calculateUtility_cheaperHigherThanExpensive() {
        Abonnement cheap = createSub("Cheap", 5.0, LocalDate.now().minusDays(5));
        Abonnement expensive = createSub("Expensive", 50.0, LocalDate.now().minusDays(5));

        double cheapUtility = calculator.calculateUtility(cheap, 0, 50.0);
        double expensiveUtility = calculator.calculateUtility(expensive, 0, 50.0);

        assertTrue(cheapUtility > expensiveUtility,
            String.format("Cheap (%.2f) should be higher than expensive (%.2f)",
                cheapUtility, expensiveUtility));
    }

    @Test
    void calculateUtility_decaysOverMonths() {
        Abonnement subscription = createSub("Test", 15.0, LocalDate.now().minusDays(100));

        double month0 = calculator.calculateUtility(subscription, 0, 25.0);
        double month3 = calculator.calculateUtility(subscription, 3, 25.0);

        assertTrue(month0 > month3,
            String.format("Month 0 (%.2f) should be higher than month 3 (%.2f)",
                month0, month3));
    }

    @Test
    void calculateUtility_returnsValidScore() {
        Abonnement subscription = createSub("Test", 15.0, LocalDate.now().minusDays(5));

        double utility = calculator.calculateUtility(subscription, 0, 25.0);

        assertTrue(utility >= 0 && utility <= 100,
            String.format("Utility %.2f should be between 0 and 100", utility));
    }

    @Test
    void calculateUtility_neverUsedLow() {
        Abonnement neverUsed = new Abonnement(
            "NeverUsed",
            LocalDate.now().minusMonths(3),
            LocalDate.now().plusMonths(9),
            20.0,
            "TestUser",
            null, // Jamais utilisé
            "Entertainment"
        );

        double utility = calculator.calculateUtility(neverUsed, 0, 25.0);

        assertTrue(utility < 50, String.format("Never used should have low utility, got %.2f", utility));
    }

    // ===== Helpers =====

    private Abonnement createSub(String name, double price, LocalDate lastUsed) {
        Abonnement abo = new Abonnement(
            name,
            LocalDate.now().minusMonths(3),
            LocalDate.now().plusMonths(9),
            price,
            "TestUser",
            lastUsed,
            "Entertainment"
        );
        abo.setPriorite("Important");
        return abo;
    }
}
