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
    private LocalDate derniereUtilisation;
    private String categorie; // Nouvelle fonctionnalité: catégorie de l'abonnement

    // Constructeur original mis à jour
    public Abonnement(String nomService, LocalDate dateDebut, LocalDate dateFin, double prixMensuel, String clientName) {
        this(nomService, dateDebut, dateFin, prixMensuel, clientName, LocalDate.now(), "Non classé"); // Par défaut, dernière utilisation est aujourd'hui, catégorie par défaut
    }

    // Constructeur sans-argument nécessaire pour la désérialisation Jackson
    public Abonnement() {
        // valeurs par défaut simples
        this.nomService = "";
        this.dateDebut = LocalDate.now();
        this.dateFin = LocalDate.now();
        this.prixMensuel = 0.0;
        this.clientName = "";
        this.derniereUtilisation = null;
        this.categorie = "Non classé";
    }

    // Constructeur avec derniereUtilisation (compatibilité avec l'ancien code qui passait 6 arguments)
    public Abonnement(String nomService, LocalDate dateDebut, LocalDate dateFin, double prixMensuel, String clientName, LocalDate derniereUtilisation) {
        this(nomService, dateDebut, dateFin, prixMensuel, clientName, derniereUtilisation, "Non classé");
    }

    // Nouveau constructeur avec derniereUtilisation et categorie
    public Abonnement(String nomService, LocalDate dateDebut, LocalDate dateFin, double prixMensuel, String clientName, LocalDate derniereUtilisation, String categorie) {
        this.nomService = nomService;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.prixMensuel = prixMensuel;
        this.clientName = clientName;
        this.derniereUtilisation = derniereUtilisation;
        this.categorie = categorie;
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

    public String getCategorie() {
        return categorie;
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

    public void setCategorie(String categorie) {
        this.categorie = categorie;
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
               "  Catégorie: " + categorie + "\n" + // Affichage de la catégorie
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
        return String.format("%s;%s;%s;%.2f;%s;%s;%s",
                nomService,
                dateDebut.format(formatter),
                dateFin.format(formatter),
                prixMensuel,
                clientName,
                derniereUtilisation != null ? derniereUtilisation.format(formatter) : "",
                categorie); // Sauvegarde de la catégorie
    }

    // Méthode statique pour créer un Abonnement à partir d'une chaîne de caractères (pour le chargement)
    public static Abonnement fromCsvString(String csvString) {
        String[] parts = csvString.split(";");
        // Support both old format (6 parts: no category) and new format (7 parts: with category)
        if (parts.length != 6 && parts.length != 7) {
            throw new IllegalArgumentException("Format CSV invalide pour l'abonnement: " + csvString);
        }
        String nomService = parts[0];
        LocalDate dateDebut = LocalDate.parse(parts[1]);
        LocalDate dateFin = LocalDate.parse(parts[2]);
        double prixMensuel = Double.parseDouble(parts[3]);
        String clientName = parts[4];
        LocalDate derniereUtilisation = null;
        String categorie = "Non classé";
        if (parts.length == 6) {
            // old format: parts[5] == derniereUtilisation
            derniereUtilisation = parts[5].isEmpty() ? null : LocalDate.parse(parts[5]);
        } else if (parts.length == 7) {
            derniereUtilisation = parts[5].isEmpty() ? null : LocalDate.parse(parts[5]);
            categorie = parts[6];
        }
        return new Abonnement(nomService, dateDebut, dateFin, prixMensuel, clientName, derniereUtilisation, categorie);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Abonnement that = (Abonnement) o;

        if (Double.compare(that.prixMensuel, prixMensuel) != 0) return false;
        if (!nomService.equals(that.nomService)) return false;
        if (!dateDebut.equals(that.dateDebut)) return false;
        if (!dateFin.equals(that.dateFin)) return false;
        if (!clientName.equals(that.clientName)) return false;
        if (derniereUtilisation != null ? !derniereUtilisation.equals(that.derniereUtilisation) : that.derniereUtilisation != null)
            return false;
        return categorie != null ? categorie.equals(that.categorie) : that.categorie == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = nomService.hashCode();
        result = 31 * result + dateDebut.hashCode();
        result = 31 * result + dateFin.hashCode();
        temp = Double.doubleToLongBits(prixMensuel);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + clientName.hashCode();
        result = 31 * result + (derniereUtilisation != null ? derniereUtilisation.hashCode() : 0);
        result = 31 * result + (categorie != null ? categorie.hashCode() : 0);
        return result;
    }
}

