package com.projet.analytics.optimization;

import com.projet.backend.domain.Abonnement;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SubscriptionOptimizationServiceImpl implements SubscriptionOptimizationService {

    // Poids pour chaque facteur du score
    private static final double POIDS_COUT = 0.4;
    private static final double POIDS_UTILISATION = 0.3;
    private static final double POIDS_ENGAGEMENT = 0.2;
    private static final double POIDS_CATEGORIE = 0.1;

    // Seuils de décision
    private static final double SEUIL_RESILIER = 30;
    private static final double SEUIL_OPTIMISER = 60;

    @Override
    public OptimizationResult analyze(List<Abonnement> abonnements) {
        List<OptimizationSuggestion> suggestions = new ArrayList<>();
        for (Abonnement sub : abonnements) {
            if (!sub.estActif()) continue;

            double score = calculateScore(sub);
            OptimizationAction action = classify(score);
            String justification = generateJustification(sub, score, action);
            double economie = calculatePotentialSaving(sub, action);

            suggestions.add(new OptimizationSuggestion(sub, action, score, justification, economie));
        }
        return new OptimizationResult(suggestions);
    }

    private double calculateScore(Abonnement sub) {
        // Le score de base est de 100, auquel on soustrait des pénalités.
        double score = 100.0;

        // 1. Forte pénalité pour le coût élevé.
        // La pénalité augmente de manière non linéaire pour que les coûts élevés soient très sanctionnés.
        double costPenalty = Math.pow(sub.getPrixMensuel(), 1.2) * 1.5;
        score -= costPenalty;

        // 2. Forte pénalité pour l'inactivité.
        // La pénalité s'intensifie avec le temps pour marquer l'absence d'utilisation.
        long daysSinceLastUse = ChronoUnit.DAYS.between(
            Optional.ofNullable(sub.getDerniereUtilisation()).orElse(sub.getDateDebut()),
            LocalDate.now()
        );
        double inactivityPenalty = (daysSinceLastUse > 30) ? Math.pow(daysSinceLastUse / 30.0, 2) * 10 : (daysSinceLastUse / 10.0);
        score -= inactivityPenalty;

        // 3. Pénalité pour l'engagement futur.
        // Pénalise les engagements longs qui pourraient bloquer une résiliation.
        long engagementMonths = Optional.ofNullable(sub.getDateFin())
            .map(dateFin -> ChronoUnit.MONTHS.between(LocalDate.now(), dateFin))
            .filter(m -> m > 0)
            .orElse(0L);
        double engagementPenalty = engagementMonths * 1.5;
        score -= engagementPenalty;

        // 4. Bonus mineur pour la fréquence de paiement (impact limité).
        double frequencyBonus = switch (Optional.ofNullable(sub.getFrequencePaiement()).orElse("Mensuel")) {
            case "Annuel" -> 7.5;
            case "Semestriel" -> 4.0;
            case "Trimestriel" -> 2.0;
            default -> 0.0;
        };
        score += frequencyBonus;

        // Le score final est borné entre 0 et 100.
        return Math.max(0, Math.min(100, score));
    }

    private OptimizationAction classify(double score) {
        if (score < SEUIL_RESILIER) {
            return OptimizationAction.RESILIER;
        } else if (score < SEUIL_OPTIMISER) {
            return OptimizationAction.OPTIMISER;
        } else {
            return OptimizationAction.CONSERVER;
        }
    }

    private String generateJustification(Abonnement sub, double score, OptimizationAction action) {
        switch (action) {
            case RESILIER:
                return String.format("Le score de %.1f est très bas. L'abonnement à '%s' semble peu utilisé et/ou trop coûteux par rapport à sa valeur.", score, sub.getNomService());
            case OPTIMISER:
                return String.format("Score de %.1f. Il y a un potentiel d'optimisation pour '%s'. Envisagez un forfait inférieur ou une renégociation.", score, sub.getNomService());
            case CONSERVER:
                return String.format("Score élevé de %.1f. L'abonnement à '%s' semble rentable et bien utilisé.", score, sub.getNomService());
            default:
                return "";
        }
    }

    private double calculatePotentialSaving(Abonnement sub, OptimizationAction action) {
        switch (action) {
            case RESILIER:
                return sub.getPrixMensuel(); // Économie totale
            case OPTIMISER:
                return sub.getPrixMensuel() * 0.3; // Estimation d'économie de 30%
            default:
                return 0;
        }
    }
}
