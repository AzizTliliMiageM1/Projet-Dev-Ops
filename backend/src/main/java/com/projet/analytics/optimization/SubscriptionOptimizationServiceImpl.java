package com.projet.analytics.optimization;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.projet.backend.domain.Abonnement;

public class SubscriptionOptimizationServiceImpl implements SubscriptionOptimizationService {

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

            suggestions.add(
                    new OptimizationSuggestion(sub, action, score, justification, economie)
            );
        }

        return new OptimizationResult(suggestions);
    }

    /**
     * Scoring calibré pour respecter les tests métier.
     */
    private double calculateScore(Abonnement sub) {

    // gratuit = toujours conservé
    if (sub.getPrixMensuel() == 0) {
        return 100.0;
    }

    double score = 100.0;

    // ----------------------------
    // coût — modéré
    // ----------------------------
    score -= sub.getPrixMensuel() * 0.8;

    // ----------------------------
    // inactivité — plus progressive
    // ----------------------------
    long daysInactive = ChronoUnit.DAYS.between(
            Optional.ofNullable(sub.getDerniereUtilisation())
                    .orElse(sub.getDateDebut()),
            LocalDate.now()
    );

    if (daysInactive > 30) {
        score -= (daysInactive - 30) * 0.7; // Inactivité pénalité
    }

    // ----------------------------
    // engagement — léger
    // ----------------------------
    long monthsRemaining = Optional.ofNullable(sub.getDateFin())
            .map(d -> ChronoUnit.MONTHS.between(LocalDate.now(), d))
            .filter(m -> m > 0)
            .orElse(0L);

    score -= monthsRemaining * 0.5;

    // ----------------------------
    // bonus fréquence
    // ----------------------------
    String freq = Optional.ofNullable(sub.getFrequencePaiement())
            .orElse("Mensuel");

    switch (freq) {
        case "Annuel" -> score += 10;
        case "Semestriel" -> score += 5;
        case "Trimestriel" -> score += 2;
    }

    return Math.max(0, Math.min(100, score));
}



    private OptimizationAction classify(double score) {

        if (score < SEUIL_RESILIER)
            return OptimizationAction.RESILIER;

        if (score < SEUIL_OPTIMISER)
            return OptimizationAction.OPTIMISER;

        return OptimizationAction.CONSERVER;
    }

    private String generateJustification(
            Abonnement sub,
            double score,
            OptimizationAction action
    ) {

        return switch (action) {

            case RESILIER -> String.format(
                    "Score %.1f — '%s' semble peu rentable.",
                    score,
                    sub.getNomService()
            );

            case OPTIMISER -> String.format(
                    "Score %.1f — optimisation possible pour '%s'.",
                    score,
                    sub.getNomService()
            );

            case CONSERVER -> String.format(
                    "Score %.1f — '%s' est rentable.",
                    score,
                    sub.getNomService()
            );
        };
    }

    private double calculatePotentialSaving(
            Abonnement sub,
            OptimizationAction action
    ) {

        return switch (action) {

            case RESILIER -> sub.getPrixMensuel();

            case OPTIMISER -> sub.getPrixMensuel() * 0.3;

            default -> 0;
        };
    }
}
