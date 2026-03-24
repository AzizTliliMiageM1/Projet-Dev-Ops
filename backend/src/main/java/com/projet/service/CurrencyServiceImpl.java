package com.projet.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CurrencyServiceImpl implements CurrencyService {

    private static final String API_BASE = "https://api.exchangerate-api.com/v4/latest/";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Set<String> DEFAULT_ALLOWED = Set.of("USD", "GBP", "EUR", "CHF");

    @Override
    public double convert(String from, String to, double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("amount must be >= 0");
        }

        String source = (from == null || from.isBlank()) ? "EUR" : from.toUpperCase();
        String target = (to == null || to.isBlank()) ? "EUR" : to.toUpperCase();
        Set<String> allowed = getAllowedCurrencies();

        if (!allowed.contains(source) || !allowed.contains(target)) {
            return amount;
        }

        if (source.equals(target)) {
            return amount;
        }

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(API_BASE + source).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            if (connection.getResponseCode() != 200) {
                return amount;
            }

            StringBuilder body = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    body.append(line);
                }
            }

            JsonNode root = MAPPER.readTree(body.toString());
            JsonNode rates = root.path("rates");
            JsonNode rate = rates.path(target);
            if (!rate.isNumber()) {
                return amount;
            }

            return amount * rate.asDouble();
        } catch (Exception ignored) {
            return amount;
        }
    }

    private Set<String> getAllowedCurrencies() {
        String fromEnv = System.getenv("ALLOWED_CURRENCIES");
        if (fromEnv == null || fromEnv.isBlank()) {
            return DEFAULT_ALLOWED;
        }

        Set<String> parsed = new LinkedHashSet<>();
        Arrays.stream(fromEnv.split(","))
            .map(String::trim)
            .map(String::toUpperCase)
            .filter(s -> s.matches("^[A-Z]{3}$"))
            .forEach(parsed::add);

        return parsed.isEmpty() ? DEFAULT_ALLOWED : parsed;
    }
}
