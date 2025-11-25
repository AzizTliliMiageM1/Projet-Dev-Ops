package com.projet.repository;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.abonnement.Abonnement;

public class FileAbonnementRepository implements AbonnementRepository {
    private final String filePath;
    private static final Logger logger = LoggerFactory.getLogger(FileAbonnementRepository.class);

    public FileAbonnementRepository(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public List<Abonnement> findAll() {
        List<Abonnement> abonnements = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) {
            return abonnements; // Retourne une liste vide si le fichier n'existe pas
        }

        boolean migrated = false;
        List<String> originalLines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            originalLines = reader.lines().filter(line -> !line.trim().isEmpty()).collect(Collectors.toList());
            for (String line : originalLines) {
                try {
                    Abonnement a = Abonnement.fromCsvString(line);
                    abonnements.add(a);
                    // detect old-format (6 parts) and mark for migration
                    int parts = line.split(";").length;
                    if (parts == 6 || parts == 7) {
                        // old formats without uuid (6 legacy, 7 new without id)
                        migrated = true;
                    }
                } catch (IllegalArgumentException ex) {
                    logger.warn("Ligne ignorée lors du chargement (format invalide): {} -> {}", line, ex.getMessage());
                }
            }
        } catch (IOException e) {
            logger.error("Erreur lors du chargement des abonnements depuis le fichier: {}", e.getMessage());
        }

        if (migrated) {
            // Réécrire le fichier avec le nouveau format (catégorie ajoutée)
            try {
                saveAll(abonnements);
                logger.info("Migration du fichier '{}' réalisée (anciennes lignes mises à jour).", filePath);
            } catch (Exception e) {
                logger.error("Erreur lors de la migration du fichier d'abonnements: {}", e.getMessage());
            }
        }

        logger.info("{} abonnements chargés depuis {}", abonnements.size(), filePath);
        return abonnements;
    }

    @Override
    public void saveAll(List<Abonnement> abonnements) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Abonnement abonnement : abonnements) {
                writer.write(abonnement.toCsvString());
                writer.newLine();
            }
            logger.info("{} abonnements sauvegardés dans {}", abonnements.size(), filePath);
        } catch (IOException e) {
            logger.error("Erreur lors de la sauvegarde des abonnements dans le fichier: {}", e.getMessage());
        }
    }



    @Override
    public Optional<Abonnement> findByUuid(String uuid) {
        return findAll().stream().filter(a -> uuid != null && uuid.equals(a.getId())).findFirst();
    }

    @Override
    public void save(Abonnement abonnement) {
        List<Abonnement> abonnements = findAll();
        // This method is primarily for adding new subscriptions.
        // For updates, the list should be modified and then saveAll called.
        abonnements.add(abonnement);
        saveAll(abonnements);
    }

    @Override
    public void delete(Abonnement abonnement) {
        List<Abonnement> abonnements = findAll();
        abonnements.remove(abonnement);
        saveAll(abonnements);
    }

    @Override
    public void deleteByUuid(String uuid) {
        List<Abonnement> abonnements = findAll();
        abonnements.removeIf(a -> uuid != null && uuid.equals(a.getId()));
        saveAll(abonnements);
    }
}