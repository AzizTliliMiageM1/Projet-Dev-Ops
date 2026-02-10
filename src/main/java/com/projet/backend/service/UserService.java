package com.projet.backend.service;

import java.util.ArrayList;
import java.util.List;

import com.projet.backend.domain.User;

/**
 * Service d'orchestration pour les opérations métier sur les utilisateurs.
 * 
 * Cette couche service :
 * - Utilise l'entité User du domaine
 * - Exécute la logique métier sans I/O
 * - Retourne des résultats structurés
 * - Est complètement indépendante de l'UI et la persistence
 */
public class UserService {

    /**
     * Valide les informations d'un utilisateur selon les règles métier.
     */
    public static class ValidationResult {
        public final boolean valid;
        public final List<String> errors;

        public ValidationResult(boolean valid, List<String> errors) {
            this.valid = valid;
            this.errors = errors;
        }

        public static ValidationResult success() {
            return new ValidationResult(true, new ArrayList<>());
        }

        public static ValidationResult failure(List<String> errors) {
            return new ValidationResult(false, errors);
        }

        public static ValidationResult failure(String error) {
            List<String> errors = new ArrayList<>();
            errors.add(error);
            return new ValidationResult(false, errors);
        }
    }

    // ========== HELPERS PRIVÉS DE VALIDATION ==========

    /**
     * Vérifie si une chaîne est nulle ou vide.
     */
    private static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Vérifie si la longueur d'une chaîne est dans une plage valide.
     */
    private static boolean isValidLength(String str, int min, int max) {
        if (str == null) return false;
        int len = str.trim().length();
        return len >= min && len <= max;
    }

    // ========== VALIDATIONS MÉTIER ==========

    /**
     * Valide une adresse email selon les règles métier basiques.
     * 
     * @param email Email à valider
     * @return true si valide, false sinon
     */
    public boolean isValidEmail(String email) {
        if (isNullOrEmpty(email)) {
            return false;
        }

        // Validation basique email (RFC simplifiée)
        String emailPattern = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Z|a-z]{2,})$";
        return email.matches(emailPattern);
    }

    /**
     * Valide un mot de passe selon les règles métier.
     * 
     * Critères :
     * - Longueur minimale de 8 caractères
     * - Au moins une majuscule
     * - Au moins un chiffre
     * 
     * @param password Mot de passe à valider
     * @return true si valide, false sinon
     */
    public boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        boolean hasUpperCase = password.matches(".*[A-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");

        return hasUpperCase && hasDigit;
    }

    /**
     * Valide un pseudo utilisateur.
     * 
     * Critères :
     * - Non vide
     * - Longueur entre 3 et 30 caractères
     * - Caractères alphanumériques, tirets et underscores uniquement
     * 
     * @param pseudo Pseudo à valider
     * @return true si valide, false sinon
     */
    public boolean isValidPseudo(String pseudo) {
        if (!isValidLength(pseudo, 3, 30)) {
            return false;
        }

        // Autorise uniquement alphanumériques, tirets et underscores
        return pseudo.trim().matches("^[a-zA-Z0-9_-]+$");
    }

    /**
     * Valide un utilisateur selon les règles métier.
     * 
     * @param user Utilisateur à valider
     * @return Résultat de validation
     */
    public ValidationResult validateUser(User user) {
        List<String> errors = new ArrayList<>();

        // Validations email
        if (!isValidEmail(user.getEmail())) {
            errors.add(BackendMessages.USER_EMAIL_INVALID);
        }

        // Validations password
        if (!isValidPassword(user.getPassword())) {
            errors.add(BackendMessages.USER_PASSWORD_INVALID);
        }

        // Validations pseudo
        if (!isValidPseudo(user.getPseudo())) {
            errors.add(BackendMessages.USER_PSEUDO_INVALID);
        }

        return errors.isEmpty() ? ValidationResult.success() : ValidationResult.failure(errors);
    }

    /**
     * Crée un nouvel utilisateur avec validation complète.
     * 
     * @param email Email de l'utilisateur
     * @param password Mot de passe en clair (sera hashé par la persistence)
     * @param pseudo Pseudo de l'utilisateur
     * @return L'utilisateur créé si valide
     * @throws IllegalArgumentException si l'utilisateur n'est pas valide
     */
    public User createUser(String email, String password, String pseudo) {
        User user = new User(email, password, pseudo);
        user.setConfirmed(false);
        user.setConfirmationToken(null);

        ValidationResult validation = validateUser(user);
        if (!validation.valid) {
            throw new IllegalArgumentException(BackendMessages.formatValidationError("Création utilisateur échouée", validation.errors));
        }

        return user;
    }

    /**
     * Met à jour le pseudo d'un utilisateur.
     * 
     * @param user Utilisateur à modifier
     * @param newPseudo Nouveau pseudo
     * @return true si la mise à jour est valide, false sinon
     */
    public boolean updatePseudo(User user, String newPseudo) {
        if (!isValidPseudo(newPseudo)) {
            return false;
        }
        user.setPseudo(newPseudo);
        return true;
    }

    /**
     * Valide la confirmation de l'utilisateur.
     * 
     * @param user Utilisateur à confirmer
     * @param token Token de confirmation fourni par l'utilisateur
     * @return true si le token est valide et l'utilisateur confirmé
     */
    public boolean confirmUser(User user, String token) {
        // Vérifier que le token correspond et n'est pas vide
        if (token == null || token.trim().isEmpty() || 
            user.getConfirmationToken() == null || 
            !user.getConfirmationToken().equals(token)) {
            return false;
        }

        user.setConfirmed(true);
        user.setConfirmationToken(null);
        return true;
    }

    /**
     * Note: Le mot de passe utilisateur ne peut pas être modifié après création
     * pour des raisons de sécurité (principes DevOps - immutabilité des identifiants).
     * Pour un vrai reset, créer un nouvel utilisateur ou implémenter
     * un workflow d'oubli de mot de passe avec token.
     *
     * Cette méthode valide un mot de passe sans l'assigner.
     * 
     * @param password Mot de passe à valider
     * @return true si le mot de passe est valide
     */
    public boolean isPasswordValid(String password) {
        return isValidPassword(password);
    }

    /**
     * Vérifie si un email est au format d'une institution reconnue.
     * 
     * Utile pour identifier les utilisateurs institutionnels.
     * 
     * @param email Email à vérifier
     * @return true si c'est un domaine institutionnel
     */
    public boolean isInstitutionalEmail(String email) {
        if (email == null || !email.contains("@")) {
            return false;
        }

        String domain = email.substring(email.indexOf("@") + 1).toLowerCase();
        
        // Listes de domaines institutionnels
        String[] institutionalDomains = {
            "parisnanterre.fr",
            "univ-paris.fr",
            "sorbonne.fr",
            "centrale.fr",
            "polytechnique.fr",
            "inria.fr"
        };

        for (String instDomain : institutionalDomains) {
            if (domain.endsWith(instDomain)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Génère un token de confirmation d'email aléatoire.
     * 
     * @return Token de 32 caractères hexadécimaux
     */
    public String generateConfirmationToken() {
        StringBuilder token = new StringBuilder();
        double random;
        for (int i = 0; i < 32; i++) {
            random = Math.random();
            if (Math.random() < 0.5) {
                // Chiffres
                token.append((int) (random * 10));
            } else {
                // Lettres hexadécimales
                int charCode = (int) (random * 16);
                token.append(Integer.toHexString(charCode));
            }
        }
        return token.toString();
    }
}
