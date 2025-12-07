package com.projet.api;

import java.time.LocalDate;
import java.util.ArrayList;
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
import com.projet.analytics.SubscriptionAnalytics;
import com.projet.service.SubscriptionOptimizer;

import spark.Request;
import static spark.Spark.before;
import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.path;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.put;
import static spark.Spark.staticFiles;

public class ApiServer {

    private static AbonnementRepository getOrCreateRepo(Request req) {
        AbonnementRepository repo = req.attribute("userRepo");
        if (repo == null) {
            String user = req.session().attribute("user_email");
            repo = user == null 
                ? new FileAbonnementRepository("abonnements.txt")
                : new com.projet.repository.UserAbonnementRepository(user);
        }
        return repo;
    }

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

        // ---- JSON ----
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);


        // =================================================
        //     🔵  API /api/...
        // =================================================
        path("/api", () -> {

            // =================================================
            // 🔒 PROTECTION : Routes abonnements - créer repo par utilisateur
            // =================================================
            before("/abonnements*", (req, res) -> {
                String user = req.session().attribute("user_email");
                
                AbonnementRepository userRepo;
                if (user == null) {
                    // Utilisateur non connecté : utiliser fichier partagé
                    userRepo = new FileAbonnementRepository("abonnements.txt");
                } else {
                    // Utilisateur connecté : fichier personnel
                    userRepo = new com.projet.repository.UserAbonnementRepository(user);
                }
                
                req.attribute("userRepo", userRepo);
            });

            // ---------------------------------------
            // 🔵 ABONNEMENTS (PAR UTILISATEUR)
            // ---------------------------------------

            get("/abonnements", (req, res) -> {
                res.type("application/json");
                AbonnementRepository repo = getOrCreateRepo(req);
                List<Abonnement> list = repo.findAll();
                return mapper.writeValueAsString(list);
            });

            get("/abonnements/:id", (req, res) -> {
                res.type("application/json");
                AbonnementRepository repo = getOrCreateRepo(req);
                String pid = req.params(":id");
                var opt = repo.findByUuid(pid);
                if (opt.isEmpty()) {
                    res.status(404);
                    return "{\"error\":\"Not found\"}";
                }
                return mapper.writeValueAsString(opt.get());
            });

            // 🔵 PRÉVISION DU COÛT SUR 3 MOIS
            get("/prediction", (req, res) -> {
                res.type("application/json");

                AbonnementRepository repo = getOrCreateRepo(req);
                List<Abonnement> list = repo.findAll();

                double total = 0;
                for (Abonnement a : list) {
                    if (a.getPrixMensuel() > 0) {
                        total += a.getPrixMensuel() * 3;
                    }
                }

                return mapper.writeValueAsString(Map.of(
                    "prediction3mois", total
                ));
            });


            post("/abonnements", (req, res) -> {
                // Vérification : seuls les utilisateurs connectés peuvent ajouter
                String user = req.session().attribute("user_email");
                if (user == null) {
                    res.status(401);
                    return "{\"error\":\"Vous devez être connecté pour ajouter un abonnement\"}";
                }
                
                Abonnement a = mapper.readValue(req.body(), Abonnement.class);

                if (a.getNomService() == null || a.getNomService().isBlank()) { res.status(400); return "{\"error\":\"nomService manquant\"}"; }
                if (a.getDateDebut() == null) { res.status(400); return "{\"error\":\"dateDebut manquante\"}"; }
                if (a.getDateFin() == null) { res.status(400); return "{\"error\":\"dateFin manquante\"}"; }
                if (a.getClientName() == null || a.getClientName().isBlank()) { res.status(400); return "{\"error\":\"clientName manquant\"}"; }
                if (a.getPrixMensuel() < 0) { res.status(400); return "{\"error\":\"prixMensuel invalide\"}"; }

                if (a.getId() == null || a.getId().isBlank()) {
                    a.setId(java.util.UUID.randomUUID().toString());
                }

                AbonnementRepository repo = getOrCreateRepo(req);
                repo.save(a);
                res.status(201);
                res.type("application/json");
                return mapper.writeValueAsString(a);
            });

            post("/abonnements/import", (req, res) -> {
                // Vérification : seuls les utilisateurs connectés peuvent importer
                String user = req.session().attribute("user_email");
                if (user == null) {
                    res.status(401);
                    return "{\"error\":\"Vous devez être connecté pour importer des abonnements\"}";
                }
                
                res.type("application/json");
                AbonnementRepository repo = getOrCreateRepo(req);
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
                // Vérification : seuls les utilisateurs connectés peuvent modifier
                String user = req.session().attribute("user_email");
                if (user == null) {
                    res.status(401);
                    return "{\"error\":\"Vous devez être connecté pour modifier un abonnement\"}";
                }
                
                String pid = req.params(":id");
                AbonnementRepository repo = getOrCreateRepo(req);

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
                // Vérification : seuls les utilisateurs connectés peuvent supprimer
                String user = req.session().attribute("user_email");
                if (user == null) {
                    res.status(401);
                    return "{\"error\":\"Vous devez être connecté pour supprimer un abonnement\"}";
                }
                
                String pid = req.params(":id");
                AbonnementRepository repo = getOrCreateRepo(req);
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
            // 🔵  EXPORT CSV DES ABONNEMENTS
            // =================================================
            get("/abonnements/export/csv", (req, res) -> {

                AbonnementRepository repo = getOrCreateRepo(req);
                List<Abonnement> list = repo.findAll();

                res.type("text/csv; charset=utf-8");
                res.header("Content-Disposition", "attachment; filename=\"abonnements.csv\"");

                StringBuilder csv = new StringBuilder();
                csv.append("id;nomService;dateDebut;dateFin;prixMensuel;clientName;derniereUtilisation;categorie\n");

                for (Abonnement a : list) {
                    csv.append(safeCsv(a.getId())).append(";")
                       .append(a.getNomService()).append(";")
                       .append(a.getDateDebut()).append(";")
                       .append(a.getDateFin()).append(";")
                       .append(a.getPrixMensuel()).append(";")
                       .append(a.getClientName()).append(";")
                       .append(a.getDerniereUtilisation()).append(";")
                       .append(a.getCategorie())
                       .append("\n");
                }

                return csv.toString();
            });

                        // =================================================
            // 🔵  IMPORT CSV DES ABONNEMENTS
            // =================================================
            post("/abonnements/import/csv", (req, res) -> {

                String user = req.session().attribute("user_email");
                if (user == null) {
                    res.status(401);
                    return "{\"error\":\"Vous devez être connecté pour importer (CSV)\"}";
                }

                String body = req.body();
                if (body == null || body.isBlank()) {
                    res.status(400);
                    return "{\"error\":\"CSV vide\"}";
                }

                AbonnementRepository repo = getOrCreateRepo(req);
                List<Abonnement> existing = repo.findAll();

                String[] lignes = body.split("\\R");
                int imported = 0;
                List<String> errors = new ArrayList<>();

                for (int i = 1; i < lignes.length; i++) {
                    String line = lignes[i].trim();
                    if (line.isEmpty()) continue;

                    String[] p = line.split(";");
                    if (p.length < 8) {
                        errors.add("Ligne " + i + " invalide (colonnes manquantes)");
                        continue;
                    }

                    try {
                        Abonnement a = new Abonnement(
                            p[1],                                 // nomService
                            LocalDate.parse(p[2]),                // dateDebut
                            LocalDate.parse(p[3]),                // dateFin
                            Double.parseDouble(p[4]),             // prixMensuel
                            p[5],                                 // clientName
                            LocalDate.parse(p[6]),                // derniereUtilisation
                            p[7]                                  // categorie
                        );

                        if (p[0] == null || p[0].isBlank()) {
                            a.setId(java.util.UUID.randomUUID().toString());
                        } else {
                            a.setId(p[0]);
                        }

                        existing.add(a);
                        imported++;

                    } catch (Exception e) {
                        errors.add("Erreur ligne " + i + " : " + e.getMessage());
                    }
                }

                repo.saveAll(existing);

                res.status(201);
                res.type("application/json");
                return mapper.writeValueAsString(
                    Map.of("imported", imported, "errors", errors)
                );
            });

            // =================================================
            // 🔵  USER - VÉRIFIER SESSION
            // =================================================
            get("/api/user/current", (req, res) -> {
                String email = req.session().attribute("user_email");
                if (email == null) {
                    res.status(401);
                    return "{\"error\":\"Non connecté\"}";
                }
                
                res.type("application/json");
                return "{\"email\":\"" + email + "\", \"connected\": true}";
            });

            // =================================================
            // 🔵  ANALYTICS - RAPPORT D'OPTIMISATION
            // =================================================
            get("/analytics/optimize", (req, res) -> {
                String email = req.session().attribute("user_email");
                if (email == null) {
                    res.status(401);
                    return "{\"error\":\"Vous devez être connecté\"}";
                }
                
                res.type("application/json");
                AbonnementRepository repo = getOrCreateRepo(req);
                List<Abonnement> abonnements = repo.findAll();
                
                SubscriptionOptimizer optimizer = new SubscriptionOptimizer();
                var report = optimizer.generateOptimizationReport(abonnements);
                
                return mapper.writeValueAsString(report);
            });

            // =================================================
            // 🔵  ANALYTICS - PRÉVISION TRÉSORERIE
            // =================================================
            get("/analytics/forecast", (req, res) -> {
                String email = req.session().attribute("user_email");
                if (email == null) {
                    res.status(401);
                    return "{\"error\":\"Vous devez être connecté\"}";
                }
                
                res.type("application/json");
                AbonnementRepository repo = getOrCreateRepo(req);
                List<Abonnement> abonnements = repo.findAll();
                
                SubscriptionAnalytics analytics = new SubscriptionAnalytics();
                var forecast = analytics.forecastCashflow(abonnements, 6);
                
                return mapper.writeValueAsString(forecast);
            });

            // =================================================
            // 🔵  ANALYTICS - MÉTRIQUES AVANCÉES
            // =================================================
            get("/analytics/metrics", (req, res) -> {
                String email = req.session().attribute("user_email");
                if (email == null) {
                    res.status(401);
                    return "{\"error\":\"Vous devez être connecté\"}";
                }
                
                res.type("application/json");
                AbonnementRepository repo = getOrCreateRepo(req);
                List<Abonnement> abonnements = repo.findAll();
                
                SubscriptionAnalytics analytics = new SubscriptionAnalytics();
                var metrics = analytics.calculateAdvancedMetrics(abonnements);
                
                return mapper.writeValueAsString(metrics);
            });

            // =================================================
            // 🔵  ANALYTICS - DÉTECTION ANOMALIES
            // =================================================
            get("/analytics/anomalies", (req, res) -> {
                String user = req.session().attribute("user_email");
                if (user == null) {
                    res.status(401);
                    return "{\"error\":\"Vous devez être connecté\"}";
                }
                
                res.type("application/json");
                AbonnementRepository repo = getOrCreateRepo(req);
                List<Abonnement> abonnements = repo.findAll();
                
                SubscriptionAnalytics analytics = new SubscriptionAnalytics();
                List<Map<String, Object>> anomalies = new ArrayList<>();
                
                for (Abonnement abo : abonnements) {
                    boolean isAnomaly = analytics.detectPriceAnomaly(abonnements, abo);
                    if (isAnomaly) {
                        anomalies.add(Map.of(
                            "id", abo.getId(),
                            "nomService", abo.getNomService(),
                            "prix", abo.getPrixMensuel(),
                            "message", "Prix anormalement élevé"
                        ));
                    }
                }
                
                return mapper.writeValueAsString(anomalies);
            });

            // =================================================
            // 🔵  ANALYTICS - DOUBLONS
            // =================================================
            get("/analytics/duplicates", (req, res) -> {
                String user = req.session().attribute("user_email");
                if (user == null) {
                    res.status(401);
                    return "{\"error\":\"Vous devez être connecté\"}";
                }
                
                res.type("application/json");
                AbonnementRepository repo = getOrCreateRepo(req);
                List<Abonnement> abonnements = repo.findAll();
                
                SubscriptionAnalytics analytics = new SubscriptionAnalytics();
                var duplicates = analytics.detectDuplicates(abonnements);
                
                return mapper.writeValueAsString(duplicates);
            });

            // =================================================
            // 🔵  ANALYTICS - RAPPORT MENSUEL
            // =================================================
            get("/analytics/monthly-report", (req, res) -> {
                String user = req.session().attribute("user_email");
                if (user == null) {
                    res.status(401);
                    return "{\"error\":\"Vous devez être connecté\"}";
                }
                
                res.type("application/json");
                AbonnementRepository repo = getOrCreateRepo(req);
                List<Abonnement> abonnements = repo.findAll();
                
                SubscriptionAnalytics analytics = new SubscriptionAnalytics();
                var report = analytics.generateMonthlyReport(abonnements);
                
                return mapper.writeValueAsString(report);
            });

            // Endpoint: Clustering des abonnements
            get("/analytics/clusters", (req, res) -> {
                String email = req.session().attribute("user_email");
                if (email == null) {
                    res.status(401);
                    return "{\"error\":\"Vous devez être connecté\"}";
                }
                
                res.type("application/json");
                AbonnementRepository repo = getOrCreateRepo(req);
                List<Abonnement> abonnements = repo.findAll();
                
                var clusters = SubscriptionAnalytics.clusterSubscriptions(abonnements);
                
                return mapper.writeValueAsString(clusters);
            });

            // Endpoint: Prédiction des dépenses
            get("/analytics/predict-spending", (req, res) -> {
                String email = req.session().attribute("user_email");
                if (email == null) {
                    res.status(401);
                    return "{\"error\":\"Vous devez être connecté\"}";
                }
                
                res.type("application/json");
                AbonnementRepository repo = getOrCreateRepo(req);
                List<Abonnement> abonnements = repo.findAll();
                
                var predictions = SubscriptionAnalytics.predictSpendingTrend(abonnements);
                
                return mapper.writeValueAsString(predictions);
            });

            // Endpoint: Détection patterns saisonniers
            get("/analytics/seasonal-patterns", (req, res) -> {
                String email = req.session().attribute("user_email");
                if (email == null) {
                    res.status(401);
                    return "{\"error\":\"Vous devez être connecté\"}";
                }
                
                res.type("application/json");
                AbonnementRepository repo = getOrCreateRepo(req);
                List<Abonnement> abonnements = repo.findAll();
                
                var patterns = SubscriptionAnalytics.detectSeasonalPatterns(abonnements);
                
                return mapper.writeValueAsString(patterns);
            });

            // Endpoint: Score santé du portefeuille
            get("/analytics/portfolio-health", (req, res) -> {
                String email = req.session().attribute("user_email");
                if (email == null) {
                    res.status(401);
                    return "{\"error\":\"Vous devez être connecté\"}";
                }
                
                res.type("application/json");
                AbonnementRepository repo = getOrCreateRepo(req);
                List<Abonnement> abonnements = repo.findAll();
                
                int healthScore = SubscriptionAnalytics.calculatePortfolioHealthScore(abonnements);
                
                return "{\"healthScore\": " + healthScore + "}";
            });

            // =================================================
            // 🔵  STATUS SESSION (pour navbar)
            // =================================================
            get("/session", (req, res) -> {
                res.type("application/json");
                String email = req.session().attribute("user_email");
                
                if (email == null) {
                    return "{\"authenticated\":false}";
                }
                
                FileUserRepository userRepo = new FileUserRepository();
                User user = userRepo.findByEmail(email);
                
                if (user == null) {
                    return "{\"authenticated\":false}";
                }
                
                return String.format("{\"authenticated\":true,\"email\":\"%s\",\"pseudo\":\"%s\"}", 
                    email, user.getPseudo());
            });

            // =================================================
            // 🔵  LOGOUT (pour navbar)
            // =================================================
            post("/logout", (req, res) -> {
                req.session().invalidate();
                return "Déconnecté.";
            });


            // =================================================
            //     🔵  INSCRIPTION UTILISATEUR
            // =================================================
            post("/register", (req, res) -> {
                res.type("text/plain");

                String email = req.queryParams("email");
                String password = req.queryParams("password");
                String pseudo = req.queryParams("pseudo");
                if (pseudo == null || pseudo.trim().isEmpty()) {
                    pseudo = email.split("@")[0];
                }

                UserService service = new UserServiceImpl();
                String token = service.register(email, password, pseudo);

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

                // 🔵 Redirection vers index.html
                res.redirect("/index.html");
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

                req.session(true).attribute("user_email", email);

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
                res.type("application/json");
                String email = req.session().attribute("user_email");
                
                if (email == null) {
                    return "{\"authenticated\":false}";
                }
                
                FileUserRepository userRepo = new FileUserRepository();
                User user = userRepo.findByEmail(email);
                
                if (user == null) {
                    return "{\"authenticated\":false}";
                }
                
                return String.format("{\"authenticated\":true,\"email\":\"%s\",\"pseudo\":\"%s\"}", 
                    email, user.getPseudo());
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

            // =================================================
            //     📧  NOTIFICATIONS EMAIL
            // =================================================
            path("/notifications", () -> {
                
                ObjectMapper emailMapper = new ObjectMapper();
                
                // Sauvegarder les paramètres email
                post("/settings", (req, res) -> {
                    res.type("application/json");
                    
                    String body = req.body();
                    Map<String, Object> settings = emailMapper.readValue(body, Map.class);
                    
                    // Sauvegarder dans la session ou base de données
                    String userEmail = req.session().attribute("user_email");
                    if (userEmail != null) {
                        // TODO: Sauvegarder dans la base de données
                        System.out.println("Paramètres email sauvegardés pour: " + userEmail);
                    }
                    
                    return "{\"success\":true,\"message\":\"Paramètres sauvegardés\"}";
                });
                
                // Sauvegarder les préférences de notifications
                post("/preferences", (req, res) -> {
                    res.type("application/json");
                    
                    String body = req.body();
                    Map<String, Object> prefs = emailMapper.readValue(body, Map.class);
                    
                    String userEmail = req.session().attribute("user_email");
                    if (userEmail != null) {
                        System.out.println("Préférences de notifications sauvegardées pour: " + userEmail);
                    }
                    
                    return "{\"success\":true,\"message\":\"Préférences sauvegardées\"}";
                });
                
                // Envoyer un email de test
                post("/test", (req, res) -> {
                    res.type("application/json");
                    
                    String body = req.body();
                    Map<String, Object> data = emailMapper.readValue(body, Map.class);
                    
                    String toEmail = (String) data.get("to");
                    
                    if (toEmail == null || toEmail.isEmpty()) {
                        res.status(400);
                        return "{\"success\":false,\"message\":\"Email manquant\"}";
                    }
                    
                    // Envoyer l'email de test via EmailService
                    EmailService emailService = EmailService.getInstance();
                    
                    // En mode développement, on simule l'envoi
                    boolean sent = emailService.sendTestEmail(toEmail);
                    
                    if (sent) {
                        return "{\"success\":true,\"message\":\"Email de test envoyé\"}";
                    } else {
                        // En mode simulation
                        System.out.println("EMAIL TEST simulé vers: " + toEmail);
                        return "{\"success\":true,\"message\":\"Email simulé (backend non configuré)\"}";
                    }
                });
                
                // Envoyer une notification générique
                post("/send", (req, res) -> {
                    res.type("application/json");
                    
                    String body = req.body();
                    Map<String, Object> emailData = emailMapper.readValue(body, Map.class);
                    
                    String toEmail = (String) emailData.get("to");
                    String type = (String) emailData.get("type");
                    Map<String, Object> data = (Map<String, Object>) emailData.get("data");
                    
                    EmailService emailService = EmailService.getInstance();
                    boolean sent = false;
                    
                    switch (type) {
                        case "expiration":
                            sent = emailService.sendExpirationAlert(
                                toEmail,
                                (String) data.get("subscriptionName"),
                                ((Number) data.get("price")).doubleValue(),
                                (String) data.get("expirationDate"),
                                ((Number) data.get("daysRemaining")).intValue()
                            );
                            break;
                            
                        case "budget":
                            sent = emailService.sendBudgetAlert(
                                toEmail,
                                ((Number) data.get("budget")).doubleValue(),
                                ((Number) data.get("spent")).doubleValue(),
                                ((Number) data.get("overspend")).doubleValue()
                            );
                            break;
                            
                        case "monthly":
                            sent = emailService.sendMonthlyReport(
                                toEmail,
                                (String) data.get("month"),
                                ((Number) data.get("totalSpent")).doubleValue(),
                                ((Number) data.get("totalSubs")).intValue(),
                                ((Number) data.get("subsCost")).doubleValue(),
                                ((Number) data.get("transactionCount")).intValue()
                            );
                            break;
                    }
                    
                    if (sent) {
                        return "{\"success\":true,\"message\":\"Notification envoyée\"}";
                    } else {
                        System.out.println("NOTIFICATION simulée - Type: " + type);
                        return "{\"success\":true,\"message\":\"Notification simulée\"}";
                    }
                });
                
            });

        }); // end /api


        System.out.println("API server démarré sur http://localhost:" + httpPort);
    }

        private static String safeCsv(String s) {
    if (s == null) return "";
    return s.replace(";", ",");
}

}
