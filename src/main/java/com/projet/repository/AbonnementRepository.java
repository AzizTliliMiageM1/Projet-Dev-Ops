package com.projet.repository;

import java.util.List;
import java.util.Optional;

import com.example.abonnement.Abonnement;

/**
 * Interface de persistance pour les Abonnements.
 * Définit les opérations utilisées par l'application.
 */
public interface AbonnementRepository {
	List<Abonnement> findAll();
	void saveAll(List<Abonnement> abonnements);
	Optional<Abonnement> findById(int id);
	Optional<Abonnement> findByUuid(String uuid);
	void save(Abonnement abonnement);
	void delete(Abonnement abonnement);
	void deleteByUuid(String uuid);
}
