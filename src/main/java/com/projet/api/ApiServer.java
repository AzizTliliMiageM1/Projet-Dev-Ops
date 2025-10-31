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
import static spark.Spark.staticFiles;

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
        // Servir les fichiers statiques (UI web) depuis resources/static
        staticFiles.location("/static");
        String repoType = System.getenv().getOrDefault("REPO", "file");
        AbonnementRepository repo;
        if ("db".equalsIgnoreCase(repoType)) {
            // Use H2 embedded DB file 'abonnements-db' in working dir
            String jdbc = System.getProperty("JDBC_URL", "jdbc:h2:./abonnements-db;AUTO_SERVER=TRUE");
            repo = new com.projet.repository.DatabaseAbonnementRepository(jdbc);
        } else {
            String dataFile = "abonnements.txt"; // même fichier que l'app console
            repo = new FileAbonnementRepository(dataFile);
        }

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
                String pid = req.params(":id");
                // treat :id as UUID only
                var opt = repo.findByUuid(pid);
                if (opt.isEmpty()) { res.status(404); return "{\"error\":\"Not found\"}"; }
                try { return mapper.writeValueAsString(opt.get()); } catch (Exception e) { res.status(500); return "{\"error\":\"serialization error\"}"; }
            });

            post("/abonnements", (req, res) -> {
                try {
                    Abonnement a = mapper.readValue(req.body(), Abonnement.class);
                    // server-side validation
                    if (a.getNomService() == null || a.getNomService().isBlank()) { res.status(400); return mapper.writeValueAsString(java.util.Map.of("error","nomService manquant")); }
                    if (a.getDateDebut() == null) { res.status(400); return mapper.writeValueAsString(java.util.Map.of("error","dateDebut manquante")); }
                    if (a.getDateFin() == null) { res.status(400); return mapper.writeValueAsString(java.util.Map.of("error","dateFin manquante")); }
                    if (a.getClientName() == null || a.getClientName().isBlank()) { res.status(400); return mapper.writeValueAsString(java.util.Map.of("error","clientName manquant")); }
                    if (a.getPrixMensuel() < 0) { res.status(400); return mapper.writeValueAsString(java.util.Map.of("error","prixMensuel invalide")); }

                    // ensure id exists
                    if (a.getId() == null || a.getId().isBlank()) a.setId(java.util.UUID.randomUUID().toString());
                    repo.save(a);
                    res.status(201);
                    res.type("application/json");
                    return mapper.writeValueAsString(a);
                } catch (com.fasterxml.jackson.databind.JsonMappingException e) {
                    res.status(400);
                    res.type("application/json");
                    return mapper.writeValueAsString(java.util.Map.of("error","JSON invalide: " + e.getMessage()));
                } catch (Exception e) {
                    res.status(500);
                    res.type("application/json");
                    return mapper.writeValueAsString(java.util.Map.of("error","Erreur interne"));
                }
            });

            put("/abonnements/:id", (req, res) -> {
                String pid = req.params(":id");
                Abonnement updated = mapper.readValue(req.body(), Abonnement.class);
                var opt = repo.findByUuid(pid);
                if (opt.isEmpty()) { res.status(404); return "{\"error\":\"Not found\"}"; }
                // ensure updated has same id
                updated.setId(pid);
                // fetch all, replace matching uuid
                List<Abonnement> all = repo.findAll();
                for (int i=0;i<all.size();i++){
                    if (pid.equals(all.get(i).getId())) { all.set(i, updated); break; }
                }
                repo.saveAll(all);
                res.type("application/json");
                return mapper.writeValueAsString(updated);
            });

            delete("/abonnements/:id", (req, res) -> {
                String pid = req.params(":id");
                var opt = repo.findByUuid(pid);
                if (opt.isEmpty()) { res.status(404); return "{\"error\":\"Not found\"}"; }
                repo.deleteByUuid(pid);
                res.status(204);
                return "";
            });
        });

        System.out.println("API server démarré sur http://localhost:4567/api/abonnements");
    }
}
