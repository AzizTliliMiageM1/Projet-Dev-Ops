package com.projet.backend.domain;

public class PaymentResult {
    private String status;
    private String message;
    private double amount;
    private String currency;

    public PaymentResult() {
    }

    public PaymentResult(String status, String message, double amount, String currency) {
        this.status = status;
        this.message = message;
        this.amount = amount;
        this.currency = currency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
