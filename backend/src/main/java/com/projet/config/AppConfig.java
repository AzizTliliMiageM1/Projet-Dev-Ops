package com.projet.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * AppConfig — chargeur de configuration centralisé.
 *
 * Priorité de résolution des variables :
 *   1. Variables d'environnement système (System.getenv)       ← Docker / CI / OS
 *   2. Fichier .env à la racine du projet                      ← développement local
 *   3. Valeur par défaut fournie par l'appelant
 *
 * Format .env (une paire KEY=VALUE par ligne, # pour les commentaires) :
 *   MAILGUN_API_KEY=key-xxxxxxxxxxxxxxxxxxxx
 *   MAILGUN_DOMAIN=mg.mondomaine.com
 *   STRIPE_SECRET_KEY=sk_live_xxxxxxxxxxxxxxxx
 *   OKTA_ORG_URL=https://dev-XXXXXXXX.okta.com
 *   OKTA_API_TOKEN=xxxxxxxxxxxxxxxxxxxxxxxxxxxx
 */
public final class AppConfig {

    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);

    /** Variables lues depuis le fichier .env au démarrage. */
    private static final Map<String, String> FILE_PROPS = new HashMap<>();

    static {
        loadDotEnv();
    }

    private AppConfig() { /* utilitaire statique */ }

    // -------------------------------------------------------------------------
    // API publique
    // -------------------------------------------------------------------------

    /**
     * Retourne la valeur de {@code key}, ou {@code ""} si non définie.
     */
    public static String get(String key) {
        return get(key, "");
    }

    /**
     * Retourne la valeur de {@code key}.<br>
     * Ordre de priorité : env système → fichier .env → {@code defaultValue}.
     */
    public static String get(String key, String defaultValue) {
        // 1. Variable d'environnement système (Docker, CI, OS)
        String sysValue = System.getenv(key);
        if (sysValue != null && !sysValue.isBlank()) {
            return sysValue.trim();
        }
        // 2. Fichier .env
        String fileValue = FILE_PROPS.get(key);
        if (fileValue != null && !fileValue.isBlank()) {
            return fileValue;
        }
        // 3. Valeur par défaut
        return defaultValue;
    }

    /** Retourne {@code true} si la clé est définie et non vide. */
    public static boolean isDefined(String key) {
        return !get(key).isBlank();
    }

    // -------------------------------------------------------------------------
    // Chargement du fichier .env
    // -------------------------------------------------------------------------

    private static void loadDotEnv() {
        // Cherche .env dans le répertoire de travail courant (racine du projet)
        // puis dans backend/ pour les lancements depuis ce sous-dossier.
        Path[] candidates = {
            Paths.get(".env"),
            Paths.get("backend", ".env"),
            Paths.get("..", ".env"),
        };

        for (Path candidate : candidates) {
            if (Files.exists(candidate)) {
                try (BufferedReader reader = Files.newBufferedReader(candidate)) {
                    int count = parseDotEnv(reader);
                    if (count > 0) {
                        logger.info("AppConfig: {} variable(s) chargée(s) depuis {}",
                            count, candidate.toAbsolutePath().normalize());
                    }
                } catch (IOException e) {
                    logger.warn("AppConfig: impossible de lire {} — {}", candidate, e.getMessage());
                }
                return; // on s'arrête au premier fichier trouvé
            }
        }
        logger.debug("AppConfig: aucun fichier .env trouvé — utilisation des variables d'environnement uniquement");
    }

    private static int parseDotEnv(BufferedReader reader) throws IOException {
        int count = 0;
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            // Ignorer les lignes vides et les commentaires
            if (line.isEmpty() || line.startsWith("#")) continue;
            int sep = line.indexOf('=');
            if (sep <= 0) continue;

            String key   = line.substring(0, sep).trim();
            String value = line.substring(sep + 1).trim();

            // Supprimer les guillemets encadrants (simples ou doubles)
            if (value.length() >= 2) {
                char first = value.charAt(0);
                char last  = value.charAt(value.length() - 1);
                if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                    value = value.substring(1, value.length() - 1);
                }
            }

            if (!key.isEmpty() && !value.isEmpty()) {
                FILE_PROPS.put(key, value);
                count++;
            }
        }
        return count;
    }
}
