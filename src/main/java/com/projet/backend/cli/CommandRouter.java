package com.projet.backend.cli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.projet.backend.adapter.AbonnementCsvConverter;
import com.projet.backend.domain.Abonnement;
import com.projet.backend.domain.User;
import com.projet.backend.service.SubscriptionService;
import com.projet.backend.service.UserService;

/**
 * CLI router for backend commands.
 *
 * This class is the thin CLI adapter layer: it parses arguments and delegates
 * to the stable backend services (domain/service). Business logic lives in
 * the service layer and must not be modified here. The router is kept small
 * and testable by allowing service injection via constructor.
 *
 * Responsibilities:
 * - parse CLI key=value arguments
 * - dispatch to backend services (injection-friendly)
 * - provide helpful user messages and validation feedback
 *
 * Usage: see tests and any `Main` that delegates to this router.
 */
public class CommandRouter {

    private final SubscriptionService subscriptionService;
    private final UserService userService;

    /**
     * Constructor with injected services for testability.
     */
    public CommandRouter(SubscriptionService subscriptionService, UserService userService) {
        this.subscriptionService = subscriptionService;
        this.userService = userService;
    }

    /**
     * Factory creating a router with default service implementations.
     */
    public static CommandRouter createDefault() {
        return new CommandRouter(new SubscriptionService(), new UserService());
    }

    /**
     * Explicit named factory kept for clarity - delegates to the backward-compatible
     * `createDefault()` so existing code keeps working while new callers can use
     * a clearer name.
     */
    public static CommandRouter createWithDefaultServices() {
        return createDefault();
    }

    /**
     * Route a CLI command (args like args[]). First token is command name.
     */
    public String route(String[] args) {
        if (args == null || args.length == 0) {
            return "No command provided. Use 'help' to see available commands.";
        }

        String cmd = args[0];
        Map<String, String> params = parseKeyValueArgs(args);

        switch (cmd) {
            case "addSubscription":
                return handleAddSubscription(params);
            case "createUser":
                return handleCreateUser(params);
            case "roiScore":
                return handleRoiScore(params);
            case "statsFromCsv":
                return handleStatsFromCsv(params);
            case "expiring":
                return handleExpiringFromCsv(params);
            case "filterByCategory":
                return handleFilterByCategory(params);
            case "topPriority":
                return handleTopPriority(params);
            case "savingOps":
                return handleSavingOps(params);
            case "help":
            case "--help":
            case "-h":
                return helpText();
            default:
                return "Unknown command: '" + cmd + "'. Use 'help' to list available commands and examples.";
        }
    }

    private Map<String, String> parseKeyValueArgs(String[] args) {
        Map<String, String> map = new HashMap<>();
        for (int i = 1; i < args.length; i++) {
            String token = args[i];
            int eq = token.indexOf('=');
            if (eq > 0 && eq < token.length() - 1) {
                String k = token.substring(0, eq);
                String v = token.substring(eq + 1);
                map.put(k, v);
            }
        }
        return map;
    }

    private String handleAddSubscription(Map<String, String> p) {
        String nomService = p.get("nomService");
        String clientName = p.get("user");
        String prixS = p.get("prixMensuel");
        String dateDebutS = p.get("dateDebut");
        String dateFinS = p.get("dateFin");
        String categorie = p.getOrDefault("categorie", "general");

        if (nomService == null || clientName == null || prixS == null || dateDebutS == null || dateFinS == null) {
            return "Missing parameters. Required: nomService user prixMensuel dateDebut dateFin";
        }

        double prix;
        LocalDate dateDebut;
        LocalDate dateFin;
        try {
            prix = Double.parseDouble(prixS);
            dateDebut = LocalDate.parse(dateDebutS);
            dateFin = LocalDate.parse(dateFinS);
        } catch (NumberFormatException | DateTimeParseException e) {
            return "Invalid number or date format: " + e.getMessage();
        }

        // Validate using the service's ValidationResult before invoking creation
        Abonnement candidate = new Abonnement(nomService, dateDebut, dateFin, prix, clientName);
        SubscriptionService.ValidationResult vr = subscriptionService.validateSubscription(candidate);
        if (!vr.valid) {
            String msg = vr.errors.stream().collect(Collectors.joining("; "));
            return "Validation failed: " + msg;
        }

        // At this point validation passed; create subscription (service may still throw)
        Abonnement created = subscriptionService.createSubscription(nomService, dateDebut, dateFin, prix, clientName, categorie);
        return "Subscription created: service=" + created.getNomService() + " user=" + created.getClientName() + " price=" + created.getPrixMensuel();
    }

    private String handleCreateUser(Map<String, String> p) {
        String email = p.get("email");
        String password = p.get("password");
        String pseudo = p.get("pseudo");

        if (email == null || password == null || pseudo == null) {
            return "Missing parameters. Required: email password pseudo";
        }

        User u = new User(email, password, pseudo, null);
        UserService.ValidationResult vr = userService.validateUser(u);
        if (!vr.valid) {
            String msg = String.join("; ", vr.errors);
            return "Validation failed: " + msg;
        }

        var created = userService.createUser(email, password, pseudo);
        return "User created: " + created.getEmail() + " (pseudo=" + created.getPseudo() + ")";
    }

    /* -------- Additional commands mapping to SubscriptionService (non-invasive) -------- */

