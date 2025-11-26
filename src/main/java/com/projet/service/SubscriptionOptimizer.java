package com.projet.service;

import com.example.abonnement.Abonnement;
import com.projet.analytics.SubscriptionAnalytics;
import com.projet.analytics.SubscriptionAnalytics.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service d'optimisation des abonnements
 * Fournit des recommandations intelligentes et des analyses prédictives
 */
public class SubscriptionOptimizer {
    
    /**
     * Génère un rapport d'optimisation complet
     */
    public static OptimizationReport generateOptimizationReport(List<Abonnement> abonnements) {
        List<Abonnement> actifs = abonnements.stream()
            .filter(Abonnement::estActif)
            .collect(Collectors.toList());
        
        // Analyser chaque abonnement
        List<SubscriptionAnalysis> analyses = new ArrayList<>();
        for (Abonnement abo : actifs) {
            double valueScore = abo.getValueScore();
            double churnRisk = abo.getChurnRisk();
            double costPerUse = abo.getCostPerUse();
            boolean isAnomaly = SubscriptionAnalytics.detectPriceAnomaly(actifs, abo);
            
            String recommendation = generateRecommendation(abo, valueScore, churnRisk, isAnomaly);
            
            analyses.add(new SubscriptionAnalysis(
                abo, valueScore, churnRisk, costPerUse, isAnomaly, recommendation
            ));
        }
        
        // Trier par risque décroissant
        analyses.sort(Comparator.comparingDouble(SubscriptionAnalysis::getChurnRisk).reversed());
        
        // Calculer économies potentielles
        double economiesPotentielles = analyses.stream()
            .filter(a -> a.getChurnRisk() > 70)
            .mapToDouble(a -> a.getAbonnement().getPrixMensuel())
            .sum();
        
        // Détecter doublons
        List<String> duplicates = SubscriptionAnalytics.detectDuplicates(actifs);
        
        // Métriques avancées
        AdvancedMetrics metrics = SubscriptionAnalytics.calculateAdvancedMetrics(actifs);
        
        // Prévisions
        Map<String, Double> forecast = SubscriptionAnalytics.forecastCashflow(actifs, 6);
        
        return new OptimizationReport(analyses, economiesPotentielles, duplicates, metrics, forecast);
    }
    
    /**
     * Génère une recommandation personnalisée
     */
    private static String generateRecommendation(Abonnement abo, double valueScore, 
                                                 double churnRisk, boolean isAnomaly) {
        List<String> recommendations = new ArrayList<>();
        
        if (churnRisk > 70) {
            recommendations.add("🚨 CRITIQUE: Envisager annulation (" + 
                String.format("%.0f%% risque", churnRisk) + ")");
        } else if (churnRisk > 50) {
            recommendations.add("⚠️ À surveiller: Utilisation faible");
        }
        
        if (valueScore < 2) {
            recommendations.add("💸 Rapport qualité/prix faible (score: " + 
                String.format("%.2f", valueScore) + ")");
        } else if (valueScore > 5) {
            recommendations.add("⭐ Excellent rapport qualité/prix");
        }
        
        if (isAnomaly) {
            recommendations.add("📈 Prix anormalement élevé");
        }
        
        if (abo.getJoursAvantExpiration() < 30 && abo.getJoursAvantExpiration() > 0) {
            recommendations.add("🔔 Expire bientôt - Décider du renouvellement");
        }
        
        if (abo.isPartage() && abo.getNombreUtilisateurs() > 1) {
            double coutParPersonne = abo.getCoutParPersonne();
            recommendations.add(String.format("👥 Partagé: %.2f€/personne", coutParPersonne));
        }
        
        if (recommendations.isEmpty()) {
            recommendations.add("✅ Abonnement optimisé");
        }
        
        return String.join(" | ", recommendations);
    }
    
