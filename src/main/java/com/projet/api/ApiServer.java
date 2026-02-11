package com.projet.api;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.projet.backend.domain.Abonnement;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.projet.repository.AbonnementRepository;
import com.projet.repository.FileAbonnementRepository;
import com.projet.user.FileUserRepository;
import com.projet.backend.domain.User;
import com.projet.user.UserService;
import com.projet.user.UserServiceImpl;
import com.projet.analytics.SubscriptionAnalytics;
import com.projet.backend.service.SubscriptionService;
import com.projet.service.SubscriptionOptimizer;
import com.projet.service.ServiceMailgun;
import com.projet.service.ServiceTauxChange;

import spark.Request;
import spark.Response;
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
            get("/user/current", (req, res) -> {
                String email = req.session().attribute("user_email");
                if (email == null) {
                    res.status(401);
                    return writeJson(res, mapper, "/api/user/current", Map.of("error", "Non connecté"));
                }

                Map<String, Object> payload = Map.of(
                    "email", email,
                    "connected", Boolean.TRUE
                );

                return writeJson(res, mapper, "/api/user/current", payload);
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
                
                AbonnementRepository repo = getOrCreateRepo(req);
                List<Abonnement> abonnements = repo.findAll();
                
                var report = SubscriptionOptimizer.generateOptimizationReport(abonnements);
                
                return writeJson(res, mapper, "/api/analytics/optimize", report);
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
                
                AbonnementRepository repo = getOrCreateRepo(req);
                List<Abonnement> abonnements = repo.findAll();
                
                var forecast = SubscriptionAnalytics.forecastCashflow(abonnements, 6);
                
                return writeJson(res, mapper, "/api/analytics/forecast", forecast);
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
                
                AbonnementRepository repo = getOrCreateRepo(req);
                List<Abonnement> abonnements = repo.findAll();
                
                var metrics = SubscriptionAnalytics.calculateAdvancedMetrics(abonnements);
                
                return writeJson(res, mapper, "/api/analytics/metrics", metrics);
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
                
                AbonnementRepository repo = getOrCreateRepo(req);
                List<Abonnement> abonnements = repo.findAll();
                
                List<Map<String, Object>> anomalies = new ArrayList<>();
                
                for (Abonnement abo : abonnements) {
                    boolean isAnomaly = SubscriptionAnalytics.detectPriceAnomaly(abonnements, abo);
                    if (isAnomaly) {
                        anomalies.add(Map.of(
                            "id", abo.getId(),
                            "nomService", abo.getNomService(),
                            "prix", abo.getPrixMensuel(),
                            "message", "Prix anormalement élevé"
                        ));
                    }
                }
                
                return writeJson(res, mapper, "/api/analytics/anomalies", anomalies);
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
                
                AbonnementRepository repo = getOrCreateRepo(req);
                List<Abonnement> abonnements = repo.findAll();
                
                var duplicates = SubscriptionAnalytics.detectDuplicates(abonnements);
                
                return writeJson(res, mapper, "/api/analytics/duplicates", duplicates);
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
                
                AbonnementRepository repo = getOrCreateRepo(req);
                List<Abonnement> abonnements = repo.findAll();
                
                var report = SubscriptionAnalytics.generateMonthlyReport(abonnements);
                
                return writeJson(res, mapper, "/api/analytics/monthly-report", report);
            });

            // =================================================
            // 🔵  ANALYTICS - RECOMMANDATIONS INTELLIGENTES
            // =================================================
            get("/recommendations", (req, res) -> {
                String email = req.session().attribute("user_email");
                if (email == null) {
                    res.status(401);
                    return "{\"error\":\"Vous devez être connecté\"}";
                }

                AbonnementRepository repo = getOrCreateRepo(req);
                List<Abonnement> abonnements = repo.findAll();

                SubscriptionService service = new SubscriptionService();

                List<Abonnement> highRisk = service.getHighChurnRiskSubscriptions(abonnements);

                List<Abonnement> savings = service.identifySavingOpportunities(abonnements);
                double totalSavings = savings.stream()
                    .mapToDouble(Abonnement::getPrixMensuel)
                    .sum();

                List<Abonnement> expiring = service.getExpiringSubscriptions(abonnements, 30).stream()
                    .sorted(Comparator.comparing(Abonnement::getDateFin))
                    .limit(5)
                    .collect(Collectors.toList());

                List<Abonnement> lowValue = new ArrayList<>();
                List<Abonnement> sortedByValue = service.sortByValueScore(abonnements);
                if (!sortedByValue.isEmpty()) {
                    int start = Math.max(0, sortedByValue.size() - 5);
                    lowValue = new ArrayList<>(sortedByValue.subList(start, sortedByValue.size()));
                    lowValue.sort(Comparator.comparingDouble(Abonnement::getValueScore));
                }

                Map<String, Object> savingsBlock = new HashMap<>();
                savingsBlock.put("totalMonthly", Math.round(totalSavings * 100.0) / 100.0);
                savingsBlock.put("candidates", savings);

                Map<String, Object> payload = new HashMap<>();
                payload.put("highRisk", highRisk);
                payload.put("lowValue", lowValue);
                payload.put("savings", savingsBlock);
                payload.put("upcomingExpirations", expiring);

                return writeJson(res, mapper, "/api/recommendations", payload);
            });

            // Endpoint: Clustering des abonnements
            get("/analytics/clusters", (req, res) -> {
                String email = req.session().attribute("user_email");
                if (email == null) {
                    res.status(401);
                    return "{\"error\":\"Vous devez être connecté\"}";
                }
                
                AbonnementRepository repo = getOrCreateRepo(req);
                List<Abonnement> abonnements = repo.findAll();
                
                var clusters = SubscriptionAnalytics.clusterSubscriptions(abonnements);
                
                return writeJson(res, mapper, "/api/analytics/clusters", clusters);
            });

            // Endpoint: Prédiction des dépenses
            get("/analytics/predict-spending", (req, res) -> {
                String email = req.session().attribute("user_email");
                if (email == null) {
                    res.status(401);
                    return "{\"error\":\"Vous devez être connecté\"}";
                }
                
                AbonnementRepository repo = getOrCreateRepo(req);
                List<Abonnement> abonnements = repo.findAll();
                
                var predictions = SubscriptionAnalytics.predictSpendingTrend(abonnements);
                
                return writeJson(res, mapper, "/api/analytics/predict-spending", predictions);
            });

            get("/analytics/budget-plan", (req, res) -> {
                String email = req.session().attribute("user_email");
                if (email == null) {
                    res.status(401);
                    return "{\"error\":\"Vous devez être connecté\"}";
                }

                String targetParam = req.queryParams("target");
                if (targetParam == null || targetParam.isBlank()) {
                    res.status(400);
                    return writeJson(res, mapper, "/api/analytics/budget-plan", Map.of("error", "Paramètre 'target' requis"));
                }

                double target;
                try {
                    target = Double.parseDouble(targetParam);
                } catch (NumberFormatException e) {
                    res.status(400);
                    return writeJson(res, mapper, "/api/analytics/budget-plan", Map.of("error", "Paramètre 'target' invalide"));
                }

                if (target < 0) {
                    res.status(400);
                    return writeJson(res, mapper, "/api/analytics/budget-plan", Map.of("error", "Le budget cible doit être positif"));
                }

                AbonnementRepository repo = getOrCreateRepo(req);
                List<Abonnement> abonnements = repo.findAll();
                var plan = SubscriptionAnalytics.planBudgetReduction(abonnements, target);

                Map<String, Object> debug = new HashMap<>();
                debug.put("currentMonthlyCost", plan.getCurrentMonthlyCost());
                debug.put("targetMonthlyBudget", plan.getTargetMonthlyBudget());
                debug.put("requiredSavings", plan.getRequiredSavings());
                debug.put("achievedSavings", plan.getAchievedSavings());
                debug.put("targetFeasible", plan.isTargetFeasible());
                debug.put("recommendedCancellations", plan.getRecommendedCancellations().stream()
                    .map(Abonnement::getNomService)
                    .collect(Collectors.toList()));
                debug.put("optionalCandidates", plan.getOptionalCandidates().stream()
                    .map(Abonnement::getNomService)
                    .collect(Collectors.toList()));
                logJsonResponse("/api/analytics/budget-plan:debug", debug, null);

                return writeJson(res, mapper, "/api/analytics/budget-plan", plan);
            });

            // Endpoint: Détection patterns saisonniers
            get("/analytics/seasonal-patterns", (req, res) -> {
                String email = req.session().attribute("user_email");
                if (email == null) {
                    res.status(401);
                    return "{\"error\":\"Vous devez être connecté\"}";
                }
                
                AbonnementRepository repo = getOrCreateRepo(req);
                List<Abonnement> abonnements = repo.findAll();
                
                var patterns = SubscriptionAnalytics.detectSeasonalPatterns(abonnements);
                
                return writeJson(res, mapper, "/api/analytics/seasonal-patterns", patterns);
            });

            // Endpoint: Score santé du portefeuille
            get("/analytics/portfolio-health", (req, res) -> {
                String email = req.session().attribute("user_email");
                if (email == null) {
                    res.status(401);
                    return "{\"error\":\"Vous devez être connecté\"}";
                }
                
                AbonnementRepository repo = getOrCreateRepo(req);
                List<Abonnement> abonnements = repo.findAll();
                
                int healthScore = (int) SubscriptionAnalytics.calculatePortfolioHealthScore(abonnements);
                
                Map<String, Object> payload = Map.of("healthScore", healthScore);
                return writeJson(res, mapper, "/api/analytics/portfolio-health", payload);
            });

            // =================================================
            // 🔵  STATUS SESSION (pour navbar)
            // =================================================
            get("/session", (req, res) -> {
                String email = req.session().attribute("user_email");
                
                if (email == null) {
                    Map<String, Object> payload = Map.of("authenticated", Boolean.FALSE);
                    return writeJson(res, mapper, "/api/session", payload);
                }
                
                FileUserRepository userRepo = new FileUserRepository();
                User user = userRepo.findByEmail(email);
                
                if (user == null) {
                    Map<String, Object> payload = Map.of("authenticated", Boolean.FALSE);
                    return writeJson(res, mapper, "/api/session", payload);
                }
                
                Map<String, Object> payload = new HashMap<>();
                payload.put("authenticated", Boolean.TRUE);
                payload.put("email", email);
                payload.put("pseudo", user.getPseudo());
                return writeJson(res, mapper, "/api/session", payload);
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

        // ===== SERVICES DISTANTS UNIQUEMENT =====

        path("/api/email", () -> {
            // Envoyer alerte expiration via Mailgun (API distante)
            post("/send-alert-expiration", (req, res) -> {
                String email = req.session().attribute("user_email");
                if (email == null) {
                    res.status(401);
                    return "{\"error\":\"Authentification requise\"}";
                }

                res.type("application/json");
                
                String nomService = req.queryParams("service");
                String prix = req.queryParams("prix");
                String dateExp = req.queryParams("dateExpiration");

                ServiceMailgun.ResultatEnvoiEmail resultat = ServiceMailgun.envoyerAlerteExpiration(
                    email, nomService, Double.parseDouble(prix), dateExp
                );

                Map<String, Object> response = new java.util.HashMap<>();
                response.put("success", resultat.success);
                response.put("messageId", resultat.messageId);
                response.put("tempsReponse", resultat.tempsReponse + "ms");
                if (resultat.erreur != null) {
                    response.put("erreur", resultat.erreur);
                    res.status(400);
                }

                return mapper.writeValueAsString(response);
            });

            // Envoyer rapport mensuel via Mailgun (API distante)
            post("/send-rapport-mensuel", (req, res) -> {
                String email = req.session().attribute("user_email");
                if (email == null) {
                    res.status(401);
                    return "{\"error\":\"Authentification requise\"}";
                }

                res.type("application/json");
                
                String mois = req.queryParams("mois");
                double coutTotal = Double.parseDouble(req.queryParams("coutTotal"));
                int nombreAbos = Integer.parseInt(req.queryParams("nombreAbos"));

                ServiceMailgun.ResultatEnvoiEmail resultat = ServiceMailgun.envoyerRapportMensuel(
                    email, mois, coutTotal, nombreAbos
                );

                Map<String, Object> response = new java.util.HashMap<>();
                response.put("success", resultat.success);
                response.put("messageId", resultat.messageId);
                response.put("tempsReponse", resultat.tempsReponse + "ms");
                if (resultat.erreur != null) {
                    response.put("erreur", resultat.erreur);
                    res.status(400);
                }

                return mapper.writeValueAsString(response);
            });

            // Alerte budget dépassé via Mailgun (API distante)
            post("/send-alerte-budget", (req, res) -> {
                String email = req.session().attribute("user_email");
                if (email == null) {
                    res.status(401);
                    return "{\"error\":\"Authentification requise\"}";
                }

                res.type("application/json");
                
                double budget = Double.parseDouble(req.queryParams("budget"));
                double depense = Double.parseDouble(req.queryParams("depense"));
                double depassement = depense - budget;

                ServiceMailgun.ResultatEnvoiEmail resultat = ServiceMailgun.envoyerAlerteDepassementBudget(
                    email, budget, depense, depassement
                );

                Map<String, Object> response = new java.util.HashMap<>();
                response.put("success", resultat.success);
                response.put("messageId", resultat.messageId);
                response.put("tempsReponse", resultat.tempsReponse + "ms");
                if (resultat.erreur != null) {
                    response.put("erreur", resultat.erreur);
                    res.status(400);
                }

                return mapper.writeValueAsString(response);
            });

            // Status Mailgun (API distante)
            get("/status", (req, res) -> {
                res.type("application/json");
                return mapper.writeValueAsString(ServiceMailgun.obtenirInfos());
            });
        });

        path("/api/currency", () -> {
            // Convertir montant entre devises via ExchangeRate API (API distante)
            post("/convert", (req, res) -> {
                res.type("application/json");
                
                double montant = Double.parseDouble(req.queryParams("montant"));
                String deviseSource = req.queryParams("source");
                String deviseCible = req.queryParams("cible");

                ServiceTauxChange.ResultatConversion resultat = ServiceTauxChange.convertir(
                    montant, deviseSource, deviseCible
                );

                return mapper.writeValueAsString(resultat);
            });

            // Convertir en EUR (cas principal)
            post("/to-eur", (req, res) -> {
                res.type("application/json");
                
                double montant = Double.parseDouble(req.queryParams("montant"));
                String devise = req.queryParams("devise");

                ServiceTauxChange.ResultatConversion resultat = ServiceTauxChange.convertirEnEuro(
                    montant, devise
                );

                return mapper.writeValueAsString(resultat);
            });

            // Analyser stabilité des devises (API distante avec test)
            post("/stabilite", (req, res) -> {
                res.type("application/json");
                
                String devise = req.queryParams("devise");
                Map<String, Object> resultat = ServiceTauxChange.analyserStabilite(devise);

                return mapper.writeValueAsString(resultat);
            });

            // Status ExchangeRate API (API distante)
            get("/status", (req, res) -> {
                res.type("application/json");
                return mapper.writeValueAsString(ServiceTauxChange.obtenirInfos());
            });
        });

        }); // end /api


        System.out.println("API server démarré sur http://localhost:" + httpPort);
    }

    private static String writeJson(Response res, ObjectMapper mapper, String route, Object payload) {
        res.type("application/json");
        try {
            String json = mapper.writeValueAsString(payload);
            logJsonResponse(route, payload, json);
            return json;
        } catch (JsonProcessingException e) {
            logJsonSerializationError(route, e);
            res.status(500);
            String fallback = "{\"error\":\"JSON serialization failure\"}";
            logRawJson(route, fallback);
            return fallback;
        }
    }

    private static void logJsonResponse(String route, Object payload, String rawJson) {
        try {
            String summary;
            if (payload == null) {
                summary = "null";
            } else if (payload instanceof String s) {
                summary = "String(len=" + s.length() + ")";
            } else if (payload instanceof java.util.Collection<?> collection) {
                summary = payload.getClass().getSimpleName() + "(size=" + collection.size() + ")";
            } else if (payload instanceof java.util.Map<?, ?> map) {
                summary = payload.getClass().getSimpleName() + "(keys=" + map.keySet() + ")";
            } else {
                summary = payload.getClass().getSimpleName();
            }
            System.out.println("[ApiServer] " + route + " payload=" + summary + " raw=" + truncateRawJson(rawJson));
        } catch (Exception e) {
            System.out.println("[ApiServer] " + route + " payload=<log-error:" + e.getMessage() + ">");
        }
    }

    private static void logJsonSerializationError(String route, Exception e) {
        System.out.println("[ApiServer] " + route + " <serialization-error> " + e.getMessage());
    }

    private static void logRawJson(String route, String rawJson) {
        System.out.println("[ApiServer] " + route + " raw=" + truncateRawJson(rawJson));
    }

    private static String truncateRawJson(String rawJson) {
        if (rawJson == null) {
            return "null";
        }
        return rawJson.length() > 200 ? rawJson.substring(0, 200) + "..." : rawJson;
    }

        private static String safeCsv(String s) {
    if (s == null) return "";
    return s.replace(";", ",");
}

}
