package com.abonnements.service;

import com.abonnements.model.Abonnement;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service metier principal pour la gestion des abonnements.
 * Stockage en memoire (pas de BDD, pas de persistence).
 */
public class AbonnementService {

    private final List<Abonnement> abonnements = new ArrayList<>();

    public AbonnementService() {
        // donnees de demo pour tester sans avoir a tout configurer
        abonnements.add(new Abonnement("Netflix", 13.99, "streaming"));
        abonnements.add(new Abonnement("Spotify", 9.99, "musique"));
        abonnements.add(new Abonnement("Amazon Prime", 8.99, "streaming"));
        abonnements.add(new Abonnement("Microsoft 365", 10.00, "productivite"));
    }

    public List<Abonnement> getAll() {
        return Collections.unmodifiableList(abonnements);
    }

    public Optional<Abonnement> getById(String id) {
        return abonnements.stream()
                .filter(a -> a.getId().equals(id))
                .findFirst();
    }

    public Abonnement ajouter(Abonnement a) {
        abonnements.add(a);
        return a;
    }

    public boolean supprimer(String id) {
        return abonnements.removeIf(a -> a.getId().equals(id));
    }

    /**
     * Calcul du total mensuel de tous les abonnements actifs.
     * C'est la fonctionnalite metier principale.
     */
    public double calculerTotalMensuel() {
        return abonnements.stream()
                .filter(Abonnement::isActif)
                .mapToDouble(Abonnement::getPrixMensuel)
                .sum();
    }

    /**
     * Regroupe les abonnements par categorie et somme les prix.
     * Utile pour savoir ou part le plus d'argent.
     */
    public Map<String, Double> getStatsParCategorie() {
        return abonnements.stream()
                .filter(Abonnement::isActif)
                .collect(Collectors.groupingBy(
                        Abonnement::getCategorie,
                        Collectors.summingDouble(Abonnement::getPrixMensuel)
                ));
    }

    /**
     * Retourne la categorie qui coute le plus cher au total.
     */
    public String getCategoriesPlusChere() {
        return getStatsParCategorie().entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("aucune");
    }

    /**
     * Retourne une analyse complete du budget: total, annuel, par categorie, moyenne.
     */
    public Map<String, Object> getAnalyseBudget() {
        double total = calculerTotalMensuel();
        long nbActifs = abonnements.stream().filter(Abonnement::isActif).count();

        Map<String, Object> analyse = new LinkedHashMap<>();
        analyse.put("totalMensuel", Math.round(total * 100.0) / 100.0);
        analyse.put("totalAnnuel", Math.round(total * 12 * 100.0) / 100.0);
        analyse.put("nombreAbonnements", nbActifs);
        analyse.put("moyenneMensuelle", nbActifs == 0 ? 0 : Math.round(total / nbActifs * 100.0) / 100.0);
        analyse.put("statsParCategorie", getStatsParCategorie());
        analyse.put("categoriePlusChere", getCategoriesPlusChere());

        return analyse;
    }
}
