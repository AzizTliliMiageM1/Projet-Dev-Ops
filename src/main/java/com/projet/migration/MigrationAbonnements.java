package com.projet.migration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.projet.backend.domain.Abonnement;

/**
 * Classe utilitaire pour migrer les fichiers d'abonnements de l'ancien format (8 colonnes)
 * vers le nouveau format complet (16 colonnes) avec les nouvelles fonctionnalités.
 * 
 * Cette migration :
 * - Crée une sauvegarde du fichier original
 * - Lit tous les abonnements dans l'ancien format
 * - Les réécrit dans le nouveau format avec valeurs par défaut
 * - Génère un rapport de migration
 */
public class MigrationAbonnements {
    
    private static final String BACKUP_SUFFIX = ".backup_";
    
    /**
     * Migre un fichier d'abonnements vers le nouveau format
     * @param filePath Chemin du fichier à migrer
     * @return true si la migration a réussi, false sinon
     */
    public static boolean migrerFichier(String filePath) {
        System.out.println("=== Début de la migration du fichier : " + filePath + " ===\n");
        
        Path originalPath = Paths.get(filePath);
        
        // Vérifier que le fichier existe
        if (!Files.exists(originalPath)) {
            System.err.println("❌ ERREUR : Le fichier " + filePath + " n'existe pas !");
            return false;
        }
        
        // Créer une sauvegarde avec timestamp
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String backupPath = filePath + BACKUP_SUFFIX + timestamp;
        
        try {
            Files.copy(originalPath, Paths.get(backupPath), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("✅ Sauvegarde créée : " + backupPath);
        } catch (IOException e) {
            System.err.println("❌ ERREUR : Impossible de créer la sauvegarde : " + e.getMessage());
            return false;
        }
        
        // Lire tous les abonnements
        List<Abonnement> abonnements = new ArrayList<>();
        int lignesLues = 0;
        int lignesIgnorees = 0;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                lignesLues++;
                try {
                    // fromCsvString gère automatiquement les 3 formats (6, 8, 16 colonnes)
                    Abonnement abonnement = Abonnement.fromCsvString(line);
                    abonnements.add(abonnement);
                } catch (Exception e) {
                    System.err.println("⚠️  Ligne " + lignesLues + " ignorée (format invalide) : " + line);
                    System.err.println("   Raison : " + e.getMessage());
                    lignesIgnorees++;
                }
            }
        } catch (IOException e) {
            System.err.println("❌ ERREUR : Impossible de lire le fichier : " + e.getMessage());
            return false;
        }
        
        System.out.println("\n📊 Statistiques de lecture :");
        System.out.println("   - Lignes lues : " + lignesLues);
        System.out.println("   - Abonnements chargés : " + abonnements.size());
        System.out.println("   - Lignes ignorées : " + lignesIgnorees);
        
        // Réécrire tous les abonnements au nouveau format (16 colonnes)
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Abonnement abonnement : abonnements) {
                // toCsvString() génère automatiquement le format 16 colonnes
                writer.write(abonnement.toCsvString());
                writer.newLine();
            }
            System.out.println("\n✅ Migration réussie : " + abonnements.size() + " abonnements migrés");
        } catch (IOException e) {
            System.err.println("❌ ERREUR : Impossible d'écrire le fichier migré : " + e.getMessage());
            
            // En cas d'erreur, restaurer la sauvegarde
            try {
                Files.copy(Paths.get(backupPath), originalPath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("♻️  Sauvegarde restaurée suite à l'erreur");
            } catch (IOException e2) {
                System.err.println("❌ ERREUR CRITIQUE : Impossible de restaurer la sauvegarde !");
            }
            return false;
        }
        
        // Afficher un exemple de migration
        if (!abonnements.isEmpty()) {
            System.out.println("\n📝 Exemple de migration (premier abonnement) :");
            System.out.println("   Client : " + abonnements.get(0).getClientName());
            System.out.println("   Service : " + abonnements.get(0).getNomService());
            System.out.println("   Catégorie : " + abonnements.get(0).getCategorie());
            System.out.println("   Priorité : " + abonnements.get(0).getPriorite());
            System.out.println("   Fréquence : " + abonnements.get(0).getFrequencePaiement());
            System.out.println("   Tags : " + abonnements.get(0).getTags());
            System.out.println("   Partagé : " + (abonnements.get(0).isPartage() ? "Oui" : "Non"));
        }
        
        System.out.println("\n=== Migration terminée avec succès ===");
        System.out.println("💾 Fichier original sauvegardé : " + backupPath);
        System.out.println("✨ Fichier migré : " + filePath);
        
        return true;
    }
    
    /**
     * Migre plusieurs fichiers d'abonnements
     * @param filePaths Liste des chemins de fichiers à migrer
     */
    public static void migrerFichiers(String... filePaths) {
        int succes = 0;
        int echecs = 0;
        
        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║  MIGRATION DES FICHIERS D'ABONNEMENTS                 ║");
        System.out.println("║  Format 8 colonnes → Format 16 colonnes               ║");
        System.out.println("╚════════════════════════════════════════════════════════╝\n");
        
        for (String filePath : filePaths) {
            if (migrerFichier(filePath)) {
                succes++;
            } else {
                echecs++;
            }
            System.out.println("\n" + "=".repeat(60) + "\n");
        }
        
        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║  RAPPORT FINAL DE MIGRATION                           ║");
        System.out.println("╠════════════════════════════════════════════════════════╣");
        System.out.println("║  ✅ Migrations réussies : " + String.format("%2d", succes) + "                          ║");
        System.out.println("║  ❌ Migrations échouées : " + String.format("%2d", echecs) + "                          ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
    }
    
    /**
     * Méthode principale pour exécuter la migration
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            // Migration du fichier par défaut
            System.out.println("Aucun fichier spécifié, migration du fichier par défaut...\n");
            migrerFichier("data/abonnements.txt");
        } else {
            // Migration des fichiers spécifiés en argument
            migrerFichiers(args);
        }
    }
}
