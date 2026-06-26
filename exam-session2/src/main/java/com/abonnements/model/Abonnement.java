package com.abonnements.model;

import java.util.UUID;

public class Abonnement {

    private String id;
    private String nom;
    private double prixMensuel;
    private String categorie;
    private boolean actif;

    // constructeur vide requis par Jackson
    public Abonnement() {
        this.id = UUID.randomUUID().toString();
        this.actif = true;
    }

    public Abonnement(String nom, double prixMensuel, String categorie) {
        this();
        this.nom = nom;
        this.prixMensuel = prixMensuel;
        this.categorie = categorie;
    }


    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public double getPrixMensuel() { return prixMensuel; }
    public void setPrixMensuel(double prixMensuel) { this.prixMensuel = prixMensuel; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }

    @Override
    public String toString() {
        return nom + " - " + prixMensuel + "€/mois (" + categorie + ")";
    }
}
