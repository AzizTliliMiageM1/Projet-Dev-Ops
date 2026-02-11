package com.projet.backend.adapter;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.projet.backend.domain.Abonnement;

class AbonnementCsvConverterTest {

    @Test
    void fromCsv_supportsLegacySixColumnFormat() {
        String csv = "ServiceX;2025-01-01;2025-12-31;9.99;Alice;2025-06-01";

        Abonnement abonnement = AbonnementCsvConverter.fromCsvString(csv);

        assertEquals("ServiceX", abonnement.getNomService());
        assertEquals(LocalDate.parse("2025-01-01"), abonnement.getDateDebut());
        assertEquals(LocalDate.parse("2025-12-31"), abonnement.getDateFin());
        assertEquals(9.99, abonnement.getPrixMensuel());
        assertEquals("Alice", abonnement.getClientName());
        assertEquals(LocalDate.parse("2025-06-01"), abonnement.getDerniereUtilisation());
        assertEquals("Non classé", abonnement.getCategorie());
    }

    @Test
    void fromCsv_supportsSevenColumnCategoryFormat() {
        String csv = "ServiceY;2025-02-01;2025-11-30;4.50;Bob;2025-03-01;Loisir";

        Abonnement abonnement = AbonnementCsvConverter.fromCsvString(csv);

        assertEquals("Loisir", abonnement.getCategorie());
    }

    @Test
    void toCsv_roundTripsExtendedFields() {
        Abonnement abonnement = new Abonnement(
            "Full",
            LocalDate.of(2025, 1, 1),
            LocalDate.of(2025, 12, 31),
            19.99,
            "Owner",
            LocalDate.of(2025, 1, 5),
            "Premium"
        );
        abonnement.setTags(List.of("tag1", "tag2"));
        abonnement.setGroupeAbonnement("Bundle");
        abonnement.setNotes("Contains;semicolon");
        abonnement.setNombreUtilisateurs(4);
        abonnement.setPartage(true);
        abonnement.setJoursRappelAvantFin(10);
        abonnement.setFrequencePaiement("Mensuel");

        String csv = AbonnementCsvConverter.toCsvString(abonnement);
        Abonnement parsed = AbonnementCsvConverter.fromCsvString(csv);

        assertEquals("Full", parsed.getNomService());
        assertEquals("Bundle", parsed.getGroupeAbonnement());
        assertTrue(parsed.getTags().containsAll(List.of("tag1", "tag2")));
        assertEquals(4, parsed.getNombreUtilisateurs());
        assertTrue(parsed.isPartage());
        assertEquals("Mensuel", parsed.getFrequencePaiement());
    }
}
