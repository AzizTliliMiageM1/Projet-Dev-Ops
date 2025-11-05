package com.example.abonnement;

import static spark.Spark.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ApiServer {

    private static final String FICHIER_ABONNEMENTS = "abonnements.txt";
    private static final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final List<Abonnement> cache = new ArrayList<>();

    public static void main(String[] args) {
        // Port et fichiers statiques
        port(8080);
        staticFiles.location("/static"); // src/main/resources/static

        // Charger en mémoire au démarrage
        charger();

        // Routes REST
        get("/api/abonnements", (req, res) -> {
            res.type("application/json");
            synchronized (cache) {
                return mapper.writeValueAsString(cache);
            }
        });

        get("/api/abonnements/:id", (req, res) -> {
            res.type("application/json");
            String id = req.params(":id");
            Abonnement a;
            synchronized (cache) {
                a = trouverParId(id);
            }
            if (a == null) {
                res.status(404);
                return "{\"error\":\"Not found\"}";
            }
            return mapper.writeValueAsString(a);
        });

        post("/api/abonnements", (req, res) -> {
            res.type("application/json");
            Abonnement a = mapper.readValue(req.body(), Abonnement.class);
            // Sécuriser l'id (Abonnement a déjà un constructeur no-arg qui génère un id si vide,
            // mais si Jackson a mis id=null on s’assure ici)
            if (a.getId() == null || a.getId().isBlank()) {
                a.setId(java.util.UUID.randomUUID().toString());
            }
            synchronized (cache) {
                cache.add(a);
                sauvegarder();
            }
            res.status(201);
            return mapper.writeValueAsString(a);
        });

        put("/api/abonnements/:id", (req, res) -> {
            res.type("application/json");
            String id = req.params(":id");
            Abonnement maj = mapper.readValue(req.body(), Abonnement.class);
            maj.setId(id); // id depuis l’URL fait foi

            boolean updated = false;
            synchronized (cache) {
                for (int i = 0; i < cache.size(); i++) {
                    if (id.equals(cache.get(i).getId())) {
                        cache.set(i, maj);
                        updated = true;
                        break;
                    }
                }
                if (updated) {
                    sauvegarder();
                }
            }
            if (!updated) {
                res.status(404);
                return "{\"error\":\"Not found\"}";
            }
            return mapper.writeValueAsString(maj);
        });

        delete("/api/abonnements/:id", (req, res) -> {
            String id = req.params(":id");
            boolean removed = false;
            synchronized (cache) {
                for (int i = 0; i < cache.size(); i++) {
                    if (id.equals(cache.get(i).getId())) {
                        cache.remove(i);
                        removed = true;
                        break;
                    }
                }
                if (removed) {
                    sauvegarder();
                }
            }
            if (!removed) {
                res.status(404);
                return "{\"error\":\"Not found\"}";
            }
            res.status(204);
            return "";
        });
    }

    // Chargement depuis abonnements.txt
    private static void charger() {
        synchronized (cache) {
            cache.clear();
            try (BufferedReader br = new BufferedReader(new FileReader(FICHIER_ABONNEMENTS, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;
                    try {
                        cache.add(Abonnement.fromCsvString(line));
                    } catch (Exception e) {
                        // ignorer les lignes invalides
                    }
                }
            } catch (Exception e) {
                // fichier absent ou autre : démarrer avec une liste vide
            }
        }
    }

    // Sauvegarde vers abonnements.txt
    private static void sauvegarder() throws Exception {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FICHIER_ABONNEMENTS, StandardCharsets.UTF_8))) {
            for (Abonnement a : cache) {
                bw.write(a.toCsvString());
                bw.newLine();
            }
        }
    }

    private static Abonnement trouverParId(String id) {
        for (Abonnement a : cache) {
            if (id.equals(a.getId())) return a;
        }
        return null;
        }
}
