package com.projet.analytics.forecast;

import com.projet.backend.domain.Abonnement;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Implementation of the ForecastService.
 */
public class ForecastServiceImpl implements ForecastService {

    private LocalDate advanceFrequency(LocalDate date, Abonnement sub) {
        return switch (sub.getFrequencePaiement()) {
            case "Trimestriel" -> date.plusMonths(3);
            case "Semestriel" -> date.plusMonths(6);
            case "Annuel" -> date.plusYears(1);
            default -> date.plusMonths(1);
        };
    }

    @Override
    public ForecastResult projectCosts(List<Abonnement> abonnements, int months) {
        if (months <= 0) {
            throw new IllegalArgumentException("Number of months must be positive.");
        }

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusMonths(months);
        Map<String, Double> projectedCosts = new TreeMap<>();

        List<Abonnement> activeSubscriptions = abonnements.stream()
                .filter(Abonnement::estActif)
                .collect(Collectors.toList());

        for (Abonnement sub : activeSubscriptions) {
            LocalDate nextRenewal = sub.getDateDebut();
            
            // Avancer la date de renouvellement jusqu'à ce qu'elle soit dans le futur ou aujourd'hui
            while (nextRenewal.isBefore(startDate)) {
                nextRenewal = advanceFrequency(nextRenewal, sub);
            }

            while (nextRenewal.isBefore(endDate)) {
                YearMonth ym = YearMonth.from(nextRenewal);
                String period = ym.toString(); // "YYYY-MM"

                double paymentAmount = switch (sub.getFrequencePaiement()) {
                    case "Annuel" -> sub.getPrixMensuel() * 12;
                    case "Semestriel" -> sub.getPrixMensuel() * 6;
                    case "Trimestriel" -> sub.getPrixMensuel() * 3;
                    default -> sub.getPrixMensuel();
                };
                
                projectedCosts.merge(period, paymentAmount, Double::sum);
                
                // Passer à la prochaine échéance
                nextRenewal = advanceFrequency(nextRenewal, sub);
            }
        }

        // Remplir les mois sans coûts pour assurer la continuité
        YearMonth currentMonth = YearMonth.from(startDate);
        for (int i = 0; i < months; i++) {
            String period = currentMonth.toString();
            projectedCosts.putIfAbsent(period, 0.0);
            currentMonth = currentMonth.plusMonths(1);
        }

        return new ForecastResult(projectedCosts, months);
    }
}
