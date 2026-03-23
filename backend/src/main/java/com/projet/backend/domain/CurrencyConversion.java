package com.projet.backend.domain;

import java.util.HashMap;
import java.util.Map;

/**
 * Représente le résultat d'une conversion de devises pour un abonnement.
 * 
 * Encapsule :
 * - L'ID de l'abonnement
 * - Le prix mensuel original (en EUR par défaut)
 * - Les prix convertis dans les différentes devises demandées
 * - Les taux de change utilisés
 * 
 * DTO utilisé pour la sérialisation JSON dans les réponses API.
 */
public class CurrencyConversion {
    
    /** Identifiant de l'abonnement */
    private String abonnementId;
    
    /** Nom du service abonné */
    private String nomService;
    
    /** Prix original (devise de base) */
    private double originalPrice;
    
    /** Devise de base (ex: EUR) */
    private String baseCurrency;
    
    /** Devises converties avec leurs prix */
    private Map<String, Double> convertedPrices = new HashMap<>();
    
    /** Taux de change utilisés */
    private Map<String, Double> exchangeRates = new HashMap<>();
    
    /** Timestamp de la conversion */
    private long timestamp = System.currentTimeMillis();

    // ========== CONSTRUCTEURS ==========
    
    public CurrencyConversion() {
    }

    public CurrencyConversion(String abonnementId, String nomService, double originalPrice, String baseCurrency) {
        this.abonnementId = abonnementId;
        this.nomService = nomService;
        this.originalPrice = originalPrice;
        this.baseCurrency = baseCurrency;
    }

    // ========== GETTERS/SETTERS ==========
    
    public String getAbonnementId() {
        return abonnementId;
    }

    public void setAbonnementId(String abonnementId) {
        this.abonnementId = abonnementId;
    }

    public String getNomService() {
        return nomService;
    }

    public void setNomService(String nomService) {
        this.nomService = nomService;
    }

    public double getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(double originalPrice) {
        this.originalPrice = originalPrice;
    }

    public String getBaseCurrency() {
        return baseCurrency;
    }

    public void setBaseCurrency(String baseCurrency) {
        this.baseCurrency = baseCurrency;
    }

    public Map<String, Double> getConvertedPrices() {
        return convertedPrices;
    }

    public void setConvertedPrices(Map<String, Double> convertedPrices) {
        this.convertedPrices = convertedPrices;
    }

    public Map<String, Double> getExchangeRates() {
        return exchangeRates;
    }

    public void setExchangeRates(Map<String, Double> exchangeRates) {
        this.exchangeRates = exchangeRates;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    // ========== MÉTHODES UTILITAIRES ==========
    
    /**
     * Ajoute une conversion pour une devise.
     */
    public void addConversion(String currency, double convertedPrice, double exchangeRate) {
        convertedPrices.put(currency, convertedPrice);
        exchangeRates.put(currency, exchangeRate);
    }

    @Override
    public String toString() {
        return "CurrencyConversion{" +
                "abonnementId='" + abonnementId + '\'' +
                ", nomService='" + nomService + '\'' +
                ", originalPrice=" + originalPrice +
                ", baseCurrency='" + baseCurrency + '\'' +
                ", convertedPrices=" + convertedPrices +
                ", exchangeRates=" + exchangeRates +
                ", timestamp=" + timestamp +
                '}';
    }
}
