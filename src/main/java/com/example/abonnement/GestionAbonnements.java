    package com.example.abonnement;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GestionAbonnements {
    private List<Abonnement> listeAbonnements;
    private Scanner scanner;

    public GestionAbonnements() {
        this.listeAbonnements = new ArrayList<>();
        this.scanner = new Scanner(System.in);
    }

    // Fonctionnalité 1: Ajouter un nouvel abonnement
    public void ajouterAbonnement() {
        System.out.println("\n--- Ajout d'un nouvel abonnement ---");

        System.out.print("Nom du client: ");
        String clientName = scanner.nextLine();

        System.out.print("Nom du service: ");
        String nomService = scanner.nextLine();

        LocalDate dateDebut = null;
        boolean dateDebutValide = false;
        while (!dateDebutValide) {
            System.out.print("Date de début (AAAA-MM-JJ): ");
            try {
                dateDebut = LocalDate.parse(scanner.nextLine());
                dateDebutValide = true;
            } catch (Exception e) {
                System.out.println("Format de date invalide. Veuillez utiliser AAAA-MM-JJ.");
            }
        }

        LocalDate dateFin = null;
        boolean dateFinValide = false;
        while (!dateFinValide) {
            System.out.print("Date de fin (AAAA-MM-JJ): ");
            try {
                dateFin = LocalDate.parse(scanner.nextLine());
                if (dateFin.isBefore(dateDebut)) {
                    System.out.println("La date de fin ne peut pas être antérieure à la date de début.");
                } else {
                    dateFinValide = true;
                }
            } catch (Exception e) {
                System.out.println("Format de date invalide. Veuillez utiliser AAAA-MM-JJ.");
            }
        }

        double prixMensuel = 0.0;
        boolean prixValide = false;
        while (!prixValide) {
            System.out.print("Prix mensuel: ");
            try {
                prixMensuel = Double.parseDouble(scanner.nextLine());
                if (prixMensuel <= 0) {
                    System.out.println("Le prix doit être un nombre positif.");
                } else {
                    prixValide = true;
                }
            } catch (NumberFormatException e) {
                System.out.println("Format de prix invalide. Veuillez entrer un nombre.");
            }
        }

        Abonnement nouvelAbonnement = new Abonnement(nomService, dateDebut, dateFin, prixMensuel, clientName);
        listeAbonnements.add(nouvelAbonnement);
        System.out.println("Abonnement ajouté avec succès !\n");
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

    // Méthode principale pour exécuter l'application
    public static void main(String[] args) {
        GestionAbonnements app = new GestionAbonnements();
        int choix = 0;

        do {
            System.out.println("\n===== Menu de Gestion des Abonnements =====");
            System.out.println("1. Ajouter un nouvel abonnement");
            System.out.println("2. Afficher tous les abonnements");
            System.out.println("3. Quitter");
            System.out.print("Votre choix: ");
            try {
                choix = Integer.parseInt(app.scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Choix invalide. Veuillez entrer un nombre.");
                choix = 0; // Réinitialiser le choix pour éviter une boucle infinie si l'utilisateur entre du texte
                continue;
            }

            switch (choix) {
                case 1:
                    app.ajouterAbonnement();
                    break;
                case 2:
                    app.afficherTousAbonnements();
                    break;
                case 3:
                    System.out.println("Merci d'avoir utilisé l'application. Au revoir !");
                    break;
                default:
                    System.out.println("Choix invalide. Veuillez réessayer.");
            }
        } while (choix != 3);

        app.scanner.close();
    }
}

