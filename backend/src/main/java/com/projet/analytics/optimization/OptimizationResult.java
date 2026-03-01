package com.projet.analytics.optimization;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class OptimizationResult {

    private final List<OptimizationSuggestion> suggestions;
    private final double totalEconomiePotentielle;

    @JsonCreator
    public OptimizationResult(
        @JsonProperty("suggestions") List<OptimizationSuggestion> suggestions,
        @JsonProperty("totalEconomiePotentielle") double totalEconomiePotentielle
    ) {
        this.suggestions = suggestions != null ? suggestions : List.of();
        this.totalEconomiePotentielle = totalEconomiePotentielle;
    }

    // Ce constructeur est utilisé par le service pour calculer l'économie totale
    public OptimizationResult(List<OptimizationSuggestion> suggestions) {
        this.suggestions = suggestions;
        this.totalEconomiePotentielle = suggestions.stream()
                .mapToDouble(OptimizationSuggestion::getEconomiePotentielle)
                .sum();
    }

    public List<OptimizationSuggestion> getSuggestions() {
        return suggestions;
    }

    public double getTotalEconomiePotentielle() {
        return totalEconomiePotentielle;
    }
}
