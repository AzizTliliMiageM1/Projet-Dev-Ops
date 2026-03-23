package com.projet.backend.cli;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Tests du routeur de commandes CLI")
class CommandRouterTest {

    private CommandRouter router;

    @BeforeEach
    void setUp() {
        router = CommandRouter.createDefault();
    }

    @Test
    @DisplayName("La commande addSubscription doit créer un abonnement")
    void shouldCreateSubscription() {
        String[] args = command(
            "addSubscription",
            "nomService=Netflix",
            "user=aziz",
            "prixMensuel=9.99",
            "dateDebut=2025-01-01",
            "dateFin=2026-01-01",
            "categorie=Streaming"
        );

        String result = router.route(args);

        assertAll(
            () -> assertTrue(result.contains("Subscription created"), "Expected creation message"),
            () -> assertTrue(result.contains("Netflix"), "Expected service name in result"),
            () -> assertTrue(result.contains("aziz"), "Expected username in result")
        );
    }

    @Test
    @DisplayName("La commande createUser doit retourner l'utilisateur créé")
    void shouldCreateUser() {
        String[] args = command(
            "createUser",
            "email=aziz@example.com",
            "password=Password1",
            "pseudo=aziz"
        );

        String result = router.route(args);

        assertAll(
            () -> assertTrue(result.contains("User created"), "Expected user creation message"),
            () -> assertTrue(result.contains("aziz@example.com"), "Expected email in result"),
            () -> assertTrue(
                result.contains("pseudo=aziz") || result.contains("(pseudo=aziz)"),
                "Expected pseudo in result"
            )
        );
    }

    @Test
    @DisplayName("La commande dashboard sans fichier doit afficher l'aide")
    void shouldDisplayHelpWhenDashboardHasNoFile() {
        String[] args = command("dashboard");

        String result = router.route(args);

        assertAll(
            () -> assertTrue(result.contains("Usage: dashboard"), "Expected help message"),
            () -> assertTrue(result.contains("tableau de bord"), "Expected French help text"),
            () -> assertTrue(result.contains("Résumé financier"), "Expected feature list")
        );
    }

    @Test
    @DisplayName("La commande dashboard avec fichier doit afficher les données du portefeuille")
    void shouldDisplayPortfolioDataWhenDashboardHasFile() {
        String[] args = command("dashboard", "file=test_abonnements_dashboard.csv");

        String result = router.route(args);

        assertAll(
            () -> assertTrue(result.contains("PORTEFEUILLE"), "Expected title"),
            () -> assertTrue(
                result.contains("FINANCIER") || result.contains("Dépense mensuelle"),
                "Expected financial section"
            ),
            () -> assertTrue(
                result.contains("STATISTIQUES") || result.contains("Total abonnements"),
                "Expected stats section"
            ),
            () -> assertTrue(
                result.contains("À SURVEILLER") || result.contains("risque"),
                "Expected risk section"
            ),
            () -> assertTrue(
                result.contains("Netflix") || result.contains("Spotify") || result.contains("Microsoft"),
                "Expected service names from CSV"
            )
        );
    }

    // =========================
    // Helpers
    // =========================

    private String[] command(String... args) {
        return args;
    }
}