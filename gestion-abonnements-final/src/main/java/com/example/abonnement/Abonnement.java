package com.example.abonnement;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Currency;
import java.util.Locale;
import java.util.Objects;

public class Abonnement {
    private static final String CSV_SEP = ";";
    private static final DateTimeFormatter DISPLAY_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter CSV_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    private String nomService;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private double prixMensuel;
    private String clientName;
    private LocalDate derniereUtilisation; // date de la dernière utilisation

    // --- Constructeurs ---
    public Abonnement(String nomService, LocalDate dateDebut, LocalDate dateFin,
                      double prixMensuel, String clientName) {
        this(nomService, dateDebut, dateFin, prixMensuel, clientName, LocalDate.now());
    }

    public Abonnement(String nomService, LocalDate dateDebut, LocalDate dateFin,
                      double prixMensuel, String clientName, LocalDate derniereUtilisation) {
        this.nomService = Objects.requireNonNull(nomService, "nomService ne doit pas être nul");
        this.dateDebut = Objects.requireNonNull(dateDebut, "dateDebut ne doit pas être nulle");
        this.dateFin = Objects.requireNonNull(dateFin, "dateFin ne doit pas être nulle");
        this.prixMensuel = prixMensuel;
        this.clientName = Objects.requireNonNull(clientName, "clientName ne doit pas être nul");
        this.derniereUtilisation = derniereUtilisation; // peut être nul
        validateState();
    }

    // --- Getters ---
    public String getNomService() { return nomService; }
    public LocalDate getDateDebut() { return dateDebut; }
    public LocalDate getDateFin() { return dateFin; }
    public double getPrixMensuel() { return prixMensuel; }
    public String getClientName() { return clientName; }
    public LocalDate getDerniereUtilisation() { return derniereUtilisation; }

    // --- Setters (avec validations légères) ---
    public void setNomService(String nomService) {
        this.nomService = Objects.requireNonNull(nomService, "nomService ne doit pas être nul");
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = Objects.requireNonNull(dateDebut, "dateDebut ne doit pas être nulle");
        ensureDatesCoherentes();
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = Objects.requireNonNull(dateFin, "dateFin ne doit pas être nulle");
        ensureDatesCoherentes();
    }

    public void setPrixMensuel(double prixMensuel) {
        if (prixMensuel < 0) {
            throw new IllegalArgumentException("prixMensuel ne peut pas être négatif");
        }
        this.prixMensuel = prixMensuel;
    }

    public void setClientName(String clientName) {
        this.clientName = Objects.requireNonNull(clientName, "clientName ne doit pas être nul");
    }

    public void setDerniereUtilisation(LocalDate derniereUtilisation) {
        this.derniereUtilisation = derniereUtilisation; // peut être nul
    }

    // --- Logique métier ---
    /** Abonnement actif si aujourd'hui ∈ [dateDebut, dateFin] (bornes incluses) */
    public boolean estActif() {
        LocalDate aujourdHui = LocalDate.now();
        return !aujourdHui.isBefore(dateDebut) && !aujourdHui.isAfter(dateFin);
    }

    /** Jours restants (0 si expiré) */
    public long joursRestants() {
        long d = ChronoUnit.DAYS.between(LocalDate.now(), dateFin);
        return Math.max(0, d);
    }

    /** Pratique pour mettre à jour la dernière utilisation à aujourd'hui */
    public void marquerUtilisationAujourdhui() {
        this.derniereUtilisation = LocalDate.now();
    }

    // --- Représentation ---
    @Override
    public String toString() {
        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.FRANCE);
        nf.setCurrency(Currency.getInstance("EUR"));

        String statut = estActif() ? "Actif" : "Expiré";
        String joursInfo = estActif() ? " (reste " + joursRestants() + " jours)" : "";
        String derniere = (derniereUtilisation != null) ? derniereUtilisation.format(DISPLAY_FMT) : "N/A";

        return "\n--- Détails de l'Abonnement ---\n" +
               "  Client: " + clientName + "\n" +
               "  Service: " + nomService + "\n" +
               "  Début: " + dateDebut.format(DISPLAY_FMT) + "\n" +
               "  Fin: " + dateFin.format(DISPLAY_FMT) + "\n" +
               "  Prix Mensuel: " + nf.format(prixMensuel) + "\n" +
               "  Dernière utilisation: " + derniere + "\n" +
               "  Statut: " + statut + joursInfo + "\n" +
               "-------------------------------";
    }

    // --- Sérialisation CSV ---
    public String toCsvString() {
        return String.join(CSV_SEP,
                safe(nomService),
                dateDebut.format(CSV_FMT),
                dateFin.format(CSV_FMT),
                String.format(Locale.ROOT, "%.2f", prixMensuel),
                safe(clientName),
                (derniereUtilisation != null ? derniereUtilisation.format(CSV_FMT) : "")
        );
    }

    public static Abonnement fromCsvString(String csvString) {
        if (csvString == null || csvString.isBlank()) {
            throw new IllegalArgumentException("Ligne CSV vide ou nulle");
        }

        // keep trailing empty field (derniereUtilisation) with limit=-1
        String[] parts = csvString.split(CSV_SEP, -1);
        if (parts.length != 6) {
            throw new IllegalArgumentException("Format CSV invalide (6 colonnes attendues) : " + csvString);
        }

        try {
            String nomService = parts[0].trim();
            LocalDate dateDebut = LocalDate.parse(parts[1].trim(), CSV_FMT);
            LocalDate dateFin = LocalDate.parse(parts[2].trim(), CSV_FMT);
            double prixMensuel = Double.parseDouble(parts[3].trim().replace(',', '.'));
            String clientName = parts[4].trim();
            LocalDate derniereUtilisation = parts[5].isBlank() ? null
                    : LocalDate.parse(parts[5].trim(), CSV_FMT);

            return new Abonnement(nomService, dateDebut, dateFin, prixMensuel, clientName, derniereUtilisation);
        } catch (Exception e) {
            throw new IllegalArgumentException("Impossible de parser l'abonnement : " + csvString, e);
        }
    }

    // --- Utilitaires internes ---
    private void validateState() {
        if (prixMensuel < 0) {
            throw new IllegalArgumentException("prixMensuel ne peut pas être négatif");
        }
        ensureDatesCoherentes();
    }

    private void ensureDatesCoherentes() {
        if (dateDebut != null && dateFin != null && dateFin.isBefore(dateDebut)) {
            throw new IllegalArgumentException("dateFin doit être >= dateDebut");
        }
    }

    private static String safe(String s) {
        return (s == null) ? "" : s;
    }

    // --- Bonus: égalité/hash pour collections & tests ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Abonnement that)) return false;
        return Double.compare(that.prixMensuel, prixMensuel) == 0
                && Objects.equals(nomService, that.nomService)
                && Objects.equals(dateDebut, that.dateDebut)
                && Objects.equals(dateFin, that.dateFin)
                && Objects.equals(clientName, that.clientName)
                && Objects.equals(derniereUtilisation, that.derniereUtilisation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nomService, dateDebut, dateFin, prixMensuel, clientName, derniereUtilisation);
    }
}
