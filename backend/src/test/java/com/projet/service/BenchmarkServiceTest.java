package com.projet.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.projet.backend.domain.BenchmarkResult;

@DisplayName("Tests du service de benchmark")
class BenchmarkServiceTest {

    private static final String NETFLIX = "Netflix";
    private static final double NETFLIX_AVERAGE_PRICE = 15.99;

    private BenchmarkService benchmarkService;

    @BeforeEach
    void setUp() {
        benchmarkService = new BenchmarkServiceImpl();
    }

    @Test
    @DisplayName("Doit identifier un abonnement au bon prix comme OPTIMIZED")
    void shouldIdentifyOptimizedSubscription() {
        BenchmarkResult result = benchmarkService.benchmark("test-123", NETFLIX, NETFLIX_AVERAGE_PRICE);

        assertAll(
            () -> assertNotNull(result),
            () -> assertEquals(NETFLIX, result.getServiceName()),
            () -> assertEquals(NETFLIX_AVERAGE_PRICE, result.getUserPrice()),
            () -> assertEquals("OPTIMIZED", result.getStatus()),
            () -> assertNotNull(result.getRecommendation()),
            () -> assertTrue(result.getPriceDeviation() < 15)
        );
    }

    @Test
    @DisplayName("Doit identifier un abonnement trop cher comme OVERPRICED")
    void shouldIdentifyOverpricedSubscription() {
        BenchmarkResult result = benchmarkService.benchmark("test-456", NETFLIX, 29.99);

        assertAll(
            () -> assertNotNull(result),
            () -> assertEquals("OVERPRICED", result.getStatus()),
            () -> assertTrue(result.getPriceDeviation() > 0),
            () -> assertTrue(
                result.getRecommendation().contains("cher")
                    || result.getRecommendation().contains("Beaucoup")
            )
        );
    }

    @Test
    @DisplayName("Doit identifier un abonnement peu cher comme UNDERPRICED")
    void shouldIdentifyUnderpricedSubscription() {
        BenchmarkResult result = benchmarkService.benchmark("test-789", NETFLIX, 5.00);

        assertAll(
            () -> assertNotNull(result),
            () -> assertEquals("UNDERPRICED", result.getStatus()),
            () -> assertTrue(result.getPriceDeviation() < 0),
            () -> assertTrue(result.getRecommendation().contains("Excellent"))
        );
    }

    @Test
    @DisplayName("Doit calculer correctement la déviation de prix")
    void shouldCalculatePriceDeviationCorrectly() {
        BenchmarkResult result = benchmarkService.benchmark("id1", NETFLIX, 20.0);

        double expectedDeviation = ((20.0 - NETFLIX_AVERAGE_PRICE) / NETFLIX_AVERAGE_PRICE) * 100;

        assertEquals(expectedDeviation, result.getPriceDeviation(), 0.5);
    }

    @Test
    @DisplayName("Doit lever une exception pour un identifiant vide")
    void shouldRejectEmptySubscriptionId() {
        assertThrows(
            IllegalArgumentException.class,
            () -> benchmarkService.benchmark("", NETFLIX, NETFLIX_AVERAGE_PRICE)
        );
    }

    @Test
    @DisplayName("Doit lever une exception pour un nom de service vide")
    void shouldRejectEmptyServiceName() {
        assertThrows(
            IllegalArgumentException.class,
            () -> benchmarkService.benchmark("id123", "", NETFLIX_AVERAGE_PRICE)
        );
    }

    @Test
    @DisplayName("Doit lever une exception pour un prix négatif")
    void shouldRejectNegativePrice() {
        assertThrows(
            IllegalArgumentException.class,
            () -> benchmarkService.benchmark("id123", NETFLIX, -10.0)
        );
    }

    @Test
    @DisplayName("Doit retourner un ratio proche de 1 pour un prix moyen")
    void shouldReturnValidPriceRatio() {
        double ratio = benchmarkService.getPriceRatio(NETFLIX_AVERAGE_PRICE, NETFLIX);

        assertTrue(ratio > 0);
        assertTrue(Math.abs(ratio - 1.0) < 0.1);
    }

    @Test
    @DisplayName("Doit gérer le ratio des services ayant une offre gratuite")
    void shouldHandlePriceRatioWithFreeOption() {
        double ratio = benchmarkService.getPriceRatio(11.99, "Spotify");

        assertTrue(ratio > 0);
    }

    @Test
    @DisplayName("Doit retourner un ratio positif pour un service inconnu")
    void shouldReturnPositiveRatioForUnknownService() {
        double ratio = benchmarkService.getPriceRatio(50.0, "UnknownService123XYZ");

        assertTrue(ratio > 0);
    }

    @Test
    @DisplayName("Doit accepter les services avec espaces dans le nom")
    void shouldSupportServiceNamesWithSpaces() {
        assertDoesNotThrow(() -> {
            BenchmarkResult result =
                benchmarkService.benchmark("id1", "Amazon Prime Video", 14.99);
            assertNotNull(result);
        });
    }

    @Test
    @DisplayName("Doit produire des recommandations différentes selon le statut")
    void shouldGenerateDifferentRecommendationsByStatus() {
        BenchmarkResult overpriced = benchmarkService.benchmark("id1", NETFLIX, 30.0);
        BenchmarkResult optimized = benchmarkService.benchmark("id2", NETFLIX, NETFLIX_AVERAGE_PRICE);
        BenchmarkResult underpriced = benchmarkService.benchmark("id3", NETFLIX, 5.0);

        assertAll(
            () -> assertNotEquals(overpriced.getRecommendation(), optimized.getRecommendation()),
            () -> assertNotEquals(optimized.getRecommendation(), underpriced.getRecommendation())
        );
    }

    @Test
    @DisplayName("Doit inclure les données de marché dans le résultat")
    void shouldIncludeMarketDataInResult() {
        BenchmarkResult result = benchmarkService.benchmark("id123", NETFLIX, NETFLIX_AVERAGE_PRICE);

        assertAll(
            () -> assertEquals("id123", result.getAbonnementId()),
            () -> assertEquals(NETFLIX, result.getServiceName()),
            () -> assertEquals(NETFLIX_AVERAGE_PRICE, result.getUserPrice()),
            () -> assertTrue(result.getMarketAveragePrice() > 0),
            () -> assertTrue(result.getMarketMinPrice() >= 0),
            () -> assertTrue(result.getMarketMaxPrice() > 0),
            () -> assertTrue(result.getMarketMinPrice() <= result.getMarketMaxPrice()),
            () -> assertNotNull(result.getRegion()),
            () -> assertTrue(result.getTimestamp() > 0)
        );
    }

    @Test
    @DisplayName("Doit gérer les abonnements gratuits")
    void shouldHandleFreeSubscription() {
        assertDoesNotThrow(() -> {
            BenchmarkResult result = benchmarkService.benchmark("id1", NETFLIX, 0.0);

            assertAll(
                () -> assertNotNull(result),
                () -> assertEquals("UNDERPRICED", result.getStatus())
            );
        });
    }

    @Test
    @DisplayName("Doit gérer les abonnements très chers")
    void shouldHandleVeryExpensiveSubscription() {
        BenchmarkResult result =
            benchmarkService.benchmark("id1", "Adobe Creative Cloud", 100.0);

        assertAll(
            () -> assertNotNull(result),
            () -> assertTrue(
                "OVERPRICED".equals(result.getStatus())
                    || Math.abs(result.getPriceDeviation()) < 30
            )
        );
    }
}