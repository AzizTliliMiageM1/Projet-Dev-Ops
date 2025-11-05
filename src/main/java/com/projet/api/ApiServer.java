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
 *  - GET    /api/abonnements
 *  - GET    /api/abonnements/:id
 *  - POST   /api/abonnements
 *  - POST   /api/abonnements/import
 *  - PUT    /api/abonnements/:id
 *  - DELETE /api/abonnements/:id
 */
public class ApiServer {
    public static void main(String[] args) {
        // ---- Port configurable (env PORT ou -Dapp.port), fallback 4567 ----
        int httpPort = Integer.parseInt(
            System.getenv().getOrDefault("PORT", System.getProperty("app.port", "4567"))
        );
        port(httpPort);

        // ---- Fichiers statiques (UI) ----
        staticFiles.location("/static");

        // ---- CORS basique ----
        spark.Spark.options("/*", (req, res) -> "OK");
        spark.Spark.before((req, res) -> {
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
            res.header("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
        });

        // ---- Repo ----
        String repoType = System.getenv().getOrDefault("REPO", "file");
        AbonnementRepository repo;
        if ("db".equalsIgnoreCase(repoType)) {
            String jdbc = System.getProperty("JDBC_URL", "jdbc:h2:./abonnements-db;AUTO_SERVER=TRUE");
            repo = new com.projet.repository.DatabaseAbonnementRepository(jdbc);
        } else {
            String dataFile = "abonnements.txt"; // même fichier que l'app console
            repo = new FileAbonnementRepository(dataFile);
        }

        // ---- ObjectMapper ----
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // ---- API ----
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
                var opt = repo.findByUuid(pid);
                if (opt.isEmpty()) { res.status(404); return "{\"error\":\"Not found\"}"; }
                try {
                    return mapper.writeValueAsString(opt.get());
                } catch (Exception e) {
                    res.status(500);
                    return "{\"error\":\"serialization error\"}";
                }
            });

            post("/abonnements", (req, res) -> {
                try {
                    Abonnement a = mapper.readValue(req.body(), Abonnement.class);
                    // validations simples
                    if (a.getNomService() == null || a.getNomService().isBlank()) { res.status(400); return mapper.writeValueAsString(java.util.Map.of("error","nomService manquant")); }
                    if (a.getDateDebut() == null) { res.status(400); return mapper.writeValueAsString(java.util.Map.of("error","dateDebut manquante")); }
                    if (a.getDateFin() == null) { res.status(400); return mapper.writeValueAsString(java.util.Map.of("error","dateFin manquante")); }
                    if (a.getClientName() == null || a.getClientName().isBlank()) { res.status(400); return mapper.writeValueAsString(java.util.Map.of("error","clientName manquant")); }
                    if (a.getPrixMensuel() < 0) { res.status(400); return mapper.writeValueAsString(java.util.Map.of("error","prixMensuel invalide")); }

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

            // ---- Import JSON (un seul endpoint) : body = [Abonnement,...] ----
            post("/abonnements/import", (req, res) -> {
                res.type("application/json");
                try {
                    List<Abonnement> arr = mapper.readValue(
                        req.body(),
                        new com.fasterxml.jackson.core.type.TypeReference<List<Abonnement>>(){}
                    );
                    if (arr == null || arr.isEmpty()) {
                        res.status(400);
                        return mapper.writeValueAsString(java.util.Map.of("error","fichier vide ou format invalide"));
                    }

                    List<Abonnement> existing = repo.findAll();
                    List<String> errors = new java.util.ArrayList<>();
                    int imported = 0;

                    for (int i = 0; i < arr.size(); i++) {
                        Abonnement a = arr.get(i);
                        if (a.getNomService() == null || a.getNomService().isBlank()) { errors.add("index="+i+" nomService manquant"); continue; }
                        if (a.getDateDebut() == null) { errors.add("index="+i+" dateDebut manquante"); continue; }
                        if (a.getDateFin() == null) { errors.add("index="+i+" dateFin manquante"); continue; }
                        if (a.getClientName() == null || a.getClientName().isBlank()) { errors.add("index="+i+" clientName manquant"); continue; }
                        if (a.getPrixMensuel() < 0) { errors.add("index="+i+" prixMensuel invalide"); continue; }
                        if (a.getId() == null || a.getId().isBlank()) a.setId(java.util.UUID.randomUUID().toString());
                        existing.add(a);
                        imported++;
                    }

                    repo.saveAll(existing);
                    res.status(201);
                    return mapper.writeValueAsString(java.util.Map.of("imported", imported, "errors", errors));
                } catch (com.fasterxml.jackson.core.JsonParseException | com.fasterxml.jackson.databind.JsonMappingException e) {
                    res.status(400);
                    return mapper.writeValueAsString(java.util.Map.of("error","JSON invalide: "+e.getMessage()));
                } catch (Exception e) {
                    res.status(500);
                    return mapper.writeValueAsString(java.util.Map.of("error","Erreur interne"));
                }
            });

            put("/abonnements/:id", (req, res) -> {
                String pid = req.params(":id");
                Abonnement updated = mapper.readValue(req.body(), Abonnement.class);
                var opt = repo.findByUuid(pid);
                if (opt.isEmpty()) { res.status(404); return "{\"error\":\"Not found\"}"; }
                updated.setId(pid);
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

        // ---- Arrêt propre ----
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try { spark.Spark.stop(); } catch (Exception ignored) {}
        }));

        System.out.println("API server démarré sur http://localhost:" + httpPort + "/api/abonnements");
    }
}
