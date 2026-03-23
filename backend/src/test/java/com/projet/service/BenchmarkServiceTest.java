package com.projet.service;

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

/**
 * Tests unitaires du service de benchmark.
 * 
 * Tests :
 * 1. Benchmark d'un service connu (OPTIMIZED)
 * 2. Benchmark d'un service surtaxé (OVERPRICED)
 * 3. Benchmark d'un service sous-taxé (UNDERPRICED)
 * 4. Gestion des erreurs et validations
 * 5. Calcul du ratio de prix
 */
@DisplayName("BenchmarkService Tests")
public class BenchmarkServiceTest {

    private BenchmarkService benchmarkService;

    @BeforeEach
    public void setUp() {
        benchmarkService = new BenchmarkServiceImpl();
    }

    @Test
    @DisplayName("Devrait identifier un abonnement OPTIMIZED")
    public void testBenchmarkOptimized() {
        // Arrange
        String abonnementId = "test-123";
        String serviceName = "Netflix";
        double userPrice = 15.99; // Prix moyen Netflix
        
        // Act
        BenchmarkResult result = benchmarkService.benchmark(abonnementId, serviceName, userPrice);
        
        // Assert
        assertNotNull(result, "Le résultat ne doit pas être null");
        assertEquals("Netflix", result.getServiceName());
        assertEquals(userPrice, result.getUserPrice());
        assertEquals("OPTIMIZED", result.getStatus(), "Netflix à prix normal devrait être OPTIMIZED");
        assertNotNull(result.getRecommendation());
        assertTrue(result.getPriceDeviation() < 15, "Déviation devrait être faible pour prix optimisé");
    }

    @Test
    @DisplayName("Devrait identifier un abonnement OVERPRICED")
    public void testBenchmarkOverpriced() {
        // Arrange
        String abonnementId = "test-456";
        String serviceName = "Netflix";
        double userPrice = 29.99; // Beaucoup trop cher
        
        // Act
        BenchmarkResult result = benchmarkService.benchmark(abonnementId, serviceName, userPrice);
        
        // Assert
        assertNotNull(result);
        assertEquals("OVERPRICED", result.getStatus(), "Prix élevé devrait être OVERPRICED");
        assertTrue(result.getPriceDeviation() > 0, "Déviation devrait être positive pour prix élevé");
        assertTrue(result.getRecommendation().contains("cher") || result.getRecommendation().contains("Beaucoup"),
                  "Recommandation devrait mentionner le prix élevé");
    }

    @Test
    @DisplayName("Devrait identifier un abonnement UNDERPRICED")
    public void testBenchmarkUnderpriced() {
        // Arrange
        String abonnementId = "test-789";
        String serviceName = "Netflix";
        double userPrice = 5.00; // Très bon marché (ex: offre promo)
        
        // Act
        BenchmarkResult result = benchmarkService.benchmark(abonnementId, serviceName, userPrice);
        
        // Assert
        assertNotNull(result);
        assertEquals("UNDERPRICED", result.getStatus(), "Prix bas devrait être UNDERPRICED");
        assertTrue(result.getPriceDeviation() < 0, "Déviation devrait être négative pour prix bas");
        assertTrue(result.getRecommendation().contains("Excellent"), "Recommandation devrait être positive");
    }

    @Test
    @DisplayName("Devrait calculer correctement la déviation de prix")
    public void testPriceDeviation() {
        // Arrange - Netflix : mercado moyen 15.99€
        String serviceName = "Netflix";
        
        // Act - Prix = 20€ (25% au-dessus)
        BenchmarkResult result = benchmarkService.benchmark("id1", serviceName, 20.0);
        
        // Assert
        double expectedDeviation = ((20.0 - 15.99) / 15.99) * 100;
        assertEquals(expectedDeviation, result.getPriceDeviation(), 0.5,
                    "La déviation devrait être calculée correctement");
    }

    @Test
    @DisplayName("Devrait lever une exception pour abonnementId vide")
    public void testValidationEmptyId() {
        assertThrows(IllegalArgumentException.class,
            () -> benchmarkService.benchmark("", "Netflix", 15.99),
            "Doit lever exception pour ID vide");
    }

    @Test
    @DisplayName("Devrait lever une exception pour serviceName vide")
    public void testValidationEmptyServiceName() {
        assertThrows(IllegalArgumentException.class,
            () -> benchmarkService.benchmark("id123", "", 15.99),
            "Doit lever exception pour serviceName vide");
    }

    @Test
    @DisplayName("Devrait lever une exception pour prix négatif")
    public void testValidationNegativePrice() {
        assertThrows(IllegalArgumentException.class,
            () -> benchmarkService.benchmark("id123", "Netflix", -10.0),
            "Doit lever exception pour prix négatif");
    }

    @Test
    @DisplayName("Devrait retourner un ratio de prix valide")
    public void testGetPriceRatio() {
        // Arrange
        String serviceName = "Netflix";  // Utiliser Netflix qui a un prix minimum > 0
        double userPrice = 15.99; // Prix moyen Netflix
        
        // Act
        double ratio = benchmarkService.getPriceRatio(userPrice, serviceName);
        
        // Assert
        assertTrue(ratio > 0, "Ratio doit être positif");
        // Netflix moyen = 15.99, donc ratio doit être proche de 1.0
        assertTrue(Math.abs(ratio - 1.0) < 0.1, "Ratio pour prix moyen devrait être proche de 1.0");
    }

