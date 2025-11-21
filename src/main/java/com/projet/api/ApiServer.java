package com.projet.api;

import java.util.List;
import java.util.Map;

import com.example.abonnement.Abonnement;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import com.projet.repository.AbonnementRepository;
import com.projet.repository.FileAbonnementRepository;
import com.projet.user.FileUserRepository;
import com.projet.user.User;
import com.projet.user.UserService;
import com.projet.user.UserServiceImpl;

import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.halt;
import static spark.Spark.path;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.put;
import static spark.Spark.staticFiles;

public class ApiServer {

    public static void main(String[] args) {

        // ---- PORT ----
        int httpPort = Integer.parseInt(
            System.getenv().getOrDefault("PORT", System.getProperty("app.port", "4567"))
        );
        port(httpPort);

        // ---- STATIC FILES ----
        staticFiles.location("/static");

        // ---- CORS ----
        spark.Spark.options("/*", (req, res) -> "OK");
        spark.Spark.before((req, res) -> {
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
            res.header("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
        });

        // =================================================
        // 🔒 PROTECTION : Routes abonnements uniquement si connecté
        // =================================================
        spark.Spark.before("/api/abonnements/*", (req, res) -> {
            String user = req.session().attribute("user");
            if (user == null) {
                halt(401, "Non autorisé");
            }
        });

        // ---- ABONNEMENT REPO ----
        String repoType = System.getenv().getOrDefault("REPO", "file");
        AbonnementRepository repo;
        if ("db".equalsIgnoreCase(repoType)) {
            String jdbc = System.getProperty("JDBC_URL", "jdbc:h2:./abonnements-db;AUTO_SERVER=TRUE");
            repo = new com.projet.repository.DatabaseAbonnementRepository(jdbc);
        } else {
            repo = new FileAbonnementRepository("abonnements.txt");
        }

        // ---- JSON ----
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);


        // =================================================
        //     🔵  API /api/...
        // =================================================
        path("/api", () -> {

            // ---------------------------------------
            // 🔵 ABONNEMENTS
            // ---------------------------------------

            get("/abonnements", (req, res) -> {
                res.type("application/json");
                List<Abonnement> list = repo.findAll();
                return mapper.writeValueAsString(list);
            });

            get("/abonnements/:id", (req, res) -> {
                res.type("application/json");
                String pid = req.params(":id");
                var opt = repo.findByUuid(pid);
                if (opt.isEmpty()) {
                    res.status(404);
                    return "{\"error\":\"Not found\"}";
                }
                return mapper.writeValueAsString(opt.get());
            });

            post("/abonnements", (req, res) -> {
                Abonnement a = mapper.readValue(req.body(), Abonnement.class);

                if (a.getNomService() == null || a.getNomService().isBlank()) { res.status(400); return "{\"error\":\"nomService manquant\"}"; }
                if (a.getDateDebut() == null) { res.status(400); return "{\"error\":\"dateDebut manquante\"}"; }
                if (a.getDateFin() == null) { res.status(400); return "{\"error\":\"dateFin manquante\"}"; }
                if (a.getClientName() == null || a.getClientName().isBlank()) { res.status(400); return "{\"error\":\"clientName manquant\"}"; }
                if (a.getPrixMensuel() < 0) { res.status(400); return "{\"error\":\"prixMensuel invalide\"}"; }

                if (a.getId() == null || a.getId().isBlank()) {
                    a.setId(java.util.UUID.randomUUID().toString());
                }

                repo.save(a);
                res.status(201);
                res.type("application/json");
                return mapper.writeValueAsString(a);
            });

            post("/abonnements/import", (req, res) -> {
                res.type("application/json");
                List<Abonnement> arr = mapper.readValue(
                    req.body(),
                    new com.fasterxml.jackson.core.type.TypeReference<List<Abonnement>>() {}
                );

                if (arr == null || arr.isEmpty()) {
                    res.status(400);
                    return mapper.writeValueAsString(Map.of("error", "fichier vide ou format invalide"));
                }

                List<Abonnement> existing = repo.findAll();
                List<String> errors = new java.util.ArrayList<>();
                int imported = 0;

                for (int i = 0; i < arr.size(); i++) {
                    Abonnement a = arr.get(i);

                    if (a.getNomService() == null || a.getNomService().isBlank()) { errors.add("index=" + i + " nomService manquant"); continue; }
                    if (a.getDateDebut() == null) { errors.add("index=" + i + " dateDebut manquante"); continue; }
                    if (a.getDateFin() == null) { errors.add("index=" + i + " dateFin manquante"); continue; }
                    if (a.getClientName() == null || a.getClientName().isBlank()) { errors.add("index=" + i + " clientName manquant"); continue; }
                    if (a.getPrixMensuel() < 0) { errors.add("index=" + i + " prixMensuel invalide"); continue; }

                    if (a.getId() == null || a.getId().isBlank()) {
                        a.setId(java.util.UUID.randomUUID().toString());
                    }

                    existing.add(a);
                    imported++;
                }

                repo.saveAll(existing);
                res.status(201);
                return mapper.writeValueAsString(Map.of("imported", imported, "errors", errors));
            });

            put("/abonnements/:id", (req, res) -> {
                String pid = req.params(":id");

                Abonnement updated = mapper.readValue(req.body(), Abonnement.class);
                var opt = repo.findByUuid(pid);

                if (opt.isEmpty()) {
                    res.status(404);
                    return "{\"error\":\"Not found\"}";
                }

                updated.setId(pid);

                List<Abonnement> all = repo.findAll();
                for (int i = 0; i < all.size(); i++) {
                    if (pid.equals(all.get(i).getId())) {
                        all.set(i, updated);
                        break;
                    }
                }

                repo.saveAll(all);
                res.type("application/json");
                return mapper.writeValueAsString(updated);
            });

            delete("/abonnements/:id", (req, res) -> {
                String pid = req.params(":id");
                var opt = repo.findByUuid(pid);
                if (opt.isEmpty()) {
                    res.status(404);
                    return "{\"error\":\"Not found\"}";
                }
                repo.deleteByUuid(pid);
                res.status(204);
                return "";
            });


            // =================================================
            //     🔵  INSCRIPTION UTILISATEUR
            // =================================================
            post("/register", (req, res) -> {
                res.type("text/plain");

                String email = req.queryParams("email");
                String password = req.queryParams("password");

                UserService service = new UserServiceImpl();
                String token = service.register(email, password);

                if (token == null) {
                    res.status(400);
                    return "Email déjà utilisé.";
                }

                return "Inscription réussie ! Vérifiez votre email pour confirmer votre compte.";
            });


            // =================================================
            //     🔵  CONFIRMATION PAR EMAIL
            // =================================================
            get("/confirm", (req, res) -> {

                String token = req.queryParams("token");
                FileUserRepository repoUser = new FileUserRepository();
                User user = repoUser.findByToken(token);

                if (user == null) {
                    res.status(400);
                    return "Token invalide.";
                }

                user.setConfirmed(true);
                repoUser.update(user);

                res.redirect("/confirm.html");
                return null;
            });


            // =================================================
            // 🔵  CONNEXION UTILISATEUR (SESSION)
// =================================================
post("/login", (req, res) -> {
    res.type("text/plain");

    String email = req.queryParams("email");
    String password = req.queryParams("password");

    FileUserRepository repoUser = new FileUserRepository();
    User user = repoUser.findByEmail(email);

    if (user == null) {
        res.status(400);
        return "Utilisateur inconnu";
    }

    if (!user.getPassword().equals(password)) {
        res.status(400);
        return "Mot de passe incorrect";
    }

    if (!user.isConfirmed()) {
        res.status(400);
        return "Veuillez confirmer votre compte avant de vous connecter.";
    }

    // 🔵 Création session
    req.session(true).attribute("user", email);

    return "Connexion réussie !";
});

// =================================================
// 🔵  LOGOUT
// =================================================
post("/logout", (req, res) -> {
    req.session().invalidate();
    return "Déconnecté.";
});

// =================================================
// 🔵  STATUS SESSION
// =================================================
get("/session", (req, res) -> {
    String user = req.session().attribute("user");
    if (user == null) return "Aucun utilisateur connecté";
    return "Connecté : " + user;
});


            // =================================================
            //     🔵  TEST EMAIL
            // =================================================
            get("/test-mail", (req, res) -> {
                res.type("text/plain");
                com.projet.email.EmailService email = new com.projet.email.EmailServiceImpl();
                email.sendEmail("f.mayssara@gmail.com", "Test DevOps", "Ceci est un email de test.");
                return "Test d'envoi d'email déclenché.";
            });

        }); // end /api


        System.out.println("API server démarré sur http://localhost:" + httpPort);
    }
}
