package com.projet.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.projet.backend.domain.Abonnement;

/**
 * DuplicateDetector - Détecteur intelligent de doublons avec fuzzy matching
 * 
 * Détecte les abonnements en doublon et chevauchants en utilisant :
 * - Levenshtein distance pour similarité textuelle
 * - Price range matching (±10%)
 * - Category overlap analysis
 * - Consolidation scoring intelligent
 */
public class DuplicateDetector {

    public static class DuplicateGroup {
        public List<Abonnement> members;
        public double totalCost;
        public double similarityScore; // 0-100
        public String groupType; // "EXACT_DUPLICATE", "SIMILAR_SERVICE", "OVERLAP"
        public List<ConsolidationSuggestion> suggestions;

        public DuplicateGroup() {
            this.members = new ArrayList<>();
            this.suggestions = new ArrayList<>();
        }
    }

    public static class ConsolidationSuggestion {
        public String serviceToKeep;
        public String serviceToRemove;
        public double potentialSavings;
        public String reason;
        public double confidence; // 0-100
        public int priority; // 1-10

        public ConsolidationSuggestion(String serviceToKeep, String serviceToRemove,
                                      double potentialSavings, String reason, double confidence, int priority) {
            this.serviceToKeep = serviceToKeep;
            this.serviceToRemove = serviceToRemove;
            this.potentialSavings = potentialSavings;
            this.reason = reason;
            this.confidence = confidence;
            this.priority = priority;
        }
    }

    public static class OverlapAnalysis {
        public Map<String, List<Abonnement>> overlapGroups;
        public Map<String, Double> consolidationPotential;
        public List<String> recommendations;
        public double totalPotentialSavings;

        public OverlapAnalysis() {
            this.overlapGroups = new HashMap<>();
            this.consolidationPotential = new HashMap<>();
            this.recommendations = new ArrayList<>();
            this.totalPotentialSavings = 0;
        }
    }

    /**
     * Analyse complète pour détecter les doublons
     */
    public static List<DuplicateGroup> detectDuplicates(List<Abonnement> abonnements) {
        List<DuplicateGroup> duplicateGroups = new ArrayList<>();
        Set<String> processed = new HashSet<>();

        for (int i = 0; i < abonnements.size(); i++) {
            if (processed.contains(abonnements.get(i).getId())) continue;

            Abonnement current = abonnements.get(i);
            DuplicateGroup group = new DuplicateGroup();
            group.members.add(current);
            processed.add(current.getId());

            // Chercher les doublons
            for (int j = i + 1; j < abonnements.size(); j++) {
                if (processed.contains(abonnements.get(j).getId())) continue;

                Abonnement other = abonnements.get(j);
                double similarity = calculateSimilarity(current, other);

                // Si similarité suffisante, ajouter au groupe
                if (similarity > 60) { // Threshold 60%
                    group.members.add(other);
                    processed.add(other.getId());
                }
            }

            // Si groupe de doublons trouvé (>1 membre)
            if (group.members.size() > 1) {
                group.totalCost = group.members.stream()
                        .mapToDouble(Abonnement::getPrixMensuel)
                        .sum();
                group.similarityScore = calculateGroupSimilarity(group.members);
                group.groupType = determineGroupType(group);
                group.suggestions = generateConsolidationSuggestions(group);

                duplicateGroups.add(group);
            }
        }

        return duplicateGroups;
    }

    /**
     * Calcule la similarité entre deux abonnements (0-100)
     */
    private static double calculateSimilarity(Abonnement abo1, Abonnement abo2) {
        double score = 0;
        int factors = 0;

        // Facteur 1: Similarité des noms (Levenshtein distance) - 40%
        double nameSimilarity = calculateLevenshteinSimilarity(
                normalizeString(abo1.getNomService()),
                normalizeString(abo2.getNomService())
        );
        score += nameSimilarity * 40;
        factors++;

        // Facteur 2: Similarité des prix (±10%) - 30%
        double priceSimilarity = calculatePriceSimilarity(abo1.getPrixMensuel(), abo2.getPrixMensuel());
        score += priceSimilarity * 30;
        factors++;

        // Facteur 3: Catégories identiques - 20%
        if (categoriesMatch(abo1.getCategorie(), abo2.getCategorie())) {
            score += 20;
        }
        factors++;

        // Facteur 4: Client identique - 10%
        if (abo1.getClientName() != null && abo1.getClientName().equals(abo2.getClientName())) {
            score += 10;
        }
        factors++;

        return score / factors;
    }