    @Test
    @DisplayName("Devrait gérer le ratio pour services avec prix min = 0")
    public void testGetPriceRatioWithFreeOption() {
        // Arrange - Spotify a une offre gratuite (prix min = 0)
        double userPrice = 11.99;
        String serviceName = "Spotify";
        
        // Act
        double ratio = benchmarkService.getPriceRatio(userPrice, serviceName);
        
        // Assert - Ratio doit être calculable même si prix min = 0
        assertTrue(ratio > 0, "Ratio doit être valide même avec offre gratuite");
    }

    @Test
    @DisplayName("Devrait retourner un ratio positif pour service inconnu")
    public void testGetPriceRatioUnknownService() {
        // Arrange
        double userPrice = 50.0;
        String unknownService = "UnknownService123XYZ"; // Service très probablement inexistant
        
        // Act
        double ratio = benchmarkService.getPriceRatio(userPrice, unknownService);
        
        // Assert - Ratio ne doit jamais craquer, doit retourner quelque chose de raisonnable
        assertTrue(ratio > 0, "Ratio doit être positif même pour service inconnu");
    }

    @Test
    @DisplayName("Devrait supporter les services avec espaces dans le nom")
    public void testBenchmarkServiceWithSpaces() {
        // Arrange
        String serviceName = "Amazon Prime Video"; // Contient des espaces
        
        // Act & Assert - Ne doit pas lever d'exception
        assertDoesNotThrow(() -> {
            BenchmarkResult result = benchmarkService.benchmark("id1", serviceName, 14.99);
            assertNotNull(result);
        });
    }

    @Test
    @DisplayName("Devrait générer des recommandations différentes selon le statut")
    public void testRecommendationsByStatus() {
        // Arrange
        String serviceName = "Netflix";
        
        // Act - Benchmark OVERPRICED
        BenchmarkResult overpriced = benchmarkService.benchmark("id1", serviceName, 30.0);
        
        // Act - Benchmark OPTIMIZED
        BenchmarkResult optimized = benchmarkService.benchmark("id2", serviceName, 15.99);
        
        // Act - Benchmark UNDERPRICED
        BenchmarkResult underpriced = benchmarkService.benchmark("id3", serviceName, 5.0);
        
        // Assert - Les recommandations doivent être différentes
        assertNotEquals(overpriced.getRecommendation(), optimized.getRecommendation(),
                       "Recommandations doivent différer selon le statut");
        assertNotEquals(optimized.getRecommendation(), underpriced.getRecommendation(),
                       "Recommandations doivent différer selon le statut");
    }

    @Test
    @DisplayName("Devrait inclure les données de marché dans le résultat")
    public void testBenchmarkResultContainsMarketData() {
        // Arrange
        String abonnementId = "id123";
        String serviceName = "Netflix";  // Utiliser Netflix qui a un prix minimum > 0
        double userPrice = 15.99;
        
        // Act
        BenchmarkResult result = benchmarkService.benchmark(abonnementId, serviceName, userPrice);
        
        // Assert
        assertEquals(abonnementId, result.getAbonnementId());
        assertEquals(serviceName, result.getServiceName());
        assertEquals(userPrice, result.getUserPrice());
        assertTrue(result.getMarketAveragePrice() > 0, "Prix moyen marché doit être positif");
        assertTrue(result.getMarketMinPrice() >= 0, "Prix min marché doit être ≥ 0");
        assertTrue(result.getMarketMaxPrice() > 0, "Prix max marché doit être positif");
        assertTrue(result.getMarketMinPrice() <= result.getMarketMaxPrice(), 
                  "Prix min doit être ≤ prix max");
        assertNotNull(result.getRegion());
        assertTrue(result.getTimestamp() > 0);
    }

    @Test
    @DisplayName("Devrait gérer les prix zéro (abonnements gratuits)")
    public void testBenchmarkFreeSubscription() {
        // Arrange
        String serviceName = "Netflix";
        double userPrice = 0.0; // Gratuit
        
        // Act & Assert
        assertDoesNotThrow(() -> {
            BenchmarkResult result = benchmarkService.benchmark("id1", serviceName, userPrice);
            assertNotNull(result);
            assertEquals("UNDERPRICED", result.getStatus(), "Gratuit doit être UNDERPRICED");
        });
    }

    @Test
    @DisplayName("Devrait gérer les prix très élevés")
    public void testBenchmarkExpensiveSubscription() {
        // Arrange
        String serviceName = "Adobe Creative Cloud";
        double userPrice = 100.0; // Très cher
        
        // Act
        BenchmarkResult result = benchmarkService.benchmark("id1", serviceName, userPrice);
        
        // Assert
        assertNotNull(result);
        assertTrue("OVERPRICED".equals(result.getStatus()) || Math.abs(result.getPriceDeviation()) < 30,
                  "Prix très élevé doit être géré");
    }
}
