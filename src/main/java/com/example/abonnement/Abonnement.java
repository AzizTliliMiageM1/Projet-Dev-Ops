package com.example.abonnement;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
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
    
    // ========== NOUVELLES FONCTIONNALITÉS ==========
    private List<String> tags; // Étiquettes personnalisées (ex: "Famille", "Travail", "Essentiel")
    private String groupeAbonnement; // ID du groupe auquel appartient cet abonnement (ex: "Pack Streaming")
    private String priorite; // "Essentiel", "Important", "Optionnel", "Luxe"
    private String notes; // Notes personnelles sur l'abonnement
    private int nombreUtilisateurs; // Combien de personnes utilisent cet abonnement
    private boolean partage; // Est-ce que l'abonnement est partagé avec d'autres
    private int joursRappelAvantFin; // Nombre de jours avant la fin pour rappel (0 = pas de rappel)
    private String frequencePaiement; // "Mensuel", "Trimestriel", "Annuel"
    private LocalDate prochaineEcheance; // Prochaine date de paiement
    private double coutTotal; // Coût total depuis le début

    // Constructeur original mis à jour
    public Abonnement(String nomService, LocalDate dateDebut, LocalDate dateFin, double prixMensuel, String clientName) {
        this(UUID.randomUUID().toString(), nomService, dateDebut, dateFin, prixMensuel, clientName, LocalDate.now(), "Non classé",
             new ArrayList<>(), null, "Important", "", 1, false, 7, "Mensuel");
    }

    // Constructeur sans-argument nécessaire pour Jackson
    public Abonnement() {
        this.id = UUID.randomUUID().toString();
        this.nomService = "";
        this.dateDebut = LocalDate.now();
        this.dateFin = LocalDate.now();
        this.prixMensuel = 0.0;
        this.clientName = "";
        this.derniereUtilisation = null;
        this.categorie = "Non classé";
        this.tags = new ArrayList<>();
        this.groupeAbonnement = null;
        this.priorite = "Important";
        this.notes = "";
        this.nombreUtilisateurs = 1;
        this.partage = false;
        this.joursRappelAvantFin = 7;
        this.frequencePaiement = "Mensuel";
        this.prochaineEcheance = LocalDate.now().plusMonths(1);
        this.coutTotal = 0.0;
    }

    // Compatibilité
    public Abonnement(String id, String nomService, LocalDate dateDebut, LocalDate dateFin, double prixMensuel, String clientName, LocalDate derniereUtilisation) {
        this(id, nomService, dateDebut, dateFin, prixMensuel, clientName, derniereUtilisation, "Non classé",
             new ArrayList<>(), null, "Important", "", 1, false, 7, "Mensuel");
    }

    // Nouveau constructeur complet
    public Abonnement(String id, String nomService, LocalDate dateDebut, LocalDate dateFin, double prixMensuel, 
                     String clientName, LocalDate derniereUtilisation, String categorie,
                     List<String> tags, String groupeAbonnement, String priorite, String notes,
                     int nombreUtilisateurs, boolean partage, int joursRappelAvantFin, String frequencePaiement) {
        this.id = (id == null || id.isBlank()) ? UUID.randomUUID().toString() : id;
        this.nomService = nomService;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.prixMensuel = prixMensuel;
        this.clientName = clientName;
        this.derniereUtilisation = derniereUtilisation;
        this.categorie = (categorie == null || categorie.isBlank()) ? "Non classé" : categorie;
        this.tags = (tags == null) ? new ArrayList<>() : new ArrayList<>(tags);
        this.groupeAbonnement = groupeAbonnement;
        this.priorite = (priorite == null || priorite.isBlank()) ? "Important" : priorite;
        this.notes = (notes == null) ? "" : notes;
        this.nombreUtilisateurs = (nombreUtilisateurs <= 0) ? 1 : nombreUtilisateurs;
        this.partage = partage;
        this.joursRappelAvantFin = joursRappelAvantFin;
        this.frequencePaiement = (frequencePaiement == null || frequencePaiement.isBlank()) ? "Mensuel" : frequencePaiement;
        this.prochaineEcheance = calculerProchaineEcheance();
        this.coutTotal = calculerCoutTotal();
    }

    // Ancien constructeur pour compatibilité
    public Abonnement(String id, String nomService, LocalDate dateDebut, LocalDate dateFin, double prixMensuel, String clientName, LocalDate derniereUtilisation, String categorie) {
        this(id, nomService, dateDebut, dateFin, prixMensuel, clientName, derniereUtilisation, categorie,
             new ArrayList<>(), null, "Important", "", 1, false, 7, "Mensuel");
    }

    // Compatibilité
    public Abonnement(String nomService, LocalDate dateDebut, LocalDate dateFin, double prixMensuel, String clientName, LocalDate derniereUtilisation, String categorie) {
        this(UUID.randomUUID().toString(), nomService, dateDebut, dateFin, prixMensuel, clientName, derniereUtilisation, categorie);
    }

    public Abonnement(String nomService, LocalDate dateDebut, LocalDate dateFin, double prixMensuel, String clientName, LocalDate derniereUtilisation) {
        this(UUID.randomUUID().toString(), nomService, dateDebut, dateFin, prixMensuel, clientName, derniereUtilisation, "Non classé");
    }

    // Getters/Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNomService() { return nomService; }
    public LocalDate getDateDebut() { return dateDebut; }
    public LocalDate getDateFin() { return dateFin; }
    public double getPrixMensuel() { return prixMensuel; }
    public String getClientName() { return clientName; }
    public LocalDate getDerniereUtilisation() { return derniereUtilisation; }
    public String getCategorie() { return categorie; }

    public void setNomService(String nomService) { this.nomService = nomService; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }
    public void setPrixMensuel(double prixMensuel) { this.prixMensuel = prixMensuel; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    public void setDerniereUtilisation(LocalDate derniereUtilisation) { this.derniereUtilisation = derniereUtilisation; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    // Nouveaux getters/setters
    public List<String> getTags() { return new ArrayList<>(tags); }
    public void setTags(List<String> tags) { this.tags = (tags == null) ? new ArrayList<>() : new ArrayList<>(tags); }
    public void ajouterTag(String tag) { 
        if (tag != null && !tag.isBlank() && !this.tags.contains(tag)) {
            this.tags.add(tag); 
        }
    }
    public void supprimerTag(String tag) { this.tags.remove(tag); }
    
    public String getGroupeAbonnement() { return groupeAbonnement; }
    public void setGroupeAbonnement(String groupeAbonnement) { this.groupeAbonnement = groupeAbonnement; }
    
    public String getPriorite() { return priorite; }
    public void setPriorite(String priorite) { this.priorite = priorite; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public int getNombreUtilisateurs() { return nombreUtilisateurs; }
    public void setNombreUtilisateurs(int nombreUtilisateurs) { 
        this.nombreUtilisateurs = (nombreUtilisateurs <= 0) ? 1 : nombreUtilisateurs; 
    }
    
    public boolean isPartage() { return partage; }
    public void setPartage(boolean partage) { this.partage = partage; }
    
    public int getJoursRappelAvantFin() { return joursRappelAvantFin; }
    public void setJoursRappelAvantFin(int joursRappelAvantFin) { this.joursRappelAvantFin = joursRappelAvantFin; }
    
    public String getFrequencePaiement() { return frequencePaiement; }
    public void setFrequencePaiement(String frequencePaiement) { this.frequencePaiement = frequencePaiement; }
    
    public LocalDate getProchaineEcheance() { return prochaineEcheance; }
    public double getCoutTotal() { return coutTotal; }
    
    // Calcul du coût par personne (si partagé)
    public double getCoutParPersonne() {
        return nombreUtilisateurs > 0 ? prixMensuel / nombreUtilisateurs : prixMensuel;
    }

    // Métier
    public boolean estActif() {
        LocalDate aujourdHui = LocalDate.now();
        return !aujourdHui.isBefore(dateDebut) && !aujourdHui.isAfter(dateFin);
    }
    
    // ========== NOUVELLES MÉTHODES MÉTIER ==========
    
    /**
     * Calcule la prochaine échéance de paiement selon la fréquence
     */
    private LocalDate calculerProchaineEcheance() {
        if (dateDebut == null) return LocalDate.now();
        
        LocalDate aujourdhui = LocalDate.now();
        LocalDate echeance = dateDebut;
        
        while (echeance.isBefore(aujourdhui)) {
            switch (frequencePaiement != null ? frequencePaiement : "Mensuel") {
                case "Mensuel" -> echeance = echeance.plusMonths(1);
                case "Trimestriel" -> echeance = echeance.plusMonths(3);
                case "Semestriel" -> echeance = echeance.plusMonths(6);
                case "Annuel" -> echeance = echeance.plusYears(1);
                default -> echeance = echeance.plusMonths(1);
            }
        }
        
        return echeance;
    }
    
    /**
     * Calcule le coût total depuis le début de l'abonnement
     */
    private double calculerCoutTotal() {
        if (dateDebut == null) return 0.0;
        
        long moisEcoules = ChronoUnit.MONTHS.between(dateDebut, LocalDate.now());
        if (moisEcoules < 0) moisEcoules = 0;
        
        return moisEcoules * prixMensuel;
    }
    
    /**
     * Vérifie si un rappel doit être envoyé (proche de la fin)
     */
    public boolean doitEnvoyerRappel() {
        if (joursRappelAvantFin <= 0 || dateFin == null) return false;
        
        long joursRestants = ChronoUnit.DAYS.between(LocalDate.now(), dateFin);
        return joursRestants <= joursRappelAvantFin && joursRestants >= 0;
    }
    
    /**
     * Retourne le statut de priorité avec emoji
     */
    public String getPrioriteAvecEmoji() {
        return switch (priorite) {
            case "Essentiel" -> "🔴 Essentiel";
            case "Important" -> "🟠 Important";
            case "Optionnel" -> "🟡 Optionnel";
            case "Luxe" -> "🟢 Luxe";
            default -> "⚪ " + priorite;
        };
    }
    
    /**
     * Calcule le coût annuel estimé
     */
    public double getCoutAnnuelEstime() {
        return switch (frequencePaiement) {
            case "Mensuel" -> prixMensuel * 12;
            case "Trimestriel" -> prixMensuel * 4;
            case "Semestriel" -> prixMensuel * 2;
            case "Annuel" -> prixMensuel;
            default -> prixMensuel * 12;
        };
    }
    
    /**
     * Vérifie si l'abonnement fait partie d'un groupe
     */
    public boolean estGroupe() {
        return groupeAbonnement != null && !groupeAbonnement.isBlank();
    }
    
    /**
     * Retourne le nombre de jours avant expiration
     */
    public long getJoursAvantExpiration() {
        return ChronoUnit.DAYS.between(LocalDate.now(), dateFin);
    }
    
    /**
     * Calcule le retour sur investissement (utilisation vs coût)
     */
    public String getROI() {
        if (derniereUtilisation == null) return "Non évalué";
        
        long joursSanUtilisation = ChronoUnit.DAYS.between(derniereUtilisation, LocalDate.now());
        
        if (joursSanUtilisation < 7) return "Excellent 🌟";
        if (joursSanUtilisation < 30) return "Bon ✅";
        if (joursSanUtilisation < 60) return "Moyen ⚠️";
        return "Faible ⛔";
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String statut = estActif() ? "Actif" : "Expiré";
        long joursRestants = ChronoUnit.DAYS.between(LocalDate.now(), dateFin);
        String joursInfo = estActif() ? " (reste " + joursRestants + " jours)" : "";
        
        StringBuilder sb = new StringBuilder();
        sb.append("\n--- Détails de l'Abonnement ---\n");
        sb.append(" Client: ").append(clientName).append("\n");
        sb.append(" Service: ").append(nomService).append("\n");
        sb.append(" Catégorie: ").append(categorie).append("\n");
        
        // Nouvelles infos
        if (!tags.isEmpty()) {
            sb.append(" Tags: ").append(String.join(", ", tags)).append("\n");
        }
        if (groupeAbonnement != null && !groupeAbonnement.isBlank()) {
            sb.append(" Groupe: ").append(groupeAbonnement).append("\n");
        }
        sb.append(" Priorité: ").append(getPrioriteAvecEmoji()).append("\n");
        
        sb.append(" Début: ").append(dateDebut.format(formatter)).append("\n");
        sb.append(" Fin: ").append(dateFin.format(formatter)).append("\n");
        sb.append(" Prix: ").append(String.format("%.2f€/%s", prixMensuel, frequencePaiement)).append("\n");
        
        if (partage && nombreUtilisateurs > 1) {
            sb.append(" Partagé avec ").append(nombreUtilisateurs).append(" personnes (")
              .append(String.format("%.2f€/personne", getCoutParPersonne())).append(")\n");
        }
        
        sb.append(" Coût total: ").append(String.format("%.2f€", coutTotal)).append("\n");
        sb.append(" Prochaine échéance: ").append(prochaineEcheance.format(formatter)).append("\n");
        sb.append(" Dernière utilisation: ")
          .append(derniereUtilisation != null ? derniereUtilisation.format(formatter) : "N/A")
          .append(" - ROI: ").append(getROI()).append("\n");
        sb.append(" Statut: ").append(statut).append(joursInfo).append("\n");
        
        if (doitEnvoyerRappel()) {
            sb.append(" ⚠️ RAPPEL: Expiration dans ").append(joursRestants).append(" jours!\n");
        }
        
        if (notes != null && !notes.isBlank()) {
            sb.append(" Notes: ").append(notes).append("\n");
        }
        
        sb.append("-------------------------------");
        return sb.toString();
    }

    // CSV
    public String toCsvString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return String.format(
                "%s;%s;%s;%s;%.2f;%s;%s;%s;%s;%s;%s;%s;%d;%s;%d;%s",
                id,
                nomService,
                dateDebut.format(formatter),
                dateFin.format(formatter),
                prixMensuel,
                clientName,
                (derniereUtilisation != null ? derniereUtilisation.format(formatter) : ""),
                categorie,
                tags.isEmpty() ? "" : String.join("|", tags),
                groupeAbonnement != null ? groupeAbonnement : "",
                priorite,
                notes != null ? notes.replace(";", "｜") : "",
                nombreUtilisateurs,
                partage ? "OUI" : "NON",
                joursRappelAvantFin,
                frequencePaiement
        );
    }

    private static double parsePrice(String s) {
        return Double.parseDouble(s.replace(',', '.').trim());
    }

    public static Abonnement fromCsvString(String csvString) {
        String[] parts = csvString.split(";");
        String id = null;
        String nomService;
        LocalDate dateDebut;
        LocalDate dateFin;
        double prixMensuel;
        String clientName;
        LocalDate derniereUtilisation = null;
        String categorie = "Non classé";

        // Format complet (16 colonnes) avec nouvelles fonctionnalités
        if (parts.length == 16) {
            id = parts[0];
            nomService = parts[1];
            dateDebut = LocalDate.parse(parts[2]);
            dateFin = LocalDate.parse(parts[3]);
            prixMensuel = parsePrice(parts[4]);
            clientName = parts[5];
            derniereUtilisation = parts[6].isEmpty() ? null : LocalDate.parse(parts[6]);
            categorie = parts[7];
            
            List<String> tags = new ArrayList<>();
            if (!parts[8].isEmpty()) {
                String[] tagArray = parts[8].split("\\|");
                for (String tag : tagArray) {
                    tags.add(tag);
                }
            }
            String groupeAbonnement = parts[9].isEmpty() ? null : parts[9];
            String priorite = parts[10];
            String notes = parts[11].isEmpty() ? null : parts[11].replace("｜", ";");
            int nombreUtilisateurs = Integer.parseInt(parts[12]);
            boolean partage = parts[13].equals("OUI");
            int joursRappelAvantFin = Integer.parseInt(parts[14]);
            String frequencePaiement = parts[15];
            
            return new Abonnement(id, nomService, dateDebut, dateFin, prixMensuel, clientName, 
                derniereUtilisation, categorie, tags, groupeAbonnement, priorite, notes, 
                nombreUtilisateurs, partage, joursRappelAvantFin, frequencePaiement);
        }
        // Format standard (8 colonnes) - rétrocompatibilité
        else if (parts.length == 8) {
            id = parts[0];
            nomService = parts[1];
            dateDebut = LocalDate.parse(parts[2]);
            dateFin = LocalDate.parse(parts[3]);
            prixMensuel = parsePrice(parts[4]); // <-- tolère virgule
            clientName = parts[5];
            derniereUtilisation = parts[6].isEmpty() ? null : LocalDate.parse(parts[6]);
            categorie = parts[7];
        } else if (parts.length == 7) {
            nomService = parts[0];
            dateDebut = LocalDate.parse(parts[1]);
            dateFin = LocalDate.parse(parts[2]);
            prixMensuel = parsePrice(parts[3]);
            clientName = parts[4];
            derniereUtilisation = parts[5].isEmpty() ? null : LocalDate.parse(parts[5]);
            categorie = parts[6];
            id = UUID.randomUUID().toString();
        } else if (parts.length == 6) {
            nomService = parts[0];
            dateDebut = LocalDate.parse(parts[1]);
            dateFin = LocalDate.parse(parts[2]);
            prixMensuel = parsePrice(parts[3]);
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
        if (derniereUtilisation != null ? !derniereUtilisation.equals(that.derniereUtilisation) : that.derniereUtilisation != null) return false;
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

    public void valider() {
    if (prixMensuel < 0) {
        throw new IllegalArgumentException("Le prix mensuel ne peut pas être négatif.");
    }
    if (dateDebut == null || dateFin == null) {
        throw new IllegalArgumentException("Les dates de début et de fin doivent être renseignées.");
    }
    if (dateDebut.isAfter(dateFin)) {
        throw new IllegalArgumentException("La date de début ne peut pas être après la date de fin.");
    }
    if (nombreUtilisateurs <= 0) {
        throw new IllegalArgumentException("Le nombre d'utilisateurs doit être supérieur à 0.");
    }
    if (joursRappelAvantFin < 0) {
        throw new IllegalArgumentException("Les jours de rappel ne peuvent pas être négatifs.");
    }
    }
}
