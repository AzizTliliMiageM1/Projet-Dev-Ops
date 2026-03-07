package com.projet.analytics.optimization;

import java.util.List;

import com.projet.backend.domain.Abonnement;

public interface SubscriptionOptimizationService {
    OptimizationResult analyze(List<Abonnement> abonnements);
}
