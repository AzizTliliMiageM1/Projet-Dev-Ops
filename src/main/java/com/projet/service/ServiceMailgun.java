package com.projet.service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * ServiceMailgun - Intégration avec l'API Mailgun pour envoi emails distants
 * 
 * API distante : https://api.mailgun.net/v3
 * Service email professionnel avec webhooks et templates
 */
public class ServiceMailgun {

    private static final String MAILGUN_DOMAIN = System.getenv().getOrDefault(
        "MAILGUN_DOMAIN", "sandboxa1b2c3d4e5f6g7h8.mailgun.org"
    );
    
    private static final String MAILGUN_API_KEY = System.getenv().getOrDefault(
        "MAILGUN_API_KEY", "key-demo-123456789"
    );
    
    private static final String MAILGUN_API_URL = "https://api.mailgun.net/v3/" + MAILGUN_DOMAIN;
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    public static class ResultatEnvoiEmail {
        public boolean success;
        public String messageId;
        public String erreur;
        public long tempsReponse;

        public ResultatEnvoiEmail(boolean success, String messageId, String erreur, long tempsReponse) {
            this.success = success;
            this.messageId = messageId;
            this.erreur = erreur;
            this.tempsReponse = tempsReponse;
        }
    }

    /**
     * Envoyer un email d'alerte d'expiration via Mailgun
     */
    public static ResultatEnvoiEmail envoyerAlerteExpiration(String destinataire, String nomService, 
                                                             double prix, String dateExpiration) {
        long debut = System.currentTimeMillis();
        
        String sujet = "Alerte : Votre abonnement " + nomService + " expire bientôt";
        String texte = String.format(
            "Bonjour,\n\n" +
            "L'abonnement '%s' (%.2f€) expire le %s.\n\n" +
            "Pensez à le renouveler avant cette date.\n\n" +
            "Cordialement,\nVotre gestionnaire d'abonnements",
            nomService, prix, dateExpiration
        );

        return envoyerEmail(destinataire, sujet, texte, debut);
    }

    /**
     * Envoyer un rapport mensuel via Mailgun
     */
    public static ResultatEnvoiEmail envoyerRapportMensuel(String destinataire, String mois, 
                                                            double coutTotal, int nombreAbos) {
        long debut = System.currentTimeMillis();
        
        String sujet = "Rapport mensuel - " + mois;
        String texte = String.format(
            "Bonjour,\n\n" +
            "Voici votre rapport pour %s :\n" +
            "- Coût total : %.2f€\n" +
            "- Nombre d'abonnements : %d\n\n" +
            "Consultez votre dashboard pour plus de détails.",
            mois, coutTotal, nombreAbos
        );

        return envoyerEmail(destinataire, sujet, texte, debut);
    }

    /**
     * Envoyer un email d'alerte budget dépassé via Mailgun
     */
    public static ResultatEnvoiEmail envoyerAlerteDepassementBudget(String destinataire, 
                                                                     double budget, double depense, double depassement) {
        long debut = System.currentTimeMillis();
        
        String sujet = "Alerte : Budget d'abonnements dépassé";
        String texte = String.format(
            "Bonjour,\n\n" +
            "Vous avez dépassé votre budget d'abonnements !\n" +
            "- Budget fixé : %.2f€\n" +
            "- Dépense actuelle : %.2f€\n" +
            "- Dépassement : %.2f€\n\n" +
            "Envisagez de réduire ou supprimer certains abonnements.",
            budget, depense, depassement
        );

        return envoyerEmail(destinataire, sujet, texte, debut);
    }

    /**
     * Méthode privée : envoyer email via API Mailgun
     */
    private static ResultatEnvoiEmail envoyerEmail(String destinataire, String sujet, 
                                                    String texte, long debut) {
        try {
            // Préparation authentification Basic Auth
            String auth = Base64.getEncoder().encodeToString(("api:" + MAILGUN_API_KEY).getBytes());

            // Données du formulaire
            String body = "from=noreply@gestion-abonnements.com" +
                          "&to=" + URLEncoder.encode(destinataire, "UTF-8") +
                          "&subject=" + URLEncoder.encode(sujet, "UTF-8") +
                          "&text=" + URLEncoder.encode(texte, "UTF-8");

            // Requête POST
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(MAILGUN_API_URL + "/messages"))
                    .header("Authorization", "Basic " + auth)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            // Envoi et réponse
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            long tempsReponse = System.currentTimeMillis() - debut;

            if (response.statusCode() == 200) {
                String messageId = extraireMessageId(response.body());
                return new ResultatEnvoiEmail(true, messageId, null, tempsReponse);
            } else {
                return new ResultatEnvoiEmail(false, null, "Erreur Mailgun : " + response.statusCode(), tempsReponse);
            }

        } catch (IOException | InterruptedException e) {
            long tempsReponse = System.currentTimeMillis() - debut;
            return new ResultatEnvoiEmail(false, null, "Exception : " + e.getMessage(), tempsReponse);
        }
    }

    /**
     * Extraire l'ID du message de la réponse Mailgun
     */
    private static String extraireMessageId(String jsonResponse) {
        try {
            int debut = jsonResponse.indexOf("\"id\": \"");
            if (debut != -1) {
                debut += 8;
                int fin = jsonResponse.indexOf("\"", debut);
                return jsonResponse.substring(debut, fin);
            }
        } catch (Exception e) {
            // Ignorer erreur parsing
        }
        return "unknown";
    }

    /**
     * Vérifier la connexion à l'API Mailgun
     */
    public static boolean verifierConnexion() {
        try {
            String auth = Base64.getEncoder().encodeToString(("api:" + MAILGUN_API_KEY).getBytes());

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(MAILGUN_API_URL + "/messages"))
                    .header("Authorization", "Basic " + auth)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200 || response.statusCode() == 400;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Obtenir les infos de configuration (sans exposer la clé)
     */
    public static Map<String, Object> obtenirInfos() {
        Map<String, Object> infos = new HashMap<>();
        infos.put("service", "Mailgun");
        infos.put("domaine", MAILGUN_DOMAIN);
        infos.put("apiUrl", MAILGUN_API_URL);
        infos.put("connecte", verifierConnexion());
        return infos;
    }
}
