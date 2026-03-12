package com.projet.analytics;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import com.projet.backend.domain.Abonnement;

/**
 * PortfolioRebalancer - Optimise le portfolio d'abonnements sous contrainte de budget.
 * 
 * Objectif: Trouver la meilleure combinaison d'abonnements qui maximise la valeur
 * tout en respectant un budget cible donné.
 * 
 * Paramètres:
 * - budgetTarget: Budget mensuel maximal
 * - valueWeight: Poids accordé à la valeur (0-1)
 * - riskWeight: Poids accordé à la réduction de risque (0-1)
 * - comfortWeight: Poids accordé au confort/diversité (0-1)
 */
public class PortfolioRebalancer {

    public static class RebalanceResult {
        public List<Abonnement> kept;
        public List<Abonnement> optimized;
        public List<Abonnement> cancelled;
        public double totalCostKept;
        public double totalCostOptimized;
        public double totalCostCancelled;
        public double savingsPotential;
        public double objectiveScore;
        public String recommendation;
        public long processingTimeMs;

        public RebalanceResult() {
            this.kept = new ArrayList<>();
            this.optimized = new ArrayList<>();
            this.cancelled = new ArrayList<>();
            this.totalCostKept = 0;
            this.totalCostOptimized = 0;
            this.totalCostCancelled = 0;
            this.savingsPotential = 0;
            this.objectiveScore = 0;
        }
    }

    /**
     * Réequilibre le portfolio selon les contraintes de budget et objectifs.
     * 
     * @param subscriptions Liste des abonnements à analyser
     * @param budgetTarget Budget mensuel cible
     * @param valueWeight Poids de la valeur (0-1, def 0.4)
     * @param riskWeight Poids du risque (0-1, def 0.3)
     * @param comfortWeight Poids du confort (0-1, def 0.3)
     * @return RebalanceResult avec les recommandations
     */
    public static RebalanceResult rebalance(
        List<Abonnement> subscriptions,
        double budgetTarget,
        double valueWeight,
        double riskWeight,
        double comfortWeight
    ) {
        long startTime = System.currentTimeMillis();
        RebalanceResult result = new RebalanceResult();

        if (subscriptions == null || subscriptions.isEmpty()) {
            result.processingTimeMs = System.currentTimeMillis() - startTime;
            result.recommendation = "Aucun abonnement à analyser.";
            return result;
        }

        // Valider les weights (somme doit être ~1)
        double totalWeight = valueWeight + riskWeight + comfortWeight;
        if (Math.abs(totalWeight - 1.0) > 0.001) {
            valueWeight /= totalWeight;
            riskWeight /= totalWeight;
            comfortWeight /= totalWeight;
        }

        // Filtrer les abonnements actifs
        LocalDate now = LocalDate.now();
        List<Abonnement> activeSubscriptions = subscriptions.stream()
            .filter(a -> isActive(a, now))
            .collect(Collectors.toList());

        if (activeSubscriptions.isEmpty()) {
            result.processingTimeMs = System.currentTimeMillis() - startTime;
            result.recommendation = "Aucun abonnement actif trouvé.";
            return result;
        }

        // Calculer scores pour chaque abonnement
        List<SubscriptionScore> scores = new ArrayList<>();
        for (Abonnement sub : activeSubscriptions) {
            SubscriptionScore score = calculateScore(sub, valueWeight, riskWeight, comfortWeight);
            scores.add(score);
        }

        // Trier par score décroissant
        scores.sort((a, b) -> Double.compare(b.totalScore, a.totalScore));

        // Appliquer l'algorithme d'optimisation
        optimizePortfolio(scores, budgetTarget, result);

        // Calculer les économies
        result.savingsPotential = result.totalCostCancelled;
        
        // Score d'objectif (0-100%)
        double totalCostKept = result.totalCostKept;
        result.objectiveScore = calculateObjectiveScore(
            totalCostKept,
            budgetTarget,
            result.kept.size(),
            activeSubscriptions.size()
        );

        // Générer recommendation
        result.recommendation = generateRecommendation(result, budgetTarget);

        result.processingTimeMs = System.currentTimeMillis() - startTime;
        return result;
    }

    /**
     * Calcule le score composite d'un abonnement.
     */
    private static SubscriptionScore calculateScore(
        Abonnement sub,
        double valueWeight,
        double riskWeight,
        double comfortWeight
    ) {
        SubscriptionScore score = new SubscriptionScore(sub);

        // Score de valeur: basé sur le ratio utilité/prix
        double valueScore = calculateValueScore(sub);
        
        // Score de risque: basé sur l'utilisation récente
        double riskScore = calculateRiskScore(sub);
        
        // Score de confort: basé sur la diversité et la couverture
        double comfortScore = calculateComfortScore(sub);

        // Score composite
        score.valueScore = valueScore;
        score.riskScore = riskScore;
        score.comfortScore = comfortScore;
        score.totalScore = (valueScore * valueWeight) + (riskScore * riskWeight) + (comfortScore * comfortWeight);

        return score;
    }

