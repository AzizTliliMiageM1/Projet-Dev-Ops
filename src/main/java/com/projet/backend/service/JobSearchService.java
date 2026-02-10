package com.projet.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.projet.backend.domain.Abonnement;
import com.projet.repository.AbonnementRepository;

/**
 * Service léger de recherche locale sur les abonnements.
 * Utilise l'interface existante `AbonnementRepository` et n'introduit aucune
 * dépendance supplémentaire. Conçu pour être non-invasif et compatible
 * avec l'architecture actuelle.
 */
public class JobSearchService {

    private final AbonnementRepository repository;

    public JobSearchService(AbonnementRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    /**
     * Recherche combinée : filtre par catégorie (exacte, case-insensitive),
     * texte libre (présent dans le nom du service ou notes) et fourchette de prix.
     * Tous les critères sont optionnels (null = ignored).
     *
     * @param categorie catégorie à filtrer (ou null)
     * @param texte texte libre à rechercher (ou null)
     * @param minPrix prix minimum inclusif (ou null)
     * @param maxPrix prix maximum inclusif (ou null)
     * @return liste d'abonnements correspondant aux critères
     */
    public List<Abonnement> search(String categorie, String texte, Double minPrix, Double maxPrix) {
        List<Abonnement> all = repository.findAll();
        if (all == null || all.isEmpty()) return new ArrayList<>();

        String texteNorm = (texte == null) ? null : texte.trim().toLowerCase();
        String catNorm = (categorie == null) ? null : categorie.trim().toLowerCase();

        return all.stream()
            .filter(a -> {
                // catégorie
                if (catNorm != null && (a.getCategorie() == null || !a.getCategorie().toLowerCase().equals(catNorm))) return false;

                // texte libre dans nomService ou notes
                if (texteNorm != null && !texteNorm.isEmpty()) {
                    boolean foundInName = a.getNomService() != null && a.getNomService().toLowerCase().contains(texteNorm);
                    boolean foundInNotes = a.getNotes() != null && a.getNotes().toLowerCase().contains(texteNorm);
                    if (!(foundInName || foundInNotes)) return false;
                }

                // prix
                double prix = a.getPrixMensuel();
                if (minPrix != null && prix < minPrix) return false;
                if (maxPrix != null && prix > maxPrix) return false;

                return true;
            })
            .collect(Collectors.toList());
    }

    /**
     * Recherche rapide par texte uniquement.
     */
    public List<Abonnement> searchByText(String texte) {
        return search(null, texte, null, null);
    }

    /**
     * Recherche par catégorie uniquement.
     */
    public List<Abonnement> searchByCategory(String categorie) {
        return search(categorie, null, null, null);
    }

    /**
     * Recherche par fourchette de prix uniquement.
     */
    public List<Abonnement> searchByPriceRange(Double minPrix, Double maxPrix) {
        return search(null, null, minPrix, maxPrix);
    }
}
