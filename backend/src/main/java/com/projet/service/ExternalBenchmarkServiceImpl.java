package com.projet.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    private static final String DEFAULT_BENCHMARK_SEARCH_API = "https://dummyjson.com/products/search?q=%s";
    
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
     * Appelle une API externe de pricing.
     *
     * Supporte deux formats de réponse :
     * - Format direct: { averagePrice, minPrice, maxPrice, currency, region }
     * - Format catalogue (ex: dummyjson): { products: [{ price }, ...] }
     */
    private BenchmarkData fetchFromExternalAPI(String serviceName) {
        try {
            String template = resolveBenchmarkApiTemplate();
            String encodedService = java.net.URLEncoder.encode(serviceName, java.nio.charset.StandardCharsets.UTF_8);
            String apiUrl = String.format(template, encodedService);

            logger.debug("Tentative de récupération benchmark via API externe: {}", apiUrl);

            HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                logger.warn("API benchmark externe status={} pour service={}", conn.getResponseCode(), serviceName);
                return null;
            }

            String response = readResponse(conn);
            JsonNode root = mapper.readTree(response);

            // Format direct
            if (root.has("averagePrice") && root.has("minPrice") && root.has("maxPrice")) {
                double average = root.path("averagePrice").asDouble(0.0);
                double min = root.path("minPrice").asDouble(0.0);
                double max = root.path("maxPrice").asDouble(0.0);
                String currency = root.path("currency").asText("EUR");
                String region = root.path("region").asText("Global");
                if (average > 0 && max > 0) {
                    return new BenchmarkData(serviceName, average, min, max, currency, region);
                }
            }

            // Format catalogue (dummyjson/products)
            JsonNode products = root.path("products");
            if (products.isArray() && products.size() > 0) {
                double sum = 0.0;
                double min = Double.MAX_VALUE;
                double max = Double.MIN_VALUE;
                int count = 0;

                for (JsonNode product : products) {
                    JsonNode priceNode = product.path("price");
                    if (!priceNode.isNumber()) {
                        continue;
                    }
                    double price = priceNode.asDouble();
                    if (price <= 0) {
                        continue;
                    }
                    sum += price;
                    min = Math.min(min, price);
                    max = Math.max(max, price);
                    count++;
                    if (count >= 10) {
                        break;
                    }
                }

                if (count > 0) {
                    double average = sum / count;
                    return new BenchmarkData(serviceName, average, min, max, "USD", "Global");
                }
            }

            return null;
        } catch (Exception e) {
            logger.error("Erreur lors de l'appel API externe : {}", e.getMessage());
            return null;
        }
    }

    private String resolveBenchmarkApiTemplate() {
        String fromEnv = System.getenv("BENCHMARK_SEARCH_API");
        if (fromEnv == null || fromEnv.isBlank()) {
            return DEFAULT_BENCHMARK_SEARCH_API;
        }

        if (!fromEnv.contains("%s")) {
            return fromEnv + "%s";
        }

        return fromEnv;
    }

    private String readResponse(HttpURLConnection conn) throws Exception {
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }
        return response.toString();
    }
}