    /**
     * Trouve les meilleures opportunités d'économies
     */
    public static List<SavingOpportunity> findSavingOpportunities(List<Abonnement> abonnements) {
        List<SavingOpportunity> opportunities = new ArrayList<>();
        
        // Opportunité 1: Abonnements inutilisés
        List<Abonnement> unused = abonnements.stream()
            .filter(Abonnement::estActif)
            .filter(abo -> abo.getChurnRisk() > 70)
            .collect(Collectors.toList());
        
        if (!unused.isEmpty()) {
            double savings = unused.stream().mapToDouble(Abonnement::getPrixMensuel).sum();
            opportunities.add(new SavingOpportunity(
                "Supprimer abonnements inutilisés",
                unused.size() + " abonnement(s) peu ou pas utilisé(s)",
                savings,
                "high"
            ));
        }
        
        // Opportunité 2: Services redondants
        Map<String, List<Abonnement>> byCategory = abonnements.stream()
            .filter(Abonnement::estActif)
            .collect(Collectors.groupingBy(Abonnement::getCategorie));
        
        byCategory.forEach((category, list) -> {
            if (list.size() > 1) {
                double savings = list.stream()
                    .skip(1) // Garder le premier
                    .mapToDouble(Abonnement::getPrixMensuel)
                    .sum();
                opportunities.add(new SavingOpportunity(
                    "Réduire redondance: " + category,
                    list.size() + " services similaires détectés",
                    savings,
                    "medium"
                ));
            }
        });
        
        // Opportunité 3: Optimiser abonnements partagés
        List<Abonnement> notShared = abonnements.stream()
            .filter(Abonnement::estActif)
            .filter(abo -> !abo.isPartage() && abo.getPrixMensuel() > 10)
            .collect(Collectors.toList());
        
        if (!notShared.isEmpty()) {
            double potentialSavings = notShared.stream()
                .mapToDouble(abo -> abo.getPrixMensuel() * 0.5) // 50% si partagé avec 1 personne
                .sum();
            opportunities.add(new SavingOpportunity(
                "Partager les abonnements",
                notShared.size() + " abonnement(s) partageables",
                potentialSavings,
                "low"
            ));
        }
        
        // Trier par économies décroissantes
        opportunities.sort(Comparator.comparingDouble(SavingOpportunity::getSavings).reversed());
        
        return opportunities;
    }
    
    /**
     * Calcule le score d'optimisation global (0-100)
     */
    public static double calculateOptimizationScore(List<Abonnement> abonnements) {
        if (abonnements.isEmpty()) return 100;
        
        List<Abonnement> actifs = abonnements.stream()
            .filter(Abonnement::estActif)
            .collect(Collectors.toList());
        
        if (actifs.isEmpty()) return 100;
        
        // Critère 1: Taux d'utilisation (40%)
        double avgValueScore = actifs.stream()
            .mapToDouble(Abonnement::getValueScore)
            .average()
            .orElse(0);
        double usageScore = Math.min(100, avgValueScore * 20) * 0.4;
        
        // Critère 2: Absence de risques (30%)
        long highRisk = actifs.stream()
            .filter(abo -> abo.getChurnRisk() > 60)
            .count();
        double riskScore = (1 - (double) highRisk / actifs.size()) * 100 * 0.3;
        
        // Critère 3: Pas de redondances (20%)
        List<String> duplicates = SubscriptionAnalytics.detectDuplicates(actifs);
        double redundancyScore = (duplicates.isEmpty() ? 100 : 50) * 0.2;
        
        // Critère 4: Optimisation du partage (10%)
        long shared = actifs.stream().filter(Abonnement::isPartage).count();
        double shareScore = ((double) shared / actifs.size()) * 100 * 0.1;
        
        return Math.round((usageScore + riskScore + redundancyScore + shareScore) * 100) / 100.0;
    }
    
    // Classes internes
    public static class SubscriptionAnalysis {
        private final Abonnement abonnement;
        private final double valueScore;
        private final double churnRisk;
        private final double costPerUse;
        private final boolean priceAnomaly;
        private final String recommendation;
        
