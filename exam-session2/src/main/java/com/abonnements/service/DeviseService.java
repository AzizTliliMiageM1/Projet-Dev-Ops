package com.abonnements.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

// Appel API distante : https://api.frankfurter.app (gratuite, sans cle)
public class DeviseService {

    private static final String API_URL = "https://api.frankfurter.app/latest?from=EUR";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public DeviseService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public double getTauxDeChange(String deviseCible) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new Exception("Erreur API Frankfurter: HTTP " + response.statusCode());
        }

        // reponse: {"base":"EUR", "rates":{"USD":1.08, "GBP":0.85, ...}}
        JsonNode json = objectMapper.readTree(response.body());
        JsonNode rates = json.get("rates");

        if (rates == null || !rates.has(deviseCible.toUpperCase())) {
            throw new Exception("Devise non supportee: " + deviseCible
                    + ". Utilisez un code ISO valide (USD, GBP, JPY, CHF...)");
        }

        return rates.get(deviseCible.toUpperCase()).asDouble();
    }

    public Map<String, Object> convertir(double montantEur, String deviseCible) throws Exception {
        double taux = getTauxDeChange(deviseCible);
        double montantConverti = Math.round(montantEur * taux * 100.0) / 100.0;

        Map<String, Object> resultat = new LinkedHashMap<>();
        resultat.put("montantOriginal", montantEur);
        resultat.put("deviseOrigine", "EUR");
        resultat.put("deviseCible", deviseCible.toUpperCase());
        resultat.put("taux", taux);
        resultat.put("montantConverti", montantConverti);
        resultat.put("source", "api.frankfurter.app");

        return resultat;
    }
}
