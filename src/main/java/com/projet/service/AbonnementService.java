package com.projet.service;

import java.time.LocalDate;
import java.util.List;

import com.example.abonnement.Abonnement;
import com.projet.dashboard.DashboardStats;

public interface AbonnementService {
	void ajouterAbonnement(String nomService, LocalDate dateDebut, LocalDate dateFin, double prixMensuel, String clientName, LocalDate derniereUtilisation, String categorie);
	List<Abonnement> getAllAbonnements();
	void modifierAbonnement(int index, String newClientName, String newNomService, LocalDate newDateDebut, LocalDate newDateFin, double newPrixMensuel, String newCategorie);
	void supprimerAbonnement(int index);
	List<Abonnement> rechercherAbonnement(String termeRecherche);
	void enregistrerUtilisation(int index, LocalDate dateUtilisation);
	List<String> verifierAlertesUtilisation();
	void sauvegarderAbonnements();
	DashboardStats getDashboardStats();
}

