package backend;

import backend.router.CommandRouter;

/**
 * CLI entry point for the backend router.
 * Usage: java -cp backend.jar backend.Main <command> [key=value ...]
 */
public class Main {
    public static void main(String[] args) {
        CommandRouter router = new CommandRouter();
        String result = router.route(args);
        System.out.println(result);
    }
}
