package com.projet.analytics;

import com.example.abonnement.Abonnement;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Classe d'analyse avancée des abonnements avec algorithmes prédictifs
 */
public class SubscriptionAnalytics {
    
    /**
     * Calcule le score de valeur perçue d'un abonnement
     * Formule: (fréquenceUtilisation * 10) / prixMensuel
     * @return Score entre 0 et 10+ (>5 = Excellent, 2-5 = Bon, <2 = À réévaluer)
     */
    public static double calculateValueScore(Abonnement abo) {
        if (abo.getPrixMensuel() == 0) return 0;
        
        // Calculer fréquence d'utilisation (utilisations par mois)
        double frequence = calculateUsageFrequency(abo);
        double score = (frequence * 10) / abo.getPrixMensuel();
        
        return Math.round(score * 100.0) / 100.0;
    }
    
    /**
     * Calcule la fréquence d'utilisation mensuelle estimée
     */
    private static double calculateUsageFrequency(Abonnement abo) {
        if (abo.getDerniereUtilisation() == null) return 0;
        
        long joursSansUtilisation = ChronoUnit.DAYS.between(abo.getDerniereUtilisation(), LocalDate.now());
        
        if (joursSansUtilisation < 7) return 20; // Utilisé récemment = haute fréquence
        if (joursSansUtilisation < 30) return 10;
        if (joursSansUtilisation < 60) return 5;
        return 1; // Rarement utilisé
    }
    
    /**
     * Calcule le risque de résiliation (Churn Risk)
     * @return Score 0-100% (0 = pas de risque, 100 = très haut risque)
     */
    public static double calculateChurnRisk(Abonnement abo) {
        double riskScore = 0;
        
        // Facteur 1: Utilisation décroissante (40% du score)
        if (abo.getDerniereUtilisation() != null) {
            long joursSansUtilisation = ChronoUnit.DAYS.between(abo.getDerniereUtilisation(), LocalDate.now());
            if (joursSansUtilisation > 60) riskScore += 40;
            else if (joursSansUtilisation > 30) riskScore += 25;
            else if (joursSansUtilisation > 14) riskScore += 10;
        } else {
            riskScore += 40; // Jamais utilisé
        }
        
        // Facteur 2: Ratio coût/utilisation (30% du score)
        double valueScore = calculateValueScore(abo);
        if (valueScore < 1) riskScore += 30;
        else if (valueScore < 2) riskScore += 20;
        else if (valueScore < 3) riskScore += 10;
        
        // Facteur 3: Priorité (20% du score)
        String priorite = abo.getPriorite();
        if ("Luxe".equals(priorite)) riskScore += 20;
        else if ("Optionnel".equals(priorite)) riskScore += 10;
        
        // Facteur 4: Proche de l'expiration (10% du score)
        long joursAvantExpiration = abo.getJoursAvantExpiration();
        if (joursAvantExpiration < 30) riskScore += 10;
        
        return Math.min(100, Math.round(riskScore * 100.0) / 100.0);
    }
    
    /**
     * Calcule le coût par utilisation
     */
    public static double calculateCostPerUse(Abonnement abo) {
        double frequence = calculateUsageFrequency(abo);
        if (frequence == 0) return abo.getPrixMensuel();
        return Math.round((abo.getPrixMensuel() / frequence) * 100.0) / 100.0;
    }
    
    /**
     * Détecte les anomalies de dépenses
     */
    public static boolean detectPriceAnomaly(List<Abonnement> abonnements, Abonnement abo) {
        if (abonnements.size() < 3) return false;
        
        // Calculer moyenne et écart-type des prix
        double moyenne = abonnements.stream()
            .mapToDouble(Abonnement::getPrixMensuel)
            .average()
            .orElse(0);
        
        double variance = abonnements.stream()
            .mapToDouble(a -> Math.pow(a.getPrixMensuel() - moyenne, 2))
            .average()
            .orElse(0);
        
        double ecartType = Math.sqrt(variance);
        
        // Anomalie si > moyenne + 2*écart-type
        return abo.getPrixMensuel() > (moyenne + 2 * ecartType);
    }
    
