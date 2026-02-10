package com.projet.backend.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.projet.backend.domain.Abonnement;

/**
 * Service d'orchestration pour les opérations métier sur les abonnements.
 * 
 * Cette couche service :
 * - Utilise les entités du domaine (Abonnement, User)
 * - Exécute la logique métier sans I/O
 * - Retourne des résultats structurés
 * - Est complètement indépendante de l'UI et la persistence
 * 
 * Le service accepte un repository en dépendance pour la persistence,
 * mais reste focused sur la logique métier.
 */
public class SubscriptionService {

    /**
     * Résultat structuré pour les opérations de validation.
     */
    public static class ValidationResult {
        public final boolean valid;
        public final List<String> errors;

        public ValidationResult(boolean valid, List<String> errors) {
            this.valid = valid;
            this.errors = errors;
        }

        public static ValidationResult success() {
            return new ValidationResult(true, new ArrayList<>());
        }

        public static ValidationResult failure(List<String> errors) {
            return new ValidationResult(false, errors);
        }

        public static ValidationResult failure(String error) {
            List<String> errors = new ArrayList<>();
            errors.add(error);
            return new ValidationResult(false, errors);
        }
    }

    /**
     * Résultat structuré pour les statistiques du portefeuille.
     */
    public static class PortfolioStats {
        public final int totalSubscriptions;
        public final int activeSubscriptions;
        public final int inactiveSubscriptions;
        public final double totalMonthlyCost;
        public final double averageMonthlyCost;
        public final double portfolioHealthScore;
        public final List<String> categoriesDistribution;
        public final int highChurnRiskCount;

        public PortfolioStats(
            int total, int active, int inactive, double monthlyCost, 
            double avgCost, double health, List<String> categories, int churnRisk
        ) {
            this.totalSubscriptions = total;
            this.activeSubscriptions = active;
            this.inactiveSubscriptions = inactive;
            this.totalMonthlyCost = monthlyCost;
            this.averageMonthlyCost = avgCost;
            this.portfolioHealthScore = health;
            this.categoriesDistribution = categories;
            this.highChurnRiskCount = churnRisk;
        }
    }

    // ========== HELPERS PRIVÉS DE VALIDATION ==========

    /**
     * Vérifie si une chaîne est nulle ou vide.
     */
    private static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Vérifie si un nombre est dans une plage valide.
     */
    private static boolean isInRange(double value, double min, double max) {
        return value >= min && value <= max;
    }

    /**
     * Vérifie si un entier est positif et supérieur à zéro.
     */
    private static boolean isPositive(int value) {
        return value > 0;
    }

    // ========== VALIDATIONS MÉTIER ==========

    /**
     * Valide un abonnement selon les règles métier.
     * 
     * @param abonnement L'abonnement à valider
     * @return Résultat avec validation et éventuels messages d'erreur
     */
    public ValidationResult validateSubscription(Abonnement abonnement) {
        List<String> errors = new ArrayList<>();

        // Validations métier
        if (abonnement.getPrixMensuel() < 0) {
            errors.add(BackendMessages.SUBSCRIPTION_PRICE_NEGATIVE);
        }

        if (abonnement.getDateDebut() == null || abonnement.getDateFin() == null) {
            errors.add(BackendMessages.SUBSCRIPTION_DATES_MISSING);
        } else if (abonnement.getDateDebut().isAfter(abonnement.getDateFin())) {
            errors.add(BackendMessages.SUBSCRIPTION_DATE_ORDER_INVALID);
        }

        if (isNullOrEmpty(abonnement.getNomService())) {
            errors.add(BackendMessages.SUBSCRIPTION_NAME_MISSING);
        }

        if (isNullOrEmpty(abonnement.getClientName())) {
            errors.add(BackendMessages.SUBSCRIPTION_CLIENT_MISSING);
        }

        if (!isPositive(abonnement.getNombreUtilisateurs())) {
            errors.add(BackendMessages.SUBSCRIPTION_USERS_INVALID);
        }

        return errors.isEmpty() ? ValidationResult.success() : ValidationResult.failure(errors);
    }