    /**
     * Score de valeur: inversement proportionnel au prix(moins cher = plus haut score).
     */
    private static double calculateValueScore(Abonnement sub) {
        double prix = sub.getPrixMensuel();
        if (prix <= 0) return 0;
        // Normaliser: max 100 points pour < 5€, décroissant
        return Math.max(0, 100 - (prix * 10));
    }

    /**
     * Score de risque: basé sur la dernière utilisation.
     */
    private static double calculateRiskScore(Abonnement sub) {
        LocalDate lastUsed = sub.getDerniereUtilisation();
        if (lastUsed == null) return 50; // Défaut: risque moyen

        long daysSinceUsed = java.time.temporal.ChronoUnit.DAYS.between(lastUsed, LocalDate.now());
        
        if (daysSinceUsed < 7) return 100;  // Utilisé récemment: très sûr
        if (daysSinceUsed < 30) return 75;  // Utilisé ce mois
        if (daysSinceUsed < 90) return 50;  // Utilisé il y a plus d'un mois
        if (daysSinceUsed < 180) return 25; // Risque: pas utilisé 2-6 mois
        return 0;                            // Très risqué: pas utilisé 6+ mois
    }

    /**
     * Score de confort: diversité des catégories.
     */
    private static double calculateComfortScore(Abonnement sub) {
        // Chaque catégorie a une importance basique
        String cat = sub.getCategorie();
        if (cat == null) return 50;
        
        return switch(cat.toLowerCase()) {
            case "streaming" -> 70;
            case "productivité" -> 85;
            case "cloud" -> 90;
            case "sécurité" -> 95;
            default -> 60;
        };
    }

    /**
     * Applique l'algorithme d'optimisation pour sélectionner les meilleurs abonnements.
     */
    private static void optimizePortfolio(
        List<SubscriptionScore> scores,
        double budgetTarget,
        RebalanceResult result
    ) {
        double currentCost = 0;

        for (SubscriptionScore score : scores) {
            Abonnement sub = score.subscription;
            double cost = sub.getPrixMensuel();

            // Essayer d'ajouter à "kept"
            if (currentCost + cost <= budgetTarget) {
                result.kept.add(sub);
                result.totalCostKept += cost;
                currentCost += cost;
            } else {
                // Si on dépasse le budget: candidat pour optimisation
                if (cost > budgetTarget * 0.15) {
                    // Coûteux: canceller
                    result.cancelled.add(sub);
                    result.totalCostCancelled += cost;
                } else {
                    // Moins coûteux: optimiser (possibilité de réduire ou remplacer)
                    result.optimized.add(sub);
                    result.totalCostOptimized += cost;
                }
            }
        }
    }

    /**
     * Calcule le score d'atteinte de l'objectif (0-100%).
     */
    private static double calculateObjectiveScore(
        double actualCost,
        double budgetTarget,
        int keptCount,
        int totalCount
    ) {
        if (budgetTarget <= 0) return 0;

        // Score 1: Respect du budget
        double budgetScore = Math.max(0, 100 - (Math.abs(actualCost - budgetTarget) / budgetTarget * 100));

        // Score 2: Couverture des services
        double coverageScore = (keptCount * 100.0) / totalCount;

        // Score composite
        return (budgetScore * 0.6) + (coverageScore * 0.4);
    }

    /**
     * Génère une recommendation textuelle basée sur les résultats.
     */
    private static String generateRecommendation(RebalanceResult result, double budgetTarget) {
        StringBuilder sb = new StringBuilder();

        if (result.kept.isEmpty() && result.optimized.isEmpty() && result.cancelled.isEmpty()) {
            return "Aucune analyse disponible.";
        }

        sb.append(result.kept.size()).append(" services conservés pour un budget optimal. ");

        if (!result.optimized.isEmpty()) {
            sb.append("Vous pourriez optimiser ").append(result.optimized.size()).append(" services. ");
        }

        if (!result.cancelled.isEmpty()) {
            sb.append("Envisagez d'annuler ").append(result.cancelled.size()).append(" services redondants. ");
        }

        if (result.savingsPotential > 0) {
            sb.append("Économies potentielles: ");
            sb.append(String.format("€%.2f/mois", result.savingsPotential));
        }

        return sb.toString();
    }

    /**
     * Vérifie si un abonnement est actuellement actif.
     */
    private static boolean isActive(Abonnement sub, LocalDate now) {
        if (sub.getDateDebut() == null || sub.getDateFin() == null) return false;
        return !now.isBefore(sub.getDateDebut()) && !now.isAfter(sub.getDateFin());
    }

    /**
     * Helper class pour stocker les scores intermédiaires.
     */
    private static class SubscriptionScore {
        Abonnement subscription;
        double valueScore;
        double riskScore;
        double comfortScore;
        double totalScore;

        SubscriptionScore(Abonnement sub) {
            this.subscription = sub;
        }
    }
}
