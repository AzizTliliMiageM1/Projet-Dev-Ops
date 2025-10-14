package com.example.abonnement;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class GestionAbonnements {
    private final List<Abonnement> listeAbonnements = new ArrayList<>();
    private final Scanner scanner = new Scanner(System.in);
    private static final String FICHIER_ABONNEMENTS = "abonnements.txt";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    public GestionAbonnements() {
        chargerAbonnements(); // Charger les abonnements au démarrage
    }

    // 🔹 Lecture d'une date
    private LocalDate lireDate(String prompt) {
        while (true) {
            System.out.print(prompt + " (AAAA-MM-JJ ou vide pour ignorer) : ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) return null;
            try {
                return LocalDate.parse(input, DATE_FMT);
            } catch (DateTimeParseException e) {
                System.out.println("❌ Format invalide. Exemple correct : 2025-10-14");
            }
        }
    }

    // 🔹 Lecture d'un double
    private double lireDouble(String prompt) {
        while (true) {
            System.out.print(prompt + " : ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) return -1; // signifie : ne pas modifier
            try {
                double val = Double.parseDouble(input);
                if (val < 0) {
                    System.out.println("❌ La valeur ne peut pas être négative.");
                } else return val;
            } catch (NumberFormatException e) {
                System.out.println("❌ Entrée invalide. Exemple : 9.99");
            }
        }
    }

    // 🔹 Ajouter un abonnement
    public void ajouterAbonnement() {
        System.out.println("\n--- Ajout d'un nouvel abonnement ---");

        System.out.print("Nom du client : ");
        String clientName = scanner.nextLine().trim();

        System.out.print("Nom du service : ");
        String nomService = scanner.nextLine().trim();

        LocalDate dateDebut = lireDate("Date de début");
        LocalDate dateFin;
        do {
            dateFin = lireDate("Date de fin");
            if (dateFin != null && dateDebut != null && dateFin.isBefore(dateDebut)) {
                System.out.println("❌ La date de fin doit être postérieure à la date de début.");
            } else break;
        } while (true);

        double prixMensuel = lireDouble("Prix mensuel (€)");

        Abonnement a = new Abonnement(nomService, dateDebut, dateFin, prixMensuel, clientName, dateDebut);
        listeAbonnements.add(a);
        System.out.println("✅ Abonnement ajouté avec succès !");
        sauvegarderAbonnements();
    }

    // 🔹 Afficher tous les abonnements
    public void afficherTousAbonnements() {
        System.out.println("\n--- Liste des abonnements ---");
        if (listeAbonnements.isEmpty()) {
            System.out.println("Aucun abonnement enregistré.");
        } else {
            for (int i = 0; i < listeAbonnements.size(); i++) {
                System.out.println("Abonnement #" + (i + 1) + " :" + listeAbonnements.get(i));
            }
        }
        System.out.println("-----------------------------------\n");
    }

    // 🔹 Modifier un abonnement
    public void modifierAbonnement() {
        System.out.println("\n--- Modification d'un abonnement ---");
        if (listeAbonnements.isEmpty()) {
            System.out.println("Aucun abonnement à modifier.");
            return;
        }

        afficherTousAbonnements();
        System.out.print("Numéro de l’abonnement à modifier : ");
        int index = lireIndex();
        if (index == -1) return;

        Abonnement a = listeAbonnements.get(index);
        System.out.println("Laissez vide pour conserver la valeur actuelle.");

        System.out.print("Nom du client (" + a.getClientName() + ") : ");
        String newClient = scanner.nextLine().trim();
        if (!newClient.isEmpty()) a.setClientName(newClient);

        System.out.print("Nom du service (" + a.getNomService() + ") : ");
        String newService = scanner.nextLine().trim();
        if (!newService.isEmpty()) a.setNomService(newService);

        LocalDate newDebut = lireDate("Nouvelle date de début (" + a.getDateDebut() + ")");
        if (newDebut != null) a.setDateDebut(newDebut);

        LocalDate newFin = lireDate("Nouvelle date de fin (" + a.getDateFin() + ")");
        if (newFin != null && !newFin.isBefore(a.getDateDebut())) a.setDateFin(newFin);

        double newPrix = lireDouble("Nouveau prix mensuel (" + a.getPrixMensuel() + ")");
        if (newPrix >= 0) a.setPrixMensuel(newPrix);

        System.out.println("✅ Abonnement modifié !");
        sauvegarderAbonnements();
    }

    // 🔹 Supprimer un abonnement
    public void supprimerAbonnement() {
        System.out.println("\n--- Suppression d’un abonnement ---");
        if (listeAbonnements.isEmpty()) {
            System.out.println("Aucun abonnement à supprimer.");
            return;
        }

        afficherTousAbonnements();
        System.out.print("Numéro de l’abonnement à supprimer : ");
        int index = lireIndex();
        if (index == -1) return;

        Abonnement supprime = listeAbonnements.remove(index);
        System.out.println("🗑️ Abonnement supprimé : " + supprime.getNomService() + " pour " + supprime.getClientName());
        sauvegarderAbonnements();
    }

    // 🔹 Recherche
    public void rechercherAbonnement() {
        System.out.println("\n--- Recherche ---");
        System.out.print("Nom du client ou service : ");
        String terme = scanner.nextLine().toLowerCase();

        List<Abonnement> resultats = listeAbonnements.stream()
                .filter(a -> a.getClientName().toLowerCase().contains(terme)
                        || a.getNomService().toLowerCase().contains(terme))
                .collect(Collectors.toList());

        if (resultats.isEmpty()) {
            System.out.println("Aucun résultat trouvé.");
        } else {
            System.out.println("Résultats :");
            resultats.forEach(System.out::println);
        }
    }

    // 🔹 Enregistrer une utilisation
    public void enregistrerUtilisation() {
        System.out.println("\n--- Enregistrer une utilisation ---");
        if (listeAbonnements.isEmpty()) {
            System.out.println("Aucun abonnement disponible.");
            return;
        }

        afficherTousAbonnements();
        System.out.print("Numéro de l’abonnement : ");
        int index = lireIndex();
        if (index == -1) return;

        Abonnement a = listeAbonnements.get(index);
        LocalDate dateUtil = lireDate("Date de la dernière utilisation (vide = aujourd’hui)");
        if (dateUtil == null) dateUtil = LocalDate.now();
        a.setDerniereUtilisation(dateUtil);

        System.out.println("✅ Dernière utilisation enregistrée : " + dateUtil);
        sauvegarderAbonnements();
    }

    // 🔹 Vérifier alertes d’utilisation
    public void verifierAlertesUtilisation() {
        System.out.println("\n--- Alertes d’utilisation ---");
        boolean alerte = false;
        for (Abonnement a : listeAbonnements) {
            if (a.estActif() && a.getDerniereUtilisation() != null) {
                long jours = ChronoUnit.DAYS.between(a.getDerniereUtilisation(), LocalDate.now());
                if (jours > 30) {
                    System.out.printf("⚠️  Alerte pour %s (%s) : non utilisé depuis %d jours.%n",
                            a.getClientName(), a.getNomService(), jours);
                    alerte = true;
                }
            }
        }
        if (!alerte) System.out.println("Aucune alerte détectée.");
    }

    // 🔹 Sauvegarde / chargement
    private void sauvegarderAbonnements() {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(FICHIER_ABONNEMENTS))) {
            for (Abonnement a : listeAbonnements) {
                w.write(a.toCsvString());
                w.newLine();
            }
            System.out.println("💾 Abonnements sauvegardés dans " + FICHIER_ABONNEMENTS);
        } catch (IOException e) {
            System.err.println("❌ Erreur de sauvegarde : " + e.getMessage());
        }
    }

    private void chargerAbonnements() {
        File fichier = new File(FICHIER_ABONNEMENTS);
        if (!fichier.exists()) {
            System.out.println("(Aucun fichier trouvé — démarrage à vide)");
            return;
        }
        try (BufferedReader r = new BufferedReader(new FileReader(fichier))) {
            String ligne;
            while ((ligne = r.readLine()) != null) {
                try {
                    listeAbonnements.add(Abonnement.fromCsvString(ligne));
                } catch (Exception e) {
                    System.err.println("⚠️ Ligne ignorée : " + ligne + " (" + e.getMessage() + ")");
                }
            }
            System.out.println("📂 " + listeAbonnements.size() + " abonnements chargés.");
        } catch (IOException e) {
            System.err.println("❌ Erreur de lecture : " + e.getMessage());
        }
    }

    // 🔹 Lecture sécurisée d’un index d’abonnement
    private int lireIndex() {
        try {
            int i = Integer.parseInt(scanner.nextLine()) - 1;
            if (i < 0 || i >= listeAbonnements.size()) {
                System.out.println("❌ Numéro invalide.");
                return -1;
            }
            return i;
        } catch (NumberFormatException e) {
            System.out.println("❌ Entrée invalide.");
            return -1;
        }
    }

    // 🔹 Menu principal
    public static void main(String[] args) {
        GestionAbonnements app = new GestionAbonnements();
        int choix;
        do {
            System.out.println("""
                    \n===== MENU GESTION ABONNEMENTS =====
                    1. Ajouter un abonnement
                    2. Afficher tous les abonnements
                    3. Modifier un abonnement
                    4. Supprimer un abonnement
                    5. Rechercher un abonnement
                    6. Enregistrer une utilisation
                    7. Vérifier les alertes d'utilisation
                    8. Quitter
                    ====================================
                    """);
            System.out.print("Votre choix : ");
            try {
                choix = Integer.parseInt(app.scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("❌ Choix invalide.");
                continue;
            }

            switch (choix) {
                case 1 -> app.ajouterAbonnement();
                case 2 -> app.afficherTousAbonnements();
                case 3 -> app.modifierAbonnement();
                case 4 -> app.supprimerAbonnement();
                case 5 -> app.rechercherAbonnement();
                case 6 -> app.enregistrerUtilisation();
                case 7 -> app.verifierAlertesUtilisation();
                case 8 -> System.out.println("👋 Fin du programme.");
                default -> System.out.println("❌ Choix invalide.");
            }
        } while (true);
    }
}
