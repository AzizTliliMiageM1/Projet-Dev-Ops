package com.projet.backend.cli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.projet.analytics.SubscriptionAnalytics;
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

    private static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

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
            case "dashboard":
                return handleDashboard(params);
            case "budgetPlan":
                return handleBudgetPlan(params);
            case "portfolioHealth":
                return handlePortfolioHealth(params);
            case "recommendations":
                return handleRecommendations(params);
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
        for (int i = 0; i < lines.size(); i++) {
            String l = lines.get(i);
            if (i == 0 || l == null || l.trim().isEmpty()) continue; // Skip header row
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

    private String handleBudgetPlan(Map<String, String> p) {
        String targetParam = p.get("target");
        if (targetParam == null || targetParam.isBlank()) {
            return "Missing 'target' parameter indicating the desired monthly budget.";
        }

        double target;
        try {
            target = Double.parseDouble(targetParam);
        } catch (NumberFormatException e) {
            return "Invalid 'target' parameter: " + e.getMessage();
        }

        String file = p.get("file");
        if (file == null || file.isBlank()) {
            return "Missing 'file' parameter pointing to CSV of abonnements.";
        }

        try {
            List<Abonnement> abonnements = readAbonnementsFromCsvPath(file);
            SubscriptionAnalytics.BudgetReductionPlan plan = SubscriptionAnalytics.planBudgetReduction(abonnements, target);
            return formatBudgetPlan(plan);
        } catch (IOException e) {
            return "Cannot read file '" + file + "': " + e.getMessage();
        } catch (IllegalArgumentException e) {
            return "Invalid target budget: " + e.getMessage();
        }
    }

    private String formatBudgetPlan(SubscriptionAnalytics.BudgetReductionPlan plan) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n📉 PLAN DE RÉDUCTION BUDGÉTAIRE\n");
        sb.append("──────────────────────────────\n");
        sb.append(String.format("Budget actuel  : %.2f€%n", plan.getCurrentMonthlyCost()));
        sb.append(String.format("Budget cible   : %.2f€%n", plan.getTargetMonthlyBudget()));
        sb.append(String.format("Économies à réaliser : %.2f€%n", plan.getRequiredSavings()));
        sb.append(String.format("Économies planifiées : %.2f€%n", plan.getAchievedSavings()));
        sb.append(String.format("Faisable : %s%n", plan.isTargetFeasible() ? "Oui" : "Non"));
        sb.append(String.format("Écart restant : %.2f€%n", plan.getShortfall()));

        sb.append("\nPriorités de résiliation :\n");
        List<Abonnement> recommended = plan.getRecommendedCancellations();
        if (recommended.isEmpty()) {
            sb.append("  - Aucune suppression nécessaire\n");
        } else {
            int rank = 1;
            for (Abonnement abo : recommended) {
                sb.append(String.format(
                    "  %d. %s (%.2f€/mois) - Risque %.0f%% | Valeur %.2f%n",
                    rank++,
                    abo.getNomService(),
                    abo.getPrixMensuel(),
                    abo.getChurnRisk(),
                    abo.getValueScore()
                ));
            }
        }

        sb.append("\nOptions additionnelles :\n");
        List<Abonnement> optional = plan.getOptionalCandidates();
        if (optional.isEmpty()) {
            sb.append("  - Aucun autre abonnement proposé\n");
        } else {
            int displayed = 0;
            for (Abonnement abo : optional) {
                if (displayed >= 3) {
                    break;
                }
                sb.append(String.format("  • %s (%.2f€/mois)%n", abo.getNomService(), abo.getPrixMensuel()));
                displayed++;
            }
            if (optional.size() > displayed) {
                sb.append(String.format("  • ... (%d autres)%n", optional.size() - displayed));
            }
        }

        return sb.toString();
    }

    private String handlePortfolioHealth(Map<String, String> p) {
        String file = p.get("file");
        if (file == null || file.isBlank()) {
            return "Missing 'file' parameter pointing to CSV of abonnements.";
        }

        try {
            List<Abonnement> abonnements = readAbonnementsFromCsvPath(file);
            if (abonnements.isEmpty()) {
                return "No subscriptions found in the provided file.";
            }
            SubscriptionService.PortfolioStats stats = subscriptionService.calculatePortfolioStats(abonnements);
            return formatPortfolioHealth(stats);
        } catch (IOException e) {
            return "Cannot read file '" + file + "': " + e.getMessage();
        }
    }

    private String formatPortfolioHealth(SubscriptionService.PortfolioStats stats) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n🩺 SANTÉ DU PORTEFEUILLE\n");
        sb.append("────────────────────────────\n");
        int score = (int) Math.round(stats.portfolioHealthScore);
        sb.append(String.format("Score global       : %d/100%n", score));
        sb.append(String.format("Abonnements actifs : %d / %d%n", stats.activeSubscriptions, stats.totalSubscriptions));
        sb.append(String.format("Dépense mensuelle  : %.2f€ (moyenne %.2f€)%n", stats.totalMonthlyCost, stats.averageMonthlyCost));
        String categories = stats.categoriesDistribution.isEmpty()
            ? "Aucune catégorie détectée"
            : String.join(", ", stats.categoriesDistribution);
        sb.append(String.format("Catégories couvertes : %s%n", categories));
        sb.append(String.format("Churn élevé         : %d abonnement(s)%n", stats.highChurnRiskCount));

        sb.append("\n" + healthAdvice(score) + "\n");
        return sb.toString();
    }

    private String healthAdvice(int score) {
        if (score >= 80) {
            return "✅ Portefeuille très sain : poursuivez le suivi mensuel pour conserver ce niveau.";
        }
        if (score >= 60) {
            return "🟡 Portefeuille équilibré : ciblez les abonnements à faible valeur pour gagner quelques points.";
        }
        if (score >= 40) {
            return "🟠 Portefeuille fragile : identifiez 1 à 2 abonnements à optimiser rapidement.";
        }
        return "🔴 Portefeuille sous tension : priorisez un plan de réduction et revalidez chaque abonnement.";
    }

    private String handleRecommendations(Map<String, String> p) {
        String file = p.get("file");
        if (file == null || file.isBlank()) {
            return "Missing 'file' parameter pointing to CSV of abonnements.";
        }

        try {
            List<Abonnement> abonnements = readAbonnementsFromCsvPath(file);
            if (abonnements.isEmpty()) {
                return "No subscriptions found in the provided file.";
            }

            List<Abonnement> highRisk = subscriptionService.getHighChurnRiskSubscriptions(abonnements);
            List<Abonnement> savings = subscriptionService.identifySavingOpportunities(abonnements);
            double totalSavings = savings.stream().mapToDouble(Abonnement::getPrixMensuel).sum();
            List<Abonnement> expiring = subscriptionService.getExpiringSubscriptions(abonnements, 30).stream()
                .sorted(Comparator.comparing(Abonnement::getDateFin))
                .limit(5)
                .collect(Collectors.toList());

            List<Abonnement> lowValue = new ArrayList<>();
            List<Abonnement> sortedByValue = subscriptionService.sortByValueScore(abonnements);
            if (!sortedByValue.isEmpty()) {
                int start = Math.max(0, sortedByValue.size() - 5);
                lowValue = new ArrayList<>(sortedByValue.subList(start, sortedByValue.size()));
                lowValue.sort(Comparator.comparingDouble(Abonnement::getValueScore));
            }

            return formatRecommendations(highRisk, lowValue, savings, totalSavings, expiring);
        } catch (IOException e) {
            return "Cannot read file '" + file + "': " + e.getMessage();
        }
    }

    private String formatRecommendations(
        List<Abonnement> highRisk,
        List<Abonnement> lowValue,
        List<Abonnement> savings,
        double totalSavings,
        List<Abonnement> expiring
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n💡 RECOMMANDATIONS PERSONNALISÉES\n");
        sb.append("─────────────────────────────────\n");

        if (totalSavings > 0) {
            sb.append(String.format("💰 Gains potentiels : %.2f€/mois%n", Math.round(totalSavings * 100.0) / 100.0));
            savings.stream().limit(5).forEach(abo ->
                sb.append(String.format(
                    "  • %s (%.2f€/mois, risque %.0f%%)%n",
                    abo.getNomService(),
                    abo.getPrixMensuel(),
                    abo.getChurnRisk()
                ))
            );
            if (savings.size() > 5) {
                sb.append(String.format("  • ... (%d autres)%n", savings.size() - 5));
            }
        } else {
            sb.append("💰 Aucun gain immédiat détecté sur la base actuelle.%n".formatted());
        }

        sb.append("\n⚠️ Haut risque de churn\n");
        if (highRisk.isEmpty()) {
            sb.append("  ✅ Aucun abonnement critique identifié.%n");
        } else {
            highRisk.stream().limit(5).forEach(abo ->
                sb.append(String.format(
                    "  • %s (%s) – %.0f%%%n",
                    abo.getNomService(),
                    abo.getCategorie(),
                    abo.getChurnRisk()
                ))
            );
            if (highRisk.size() > 5) {
                sb.append(String.format("  • ... (%d autres)%n", highRisk.size() - 5));
            }
        }

        sb.append("\n📉 Faible valeur perçue\n");
        if (lowValue.isEmpty()) {
            sb.append("  ✅ Tous les abonnements affichent une valeur correcte.%n");
        } else {
            lowValue.forEach(abo ->
                sb.append(String.format(
                    "  • %s – score %.2f%n",
                    abo.getNomService(),
                    abo.getValueScore()
                ))
            );
        }

        sb.append("\n⏰ Expirations proches (30j)\n");
        if (expiring.isEmpty()) {
            sb.append("  ✅ Aucun renouvellement urgent.%n");
        } else {
            expiring.forEach(abo ->
                sb.append(String.format(
                    "  • %s – fin le %s%n",
                    abo.getNomService(),
                    abo.getDateFin().format(DISPLAY_DATE)
                ))
            );
        }

        return sb.toString();
    }

    /**
     * Affiche un dashboard complet du portefeuille d'abonnements
     * 
     * Optionnel: fichier CSV à analyser. Si absent, affiche un message informatif.
     */
    private String handleDashboard(Map<String, String> p) {
        String file = p.get("file");
        try {
            List<Abonnement> abonnements;
            if (file != null && !file.trim().isEmpty()) {
                abonnements = readAbonnementsFromCsvPath(file);
            } else {
                // Sans fichier, on affiche juste un message d'aide
                return "Usage: dashboard [file=chemin/vers/abonnements.csv]\n"
                    + "Affiche un tableau de bord complet avec:\n"
                    + "  - Résumé financier et score santé\n"
                    + "  - Composition par catégorie\n"
                    + "  - Abonnements à haut risque\n"
                    + "  - Top priorités à conserver\n"
                    + "  - Opportunités d'économies\n"
                    + "  - Expirations proches";
            }
            return DashboardFormatter.formatPortfolioDashboard(abonnements);
        } catch (IOException e) {
            return "Cannot read file '" + file + "': " + e.getMessage();
        }
    }

    private String helpText() {
        return "Available commands:\n\n"
            + "CORE OPERATIONS\n"
            + "  addSubscription nomService=... user=... prixMensuel=... dateDebut=YYYY-MM-DD dateFin=YYYY-MM-DD [categorie=...]\n"
            + "  createUser email=... password=... pseudo=...\n"
            + "  roiScore nomService=... user=... prixMensuel=... dateDebut=YYYY-MM-DD dateFin=YYYY-MM-DD\n\n"
            + "FILE ANALYSIS\n"
            + "  statsFromCsv file=abonnements.csv\n"
            + "  expiring file=abonnements.csv\n"
            + "  filterByCategory file=abonnements.csv categorie=Streaming\n"
            + "  topPriority file=abonnements.csv\n"
            + "  savingOps file=abonnements.csv\n\n"
            + "DASHBOARD\n"
            + "  dashboard [file=abonnements.csv]\n\n"
            + "BUDGET\n"
            + "  budgetPlan target=150 file=abonnements.csv\n\n"
            + "ANALYTICS\n"
            + "  portfolioHealth file=abonnements.csv\n"
            + "  recommendations file=abonnements.csv\n\n"
            + "Notes: validation errors are reported; no business logic is changed.";
    }
}