    private String handleRoiScore(Map<String, String> p) {
        String nomService = p.get("nomService");
        String clientName = p.get("user");
        String prixS = p.get("prixMensuel");
        String dateDebutS = p.get("dateDebut");
        String dateFinS = p.get("dateFin");

        if (nomService == null || clientName == null || prixS == null || dateDebutS == null || dateFinS == null) {
            return "Missing parameters for roiScore. Required: nomService user prixMensuel dateDebut dateFin";
        }

        try {
            double prix = Double.parseDouble(prixS);
            LocalDate dd = LocalDate.parse(dateDebutS);
            LocalDate df = LocalDate.parse(dateFinS);
            Abonnement a = new Abonnement(nomService, dd, df, prix, clientName);
            double score = subscriptionService.calculateRoiScore(a);
            return String.format("ROI score for %s (user=%s): %.2f", nomService, clientName, score);
        } catch (NumberFormatException | DateTimeParseException e) {
            return "Invalid numeric/date parameter: " + e.getMessage();
        }
    }

    private List<Abonnement> readAbonnementsFromCsvPath(String path) throws IOException {
        List<String> lines = Files.readAllLines(Path.of(path));
        List<Abonnement> list = new ArrayList<>();
        for (String l : lines) {
            if (l == null || l.trim().isEmpty()) continue;
            list.add(AbonnementCsvConverter.fromCsvString(l));
        }
        return list;
    }

    private String handleStatsFromCsv(Map<String, String> p) {
        String file = p.get("file");
        if (file == null) return "Missing 'file' parameter pointing to CSV of abonnements.";
        try {
            List<Abonnement> abonnements = readAbonnementsFromCsvPath(file);
            SubscriptionService.PortfolioStats stats = subscriptionService.calculatePortfolioStats(abonnements);
            return String.format("Portfolio: total=%d active=%d inactive=%d totalMonthly=%.2f avgMonthly=%.2f health=%.2f",
                stats.totalSubscriptions, stats.activeSubscriptions, stats.inactiveSubscriptions, stats.totalMonthlyCost, stats.averageMonthlyCost, stats.portfolioHealthScore);
        } catch (IOException e) {
            return "Cannot read file '" + file + "': " + e.getMessage();
        }
    }

    private String handleExpiringFromCsv(Map<String, String> p) {
        String file = p.get("file");
        String joursS = p.getOrDefault("jours", "30");
        if (file == null) return "Missing 'file' parameter pointing to CSV of abonnements.";
        try {
            int jours = Integer.parseInt(joursS);
            List<Abonnement> abonnements = readAbonnementsFromCsvPath(file);
            List<Abonnement> exp = subscriptionService.getExpiringSubscriptions(abonnements, jours);
            return "Expiring subscriptions (next " + jours + " days): " + exp.stream().map(Abonnement::getNomService).collect(Collectors.joining(", "));
        } catch (NumberFormatException e) {
            return "Invalid 'jours' parameter: " + e.getMessage();
        } catch (IOException e) {
            return "Cannot read file '" + file + "': " + e.getMessage();
        }
    }

    private String handleFilterByCategory(Map<String, String> p) {
        String file = p.get("file");
        String categorie = p.get("categorie");
        if (file == null || categorie == null) return "Missing 'file' and/or 'categorie' parameters.";
        try {
            List<Abonnement> abonnements = readAbonnementsFromCsvPath(file);
            List<Abonnement> res = subscriptionService.filterByCategory(abonnements, categorie);
            return "Found: " + res.size() + " abonnements: " + res.stream().map(Abonnement::getNomService).collect(Collectors.joining(", "));
        } catch (IOException e) {
            return "Cannot read file '" + file + "': " + e.getMessage();
        }
    }

    private String handleTopPriority(Map<String, String> p) {
        String file = p.get("file");
        if (file == null) return "Missing 'file' parameter pointing to CSV of abonnements.";
        try {
            List<Abonnement> abonnements = readAbonnementsFromCsvPath(file);
            List<Abonnement> top = subscriptionService.getTopPrioritySubscriptions(abonnements);
            return "Top priority: " + top.stream().map(Abonnement::getNomService).collect(Collectors.joining(", "));
        } catch (IOException e) {
            return "Cannot read file '" + file + "': " + e.getMessage();
        }
    }

    private String handleSavingOps(Map<String, String> p) {
        String file = p.get("file");
        if (file == null) return "Missing 'file' parameter pointing to CSV of abonnements.";
        try {
            List<Abonnement> abonnements = readAbonnementsFromCsvPath(file);
            List<Abonnement> cand = subscriptionService.identifySavingOpportunities(abonnements);
            return "Saving opportunities: " + cand.size() + " abonnements: " + cand.stream().map(Abonnement::getNomService).collect(Collectors.joining(", "));
        } catch (IOException e) {
            return "Cannot read file '" + file + "': " + e.getMessage();
        }
    }

    private String helpText() {
        return "Available commands:\n"
            + "  addSubscription nomService=... user=... prixMensuel=... dateDebut=YYYY-MM-DD dateFin=YYYY-MM-DD [categorie=...]\n"
            + "  createUser email=... password=... pseudo=...\n"
            + "  help\n"
            + "Notes: validation errors are reported and no business logic is changed.";
    }
}
