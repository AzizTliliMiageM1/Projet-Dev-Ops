package com.projet.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.projet.backend.adapter.AbonnementCsvConverter;
import com.projet.backend.domain.Abonnement;

public class FileAbonnementRepositoryTest {

    @Test
    public void testMigrationOfOldCsvFormat() throws IOException {
        Path tempFile = Files.createTempFile("abonnements-test", ".txt");
        // old format: 6 fields (no category)
        Files.write(tempFile, List.of(
                "ServiceA;2025-01-01;2025-12-31;5.00;UserA;2025-06-01",
                "ServiceB;2025-02-01;2025-08-31;3.50;UserB;2025-03-01"
        ));

        FileAbonnementRepository repo = new FileAbonnementRepository(tempFile.toString());
        List<Abonnement> all = repo.findAll();
        assertEquals(2, all.size());

        // After findAll(), the file should migrate to extended 16-field format
        List<String> lines = Files.readAllLines(tempFile);
        for (String line : lines) {
            String[] parts = line.split(";");
            assertEquals(16, parts.length, "Line should have 16 parts after migration (extended format)");
        }

        // cleanup
        Files.deleteIfExists(tempFile);
    }

    @Test
    public void findAll_skipsCorruptedLines() throws IOException {
        Path tempFile = Files.createTempFile("abonnements-corrupt", ".txt");

        Abonnement valid = new Abonnement("ServiceValid", 
            java.time.LocalDate.now().minusMonths(1),
            java.time.LocalDate.now().plusMonths(6),
            12.5,
            "UserValid",
            java.time.LocalDate.now().minusDays(3),
            "Pro"
        );

        String validLine = AbonnementCsvConverter.toCsvString(valid);

        Files.write(tempFile, List.of(
            validLine,
            "corrupted-line-without-enough-fields",
            "Another;bad;line"
        ));

        FileAbonnementRepository repo = new FileAbonnementRepository(tempFile.toString());
        List<Abonnement> all = repo.findAll();

        assertEquals(1, all.size());
        assertEquals("ServiceValid", all.get(0).getNomService());

        Files.deleteIfExists(tempFile);
    }

    @Test
    public void saveAndReload_preservesExtendedColumns() throws IOException {
        Path tempFile = Files.createTempFile("abonnements-roundtrip", ".txt");

        Abonnement abonnement = new Abonnement(
            "RoundTrip",
            java.time.LocalDate.of(2025, 1, 1),
            java.time.LocalDate.of(2025, 12, 31),
            29.99,
            "Tester",
            java.time.LocalDate.of(2025, 1, 15),
            "Business"
        );
        abonnement.setTags(List.of("team", "priority"));
        abonnement.setGroupeAbonnement("Pack Pro");
        abonnement.setNotes("note;with;semicolons");
        abonnement.setNombreUtilisateurs(3);
        abonnement.setPartage(true);
        abonnement.setJoursRappelAvantFin(14);
        abonnement.setFrequencePaiement("Trimestriel");

        FileAbonnementRepository repo = new FileAbonnementRepository(tempFile.toString());
        repo.saveAll(List.of(abonnement));

        List<Abonnement> reloaded = repo.findAll();
        assertEquals(1, reloaded.size());
        Abonnement back = reloaded.get(0);

        assertEquals("RoundTrip", back.getNomService());
        assertEquals("Pack Pro", back.getGroupeAbonnement());
        assertTrue(back.getTags().contains("team"));
        assertEquals(3, back.getNombreUtilisateurs());
        assertTrue(back.isPartage());
        assertEquals("Trimestriel", back.getFrequencePaiement());

        Files.deleteIfExists(tempFile);
    }
}
