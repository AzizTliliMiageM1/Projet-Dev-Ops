package com.projet.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.projet.backend.domain.Abonnement;
import com.projet.repository.AbonnementRepository;

class JobSearchServiceTest {

    static class InMemoryRepo implements AbonnementRepository {
        private final List<Abonnement> data = new ArrayList<>();

        InMemoryRepo(List<Abonnement> initial) { data.addAll(initial); }

        @Override public List<Abonnement> findAll() { return new ArrayList<>(data); }
        @Override public void saveAll(List<Abonnement> abonnements) { data.clear(); data.addAll(abonnements); }
        @Override public java.util.Optional<Abonnement> findByUuid(String uuid) { return data.stream().filter(a->a.getId().equals(uuid)).findFirst(); }
        @Override public void save(Abonnement abonnement) { data.add(abonnement); }
        @Override public void delete(Abonnement abonnement) { data.remove(abonnement); }
        @Override public void deleteByUuid(String uuid) { data.removeIf(a->a.getId().equals(uuid)); }
    }

    private Abonnement a(String name, String categorie, double prix) {
        Abonnement ab = new Abonnement(name, LocalDate.now().minusMonths(6), LocalDate.now().plusMonths(6), prix, "client");
        ab.setCategorie(categorie);
        ab.setNotes("notes for " + name);
        return ab;
    }

    @Test
    void searchByCategory_returnsMatching() {
        List<Abonnement> list = List.of(
            a("Spotify","music",9.99),
            a("Netflix","video",12.99),
            a("Canal+","video",19.99)
        );

        JobSearchService svc = new JobSearchService(new InMemoryRepo(list));
        List<Abonnement> res = svc.searchByCategory("video");
        assertEquals(2, res.size());
    }

    @Test
    void searchByText_matchesNameAndNotes() {
        List<Abonnement> list = List.of(
            a("Spotify Premium","music",9.99),
            a("Local Newspaper","news",5.0)
        );

        JobSearchService svc = new JobSearchService(new InMemoryRepo(list));
        List<Abonnement> res = svc.searchByText("spotify");
        assertEquals(1, res.size());
    }

    @Test
    void searchByPriceRange_filtersCorrectly() {
        List<Abonnement> list = List.of(
            a("Cheap","other",1.0),
            a("Mid","other",10.0),
            a("Expensive","other",50.0)
        );

        JobSearchService svc = new JobSearchService(new InMemoryRepo(list));
        List<Abonnement> res = svc.searchByPriceRange(5.0, 20.0);
        assertEquals(1, res.size());
    }

    @Test
    void combinedSearch_works() {
        List<Abonnement> list = List.of(
            a("Spotify","music",9.99),
            a("Spotify Family","music",15.99),
            a("Business Tool","productivity",29.99)
        );

        JobSearchService svc = new JobSearchService(new InMemoryRepo(list));
        List<Abonnement> res = svc.search("music","spotify",10.0,20.0);
        // should match only Spotify Family
        assertEquals(1, res.size());
    }
}
