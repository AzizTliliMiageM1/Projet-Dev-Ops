package com.projet.analytics.lifecycle;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.projet.analytics.SubscriptionAnalytics;
import com.projet.backend.domain.Abonnement;

/**
 * Calcule l'utilité (utility score) d'un abonnement pour un mois donné.
 * 
 * Formule:
 * score = (valueScore * 0.4 - costPenalty * 0.3 - churnRisk * 0.2 + predictedUsage * 0.1)
 * 
 * où :
 * - valueScore: Score de valeur perçue [0-10+]
 * - costPenalty: Pénalité de coût (normalisée)
 * - churnRisk: Risque de résiliation [0-100]
 * - predictedUsage: Utilisation prédite pour le mois
 */
public class SubscriptionUtilityCalculator {

    /**
     * Calcule l'utilité d'un abonnement pour un mois donné.
     * 
     * @param abo Abonnement à évaluer
     * @param monthIndex Index du mois (0 = mois courant, 1 = mois prochain, etc.)
     * @param maxPrice Prix maximum d'un abonnement (pour normalisation)
     * @return Utilité (score pondéré normalisé)
     */
    public double calculateUtility(Abonnement abo, int monthIndex, double maxPrice) {
        double valueScore = SubscriptionAnalytics.calculateValueScore(abo);
        double churnRisk = SubscriptionAnalytics.calculateChurnRisk(abo);
        
        // Pénalité de coût (normalisée)
        double costPenalty = normalizeCost(abo.getPrixMensuel(), maxPrice);
        
        // Utilisation prédite (décroît au fil des mois si pas d'utilisation)
        double predictedUsage = predictUsageForMonth(abo, monthIndex);
        
        // Calcul final : combinaison pondérée
        double utility = 
            (valueScore * 0.4) - 
            (costPenalty * 0.1) - 
            (churnRisk / 100.0 * 0.2) + 
            (predictedUsage * 0.1);
        
        // Normaliser le score entre 0 et 100
        return Math.max(0, Math.min(100, utility * 5));
    }

    /**
     * Normalise le coût en pénalité [0-10] pour équilibrer la formule.
     */
    private double normalizeCost(double price, double maxPrice) {
        if (maxPrice == 0) return 0;
        return (price / maxPrice) * 10;
    }

    /**
     * Prédit l'utilisation d'un abonnement pour un mois donné.
     * 
     * Logique :
     * - Si dernière utilisation < 7 jours : utilisation haute
     * - Si dernière utilisation < 30 jours : utilisation moyenne
     * - Sinon : utilisation basse
     * 
     * Décroissance : l'utilisation prédite diminue aux mois futurs sansutilisation récente
     */
    private double predictUsageForMonth(Abonnement abo, int monthIndex) {
        if (abo.getDerniereUtilisation() == null) {
            return 10 - (monthIndex * 2); // Décroît rapidement
        }

        long joursSansUtilisation = ChronoUnit.DAYS.between(abo.getDerniereUtilisation(), LocalDate.now());
        double baseUsage;

        if (joursSansUtilisation < 7) {
            baseUsage = 90; // Très actif
        } else if (joursSansUtilisation < 30) {
            baseUsage = 60; // Actif
        } else if (joursSansUtilisation < 60) {
            baseUsage = 30; // Peu actif
        } else {
            baseUsage = 10; // Dormant
        }

        // Décroissance pour les mois futurs
        double decay = baseUsage - (monthIndex * 5);
        return Math.max(0, decay);
    }
}
