package com.projet.service;

import java.io.BufferedReader;
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
 * Implémentation du service de benchmark de prix d'abonnements.
 * 
 * Cette implémentation :
 * - Appelle une API externe de prix de marché (ou simule une réponse)
 * - Gère les timeouts et erreurs gracieusement
 * - Fournit des prix de fallback basés sur des données clés
 * - Logging complet pour la traçabilité
 * 
 * Pour la démo, utilise des données simulées mais le pattern permet
 * facilement de connecter une vraie API de pricing.
 */
public class ExternalBenchmarkServiceImpl implements ExternalBenchmarkService {
    
    private static final Logger logger = LoggerFactory.getLogger(ExternalBenchmarkServiceImpl.class);
    
    private static final int TIMEOUT_MS = 5000; // 5 secondes
    private static final ObjectMapper mapper = new ObjectMapper();
    
    /**
     * Base de données de fallback avec les prix du marché pour services populaires.
     * Ces données sont approximatives et basées sur les services réels en 2026.
     */
    private static final Map<String, BenchmarkData> MARKET_DATABASE = initializeMarketDB();
    
    private static Map<String, BenchmarkData> initializeMarketDB() {
        Map<String, BenchmarkData> db = new HashMap<>();
        
        // Streaming & Entertainment
        db.put("Netflix", new BenchmarkData("Netflix", 15.99, 6.99, 22.99, "EUR", "Europe"));
        db.put("Spotify", new BenchmarkData("Spotify", 11.99, 0.00, 14.99, "EUR", "Europe"));
        db.put("Disney+", new BenchmarkData("Disney+", 10.99, 10.99, 10.99, "EUR", "Europe"));
        db.put("Amazon Prime Video", new BenchmarkData("Amazon Prime Video", 14.99, 0.00, 14.99, "EUR", "Europe"));
        
        // Productivity & Tools
        db.put("Microsoft 365", new BenchmarkData("Microsoft 365", 9.99, 9.99, 19.99, "EUR", "Europe"));
        db.put("Adobe Creative Cloud", new BenchmarkData("Adobe Creative Cloud", 64.99, 19.99, 64.99, "EUR", "Europe"));
        db.put("Slack", new BenchmarkData("Slack", 8.99, 0.00, 12.99, "EUR", "Europe"));
        db.put("Dropbox", new BenchmarkData("Dropbox", 11.99, 0.00, 19.99, "EUR", "Europe"));
        
        // Fitness & Health
        db.put("Fitbit Premium", new BenchmarkData("Fitbit Premium", 10.99, 0.00, 10.99, "EUR", "Europe"));
        db.put("Apple Fitness+", new BenchmarkData("Apple Fitness+", 12.99, 0.00, 12.99, "EUR", "Europe"));
        
        // News & Reading
        db.put("Medium", new BenchmarkData("Medium", 12.99, 0.00, 12.99, "EUR", "Europe"));
        db.put("The New York Times", new BenchmarkData("The New York Times", 17.00, 1.00, 25.00, "EUR", "Europe"));
        
        // Cloud & Development
        db.put("GitHub Pro", new BenchmarkData("GitHub Pro", 4.00, 0.00, 4.00, "EUR", "Europe"));
        db.put("JetBrains IDE", new BenchmarkData("JetBrains IDE", 19.99, 14.99, 199.00, "EUR", "Europe"));
        
        return db;
    }
    
    @Override
    public BenchmarkData getMarketPrices(String serviceName) {
        if (serviceName == null || serviceName.trim().isEmpty()) {
            throw new IllegalArgumentException("serviceName ne peut pas être vide");
        }
        
        String normalizedName = serviceName.trim();
        
        try {
            // Essayer de récupérer depuis la base de données de fallback d'abord
            BenchmarkData fallback = MARKET_DATABASE.get(normalizedName);
            if (fallback != null) {
                logger.info("Prix de marché trouvés pour {} (depuis base locale)", normalizedName);
                return fallback;
            }
            
            // Si pas trouvé en local, essayer l'API externe (simulation)
            BenchmarkData fromApi = fetchFromExternalAPI(normalizedName);
            if (fromApi != null) {
                logger.info("Prix de marché récupérés pour {} (depuis API externe)", normalizedName);
                return fromApi;
            }
            
            // Fallback : Service inconnu, générer des données par défaut
            logger.warn("Service {} inconnu, utilisation de données par défaut", normalizedName);
            return new BenchmarkData(normalizedName, 14.99, 5.00, 29.99, "EUR", "Europe");
            
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des prix pour {} : {}", normalizedName, e.getMessage());
            // Retourner fallback plutôt que lever une exception
            return new BenchmarkData(normalizedName, 14.99, 5.00, 29.99, "EUR", "Europe");
        }
    }
    
    /**
     * Simule l'appel à une API externe de pricing.
     * Dans un cas réel, cela pourrait être remplacé par un vrai appel HTTP.
     */
    private BenchmarkData fetchFromExternalAPI(String serviceName) {
        try {
            // Simulation : générer une réponse JSON fictive
            // En production, ce serait un vrai appel HTTP à une API de pricing
            
            logger.debug("Tentative de récupération des données pour {} via API externe...", serviceName);
            
            // Simulation d'un appel avec délai minimal
            Thread.sleep(100);
            
            // Retourner null pour signifier que l'API ne connaît pas ce service
            // (fallback sera utilisé)
            return null;
            
        } catch (InterruptedException e) {
            logger.warn("Interruption lors de l'appel API pour {}", serviceName);
            Thread.currentThread().interrupt();
            return null;
        } catch (Exception e) {
            logger.error("Erreur lors de l'appel API externe : {}", e.getMessage());
            return null;
        }
    }
}
