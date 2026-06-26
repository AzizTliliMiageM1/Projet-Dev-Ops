package com.abonnements.api;

import com.abonnements.model.Abonnement;
import com.abonnements.service.AbonnementService;
import com.abonnements.service.DeviseService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

import static spark.Spark.*;

/**
 * Serveur REST construit avec Spark Framework.
 *
 * Endpoints disponibles:
 *   GET  /health                        → statut du serveur
 *   GET  /api/abonnements               → liste tous les abonnements
 *   POST /api/abonnements               → ajouter un abonnement (JSON body)
 *   DELETE /api/abonnements/:id         → supprimer par id
 *   GET  /api/budget/total              → total mensuel en EUR
 *   GET  /api/budget/stats              → analyse complete par categorie
 *   GET  /api/budget/convertir?devise=  → conversion via API Frankfurter (distante)
 */
public class ApiServer {

    private final AbonnementService abonnementService;
    private final DeviseService deviseService;
    private final ObjectMapper mapper;

    public ApiServer() {
        this.abonnementService = new AbonnementService();
        this.deviseService = new DeviseService();
        this.mapper = new ObjectMapper();
    }

    public void start(int port) {
        port(port);

        staticFiles.location("/static");

        // CORS : autoriser les appels depuis le navigateur
        before((req, res) -> {
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
            res.header("Access-Control-Allow-Headers", "Content-Type");
            res.type("application/json");
        });

        options("/*", (req, res) -> "OK");

        get("/health", (req, res) -> {
            Map<String, String> status = new HashMap<>();
            status.put("status", "ok");
            status.put("application", "Budget Tracker");
            status.put("version", "1.0");
            return mapper.writeValueAsString(status);
        });

        get("/api/abonnements", (req, res) ->
                mapper.writeValueAsString(abonnementService.getAll())
        );

        post("/api/abonnements", (req, res) -> {
            try {
                Abonnement a = mapper.readValue(req.body(), Abonnement.class);

                if (a.getNom() == null || a.getNom().isBlank()) {
                    res.status(400);
                    return erreur("Le champ 'nom' est obligatoire");
                }
                if (a.getPrixMensuel() <= 0) {
                    res.status(400);
                    return erreur("Le prix mensuel doit etre superieur a 0");
                }

                Abonnement cree = abonnementService.ajouter(a);
                res.status(201);
                return mapper.writeValueAsString(cree);

            } catch (Exception e) {
                res.status(400);
                return erreur("Corps JSON invalide: " + e.getMessage());
            }
        });

        delete("/api/abonnements/:id", (req, res) -> {
            String id = req.params(":id");
            boolean supprime = abonnementService.supprimer(id);
            if (supprime) {
                return message("Abonnement supprime avec succes");
            }
            res.status(404);
            return erreur("Abonnement introuvable pour id: " + id);
        });

        get("/api/budget/total", (req, res) -> {
            double total = abonnementService.calculerTotalMensuel();
            Map<String, Object> rep = new HashMap<>();
            rep.put("totalMensuel", total);
            rep.put("totalAnnuel", Math.round(total * 12 * 100.0) / 100.0);
            rep.put("devise", "EUR");
            return mapper.writeValueAsString(rep);
        });

        get("/api/budget/stats", (req, res) ->
                mapper.writeValueAsString(abonnementService.getAnalyseBudget())
        );

        // appel API distante Frankfurter pour convertir le total en devise
        get("/api/budget/convertir", (req, res) -> {
            String devise = req.queryParams("devise");

            if (devise == null || devise.isBlank()) {
                res.status(400);
                return erreur("Parametre 'devise' manquant. Exemple: ?devise=USD");
            }

            try {
                double total = abonnementService.calculerTotalMensuel();
                Map<String, Object> result = deviseService.convertir(total, devise);
                return mapper.writeValueAsString(result);
            } catch (Exception e) {
                res.status(500);
                return erreur(e.getMessage());
            }
        });

        System.out.println("==============================================");
        System.out.println("  Budget Tracker demarre sur le port " + port);
        System.out.println("  http://localhost:" + port + "/health");
        System.out.println("  http://localhost:" + port + "/api/abonnements");
        System.out.println("  http://localhost:" + port + "/api/budget/total");
        System.out.println("  http://localhost:" + port + "/api/budget/stats");
        System.out.println("  http://localhost:" + port + "/api/budget/convertir?devise=USD");
        System.out.println("==============================================");
    }

    private String erreur(String msg) throws Exception {
        Map<String, String> m = new HashMap<>();
        m.put("erreur", msg);
        return mapper.writeValueAsString(m);
    }

    private String message(String msg) throws Exception {
        Map<String, String> m = new HashMap<>();
        m.put("message", msg);
        return mapper.writeValueAsString(m);
    }
}
