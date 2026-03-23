package com.projet.service;

import com.projet.backend.domain.BenchmarkResult;

/**
 * Service de benchmark d'abonnements.
 * 
 * Logique métier pour comparer un abonnement utilisateur avec les prix du marché.
 * 
 * Responsabilités :
 * - Récupérer les données de marché
 * - Comparer les prix
 * - Générer les recommandations
 * 
 * Dépend de ExternalBenchmarkService pour l'accès aux données externes.
 */
public interface BenchmarkService {

    /**
     * Génère un rapport de benchmark pour un abonnement donné.
     * 
     * @param abonnementId ID de l'abonnement à analyser
     * @param serviceName Nom du service (ex: Netflix, Spotify)
     * @param userPrice Prix payé par l'utilisateur
     * @return BenchmarkResult contenant la comparaison et recommandations
     * @throws IllegalArgumentException Si les données sont invalides
     */
    BenchmarkResult benchmark(String abonnementId, String serviceName, double userPrice);

    /**
     * Calcule le ratio de prix (userPrice / marketAverage).
     * Utilisé pour d'autres analyses.
     * 
     * @param userPrice Prix payé par l'utilisateur
     * @param serviceName Nom du service
     * @return Ratio (1.0 = exact, > 1.0 = overpriced, < 1.0 = underpriced)
     */
    double getPriceRatio(double userPrice, String serviceName);
}
