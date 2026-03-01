package com.projet.backend.service;

/**
 * Exception métier pour les erreurs du backend.
 * 
 * Utilisée pour les erreurs de validation métier, les contraintes
 * métier violées, et les opérations invalides sur les données.
 */
public class BackendException extends RuntimeException {

    public BackendException(String message) {
        super(message);
    }

    public BackendException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Factory method pour créer une exception à partir d'une liste d'erreurs.
     */
    public static BackendException fromValidationErrors(java.util.List<String> errors) {
        return new BackendException("Validation échouée: " + String.join("; ", errors));
    }
}
