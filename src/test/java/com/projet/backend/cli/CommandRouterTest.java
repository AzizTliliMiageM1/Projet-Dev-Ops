package com.projet.backend.cli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class CommandRouterTest {

    private Path createSampleCsv() throws IOException {
        LocalDate now = LocalDate.now();
        Path csv = Files.createTempFile("abonnements-sample", ".csv");

        String header = "nomService;dateDebut;dateFin;prixMensuel;client;derniereUtilisation;categorie";
        String streaming = String.format(
            "Netflix;%s;%s;19.99;alice;%s;Streaming",
            now.minusMonths(12),
            now.plusMonths(6),
            now.minusDays(5)
        );
        String gym = String.format(
            "LuxuryGym;%s;%s;59.00;alice;%s;Sport",
            now.minusMonths(6),
            now.plusMonths(3),
            now.minusDays(120)
        );
        String magazine = String.format(
            "MagazinePlus;%s;%s;12.50;alice;%s;Lecture",
            now.minusMonths(3),
            now.plusDays(10),
            now.minusDays(95)
        );
        String cloud = String.format(
            "CloudBox;%s;%s;9.99;alice;%s;Productivite",
            now.minusMonths(2),
            now.plusMonths(2),
            now.minusDays(40)
        );

        Files.write(csv, List.of(header, streaming, gym, magazine, cloud));
        return csv;
    }

    @Test
    public void addSubscription_shouldCreateSubscription() {
        CommandRouter router = CommandRouter.createDefault();
        String[] args = new String[] {
            "addSubscription",
            "nomService=Netflix",
            "user=aziz",
            "prixMensuel=9.99",
            "dateDebut=2025-01-01",
            "dateFin=2026-01-01",
            "categorie=Streaming"
        };

        String result = router.route(args);
        assertTrue(result.contains("Subscription created"), "Expected creation message");
        assertTrue(result.contains("Netflix"));
        assertTrue(result.contains("aziz"));
    }

    @Test
    public void createUser_shouldReturnCreatedUser() {
        CommandRouter router = CommandRouter.createDefault();
        String[] args = new String[] {
            "createUser",
            "email=aziz@example.com",
            "password=Password1",
            "pseudo=aziz"
        };

        String result = router.route(args);
        assertTrue(result.contains("User created"), "Expected user creation message");
        assertTrue(result.contains("aziz@example.com"));
        assertTrue(result.contains("pseudo=aziz") || result.contains("(pseudo=aziz)"));
    }

    @Test
    public void dashboard_withoutFile_shouldDisplayHelp() {
        CommandRouter router = CommandRouter.createDefault();
        String[] args = new String[] {"dashboard"};

        String result = router.route(args);
        assertTrue(result.contains("Usage: dashboard"), "Expected help message");
        assertTrue(result.contains("tableau de bord"), "Expected French help text");
        assertTrue(result.contains("Résumé financier"), "Expected feature list");
    }

    @Test
    public void dashboard_withFile_shouldDisplayPortfolioData() {
        CommandRouter router = CommandRouter.createDefault();
        String[] args = new String[] {"dashboard", "file=test_abonnements_dashboard.csv"};

        String result = router.route(args);
        
        // Vérifier les sections principales
        assertTrue(result.contains("PORTEFEUILLE"), "Expected title");
        assertTrue(result.contains("FINANCIER") || result.contains("Dépense mensuelle"), "Expected financial section");
        assertTrue(result.contains("STATISTIQUES") || result.contains("Total abonnements"), "Expected stats");
        assertTrue(result.contains("À SURVEILLER") || result.contains("risque"), "Expected risk section");
        
        // Vérifier que les données sont présentes
        assertTrue(result.contains("Netflix") || result.contains("Spotify") || result.contains("Microsoft"), 
            "Expected service names from CSV");
    }

    @Test
    public void budgetPlan_withFile_shouldExposePlanDetails() {
        CommandRouter router = CommandRouter.createDefault();
        String[] args = new String[] {"budgetPlan", "file=test_abonnements_dashboard.csv", "target=150"};

        String result = router.route(args);

        assertTrue(result.contains("PLAN DE RÉDUCTION BUDGÉTAIRE"), "Expected plan header");
        assertTrue(result.contains("Budget actuel"), "Expected current budget line");
        assertTrue(result.contains("Priorités de résiliation"), "Expected cancellation priorities");
        assertTrue(result.contains("Écart restant"), "Expected shortfall information");
    }

    @Test
    public void budgetPlan_missingTarget_shouldReturnError() {
        CommandRouter router = CommandRouter.createDefault();
        String[] args = new String[] {"budgetPlan", "file=test_abonnements_dashboard.csv"};

        String result = router.route(args);

        assertTrue(result.contains("Missing 'target'"), "Expected missing target warning");
    }

    @Test
    public void portfolioHealth_shouldSummarizePortfolio() throws IOException {
        CommandRouter router = CommandRouter.createDefault();
        Path csv = createSampleCsv();

        String result = router.route(new String[] {"portfolioHealth", "file=" + csv});

        assertTrue(result.contains("SANTÉ DU PORTEFEUILLE"));
        assertTrue(result.contains("Score global"));
        assertTrue(result.contains("Churn élevé"));
    }

    @Test
    public void recommendations_shouldHighlightActions() throws IOException {
        CommandRouter router = CommandRouter.createDefault();
        Path csv = createSampleCsv();

        String result = router.route(new String[] {"recommendations", "file=" + csv});

        assertTrue(result.contains("RECOMMANDATIONS PERSONNALISÉES"));
        assertTrue(result.contains("Haut risque"));
        assertTrue(result.contains("Expirations proches"));
    }

    @Test
    public void budgetPlan_shouldWorkWithGeneratedCsv() throws IOException {
        CommandRouter router = CommandRouter.createDefault();
        Path csv = createSampleCsv();

        String result = router.route(new String[] {"budgetPlan", "target=40", "file=" + csv});

        assertTrue(result.contains("PLAN DE RÉDUCTION BUDGÉTAIRE"));
        assertTrue(result.contains("Priorités de résiliation"));
    }
}

