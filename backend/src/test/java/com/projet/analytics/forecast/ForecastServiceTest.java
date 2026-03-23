package com.projet.analytics.forecast;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.projet.backend.domain.Abonnement;

class ForecastServiceTest {

    private ForecastService forecastService;

    @BeforeEach
    void setUp() {
        forecastService = new ForecastServiceImpl();
    }

    @Test
    @DisplayName("Doit projeter les coûts sur 6 mois pour des abonnements mensuels et annuels")
    void shouldProjectCostsOverSixMonths() {
        // Arrange
        LocalDate fixedDate = LocalDate.of(2026, 3, 23);

        Abonnement netflix = createAbonnement(
                "Netflix",
                fixedDate.minusMonths(1),
                fixedDate.plusYears(1),
                15.0,
                "user1",
                "Mensuel"
        );

        Abonnement spotify = createAbonnement(
                "Spotify",
                fixedDate.minusMonths(2),
                fixedDate.plusYears(1),
                10.0,
                "user1",
                "Mensuel"
        );

        Abonnement office365 = createAbonnement(
                "Office 365",
                fixedDate.plusMonths(2),
                fixedDate.plusYears(1),
                120.0,
                "user1",
                "Annuel"
        );

        List<Abonnement> abonnements = List.of(netflix, spotify, office365);

        // Act
        ForecastResult result = forecastService.projectCosts(abonnements, 6);

        // Assert
        assertAll("Vérification du résultat de projection",
                () -> assertNotNull(result, "Le résultat ne doit pas être null"),
                () -> assertEquals(6, result.getMonths(), "La projection doit couvrir 6 mois"),
                () -> assertNotNull(result.getProjectedCosts(), "La map des coûts projetés ne doit pas être null"),
                () -> assertFalse(result.getProjectedCosts().isEmpty(), "Les coûts projetés ne doivent pas être vides")
        );

        Map<?, ?> projectedCosts = result.getProjectedCosts();

        assertTrue(
                projectedCosts.size() <= 6,
                "Le nombre de périodes projetées ne doit pas dépasser le nombre de mois demandé"
        );
    }

    private Abonnement createAbonnement(
            String nomService,
            LocalDate dateDebut,
            LocalDate dateFin,
            double prix,
            String client,
            String frequencePaiement
    ) {
        Abonnement abonnement = new Abonnement(nomService, dateDebut, dateFin, prix, client);
        abonnement.setFrequencePaiement(frequencePaiement);
        return abonnement;
    }
}