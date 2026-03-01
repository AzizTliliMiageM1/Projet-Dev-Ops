package com.projet.backend.service;

/**
 * Messages d'erreur métier du backend.
 * 
 * Centralisent les textes d'erreur pour cohérence et maintenance.
 */
public class BackendMessages {

    // Validation Subscription
    public static final String SUBSCRIPTION_PRICE_NEGATIVE = "Le prix mensuel ne peut pas être négatif";
    public static final String SUBSCRIPTION_DATES_MISSING = "Les dates de début et de fin doivent être renseignées";
    public static final String SUBSCRIPTION_DATE_ORDER_INVALID = "La date de début doit être avant ou égale à la date de fin";
    public static final String SUBSCRIPTION_NAME_MISSING = "Le nom du service est obligatoire";
    public static final String SUBSCRIPTION_CLIENT_MISSING = "Le nom du client est obligatoire";
    public static final String SUBSCRIPTION_USERS_INVALID = "Le nombre d'utilisateurs doit être supérieur à 0";

    // Validation User
    public static final String USER_EMAIL_INVALID = "Email invalide ou vide";
    public static final String USER_PASSWORD_INVALID = "Mot de passe doit avoir au moins 8 caractères, une majuscule et un chiffre";
    public static final String USER_PSEUDO_INVALID = "Pseudo invalide (3-30 caractères, alphanumériques _ et - uniquement)";

    // Operation errors
    public static final String OPERATION_FAILED = "Opération échouée";
    public static final String VALIDATION_FAILED = "Validation échouée";

    /**
     * Formate un message d'erreur avec raisons détaillées.
     */
    public static String formatValidationError(String operation, java.util.List<String> errors) {
        return operation + ": " + String.join("; ", errors);
    }
}
