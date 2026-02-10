package com.projet.backend.domain;

public class User {
    private String email;
    private String password;
    private String pseudo;
    private boolean confirmed;
    private String confirmationToken;

    public User(String email, String password, String confirmationToken) {
        this.email = email;
        this.password = password;
        this.pseudo = email.split("@")[0]; // Par défaut, pseudo = partie avant @
        this.confirmed = false;
        this.confirmationToken = confirmationToken;
    }

    public User(String email, String password, String pseudo, String confirmationToken) {
        this.email = email;
        this.password = password;
        this.pseudo = pseudo;
        this.confirmed = false;
        this.confirmationToken = confirmationToken;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getPseudo() {
        return pseudo;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public String getConfirmationToken() {
        return confirmationToken;
    }

    public void setConfirmationToken(String confirmationToken) {
        this.confirmationToken = confirmationToken;
    }
}
