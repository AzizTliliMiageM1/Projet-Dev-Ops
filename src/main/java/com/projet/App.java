package com.projet;

/**
 * Point d'entrée principal pour le backend autonome.
 * 
 * Modes d'exécution :
 * - Sans args: Lance le serveur API (port 4567 par défaut)
 * - --help: Affiche les options disponibles
 * 
 * Usage:
 *   java -jar backend.jar                    # Serveur API
 *   java -jar backend.jar --help             # Affiche l'aide
 * 
 * Le backend peut être exécuté de manière indépendante sans UI ni d'autres dépendances.
 */
public class App {
    public static void main(String[] args) {
        // Validation des arguments
        if (args != null && args.length > 0) {
            String cmd = args[0];
            if ("--help".equals(cmd) || "-h".equals(cmd)) {
                printHelp();
                System.exit(0);
            }
        }
        
        // Mode par défaut: lancer le serveur API
        try {
            com.projet.api.ApiServer.main(args);
        } catch (Exception e) {
            System.err.println("Erreur au démarrage du serveur API: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void printHelp() {
        System.out.println("════════════════════════════════════════════════════════════");
        System.out.println("  Backend autonome - Gestion d'abonnements");
        System.out.println("════════════════════════════════════════════════════════════");
        System.out.println();
        System.out.println("Usage: java -jar backend.jar [OPTIONS]");
        System.out.println();
        System.out.println("OPTIONS:");
        System.out.println("  (aucun)    Démarre le serveur API sur le port 4567");
        System.out.println("  --help     Affiche ce message d'aide");
        System.out.println("  -h         Affiche ce message d'aide");
        System.out.println();
        System.out.println("Exemples:");
        System.out.println("  java -jar backend.jar");
        System.out.println("  curl http://localhost:4567/api/abonnements/all");
        System.out.println();
        System.out.println("Architecture:");
        System.out.println("  - Domain Layer (backend/domain) : Logique métier pure");
        System.out.println("  - Service Layer (backend/service) : Orchestration métier");
        System.out.println("  - Adapter Layer (backend/adapter) : Conversions CSV");
        System.out.println("  - API Layer : Endpoints REST");
        System.out.println("════════════════════════════════════════════════════════════");
    }
}
