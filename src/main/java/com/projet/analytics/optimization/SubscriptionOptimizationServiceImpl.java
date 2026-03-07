package com.projet.analytics.optimization;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import com.projet.backend.domain.Abonnement;

public class SubscriptionOptimizationServiceImpl implements SubscriptionOptimizationService {

    private static final double SEUIL_RESILIER = 30;
    private static final double SEUIL_OPTIMISER = 60;

    @Override
    public OptimizationResult analyze(List<Abonnement> abonnements) {
        List<OptimizationSuggestion> suggestions = new ArrayList<>();
        for (Abonnement sub : abonnements) {
            if (!sub.estActif()) {
                continue;
            }

            double score = calculateScore(sub);
            OptimizationAction action = classify(score);
            String justification = generateJustification(sub, score, action);
            double economie = calculatePotentialSaving(sub, action);

            suggestions.add(new OptimizationSuggestion(sub, action, score, justification, economie));
        }
        return new OptimizationResult(suggestions);
    }

    private double calculateScore(Abonnement sub) {
        if (sub.getPrixMensuel() <= 0) {
            return 100.0;
        }

        double score = 100.0;

        double costPenalty = sub.getPrixMensuel() * 2.0;
        if (costPenalty > 50.0) {
            costPenalty = 50.0;
        }
        score -= costPenalty;

        LocalDate referenceUsage = sub.getDerniereUtilisation() != null ? sub.getDerniereUtilisation() : sub.getDateDebut();
        if (referenceUsage == null) {
            referenceUsage = LocalDate.now();
        }
        long daysSinceLastUse = ChronoUnit.DAYS.between(referenceUsage, LocalDate.now());
        if (daysSinceLastUse < 0) {
            daysSinceLastUse = 0;
        }
        double inactivityPenalty = daysSinceLastUse / 4.0;
        if (inactivityPenalty > 50.0) {
            inactivityPenalty = 50.0;
        }
        score -= inactivityPenalty;

        long monthsRemaining = 0;
        if (sub.getDateFin() != null) {
            monthsRemaining = ChronoUnit.MONTHS.between(LocalDate.now(), sub.getDateFin());
            if (monthsRemaining < 0) {
                monthsRemaining = 0;
            }
        }
        double engagementPenalty = monthsRemaining;
        if (engagementPenalty > 15.0) {
            engagementPenalty = 15.0;
        }
        score -= engagementPenalty;

        if (score < 0.0) {
            score = 0.0;
        }
        if (score > 100.0) {
            score = 100.0;
        }
        return score;
    }

    private OptimizationAction classify(double score) {
        if (score < SEUIL_RESILIER) {
            return OptimizationAction.RESILIER;
        }
        if (score < SEUIL_OPTIMISER) {
            return OptimizationAction.OPTIMISER;
        }
        return OptimizationAction.CONSERVER;
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
                return sub.getPrixMensuel();
            case OPTIMISER:
                return sub.getPrixMensuel() * 0.3;
            default:
                return 0;
        }
    }
}