    /**
     * Optimise le budget en identifiant les abonnements à supprimer
     */
    public static List<OptimizationSuggestion> optimizeBudget(List<Abonnement> abonnements, double budgetCible) {
        List<OptimizationSuggestion> suggestions = new ArrayList<>();
        
        double coutTotal = abonnements.stream()
            .filter(Abonnement::estActif)
            .mapToDouble(Abonnement::getPrixMensuel)
            .sum();
        
        if (coutTotal <= budgetCible) {
            return suggestions; // Déjà dans le budget
        }
        
        double economiesRequises = coutTotal - budgetCible;
        
        // Trier par score de valeur (du plus faible au plus élevé)
        List<Abonnement> candidats = abonnements.stream()
            .filter(Abonnement::estActif)
            .sorted(Comparator.comparingDouble(SubscriptionAnalytics::calculateValueScore))
            .collect(Collectors.toList());
        
        double economiesCumulees = 0;
        for (Abonnement abo : candidats) {
            if (economiesCumulees >= economiesRequises) break;
            
            double valueScore = calculateValueScore(abo);
            double churnRisk = calculateChurnRisk(abo);
            
            suggestions.add(new OptimizationSuggestion(
                abo,
                "Supprimer",
                abo.getPrixMensuel(),
                String.format("Score valeur: %.2f | Risque résiliation: %.0f%%", valueScore, churnRisk)
            ));
            
            economiesCumulees += abo.getPrixMensuel();
        }
        
        return suggestions;
    }
    
    /**
     * Identifie les abonnements redondants
     */
    public static List<String> detectDuplicates(List<Abonnement> abonnements) {
        List<String> warnings = new ArrayList<>();
        Map<String, Long> categoryCounts = abonnements.stream()
            .filter(Abonnement::estActif)
            .collect(Collectors.groupingBy(
                Abonnement::getCategorie,
                Collectors.counting()
            ));
        
        categoryCounts.forEach((categorie, count) -> {
            if (count > 1) {
                warnings.add(String.format(
                    "⚠️ %d services dans la catégorie '%s' - Vérifier redondance",
                    count, categorie
                ));
            }
        });
        
        return warnings;
    }
    
    /**
     * Prévoit les dépenses futures
     */
    public static Map<String, Double> forecastCashflow(List<Abonnement> abonnements, int moisFuturs) {
        Map<String, Double> previsions = new LinkedHashMap<>();
        LocalDate dateActuelle = LocalDate.now();
        
        for (int i = 0; i < moisFuturs; i++) {
            LocalDate mois = dateActuelle.plusMonths(i);
            String moisLabel = String.format("%d-%02d", mois.getYear(), mois.getMonthValue());
            
            double coutMois = abonnements.stream()
                .filter(abo -> {
                    LocalDate prochaineEcheance = abo.getProchaineEcheance();
                    return prochaineEcheance != null &&
                           prochaineEcheance.getYear() == mois.getYear() &&
                           prochaineEcheance.getMonthValue() == mois.getMonthValue();
                })
                .mapToDouble(Abonnement::getPrixMensuel)
                .sum();
            
            // Ajouter inflation de 2% par an
            double inflation = 1 + (0.02 * i / 12.0);
            previsions.put(moisLabel, Math.round(coutMois * inflation * 100.0) / 100.0);
        }
        
        return previsions;
    }
    
    /**
     * Calcule les métriques avancées
     */
    public static AdvancedMetrics calculateAdvancedMetrics(List<Abonnement> abonnements) {
        double ltv = abonnements.stream()
            .mapToDouble(Abonnement::getCoutTotal)
            .average()
            .orElse(0);
        
        double roi = abonnements.stream()
            .mapToDouble(abo -> {
                double valueScore = calculateValueScore(abo);
                return valueScore > 0 ? (valueScore - 1) * 100 : 0;
            })
            .average()
            .orElse(0);
        
        long abonnementsRisque = abonnements.stream()
            .filter(abo -> calculateChurnRisk(abo) > 60)
            .count();
        
        return new AdvancedMetrics(ltv, roi, abonnementsRisque);
    }
    
