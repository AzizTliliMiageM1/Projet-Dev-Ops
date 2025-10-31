package com.example.abonnement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;

/**
 * Classe principale de l'application. Gère le menu console et les actions.
 * Commentaires écrits de manière simple, comme dans un projet étudiant.
 */
public class GestionAbonnements {
    private List<Abonnement> listeAbonnements;
    private Scanner scanner;
    private static final String FICHIER_ABONNEMENTS = "abonnements.txt";

    // Jackson mapper pour export/import JSON
    private final ObjectMapper objectMapper;

    // Couleurs ANSI simples pour améliorer l'interface console
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_CYAN = "\u001B[36m";

    public GestionAbonnements() {
        this.listeAbonnements = new ArrayList<>();
        this.scanner = new Scanner(System.in);
        // Préparer mapper JSON pour LocalDate
        this.objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

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

        // Lors de l'ajout, la dernière utilisation est par défaut la date de début
        Abonnement nouvelAbonnement = new Abonnement(nomService, dateDebut, dateFin, prixMensuel, clientName, dateDebut);
        listeAbonnements.add(nouvelAbonnement);
        System.out.println("Abonnement ajouté avec succès !\n");
        sauvegarderAbonnements();
    }

    // Fonctionnalité 2: Afficher tous les abonnements
    public void afficherTousAbonnements() {
        System.out.println("\n" + ANSI_CYAN + "=== Liste de tous les abonnements ===" + ANSI_RESET);
        if (listeAbonnements.isEmpty()) {
            System.out.println(ANSI_YELLOW + "Aucun abonnement enregistré pour le moment." + ANSI_RESET);
        } else {
            // Affichage sous forme de tableau simple
            System.out.printf(ANSI_GREEN + "%-4s | %-20s | %-10s | %-10s | %-8s | %-12s" + ANSI_RESET + "\n",
                    "#", "Client", "Service", "Début", "Fin", "Prix/mois");
            System.out.println("----+----------------------+------------+------------+----------+-------------");
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            for (int i = 0; i < listeAbonnements.size(); i++) {
                Abonnement a = listeAbonnements.get(i);
                String debut = a.getDateDebut() != null ? a.getDateDebut().format(fmt) : "-";
                String fin = a.getDateFin() != null ? a.getDateFin().format(fmt) : "-";
                System.out.printf("%-4d | %-20s | %-10s | %-10s | %-8s | %-12s\n",
                        (i + 1), truncate(a.getClientName(), 20), truncate(a.getNomService(), 10), debut, fin, String.format("%.2f€", a.getPrixMensuel()));
            }
        }
        System.out.println(ANSI_CYAN + "======================================" + ANSI_RESET + "\n");
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

    // Petite utilitaire pour tronquer des champs trop longs
    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 3) + "...";
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
            Abonnement abonnementSupprime = listeAbonnements.get(index);
            // Confirmation avant suppression — bonne pratique pour éviter les erreurs
            System.out.print("Confirmer la suppression de l'abonnement de " + abonnementSupprime.getClientName() + " pour " + abonnementSupprime.getNomService() + " ? (o/N): ");
            String confirm = scanner.nextLine().trim().toLowerCase();
            if (confirm.equals("o") || confirm.equals("oui")) {
                listeAbonnements.remove(index);
                System.out.println("Abonnement supprimé avec succès !\n");
                sauvegarderAbonnements();
            } else {
                System.out.println("Suppression annulée.");
            }
        } else {
            System.out.println("Numéro d'abonnement invalide.");
        }
    }

    // Exporter la liste en JSON
    public void exporterJson() {
        System.out.print("Chemin du fichier JSON (par défaut abonnements.json): ");
        String path = scanner.nextLine().trim();
        if (path.isEmpty()) path = "abonnements.json";
        try (Writer writer = new FileWriter(path)) {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(writer, listeAbonnements);
            System.out.println("Export JSON effectué dans : " + path);
        } catch (IOException e) {
            System.out.println("Erreur lors de l'export JSON: " + e.getMessage());
        }
    }

    // Importer depuis un fichier JSON (ajoute les abonnements au list)
    public void importerJson() {
        System.out.print("Chemin du fichier JSON à importer: ");
        String path = scanner.nextLine().trim();
        if (path.isEmpty()) {
            System.out.println("Aucun fichier fourni.");
            return;
        }
        try (Reader reader = new FileReader(path)) {
            Abonnement[] arr = objectMapper.readValue(reader, Abonnement[].class);
            for (Abonnement a : arr) {
                listeAbonnements.add(a);
            }
            sauvegarderAbonnements();
            System.out.println("Import JSON réussi. " + arr.length + " abonnements ajoutés.");
        } catch (IOException e) {
            System.out.println("Erreur lors de l'import JSON: " + e.getMessage());
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

    // Nouvelle Fonctionnalité 6: Enregistrer une utilisation pour un abonnement
    public void enregistrerUtilisation() {
        System.out.println("\n--- Enregistrer une utilisation ---");
        if (listeAbonnements.isEmpty()) {
            System.out.println("Aucun abonnement enregistré.");
            return;
        }

        afficherTousAbonnements();
        System.out.print("Entrez le numéro de l'abonnement pour lequel enregistrer l'utilisation: ");
        int index = -1;
        try {
            index = Integer.parseInt(scanner.nextLine()) - 1;
        } catch (NumberFormatException e) {
            System.out.println("Entrée invalide. Veuillez entrer un nombre.");
            return;
        }

        if (index >= 0 && index < listeAbonnements.size()) {
            Abonnement abonnement = listeAbonnements.get(index);
            LocalDate dateUtilisation = lireDate("Date de la dernière utilisation (laisser vide pour aujourd'hui)");
            if (dateUtilisation == null) {
                dateUtilisation = LocalDate.now();
            }
            abonnement.setDerniereUtilisation(dateUtilisation);
            System.out.println("Utilisation enregistrée pour l'abonnement de " + abonnement.getClientName() + " le " + dateUtilisation + "\n");
            sauvegarderAbonnements();
        } else {
            System.out.println("Numéro d'abonnement invalide.");
        }
    }

    // Nouvelle Fonctionnalité 7: Vérifier les alertes d'utilisation
    public void verifierAlertesUtilisation() {
        System.out.println("\n--- Vérification des alertes d'utilisation ---");
        if (listeAbonnements.isEmpty()) {
            System.out.println("Aucun abonnement enregistré.");
            return;
        }

        boolean alerteTrouvee = false;
        for (Abonnement abonnement : listeAbonnements) {
            if (abonnement.estActif() && abonnement.getDerniereUtilisation() != null) {
                long joursDepuisUtilisation = ChronoUnit.DAYS.between(abonnement.getDerniereUtilisation(), LocalDate.now());
                // Exemple d'alerte: si pas utilisé depuis plus de 30 jours
                if (joursDepuisUtilisation > 30) {
                    System.out.println("ALERTE pour " + abonnement.getClientName() + " (Service: " + abonnement.getNomService() + "):");
                    System.out.println("  - Abonnement actif mais non utilisé depuis " + joursDepuisUtilisation + " jours.");
                    System.out.println("  - Dernière utilisation: " + abonnement.getDerniereUtilisation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    System.out.println("  - Coût mensuel: " + String.format("%.2f€", abonnement.getPrixMensuel()));
                    System.out.println("  - Pensez à l'utiliser ou à le résilier pour éviter des frais inutiles !\n");
                    alerteTrouvee = true;
                }
            }
        }

        if (!alerteTrouvee) {
            System.out.println("Aucune alerte d'utilisation trouvée pour les abonnements actifs.\n");
        }
        System.out.println("-------------------------------------\n");
    }

    // Sauvegarder les abonnements dans un fichier
    private void sauvegarderAbonnements() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FICHIER_ABONNEMENTS))) {
            for (Abonnement abonnement : listeAbonnements) {
                writer.write(abonnement.toCsvString());
                writer.newLine();
            }
            System.out.println("Abonnements sauvegardés dans " + FICHIER_ABONNEMENTS);
        } catch (IOException e) {
            System.err.println("Erreur lors de la sauvegarde des abonnements: " + e.getMessage());
        }
    }

    // Charger les abonnements depuis un fichier
    private void chargerAbonnements() {
        File fichier = new File(FICHIER_ABONNEMENTS);
        if (!fichier.exists()) {
            System.out.println("Fichier de sauvegarde non trouvé. Démarrage avec une liste vide.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(FICHIER_ABONNEMENTS))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    listeAbonnements.add(Abonnement.fromCsvString(line));
                } catch (Exception e) { // Catch toutes les exceptions pour une robustesse accrue lors du chargement
                    System.err.println("Erreur lors de la lecture d'une ligne du fichier de sauvegarde, ligne ignorée: " + line + " - " + e.getMessage());
                }
            }
            System.out.println(listeAbonnements.size() + " abonnements chargés depuis " + FICHIER_ABONNEMENTS);
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement des abonnements: " + e.getMessage());
        }
    }

    // Méthode principale pour exécuter l'application
    public static void main(String[] args) {
        GestionAbonnements app = new GestionAbonnements();
        int choix;

    // Boucle principale du menu. On sort avec l'option 10 (Quitter).
    do {
            System.out.println("\n===== Menu de Gestion des Abonnements =====");
            System.out.println("1. Ajouter un nouvel abonnement");
            System.out.println("2. Afficher tous les abonnements");
            System.out.println("3. Modifier un abonnement");
            System.out.println("4. Supprimer un abonnement");
            System.out.println("5. Rechercher un abonnement");
            System.out.println("6. Enregistrer une utilisation");
            System.out.println("7. Vérifier les alertes d'utilisation");
            System.out.println("8. Exporter en JSON");
            System.out.println("9. Importer depuis JSON");
            System.out.println("10. Quitter");
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
                    app.enregistrerUtilisation();
                    break;
                case 7:
                    app.verifierAlertesUtilisation();
                    break;
                case 8:
                    app.exporterJson();
                    break;
                case 9:
                    app.importerJson();
                    break;
                case 10:
                    System.out.println("Merci d'avoir utilisé l'application. Au revoir !");
                    break;
                default:
                    System.out.println("Choix invalide. Veuillez réessayer.");
            }
    } while (choix != 10);

        app.scanner.close();
    }
}

