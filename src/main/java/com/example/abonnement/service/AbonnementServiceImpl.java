package com.example.abonnement.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.example.abonnement.Abonnement;

public class AbonnementServiceImpl implements AbonnementService {

    private static final String FICHIER = "abonnements.txt";
    private List<Abonnement> abonnements = new ArrayList<>();

    public AbonnementServiceImpl() {
        chargerDepuisFichier();
    }

    @Override
    public List<Abonnement> listerAbonnements() {
        return abonnements;
    }

    @Override
    public Abonnement trouverParId(String id) {
        return abonnements.stream()
                .filter(a -> a.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void ajouterAbonnement(Abonnement abonnement) {
        if (abonnement.getId() == null || abonnement.getId().isBlank()) {
            abonnement.setId(UUID.randomUUID().toString());
        }
        abonnements.add(abonnement);
        sauvegarderDansFichier();
    }

    @Override
    public boolean modifierAbonnement(Abonnement maj) {
        for (int i = 0; i < abonnements.size(); i++) {
            if (abonnements.get(i).getId().equals(maj.getId())) {
                abonnements.set(i, maj);
                sauvegarderDansFichier();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean supprimerAbonnement(String id) {
        boolean removed = abonnements.removeIf(a -> a.getId().equals(id));
        if (removed) {
            sauvegarderDansFichier();
        }
        return removed;
    }

    private void sauvegarderDansFichier() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FICHIER))) {
            for (Abonnement a : abonnements) {
                writer.write(a.toCsvString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Erreur sauvegarde: " + e.getMessage());
        }
    }

    private void chargerDepuisFichier() {
        File f = new File(FICHIER);
        if (!f.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(FICHIER))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    abonnements.add(Abonnement.fromCsvString(line));
                } catch (Exception e) {
                    System.err.println("Ligne ignorée: " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur chargement: " + e.getMessage());
        }
    }
}
