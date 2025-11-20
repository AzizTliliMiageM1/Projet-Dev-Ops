package com.projet.user;

public class User {
    private String email;
    private String password;
    private boolean confirmed;
    private String confirmationToken;

    public User(String email, String password, String confirmationToken) {
        this.email = email;
        this.password = password;
        this.confirmed = false;
        this.confirmationToken = confirmationToken;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
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
