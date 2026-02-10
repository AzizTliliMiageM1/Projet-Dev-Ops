package com.projet.backend.cli;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class CommandRouterTest {

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
}

