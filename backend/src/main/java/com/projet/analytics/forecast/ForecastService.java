package com.projet.analytics.forecast;

import com.projet.backend.domain.Abonnement;
import java.util.List;

/**
 * Service for financial forecasting of subscription costs.
 */
public interface ForecastService {

    /**
     * Projects the future costs of a given list of subscriptions over a specified number of months.
     *
     * @param abonnements The list of subscriptions to analyze.
     * @param months The number of months to project costs for.
     * @return A ForecastResult containing the projected costs.
     */
    ForecastResult projectCosts(List<Abonnement> abonnements, int months);
}
