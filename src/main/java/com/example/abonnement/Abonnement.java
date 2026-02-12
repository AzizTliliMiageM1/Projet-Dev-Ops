package com.example.abonnement;

import java.time.LocalDate;

/**
 * Classe de compatibilité pour les anciens tests qui utilisaient
 * le package `com.example.abonnement.Abonnement`.
 * Elle étend la nouvelle entité `com.projet.backend.domain.Abonnement`
 * pour éviter les refactors massifs des tests existants.
 */
public class Abonnement extends com.projet.backend.domain.Abonnement {

	public Abonnement() {
		super();
	}

	public Abonnement(String nomService, LocalDate dateDebut, LocalDate dateFin, double prixMensuel, String clientName) {
		super(nomService, dateDebut, dateFin, prixMensuel, clientName);
	}

	public Abonnement(String id, String nomService, LocalDate dateDebut, LocalDate dateFin, double prixMensuel, String clientName, LocalDate derniereUtilisation, String categorie) {
		super(id, nomService, dateDebut, dateFin, prixMensuel, clientName, derniereUtilisation, categorie);
	}
}
