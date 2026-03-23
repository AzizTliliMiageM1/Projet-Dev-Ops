package com.projet.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.projet.backend.domain.BenchmarkResult;

/**
 * Implémentation du service de benchmark.
 * 
 * Utilise ExternalBenchmarkService pour récupérer les données de marché
 * et génère les rapports de comparaison de prix.
 */
public class BenchmarkServiceImpl implements BenchmarkService {
    
    private static final Logger logger = LoggerFactory.getLogger(BenchmarkServiceImpl.class);
    
    private final ExternalBenchmarkService externalBenchmarkService;
    
    /**
     * Constructeur avec injection de la dépendance.
     * Permet une meilleure testabilité.
     */
    public BenchmarkServiceImpl(ExternalBenchmarkService externalBenchmarkService) {
        this.externalBenchmarkService = externalBenchmarkService;
    }
    
    /**
     * Constructeur par défaut (utilise l'implémentation standard).
     */
    public BenchmarkServiceImpl() {
        this(new ExternalBenchmarkServiceImpl());
    }

    @Override
    public BenchmarkResult benchmark(String abonnementId, String serviceName, double userPrice) {
        // Validation des entrées
        if (abonnementId == null || abonnementId.isEmpty()) {
            throw new IllegalArgumentException("abonnementId ne peut pas être vide");
        }
        if (serviceName == null || serviceName.isEmpty()) {
            throw new IllegalArgumentException("serviceName ne peut pas être vide");
        }
        if (userPrice < 0) {
            throw new IllegalArgumentException("userPrice ne peut pas être négatif");
        }
        
        try {
            logger.info("Benchmark en cours pour {} (€{})", serviceName, userPrice);
            
            // Récupérer les données de marché
            ExternalBenchmarkService.BenchmarkData marketData = 
                externalBenchmarkService.getMarketPrices(serviceName);
            
            if (marketData == null) {
                logger.warn("Données de marché non disponibles pour {}", serviceName);
                throw new RuntimeException("Données de marché indisponibles pour " + serviceName);
            }
            
            // Créer le résultat de benchmark
            BenchmarkResult result = new BenchmarkResult(
                abonnementId,
                serviceName,
                userPrice,
                marketData.averagePrice,
                marketData.minPrice,
                marketData.maxPrice
            );
            
            result.setRegion(marketData.region);
            
            logger.info("Benchmark complété : {} - Statut: {}", serviceName, result.getStatus());
            
            return result;
            
        } catch (Exception e) {
            logger.error("Erreur lors du benchmark pour {} : {}", serviceName, e.getMessage(), e);
            throw new RuntimeException("Erreur lors du benchmark : " + e.getMessage(), e);
        }
    }

    @Override
    public double getPriceRatio(double userPrice, String serviceName) {
        try {
            ExternalBenchmarkService.BenchmarkData marketData = 
                externalBenchmarkService.getMarketPrices(serviceName);
            
            if (marketData == null || marketData.averagePrice == 0) {
                return 1.0; // Ratio neutre
            }
            
            return userPrice / marketData.averagePrice;
            
        } catch (Exception e) {
            logger.warn("Erreur lors du calcul du ratio pour {} : {}", serviceName, e.getMessage());
            return 1.0; // Fallback : ratio neutre
        }
    }
}
