package com.projet.user;

public class User {
    private String email;
    private String password;
    private boolean confirmed;

    public User(String email, String password) {
        this.email = email;
        this.password = password;
        this.confirmed = false;
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
}
