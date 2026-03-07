package com.projet.analytics.optimization;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.projet.backend.domain.Abonnement;

class PortfolioRebalancerTest {

    private final PortfolioRebalancer rebalancer = new PortfolioRebalancer();

    @Test
    void respectsBudgetAndKeepsHighestScores() {
        Abonnement premium = createSubscription(
            "PremiumA", 3.0, 1, "Essentiel", true, List.of("Confort"));
        Abonnement workTool = createSubscription(
            "WorkSuite", 12.0, 12, "Important", false, List.of());
        Abonnement idle = createSubscription(
            "IdleService", 25.0, 120, "Optionnel", false, List.of());

        OptimizationConstraint constraint = OptimizationConstraint.of(25.0, 4.0, 3.0, 3.0);
        RebalanceResult result = rebalancer.rebalance(List.of(workTool, idle, premium), constraint);

        assertEquals(2, result.getKept().size());
        assertTrue(result.getKept().stream().anyMatch(abo -> "PremiumA".equals(abo.getNomService())));
        assertTrue(result.getKept().stream().anyMatch(abo -> "WorkSuite".equals(abo.getNomService())));
        assertTrue(result.getCancelled().stream().anyMatch(abo -> "IdleService".equals(abo.getNomService())));
        assertTrue(result.getFinalMonthlyCost() <= 25.0 + 1e-6);
    }

    @Test
    void zeroBudgetKeepsOnlyFreeSubscriptions() {
        Abonnement freeService = createSubscription(
            "Free", 0.0, 5, "Essentiel", true, List.of("Confort"));
        Abonnement paid = createSubscription(
            "Paid", 15.0, 10, "Important", false, List.of());

        OptimizationConstraint constraint = OptimizationConstraint.of(0.0, 2.0, 1.0, 1.0);
        RebalanceResult result = rebalancer.rebalance(List.of(freeService, paid), constraint);

        assertEquals(1, result.getKept().size());
        assertEquals("Free", result.getKept().get(0).getNomService());
        assertEquals(0.0, result.getFinalMonthlyCost(), 1e-6);
        assertFalse(result.getKept().stream().anyMatch(abo -> "Paid".equals(abo.getNomService())));
        boolean paidHandled = result.getOptimized().stream().anyMatch(abo -> "Paid".equals(abo.getNomService()))
            || result.getCancelled().stream().anyMatch(abo -> "Paid".equals(abo.getNomService()));
        assertTrue(paidHandled);
    }

    @Test
    void maintainsDescendingScoreOrderIndependentlyOfInput() {
        Abonnement high = createSubscription(
            "High", 4.0, 2, "Essentiel", true, List.of("Confort"));
        Abonnement medium = createSubscription(
            "Medium", 10.0, 20, "Important", false, List.of());
        Abonnement low = createSubscription(
               "Low", 20.0, 90, "Optionnel", false, List.of());

        OptimizationConstraint constraint = OptimizationConstraint.of(60.0, 3.0, 3.0, 4.0);
        RebalanceResult result = rebalancer.rebalance(List.of(medium, low, high), constraint);

        assertEquals(List.of("High", "Medium", "Low"),
            result.getKept().stream().map(Abonnement::getNomService).toList());
    }

    private Abonnement createSubscription(
        String name,
        double price,
        int daysSinceLastUse,
        String priorite,
        boolean partage,
        List<String> tags
    ) {
        LocalDate start = LocalDate.now().minusMonths(6);
        LocalDate end = LocalDate.now().plusMonths(6);
        LocalDate lastUse = LocalDate.now().minusDays(daysSinceLastUse);

        Abonnement abonnement = new Abonnement(name, start, end, price, "ClientTest", lastUse, "Service");
        abonnement.setPriorite(priorite);
        abonnement.setPartage(partage);
        abonnement.setTags(tags);
        return abonnement;
    }
}
