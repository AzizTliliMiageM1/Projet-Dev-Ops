package com.projet.backend.domain;

import java.time.LocalDate;

/**
 * Représentation d'une transaction bancaire.
 * Utilisée par le système de détection Open Banking.
 */
public class Transaction {
    private String rawLabel;
    private String normalizedService;
    private String category;
    private double amount;
    private LocalDate date;

    public Transaction(String rawLabel, double amount, LocalDate date) {
        this.rawLabel = rawLabel != null ? rawLabel.trim() : "";
        this.amount = Math.abs(amount); // Toujours positif
        this.date = date;
        this.normalizedService = "";
        this.category = "Autre";
    }

    // Getters & Setters
    public String getRawLabel() { return rawLabel; }
    public void setRawLabel(String rawLabel) { this.rawLabel = rawLabel; }

    public String getNormalizedService() { return normalizedService; }
    public void setNormalizedService(String normalizedService) { this.normalizedService = normalizedService; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    @Override
    public String toString() {
        return "Transaction{" +
                "rawLabel='" + rawLabel + '\'' +
                ", normalizedService='" + normalizedService + '\'' +
                ", amount=" + amount +
                ", date=" + date +
                '}';
    }
}
