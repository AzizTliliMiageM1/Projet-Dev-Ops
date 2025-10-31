package com.example.abonnement;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Comparator;
import java.util.Map;

public class AbonnementServiceImpl implements AbonnementService {
    private AbonnementRepository abonnementRepository;
    private List<Abonnement> listeAbonnements;

    public AbonnementServiceImpl(AbonnementRepository abonnementRepository) {
        this.abonnementRepository = abonnementRepository;
        this.listeAbonnements = abonnementRepository.findAll();
    }

    @Override
    public void ajouterAbonnement(String nomService, LocalDate dateDebut, LocalDate dateFin, double prixMensuel, String clientName, LocalDate derniereUtilisation, String categorie) {
        Abonnement nouvelAbonnement = new Abonnement(nomService, dateDebut, dateFin, prixMensuel, clientName, derniereUtilisation, categorie);
        listeAbonnements.add(nouvelAbonnement);
        sauvegarderAbonnements();
    }

    @Override
    public List<Abonnement> getAllAbonnements() {
        return new ArrayList<>(listeAbonnements); // Retourne une copie pour éviter les modifications externes directes
    }

    @Override
    public void modifierAbonnement(int index, String newClientName, String newNomService, LocalDate newDateDebut, LocalDate newDateFin, double newPrixMensuel, String newCategorie) {
        if (index >= 0 && index < listeAbonnements.size()) {
            Abonnement abonnement = listeAbonnements.get(index);
            if (newClientName != null && !newClientName.isEmpty()) {
                abonnement.setClientName(newClientName);
            }
            if (newNomService != null && !newNomService.isEmpty()) {
                abonnement.setNomService(newNomService);
            }
            if (newDateDebut != null) {
                abonnement.setDateDebut(newDateDebut);
            }
            if (newDateFin != null) {
                if (newDateFin.isBefore(abonnement.getDateDebut())) {
                    System.out.println("La nouvelle date de fin ne peut pas être antérieure à la date de début. Modification annulée.");
                } else {
                    abonnement.setDateFin(newDateFin);
                }
            }
            if (newPrixMensuel > 0) {
                abonnement.setPrixMensuel(newPrixMensuel);
            }
            if (newCategorie != null && !newCategorie.isEmpty()) {
                abonnement.setCategorie(newCategorie);
            }
            sauvegarderAbonnements();
        } else {
            throw new IllegalArgumentException("Index d'abonnement invalide.");
        }
    }

    @Override
    public void supprimerAbonnement(int index) {
        if (index >= 0 && index < listeAbonnements.size()) {
            listeAbonnements.remove(index);
            sauvegarderAbonnements();
        } else {
            throw new IllegalArgumentException("Index d'abonnement invalide.");
        }
    }

    @Override
    public List<Abonnement> rechercherAbonnement(String termeRecherche) {
        String lowerCaseTermeRecherche = termeRecherche.toLowerCase();
        return listeAbonnements.stream()
                .filter(a -> a.getClientName().toLowerCase().contains(lowerCaseTermeRecherche) ||
                             a.getNomService().toLowerCase().contains(lowerCaseTermeRecherche))
                .collect(Collectors.toList());
    }

    @Override
    public void enregistrerUtilisation(int index, LocalDate dateUtilisation) {
        if (index >= 0 && index < listeAbonnements.size()) {
            Abonnement abonnement = listeAbonnements.get(index);
            abonnement.setDerniereUtilisation(dateUtilisation);
            sauvegarderAbonnements();
        } else {
            throw new IllegalArgumentException("Index d'abonnement invalide.");
        }
    }

    @Override
    public List<String> verifierAlertesUtilisation() {
        List<String> alertes = new ArrayList<>();
        for (Abonnement abonnement : listeAbonnements) {
            if (abonnement.estActif() && abonnement.getDerniereUtilisation() != null) {
                long joursDepuisUtilisation = ChronoUnit.DAYS.between(abonnement.getDerniereUtilisation(), LocalDate.now());
                if (joursDepuisUtilisation > 30) {
                    alertes.add("ALERTE pour " + abonnement.getClientName() + " (Service: " + abonnement.getNomService() +
                                 "): Abonnement actif mais non utilisé depuis " + joursDepuisUtilisation + " jours.");
                }
            }
            if (abonnement.estActif()) {
                long joursRestants = ChronoUnit.DAYS.between(LocalDate.now(), abonnement.getDateFin());
                if (joursRestants <= 7 && joursRestants >=0 ) { // Alerte si moins de 7 jours restants
                    alertes.add("ALERTE pour " + abonnement.getClientName() + " (Service: " + abonnement.getNomService() +
                                 "): Expire dans " + joursRestants + " jours.");
                }
            }
        }
        return alertes;
    }

    @Override
    public void sauvegarderAbonnements() {
        abonnementRepository.saveAll(listeAbonnements);
    }

    @Override
    public DashboardStats getDashboardStats() {
        double totalMonthlyCost = listeAbonnements.stream()
                .filter(Abonnement::estActif)
                .mapToDouble(Abonnement::getPrixMensuel)
                .sum();

        Abonnement mostExpensiveAbonnement = listeAbonnements.stream()
                .filter(Abonnement::estActif)
                .max(Comparator.comparingDouble(Abonnement::getPrixMensuel))
                .orElse(null);

        List<Abonnement> expiringSoonAbonnements = listeAbonnements.stream()
                .filter(Abonnement::estActif)
                .filter(a -> ChronoUnit.DAYS.between(LocalDate.now(), a.getDateFin()) <= 30 && ChronoUnit.DAYS.between(LocalDate.now(), a.getDateFin()) >= 0)
                .collect(Collectors.toList());

        Map<String, Double> categorySpending = listeAbonnements.stream()
                .filter(Abonnement::estActif)
                .collect(Collectors.groupingBy(Abonnement::getCategorie,
                        Collectors.summingDouble(Abonnement::getPrixMensuel)));

        return new DashboardStats(totalMonthlyCost, mostExpensiveAbonnement, expiringSoonAbonnements, categorySpending);
    }
}