    /**
     * Calcule la distance de Levenshtein normalisée (0-100)
     * Plus haute = plus similaire
     */
    private static double calculateLevenshteinSimilarity(String s1, String s2) {
        int distance = levenshteinDistance(s1, s2);
        int maxLen = Math.max(s1.length(), s2.length());
        if (maxLen == 0) return 100;

        double similarity = 1 - ((double) distance / maxLen);
        return Math.max(0, Math.min(100, similarity * 100));
    }

    /**
     * Implémentation de la distance de Levenshtein
     */
    private static int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(Math.min(dp[i - 1][j], dp[i][j - 1]), dp[i - 1][j - 1]);
                }
            }
        }

        return dp[s1.length()][s2.length()];
    }

    /**
     * Calcule la similarité de prix (0-100)
     * 100 = prix identique, 0 = prix très différents
     */
    private static double calculatePriceSimilarity(double price1, double price2) {
        if (price1 == 0 && price2 == 0) return 100;
        if (price1 == 0 || price2 == 0) return 0;

        double minPrice = Math.min(price1, price2);
        double maxPrice = Math.max(price1, price2);
        double percentDiff = ((maxPrice - minPrice) / minPrice) * 100;

        if (percentDiff < 5) return 100; // Pratiquement identique
        if (percentDiff < 10) return 80;
        if (percentDiff < 15) return 60;
        if (percentDiff < 20) return 40;
        return Math.max(0, 100 - percentDiff);
    }

    /**
     * Normalise une string pour comparaison (minuscules, accents, espaces)
     */
    private static String normalizeString(String s) {
        if (s == null) return "";
        return s.toLowerCase()
                .replaceAll("[àâä]", "a")
                .replaceAll("[éèêë]", "e")
                .replaceAll("[ïî]", "i")
                .replaceAll("[ôö]", "o")
                .replaceAll("[ûü]", "u")
                .replaceAll("[ç]", "c")
                .replaceAll("\\s+", "")
                .replaceAll("[^a-z0-9]", "");
    }

    /**
     * Calcule la similarité moyenne d'un groupe
     */
    private static double calculateGroupSimilarity(List<Abonnement> members) {
        if (members.size() <= 1) return 100;

        double totalSimilarity = 0;
        int comparisons = 0;

        for (int i = 0; i < members.size(); i++) {
            for (int j = i + 1; j < members.size(); j++) {
                totalSimilarity += calculateSimilarity(members.get(i), members.get(j));
                comparisons++;
            }
        }

        return totalSimilarity / comparisons;
    }

    /**
     * Détermine le type de groupe (EXACT_DUPLICATE, SIMILAR_SERVICE, OVERLAP)
     */
    private static String determineGroupType(DuplicateGroup group) {
        double similarity = group.similarityScore;

        if (similarity >= 85) return "EXACT_DUPLICATE";
        if (similarity >= 70) return "SIMILAR_SERVICE";
        return "OVERLAP";
    }

    /**
     * Génère les suggestions de consolidation pour un groupe
     */
    private static List<ConsolidationSuggestion> generateConsolidationSuggestions(DuplicateGroup group) {
        List<ConsolidationSuggestion> suggestions = new ArrayList<>();

        if (group.members.size() < 2) return suggestions;

        // Trier par score de valeur (garder les meilleurs)
        List<Abonnement> sorted = group.members.stream()
                .sorted((a, b) -> Double.compare(
                        calculateServiceValue(b),
                        calculateServiceValue(a)
                ))
                .collect(Collectors.toList());

        Abonnement best = sorted.get(0);

        // Recommandations pour les autres
        for (int i = 1; i < sorted.size(); i++) {
            Abonnement toRemove = sorted.get(i);
            double savings = toRemove.getPrixMensuel();
            String reason = determineRemovalReason(best, toRemove);
            double confidence = group.similarityScore;
            int priority = (i == 1) ? 10 : (11 - i); // Plus haut pour les pires

            suggestions.add(new ConsolidationSuggestion(
                    best.getNomService(),
                    toRemove.getNomService(),
                    savings,
                    reason,
                    confidence,
                    priority
            ));
        }

        return suggestions;
    }

    /**
     * Calcule le score de valeur d'un service
     */
    private static double calculateServiceValue(Abonnement abo) {
        double score = 5;

        // Utilisation
        if (abo.getDerniereUtilisation() != null) {
            long days = java.time.temporal.ChronoUnit.DAYS.between(abo.getDerniereUtilisation(), java.time.LocalDate.now());
            if (days < 7) score += 3;
            else if (days < 30) score += 1;
            else if (days > 60) score -= 3;
        }

        // Prix bas = mieux
        if (abo.getPrixMensuel() < 10) score += 2;
        else if (abo.getPrixMensuel() > 25) score -= 2;

        // Priorité
        if ("Essential".equals(abo.getPriorite())) score += 2;
        if ("Luxe".equals(abo.getPriorite())) score -= 1;

        return Math.max(0, Math.min(10, score));
    }

    /**
     * Détermine la raison de la suppression recommandée
     */
    private static String determineRemovalReason(Abonnement toKeep, Abonnement toRemove) {
        if (calculateServiceValue(toKeep) > calculateServiceValue(toRemove)) {
            return "Le service " + toKeep.getNomService() + " offre une meilleure valeur";
        }
        
        if (toKeep.getPrixMensuel() < toRemove.getPrixMensuel()) {
            return "Moins cher : " + toKeep.getNomService() + " (" + toKeep.getPrixMensuel() + "€)";
        }

        if (toKeep.getDerniereUtilisation() != null && toRemove.getDerniereUtilisation() == null) {
            return toKeep.getNomService() + " est utilisé, contrairement à celui-ci";
        }

        return "Service redondant, fusion recommandée";
    }

    /**
     * Analyse les chevauchements de services par catégorie
     */
    public static OverlapAnalysis analyzeOverlaps(List<Abonnement> abonnements) {
        OverlapAnalysis analysis = new OverlapAnalysis();

        // Grouper par catégorie
        Map<String, List<Abonnement>> byCategory = abonnements.stream()
                .collect(Collectors.groupingBy(Abonnement::getCategorie));

        // Analyser chaque catégorie
        for (Map.Entry<String, List<Abonnement>> entry : byCategory.entrySet()) {
            String category = entry.getKey();
            List<Abonnement> services = entry.getValue();

            // S'il y a plusieurs services dans la même catégorie = chevauchement
            if (services.size() > 1) {
                analysis.overlapGroups.put(category, services);

                // Calcul potentiel d'économies = supprimer le moins utilisé
                Abonnement worst = services.stream()
                        .min((a, b) -> Double.compare(
                                calculateServiceValue(a),
                                calculateServiceValue(b)
                        ))
                        .orElse(null);

                if (worst != null) {
                    double savings = worst.getPrixMensuel();
                    analysis.consolidationPotential.put(category, savings);
                    analysis.totalPotentialSavings += savings;

                    String recommendation = "Catégorie " + category + ": consolidez les services. " +
                            "Supprimez " + worst.getNomService() + " et gardez les plus utilisés";
                    analysis.recommendations.add(recommendation);
                }
            }
        }

        return analysis;
    }

    /**
     * Comparaison avancée pour trouver les services similaires
     */
    public static Map<String, Object> advancedDuplicateReport(List<Abonnement> abonnements) {
        Map<String, Object> report = new HashMap<>();

        List<DuplicateGroup> groups = detectDuplicates(abonnements);
        OverlapAnalysis overlaps = analyzeOverlaps(abonnements);

        report.put("duplicateCount", groups.size());
        report.put("duplicateGroups", groups.size() > 0 ? groups.size() : "Aucun doublon détecté");
        report.put("totalPotentialSavings", groups.stream()
                .flatMap(g -> g.suggestions.stream())
                .mapToDouble(s -> s.potentialSavings)
                .sum() + overlaps.totalPotentialSavings);

        List<String> allRecommendations = new ArrayList<>();
        for (DuplicateGroup group : groups) {
            for (ConsolidationSuggestion sugg : group.suggestions) {
                allRecommendations.add("Supprimez " + sugg.serviceToRemove + 
                        " (économie: " + sugg.potentialSavings + "€/mois)");
            }
        }
        allRecommendations.addAll(overlaps.recommendations);

        report.put("recommendations", allRecommendations);
        report.put("analysisDate", java.time.LocalDate.now());
        report.put("overlapsByCategory", overlaps.overlapGroups.keySet());

        return report;
    }

    private static boolean categoriesMatch(String cat1, String cat2) {
        if (cat1 == null || cat2 == null) return false;
        return cat1.equalsIgnoreCase(cat2) ||
               (cat1.contains("Stream") && cat2.contains("Stream")) ||
               (cat1.contains("Music") && cat2.contains("Music")) ||
               (cat1.contains("Cloud") && cat2.contains("Cloud"));
    }
}
