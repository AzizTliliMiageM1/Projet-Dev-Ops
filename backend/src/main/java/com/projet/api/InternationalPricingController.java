package com.projet.api;

import static spark.Spark.get;
import static spark.Spark.path;
import static spark.Spark.post;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projet.backend.domain.Abonnement;
import com.projet.backend.domain.PaymentResult;
import com.projet.repository.AbonnementRepository;
import com.projet.repository.FileAbonnementRepository;
import com.projet.repository.UserAbonnementRepository;
import com.projet.service.CountryCurrencyMapper;
import com.projet.service.CurrencyCatalogService;
import com.projet.service.CurrencyCatalogServiceImpl;
import com.projet.service.CurrencyService;
import com.projet.service.CurrencyServiceImpl;
import com.projet.service.PaymentService;
import com.projet.service.PaymentServiceImpl;

import spark.Request;

public final class InternationalPricingController {

    private static final CurrencyService CURRENCY_SERVICE = new CurrencyServiceImpl();
    private static final CurrencyCatalogService CURRENCY_CATALOG_SERVICE = new CurrencyCatalogServiceImpl();
    private static final PaymentService PAYMENT_SERVICE = new PaymentServiceImpl();

    private InternationalPricingController() {
    }

    private static AbonnementRepository getOrCreateRepo(Request req) {
        String user = req.session().attribute("user_email");
        if (user == null) {
            return new FileAbonnementRepository("abonnements.txt");
        }
        return new UserAbonnementRepository(user);
    }

    public static void register(ObjectMapper mapper) {
        get("/get-all-currencies", (req, res) -> {
            res.type("application/json");
            Map<String, Object> payload = new HashMap<>();
            payload.put("currencies", CURRENCY_CATALOG_SERVICE.getAllCurrencies());
            payload.put("provider", resolveCurrencyProvider());
            return mapper.writeValueAsString(payload);
        });

        path("/user", () -> {
            post("/country", (req, res) -> {
                res.type("application/json");

                Map<String, String> body = mapper.readValue(req.body(), new TypeReference<Map<String, String>>() {});
                String country = body.getOrDefault("country", "FR").toUpperCase();
                String countryName = body.getOrDefault("countryName", country);
                String requestedCurrency = body.getOrDefault("currency", "").toUpperCase();

                String currency;
                if (requestedCurrency.matches("^[A-Z]{3}$")) {
                    currency = requestedCurrency;
                } else {
                    currency = CountryCurrencyMapper.currencyForCountry(country);
                }

                req.session(true).attribute("user_country", country);
                req.session(true).attribute("user_country_name", countryName);
                req.session(true).attribute("user_currency", currency);

                return mapper.writeValueAsString(Map.of(
                    "country", country,
                    "countryName", countryName,
                    "currency", currency
                ));
            });
        });

        get("/convert/abonnements", (req, res) -> {
            res.type("application/json");

            String country = req.session().attribute("user_country");
            if (country == null || country.isBlank()) {
                country = "FR";
                req.session(true).attribute("user_country", country);
            }

            String countryName = req.session().attribute("user_country_name");
            if (countryName == null || countryName.isBlank()) {
                countryName = country;
            }

            String userCurrency = req.session().attribute("user_currency");
            if (userCurrency == null || userCurrency.isBlank()) {
                userCurrency = CountryCurrencyMapper.currencyForCountry(country);
                req.session(true).attribute("user_currency", userCurrency);
            }

            AbonnementRepository repo = getOrCreateRepo(req);
            List<Abonnement> abonnements = repo.findAll();
            List<Map<String, Object>> converted = new ArrayList<>();

            for (Abonnement abo : abonnements) {
                String sourceCurrency = abo.getCurrency();
                double originalPrice = abo.getPrixMensuel();
                double convertedPrice = CURRENCY_SERVICE.convert(sourceCurrency, userCurrency, originalPrice);

                Map<String, Object> row = new HashMap<>();
                row.put("id", abo.getId());
                row.put("nomService", abo.getNomService());
                row.put("clientName", abo.getClientName());
                row.put("dateDebut", abo.getDateDebut());
                row.put("dateFin", abo.getDateFin());
                row.put("originalPrice", originalPrice);
                row.put("originalCurrency", sourceCurrency);
                row.put("convertedPrice", convertedPrice);
                row.put("targetCurrency", userCurrency);
                row.put("country", country);
                row.put("countryName", countryName);
                converted.add(row);
            }

            return mapper.writeValueAsString(converted);
        });

        path("/payment", () -> {
            post("/simulate", (req, res) -> {
                res.type("application/json");

                Map<String, Object> body = mapper.readValue(req.body(), new TypeReference<Map<String, Object>>() {});
                double amount = Double.parseDouble(String.valueOf(body.getOrDefault("amount", 0)));
                String currency = String.valueOf(body.getOrDefault("currency", "EUR"));

                PaymentResult result = PAYMENT_SERVICE.simulatePayment(amount, currency);
                return mapper.writeValueAsString(result);
            });
        });
    }

    private static String resolveCurrencyProvider() {
        String fixer = System.getenv("FIXER_API_KEY");
        if (fixer != null && !fixer.isBlank()) {
            return "fixer";
        }

        String currencyLayer = System.getenv("CURRENCYLAYER_API_KEY");
        if (currencyLayer != null && !currencyLayer.isBlank()) {
            return "currencylayer";
        }

        return "fallback-local";
    }
}
