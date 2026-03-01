package com.projet.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * ServiceTauxChange - Intégration avec l'API ExchangeRate pour conversion devises
 * 
 * API distante : https://api.exchangerate-api.com/v4
 * Service gratuit pour conversion entre devises en temps réel
 */
public class ServiceTauxChange {

    private static final String API_URL = "https://api.exchangerate-api.com/v4/latest";
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    
    // Cache simple avec TTL (5 minutes)
    private static final Map<String, CacheTaux> cacheTaux = new HashMap<>();
    private static final long TTL_MS = 5 * 60 * 1000; // 5 minutes

    public static class ResultatConversion {
        public boolean success;
        public double montantSource;
        public String deviseSource;
        public double montantCible;
        public String deviseCible;
        public double taux;
        public String erreur;
        public long tempsReponse;

        public ResultatConversion(boolean success, double montantSource, String deviseSource,
                                 double montantCible, String deviseCible, double taux,
                                 String erreur, long tempsReponse) {
            this.success = success;
            this.montantSource = montantSource;
            this.deviseSource = deviseSource;
            this.montantCible = montantCible;
            this.deviseCible = deviseCible;
            this.taux = taux;
            this.erreur = erreur;
            this.tempsReponse = tempsReponse;
        }
    }

    private static class CacheTaux {
        long timestamp;
        Map<String, Double> taux;

        CacheTaux(Map<String, Double> taux) {
            this.timestamp = System.currentTimeMillis();
            this.taux = taux;
        }

        boolean estExpire() {
            return System.currentTimeMillis() - timestamp > TTL_MS;
        }
    }

    /**
     * Convertir un montant d'une devise à une autre
     */
    public static ResultatConversion convertir(double montant, String deviseSource, String deviseCible) {
        long debut = System.currentTimeMillis();

        try {
            // Normaliser les codes devise en majuscule
            deviseSource = deviseSource.toUpperCase();
            deviseCible = deviseCible.toUpperCase();

            // Récupérer les taux
            Map<String, Double> taux = obtenirTaux(deviseSource);
            
            if (taux == null || !taux.containsKey(deviseCible)) {
                long tempsReponse = System.currentTimeMillis() - debut;
                return new ResultatConversion(false, montant, deviseSource, 0, deviseCible, 0,
                        "Devise non disponible : " + deviseCible, tempsReponse);
            }

            double tauxChange = taux.get(deviseCible);
            double montantConverti = montant * tauxChange;
            
            long tempsReponse = System.currentTimeMillis() - debut;
            return new ResultatConversion(true, montant, deviseSource, montantConverti, 
                    deviseCible, tauxChange, null, tempsReponse);

        } catch (Exception e) {
            long tempsReponse = System.currentTimeMillis() - debut;
            return new ResultatConversion(false, montant, deviseSource, 0, deviseCible, 0,
                    "Erreur : " + e.getMessage(), tempsReponse);
        }
    }

    /**
     * Obtenir les taux de change pour une devise source
     */
    private static Map<String, Double> obtenirTaux(String deviseSource) {
        // Vérifier le cache
        if (cacheTaux.containsKey(deviseSource)) {
            CacheTaux cache = cacheTaux.get(deviseSource);
            if (!cache.estExpire()) {
                return cache.taux;
            } else {
                cacheTaux.remove(deviseSource);
            }
        }

        // Appel à l'API
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + "/" + deviseSource))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Map<String, Double> taux = extraireTaux(response.body());
                if (taux != null && !taux.isEmpty()) {
                    // Mettre en cache
                    cacheTaux.put(deviseSource, new CacheTaux(taux));
                    return taux;
                }
            }

        } catch (IOException | InterruptedException e) {
            // Ignorer et retourner null
        }

        return null;
    }

    /**
     * Extraire les taux de change de la réponse JSON
     */
    private static Map<String, Double> extraireTaux(String jsonResponse) {
        Map<String, Double> taux = new HashMap<>();

        try {
            // Chercher la section "rates"
            int debut = jsonResponse.indexOf("\"rates\":{");
            if (debut == -1) {
                return null;
            }

            debut += 9; // Longueur de "\"rates\":{"
            int fin = jsonResponse.indexOf("}", debut);

            String ratessString = jsonResponse.substring(debut, fin);
            String[] paires = ratessString.split(",");

            for (String paire : paires) {
                String[] parts = paire.split(":");
                if (parts.length == 2) {
                    String devise = parts[0].replaceAll("[\"\\s]", "").trim();
                    String valeur = parts[1].replaceAll("[\"\\s]", "").trim();
                    
                    try {
                        double tauxValue = Double.parseDouble(valeur);
                        taux.put(devise, tauxValue);
                    } catch (NumberFormatException e) {
                        // Ignorer les valeurs non numériques
                    }
                }
            }

        } catch (Exception e) {
            // Ignorer erreur parsing
        }

        return taux.isEmpty() ? null : taux;
    }

    /**
     * Convertir un prix en devise EUR (cas spécifique pour abonnements)
     */
    public static ResultatConversion convertirEnEuro(double montant, String deviseSource) {
        return convertir(montant, deviseSource, "EUR");
    }

    /**
     * Analyser la stabilité des devises (variation de taux)
     */
    public static Map<String, Object> analyserStabilite(String devise) {
        Map<String, Object> resultat = new HashMap<>();
        
        try {
            // Premier appel
            Map<String, Double> taux1 = obtenirTaux(devise.toUpperCase());
            
            // Attendre 10 secondes
            Thread.sleep(10000);
            
            // Vider le cache pour forcer un nouvel appel
            cacheTaux.remove(devise.toUpperCase());
            
            // Deuxième appel
            Map<String, Double> taux2 = obtenirTaux(devise.toUpperCase());

            if (taux1 != null && taux2 != null) {
                int deviseStables = 0;
                int deviseVolatiles = 0;
                double variationMoyenne = 0;

                for (String key : taux1.keySet()) {
                    if (taux2.containsKey(key)) {
                        double variation = Math.abs(taux1.get(key) - taux2.get(key)) / taux1.get(key);
                        variationMoyenne += variation;
                        
                        if (variation < 0.01) {
                            deviseStables++;
                        } else {
                            deviseVolatiles++;
                        }
                    }
                }

                variationMoyenne = variationMoyenne / taux1.size();

                resultat.put("success", true);
                resultat.put("devise", devise.toUpperCase());
                resultat.put("deviseStables", deviseStables);
                resultat.put("deviseVolatiles", deviseVolatiles);
                resultat.put("variationMoyenne", String.format("%.4f%%", variationMoyenne * 100));
            } else {
                resultat.put("success", false);
                resultat.put("erreur", "Impossible de récupérer les taux");
            }

        } catch (InterruptedException e) {
            resultat.put("success", false);
            resultat.put("erreur", "Interruption : " + e.getMessage());
        }

        return resultat;
    }

    /**
     * Vérifier la connexion à l'API ExchangeRate
     */
    public static boolean verifierConnexion() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + "/USD"))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Obtenir les infos de configuration
     */
    public static Map<String, Object> obtenirInfos() {
        Map<String, Object> infos = new HashMap<>();
        infos.put("service", "ExchangeRate-API");
        infos.put("apiUrl", API_URL);
        infos.put("cacheTTL", "5 minutes");
        infos.put("devisesCachees", cacheTaux.size());
        infos.put("connecte", verifierConnexion());
        return infos;
    }
}
