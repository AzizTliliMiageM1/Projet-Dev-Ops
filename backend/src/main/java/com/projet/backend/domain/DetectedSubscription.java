package com.projet.backend.domain;

import java.time.LocalDate;

/**
 * Représentation d'un abonnement détecté via Open Banking.
 */
public class DetectedSubscription {
    private String service;
    private String category;
    private double amount;
    private String frequency;
    private double confidence;
    private double optimizationScore;
    private String recommendation;
    private int occurrences;
    private LocalDate firstDetected;
    private LocalDate lastDetected;

    public DetectedSubscription(String service, String category, double amount, String frequency) {
        this.service = service;
        this.category = category;
        this.amount = amount;
        this.frequency = frequency;
        this.confidence = 0.5;
        this.optimizationScore = 0.0;
        this.recommendation = "";
        this.occurrences = 0;
        this.firstDetected = LocalDate.now();
        this.lastDetected = LocalDate.now();
    }

    // Getters & Setters
    public String getService() { return service; }
    public void setService(String service) { this.service = service; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = Math.min(1.0, Math.max(0.0, confidence)); }

    public double getOptimizationScore() { return optimizationScore; }
    public void setOptimizationScore(double optimizationScore) { this.optimizationScore = optimizationScore; }

    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }

    public int getOccurrences() { return occurrences; }
    public void setOccurrences(int occurrences) { this.occurrences = occurrences; }

    public LocalDate getFirstDetected() { return firstDetected; }
    public void setFirstDetected(LocalDate firstDetected) { this.firstDetected = firstDetected; }

    public LocalDate getLastDetected() { return lastDetected; }
    public void setLastDetected(LocalDate lastDetected) { this.lastDetected = lastDetected; }

    @Override
    public String toString() {
        return "DetectedSubscription{" +
                "service='" + service + '\'' +
                ", category='" + category + '\'' +
                ", amount=" + amount +
                ", frequency='" + frequency + '\'' +
                ", confidence=" + confidence +
                ", occurrences=" + occurrences +
                '}';
    }
}
