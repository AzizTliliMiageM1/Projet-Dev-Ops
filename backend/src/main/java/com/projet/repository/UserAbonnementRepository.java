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

import com.projet.backend.adapter.AbonnementCsvConverter;
import com.projet.backend.domain.Abonnement;

/**
 * Repository pour gérer les abonnements par utilisateur
 * Chaque utilisateur a son propre fichier d'abonnements
 */
public class UserAbonnementRepository implements AbonnementRepository {
    private final String userEmail;
    private final String baseDir;
    private static final Logger logger = LoggerFactory.getLogger(UserAbonnementRepository.class);

    public UserAbonnementRepository(String userEmail) {
        this.userEmail = userEmail;
        this.baseDir = "data/abonnements/";
        
        // Créer le répertoire s'il n'existe pas
        File dir = new File(baseDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    private String getFilePath() {
        // Remplacer les caractères invalides dans le nom de fichier
        String sanitizedEmail = userEmail.replaceAll("[^a-zA-Z0-9@._-]", "_");
        return baseDir + "abonnements_" + sanitizedEmail + ".txt";
    }

    @Override
    public List<Abonnement> findAll() {
        List<Abonnement> abonnements = new ArrayList<>();
        File file = new File(getFilePath());
        
        if (!file.exists()) {
            logger.info("Aucun fichier d'abonnements trouvé pour {}", userEmail);
            return abonnements;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            List<String> lines = reader.lines()
                .filter(line -> !line.trim().isEmpty())
                .collect(Collectors.toList());
                
            for (String line : lines) {
                try {
                    Abonnement a = AbonnementCsvConverter.fromCsvString(line);
                    abonnements.add(a);
                } catch (IllegalArgumentException ex) {
                    logger.warn("Ligne ignorée pour {} : {}", userEmail, ex.getMessage());
                }
            }
            
            logger.info("{} abonnements chargés pour {}", abonnements.size(), userEmail);
        } catch (IOException e) {
            logger.error("Erreur lors du chargement des abonnements pour {}", userEmail, e);
        }

        return abonnements;
    }

    @Override
    public void saveAll(List<Abonnement> abonnements) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(getFilePath()))) {
            for (Abonnement a : abonnements) {
                writer.write(AbonnementCsvConverter.toCsvString(a));
                writer.newLine();
            }
            logger.info("{} abonnements sauvegardés pour {}", abonnements.size(), userEmail);
        } catch (IOException e) {
            logger.error("Erreur lors de la sauvegarde des abonnements pour {}", userEmail, e);
        }
    }

    @Override
    public Optional<Abonnement> findByUuid(String uuid) {
        return findAll().stream()
            .filter(a -> a.getId() != null && a.getId().equals(uuid))
            .findFirst();
    }

    @Override
    public void save(Abonnement abonnement) {
        List<Abonnement> all = findAll();
        
        // Chercher si existe déjà
        Optional<Abonnement> existing = all.stream()
            .filter(a -> a.getId() != null && a.getId().equals(abonnement.getId()))
            .findFirst();
            
        if (existing.isPresent()) {
            // Remplacer
            all = all.stream()
                .map(a -> a.getId().equals(abonnement.getId()) ? abonnement : a)
                .collect(Collectors.toList());
        } else {
            // Ajouter
            all.add(abonnement);
        }
        
        saveAll(all);
    }

    @Override
    public void delete(Abonnement abonnement) {
        if (abonnement.getId() != null) {
            deleteByUuid(abonnement.getId());
        }
    }

    @Override
    public void deleteByUuid(String uuid) {
        List<Abonnement> all = findAll();
        List<Abonnement> filtered = all.stream()
            .filter(a -> a.getId() == null || !a.getId().equals(uuid))
            .collect(Collectors.toList());
            
        if (filtered.size() < all.size()) {
            saveAll(filtered);
            logger.info("Abonnement {} supprimé pour {}", uuid, userEmail);
        }
    }

    /**
     * Obtenir le nombre total d'utilisateurs ayant des abonnements
     */
    public static int getTotalUsers() {
        File dir = new File("data/abonnements/");
        if (!dir.exists()) return 0;
        
        File[] files = dir.listFiles((d, name) -> name.startsWith("abonnements_") && name.endsWith(".txt"));
        return files != null ? files.length : 0;
    }
}
