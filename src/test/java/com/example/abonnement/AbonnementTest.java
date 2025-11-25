package com.example.abonnement;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class AbonnementTest {

    @Test
    public void testFromCsvStringOldFormat() {
        String csv = "ServiceX;2025-01-01;2025-12-31;9.99;Alice;2025-06-01";
        Abonnement a = Abonnement.fromCsvString(csv);
        assertEquals("ServiceX", a.getNomService());
        assertEquals(LocalDate.parse("2025-01-01"), a.getDateDebut());
        assertEquals(LocalDate.parse("2025-12-31"), a.getDateFin());
        assertEquals(9.99, a.getPrixMensuel());
        assertEquals("Alice", a.getClientName());
        assertEquals(LocalDate.parse("2025-06-01"), a.getDerniereUtilisation());
        assertEquals("Non classé", a.getCategorie());
    }

    @Test
    public void testFromCsvStringNewFormat() {
        String csv = "ServiceY;2025-02-01;2025-11-30;4.50;Bob;2025-03-01;Loisir";
        Abonnement a = Abonnement.fromCsvString(csv);
        assertEquals("Loisir", a.getCategorie());
    }

    @Test
    public void testEstActif() {
        LocalDate start = LocalDate.now().minusDays(1);
        LocalDate end = LocalDate.now().plusDays(10);
        Abonnement a = new Abonnement("S", start, end, 1.0, "C");
        assertTrue(a.estActif());
    }
}
