package com.example.abonnement;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class GestionAbonnements {
    private List<Abonnement> listeAbonnements;
    private Scanner scanner;
    private static final String FICHIER_ABONNEMENTS = "abonnements.txt";

    public GestionAbonnements() {
        this.listeAbonnements = new ArrayList<>();
        this.scanner = new Scanner(System.in);
        chargerAbonnements(); // Charger les abonnements au démarrage
    }

    // Méthode utilitaire pour lire une date de l'utilisateur
    private LocalDate lireDate(String prompt) {
        LocalDate date = null;
        boolean valide = false;
        while (!valide) {
            System.out.print(prompt + " (AAAA-MM-JJ): ");
            String input = scanner.nextLine();
            if (input.isEmpty()) {
                return null; // Permet de ne pas modifier la date lors d'une mise à jour
            }
            try {
                date = LocalDate.parse(input);
                valide = true;
            } catch (DateTimeParseException e) {
                System.out.println("Format de date invalide. Veuillez utiliser AAAA-MM-JJ.");
            }
        }
        return date;
    }

    // Méthode utilitaire pour lire un double de l'utilisateur
    private double lireDouble(String prompt) {
        double valeur = 0.0;
        boolean valide = false;
        while (!valide) {
            System.out.print(prompt + ": ");
            String input = scanner.nextLine();
            if (input.isEmpty()) {
                return 0.0; // Permet de ne pas modifier le prix lors d'une mise à jour
            }
            try {
                valeur = Double.parseDouble(input);
                if (valeur < 0) {
                    System.out.println("La valeur ne peut pas être négative.");
                } else {
                    valide = true;
                }
            } catch (NumberFormatException e) {
                System.out.println("Format invalide. Veuillez entrer un nombre.");
            }
        }
        return valeur;
    }

    // Fonctionnalité 1: Ajouter un nouvel abonnement
    public void ajouterAbonnement() {
        System.out.println("\n--- Ajout d'un nouvel abonnement ---");

        System.out.print("Nom du client: ");
        String clientName = scanner.nextLine();

        System.out.print("Nom du service: ");
        String nomService = scanner.nextLine();

        LocalDate dateDebut = lireDate("Date de début");
        LocalDate dateFin = null;
        boolean dateFinValide = false;
        while (!dateFinValide) {
            dateFin = lireDate("Date de fin");
            if (dateFin != null && dateDebut != null && dateFin.isBefore(dateDebut)) {
                System.out.println("La date de fin ne peut pas être antérieure à la date de début.");
            } else {
                dateFinValide = true;
            }
        }

        double prixMensuel = lireDouble("Prix mensuel");

        Abonnement nouvelAbonnement = new Abonnement(nomService, dateDebut, dateFin, prixMensuel, clientName);
        listeAbonnements.add(nouvelAbonnement);
        System.out.println("Abonnement ajouté avec succès !\n");
        sauvegarderAbonnements();
    }

    // Fonctionnalité 2: Afficher tous les abonnements
    public void afficherTousAbonnements() {
        System.out.println("\n--- Liste de tous les abonnements ---");
        if (listeAbonnements.isEmpty()) {
            System.out.println("Aucun abonnement enregistré pour le moment.");
        } else {
            for (int i = 0; i < listeAbonnements.size(); i++) {
                System.out.println("Abonnement N°" + (i + 1) + ":");
                System.out.println(listeAbonnements.get(i));
            }
        }
        System.out.println("-------------------------------------\n");
    }

    // Fonctionnalité 3: Modifier un abonnement
    public void modifierAbonnement() {
        System.out.println("\n--- Modification d'un abonnement ---");
        if (listeAbonnements.isEmpty()) {
            System.out.println("Aucun abonnement à modifier.");
            return;
        }

        afficherTousAbonnements();
        System.out.print("Entrez le numéro de l'abonnement à modifier: ");
        int index = -1;
        try {
            index = Integer.parseInt(scanner.nextLine()) - 1;
        } catch (NumberFormatException e) {
            System.out.println("Entrée invalide. Veuillez entrer un nombre.");
            return;
        }

        if (index >= 0 && index < listeAbonnements.size()) {
            Abonnement abonnement = listeAbonnements.get(index);
            System.out.println("Modification de l'abonnement N°" + (index + 1) + ". Laissez vide pour ne pas changer.");

            System.out.print("Nouveau nom du client (" + abonnement.getClientName() + "): ");
            String newClientName = scanner.nextLine();
            if (!newClientName.isEmpty()) {
                abonnement.setClientName(newClientName);
            }

            System.out.print("Nouveau nom du service (" + abonnement.getNomService() + "): ");
            String newNomService = scanner.nextLine();
            if (!newNomService.isEmpty()) {
                abonnement.setNomService(newNomService);
            }

            LocalDate newDateDebut = lireDate("Nouvelle date de début (" + abonnement.getDateDebut() + ")");
            if (newDateDebut != null) {
                abonnement.setDateDebut(newDateDebut);
            }

            LocalDate newDateFin = lireDate("Nouvelle date de fin (" + abonnement.getDateFin() + ")");
            if (newDateFin != null) {
                if (newDateFin.isBefore(abonnement.getDateDebut())) {
                    System.out.println("La nouvelle date de fin ne peut pas être antérieure à la date de début. Modification annulée.");
                } else {
                    abonnement.setDateFin(newDateFin);
                }
            }

            double newPrixMensuel = lireDouble("Nouveau prix mensuel (" + abonnement.getPrixMensuel() + ")");
            if (newPrixMensuel > 0) {
                abonnement.setPrixMensuel(newPrixMensuel);
            }

            System.out.println("Abonnement modifié avec succès !\n");
            sauvegarderAbonnements();
        } else {
            System.out.println("Numéro d'abonnement invalide.");
        }
    }

    // Fonctionnalité 4: Supprimer un abonnement
    public void supprimerAbonnement() {
        System.out.println("\n--- Suppression d'un abonnement ---");
        if (listeAbonnements.isEmpty()) {
            System.out.println("Aucun abonnement à supprimer.");
            return;
        }

        afficherTousAbonnements();
        System.out.print("Entrez le numéro de l'abonnement à supprimer: ");
        int index = -1;
        try {
            index = Integer.parseInt(scanner.nextLine()) - 1;
        } catch (NumberFormatException e) {
            System.out.println("Entrée invalide. Veuillez entrer un nombre.");
            return;
        }

        if (index >= 0 && index < listeAbonnements.size()) {
            Abonnement abonnementSupprime = listeAbonnements.remove(index);
            System.out.println("Abonnement de " + abonnementSupprime.getClientName() + " pour " + abonnementSupprime.getNomService() + " supprimé avec succès !\n");
            sauvegarderAbonnements();
        } else {
            System.out.println("Numéro d'abonnement invalide.");
        }
    }

    // Fonctionnalité 5: Rechercher un abonnement
    public void rechercherAbonnement() {
        System.out.println("\n--- Recherche d'abonnements ---");
        System.out.print("Entrez le nom du client ou du service à rechercher: ");
        String termeRecherche = scanner.nextLine().toLowerCase();

        List<Abonnement> resultats = listeAbonnements.stream()
                .filter(a -> a.getClientName().toLowerCase().contains(termeRecherche) ||
                             a.getNomService().toLowerCase().contains(termeRecherche))
                .collect(Collectors.toList());

        if (resultats.isEmpty()) {
            System.out.println("Aucun abonnement trouvé pour le terme: " + termeRecherche);
        } else {
            System.out.println("\n--- Résultats de la recherche ---");
            for (int i = 0; i < resultats.size(); i++) {
                System.out.println("Résultat N°" + (i + 1) + ":");
                System.out.println(resultats.get(i));
            }
            System.out.println("-------------------------------------\n");
        }
    }

    // Sauvegarder les abonnements dans un fichier
    private void sauvegarderAbonnements() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FICHIER_ABONNEMENTS))) {
            for (Abonnement abonnement : listeAbonnements) {
                writer.write(abonnement.toCsvString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la sauvegarde des abonnements: " + e.getMessage());
        }
    }

    // Charger les abonnements depuis un fichier
    private void chargerAbonnements() {
        File fichier = new File(FICHIER_ABONNEMENTS);
        if (!fichier.exists()) {
            return; // Pas de fichier, on commence avec une liste vide
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(FICHIER_ABONNEMENTS))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    listeAbonnements.add(Abonnement.fromCsvString(line));
                } catch (Exception e) {
                    System.err.println("Erreur lors de la lecture d'une ligne du fichier de sauvegarde, ligne ignorée: " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement des abonnements: " + e.getMessage());
        }
    }

    // Méthode principale pour exécuter l'application
    public static void main(String[] args) {
        GestionAbonnements app = new GestionAbonnements();
        int choix;

        do {
            System.out.println("\n===== Menu de Gestion des Abonnements =====");
            System.out.println("1. Ajouter un nouvel abonnement");
            System.out.println("2. Afficher tous les abonnements");
            System.out.println("3. Modifier un abonnement");
            System.out.println("4. Supprimer un abonnement");
            System.out.println("5. Rechercher un abonnement");
            System.out.println("6. Quitter");
            System.out.print("Votre choix: ");
            choix = 0; // Initialiser pour éviter les erreurs si l'entrée est invalide
            try {
                choix = Integer.parseInt(app.scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Choix invalide. Veuillez entrer un nombre.");
                continue; // Revenir au début de la boucle
            }

            switch (choix) {
                case 1:
                    app.ajouterAbonnement();
                    break;
                case 2:
                    app.afficherTousAbonnements();
                    break;
                case 3:
                    app.modifierAbonnement();
                    break;
                case 4:
                    app.supprimerAbonnement();
                    break;
                case 5:
                    app.rechercherAbonnement();
                    break;
                case 6:
                    System.out.println("Merci d'avoir utilisé l'application. Au revoir !");
                    break;
                default:
                    System.out.println("Choix invalide. Veuillez réessayer.");
            }
        } while (choix != 6);

        app.scanner.close();
    }
}

