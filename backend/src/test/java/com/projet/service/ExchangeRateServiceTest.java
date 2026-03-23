package com.projet.service;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests unitaires du service ExchangeRateService.
 * 
 * Tests :
 * 1. Récupération des taux de change
 * 2. Conversion de montants simples
 * 3. Fallback en cas d'API indisponible
 * 4. Gestion des conversions entre devises identiques
 */
@DisplayName("ExchangeRateService Tests")
public class ExchangeRateServiceTest {

    private final ExchangeRateService exchangeService = new ExchangeRateServiceImpl();

    @Test
    @DisplayName("Devrait récupérer les taux de change depuis l'API")
    public void testGetExchangeRates() {
        // Arrange
        String baseCurrency = "EUR";
        String targetCurrencies = "USD,GBP,CHF";
        
        // Act
        Map<String, Double> rates = exchangeService.getExchangeRates(baseCurrency, targetCurrencies);
        
        // Assert
        assertNotNull(rates, "Les taux ne doivent pas être null");
        assertFalse(rates.isEmpty(), "Au moins un taux doit être retourné");
        
        // Vérifier que les devises demandées sont présentes (ou le fallback)
        assertTrue(
            rates.containsKey("USD") || rates.containsKey("GBP") || rates.containsKey("CHF"),
            "Au moins une des devises demandées doit être présente"
        );
        
        // Tous les taux doivent être positifs
        for (Double rate : rates.values()) {
            assertTrue(rate > 0, "Les taux de change doivent être positifs");
        }
    }

    @Test
    @DisplayName("Devrait convertir correctement un montant")
    public void testConvertAmount() {
        // Arrange
        double amount = 100.0; // EUR
        String fromCurrency = "EUR";
        String toCurrency = "USD";
        
        // Act
        double converted = exchangeService.convertAmount(amount, fromCurrency, toCurrency);
        
        // Assert
        assertTrue(converted > 0, "Le montant converti doit être positif");
        assertTrue(converted > amount * 1.0, "USD devrait être supérieur à EUR (approx)");
        
        // Vérifier que le résultat est raisonnable : entre 1.0 et 1.2 USD pour 1 EUR
        assertTrue(
            converted > 100 && converted < 140,
            "La conversion 100 EUR -> USD devrait être entre 100 et 140"
        );
    }

    @Test
    @DisplayName("Devrait retourner le même montant pour conversion identical")
    public void testConvertAmountSameCurrency() {
        // Arrange
        double amount = 50.0;
        String currency = "EUR";
        
        // Act
        double converted = exchangeService.convertAmount(amount, currency, currency);
        
        // Assert
        assertEquals(amount, converted, "Convertir dans la même devise doit retourner le même montant");
    }

    @Test
    @DisplayName("Devrait gérer les erreurs gracieusement (fallback)")
    public void testFallbackOnError() {
        // Arrange - Utiliser des devises invalides pour forcer le fallback
        String baseCurrency = "XXX"; // Devise invalide
        String targetCurrencies = "USD,GBP";
        
        // Act - Ne doit pas lever d'exception
        assertDoesNotThrow(
            () -> {
                Map<String, Double> rates = exchangeService.getExchangeRates(baseCurrency, targetCurrencies);
                assertNotNull(rates, "Doit retourner un fallback même avec devise invalide");
            },
            "Le service doit gérer les erreurs gracieusement"
        );
    }

    @Test
    @DisplayName("Devrait convertir plusieurs montants avec cohérence")
    public void testConversionConsistency() {
        // Arrange
        double amount1 = 100.0;
        double amount2 = 200.0;
        
        // Act
        double result1 = exchangeService.convertAmount(amount1, "EUR", "USD");
        double result2 = exchangeService.convertAmount(amount2, "EUR", "USD");
        
        // Assert
        // Le ratio doit être préservé
        double ratio1 = result1 / amount1;
        double ratio2 = result2 / amount2;
        
        // Les taux doivent être cohérents (différence < 1%)
        assertEquals(ratio1, ratio2, 0.01, "Les taux doivent être cohérents entre conversions");
    }

    @Test
    @DisplayName("Devrait lever une exception pour devise null")
    public void testNullCurrencyHandling() {
        // Arrange, Act, Assert
        assertThrows(
            IllegalArgumentException.class,
            () -> exchangeService.convertAmount(100, null, "USD"),
            "Doit lever une exception pour devise source nulle"
        );
        
        assertThrows(
            IllegalArgumentException.class,
            () -> exchangeService.convertAmount(100, "EUR", null),
            "Doit lever une exception pour devise cible nulle"
        );
    }

    @Test
    @DisplayName("Devrait récupérer les taux par défaut si API indisponible")
    public void testFallbackRates() {
        // Arrange - Service avec API qui sera en fallback
        ExchangeRateService service = new ExchangeRateServiceImpl();
        
        // Act
        Map<String, Double> rates = service.getExchangeRates("EUR", null);
        
        // Assert
        assertNotNull(rates, "Doit retourner des taux même sans spécification");
        assertFalse(rates.isEmpty(), "Les taux de fallback doivent être retournés");
        
        // Vérifier que les devises principales sont présentes
        assertTrue(
            rates.size() > 0,
            "Au moins quelques devises par défaut doivent être disponibles"
        );
    }

    @Test
    @DisplayName("Devrait gérer les devises en majuscules et minuscules")
    public void testCaseInsensitivity() {
        // Arrange
        double amount = 100.0;
        
        // Act - Tester avec différentes casses
        double result1 = exchangeService.convertAmount(amount, "eur", "usd");
        double result2 = exchangeService.convertAmount(amount, "EUR", "USD");
        double result3 = exchangeService.convertAmount(amount, "Eur", "Usd");
        
        // Assert - Tous les résultats doivent être identiques
        assertEquals(result1, result2, "Conversion doit être insensible à la casse");
        assertEquals(result2, result3, "Conversion doit être insensible à la casse");
    }
}