        public SubscriptionAnalysis(Abonnement abo, double valueScore, double churnRisk,
                                   double costPerUse, boolean priceAnomaly, String recommendation) {
            this.abonnement = abo;
            this.valueScore = valueScore;
            this.churnRisk = churnRisk;
            this.costPerUse = costPerUse;
            this.priceAnomaly = priceAnomaly;
            this.recommendation = recommendation;
        }
        
        public Abonnement getAbonnement() { return abonnement; }
        public double getValueScore() { return valueScore; }
        public double getChurnRisk() { return churnRisk; }
        public double getCostPerUse() { return costPerUse; }
        public boolean isPriceAnomaly() { return priceAnomaly; }
        public String getRecommendation() { return recommendation; }
        
        @Override
        public String toString() {
            return String.format(
                "%s - Score: %.2f | Risque: %.0f%% | Coût/usage: %.2f€\n  → %s",
                abonnement.getNomService(), valueScore, churnRisk, costPerUse, recommendation
            );
        }
    }
    
    public static class OptimizationReport {
        private final List<SubscriptionAnalysis> analyses;
        private final double economiesPotentielles;
        private final List<String> duplicateWarnings;
        private final AdvancedMetrics metrics;
        private final Map<String, Double> cashflowForecast;
        
        public OptimizationReport(List<SubscriptionAnalysis> analyses, double economies,
                                List<String> duplicates, AdvancedMetrics metrics,
                                Map<String, Double> forecast) {
            this.analyses = analyses;
            this.economiesPotentielles = economies;
            this.duplicateWarnings = duplicates;
            this.metrics = metrics;
            this.cashflowForecast = forecast;
        }
        
        public List<SubscriptionAnalysis> getAnalyses() { return analyses; }
        public double getEconomiesPotentielles() { return economiesPotentielles; }
        public List<String> getDuplicateWarnings() { return duplicateWarnings; }
        public AdvancedMetrics getMetrics() { return metrics; }
        public Map<String, Double> getCashflowForecast() { return cashflowForecast; }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("\n🎯 RAPPORT D'OPTIMISATION\n");
            sb.append("========================\n\n");
            
            sb.append("📊 Métriques Avancées:\n");
            sb.append(String.format("  - LTV moyen: %.2f€\n", metrics.getLifetimeValue()));
            sb.append(String.format("  - ROI moyen: %.2f%%\n", metrics.getAverageROI()));
            sb.append(String.format("  - Abonnements à risque: %d\n\n", metrics.getHighRiskCount()));
            
            sb.append("💰 Économies Potentielles: ").append(String.format("%.2f€/mois\n\n", economiesPotentielles));
            
            if (!duplicateWarnings.isEmpty()) {
                sb.append("⚠️ Redondances Détectées:\n");
                duplicateWarnings.forEach(w -> sb.append("  ").append(w).append("\n"));
                sb.append("\n");
            }
            
            sb.append("📈 Analyse Détaillée:\n");
            analyses.forEach(a -> sb.append("  ").append(a).append("\n\n"));
            
            sb.append("💵 Prévision Trésorerie (6 mois):\n");
            cashflowForecast.forEach((mois, cout) -> 
                sb.append(String.format("  %s: %.2f€\n", mois, cout))
            );
            
            return sb.toString();
        }
    }
    
    public static class SavingOpportunity {
        private final String action;
        private final String description;
        private final double savings;
        private final String priority;
        
        public SavingOpportunity(String action, String description, double savings, String priority) {
            this.action = action;
            this.description = description;
            this.savings = savings;
            this.priority = priority;
        }
        
        public String getAction() { return action; }
        public String getDescription() { return description; }
        public double getSavings() { return savings; }
        public String getPriority() { return priority; }
        
        @Override
        public String toString() {
            String emoji = switch (priority) {
                case "high" -> "🔴";
                case "medium" -> "🟡";
                case "low" -> "🟢";
                default -> "⚪";
            };
            return String.format("%s %s - %s (%.2f€/mois)", 
                emoji, action, description, savings);
        }
    }
}
