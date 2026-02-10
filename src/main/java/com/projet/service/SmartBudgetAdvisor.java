package com.projet.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.projet.backend.domain.Abonnement;

/**
 * SmartBudgetAdvisor - Optimiseur budgétaire intelligent avec algorithmes avancés
 * 
 * Fournit des recommandations d'optimisation budgétaires basées sur :
 * - Clustering K-means des abonnements
 * - Scoring multi-critères
 * - Heuristique de recommandation
 * - Prédictions des économies potentielles
 */
public class SmartBudgetAdvisor {

    public static class BudgetAnalysis {
        public double totalMonthlyCost;
        public double potentialSavings;
        public double savingsPercentage;
        public int healthScore;
        public List<RecommendationItem> recommendations;
        public List<CategorySummary> categorySummaries;
        public Map<String, Object> sixMonthForecast;

        @Override
        public String toString() {
            return "BudgetAnalysis{" +
                    "totalMonthlyCost=" + totalMonthlyCost +
                    ", potentialSavings=" + potentialSavings +
                    ", savingsPercentage=" + savingsPercentage +
                    ", healthScore=" + healthScore +
                    ", recommendations=" + recommendations.size() +
                    ", categories=" + categorySummaries.size() +
                    '}';
        }
    }

    public static class RecommendationItem {
        public String abonnementId;
        public String serviceName;
        public String actionType; // "CANCEL", "DOWNGRADE", "CONSOLIDATE"
        public String reasoning;
        public double monthlySavings;
        public int priority; // 1-10, 10 = haute priorité
        public double confidence; // 0-100

        public RecommendationItem(String abonnementId, String serviceName, String actionType,
                                 String reasoning, double monthlySavings, int priority, double confidence) {
            this.abonnementId = abonnementId;
            this.serviceName = serviceName;
            this.actionType = actionType;
            this.reasoning = reasoning;
            this.monthlySavings = monthlySavings;
            this.priority = priority;
            this.confidence = confidence;
        }
    }

    public static class CategorySummary {
        public String category;
        public double totalCost;
        public int count;
        public double averageScore;
        public List<String> services;

        public CategorySummary(String category) {
            this.category = category;
            this.services = new ArrayList<>();
            this.totalCost = 0;
            this.count = 0;
            this.averageScore = 0;
        }
    }

    public static class Cluster {
        public List<Abonnement> members;
        public double centroidPrice;
        public String clusterType; // "CHEAP", "MEDIUM", "EXPENSIVE"

        public Cluster() {
            this.members = new ArrayList<>();
        }
    }

    /**
     * Analyse complète du budget avec recommandations
     */
    public static BudgetAnalysis analyzeBudget(List<Abonnement> abonnements) {
        if (abonnements == null || abonnements.isEmpty()) {
            return createEmptyAnalysis();
        }

        BudgetAnalysis analysis = new BudgetAnalysis();
        
        // Calculs de base
        analysis.totalMonthlyCost = abonnements.stream()
                .mapToDouble(Abonnement::getPrixMensuel)
                .sum();

        // Clustering K-means sur 3 clusters (cheap, medium, expensive)
        List<Cluster> clusters = performKMeansClustering(abonnements, 3);
        
        // Scoring et recommandations
        analysis.recommendations = generateRecommendations(abonnements, clusters);
        analysis.potentialSavings = analysis.recommendations.stream()
                .mapToDouble(r -> r.monthlySavings)
                .sum();
        
        analysis.savingsPercentage = (analysis.totalMonthlyCost > 0) ?
                (analysis.potentialSavings / analysis.totalMonthlyCost) * 100 : 0;

        // Score de santé du portefeuille (0-100)
        analysis.healthScore = calculatePortfolioHealthScore(abonnements, analysis);

        // Résumés par catégorie
        analysis.categorySummaries = generateCategorySummaries(abonnements);

        // Prédictions 6 mois
        analysis.sixMonthForecast = generateSixMonthForecast(analysis);

        return analysis;
    }

