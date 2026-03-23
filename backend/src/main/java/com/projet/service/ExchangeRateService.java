package com.projet.service;

import java.util.Map;

/**
 * Service d'intégration avec une API externe de taux de change.
 * 
 * Responsabilités :
 * - Récupérer les taux de change actuels
 * - Convertir des montants entre devises
 * - Gérer les erreurs de l'API externe
 * - Fournir des fallbacks en cas d'indisponibilité
 * 
 * Cette interface permet une abstraction sur le fournisseur d'API.
 */
public interface ExchangeRateService {

    /**
     * Récupère les taux de change pour une devise de base
     * vers un ensemble de devises cibles.
     * 
     * @param baseCurrency La devise de base (ex: EUR, USD)
     * @param targetCurrencies Les devises cibles séparées par des virgules (ex: USD,GBP,CHF)
     * @return Map<devise, taux> où le taux indique combien de 1 unité de base = X unités cibles
     * @throws RuntimeException Si la récupération des taux échoue définitivement
     */
    Map<String, Double> getExchangeRates(String baseCurrency, String targetCurrencies);

    /**
     * Convertit un montant d'une devise à une autre.
     * 
     * @param amount Le montant à convertir
     * @param fromCurrency Devise source (ex: EUR)
     * @param toCurrency Devise cible (ex: USD)
     * @return Le montant converti
     * @throws RuntimeException Si la conversion échoue
     */
    double convertAmount(double amount, String fromCurrency, String toCurrency);
}
