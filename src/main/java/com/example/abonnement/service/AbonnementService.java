package com.example.abonnement.service;

import com.example.abonnement.Abonnement;
import java.util.List;

public interface AbonnementService {

    List<Abonnement> listerAbonnements();

    Abonnement trouverParId(String id);

    void ajouterAbonnement(Abonnement abonnement);

    boolean modifierAbonnement(Abonnement abonnement);

    boolean supprimerAbonnement(String id);
}
