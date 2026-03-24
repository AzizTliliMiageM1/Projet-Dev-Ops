package com.projet.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Currency;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CurrencyCatalogServiceImpl implements CurrencyCatalogService {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String FIXER_SYMBOLS_URL = "https://api.apilayer.com/fixer/symbols";
    private static final String CURRENCYLAYER_LIST_URL = "http://api.currencylayer.com/list";
    private static final Set<String> DEFAULT_ALLOWED = Set.of("USD", "GBP", "EUR", "CHF");

    @Override
    public Set<String> getAllCurrencies() {
        Set<String> allowed = getAllowedCurrencies();
        Set<String> symbols = new TreeSet<>();

        symbols.addAll(fetchFixerSymbols());
        symbols.retainAll(allowed);
        if (!symbols.isEmpty()) {
            return symbols;
        }

        symbols.addAll(fetchCurrencyLayerSymbols());
        symbols.retainAll(allowed);
        if (!symbols.isEmpty()) {
            return symbols;
        }

        // Fallback local pour garantir la fonctionnalite meme sans cle API.
        for (Currency currency : Currency.getAvailableCurrencies()) {
            String code = currency.getCurrencyCode();
            if (allowed.contains(code)) {
                symbols.add(code);
            }
        }
        return symbols;
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

    private Set<String> fetchFixerSymbols() {
        Set<String> result = new TreeSet<>();
        String apiKey = System.getenv("FIXER_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            return result;
        }

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(FIXER_SYMBOLS_URL).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("apikey", apiKey);

            if (connection.getResponseCode() != 200) {
                return result;
            }

            String body = readBody(connection);
            JsonNode root = MAPPER.readTree(body);
            JsonNode symbols = root.path("symbols");
            symbols.fieldNames().forEachRemaining(result::add);
        } catch (Exception ignored) {
            // fallback handled by caller
        }

        return result;
    }

    private Set<String> fetchCurrencyLayerSymbols() {
        Set<String> result = new TreeSet<>();
        String accessKey = System.getenv("CURRENCYLAYER_API_KEY");
        if (accessKey == null || accessKey.isBlank()) {
            return result;
        }

        try {
            String url = CURRENCYLAYER_LIST_URL + "?access_key=" + accessKey;
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            if (connection.getResponseCode() != 200) {
                return result;
            }

            String body = readBody(connection);
            JsonNode root = MAPPER.readTree(body);
            JsonNode currencies = root.path("currencies");
            currencies.fieldNames().forEachRemaining(result::add);
        } catch (Exception ignored) {
            // fallback handled by caller
        }

        return result;
    }

    private String readBody(HttpURLConnection connection) throws Exception {
        StringBuilder body = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
        }
        return body.toString();
    }
}
