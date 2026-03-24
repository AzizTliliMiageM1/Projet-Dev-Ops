package com.projet.service;

import java.util.concurrent.ThreadLocalRandom;

import com.projet.backend.domain.PaymentResult;

public class PaymentServiceImpl implements PaymentService {

    @Override
    public PaymentResult simulatePayment(double amount, String currency) {
        boolean success = ThreadLocalRandom.current().nextInt(100) < 90;
        String status = success ? "SUCCESS" : "FAILED";
        String message = success ? "Paiement simulé validé" : "Paiement simulé refusé";
        String safeCurrency = (currency == null || currency.isBlank()) ? "EUR" : currency.toUpperCase();
        return new PaymentResult(status, message, amount, safeCurrency);
    }
}
