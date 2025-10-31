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

            // Import JSON endpoint: accepts a JSON array of abonnements
            post("/abonnements/import", (req, res) -> {
                try {
                    // read as array of Abonnement
                    List<Abonnement> items = mapper.readValue(req.body(), new com.fasterxml.jackson.core.type.TypeReference<List<Abonnement>>(){});
                    if (items == null || items.isEmpty()) { res.status(400); return mapper.writeValueAsString(java.util.Map.of("error","no items to import")); }
                    // basic validation and ensure ids
                    List<Abonnement> toSave = new java.util.ArrayList<>();
                    for (Abonnement a : items) {
                        if (a.getNomService() == null || a.getNomService().isBlank()) continue;
                        if (a.getDateDebut() == null || a.getDateFin() == null) continue;
                        if (a.getClientName() == null || a.getClientName().isBlank()) continue;
                        if (a.getPrixMensuel() < 0) continue;
                        if (a.getId() == null || a.getId().isBlank()) a.setId(java.util.UUID.randomUUID().toString());
                        toSave.add(a);
                    }
                    if (toSave.isEmpty()) { res.status(400); return mapper.writeValueAsString(java.util.Map.of("error","no valid items to import")); }
                    // merge with existing and save all
                    List<Abonnement> existing = repo.findAll();
                    existing.addAll(toSave);
                    repo.saveAll(existing);
                    res.status(201);
                    return mapper.writeValueAsString(java.util.Map.of("imported", toSave.size()));
                } catch (com.fasterxml.jackson.core.JsonParseException | com.fasterxml.jackson.databind.JsonMappingException e) {
                    res.status(400);
                    return mapper.writeValueAsString(java.util.Map.of("error","JSON invalide: " + e.getMessage()));
                } catch (Exception e) {
                    res.status(500);
                    return mapper.writeValueAsString(java.util.Map.of("error","Erreur interne lors de l'import"));
                }
            });

            // Import JSON array endpoint: accepts a JSON array of abonnements
            post("/abonnements/import", (req, res) -> {
                res.type("application/json");
                try {
                    // parse body as array of Abonnement
                    java.util.List<Abonnement> arr = mapper.readValue(req.body(), new com.fasterxml.jackson.core.type.TypeReference<java.util.List<Abonnement>>(){});
                    if (arr == null || arr.isEmpty()) { res.status(400); return mapper.writeValueAsString(java.util.Map.of("error","fichier vide ou format invalide")); }

                    java.util.List<Abonnement> existing = repo.findAll();
                    java.util.List<String> errors = new java.util.ArrayList<>();
                    int imported = 0;
                    for (int i=0;i<arr.size();i++){
                        Abonnement a = arr.get(i);
                        // basic validation (reuse same rules)
                        if (a.getNomService() == null || a.getNomService().isBlank()) { errors.add("index="+i+" nomService manquant"); continue; }
                        if (a.getDateDebut() == null) { errors.add("index="+i+" dateDebut manquante"); continue; }
                        if (a.getDateFin() == null) { errors.add("index="+i+" dateFin manquante"); continue; }
                        if (a.getClientName() == null || a.getClientName().isBlank()) { errors.add("index="+i+" clientName manquant"); continue; }
                        if (a.getPrixMensuel() < 0) { errors.add("index="+i+" prixMensuel invalide"); continue; }
                        if (a.getId() == null || a.getId().isBlank()) a.setId(java.util.UUID.randomUUID().toString());
                        existing.add(a);
                        imported++;
                    }
                    // persist merged list
                    repo.saveAll(existing);
                    java.util.Map<String,Object> summary = new java.util.HashMap<>();
                    summary.put("imported", imported);
                    summary.put("errors", errors);
                    res.status(201);
                    return mapper.writeValueAsString(summary);
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