    /**
     * Génère un rapport mensuel intelligent
     */
    public static MonthlyReport generateMonthlyReport(List<Abonnement> abonnements) {
        List<Abonnement> top3Depenses = abonnements.stream()
            .filter(Abonnement::estActif)
            .sorted(Comparator.comparingDouble(Abonnement::getPrixMensuel).reversed())
            .limit(3)
            .collect(Collectors.toList());
        
        List<Abonnement> inutilises = abonnements.stream()
            .filter(abo -> calculateChurnRisk(abo) > 70)
            .collect(Collectors.toList());
        
        double economiesPotentielles = inutilises.stream()
            .mapToDouble(Abonnement::getPrixMensuel)
            .sum();
        
        List<String> recommendations = new ArrayList<>();
        
        // Recommandations personnalisées
        if (!inutilises.isEmpty()) {
            recommendations.add(String.format(
                "💰 Économisez %.2f€/mois en supprimant %d abonnement(s) peu utilisé(s)",
                economiesPotentielles, inutilises.size()
            ));
        }
        
        List<String> duplicates = detectDuplicates(abonnements);
        recommendations.addAll(duplicates);
        
        long abonnementsExpireSoon = abonnements.stream()
            .filter(abo -> abo.getJoursAvantExpiration() < 30 && abo.getJoursAvantExpiration() > 0)
            .count();
        
        if (abonnementsExpireSoon > 0) {
            recommendations.add(String.format(
                "⏰ %d abonnement(s) expire(nt) dans moins de 30 jours - Pensez à renouveler",
                abonnementsExpireSoon
            ));
        }
        
        return new MonthlyReport(top3Depenses, inutilises, economiesPotentielles, recommendations);
    }
    
    // Classes internes pour les résultats
    public static class OptimizationSuggestion {
        private final Abonnement abonnement;
        private final String action;
        private final double economie;
        private final String raison;
        
        public OptimizationSuggestion(Abonnement abonnement, String action, double economie, String raison) {
            this.abonnement = abonnement;
            this.action = action;
            this.economie = economie;
            this.raison = raison;
        }
        
        public Abonnement getAbonnement() { return abonnement; }
        public String getAction() { return action; }
        public double getEconomie() { return economie; }
        public String getRaison() { return raison; }
        
        @Override
        public String toString() {
            return String.format("%s %s - Économie: %.2f€ (%s)", 
                action, abonnement.getNomService(), economie, raison);
        }
    }
    
    public static class AdvancedMetrics {
        private final double lifetimeValue;
        private final double averageROI;
        private final long highRiskCount;
        
        public AdvancedMetrics(double lifetimeValue, double averageROI, long highRiskCount) {
            this.lifetimeValue = lifetimeValue;
            this.averageROI = averageROI;
            this.highRiskCount = highRiskCount;
        }
        
        public double getLifetimeValue() { return lifetimeValue; }
        public double getAverageROI() { return averageROI; }
        public long getHighRiskCount() { return highRiskCount; }
    }
    
    public static class MonthlyReport {
        private final List<Abonnement> top3Depenses;
        private final List<Abonnement> abonnementsInutilises;
        private final double economiesPotentielles;
        private final List<String> recommendations;
        
        public MonthlyReport(List<Abonnement> top3, List<Abonnement> inutilises, 
                           double economies, List<String> recommendations) {
            this.top3Depenses = top3;
            this.abonnementsInutilises = inutilises;
            this.economiesPotentielles = economies;
            this.recommendations = recommendations;
        }
        
        public List<Abonnement> getTop3Depenses() { return top3Depenses; }
        public List<Abonnement> getAbonnementsInutilises() { return abonnementsInutilises; }
        public double getEconomiesPotentielles() { return economiesPotentielles; }
        public List<String> getRecommendations() { return recommendations; }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("\n📊 RAPPORT MENSUEL\n");
            sb.append("===================\n\n");
            
            sb.append("💰 Top 3 Dépenses:\n");
            top3Depenses.forEach(abo -> 
                sb.append(String.format("  - %s: %.2f€\n", abo.getNomService(), abo.getPrixMensuel()))
            );
            
            sb.append(String.format("\n⚠️ Abonnements inutilisés: %d (%.2f€ d'économies potentielles)\n", 
                abonnementsInutilises.size(), economiesPotentielles));
            
            sb.append("\n💡 Recommandations:\n");
            recommendations.forEach(rec -> sb.append("  ").append(rec).append("\n"));
            
            return sb.toString();
        }
    }
}
