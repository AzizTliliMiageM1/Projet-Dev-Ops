package com.example.abonnement;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FileAbonnementRepository implements AbonnementRepository {
    private final String filePath;

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

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            abonnements = reader.lines()
                    .filter(line -> !line.trim().isEmpty())
                    .map(Abonnement::fromCsvString)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement des abonnements depuis le fichier: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Erreur de format dans le fichier d'abonnements: " + e.getMessage());
        }
        return abonnements;
    }

    @Override
    public void saveAll(List<Abonnement> abonnements) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Abonnement abonnement : abonnements) {
                writer.write(abonnement.toCsvString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la sauvegarde des abonnements dans le fichier: " + e.getMessage());
        }
    }

    @Override
    public Optional<Abonnement> findById(int id) {
        List<Abonnement> abonnements = findAll();
        if (id >= 0 && id < abonnements.size()) {
            return Optional.of(abonnements.get(id));
        }
        return Optional.empty();
    }

    @Override
    public void save(Abonnement abonnement) {
        List<Abonnement> abonnements = findAll();
        // This method is primarily for adding new subscriptions. 
        // For updates, the list should be modified and then saveAll called.
        // For simplicity in this context, we'll assume 'save' adds if not present, but a real repo would handle IDs.
        // Since we are using list index as ID for now, this method will be less used directly for updates.
        abonnements.add(abonnement);
        saveAll(abonnements);
    }

    @Override
    public void delete(Abonnement abonnement) {
        List<Abonnement> abonnements = findAll();
        abonnements.remove(abonnement);
        saveAll(abonnements);
    }
}

touch src/main/java/com/tonpackage/r