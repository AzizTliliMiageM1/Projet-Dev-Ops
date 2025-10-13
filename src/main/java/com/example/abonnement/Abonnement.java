package com.example.abonnement;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class Abonnement {
    private String nomService;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private double prixMensuel;
    private String clientName;

    // Constructeur
    public Abonnement(String nomService, LocalDate dateDebut, LocalDate dateFin, double prixMensuel, String clientName) {
        this.nomService = nomService;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.prixMensuel = prixMensuel;
        this.clientName = clientName;
    }

    // Getters
    public String getNomService() {
        return nomService;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public double getPrixMensuel() {
        return prixMensuel;
    }

    public String getClientName() {
        return clientName;
    }

    // Méthode pour vérifier si l'abonnement est actif
    public boolean estActif() {
        LocalDate aujourdHui = LocalDate.now();
        return !aujourdHui.isBefore(dateDebut) && !aujourdHui.isAfter(dateFin);
    }

    // Méthode pour afficher les détails de l'abonnement
    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String statut = estActif() ? "Actif" : "Expiré";
        long joursRestants = ChronoUnit.DAYS.between(LocalDate.now(), dateFin);
        String joursInfo = estActif() ? " (reste " + joursRestants + " jours)" : "";

        return "\n--- Détails de l'Abonnement ---\n" +
               "  Client: " + clientName + "\n" +
               "  Service: " + nomService + "\n" +
               "  Début: " + dateDebut.format(formatter) + "\n" +
               "  Fin: " + dateFin.format(formatter) + "\n" +
               "  Prix Mensuel: " + String.format("%.2f€", prixMensuel) + "\n" +
               "  Statut: " + statut + joursInfo + "\n" +
               "-------------------------------";
    }
}

