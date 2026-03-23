package com.projet.service;

/**
 * Service d'intégration avec une API externe de benchmark de prix.
 * 
 * Cette interface permet de récupérer les prix moyens du marché pour différents
 * types de services d'abonnement, afin de comparer avec les prix utilisateurs.
 * 
 * Responsabilités :
 * - Récupérer les données de marché depuis une API externe
 * - Gérer les erreurs et timeouts
 * - Fournir des fallback cohérents en cas d'indisponibilité
 */
public interface ExternalBenchmarkService {

    /**
     * Récupère les données de benchmark pour un service donné.
     * 
     * @param serviceName Le nom du service (ex: Netflix, Spotify)
     * @return BenchmarkData contenant le prix moyen, min, max du marché
     * @throws RuntimeException Si l'API externe échoue définitivement
     */
    BenchmarkData getMarketPrices(String serviceName);

    /**
     * Classe interne pour encapsuler les données de marché.
     */
    class BenchmarkData {
        public final String serviceName;
        public final double averagePrice;
        public final double minPrice;
        public final double maxPrice;
        public final String currency;
        public final String region;
        public final long timestamp;

        public BenchmarkData(String serviceName, double averagePrice, double minPrice, 
                           double maxPrice, String currency, String region) {
            this.serviceName = serviceName;
            this.averagePrice = averagePrice;
            this.minPrice = minPrice;
            this.maxPrice = maxPrice;
            this.currency = currency;
            this.region = region;
            this.timestamp = System.currentTimeMillis();
        }

        @Override
        public String toString() {
            return "BenchmarkData{" +
                    "serviceName='" + serviceName + '\'' +
                    ", averagePrice=" + averagePrice +
                    ", minPrice=" + minPrice +
                    ", maxPrice=" + maxPrice +
                    ", currency='" + currency + '\'' +
                    ", region='" + region + '\'' +
                    ", timestamp=" + timestamp +
                    '}';
        }
    }
}
