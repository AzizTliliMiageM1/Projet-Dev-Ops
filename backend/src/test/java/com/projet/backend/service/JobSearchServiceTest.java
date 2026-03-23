package com.projet.backend.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.projet.backend.domain.Abonnement;
import com.projet.repository.AbonnementRepository;

@DisplayName("Tests du service de recherche d'abonnements")
class JobSearchServiceTest {

    private static final LocalDate FIXED_DATE = LocalDate.of(2026, 3, 23);

    static class InMemoryRepo implements AbonnementRepository {
        private final List<Abonnement> data = new ArrayList<>();

        InMemoryRepo(List<Abonnement> initial) {
            data.addAll(initial);
        }

        @Override
        public List<Abonnement> findAll() {
            return new ArrayList<>(data);
        }

        @Override
        public void saveAll(List<Abonnement> abonnements) {
            data.clear();
            data.addAll(abonnements);
        }

        @Override
        public Optional<Abonnement> findByUuid(String uuid) {
            return data.stream()
                .filter(a -> a.getId().equals(uuid))
                .findFirst();
        }

        @Override
        public void save(Abonnement abonnement) {
            data.add(abonnement);
        }

        @Override
        public void delete(Abonnement abonnement) {
            data.remove(abonnement);
        }

        @Override
        public void deleteByUuid(String uuid) {
            data.removeIf(a -> a.getId().equals(uuid));
        }
    }

    private JobSearchService service;

    @BeforeEach
    void setUp() {
        service = null;
    }

    @Test
    @DisplayName("La recherche par catégorie doit retourner les abonnements correspondants")
    void shouldReturnMatchingSubscriptionsByCategory() {
        service = createService(
            subscription("Spotify", "music", 9.99),
            subscription("Netflix", "video", 12.99),
            subscription("Canal+", "video", 19.99)
        );

        List<Abonnement> result = service.searchByCategory("video");

        assertAll(
            () -> assertEquals(2, result.size()),
            () -> assertEquals("Netflix", result.get(0).getNomService()),
            () -> assertEquals("Canal+", result.get(1).getNomService())
        );
    }

    @Test
    @DisplayName("La recherche textuelle doit trouver dans le nom et les notes")
    void shouldMatchByTextInNameAndNotes() {
        service = createService(
            subscription("Spotify Premium", "music", 9.99),
            subscription("Local Newspaper", "news", 5.0)
        );

        List<Abonnement> result = service.searchByText("spotify");

        assertAll(
            () -> assertEquals(1, result.size()),
            () -> assertEquals("Spotify Premium", result.get(0).getNomService())
        );
    }

    @Test
    @DisplayName("La recherche par plage de prix doit filtrer correctement")
    void shouldFilterByPriceRange() {
        service = createService(
            subscription("Cheap", "other", 1.0),
            subscription("Mid", "other", 10.0),
            subscription("Expensive", "other", 50.0)
        );

        List<Abonnement> result = service.searchByPriceRange(5.0, 20.0);

        assertAll(
            () -> assertEquals(1, result.size()),
            () -> assertEquals("Mid", result.get(0).getNomService())
        );
    }

    @Test
    @DisplayName("La recherche combinée doit retourner uniquement les abonnements correspondants à tous les critères")
    void shouldSupportCombinedSearch() {
        service = createService(
            subscription("Spotify", "music", 9.99),
            subscription("Spotify Family", "music", 15.99),
            subscription("Business Tool", "productivity", 29.99)
        );

        List<Abonnement> result = service.search("music", "spotify", 10.0, 20.0);

        assertAll(
            () -> assertEquals(1, result.size()),
            () -> assertEquals("Spotify Family", result.get(0).getNomService())
        );
    }

    // Helpers

    private JobSearchService createService(Abonnement... abonnements) {
        return new JobSearchService(new InMemoryRepo(List.of(abonnements)));
    }

    private Abonnement subscription(String name, String category, double price) {
        Abonnement abonnement = new Abonnement(
            name,
            FIXED_DATE.minusMonths(6),
            FIXED_DATE.plusMonths(6),
            price,
            "client"
        );
        abonnement.setCategorie(category);
        abonnement.setNotes("notes for " + name);
        return abonnement;
    }
}