package com.projet.backend.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.projet.backend.domain.Abonnement;

class SubscriptionServiceTest {

    private final SubscriptionService service = new SubscriptionService();

    @Test
    void validateSubscription_returnsSuccessForValidInput() {
        Abonnement abo = buildAbonnement(
            "Netflix", 17.99, LocalDate.now().minusMonths(3), LocalDate.now().plusMonths(6),
            LocalDate.now().minusDays(2), "Streaming", "Important"
        );

        SubscriptionService.ValidationResult result = service.validateSubscription(abo);

        assertTrue(result.valid);
        assertTrue(result.errors.isEmpty());
    }

    @Test
    void validateSubscription_rejectsNegativePrice() {
        Abonnement abo = buildAbonnement(
            "Invalid", -5.0, LocalDate.now().minusDays(10), LocalDate.now().plusDays(10),
            LocalDate.now().minusDays(1), "Divers", "Optionnel"
        );

        SubscriptionService.ValidationResult result = service.validateSubscription(abo);

        assertFalse(result.valid);
        assertTrue(result.errors.stream().anyMatch(msg -> msg.contains("prix")));
    }

    @Test
    void createSubscription_throwsOnInvalidData() {
        assertThrows(IllegalArgumentException.class, () ->
            service.createSubscription(
                "Erreur", LocalDate.now(), LocalDate.now().minusDays(1), 10.0, "client", "Divers"
            )
        );
    }

    @Test
    void getHighChurnRiskSubscriptions_detectsRiskyEntries() {
        Abonnement safe = buildAbonnement(
            "Spotify", 9.99, LocalDate.now().minusMonths(6), LocalDate.now().plusMonths(6),
            LocalDate.now().minusDays(1), "Musique", "Essentiel"
        );
        Abonnement risky = buildAbonnement(
            "Canal+", 25.0, LocalDate.now().minusMonths(12), LocalDate.now().plusDays(5),
            LocalDate.now().minusDays(90), "Streaming", "Optionnel"
        );

        List<Abonnement> result = service.getHighChurnRiskSubscriptions(List.of(safe, risky));

        assertEquals(1, result.size());
        assertEquals(risky.getId(), result.get(0).getId());
    }

    @Test
    void calculatePortfolioStats_computesAggregatedMetrics() {
        Abonnement active = buildAbonnement(
            "Active", 20.0, LocalDate.now().minusMonths(2), LocalDate.now().plusMonths(2),
            LocalDate.now().minusDays(5), "Streaming", "Important"
        );
        Abonnement expiring = buildAbonnement(
            "Expiring", 15.0, LocalDate.now().minusMonths(1), LocalDate.now().plusDays(5),
            LocalDate.now().minusDays(40), "Divers", "Optionnel"
        );
        Abonnement inactive = buildAbonnement(
            "Inactive", 12.0, LocalDate.now().minusYears(1), LocalDate.now().minusDays(10),
            LocalDate.now().minusDays(120), "News", "Optionnel"
        );

        SubscriptionService.PortfolioStats stats = service.calculatePortfolioStats(List.of(active, expiring, inactive));

        assertEquals(3, stats.totalSubscriptions);
        assertEquals(2, stats.activeSubscriptions);
        assertEquals(1, stats.inactiveSubscriptions);
        assertEquals(47.0, stats.totalMonthlyCost, 0.01);
        assertTrue(stats.portfolioHealthScore > 0);
        assertEquals(3, stats.categoriesDistribution.size());
    }

    @Test
    void calculateRoiScore_returnsBoundedValue() {
        Abonnement abo = buildAbonnement(
            "Productivite", 30.0, LocalDate.now().minusMonths(6), LocalDate.now().plusMonths(6),
            LocalDate.now().minusDays(1), "Productivite", "Essentiel"
        );

        double roi = service.calculateRoiScore(abo);

        assertTrue(roi > 0);
        assertTrue(roi <= 100);
    }

    @Test
    void identifySavingOpportunities_flagsInactiveOrHighRisk() {
        Abonnement dormant = buildAbonnement(
            "Dormant", 19.0, LocalDate.now().minusMonths(12), LocalDate.now().plusMonths(1),
            LocalDate.now().minusDays(120), "Divers", "Optionnel"
        );
        Abonnement active = buildAbonnement(
            "Active", 12.0, LocalDate.now().minusMonths(3), LocalDate.now().plusMonths(6),
            LocalDate.now().minusDays(3), "Streaming", "Essentiel"
        );

        List<Abonnement> result = service.identifySavingOpportunities(List.of(dormant, active));

        assertEquals(1, result.size());
        assertEquals(dormant.getId(), result.get(0).getId());
    }

    @Test
    void getTopPrioritySubscriptions_returnsHighValueFirst() {
        Abonnement mustKeep = buildAbonnement(
            "MustKeep", 25.0, LocalDate.now().minusMonths(3), LocalDate.now().plusMonths(6),
            LocalDate.now().minusDays(1), "Pro", "Essentiel"
        );
        Abonnement lowValue = buildAbonnement(
            "LowValue", 15.0, LocalDate.now().minusMonths(5), LocalDate.now().plusMonths(2),
            LocalDate.now().minusDays(90), "Loisir", "Optionnel"
        );
        // degrade value score by setting distant last use
        lowValue.setDerniereUtilisation(LocalDate.now().minusDays(120));

        List<Abonnement> ordered = service.getTopPrioritySubscriptions(List.of(lowValue, mustKeep));

        List<String> orderedNames = ordered.stream().map(Abonnement::getNomService).collect(Collectors.toList());
        assertEquals("MustKeep", orderedNames.get(0));
        assertTrue(orderedNames.contains("LowValue"));
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
