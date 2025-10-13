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
    private LocalDate derniereUtilisation; // Nouvelle fonctionnalité: date de la dernière utilisation

    // Constructeur original
    public Abonnement(String nomService, LocalDate dateDebut, LocalDate dateFin, double prixMensuel, String clientName) {
        this(nomService, dateDebut, dateFin, prixMensuel, clientName, LocalDate.now()); // Par défaut, dernière utilisation est aujourd'hui
    }

    // Nouveau constructeur avec derniereUtilisation
    public Abonnement(String nomService, LocalDate dateDebut, LocalDate dateFin, double prixMensuel, String clientName, LocalDate derniereUtilisation) {
        this.nomService = nomService;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.prixMensuel = prixMensuel;
        this.clientName = clientName;
        this.derniereUtilisation = derniereUtilisation;
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

    public LocalDate getDerniereUtilisation() {
        return derniereUtilisation;
    }

    // Setters
    public void setNomService(String nomService) {
        this.nomService = nomService;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public void setPrixMensuel(double prixMensuel) {
        this.prixMensuel = prixMensuel;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public void setDerniereUtilisation(LocalDate derniereUtilisation) {
        this.derniereUtilisation = derniereUtilisation;
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
               "  Dernière utilisation: " + (derniereUtilisation != null ? derniereUtilisation.format(formatter) : "N/A") + "\n" +
               "  Statut: " + statut + joursInfo + "\n" +
               "-------------------------------";
    }

    // Méthode pour convertir l'abonnement en une chaîne de caractères pour la sauvegarde
    public String toCsvString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return String.format("%s;%s;%s;%.2f;%s;%s",
                nomService,
                dateDebut.format(formatter),
                dateFin.format(formatter),
                prixMensuel,
                clientName,
                derniereUtilisation != null ? derniereUtilisation.format(formatter) : ""); // Sauvegarde de la dernière utilisation
    }

    // Méthode statique pour créer un Abonnement à partir d'une chaîne de caractères (pour le chargement)
    public static Abonnement fromCsvString(String csvString) {
        String[] parts = csvString.split(";");
        if (parts.length != 6) { // Le nombre de parties a changé
            throw new IllegalArgumentException("Format CSV invalide pour l'abonnement: " + csvString);
        }
        String nomService = parts[0];
        LocalDate dateDebut = LocalDate.parse(parts[1]);
        LocalDate dateFin = LocalDate.parse(parts[2]);
        double prixMensuel = Double.parseDouble(parts[3]);
        String clientName = parts[4];
        LocalDate derniereUtilisation = parts[5].isEmpty() ? null : LocalDate.parse(parts[5]); // Chargement de la dernière utilisation
        return new Abonnement(nomService, dateDebut, dateFin, prixMensuel, clientName, derniereUtilisation);
    }
}

