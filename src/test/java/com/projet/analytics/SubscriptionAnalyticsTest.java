package com.projet.analytics;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.projet.backend.domain.Abonnement;

class SubscriptionAnalyticsTest {

    @Test
    void calculateChurnRisk_flagsDormantSubscription() {
        Abonnement risky = buildAbonnement(
            "Plateforme", 25.0, LocalDate.now().minusMonths(10), LocalDate.now().plusMonths(2),
            LocalDate.now().minusDays(120), "Video", "Optionnel"
        );
        Abonnement healthy = buildAbonnement(
            "Service Pro", 15.0, LocalDate.now().minusMonths(6), LocalDate.now().plusMonths(6),
            LocalDate.now().minusDays(2), "Productivite", "Essentiel"
        );

        double riskScore = SubscriptionAnalytics.calculateChurnRisk(risky);
        double healthyScore = SubscriptionAnalytics.calculateChurnRisk(healthy);

        assertTrue(riskScore > 70);
        assertTrue(healthyScore < 30);
    }

    @Test
    void calculatePortfolioHealthScore_combinesActivityDiversificationAndInactivity() {
        Abonnement active1 = buildAbonnement(
            "Netflix", 17.99, LocalDate.now().minusMonths(6), LocalDate.now().plusMonths(6),
            LocalDate.now().minusDays(2), "Streaming", "Important"
        );
        Abonnement active2 = buildAbonnement(
            "Spotify", 9.99, LocalDate.now().minusMonths(3), LocalDate.now().plusMonths(3),
            LocalDate.now().minusDays(3), "Musique", "Important"
        );
        Abonnement inactive = buildAbonnement(
            "Magazine", 5.0, LocalDate.now().minusYears(1), LocalDate.now().minusDays(30),
            LocalDate.now().minusDays(90), "Lecture", "Optionnel"
        );

        List<Abonnement> portfolio = List.of(active1, active2, inactive);

        double healthScore = SubscriptionAnalytics.calculatePortfolioHealthScore(portfolio);

        assertTrue(healthScore > 0);
        assertTrue(healthScore < 100);
    }

    @Test
    void detectPriceAnomaly_identifiesOutlier() {
        List<Abonnement> portfolio = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            portfolio.add(
                buildAbonnement(
                    "Service " + i,
                    10.0 + (i % 3),
                    LocalDate.now().minusMonths(4),
                    LocalDate.now().plusMonths(4),
                    LocalDate.now().minusDays(5 + i),
                    "Divers",
                    "Important"
                )
            );
        }

        Abonnement expensive = buildAbonnement(
            "Premium", 120.0, LocalDate.now().minusMonths(4), LocalDate.now().plusMonths(4),
            LocalDate.now().minusDays(10), "Divers", "Optionnel"
        );
        portfolio.add(expensive);

        boolean anomaly = SubscriptionAnalytics.detectPriceAnomaly(portfolio, expensive);
        boolean normalFlag = SubscriptionAnalytics.detectPriceAnomaly(portfolio, portfolio.get(0));

