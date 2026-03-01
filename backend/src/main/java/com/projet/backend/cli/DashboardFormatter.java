package com.projet.backend.cli;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import com.projet.backend.domain.Abonnement;
import com.projet.backend.service.SubscriptionService;

/**
 * Formatte les données du portefeuille d'abonnements pour affichage texte riche
 * dans l'interface CLI.
 * 
 * Responsabilités:
 * - Formater les données métier en texte lisible
 * - Ajouter estruture visuelle (tableaux, séparateurs)
 * - Utiliser symboles et emojis pour clarté
 * - Respecter largeur console (~80 chars)
 */
public class DashboardFormatter {

    private static final String SEPARATOR = "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━";
    private static final String LIGHT_SEP = "───────────────────────────────────────";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Formate et retourne un dashboard complet du portefeuille
     */
    public static String formatPortfolioDashboard(List<Abonnement> abonnements) {
        if (abonnements == null || abonnements.isEmpty()) {
            return "Portefeuille vide. Aucun abonnement à afficher.";
        }

        StringBuilder sb = new StringBuilder();
        
        // Titre
        sb.append("\n");
        sb.append(String.format("📊 PORTEFEUILLE %s%n", LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy")).toUpperCase()));
        sb.append(SEPARATOR).append("\n");

        // Résumé global
        sb.append(formatGlobalSummary(abonnements)).append("\n");
        
        // Composition par catégorie
        sb.append(formatCompositionByCategory(abonnements)).append("\n");
        
        // À surveiller
        sb.append(formatHighRiskSubscriptions(abonnements)).append("\n");
        
        // Top priorités
        sb.append(formatTopPriorities(abonnements)).append("\n");
        
        // Opportunités d'économies
        sb.append(formatSavingOpportunities(abonnements)).append("\n");
        
        // Expirations proches
        sb.append(formatUpcomingExpirations(abonnements)).append("\n");

        return sb.toString();
    }

    /**
     * Section résumé global du portefeuille
     */
    private static String formatGlobalSummary(List<Abonnement> abonnements) {
        StringBuilder sb = new StringBuilder();
        
        SubscriptionService service = new SubscriptionService();
        SubscriptionService.PortfolioStats stats = service.calculatePortfolioStats(abonnements);
        
        // Résumé financier
        sb.append("💰 FINANCIER\n");
        sb.append(String.format("  Dépense mensuelle:      %.2f€%n", stats.totalMonthlyCost));
        sb.append(String.format("  Dépense moyenne/abo:    %.2f€%n", stats.averageMonthlyCost));
        double yearlyEstimate = stats.totalMonthlyCost * 12;
        sb.append(String.format("  Projection annuelle:    %.2f€%n", yearlyEstimate));
        
        // Score santé
        int healthPercentage = (int) stats.portfolioHealthScore;
        String healthEmoji = getHealthEmoji(healthPercentage);
        sb.append(String.format("  Score santé:            %s %d/100%n", healthEmoji, healthPercentage));
        
        // Statistiques abonnements
        sb.append("\n📈 STATISTIQUES\n");
        sb.append(String.format("  Total abonnements:      %d%n", stats.totalSubscriptions));
        sb.append(String.format("  En cours:               %d%n", stats.activeSubscriptions));
        sb.append(String.format("  Inactifs:               %d%n", stats.inactiveSubscriptions));
        sb.append(String.format("  Catégories:             %d%n", stats.categoriesDistribution.size()));
        sb.append(String.format("  À haut risque churn:    %d%n", stats.highChurnRiskCount));
        
        sb.append(LIGHT_SEP);
        return sb.toString();
    }

    /**
     * Section composition par catégorie
     */
    private static String formatCompositionByCategory(List<Abonnement> abonnements) {
        StringBuilder sb = new StringBuilder();
        SubscriptionService service = new SubscriptionService();
        
        sb.append("\n📁 COMPOSITION PAR CATÉGORIE\n");
        
        // Grouper par catégorie
        Map<String, List<Abonnement>> byCategory = new java.util.HashMap<>();
        for (Abonnement abo : abonnements) {
            String cat = abo.getCategorie() != null ? abo.getCategorie() : "Non classé";
            byCategory.computeIfAbsent(cat, k -> new java.util.ArrayList<>()).add(abo);
        }
        
        // Afficher par catégorie
        byCategory.forEach((category, abosList) -> {
            double categoryTotal = abosList.stream()
                .mapToDouble(Abonnement::getPrixMensuel)
                .sum();
            double percentage = (categoryTotal / abonnements.stream().mapToDouble(Abonnement::getPrixMensuel).sum()) * 100;
            
            sb.append(String.format("  %-20s %6.2f€  (%5.1f%%) [%d abo]%n", 
                category, categoryTotal, percentage, abosList.size()));
        });
        
        sb.append(LIGHT_SEP);
        return sb.toString();
    }

    /**
     * Section abonnements à haut risque
     */
    private static String formatHighRiskSubscriptions(List<Abonnement> abonnements) {
        StringBuilder sb = new StringBuilder();
        
        List<Abonnement> highRisk = abonnements.stream()
            .filter(a -> a.getChurnRisk() > 70)
            .collect(java.util.stream.Collectors.toList());
        
        sb.append("\n⚠️ À SURVEILLER (risque churn > 70)\n");
        
        if (highRisk.isEmpty()) {
            sb.append("  ✅ Aucun abonnement à risque\n");
        } else {
            highRisk.forEach(abo -> {
                int riskPct = (int) abo.getChurnRisk();
                String riskBar = createRiskBar(riskPct);
                sb.append(String.format("  %-30s [%s] %d%%%n", 
                    truncate(abo.getNomService(), 28), riskBar, riskPct));
            });
        }
        
        sb.append(LIGHT_SEP);
        return sb.toString();
    }

    /**
     * Section top priorités à conserver
     */
    private static String formatTopPriorities(List<Abonnement> abonnements) {
        StringBuilder sb = new StringBuilder();
        SubscriptionService service = new SubscriptionService();
        
        List<Abonnement> topPriority = service.getTopPrioritySubscriptions(abonnements);
        
        sb.append("\n✅ TOP À CONSERVER (meilleure valeur)\n");
        
        int rank = 1;
        for (Abonnement abo : topPriority) {
            double score = service.calculateRoiScore(abo);
            String emoji = getRankEmoji(rank);
            sb.append(String.format("  %s %-25s [Score: %.1f]%n", 
                emoji, truncate(abo.getNomService(), 23), score));
            rank++;
        }
        
        sb.append(LIGHT_SEP);
        return sb.toString();
    }

    /**
     * Section opportunités d'économies
     */
    private static String formatSavingOpportunities(List<Abonnement> abonnements) {
        StringBuilder sb = new StringBuilder();
        SubscriptionService service = new SubscriptionService();
        
        List<Abonnement> savings = service.identifySavingOpportunities(abonnements);
        
        sb.append("\n💡 ÉCONOMIES POTENTIELLES\n");
        
        if (savings.isEmpty()) {
            sb.append("  ✅ Aucune opportunité d'économies identifiée\n");
        } else {
            double totalSavings = savings.stream()
                .mapToDouble(Abonnement::getPrixMensuel)
                .sum();
            
            savings.forEach(abo -> {
                String reason = getEconomyReason(abo);
                sb.append(String.format("  • %-28s +%.2f€/mois (%s)%n",
                    truncate(abo.getNomService(), 26), abo.getPrixMensuel(), reason));
            });
            
            double percentage = (totalSavings / abonnements.stream().mapToDouble(Abonnement::getPrixMensuel).sum()) * 100;
            sb.append(String.format("  ─────────────────────────────────────%n"));
            sb.append(String.format("  💰 Total possible: +%.2f€/mois (%.1f%% d'économies)%n", 
                totalSavings, percentage));
        }
        
        sb.append(LIGHT_SEP);
        return sb.toString();
    }

    /**
     * Section expirations proches
     */
    private static String formatUpcomingExpirations(List<Abonnement> abonnements) {
        StringBuilder sb = new StringBuilder();
        SubscriptionService service = new SubscriptionService();
        
        LocalDate today = LocalDate.now();
        LocalDate in30Days = today.plusDays(30);
        
        List<Abonnement> expiring = service.getExpiringSubscriptions(abonnements, 30);
        
        sb.append("\n📅 EXPIRATIONS PROCHES (< 30 jours)\n");
        
        if (expiring.isEmpty()) {
            sb.append("  ✅ Aucune expiration prévue\n");
        } else {
            expiring.stream()
                .sorted((a, b) -> a.getDateFin().compareTo(b.getDateFin()))
                .forEach(abo -> {
                    long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(today, abo.getDateFin());
                    String urgencyEmoji = getUrgencyEmoji(daysLeft);
                    sb.append(String.format("  %s %-25s [%d jours] %s%n",
                        urgencyEmoji,
                        truncate(abo.getNomService(), 23),
                        daysLeft,
                        abo.getDateFin().format(DATE_FORMAT)));
                });
        }
        
        sb.append(LIGHT_SEP);
        return sb.toString();
    }

    // ===== HELPERS UTILITAIRES =====

    /**
     * Crée une barre visuelle pour l'affichage du risque
     */
    private static String createRiskBar(int percentage) {
        int filled = (percentage / 10);
        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < 10; i++) {
            bar.append(i < filled ? "█" : "░");
        }
        bar.append("]");
        return bar.toString();
    }

    /**
     * Retourne emoji selon le score de santé
     */
    private static String getHealthEmoji(int score) {
        if (score >= 80) return "🟢";
        if (score >= 60) return "🟡";
        if (score >= 40) return "🟠";
        return "🔴";
    }

    /**
     * Retourne emoji selon le rang
     */
    private static String getRankEmoji(int rank) {
        return switch (rank) {
            case 1 -> "🥇";
            case 2 -> "🥈";
            case 3 -> "🥉";
            default -> "  ";
        };
    }

    /**
     * Retourne emoji selon l'urgence d'expiration
     */
    private static String getUrgencyEmoji(long daysLeft) {
        if (daysLeft <= 7) return "🔴";
        if (daysLeft <= 14) return "🟠";
        return "🟡";
    }

    /**
     * Détermine la raison de l'opportunité d'économie
     */
    private static String getEconomyReason(Abonnement abo) {
        if (abo.getChurnRisk() > 70) {
            return "Haut risque churn";
        }
        
        if (abo.getDerniereUtilisation() != null) {
            long daysUnused = java.time.temporal.ChronoUnit.DAYS
                .between(abo.getDerniereUtilisation(), LocalDate.now());
            if (daysUnused > 60) {
                return "Inactif " + daysUnused + "j";
            }
        }
        
        return "À réévaluer";
    }

    /**
     * Tronque une chaîne à une longueur maximale
     */
    private static String truncate(String str, int maxLength) {
        if (str == null) return "";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 2) + "...";
    }
}
