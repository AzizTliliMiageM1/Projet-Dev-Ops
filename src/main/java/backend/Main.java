package backend;

import com.projet.backend.cli.CommandRouter;

/**
 * CLI entry point for the backend router.
 * Usage: java -cp backend.jar backend.Main <command> [key=value ...]
 */
public class Main {
    public static void main(String[] args) {
        CommandRouter router = CommandRouter.createDefault();
        String result = router.route(args);
        System.out.println(result);
    }
}
