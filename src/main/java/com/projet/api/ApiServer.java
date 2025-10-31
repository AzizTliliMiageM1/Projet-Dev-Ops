package com.projet.api;

import java.util.List;

import com.example.abonnement.Abonnement;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.projet.repository.AbonnementRepository;
import com.projet.repository.FileAbonnementRepository;

import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.path;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.put;

/**
 * Petit serveur HTTP REST pour exposer les abonnements.
 * Endpoints:
 *  - GET  /api/abonnements
 *  - GET  /api/abonnements/:id
 *  - POST /api/abonnements
 *  - PUT  /api/abonnements/:id
 *  - DELETE /api/abonnements/:id
 */
public class ApiServer {
    public static void main(String[] args) {
        port(4567);
        String dataFile = "abonnements.txt"; // même fichier que l'app console
        AbonnementRepository repo = new FileAbonnementRepository(dataFile);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        path("/api", () -> {
            get("/abonnements", (req, res) -> {
                res.type("application/json");
                List<Abonnement> list = repo.findAll();
                try {
                    return mapper.writeValueAsString(list);
                } catch (Exception e) {
                    res.status(500);
                    return "{\"error\":\"serialization error\"}";
                }
            });

            get("/abonnements/:id", (req, res) -> {
                res.type("application/json");
                try {
                    int id = Integer.parseInt(req.params(":id"));
                    var opt = repo.findById(id);
                    if (opt.isEmpty()) {
                        res.status(404);
                        return "{\"error\":\"Not found\"}";
                    }
                    try {
                        return mapper.writeValueAsString(opt.get());
                    } catch (Exception e) {
                        res.status(500);
                        return "{\"error\":\"serialization error\"}";
                    }
                } catch (NumberFormatException e) {
                    res.status(400);
                    return "{\"error\":\"Invalid id\"}";
                }
            });

            post("/abonnements", (req, res) -> {
                try {
                    Abonnement a = mapper.readValue(req.body(), Abonnement.class);
                    repo.save(a);
                    res.status(201);
                    res.type("application/json");
                    return mapper.writeValueAsString(a);
                } catch (Exception e) {
                    res.status(400);
                    res.type("application/json");
                    return "{\"error\":\"Bad request: " + e.getMessage() + "\"}";
                }
            });

            put("/abonnements/:id", (req, res) -> {
                try {
                    int id = Integer.parseInt(req.params(":id"));
                    List<Abonnement> all = repo.findAll();
                    if (id < 0 || id >= all.size()) {
                        res.status(404);
                        return "{\"error\":\"Not found\"}";
                    }
                    Abonnement updated = mapper.readValue(req.body(), Abonnement.class);
                    all.set(id, updated);
                    repo.saveAll(all);
                    res.type("application/json");
                    return mapper.writeValueAsString(updated);
                } catch (NumberFormatException e) {
                    res.status(400);
                    return "{\"error\":\"Invalid id\"}";
                }
            });

            delete("/abonnements/:id", (req, res) -> {
                try {
                    int id = Integer.parseInt(req.params(":id"));
                    List<Abonnement> all = repo.findAll();
                    if (id < 0 || id >= all.size()) {
                        res.status(404);
                        return "{\"error\":\"Not found\"}";
                    }
                    Abonnement toDelete = all.get(id);
                    repo.delete(toDelete);
                    res.status(204);
                    return "";
                } catch (NumberFormatException e) {
                    res.status(400);
                    return "{\"error\":\"Invalid id\"}";
                }
            });
        });

        System.out.println("API server démarré sur http://localhost:4567/api/abonnements");
    }
}
