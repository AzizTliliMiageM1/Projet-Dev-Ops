package com.projet.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

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

        // After findAll(), the file should have been migrated to include id + all new fields (16 fields)
        List<String> lines = Files.readAllLines(tempFile);
        for (String line : lines) {
            String[] parts = line.split(";");
            assertEquals(16, parts.length, "Line should have 16 parts after migration");
        }

        // cleanup
        Files.deleteIfExists(tempFile);
    }
}
