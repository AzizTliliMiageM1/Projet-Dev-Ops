package com.projet.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implémentation du service de taux de change avec ExchangeRate-API.
 * 
 * Cette implémentation utilise HttpURLConnection (built-in) pour éviter
 * les dépendances externes inutiles.
 * 
 * Utilise l'API : https://api.exchangerate-api.com/v4/latest/{base}
 * 
 * Gestion des erreurs :
 * - Timeout : 5 secondes par défaut
 * - Fallback : Retourne des taux par défaut si l'API est indisponible
 * - Logging : Tous les problèmes sont loggés
 */
public class ExchangeRateServiceImpl implements ExchangeRateService {
    
    private static final Logger logger = LoggerFactory.getLogger(ExchangeRateServiceImpl.class);
    
    private static final String API_BASE = "https://api.exchangerate-api.com/v4/latest/";
    private static final int TIMEOUT_MS = 5000; // 5 secondes
    private static final ObjectMapper mapper = new ObjectMapper();
    
    /**
     * Taux de change par défaut (fallback) en cas d'indisponibilité de l'API.
     * Ces taux sont approx et permettent la continuité de service.
     */
    private static final Map<String, Double> FALLBACK_RATES = Map.of(
        "USD", 1.08,
        "GBP", 0.86,
        "CHF", 0.96,
        "JPY", 157.0,
        "CAD", 1.45,
        "AUD", 1.62,
        "CNY", 7.75,
        "INR", 89.5,
        "EUR", 1.0
    );

    @Override
    public Map<String, Double> getExchangeRates(String baseCurrency, String targetCurrencies) {
        if (baseCurrency == null || baseCurrency.isEmpty()) {
            baseCurrency = "EUR";
        }
        baseCurrency = baseCurrency.toUpperCase();
        
        try {
            String url = API_BASE + baseCurrency;
            logger.info("Récupération des taux de change depuis {}", url);
            
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);
            conn.setRequestProperty("Accept", "application/json");
            
            int status = conn.getResponseCode();
            if (status != 200) {
                logger.warn("API ExchangeRate retourné le status {}, fallback activé", status);
                return getFallbackRates(targetCurrencies);
            }
            
            // Parser la réponse JSON
            String response = readResponse(conn);
            JsonNode root = mapper.readTree(response);
            JsonNode rates = root.get("rates");
            
            // Extraire les devises demandées
            Map<String, Double> result = new HashMap<>();
            if (targetCurrencies != null && !targetCurrencies.isEmpty()) {
                for (String currency : targetCurrencies.split(",")) {
                    currency = currency.trim().toUpperCase();
                    JsonNode rateNode = rates.get(currency);
                    if (rateNode != null && rateNode.isNumber()) {
                        result.put(currency, rateNode.asDouble());
                    }
                }
            } else {
                // Si pas de devises spécifiées, retourner les devises principales
                for (String currency : FALLBACK_RATES.keySet()) {
                    JsonNode rateNode = rates.get(currency);
                    if (rateNode != null && rateNode.isNumber()) {
                        result.put(currency, rateNode.asDouble());
                    }
                }
            }
            
            conn.disconnect();
            logger.info("Taux de change récupérés avec succès : {}", result.keySet());
            return result;
            
        } catch (IOException e) {
            logger.error("Erreur lors de la récupération des taux de change : {}", e.getMessage());
            return getFallbackRates(targetCurrencies);
        } catch (Exception e) {
            logger.error("Erreur inattendue lors de la récupération des taux : {}", e.getMessage(), e);
            return getFallbackRates(targetCurrencies);
        }
    }

    @Override
    public double convertAmount(double amount, String fromCurrency, String toCurrency) {
        if (fromCurrency == null || toCurrency == null) {
            throw new IllegalArgumentException("Les devises ne peuvent pas être nulles");
        }
        
        if (fromCurrency.equalsIgnoreCase(toCurrency)) {
            return amount;
        }
        
        try {
            Map<String, Double> rates = getExchangeRates(fromCurrency, toCurrency);
            Double rate = rates.get(toCurrency.toUpperCase());
            
            if (rate == null) {
                logger.warn("Taux de change non trouvé pour {} -> {}", fromCurrency, toCurrency);
                return amount; // Fallback : retourner le montant original
            }
            
            return amount * rate;
        } catch (Exception e) {
            logger.error("Erreur lors de la conversion {} -> {} : {}", 
                fromCurrency, toCurrency, e.getMessage());
            return amount; // Fallback : retourner le montant original
        }
    }

    /**
     * Lit la réponse HTTP complète.
     */
    private String readResponse(HttpURLConnection conn) throws IOException {
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }
        return response.toString();
    }

    /**
     * Retourne les taux de change par défaut en cas d'indisponibilité.
     */
    private Map<String, Double> getFallbackRates(String targetCurrencies) {
        Map<String, Double> fallback = new HashMap<>();
        
        if (targetCurrencies != null && !targetCurrencies.isEmpty()) {
            for (String currency : targetCurrencies.split(",")) {
                currency = currency.trim().toUpperCase();
                Double rate = FALLBACK_RATES.get(currency);
                if (rate != null) {
                    fallback.put(currency, rate);
                }
            }
        } else {
            fallback.putAll(FALLBACK_RATES);
        }
        
        logger.info("Utilisation des taux de change par défaut : {}", fallback.keySet());
        return fallback;
    }
}
