package com.projet.backend.domain;

/**
 * Entité métier représentant un utilisateur du système.
 * 
 * Encapsule les données d'authentification et d'identification :
 * - Email unique et adresse de contact
 * - Mot de passe en clair (à hasher par la couche persistence)
 * - Pseudo identifiant du profil public
 * - Statut de confirmation d'email
 * - Token pour confirmer l'email
 * 
 * Cette entité est immuable après création pour des raisons de sécurité.
 * Les modifications de pseudo sont possibles via le service.
 */
public class User {
    /** Adresse email unique de l'utilisateur */
    private String email;
    
    /** Mot de passe en clair (à hasher par la persistence) */
    private String password;
    
    /** Pseudo public de l'utilisateur (3-30 caractères) */
    private String pseudo;
    
    /** Indique si l'utilisateur a confirmé son adresse email */
    private boolean confirmed;
    
    /** Token de confirmation envoyé à l'email pour vérifier la possession */
    private String confirmationToken;

    // ========== CONSTRUCTEURS ==========

    /**
     * Constructeur avec paramètres minimaux.
     * Le pseudo est automatiquement défini à la partie avant @ de l'email.
     * 
     * @param email Adresse email unique
     * @param password Mot de passe en clair
     * @param confirmationToken Token unique pour confirmation d'email
     */
    public User(String email, String password, String confirmationToken) {
        this.email = email;
        this.password = password;
        this.pseudo = email.split("@")[0]; // Par défaut, pseudo = partie avant @
        this.confirmed = false;
        this.confirmationToken = confirmationToken;
    }

    /**
     * Constructeur avec pseudo personnalisé.
     * 
     * @param email Adresse email unique
     * @param password Mot de passe en clair
     * @param pseudo Pseudo public de l'utilisateur
     * @param confirmationToken Token unique pour confirmation d'email
     */
    public User(String email, String password, String pseudo, String confirmationToken) {
        this.email = email;
        this.password = password;
        this.pseudo = pseudo;
        this.confirmed = false;
        this.confirmationToken = confirmationToken;
    }

    // ========== ACCESSEURS ==========

    /**
     * Retourne l'adresse email de l'utilisateur (immutable).
     * 
     * @return Email unique de l'utilisateur
     */
    public String getEmail() {
        return email;
    }

    /**
     * Retourne le mot de passe (immutable après création).
     * 
     * @return Mot de passe en clair
     */
    public String getPassword() {
        return password;
    }

    /**
     * Retourne le pseudo de l'utilisateur (identifiant public).
     * 
     * @return Pseudo (3-30 caractères)
     */
    public String getPseudo() {
        return pseudo;
    }

    /**
     * Modifie le pseudo de l'utilisateur.
     * La validation est effectuée par UserService.
     * 
     * @param pseudo Nouveau pseudo
     */
    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    /**
     * Vérifie si l'utilisateur a confirmé son adresse email.
     * 
     * @return true si l'email est confirmé, false sinon
     */
    public boolean isConfirmed() {
        return confirmed;
    }

    /**
     * Définit le statut de confirmation de l'email.
     * 
     * @param confirmed true si l'email est confirmé
     */
    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    /**
     * Retourne le token de confirmation d'email.
     * 
     * @return Token unique pour vérifier la possession de l'email
     */
    public String getConfirmationToken() {
        return confirmationToken;
    }

    /**
     * Définit le token de confirmation d'email.
     * 
     * @param confirmationToken Token unique à envoyer à l'email
     */
    public void setConfirmationToken(String confirmationToken) {
        this.confirmationToken = confirmationToken;
    }
}
