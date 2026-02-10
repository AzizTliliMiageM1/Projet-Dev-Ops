package com.projet.backend.adapter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.projet.backend.domain.Abonnement;

/**
 * Adapter class for CSV serialization/deserialization of Abonnement.
 * Infrastructure concerns extracted from domain for purity.
 * 
 * Formats supported:
 * - Format complet (16 colonnes): id;nomService;dateDebut;dateFin;prixMensuel;clientName;...
 * - Format standard (8 colonnes): id;nomService;dateDebut;dateFin;prixMensuel;clientName;derniereUtilisation;categorie
 * - Format ancien (7 colonnes): nomService;dateDebut;dateFin;prixMensuel;clientName;derniereUtilisation;categorie
 * - Format legacy (6 colonnes): nomService;dateDebut;dateFin;prixMensuel;clientName;derniereUtilisation
 */
public class AbonnementCsvConverter {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Converts an Abonnement to CSV string format (16-column format).
     */
    public static String toCsvString(Abonnement abonnement) {
        return String.format(
                "%s;%s;%s;%s;%.2f;%s;%s;%s;%s;%s;%s;%s;%d;%s;%d;%s",
                abonnement.getId() != null ? abonnement.getId() : "",
                abonnement.getNomService() != null ? abonnement.getNomService() : "",
                abonnement.getDateDebut() != null ? abonnement.getDateDebut().format(FORMATTER) : "",
                abonnement.getDateFin() != null ? abonnement.getDateFin().format(FORMATTER) : "",
                abonnement.getPrixMensuel(),
                abonnement.getClientName() != null ? abonnement.getClientName() : "",
                (abonnement.getDerniereUtilisation() != null ? abonnement.getDerniereUtilisation().format(FORMATTER) : ""),
                abonnement.getCategorie() != null ? abonnement.getCategorie() : "Non classé",
                abonnement.getTags() != null && !abonnement.getTags().isEmpty() ? String.join("|", abonnement.getTags()) : "",
                abonnement.getGroupeAbonnement() != null ? abonnement.getGroupeAbonnement() : "",
                abonnement.getPriorite() != null ? abonnement.getPriorite() : "Important",
                abonnement.getNotes() != null ? abonnement.getNotes().replace(";", "｜").replace("\n", " ").replace("\r", " ") : "",
                abonnement.getNombreUtilisateurs(),
                abonnement.isPartage() ? "OUI" : "NON",
                abonnement.getJoursRappelAvantFin(),
                abonnement.getFrequencePaiement() != null ? abonnement.getFrequencePaiement() : "Mensuel"
        );
    }

    /**
     * Parses a string price value, tolerating both dot and comma as decimal separator.
     */
    private static double parsePrice(String s) {
        return Double.parseDouble(s.replace(',', '.').trim());
    }

    /**
     * Converts CSV string to Abonnement object.
     * Supports multiple CSV formats for backwards compatibility.
     */
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
            prixMensuel = parsePrice(parts[4]);
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
}
