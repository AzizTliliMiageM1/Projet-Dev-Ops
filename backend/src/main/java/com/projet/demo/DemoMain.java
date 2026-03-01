package com.projet.demo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import com.projet.backend.domain.Abonnement;
import com.projet.repository.AbonnementRepository;
import com.projet.repository.FileAbonnementRepository;

/**
 * Classe Main de démonstration avec code métier
 * 
 * Cette classe illustre comment utiliser le système de gestion d'abonnements
 * avec toutes les nouvelles fonctionnalités (tags, groupes, priorités, partage, etc.)
 * 
 * Utilisation : mvn exec:java -Dexec.mainClass=com.projet.demo.DemoMain
 */
public class DemoMain {
    
    private static final Scanner scanner = new Scanner(System.in);
    private static final AbonnementRepository repo = new FileAbonnementRepository("demo_abonnements.txt");
    
    // Couleurs ANSI pour l'affichage console
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String PURPLE = "\u001B[35m";
    private static final String CYAN = "\u001B[36m";
    
    public static void main(String[] args) {
        afficherBanniere();
        
        boolean continuer = true;
        while (continuer) {
            afficherMenu();
            int choix = lireChoix();
            
            switch (choix) {
                case 1 -> creerAbonnementsDemo();
                case 2 -> afficherTousLesAbonnements();
                case 3 -> analyserParPriorite();
                case 4 -> analyserParGroupe();
                case 5 -> afficherAbonnementsPartages();
                case 6 -> analyserROI();
                case 7 -> afficherRappels();
                case 8 -> calculerEconomiesPartage();
                case 9 -> analyserFrequencesPaiement();
                case 10 -> rechercherParTag();
                case 11 -> afficherStatistiquesCompletes();
                case 0 -> {
                    System.out.println(GREEN + "\n✅ Au revoir !" + RESET);
                    continuer = false;
                }
                default -> System.out.println(RED + "❌ Choix invalide" + RESET);
            }
        }
        
        scanner.close();
    }
    
    private static void afficherBanniere() {
        System.out.println(CYAN + """
        ╔══════════════════════════════════════════════════════════════╗
        ║                                                              ║
        ║   🎯  GESTIONNAIRE D'ABONNEMENTS - VERSION ENRICHIE  🎯     ║
        ║                                                              ║
        ║   Démonstration du code métier et des nouvelles             ║
        ║   fonctionnalités : tags, groupes, priorités, partage       ║
        ║                                                              ║
        ╚══════════════════════════════════════════════════════════════╝
        """ + RESET);
    }
    