        assertTrue(anomaly);
        assertFalse(normalFlag);
    }

    @Test
    void calculateValueScore_rewardsRecentUsage() {
        Abonnement recent = buildAbonnement(
            "Recent", 15.0, LocalDate.now().minusMonths(2), LocalDate.now().plusMonths(6),
            LocalDate.now().minusDays(2), "Pro", "Essentiel"
        );
        Abonnement stale = buildAbonnement(
            "Stale", 15.0, LocalDate.now().minusMonths(6), LocalDate.now().plusMonths(6),
            LocalDate.now().minusDays(90), "Pro", "Important"
        );

        double recentScore = SubscriptionAnalytics.calculateValueScore(recent);
        double staleScore = SubscriptionAnalytics.calculateValueScore(stale);

        assertTrue(recentScore > staleScore);
        assertTrue(recentScore > 0);
    }

    @Test
    void generateMonthlyReport_collectsRecommendationsAndTopSpenders() {
        List<Abonnement> abonnements = new ArrayList<>();
        abonnements.add(buildAbonnement(
            "Gold", 80.0, LocalDate.now().minusMonths(3), LocalDate.now().plusMonths(3),
            LocalDate.now().minusDays(5), "Premium", "Important"
        ));
        abonnements.add(buildAbonnement(
            "Silver", 40.0, LocalDate.now().minusMonths(4), LocalDate.now().plusMonths(2),
            LocalDate.now().minusDays(200), "Premium", "Optionnel"
        ));
        abonnements.add(buildAbonnement(
            "Bronze", 25.0, LocalDate.now().minusMonths(5), LocalDate.now().plusMonths(1),
            LocalDate.now().minusDays(190), "Loisir", "Optionnel"
        ));
        abonnements.add(buildAbonnement(
            "Basic", 9.0, LocalDate.now().minusMonths(2), LocalDate.now().plusMonths(4),
            LocalDate.now().minusDays(1), "Essentiel", "Essentiel"
        ));

        SubscriptionAnalytics.MonthlyReport report = SubscriptionAnalytics.generateMonthlyReport(abonnements);

        assertEquals(3, report.getTop3Depenses().size());
        assertTrue(report.getEconomiesPotentielles() >= 65.0);
        assertFalse(report.getRecommendations().isEmpty());
        assertTrue(report.getAbonnementsInutilises().stream().allMatch(a -> a.getChurnRisk() > 70));
    }

    @Test
    void planBudgetReduction_prioritisesHighRiskSubscriptions() {
        Abonnement essentiel = buildAbonnement(
            "Essentiel", 45.0, LocalDate.now().minusMonths(2), LocalDate.now().plusMonths(10),
            LocalDate.now().minusDays(1), "Travail", "Essentiel"
        );
        essentiel.setPriorite("Essentiel");

        Abonnement luxe = buildAbonnement(
            "Luxe", 30.0, LocalDate.now().minusMonths(6), LocalDate.now().plusMonths(2),
            LocalDate.now().minusDays(120), "Loisir", "Luxe"
        );

        Abonnement optionnel = buildAbonnement(
            "Optionnel", 25.0, LocalDate.now().minusMonths(4), LocalDate.now().plusMonths(1),
            LocalDate.now().minusDays(90), "Streaming", "Optionnel"
        );

        List<Abonnement> portfolio = List.of(essentiel, luxe, optionnel);

        SubscriptionAnalytics.BudgetReductionPlan plan = SubscriptionAnalytics.planBudgetReduction(portfolio, 50.0);

        assertTrue(plan.isTargetFeasible());
        List<String> recommendedNames = plan.getRecommendedCancellations().stream()
            .map(Abonnement::getNomService)
            .toList();
        assertTrue(recommendedNames.contains("Luxe"));
        assertTrue(recommendedNames.contains("Optionnel"));
        assertFalse(recommendedNames.contains("Essentiel"));
        assertTrue(plan.getAchievedSavings() >= plan.getRequiredSavings());
    }

    @Test
    void planBudgetReduction_returnsEmptyWhenBudgetAlreadyMet() {
        List<Abonnement> portfolio = List.of(buildAbonnement(
            "Mini", 10.0, LocalDate.now().minusMonths(1), LocalDate.now().plusMonths(2),
            LocalDate.now().minusDays(3), "Divers", "Important"
        ));

        SubscriptionAnalytics.BudgetReductionPlan plan = SubscriptionAnalytics.planBudgetReduction(portfolio, 20.0);

        assertTrue(plan.getRecommendedCancellations().isEmpty());
        assertEquals(0.0, plan.getRequiredSavings());
        assertTrue(plan.isTargetFeasible());
    }

    @Test
    void planBudgetReduction_rejectsNegativeTarget() {
        assertThrows(IllegalArgumentException.class, () ->
            SubscriptionAnalytics.planBudgetReduction(List.of(), -5.0)
        );
    }

    private Abonnement buildAbonnement(
        String nom, double prix, LocalDate debut, LocalDate fin, LocalDate lastUse,
        String categorie, String priorite
    ) {
        Abonnement abo = new Abonnement(nom, debut, fin, prix, "client", lastUse, categorie);
        abo.setPriorite(priorite);
        return abo;
    }
}
