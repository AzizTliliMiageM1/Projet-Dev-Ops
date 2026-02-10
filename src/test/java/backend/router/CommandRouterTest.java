package backend.router;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class CommandRouterTest {

    @Test
    public void addSubscription_shouldCreateSubscription() {
        CommandRouter router = new CommandRouter();
        String[] args = new String[] {
            "addSubscription",
            "nomService=Netflix",
            "user=aziz",
            "prixMensuel=9.99",
            "dateDebut=2025-01-01",
            "dateFin=2026-01-01",
            "categorie=Streaming"
        };

        String result = router.route(args);
        assertTrue(result.contains("Subscription created"), "Expected creation message");
        assertTrue(result.contains("Netflix"));
        assertTrue(result.contains("aziz"));
    }

    @Test
    public void createUser_shouldReturnCreatedUser() {
        CommandRouter router = new CommandRouter();
        String[] args = new String[] {
            "createUser",
            "email=aziz@example.com",
            "password=Password1",
            "pseudo=aziz"
        };

        String result = router.route(args);
        assertTrue(result.contains("User created"), "Expected user creation message");
        assertTrue(result.contains("aziz@example.com"));
        assertTrue(result.contains("pseudo=aziz") || result.contains("(pseudo=aziz)"));
    }
}