    private static void afficherMenu() {
        System.out.println(YELLOW + "\n━━━━━━━━━━━━━━━━━━ MENU PRINCIPAL ━━━━━━━━━━━━━━━━━━" + RESET);
        System.out.println(CYAN + " 1." + RESET + "  🎬 Créer des abonnements de démonstration");
        System.out.println(CYAN + " 2." + RESET + "  📋 Afficher tous les abonnements");
        System.out.println(CYAN + " 3." + RESET + "  🎯 Analyser par priorité");
        System.out.println(CYAN + " 4." + RESET + "  📦 Analyser par groupe");
        System.out.println(CYAN + " 5." + RESET + "  👥 Afficher abonnements partagés");
        System.out.println(CYAN + " 6." + RESET + "  📈 Analyser ROI (retour sur investissement)");
        System.out.println(CYAN + " 7." + RESET + "  🔔 Afficher rappels d'expiration");
        System.out.println(CYAN + " 8." + RESET + "  💰 Calculer économies du partage");
        System.out.println(CYAN + " 9." + RESET + "  📊 Analyser fréquences de paiement");
        System.out.println(CYAN + "10." + RESET + "  🏷️  Rechercher par tag");
        System.out.println(CYAN + "11." + RESET + "  📊 Statistiques complètes");
        System.out.println(CYAN + " 0." + RESET + "  🚪 Quitter");
        System.out.println(YELLOW + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" + RESET);
        System.out.print(GREEN + "Votre choix : " + RESET);
    }
    
    private static int lireChoix() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    /**
     * CODE MÉTIER 1 : Création d'abonnements avec toutes les fonctionnalités
     */
    private static void creerAbonnementsDemo() {
        System.out.println(PURPLE + "\n🎬 Création d'abonnements de démonstration..." + RESET);
        
        List<Abonnement> abonnements = new ArrayList<>();
        
        // 1. Netflix - Partagé en famille (Pack Streaming)
        Abonnement netflix = new Abonnement(
            null,                                           // id (auto-généré)
            "Netflix Premium",                              // nomService
            LocalDate.now().minusMonths(6),                // dateDebut
            LocalDate.now().plusMonths(6),                 // dateFin
            17.99,                                          // prixMensuel
            "demo@email.com",                              // clientName
            LocalDate.now().minusDays(2),                  // derniereUtilisation
            "Streaming",                                    // categorie
            Arrays.asList("Famille", "Essentiel", "Divertissement"), // tags
            "Pack Streaming",                               // groupeAbonnement
            "Important",                                    // priorite
            "Compte familial partagé - Login : famille@netflix.com", // notes
            4,                                              // nombreUtilisateurs
            true,                                           // partage
            7,                                              // joursRappelAvantFin
            "Mensuel"                                       // frequencePaiement
        );
        abonnements.add(netflix);
        
        // 2. Spotify - Partagé (Pack Streaming)
        Abonnement spotify = new Abonnement(
            null,
            "Spotify Family",
            LocalDate.now().minusMonths(12),
            LocalDate.now().plusMonths(12),
            15.99,
            "demo@email.com",
            LocalDate.now().minusDays(1),
            "Musique",
            Arrays.asList("Famille", "Musique", "Quotidien"),
            "Pack Streaming",
            "Important",
            "Premium Family - 6 comptes",
            6,
            true,
            14,
            "Mensuel"
        );
        abonnements.add(spotify);
        
        // 3. Office 365 - Professionnel (Suite Microsoft)
        Abonnement office = new Abonnement(
            null,
            "Microsoft 365 Business",
            LocalDate.now().minusMonths(3),
            LocalDate.now().plusMonths(9),
            12.50,
            "demo@email.com",
            LocalDate.now(),
            "Productivité",
            Arrays.asList("Travail", "Essentiel", "Productivité"),
            "Suite Microsoft",
            "Essentiel",
            "Inclut Word, Excel, PowerPoint, OneDrive 1TB",
            1,
            false,
            30,
            "Annuel"
        );
        abonnements.add(office);
        
        // 4. Salle de sport - Individuel, peu utilisé
        Abonnement sport = new Abonnement(
            null,
            "Basic Fit",
            LocalDate.now().minusMonths(18),
            LocalDate.now().plusMonths(6),
            19.99,
            "demo@email.com",
            LocalDate.now().minusDays(95),
            "Sport & Santé",
            Arrays.asList("Santé", "Sport"),
            null,
            "Optionnel",
            "Formule Classic - Tous les clubs",
            1,
            false,
            7,
            "Mensuel"
        );
        abonnements.add(sport);
        
        // 5. Adobe Creative Cloud - Trimestriel
        Abonnement adobe = new Abonnement(
            null,
            "Adobe Creative Cloud",
            LocalDate.now().minusMonths(9),
            LocalDate.now().plusMonths(3),
            59.99,
            "demo@email.com",
            LocalDate.now().minusDays(5),
            "Créativité",
            Arrays.asList("Travail", "Création", "Professionnel"),
            "Suite Adobe",
            "Important",
            "Photoshop + Lightroom + Illustrator",
            1,
            false,
            14,
            "Trimestriel"
        );
        abonnements.add(adobe);
        
        // 6. Disney+ - Luxe, partagé
        Abonnement disney = new Abonnement(
            null,
            "Disney+",
            LocalDate.now().minusMonths(2),
            LocalDate.now().plusDays(15),
            8.99,
            "demo@email.com",
            LocalDate.now().minusDays(45),
            "Streaming",
            Arrays.asList("Famille", "Enfants", "Divertissement"),
            "Pack Streaming",
            "Luxe",
            "Partagé avec les cousins",
            2,
            true,
            7,
            "Mensuel"
        );
        abonnements.add(disney);
        
        // 7. Amazon Prime - Semestriel
        Abonnement prime = new Abonnement(
            null,
            "Amazon Prime",
            LocalDate.now().minusMonths(4),
            LocalDate.now().plusMonths(2),
            49.00,
            "demo@email.com",
            LocalDate.now().minusDays(3),
            "E-commerce",
            Arrays.asList("Shopping", "Livraison", "Vidéo"),
            null,
            "Important",
            "Livraison gratuite + Prime Video inclus",
            1,
            false,
            15,
            "Semestriel"
        );
        abonnements.add(prime);
        
        // 8. iCloud - Stockage
        Abonnement icloud = new Abonnement(
            null,
            "iCloud+ 200GB",
            LocalDate.now().minusYears(1),
            LocalDate.now().plusMonths(12),
            2.99,
            "demo@email.com",
            LocalDate.now().minusDays(10),
            "Stockage",
            Arrays.asList("Stockage", "Photos", "Sauvegarde"),
            null,
            "Essentiel",
            "Sauvegarde automatique iPhone + iPad",
            1,
            false,
            7,
            "Mensuel"
        );
        abonnements.add(icloud);
        
        // Sauvegarder tous les abonnements
        for (Abonnement abo : abonnements) {
            repo.save(abo);
        }
        
        System.out.println(GREEN + "✅ " + abonnements.size() + " abonnements créés avec succès !" + RESET);
        System.out.println(CYAN + "   - 3 abonnements partagés (Netflix, Spotify, Disney+)" + RESET);
        System.out.println(CYAN + "   - 2 packs/groupes (Pack Streaming, Suite Microsoft/Adobe)" + RESET);
        System.out.println(CYAN + "   - 4 fréquences différentes (Mensuel, Trimestriel, Semestriel, Annuel)" + RESET);
        System.out.println(CYAN + "   - Tags multiples pour organisation" + RESET);
    }
    
    /**
     * CODE MÉTIER 2 : Affichage enrichi des abonnements
     */
    private static void afficherTousLesAbonnements() {
        List<Abonnement> abonnements = repo.findAll();
        
        if (abonnements.isEmpty()) {
            System.out.println(YELLOW + "\n⚠️  Aucun abonnement trouvé. Créez d'abord des abonnements de démo (option 1)" + RESET);
            return;
        }
        
        System.out.println(PURPLE + "\n📋 LISTE COMPLÈTE DES ABONNEMENTS" + RESET);
        System.out.println(YELLOW + "═".repeat(120) + RESET);
        
        for (int i = 0; i < abonnements.size(); i++) {
            Abonnement abo = abonnements.get(i);
            
            System.out.println(CYAN + "\n[" + (i+1) + "] " + abo.getNomService() + RESET);
            System.out.println("    " + abo.getPrioriteAvecEmoji());
            
            if (!abo.getTags().isEmpty()) {
                System.out.println("    🏷️  Tags : " + String.join(", ", abo.getTags()));
            }
            
            if (abo.estGroupe()) {
                System.out.println("    📦 Groupe : " + abo.getGroupeAbonnement());
            }
            
            System.out.println("    💰 Prix : " + String.format("%.2f€/%s", abo.getPrixMensuel(), abo.getFrequencePaiement()));
            System.out.println("    📊 Coût annuel estimé : " + String.format("%.2f€", abo.getCoutAnnuelEstime()));
            
            if (abo.isPartage()) {
                System.out.println("    👥 Partagé avec " + abo.getNombreUtilisateurs() + " personnes → " 
                    + String.format("%.2f€/personne", abo.getCoutParPersonne()));
            }
            
            System.out.println("    📈 ROI : " + abo.getROI());
            System.out.println("    📅 Prochaine échéance : " + abo.getProchaineEcheance() 
                + " (dans " + abo.getJoursAvantExpiration() + " jours)");
            
            if (abo.doitEnvoyerRappel()) {
                System.out.println(RED + "    🔔 RAPPEL : Expiration proche !" + RESET);
            }
            
            if (abo.getNotes() != null && !abo.getNotes().isBlank()) {
                System.out.println("    📝 Notes : " + abo.getNotes());
            }
            
            System.out.println(YELLOW + "    " + "─".repeat(100) + RESET);
        }
        
        System.out.println(GREEN + "\n✅ Total : " + abonnements.size() + " abonnements" + RESET);
    }
    
    /**
     * CODE MÉTIER 3 : Analyse par priorité
     */
    private static void analyserParPriorite() {
        List<Abonnement> abonnements = repo.findAll();
        
        if (abonnements.isEmpty()) {
            System.out.println(YELLOW + "\n⚠️  Aucun abonnement à analyser" + RESET);
            return;
        }
        
        System.out.println(PURPLE + "\n🎯 ANALYSE PAR PRIORITÉ" + RESET);
        System.out.println(YELLOW + "═".repeat(100) + RESET);
        
        String[] priorites = {"Essentiel", "Important", "Optionnel", "Luxe"};
        
        for (String priorite : priorites) {
            List<Abonnement> parPriorite = abonnements.stream()
                .filter(a -> a.getPriorite().equals(priorite))
                .toList();
            
            if (!parPriorite.isEmpty()) {
                double coutTotal = parPriorite.stream()
                    .mapToDouble(Abonnement::getCoutAnnuelEstime)
                    .sum();
                
                System.out.println("\n" + parPriorite.get(0).getPrioriteAvecEmoji() + " (" + parPriorite.size() + " abonnements)");
                System.out.println("   Coût annuel total : " + String.format("%.2f€", coutTotal));
                System.out.println("   Services : " + parPriorite.stream()
                    .map(Abonnement::getNomService)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse(""));
            }
        }
        
        double coutTotalGeneral = abonnements.stream()
            .mapToDouble(Abonnement::getCoutAnnuelEstime)
            .sum();
        
        System.out.println(GREEN + "\n💰 Coût annuel total : " + String.format("%.2f€", coutTotalGeneral) + RESET);
    }
    
    /**
     * CODE MÉTIER 4 : Analyse par groupe
     */
    private static void analyserParGroupe() {
        List<Abonnement> abonnements = repo.findAll();
        
        List<Abonnement> groupes = abonnements.stream()
            .filter(Abonnement::estGroupe)
            .toList();
        
        if (groupes.isEmpty()) {
            System.out.println(YELLOW + "\n⚠️  Aucun abonnement groupé trouvé" + RESET);
            return;
        }
        
        System.out.println(PURPLE + "\n📦 ANALYSE PAR GROUPE" + RESET);
        System.out.println(YELLOW + "═".repeat(100) + RESET);
        
        groupes.stream()
            .map(Abonnement::getGroupeAbonnement)
            .distinct()
            .forEach(groupe -> {
                List<Abonnement> membresDuGroupe = abonnements.stream()
                    .filter(a -> groupe.equals(a.getGroupeAbonnement()))
                    .toList();
                
                double coutGroupe = membresDuGroupe.stream()
                    .mapToDouble(Abonnement::getCoutAnnuelEstime)
                    .sum();
                
                System.out.println("\n📦 " + groupe + " (" + membresDuGroupe.size() + " abonnements)");
                System.out.println("   Coût annuel du pack : " + String.format("%.2f€", coutGroupe));
                membresDuGroupe.forEach(abo -> 
                    System.out.println("   • " + abo.getNomService() + " - " 
                        + String.format("%.2f€/an", abo.getCoutAnnuelEstime()))
                );
            });
    }
    
    /**
     * CODE MÉTIER 5 : Affichage abonnements partagés
     */
    private static void afficherAbonnementsPartages() {
        List<Abonnement> partages = repo.findAll().stream()
            .filter(Abonnement::isPartage)
            .toList();
        
        if (partages.isEmpty()) {
            System.out.println(YELLOW + "\n⚠️  Aucun abonnement partagé trouvé" + RESET);
            return;
        }
        
        System.out.println(PURPLE + "\n👥 ABONNEMENTS PARTAGÉS" + RESET);
        System.out.println(YELLOW + "═".repeat(100) + RESET);
        
        partages.forEach(abo -> {
            System.out.println("\n• " + abo.getNomService());
            System.out.println("  Prix total : " + String.format("%.2f€/%s", abo.getPrixMensuel(), abo.getFrequencePaiement()));
            System.out.println("  Partagé avec : " + abo.getNombreUtilisateurs() + " personnes");
            System.out.println("  Coût par personne : " + String.format("%.2f€", abo.getCoutParPersonne()));
            System.out.println("  Économie individuelle : " + 
                String.format("%.2f€", abo.getPrixMensuel() - abo.getCoutParPersonne()));
        });
    }
    
    /**
     * CODE MÉTIER 6 : Analyse ROI
     */
    private static void analyserROI() {
        List<Abonnement> abonnements = repo.findAll();
        
        if (abonnements.isEmpty()) {
            System.out.println(YELLOW + "\n⚠️  Aucun abonnement à analyser" + RESET);
            return;
        }
        
        System.out.println(PURPLE + "\n📈 ANALYSE ROI (Retour sur Investissement)" + RESET);
        System.out.println(YELLOW + "═".repeat(100) + RESET);
        
        String[] niveauxROI = {"Excellent 🌟", "Bon ✅", "Moyen ⚠️", "Faible ⛔"};
        
        for (String niveau : niveauxROI) {
            List<Abonnement> parROI = abonnements.stream()
                .filter(a -> a.getROI().equals(niveau))
                .toList();
            
            if (!parROI.isEmpty()) {
                System.out.println("\n" + niveau + " (" + parROI.size() + " abonnements)");
                parROI.forEach(abo -> {
                    long joursDepuisUtilisation = abo.getDerniereUtilisation() != null 
                        ? java.time.temporal.ChronoUnit.DAYS.between(abo.getDerniereUtilisation(), LocalDate.now())
                        : 999;
                    System.out.println("  • " + abo.getNomService() 
                        + " - Dernière utilisation il y a " + joursDepuisUtilisation + " jours");
                });
            }
        }
        
        // Alertes pour abonnements inutilisés
        long nbFaibleROI = abonnements.stream()
            .filter(a -> a.getROI().contains("⛔"))
            .count();
        
        if (nbFaibleROI > 0) {
            double coutGaspille = abonnements.stream()
                .filter(a -> a.getROI().contains("⛔"))
                .mapToDouble(Abonnement::getCoutAnnuelEstime)
                .sum();
            
            System.out.println(RED + "\n⚠️  ALERTE : " + nbFaibleROI + " abonnement(s) peu utilisé(s)" + RESET);
            System.out.println(RED + "   Potentiel d'économie : " + String.format("%.2f€/an", coutGaspille) + RESET);
        }
    }
    
    /**
     * CODE MÉTIER 7 : Rappels d'expiration
     */
    private static void afficherRappels() {
        List<Abonnement> rappels = repo.findAll().stream()
            .filter(Abonnement::doitEnvoyerRappel)
            .toList();
        
        if (rappels.isEmpty()) {
            System.out.println(GREEN + "\n✅ Aucun rappel aujourd'hui" + RESET);
            return;
        }
        
        System.out.println(RED + "\n🔔 RAPPELS D'EXPIRATION" + RESET);
        System.out.println(YELLOW + "═".repeat(100) + RESET);
        
        rappels.forEach(abo -> {
            long joursRestants = abo.getJoursAvantExpiration();
            String urgence = joursRestants <= 3 ? RED : YELLOW;
            
            System.out.println(urgence + "\n⚠️  " + abo.getNomService() + RESET);
            System.out.println("   Expire dans : " + joursRestants + " jours (le " + abo.getDateFin() + ")");
            System.out.println("   Prix : " + String.format("%.2f€/%s", abo.getPrixMensuel(), abo.getFrequencePaiement()));
            System.out.println("   Priorité : " + abo.getPrioriteAvecEmoji());
        });
        
        System.out.println(YELLOW + "\n💡 Pensez à renouveler ou annuler ces abonnements !" + RESET);
    }
    
    /**
     * CODE MÉTIER 8 : Calcul économies du partage
     */
    private static void calculerEconomiesPartage() {
        List<Abonnement> partages = repo.findAll().stream()
            .filter(Abonnement::isPartage)
            .filter(a -> a.getNombreUtilisateurs() > 1)
            .toList();
        
        if (partages.isEmpty()) {
            System.out.println(YELLOW + "\n⚠️  Aucun abonnement partagé trouvé" + RESET);
            return;
        }
        
        System.out.println(PURPLE + "\n💰 ÉCONOMIES GRÂCE AU PARTAGE" + RESET);
        System.out.println(YELLOW + "═".repeat(100) + RESET);
        
        double economiesAnnuelles = 0;
        
        for (Abonnement abo : partages) {
            double economieParMois = abo.getPrixMensuel() - abo.getCoutParPersonne();
            double economieAnnuelle = economieParMois * 12;
            economiesAnnuelles += economieAnnuelle;
            
            System.out.println("\n• " + abo.getNomService());
            System.out.println("  Prix solo : " + String.format("%.2f€/mois", abo.getPrixMensuel()));
            System.out.println("  Prix partagé : " + String.format("%.2f€/mois", abo.getCoutParPersonne()) 
                + " (divisé par " + abo.getNombreUtilisateurs() + ")");
            System.out.println(GREEN + "  Économie : " + String.format("%.2f€/mois", economieParMois) 
                + " → " + String.format("%.2f€/an", economieAnnuelle) + RESET);
        }
        
        System.out.println(GREEN + "\n🎉 TOTAL ÉCONOMIES ANNUELLES : " 
            + String.format("%.2f€", economiesAnnuelles) + RESET);
    }
    
    /**
     * CODE MÉTIER 9 : Analyse fréquences de paiement
     */
    private static void analyserFrequencesPaiement() {
        List<Abonnement> abonnements = repo.findAll();
        
        if (abonnements.isEmpty()) {
            System.out.println(YELLOW + "\n⚠️  Aucun abonnement à analyser" + RESET);
            return;
        }
        
        System.out.println(PURPLE + "\n📊 ANALYSE DES FRÉQUENCES DE PAIEMENT" + RESET);
        System.out.println(YELLOW + "═".repeat(100) + RESET);
        
        String[] frequences = {"Mensuel", "Trimestriel", "Semestriel", "Annuel"};
        
        for (String freq : frequences) {
            List<Abonnement> parFreq = abonnements.stream()
                .filter(a -> a.getFrequencePaiement().equals(freq))
                .toList();
            
            if (!parFreq.isEmpty()) {
                double coutTotal = parFreq.stream()
                    .mapToDouble(Abonnement::getCoutAnnuelEstime)
                    .sum();
                
                System.out.println("\n" + freq + " (" + parFreq.size() + " abonnements)");
                System.out.println("   Coût annuel cumulé : " + String.format("%.2f€", coutTotal));
                parFreq.forEach(abo -> 
                    System.out.println("   • " + abo.getNomService() + " - " 
                        + String.format("%.2f€/%s", abo.getPrixMensuel(), freq))
                );
            }
        }
    }
    
    /**
     * CODE MÉTIER 10 : Recherche par tag
     */
    private static void rechercherParTag() {
        List<Abonnement> abonnements = repo.findAll();
        
        if (abonnements.isEmpty()) {
            System.out.println(YELLOW + "\n⚠️  Aucun abonnement trouvé" + RESET);
            return;
        }
        
        // Lister tous les tags disponibles
        List<String> tousTags = abonnements.stream()
            .flatMap(a -> a.getTags().stream())
            .distinct()
            .sorted()
            .toList();
        
        if (tousTags.isEmpty()) {
            System.out.println(YELLOW + "\n⚠️  Aucun tag trouvé" + RESET);
            return;
        }
        
        System.out.println(PURPLE + "\n🏷️  RECHERCHE PAR TAG" + RESET);
        System.out.println(YELLOW + "═".repeat(100) + RESET);
        System.out.println("\nTags disponibles : " + String.join(", ", tousTags));
        System.out.print(GREEN + "\nEntrez un tag à rechercher : " + RESET);
        
        String tag = scanner.nextLine().trim();
        
        List<Abonnement> resultats = abonnements.stream()
            .filter(a -> a.getTags().stream()
                .anyMatch(t -> t.toLowerCase().contains(tag.toLowerCase())))
            .toList();
        
        if (resultats.isEmpty()) {
            System.out.println(YELLOW + "\n⚠️  Aucun abonnement trouvé avec le tag '" + tag + "'" + RESET);
        } else {
            System.out.println(GREEN + "\n✅ " + resultats.size() + " abonnement(s) trouvé(s) :" + RESET);
            resultats.forEach(abo -> {
                System.out.println("\n• " + abo.getNomService());
                System.out.println("  Tags : " + String.join(", ", abo.getTags()));
                System.out.println("  Prix : " + String.format("%.2f€/an", abo.getCoutAnnuelEstime()));
            });
        }
    }
    
    /**
     * CODE MÉTIER 11 : Statistiques complètes
     */
    private static void afficherStatistiquesCompletes() {
        List<Abonnement> abonnements = repo.findAll();
        
        if (abonnements.isEmpty()) {
            System.out.println(YELLOW + "\n⚠️  Aucun abonnement à analyser" + RESET);
            return;
        }
        
        System.out.println(PURPLE + "\n📊 STATISTIQUES COMPLÈTES" + RESET);
        System.out.println(YELLOW + "═".repeat(100) + RESET);
        
        // Statistiques générales
        int total = abonnements.size();
        long actifs = abonnements.stream().filter(Abonnement::estActif).count();
        long expires = total - actifs;
        
        System.out.println("\n" + CYAN + "📈 GÉNÉRAL" + RESET);
        System.out.println("   Total abonnements : " + total);
        System.out.println("   Actifs : " + actifs);
        System.out.println("   Expirés : " + expires);
        
        // Coûts
        double coutAnnuelTotal = abonnements.stream()
            .filter(Abonnement::estActif)
            .mapToDouble(Abonnement::getCoutAnnuelEstime)
            .sum();
        double coutMensuelMoyen = coutAnnuelTotal / 12;
        
        System.out.println("\n" + CYAN + "💰 COÛTS" + RESET);
        System.out.println("   Coût annuel total : " + String.format("%.2f€", coutAnnuelTotal));
        System.out.println("   Coût mensuel moyen : " + String.format("%.2f€", coutMensuelMoyen));
        
        // Partage
        long nbPartages = abonnements.stream().filter(Abonnement::isPartage).count();
        double economiesPartage = abonnements.stream()
            .filter(a -> a.isPartage() && a.getNombreUtilisateurs() > 1)
            .mapToDouble(a -> (a.getPrixMensuel() - a.getCoutParPersonne()) * 12)
            .sum();
        
        System.out.println("\n" + CYAN + "👥 PARTAGE" + RESET);
        System.out.println("   Abonnements partagés : " + nbPartages);
        System.out.println("   Économies annuelles : " + String.format("%.2f€", economiesPartage));
        
        // ROI
        long excellent = abonnements.stream().filter(a -> a.getROI().contains("🌟")).count();
        long bon = abonnements.stream().filter(a -> a.getROI().contains("✅")).count();
        long moyen = abonnements.stream().filter(a -> a.getROI().contains("⚠️")).count();
        long faible = abonnements.stream().filter(a -> a.getROI().contains("⛔")).count();
        
        System.out.println("\n" + CYAN + "📈 ROI" + RESET);
        System.out.println("   Excellent : " + excellent);
        System.out.println("   Bon : " + bon);
        System.out.println("   Moyen : " + moyen);
        System.out.println("   Faible : " + faible + (faible > 0 ? " ⚠️" : ""));
        
        // Priorités
        long essentiels = abonnements.stream().filter(a -> a.getPriorite().equals("Essentiel")).count();
        long importants = abonnements.stream().filter(a -> a.getPriorite().equals("Important")).count();
        long optionnels = abonnements.stream().filter(a -> a.getPriorite().equals("Optionnel")).count();
        long luxe = abonnements.stream().filter(a -> a.getPriorite().equals("Luxe")).count();
        
        System.out.println("\n" + CYAN + "🎯 PRIORITÉS" + RESET);
        System.out.println("   🔴 Essentiels : " + essentiels);
        System.out.println("   🟠 Importants : " + importants);
        System.out.println("   🟡 Optionnels : " + optionnels);
        System.out.println("   🟢 Luxe : " + luxe);
        
        // Rappels
        long rappels = abonnements.stream().filter(Abonnement::doitEnvoyerRappel).count();
        System.out.println("\n" + CYAN + "🔔 RAPPELS" + RESET);
        System.out.println("   Expirations proches : " + rappels + (rappels > 0 ? " ⚠️" : " ✅"));
        
        System.out.println(YELLOW + "\n═".repeat(100) + RESET);
    }
}
