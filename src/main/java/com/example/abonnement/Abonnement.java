package com.example.abonnement;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class Abonnement {
    private String id; // UUID string persistant
    private String nomService;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private double prixMensuel;
    private String clientName;
    private LocalDate derniereUtilisation;
    private String categorie; // Nouvelle fonctionnalité: catégorie de l'abonnement

    // Constructeur original mis à jour
    public Abonnement(String nomService, LocalDate dateDebut, LocalDate dateFin, double prixMensuel, String clientName) {
        this(UUID.randomUUID().toString(), nomService, dateDebut, dateFin, prixMensuel, clientName, LocalDate.now(), "Non classé"); // génère un id
    }

    // Constructeur sans-argument nécessaire pour la désérialisation Jackson
    public Abonnement() {
        // valeurs par défaut simples
        this.id = UUID.randomUUID().toString();
        this.nomService = "";
        this.dateDebut = LocalDate.now();
        this.dateFin = LocalDate.now();
        this.prixMensuel = 0.0;
        this.clientName = "";
        this.derniereUtilisation = null;
        this.categorie = "Non classé";
    }

    // Constructeur avec derniereUtilisation (compatibilité)
    public Abonnement(String id, String nomService, LocalDate dateDebut, LocalDate dateFin, double prixMensuel, String clientName, LocalDate derniereUtilisation) {
        this(id, nomService, dateDebut, dateFin, prixMensuel, clientName, derniereUtilisation, "Non classé");
    }

    // Nouveau constructeur avec id, derniereUtilisation et categorie
    public Abonnement(String id, String nomService, LocalDate dateDebut, LocalDate dateFin, double prixMensuel, String clientName, LocalDate derniereUtilisation, String categorie) {
        this.id = (id == null || id.isBlank()) ? UUID.randomUUID().toString() : id;
        this.nomService = nomService;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.prixMensuel = prixMensuel;
        this.clientName = clientName;
        this.derniereUtilisation = derniereUtilisation;
        this.categorie = categorie;
    }

    // Compatibilité : constructeur avec derniereUtilisation + categorie (sans id)
    public Abonnement(String nomService, LocalDate dateDebut, LocalDate dateFin, double prixMensuel, String clientName, LocalDate derniereUtilisation, String categorie) {
        this(UUID.randomUUID().toString(), nomService, dateDebut, dateFin, prixMensuel, clientName, derniereUtilisation, categorie);
    }

    // Compatibilité : constructeur avec derniereUtilisation (sans id, sans categorie)
    public Abonnement(String nomService, LocalDate dateDebut, LocalDate dateFin, double prixMensuel, String clientName, LocalDate derniereUtilisation) {
        this(UUID.randomUUID().toString(), nomService, dateDebut, dateFin, prixMensuel, clientName, derniereUtilisation, "Non classé");
    }

    // getter/setter for id
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

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
    // CSV format: id;nomService;dateDebut;dateFin;prixMensuel;clientName;derniereUtilisation;categorie
    return String.format("%s;%s;%s;%s;%.2f;%s;%s;%s",
        id,
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
        // New CSV format with id: 8 parts (id + existing 7 fields)
        // Support also older formats (6 or 7 parts) for backward compatibility
        String id = null;
        String nomService;
        LocalDate dateDebut;
        LocalDate dateFin;
        double prixMensuel;
        String clientName;
        LocalDate derniereUtilisation = null;
        String categorie = "Non classé";

        if (parts.length == 8) {
            id = parts[0];
            nomService = parts[1];
            dateDebut = LocalDate.parse(parts[2]);
            dateFin = LocalDate.parse(parts[3]);
            prixMensuel = Double.parseDouble(parts[4]);
            clientName = parts[5];
            derniereUtilisation = parts[6].isEmpty() ? null : LocalDate.parse(parts[6]);
            categorie = parts[7];
        } else if (parts.length == 7) {
            // old new-format without id: nomService;dateDebut;dateFin;prix;client;derniere;categorie
            nomService = parts[0];
            dateDebut = LocalDate.parse(parts[1]);
            dateFin = LocalDate.parse(parts[2]);
            prixMensuel = Double.parseDouble(parts[3]);
            clientName = parts[4];
            derniereUtilisation = parts[5].isEmpty() ? null : LocalDate.parse(parts[5]);
            categorie = parts[6];
            id = UUID.randomUUID().toString();
        } else if (parts.length == 6) {
            // legacy format: nomService;dateDebut;dateFin;prix;client;derniereUtilisation
            nomService = parts[0];
            dateDebut = LocalDate.parse(parts[1]);
            dateFin = LocalDate.parse(parts[2]);
            prixMensuel = Double.parseDouble(parts[3]);
            clientName = parts[4];
            derniereUtilisation = parts[5].isEmpty() ? null : LocalDate.parse(parts[5]);
            id = UUID.randomUUID().toString();
        } else {
            throw new IllegalArgumentException("Format CSV invalide pour l'abonnement: " + csvString);
        }

        return new Abonnement(id, nomService, dateDebut, dateFin, prixMensuel, clientName, derniereUtilisation, categorie);
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