    /**
     * Crée un nouvel abonnement avec validation.
     * 
     * @param nomService Nom du service
     * @param dateDebut Date de début
     * @param dateFin Date de fin
     * @param prixMensuel Prix mensuel
     * @param clientName Nom du client
     * @param categorie Catégorie
     * @return L'abonnement créé si valide, null sinon
     */
    public Abonnement createSubscription(
        String nomService, LocalDate dateDebut, LocalDate dateFin,
        double prixMensuel, String clientName, String categorie
    ) {
        Abonnement abonnement = new Abonnement(
            null, nomService, dateDebut, dateFin, prixMensuel, clientName, null, categorie
        );

        ValidationResult validation = validateSubscription(abonnement);
        if (!validation.valid) {
            throw new IllegalArgumentException(BackendMessages.formatValidationError("Création abonnement échouée", validation.errors));
        }

        return abonnement;
    }

    /**
     * Filtre les abonnements par catégorie.
     * 
     * @param abonnements Liste d'abonnements
     * @param categorie Catégorie à filtrer
     * @return Abonnements correspondant à la catégorie
     */
    public List<Abonnement> filterByCategory(List<Abonnement> abonnements, String categorie) {
        if (abonnements == null || abonnements.isEmpty() || categorie == null) {
            return new ArrayList<>();
        }
        return abonnements.stream()
            .filter(a -> categorie.equalsIgnoreCase(a.getCategorie()))
            .collect(Collectors.toList());
    }

    /**
     * Récupère les abonnements actifs à une date donnée.
     * 
     * @param abonnements Liste d'abonnements
     * @param date Date à vérifier
     * @return Abonnements actifs à la date donnée
     */
    public List<Abonnement> getActiveSubscriptions(List<Abonnement> abonnements, LocalDate date) {
        if (abonnements == null || abonnements.isEmpty() || date == null) {
            return new ArrayList<>();
        }
        return abonnements.stream()
            .filter(a -> !a.getDateDebut().isAfter(date) && !a.getDateFin().isBefore(date))
            .collect(Collectors.toList());
    }

    /**
     * Récupère les abonnements actifs aujourd'hui.
     * 
     * @param abonnements Liste d'abonnements
     * @return Abonnements actifs aujourd'hui
     */
    public List<Abonnement> getActiveSubscriptions(List<Abonnement> abonnements) {
        if (abonnements == null || abonnements.isEmpty()) {
            return new ArrayList<>();
        }
        return getActiveSubscriptions(abonnements, LocalDate.now());
    }

    /**
     * Récupère les abonnements qui vont expirer bientôt.
     * 
     * @param abonnements Liste d'abonnements
     * @param joursAvant Nombre de jours avant expiration
     * @return Abonnements expirant dans le délai spécifié
     */
    public List<Abonnement> getExpiringSubscriptions(List<Abonnement> abonnements, int joursAvant) {
        if (abonnements == null || abonnements.isEmpty()) {
            return new ArrayList<>();
        }
        LocalDate today = LocalDate.now();
        LocalDate threshold = today.plusDays(joursAvant);

        return abonnements.stream()
            .filter(a -> a.estActif() && 
                    a.getDateFin().isAfter(today) && 
                    !a.getDateFin().isAfter(threshold))
            .collect(Collectors.toList());
    }

    /**
     * Récupère les abonnements avec risque de churn élevé.
     * 
     * Utilise la métrique `getChurnRisk()` du domaine (0-100).
     * Considère risque élevé si score > 70.
     * 
     * @param abonnements Liste d'abonnements
     * @return Abonnements à risque de churn
     */
    public List<Abonnement> getHighChurnRiskSubscriptions(List<Abonnement> abonnements) {
        if (abonnements == null || abonnements.isEmpty()) {
            return new ArrayList<>();
        }
        return abonnements.stream()
            .filter(a -> a.getChurnRisk() > 70)
            .collect(Collectors.toList());
    }

