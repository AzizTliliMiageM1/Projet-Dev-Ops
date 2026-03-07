package com.projet.analytics.lifecycle;

/**
 * Énumération des décisions possibles pour un abonnement sur un mois.
 */
public enum LifecycleDecision {
    /** Garder l'abonnement actif pour ce mois */
    KEEP,
    
    /** Pausertemporairement l'abonnement (potentiellement réactiver plus tard) */
    PAUSE,
    
    /** Activer un abonnement suspendu */
    ACTIVATE
}
