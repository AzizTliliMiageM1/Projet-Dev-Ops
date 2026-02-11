package com.projet.backend.cli;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.projet.analytics.SubscriptionAnalytics;
import com.projet.backend.domain.Abonnement;
import com.projet.backend.service.SubscriptionService;
import com.projet.backend.service.SubscriptionService.PortfolioStats;

/**
 * Scenario console pour démontrer les capacités backend sans UI.
 */
public final class BackendScenarioRunner {

    private BackendScenarioRunner() {
        // Utility class
    }

    public static void main(String[] args) {
        List<Abonnement> abonnements = buildSamplePortfolio();

        SubscriptionService service = new SubscriptionService();
        PortfolioStats stats = service.calculatePortfolioStats(abonnements);

        System.out.println("=== DEMONSTRATION BACKEND ===");
        System.out.println("Total abonnements: " + stats.totalSubscriptions);
        System.out.println("Actifs: " + stats.activeSubscriptions);
        System.out.println("Inactifs: " + stats.inactiveSubscriptions);
        System.out.println("Cout total mensuel: " + round(stats.totalMonthlyCost));
        System.out.println("Score sante portefeuille: " + round(stats.portfolioHealthScore));
        System.out.println("Categorie(s): " + stats.categoriesDistribution);
        System.out.println("Abonnements a risque: " + stats.highChurnRiskCount);

        System.out.println();
        System.out.println("-- Focus analytique --");
        abonnements.forEach(abo -> {
            double valueScore = SubscriptionAnalytics.calculateValueScore(abo);
            double churn = SubscriptionAnalytics.calculateChurnRisk(abo);
            double roi = service.calculateRoiScore(abo);

            System.out.println("Service: " + abo.getNomService());
            System.out.println("  Prix mensuel: " + abo.getPrixMensuel());
            System.out.println("  Score valeur: " + round(valueScore));
            System.out.println("  Risque churn: " + round(churn));
            System.out.println("  Score ROI: " + round(roi));
        });

        System.out.println();
        System.out.println("Economies potentielles: " + round(
            service.identifySavingOpportunities(abonnements)
                   .stream()
                   .mapToDouble(Abonnement::getPrixMensuel)
                   .sum()
        ));

        System.out.println("=== FIN SCENARIO ===");
    }

    private static List<Abonnement> buildSamplePortfolio() {
        List<Abonnement> list = new ArrayList<>();

        list.add(createAbonnement(
            "Netflix", "Streaming", "Optionnel", 17.99,
            LocalDate.now().minusMonths(8),
            LocalDate.now().plusMonths(2),
            LocalDate.now().minusDays(3)
        ));
        list.add(createAbonnement(
            "Spotify", "Musique", "Essentiel", 9.99,
            LocalDate.now().minusYears(2),
            LocalDate.now().plusYears(1),
            LocalDate.now().minusDays(1)
        ));
        list.add(createAbonnement(
            "Adobe Creative Cloud", "Productivite", "Important", 59.99,
            LocalDate.now().minusMonths(14),
            LocalDate.now().plusMonths(1),
            LocalDate.now().minusDays(28)
        ));
        list.add(createAbonnement(
            "Canal+", "Streaming", "Optionnel", 25.00,
            LocalDate.now().minusMonths(12),
            LocalDate.now().plusDays(10),
            LocalDate.now().minusDays(90)
        ));

        return list;
    }

    private static Abonnement createAbonnement(
        String nom, String categorie, String priorite, double prix,
        LocalDate debut, LocalDate fin, LocalDate derniereUtilisation
    ) {
        Abonnement abo = new Abonnement(
            nom,
            debut,
            fin,
            prix,
            "demo-user",
            derniereUtilisation,
            categorie
        );
        abo.setPriorite(priorite);
        abo.setNombreUtilisateurs(1);
        abo.setNotes("Scenario backend");
        return abo;
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
