package com.projet.service;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Tests du service de taux de change")
class ExchangeRateServiceTest {

    private static final String EUR = "EUR";
    private static final String USD = "USD";
    private static final String GBP = "GBP";
    private static final String CHF = "CHF";

    private ExchangeRateService exchangeService;

    @BeforeEach
    void setUp() {
        exchangeService = new ExchangeRateServiceImpl();
    }

    @Test
    @DisplayName("Doit récupérer des taux de change valides")
    void shouldGetExchangeRates() {
        Map<String, Double> rates = exchangeService.getExchangeRates(EUR, "USD,GBP,CHF");

        assertAll(
            () -> assertNotNull(rates, "Les taux ne doivent pas être null"),
            () -> assertFalse(rates.isEmpty(), "Au moins un taux doit être retourné"),
            () -> assertTrue(
                rates.containsKey(USD) || rates.containsKey(GBP) || rates.containsKey(CHF),
                "Au moins une des devises demandées doit être présente"
            )
        );

        for (Double rate : rates.values()) {
            assertTrue(rate > 0, "Les taux de change doivent être positifs");
        }
    }

    @Test
    @DisplayName("Doit convertir correctement un montant")
    void shouldConvertAmount() {
        double amount = 100.0;

        double converted = exchangeService.convertAmount(amount, EUR, USD);

        assertAll(
            () -> assertTrue(converted > 0, "Le montant converti doit être positif"),
            () -> assertTrue(converted > amount, "Le montant converti devrait être supérieur à 100"),
            () -> assertTrue(
                converted > 100 && converted < 140,
                "La conversion 100 EUR -> USD devrait rester dans une plage raisonnable"
            )
        );
    }

    @Test
    @DisplayName("Doit retourner le même montant pour une conversion dans la même devise")
    void shouldReturnSameAmountForSameCurrency() {
        double amount = 50.0;

        double converted = exchangeService.convertAmount(amount, EUR, EUR);

        assertEquals(amount, converted, 1e-6);
    }

    @Test
    @DisplayName("Doit gérer les erreurs avec un fallback")
    void shouldFallbackGracefullyOnError() {
        assertDoesNotThrow(() -> {
            Map<String, Double> rates = exchangeService.getExchangeRates("XXX", "USD,GBP");

            assertNotNull(rates, "Un fallback doit être retourné même avec une devise invalide");
        });
    }

    @Test
    @DisplayName("Doit conserver un ratio cohérent entre plusieurs conversions")
    void shouldKeepConversionConsistency() {
        double amount1 = 100.0;
        double amount2 = 200.0;

        double result1 = exchangeService.convertAmount(amount1, EUR, USD);
        double result2 = exchangeService.convertAmount(amount2, EUR, USD);

        double ratio1 = result1 / amount1;
        double ratio2 = result2 / amount2;

        assertEquals(ratio1, ratio2, 0.01, "Les taux doivent rester cohérents entre conversions");
    }

    @Test
    @DisplayName("Doit lever une exception si une devise est nulle")
    void shouldRejectNullCurrency() {
        assertAll(
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> exchangeService.convertAmount(100, null, USD)
            ),
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> exchangeService.convertAmount(100, EUR, null)
            )
        );
    }

    @Test
    @DisplayName("Doit retourner des taux par défaut si aucune devise cible n'est fournie")
    void shouldReturnFallbackRatesWhenTargetsAreMissing() {
        Map<String, Double> rates = exchangeService.getExchangeRates(EUR, null);

        assertAll(
            () -> assertNotNull(rates, "Les taux ne doivent pas être null"),
            () -> assertFalse(rates.isEmpty(), "Les taux de fallback ne doivent pas être vides"),
            () -> assertTrue(rates.size() > 0, "Au moins quelques devises doivent être disponibles")
        );
    }

    @Test
    @DisplayName("Doit être insensible à la casse des devises")
    void shouldBeCaseInsensitive() {
        double amount = 100.0;

        double result1 = exchangeService.convertAmount(amount, "eur", "usd");
        double result2 = exchangeService.convertAmount(amount, "EUR", "USD");
        double result3 = exchangeService.convertAmount(amount, "Eur", "Usd");

        assertAll(
            () -> assertEquals(result1, result2, 1e-6),
            () -> assertEquals(result2, result3, 1e-6)
        );
    }
}