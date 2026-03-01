package com.projet.analytics.forecast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents the result of a financial forecast.
 * This class holds the projected costs for a series of future periods (e.g., months).
 */
public class ForecastResult {

    private final Map<String, Double> projectedCosts;
    private final int months;
    private final double totalProjectedCost;
    private final double averageMonthlyCost;
    private final String maxMonth;
    private final double trendIndicator;

    /**
     * Constructs a new ForecastResult.
     *
     * @param projectedCosts A map where keys are period identifiers (e.g., "YYYY-MM")
     *                       and values are the projected costs for that period.
     * @param months The number of months included in the forecast.
     */
    public ForecastResult(Map<String, Double> projectedCosts, int months) {
        this.projectedCosts = projectedCosts;
        this.months = months;
        this.totalProjectedCost = projectedCosts.values().stream().mapToDouble(Double::doubleValue).sum();
        this.averageMonthlyCost = this.totalProjectedCost / (months > 0 ? months : 1);

        this.maxMonth = projectedCosts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
        
        List<Double> costs = new ArrayList<>(projectedCosts.values());
        if (costs.size() > 1) {
            double firstMonthCost = costs.get(0);
            double lastMonthCost = costs.get(costs.size() - 1);
            this.trendIndicator = (lastMonthCost - firstMonthCost) / (firstMonthCost > 0 ? firstMonthCost : 1);
        } else {
            this.trendIndicator = 0.0;
        }
    }

    // Getters
    public Map<String, Double> getProjectedCosts() {
        return projectedCosts;
    }

    public int getMonths() {
        return months;
    }

    public double getTotalProjectedCost() {
        return totalProjectedCost;
    }

    public double getAverageMonthlyCost() {
        return averageMonthlyCost;
    }

    public String getMaxMonth() {
        return maxMonth;
    }

    public double getTrendIndicator() {
        return trendIndicator;
    }
}