    /**
     * Calcule les statistiques complètes du portefeuille d'abonnements.
     * 
     * @param abonnements Liste d'abonnements
     * @return Statistiques du portefeuille
     */
    public PortfolioStats calculatePortfolioStats(List<Abonnement> abonnements) {
        if (abonnements == null || abonnements.isEmpty()) {
            return new PortfolioStats(0, 0, 0, 0.0, 0.0, 0.0, new ArrayList<>(), 0);
        }

        LocalDate today = LocalDate.now();
        int total = abonnements.size();

        // Abonnements actifs vs inactifs
        List<Abonnement> active = getActiveSubscriptions(abonnements, today);
        int activeCount = active.size();
        int inactiveCount = total - activeCount;

        // Coûts mensuels
        double totalMonthlyCost = abonnements.stream()
            .mapToDouble(Abonnement::getPrixMensuel)
            .sum();
        double avgMonthlyCost = total > 0 ? totalMonthlyCost / total : 0.0;

        // Score de santé du portefeuille
        double portfolioHealth = calculatePortfolioHealth(abonnements);

        // Distribution des catégories
        List<String> categories = abonnements.stream()
            .map(Abonnement::getCategorie)
            .distinct()
            .collect(Collectors.toList());

        // Abonnements à risque de churn élevé
        int highChurnRisk = (int) abonnements.stream()
            .filter(a -> a.getChurnRisk() > 70)
            .count();

        return new PortfolioStats(
            total, activeCount, inactiveCount, totalMonthlyCost, avgMonthlyCost,
            portfolioHealth, categories, highChurnRisk
        );
    }

    /**
     * Calcule le score de santé du portefeuille (0-100).
     * 
     * Prend en compte :
     * - Ratio d'abonnements actifs
     * - Diversification des catégories
     * - Potentiel d'économies
     * 
     * @param abonnements Liste d'abonnements
     * @return Score de santé (0-100)
     */
    public double calculatePortfolioHealth(List<Abonnement> abonnements) {
        if (abonnements == null || abonnements.isEmpty()) {
            return 0.0;
        }

        // Score de base sur activation
        LocalDate today = LocalDate.now();
        long activeCount = abonnements.stream()
            .filter(a -> a.estActif())
            .count();
        double activationScore = (activeCount * 100.0) / abonnements.size() * 0.4; // 40% du score

        // Score de diversification des catégories
        long categoryCount = abonnements.stream()
            .map(Abonnement::getCategorie)
            .distinct()
            .count();
        double diversificationScore = Math.min(categoryCount * 10, 100) * 0.3; // 30% du score

        // Score sur l'inactivité (potentiel d'économies)
        long inactiveCount = abonnements.size() - activeCount;
        long maxInactive = abonnements.size();
        double inactivityScore = (1.0 - (inactiveCount * 1.0 / maxInactive)) * 100 * 0.3; // 30% du score

        return Math.min(activationScore + diversificationScore + inactivityScore, 100.0);
    }

    /**
     * Calcule le score ROI numérique (Return On Investment) d'un abonnement.
     * 
     * Basé sur la valeur et la fréquence d'utilisation.
     * 
     * @param abonnement L'abonnement
     * @return Score ROI (0-100)
     */
    public double calculateRoiScore(Abonnement abonnement) {
        // Combine value score et usage frequency pour un ROI numérique
        double valueScore = abonnement.getValueScore();
        double usageFreq = abonnement.getUsageFrequency();
        double churnRisk = abonnement.getChurnRisk();
        
        // Formula: value * usage / churn risk (normalized to 0-100)
        if (churnRisk >= 100) return 0;
        return Math.min((valueScore * usageFreq) / (100 - churnRisk + 1), 100.0);
    }

