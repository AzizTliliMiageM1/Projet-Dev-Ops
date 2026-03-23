package com.projet.backend.domain;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.projet.backend.adapter.AbonnementCsvConverter;

@DisplayName("Tests du domaine Abonnement")
class AbonnementTest {

    @Test
    @DisplayName("Doit parser ancien format CSV")
    void shouldParseOldCsvFormat() {
        String csv = oldFormatCsv();

        Abonnement a = AbonnementCsvConverter.fromCsvString(csv);

        assertAll(
            () -> assertEquals("ServiceX", a.getNomService()),
            () -> assertEquals(date("2025-01-01"), a.getDateDebut()),
            () -> assertEquals(date("2025-12-31"), a.getDateFin()),
            () -> assertEquals(9.99, a.getPrixMensuel()),
            () -> assertEquals("Alice", a.getClientName()),
            () -> assertEquals(date("2025-06-01"), a.getDerniereUtilisation()),
            () -> assertEquals("Non classé", a.getCategorie())
        );
    }

    @Test
    @DisplayName("Doit parser nouveau format CSV avec catégorie")
    void shouldParseNewCsvFormat() {
        String csv = newFormatCsv();

        Abonnement a = AbonnementCsvConverter.fromCsvString(csv);

        assertEquals("Loisir", a.getCategorie());
    }

    @Test
    @DisplayName("Doit être actif si date actuelle entre début et fin")
    void shouldBeActive() {
        Abonnement a = new Abonnement(
            "S",
            LocalDate.now().minusDays(1),
            LocalDate.now().plusDays(10),
            1.0,
            "C"
        );

        assertTrue(a.estActif());
    }

    // =========================
    // Helpers
    // =========================

    private String oldFormatCsv() {
        return "ServiceX;2025-01-01;2025-12-31;9.99;Alice;2025-06-01";
    }

    private String newFormatCsv() {
        return "ServiceY;2025-02-01;2025-11-30;4.50;Bob;2025-03-01;Loisir";
    }

    private LocalDate date(String d) {
        return LocalDate.parse(d);
    }
}