    /**
     * Clustering K-means pour grouper les abonnements par prix
     */
    private static List<Cluster> performKMeansClustering(List<Abonnement> abonnements, int k) {
        if (abonnements.isEmpty()) return new ArrayList<>();

        // Initialiser les centroids
        List<Double> centroids = new ArrayList<>();
        double minPrice = abonnements.stream().mapToDouble(Abonnement::getPrixMensuel).min().orElse(0);
        double maxPrice = abonnements.stream().mapToDouble(Abonnement::getPrixMensuel).max().orElse(100);
        double step = (maxPrice - minPrice) / k;

        for (int i = 0; i < k; i++) {
            centroids.add(minPrice + (step * i) + step / 2);
        }

        // Itérations K-means (convergence)
        for (int iter = 0; iter < 10; iter++) {
            List<Cluster> clusters = new ArrayList<>();
            for (int i = 0; i < k; i++) {
                Cluster c = new Cluster();
                c.centroidPrice = centroids.get(i);
                clusters.add(c);
            }

            // Assigner chaque abonnement au cluster le plus proche
            for (Abonnement abo : abonnements) {
                int closestCluster = 0;
                double minDistance = Math.abs(abo.getPrixMensuel() - centroids.get(0));

                for (int i = 1; i < k; i++) {
                    double distance = Math.abs(abo.getPrixMensuel() - centroids.get(i));
                    if (distance < minDistance) {
                        minDistance = distance;
                        closestCluster = i;
                    }
                }

                clusters.get(closestCluster).members.add(abo);
            }

            // Recalculer les centroids
            List<Double> newCentroids = new ArrayList<>();
            for (Cluster cluster : clusters) {
                if (cluster.members.isEmpty()) {
                    newCentroids.add(centroids.get(clusters.indexOf(cluster)));
                } else {
                    double avg = cluster.members.stream()
                            .mapToDouble(Abonnement::getPrixMensuel)
                            .average()
                            .orElse(0);
                    newCentroids.add(avg);
                }
            }

            // Vérifier convergence
            boolean converged = true;
            for (int i = 0; i < k; i++) {
                if (Math.abs(newCentroids.get(i) - centroids.get(i)) > 0.01) {
                    converged = false;
                    break;
                }
            }
            centroids = newCentroids;
            if (converged) break;
        }

        // Créer clusters finaux
        List<Cluster> finalClusters = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            Cluster c = new Cluster();
            c.centroidPrice = centroids.get(i);
            if (i == 0) c.clusterType = "CHEAP";
            else if (i == 1) c.clusterType = "MEDIUM";
            else c.clusterType = "EXPENSIVE";
            finalClusters.add(c);
        }

        // Assigner final
        for (Abonnement abo : abonnements) {
            int closestCluster = 0;
            double minDistance = Math.abs(abo.getPrixMensuel() - centroids.get(0));

            for (int i = 1; i < k; i++) {
                double distance = Math.abs(abo.getPrixMensuel() - centroids.get(i));
                if (distance < minDistance) {
                    minDistance = distance;
                    closestCluster = i;
                }
            }

            finalClusters.get(closestCluster).members.add(abo);
        }

