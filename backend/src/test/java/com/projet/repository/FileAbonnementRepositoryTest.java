package com.projet.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.projet.backend.domain.Abonnement;

@DisplayName("Tests du repository fichier des abonnements")
class FileAbonnementRepositoryTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Doit migrer automatiquement un ancien format CSV vers le nouveau format")
    void shouldMigrateOldCsvFormat() throws IOException {
        Path tempFile = tempDir.resolve("abonnements-test.txt");

        Files.write(tempFile, List.of(
            "ServiceA;2025-01-01;2025-12-31;5.00;UserA;2025-06-01",
            "ServiceB;2025-02-01;2025-08-31;3.50;UserB;2025-03-01"
        ));

        FileAbonnementRepository repo = new FileAbonnementRepository(tempFile.toString());

        // Act
        List<Abonnement> abonnements = repo.findAll();
        List<String> migratedLines = Files.readAllLines(tempFile);

        // Assert
        assertAll(
            () -> assertEquals(2, abonnements.size(), "Le repository doit charger 2 abonnements"),
            () -> assertEquals(2, migratedLines.size(), "Le fichier migré doit contenir 2 lignes")
        );

        for (String line : migratedLines) {
            String[] parts = line.split(";");
            assertEquals(16, parts.length, "Chaque ligne migrée doit contenir 16 colonnes");
        }

        assertAll(
            () -> assertEquals("ServiceA", abonnements.get(0).getNomService()),
            () -> assertEquals("ServiceB", abonnements.get(1).getNomService())
        );
    }
}