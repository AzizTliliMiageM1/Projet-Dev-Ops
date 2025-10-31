package com.projet.repository;

import com.example.abonnement.Abonnement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository minimal utilisant H2 (ou autre JDBC) pour stocker les abonnements.
 * Important: pour rester compatible avec l'API existante qui utilise des indices 0-based
 * comme identifiants, les méthodes findById/delete/put utilisent l'index comme OFFSET
 * dans les requêtes ORDER BY id.
 */
public class DatabaseAbonnementRepository implements AbonnementRepository {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseAbonnementRepository.class);
    private final String jdbcUrl;

    public DatabaseAbonnementRepository(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
        try (Connection c = getConnection(); Statement s = c.createStatement()) {
            s.execute("CREATE TABLE IF NOT EXISTS abonnements ("
                    + "id IDENTITY PRIMARY KEY,"
                    + "nom_service VARCHAR(255),"
                    + "date_debut DATE,"
                    + "date_fin DATE,"
                    + "prix_mensuel DOUBLE,"
                    + "client_name VARCHAR(255),"
                    + "derniere_utilisation DATE,"
                    + "categorie VARCHAR(255)"
                    + ")");
        } catch (SQLException e) {
            logger.error("Erreur initialisation DB: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl);
    }

    @Override
    public List<Abonnement> findAll() {
        List<Abonnement> out = new ArrayList<>();
        String sql = "SELECT nom_service, date_debut, date_fin, prix_mensuel, client_name, derniere_utilisation, categorie FROM abonnements ORDER BY id";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String nom = rs.getString(1);
                Date dDeb = rs.getDate(2);
                Date dFin = rs.getDate(3);
                double prix = rs.getDouble(4);
                String client = rs.getString(5);
                Date last = rs.getDate(6);
                String cat = rs.getString(7);
                LocalDate ldDeb = dDeb != null ? dDeb.toLocalDate() : LocalDate.now();
                LocalDate ldFin = dFin != null ? dFin.toLocalDate() : LocalDate.now();
                LocalDate ldLast = last != null ? last.toLocalDate() : null;
                Abonnement a = new Abonnement(nom, ldDeb, ldFin, prix, client, ldLast, cat != null ? cat : "Non classé");
                out.add(a);
            }
        } catch (SQLException e) {
            logger.error("Erreur findAll DB: {}", e.getMessage(), e);
        }
        logger.info("{} abonnements chargés depuis la base", out.size());
        return out;
    }

    @Override
    public void saveAll(List<Abonnement> abonnements) {
        String deleteSql = "DELETE FROM abonnements";
        String insertSql = "INSERT INTO abonnements (nom_service, date_debut, date_fin, prix_mensuel, client_name, derniere_utilisation, categorie) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = getConnection()) {
            c.setAutoCommit(false);
            try (Statement st = c.createStatement()) {
                st.execute(deleteSql);
            }
            try (PreparedStatement ps = c.prepareStatement(insertSql)) {
                for (Abonnement a : abonnements) {
                    ps.setString(1, a.getNomService());
                    ps.setDate(2, a.getDateDebut() != null ? Date.valueOf(a.getDateDebut()) : null);
                    ps.setDate(3, a.getDateFin() != null ? Date.valueOf(a.getDateFin()) : null);
                    ps.setDouble(4, a.getPrixMensuel());
                    ps.setString(5, a.getClientName());
                    ps.setDate(6, a.getDerniereUtilisation() != null ? Date.valueOf(a.getDerniereUtilisation()) : null);
                    ps.setString(7, a.getCategorie());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            c.commit();
            logger.info("{} abonnements sauvegardés dans la base", abonnements.size());
        } catch (SQLException e) {
            logger.error("Erreur saveAll DB: {}", e.getMessage(), e);
        }
    }

    @Override
    public Optional<Abonnement> findById(int id) {
        // id here is treated as offset in ordered set
        String sql = "SELECT nom_service, date_debut, date_fin, prix_mensuel, client_name, derniere_utilisation, categorie FROM abonnements ORDER BY id LIMIT 1 OFFSET ?";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String nom = rs.getString(1);
                    Date dDeb = rs.getDate(2);
                    Date dFin = rs.getDate(3);
                    double prix = rs.getDouble(4);
                    String client = rs.getString(5);
                    Date last = rs.getDate(6);
                    String cat = rs.getString(7);
                    LocalDate ldDeb = dDeb != null ? dDeb.toLocalDate() : LocalDate.now();
                    LocalDate ldFin = dFin != null ? dFin.toLocalDate() : LocalDate.now();
                    LocalDate ldLast = last != null ? last.toLocalDate() : null;
                    Abonnement a = new Abonnement(nom, ldDeb, ldFin, prix, client, ldLast, cat != null ? cat : "Non classé");
                    return Optional.of(a);
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur findById DB: {}", e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public void save(Abonnement abonnement) {
        String insertSql = "INSERT INTO abonnements (nom_service, date_debut, date_fin, prix_mensuel, client_name, derniere_utilisation, categorie) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(insertSql)) {
            ps.setString(1, abonnement.getNomService());
            ps.setDate(2, abonnement.getDateDebut() != null ? Date.valueOf(abonnement.getDateDebut()) : null);
            ps.setDate(3, abonnement.getDateFin() != null ? Date.valueOf(abonnement.getDateFin()) : null);
            ps.setDouble(4, abonnement.getPrixMensuel());
            ps.setString(5, abonnement.getClientName());
            ps.setDate(6, abonnement.getDerniereUtilisation() != null ? Date.valueOf(abonnement.getDerniereUtilisation()) : null);
            ps.setString(7, abonnement.getCategorie());
            ps.executeUpdate();
            logger.info("Abonnement inséré: {} - {}", abonnement.getClientName(), abonnement.getNomService());
        } catch (SQLException e) {
            logger.error("Erreur insert DB: {}", e.getMessage(), e);
        }
    }

    @Override
    public void delete(Abonnement abonnement) {
        // find its offset in the ordered list and delete by id
        List<Abonnement> all = findAll();
        int idx = -1;
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).equals(abonnement)) { idx = i; break; }
        }
        if (idx == -1) return;
        // get db id at offset
        String idSql = "SELECT id FROM abonnements ORDER BY id LIMIT 1 OFFSET ?";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(idSql)) {
            ps.setInt(1, idx);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long dbId = rs.getLong(1);
                    try (PreparedStatement del = c.prepareStatement("DELETE FROM abonnements WHERE id = ?")) {
                        del.setLong(1, dbId);
                        del.executeUpdate();
                        logger.info("Abonnement supprimé id={}", dbId);
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur delete DB: {}", e.getMessage(), e);
        }
    }
}
