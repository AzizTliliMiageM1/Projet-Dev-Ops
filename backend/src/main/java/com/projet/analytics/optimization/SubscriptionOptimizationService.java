package com.projet.analytics.optimization;

import com.projet.backend.domain.Abonnement;
import java.util.List;

public interface SubscriptionOptimizationService {
    OptimizationResult analyze(List<Abonnement> abonnements);
}
