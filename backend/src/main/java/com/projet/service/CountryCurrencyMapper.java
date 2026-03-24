package com.projet.service;

import java.util.Map;

public final class CountryCurrencyMapper {

    private static final Map<String, String> COUNTRY_TO_CURRENCY = Map.ofEntries(
        Map.entry("FR", "EUR"),
        Map.entry("IN", "INR"),
        Map.entry("DZ", "DZD"),
        Map.entry("US", "USD"),
        Map.entry("GB", "GBP"),
        Map.entry("CA", "CAD"),
        Map.entry("CH", "CHF"),
        Map.entry("JP", "JPY")
    );

    private CountryCurrencyMapper() {
    }

    public static String currencyForCountry(String countryCode) {
        if (countryCode == null || countryCode.isBlank()) {
            return "EUR";
        }
        return COUNTRY_TO_CURRENCY.getOrDefault(countryCode.toUpperCase(), "EUR");
    }
}
