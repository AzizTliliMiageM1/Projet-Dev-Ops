package backend.router;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

import com.projet.backend.service.SubscriptionService;
import com.projet.backend.service.UserService;

/**
 * Simple router that dispatches textual commands to backend services.
 *
 * This class is intentionally lightweight: it parses CLI style
 * arguments of the form key=value and calls the services in
 * `com.projet.backend.service`.
 */
public class CommandRouter {

    private final SubscriptionService subscriptionService = new SubscriptionService();
    private final UserService userService = new UserService();

    /**
     * Route a command expressed as an array of tokens (like args[]).
     * The first token is the command name, the rest are key=value pairs.
     *
     * @param args command tokens
     * @return readable result string
     */
    public String route(String[] args) {
        if (args == null || args.length == 0) {
            return "No command provided";
        }

        String cmd = args[0];
        Map<String, String> params = parseKeyValueArgs(args);

        try {
            switch (cmd) {
                case "addSubscription":
                    return handleAddSubscription(params);
                case "createUser":
                    return handleCreateUser(params);
                default:
                    return "Unknown command: " + cmd;
            }
        } catch (IllegalArgumentException e) {
            return "Error: " + e.getMessage();
        } catch (Exception e) {
            return "Unhandled error: " + e.getClass().getSimpleName() + " - " + e.getMessage();
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
        // Required: nomService, clientName (user), prixMensuel, dateDebut, dateFin
        String nomService = p.get("nomService");
        String clientName = p.get("user");
        String prixS = p.get("prixMensuel");
        String dateDebutS = p.get("dateDebut");
        String dateFinS = p.get("dateFin");
        String categorie = p.getOrDefault("categorie", "general");

        if (nomService == null || clientName == null || prixS == null || dateDebutS == null || dateFinS == null) {
            return "Missing required parameters for addSubscription. Required: nomService user prixMensuel dateDebut dateFin";
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

        // Delegate to service (service returns an Abonnement object; we only show a summary)
        var abonnement = subscriptionService.createSubscription(nomService, dateDebut, dateFin, prix, clientName, categorie);
        return "Subscription created: service=" + abonnement.getNomService() + " user=" + abonnement.getClientName() + " price=" + abonnement.getPrixMensuel();
    }

    private String handleCreateUser(Map<String, String> p) {
        String email = p.get("email");
        String password = p.get("password");
        String pseudo = p.get("pseudo");

        if (email == null || password == null || pseudo == null) {
            return "Missing required parameters for createUser. Required: email password pseudo";
        }

        var user = userService.createUser(email, password, pseudo);
        return "User created: " + user.getEmail() + " (pseudo=" + user.getPseudo() + ")";
    }
}
