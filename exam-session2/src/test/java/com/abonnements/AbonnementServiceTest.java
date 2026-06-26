package com.abonnements;

import com.abonnements.model.Abonnement;
import com.abonnements.service.AbonnementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour AbonnementService.
 * On teste la logique metier sans demarrer le serveur HTTP.
 */
class AbonnementServiceTest {

    private AbonnementService service;

    @BeforeEach
    void setUp() {
        // nouveau service avant chaque test avec les 4 abonnements de demo
        service = new AbonnementService();
    }

    @Test
    void testAjouterAbonnement() {
        int tailleAvant = service.getAll().size();
        Abonnement a = new Abonnement("Disney+", 8.99, "streaming");
        service.ajouter(a);
        assertEquals(tailleAvant + 1, service.getAll().size());
    }

    @Test
    void testTotalMensuelAvecDonneesDemo() {
        // Netflix(13.99) + Spotify(9.99) + Amazon(8.99) + M365(10.00) = 42.97
        double total = service.calculerTotalMensuel();
        assertEquals(42.97, total, 0.01);
    }

    @Test
    void testTotalAugmenteApresAjout() {
        double avant = service.calculerTotalMensuel();
        service.ajouter(new Abonnement("Canal+", 19.99, "streaming"));
        double apres = service.calculerTotalMensuel();
        assertEquals(avant + 19.99, apres, 0.01);
    }

    @Test
    void testSupprimerAbonnement() {
        Abonnement a = new Abonnement("Test", 5.00, "test");
        service.ajouter(a);
        int tailleAvant = service.getAll().size();

        boolean ok = service.supprimer(a.getId());

        assertTrue(ok, "La suppression devrait reussir");
        assertEquals(tailleAvant - 1, service.getAll().size());
    }

    @Test
    void testSupprimerIdQuiNExistePas() {
        boolean ok = service.supprimer("id-qui-nexiste-pas-du-tout");
        assertFalse(ok, "Supprimer un id inexistant doit retourner false");
    }

    @Test
    void testStatsParCategorieStreaming() {
        Map<String, Double> stats = service.getStatsParCategorie();
        // Netflix(13.99) + Amazon Prime(8.99) = 22.98 dans "streaming"
        assertTrue(stats.containsKey("streaming"));
        assertEquals(22.98, stats.get("streaming"), 0.01);
    }

    @Test
    void testAnalyseBudgetContientTousLesCles() {
        Map<String, Object> analyse = service.getAnalyseBudget();
        assertTrue(analyse.containsKey("totalMensuel"));
        assertTrue(analyse.containsKey("totalAnnuel"));
        assertTrue(analyse.containsKey("nombreAbonnements"));
        assertTrue(analyse.containsKey("statsParCategorie"));
        assertTrue(analyse.containsKey("categoriePlusChere"));
        assertTrue(analyse.containsKey("moyenneMensuelle"));
    }

    @Test
    void testAbonnementInactifExcluDuTotal() {
        Abonnement inactif = new Abonnement("Service Inactif", 50.00, "test");
        inactif.setActif(false);
        service.ajouter(inactif);

        // le total ne doit pas avoir change
        assertEquals(42.97, service.calculerTotalMensuel(), 0.01);
    }
}