        return finalClusters;
    }

    /**
     * Génère les recommandations d'économie
     */
    private static List<RecommendationItem> generateRecommendations(List<Abonnement> abonnements,
                                                                     List<Cluster> clusters) {
        List<RecommendationItem> recommendations = new ArrayList<>();

        for (Abonnement abo : abonnements) {
            // Score de valeur (0-10)
            double valueScore = calculateValueScore(abo);
            
            // Score d'inactivité
            double inactivityScore = calculateInactivityScore(abo);
            
            // Score de prix relatif
            double priceRelativeScore = calculatePriceRelativeScore(abo, clusters);

            // Heuristique : Si faible valeur + haute inactivité => recommander
            if (valueScore < 3 && inactivityScore > 5) {
                recommendations.add(new RecommendationItem(
                        abo.getId(),
                        abo.getNomService(),
                        "CANCEL",
                        "Service peu utilisé avec mauvaise valeur perçue",
                        abo.getPrixMensuel(),
                        9,
                        85
                ));
            }
            
            // Si prix anormalement haut et usage moyen => downgrade
            else if (priceRelativeScore > 7 && valueScore < 5) {
                recommendations.add(new RecommendationItem(
                        abo.getId(),
                        abo.getNomService(),
                        "DOWNGRADE",
                        "Service cher comparé à la catégorie, possibilité de réduire",
                        abo.getPrixMensuel() * 0.3, // économie estimée 30%
                        7,
                        70
                ));
            }
            
            // Si services similaires (même catégorie, prix proche) => consolidation
            List<Abonnement> similarServices = findSimilarServices(abo, abonnements);
            if (similarServices.size() > 1) {
                recommendations.add(new RecommendationItem(
                        abo.getId(),
                        abo.getNomService(),
                        "CONSOLIDATE",
                        "Services similaires détectés - possibilité de fusionner",
                        abo.getPrixMensuel() * 0.5, // économie estimée 50%
                        8,
                        60
                ));
            }
        }

        // Trier par priorité et économies potentielles
        recommendations.sort((a, b) -> {
            int priorityCompare = Integer.compare(b.priority, a.priority);
            if (priorityCompare != 0) return priorityCompare;
            return Double.compare(b.monthlySavings, a.monthlySavings);
        });

        return recommendations;
    }

    /**
     * Calcule le score de valeur d'un abonnement (0-10)
     */
    private static double calculateValueScore(Abonnement abo) {
        double score = 5.0; // Base neutre

        // Facteur utilisation récente (+3 si utilisé < 7 jours)
        if (abo.getDerniereUtilisation() != null) {
            long daysSinceUse = ChronoUnit.DAYS.between(abo.getDerniereUtilisation(), LocalDate.now());
            if (daysSinceUse < 7) score += 3;
            else if (daysSinceUse < 30) score += 1;
            else if (daysSinceUse > 60) score -= 3;
        } else {
            score -= 2; // Jamais utilisé
        }

        // Facteur prix (-1 si très cher, +1 si bon marché)
        if (abo.getPrixMensuel() > 20) score -= 1;
        else if (abo.getPrixMensuel() < 5) score += 1;

        // Facteur priorité
        String priorite = abo.getPriorite();
        if ("Essential".equals(priorite)) score += 2;
        else if ("Luxe".equals(priorite)) score -= 1;

        return Math.max(0, Math.min(10, score));
    }

    /**
     * Calcule le score d'inactivité (0-10)
     */
    private static double calculateInactivityScore(Abonnement abo) {
        if (abo.getDerniereUtilisation() == null) return 10; // Jamais utilisé = très inactif

        long daysSinceUse = ChronoUnit.DAYS.between(abo.getDerniereUtilisation(), LocalDate.now());
        
        if (daysSinceUse < 7) return 0;
        if (daysSinceUse < 30) return 2;
        if (daysSinceUse < 60) return 5;
        return 10;
    }

    /**
     * Calcule le score de prix relatif comparé au cluster
     */
    private static double calculatePriceRelativeScore(Abonnement abo, List<Cluster> clusters) {
        for (Cluster cluster : clusters) {
            if (cluster.members.contains(abo)) {
                double relativePrice = abo.getPrixMensuel() / (cluster.centroidPrice + 1);
                return Math.min(10, relativePrice * 5);
            }
        }
        return 5;
    }

    /**
     * Trouve les services similaires (même catégorie, prix proche)
     */
    private static List<Abonnement> findSimilarServices(Abonnement target, List<Abonnement> allServices) {
        return allServices.stream()
                .filter(abo -> !abo.getId().equals(target.getId()))
                .filter(abo -> categoriesMatch(abo.getCategorie(), target.getCategorie()))
                .filter(abo -> Math.abs(abo.getPrixMensuel() - target.getPrixMensuel()) < 5)
                .collect(Collectors.toList());
    }

    private static boolean categoriesMatch(String cat1, String cat2) {
        if (cat1 == null || cat2 == null) return false;
        return cat1.equalsIgnoreCase(cat2) ||
               (cat1.contains("Stream") && cat2.contains("Stream")) ||
               (cat1.contains("Music") && cat2.contains("Music")) ||
               (cat1.contains("Cloud") && cat2.contains("Cloud"));
    }

    /**
     * Calcule le score de santé du portefeuille (0-100)
     */
    private static int calculatePortfolioHealthScore(List<Abonnement> abonnements, BudgetAnalysis analysis) {
        int score = 50; // Base

        // Facteur économies potentielles (max +25)
        double savingsRatio = analysis.savingsPercentage;
        if (savingsRatio < 10) score += 25;
        else if (savingsRatio < 20) score += 15;
        else if (savingsRatio < 30) score += 5;
        else score -= 10; // Trop d'économies potentielles = mauvaise gestion

        // Facteur diversification (max +15)
        Map<String, Long> categoryCount = abonnements.stream()
                .collect(Collectors.groupingBy(Abonnement::getCategorie, Collectors.counting()));
        if (categoryCount.size() >= 5) score += 15;
        else if (categoryCount.size() >= 3) score += 10;
        else if (categoryCount.size() == 1) score -= 10;

        // Facteur inactivité (max -20)
        long inactiveCount = abonnements.stream()
                .filter(a -> {
                    if (a.getDerniereUtilisation() == null) return true;
                    long days = ChronoUnit.DAYS.between(a.getDerniereUtilisation(), LocalDate.now());
                    return days > 60;
                })
                .count();
        
        if (inactiveCount > abonnements.size() * 0.3) score -= 20;
        else if (inactiveCount > 0) score -= 10;

        return Math.max(0, Math.min(100, score));
    }

    /**
     * Génère des résumés par catégorie
     */
    private static List<CategorySummary> generateCategorySummaries(List<Abonnement> abonnements) {
        Map<String, List<Abonnement>> byCategory = abonnements.stream()
                .collect(Collectors.groupingBy(Abonnement::getCategorie));

        return byCategory.entrySet().stream()
                .map(entry -> {
                    CategorySummary summary = new CategorySummary(entry.getKey());
                    summary.totalCost = entry.getValue().stream()
                            .mapToDouble(Abonnement::getPrixMensuel)
                            .sum();
                    summary.count = entry.getValue().size();
                    summary.averageScore = entry.getValue().stream()
                            .mapToDouble(SmartBudgetAdvisor::calculateValueScore)
                            .average()
                            .orElse(0);
                    summary.services = entry.getValue().stream()
                            .map(Abonnement::getNomService)
                            .collect(Collectors.toList());
                    return summary;
                })
                .sorted((a, b) -> Double.compare(b.totalCost, a.totalCost))
                .collect(Collectors.toList());
    }

    /**
     * Génère les prédictions pour 6 mois
     */
    private static Map<String, Object> generateSixMonthForecast(BudgetAnalysis analysis) {
        Map<String, Object> forecast = new HashMap<>();

        double currentCost = analysis.totalMonthlyCost;
        double projectedCost = currentCost - (analysis.potentialSavings / 2); // Scenario moyen
        double bestCaseCost = currentCost - analysis.potentialSavings; // Tout appliqué
        double worstCaseCost = currentCost; // Rien appliqué

        Map<String, Double> monthlyCosts = new HashMap<>();
        monthlyCosts.put("month1", currentCost);
        monthlyCosts.put("month2", (currentCost + projectedCost) / 2);
        monthlyCosts.put("month3", projectedCost);
        monthlyCosts.put("month4", projectedCost);
        monthlyCosts.put("month5", projectedCost);
        monthlyCosts.put("month6", projectedCost);

        forecast.put("monthlyCosts", monthlyCosts);
        forecast.put("projectedSavings", (projectedCost - currentCost) * 6);
        forecast.put("bestCaseSavings", (bestCaseCost - currentCost) * 6);
        forecast.put("worstCaseSavings", 0.0);

        return forecast;
    }

    /**
     * Scénario d'optimisation personnalisé
     */
    public static Map<String, Object> optimizationScenario(List<Abonnement> abonnements, String scenario) {
        Map<String, Object> result = new HashMap<>();

        double originalCost = abonnements.stream().mapToDouble(Abonnement::getPrixMensuel).sum();
        double newCost = originalCost;
        int actionsCount = 0;

        if ("aggressive".equalsIgnoreCase(scenario)) {
            // Supprimer tous les services peu utilisés et chers
            newCost = abonnements.stream()
                    .filter(a -> calculateValueScore(a) > 3 || calculateInactivityScore(a) < 5)
                    .mapToDouble(Abonnement::getPrixMensuel)
                    .sum();
            actionsCount = (int) abonnements.stream()
                    .filter(a -> calculateValueScore(a) <= 3 || calculateInactivityScore(a) >= 5)
                    .count();
        } else if ("moderate".equalsIgnoreCase(scenario)) {
            // Supprimer les plus mauvais, downgrader les moyens
            newCost = abonnements.stream()
                    .filter(a -> calculateValueScore(a) > 2)
                    .mapToDouble(a -> calculateValueScore(a) < 4 ? a.getPrixMensuel() * 0.7 : a.getPrixMensuel())
                    .sum();
            actionsCount = (int) abonnements.stream()
                    .filter(a -> calculateValueScore(a) <= 2 || calculateValueScore(a) < 4)
                    .count();
        } else {
            // Scénario conservative : juste consolidation
            newCost = originalCost * 0.95;
            actionsCount = 1;
        }

        result.put("scenario", scenario);
        result.put("originalCost", originalCost);
        result.put("newCost", Math.max(0, newCost));
        result.put("savings", Math.max(0, originalCost - newCost));
        result.put("savingsPercentage", ((originalCost - newCost) / originalCost) * 100);
        result.put("actionsCount", actionsCount);
        result.put("roi", "6 mois de remboursement via economie");

        return result;
    }

    private static BudgetAnalysis createEmptyAnalysis() {
        BudgetAnalysis analysis = new BudgetAnalysis();
        analysis.totalMonthlyCost = 0;
        analysis.potentialSavings = 0;
        analysis.savingsPercentage = 0;
        analysis.healthScore = 50;
        analysis.recommendations = new ArrayList<>();
        analysis.categorySummaries = new ArrayList<>();
        analysis.sixMonthForecast = new HashMap<>();
        return analysis;
    }
}