    /**
     * Récupère les abonnements d'un utilisateur spécifique.
     * 
     * @param abonnements Liste totale d'abonnements
     * @param clientName Nom du client à filtrer
     * @return Abonnements du client
     */
    public List<Abonnement> getSubscriptionsByClient(List<Abonnement> abonnements, String clientName) {
        if (abonnements == null || abonnements.isEmpty() || clientName == null) {
            return new ArrayList<>();
        }
        return abonnements.stream()
            .filter(a -> clientName.equalsIgnoreCase(a.getClientName()))
            .collect(Collectors.toList());
    }

    /**
     * Trie les abonnements par coût mensuel (ordre décroissant).
     * 
     * @param abonnements Liste d'abonnements
     * @return Abonnements triés par coût
     */
    public List<Abonnement> sortByMonthlyCost(List<Abonnement> abonnements) {
        if (abonnements == null || abonnements.isEmpty()) {
            return new ArrayList<>();
        }
        return abonnements.stream()
            .sorted((a, b) -> Double.compare(b.getPrixMensuel(), a.getPrixMensuel()))
            .collect(Collectors.toList());
    }

    /**
     * Trie les abonnements par valeur/score (ordre décroissant).
     * 
     * Utilise la métrique de valeur du domaine pour évaluer chaque abonnement.
     * 
     * @param abonnements Liste d'abonnements
     * @return Abonnements triés par score de valeur
     */
    public List<Abonnement> sortByValueScore(List<Abonnement> abonnements) {
        if (abonnements == null || abonnements.isEmpty()) {
            return new ArrayList<>();
        }
        return abonnements.stream()
            .sorted((a, b) -> Double.compare(b.getValueScore(), a.getValueScore()))
            .collect(Collectors.toList());
    }

    /**
     * Calcule le coût total d'un portefeuille sur une période donnée.
     * 
     * Utilise les coûts cumulés de chaque abonnement.
     * 
     * @param abonnements Liste d'abonnements
     * @return Coût total cumulé
     */
    public double calculateTotalCost(List<Abonnement> abonnements) {
        if (abonnements == null || abonnements.isEmpty()) {
            return 0.0;
        }
        return abonnements.stream()
            .mapToDouble(Abonnement::getCoutTotal)
            .sum();
    }

    /**
     * Recommande les abonnements à garder en priorité.
     * 
     * Basé sur :
     * - Valeur (score de valeur élevé)
     * - Activité (utilisation récente)
     * - ROI positif
     * 
     * @param abonnements Liste d'abonnements
     * @return Top 5 abonnements à conserver
     */
    public List<Abonnement> getTopPrioritySubscriptions(List<Abonnement> abonnements) {
        if (abonnements == null || abonnements.isEmpty()) {
            return new ArrayList<>();
        }
        return abonnements.stream()
            .sorted((a, b) -> {
                // Critères: valeur score, puis ROI numérique, puis activité récente
                int valueComparison = Double.compare(b.getValueScore(), a.getValueScore());
                if (valueComparison != 0) return valueComparison;

                int roiComparison = Double.compare(calculateRoiScore(b), calculateRoiScore(a));
                if (roiComparison != 0) return roiComparison;

                // Dernier si plus récent = meilleur
                if (b.getDerniereUtilisation() != null && a.getDerniereUtilisation() != null) {
                    return b.getDerniereUtilisation().compareTo(a.getDerniereUtilisation());
                }
                return 0;
            })
            .limit(5)
            .collect(Collectors.toList());
    }

    /**
     * Identifie les opportunités d'économies dans le portefeuille.
     * 
     * Retourne les abonnements candidats à annulation ou réduction.
     * Basé sur le risque de churn élevé et l'inactivité.
     * 
     * @param abonnements Liste d'abonnements
     * @return Abonnements candidats à annulation
     */
    public List<Abonnement> identifySavingOpportunities(List<Abonnement> abonnements) {
        if (abonnements == null || abonnements.isEmpty()) {
            return new ArrayList<>();
        }
        return abonnements.stream()
            .filter(a -> a.getChurnRisk() > 70 || 
                         (a.getDerniereUtilisation() != null && 
                          a.getDerniereUtilisation().isBefore(LocalDate.now().minusMonths(2))))
            .collect(Collectors.toList());
    }
}
