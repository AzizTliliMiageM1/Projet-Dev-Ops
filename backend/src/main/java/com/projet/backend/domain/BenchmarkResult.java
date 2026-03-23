package com.projet.backend.domain;

/**
 * Résultat du benchmark d'abonnement.
 * 
 * Représente la comparaison entre le prix d'un abonnement utilisateur
 * et les prix du marché.
 * 
 * Utilisé pour la réponse JSON de l'endpoint /api/abonnements/{id}/benchmark
 */
public class BenchmarkResult {
    
    /** ID de l'abonnement analysé */
    private String abonnementId;
    
    /** Nom du service */
    private String serviceName;
    
    /** Prix de l'abonnement utilisateur (EUR) */
    private double userPrice;
    
    /** Prix moyen du marché (EUR) */
    private double marketAveragePrice;
    
    /** Prix minimum du marché (EUR) */
    private double marketMinPrice;
    
    /** Prix maximum du marché (EUR) */
    private double marketMaxPrice;
    
    /** Statut du benchmark : OVERPRICED, OPTIMIZED, UNDERPRICED */
    private String status;
    
    /** Pourcentage de différence par rapport à la moyenne du marché */
    private double priceDeviation;
    
    /** Recommandation générée */
    private String recommendation;
    
    /** Région du benchmark */
    private String region;
    
    /** Timestamp du benchmark */
    private long timestamp;

    // ========== CONSTRUCTEURS ==========
    
    public BenchmarkResult() {
    }

    public BenchmarkResult(String abonnementId, String serviceName, double userPrice,
                          double marketAveragePrice, double marketMinPrice, 
                          double marketMaxPrice) {
        this.abonnementId = abonnementId;
        this.serviceName = serviceName;
        this.userPrice = userPrice;
        this.marketAveragePrice = marketAveragePrice;
        this.marketMinPrice = marketMinPrice;
        this.marketMaxPrice = marketMaxPrice;
        this.timestamp = System.currentTimeMillis();
        
        // Calculer la déviation et le statut
        this.priceDeviation = calculateDeviation();
        this.status = determineStatus();
        this.recommendation = generateRecommendation();
    }

    // ========== CALCULS MÉTIER ==========
    
    /**
     * Calcule le pourcentage de déviation par rapport au prix moyen du marché.
     * 
     * Positif = utilisateur paie plus que la moyenne
     * Négatif = utilisateur paie moins que la moyenne
     */
    private double calculateDeviation() {
        if (marketAveragePrice == 0) return 0;
        return ((userPrice - marketAveragePrice) / marketAveragePrice) * 100;
    }

    /**
     * Détermine le statut du prix.
     * 
     * Logique :
     * - UNDERPRICED : Prix utilisateur < prix min marché - 10%
     * - OPTIMIZED : Prix utilisateur dans la plage de marché ± 10%
     * - OVERPRICED : Prix utilisateur > prix max marché + 10%
     */
    private String determineStatus() {
        double lowerBound = marketMinPrice * 0.9;
        double upperBound = marketMaxPrice * 1.1;
        
        if (userPrice < lowerBound) {
            return "UNDERPRICED";
        } else if (userPrice > upperBound) {
            return "OVERPRICED";
        } else {
            return "OPTIMIZED";
        }
    }

    /**
     * Génère une recommandation basée sur le statut et la déviation.
     */
    private String generateRecommendation() {
        double deviation = Math.abs(priceDeviation);
        
        switch (status) {
            case "OVERPRICED":
                if (deviation > 50) {
                    return "⚠️ Beaucoup trop cher ! Considérez de changer de prestataire. " +
                           "Le marché propose " + String.format("%.2f", marketAveragePrice) + "€ en moyenne.";
                } else if (deviation > 20) {
                    return "⚠️ Considérez de négocier ou chercher des alternatives. " +
                           "Économies potentielles : " + String.format("%.2f", userPrice - marketAveragePrice) + "€/mois";
                } else {
                    return "Légèrement au-dessus du marché. Vérifiez si l'offre justifie le surcoût.";
                }
                
            case "UNDERPRICED":
                return "✅ Excellent deal ! Vous bénéficiez d'un bon tarif. " +
                       "Continuez à surveiller les évolutions de prix.";
                
            case "OPTIMIZED":
            default:
                if (deviation < 5) {
                    return "✅ Parfaitement optimisé ! Votre prix suit exactement la tendance du marché.";
                } else if (priceDeviation > 0) {
                    return "Légèrement au-dessus du marché. Prix raisonnables pour la qualité du service.";
                } else {
                    return "Légèrement en dessous du marché. Bonne tarification.";
                }
        }
    }

    // ========== GETTERS/SETTERS ==========
    
    public String getAbonnementId() {
        return abonnementId;
    }

    public void setAbonnementId(String abonnementId) {
        this.abonnementId = abonnementId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public double getUserPrice() {
        return userPrice;
    }

    public void setUserPrice(double userPrice) {
        this.userPrice = userPrice;
    }

    public double getMarketAveragePrice() {
        return marketAveragePrice;
    }

    public void setMarketAveragePrice(double marketAveragePrice) {
        this.marketAveragePrice = marketAveragePrice;
    }

    public double getMarketMinPrice() {
        return marketMinPrice;
    }

    public void setMarketMinPrice(double marketMinPrice) {
        this.marketMinPrice = marketMinPrice;
    }

    public double getMarketMaxPrice() {
        return marketMaxPrice;
    }

    public void setMarketMaxPrice(double marketMaxPrice) {
        this.marketMaxPrice = marketMaxPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getPriceDeviation() {
        return priceDeviation;
    }

    public void setPriceDeviation(double priceDeviation) {
        this.priceDeviation = priceDeviation;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "BenchmarkResult{" +
                "abonnementId='" + abonnementId + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", userPrice=" + userPrice +
                ", marketAveragePrice=" + marketAveragePrice +
                ", status='" + status + '\'' +
                ", priceDeviation=" + String.format("%.2f", priceDeviation) + "%" +
                ", recommendation='" + recommendation + '\'' +
                '}';
    }
}
