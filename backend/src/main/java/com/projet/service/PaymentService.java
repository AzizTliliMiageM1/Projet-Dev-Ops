package com.projet.service;

import com.projet.backend.domain.PaymentResult;

public interface PaymentService {
    PaymentResult simulatePayment(double amount, String currency);
}
