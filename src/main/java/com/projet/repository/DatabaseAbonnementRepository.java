package com.projet.repository;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.projet.backend.domain.Abonnement;

/**
 * Implémentation JDBC simple pour H2 (ou autre via JDBC URL) de AbonnementRepository.
 * Utilise une table `abonnements` avec une colonne id (UUID stockée en texte).
 */
public class DatabaseAbonnementRepository implements AbonnementRepository {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseAbonnementRepository.class);
    private final String jdbcUrl;
    private final String username;
    private final String password;
    public DatabaseAbonnementRepository() {
        this(System.getenv("JDBC_URL") != null ? System.getenv("JDBC_URL") : "jdbc:h2:./data/abonnements-db",
             System.getenv("JDBC_USER"), System.getenv("JDBC_PASS"));
    }

    /**
     * Convenience constructor used by ApiServer when passing a single JDBC URL string.
     */
    public DatabaseAbonnementRepository(String jdbcUrl) {
        this(jdbcUrl, System.getenv("JDBC_USER"), System.getenv("JDBC_PASS"));
    }

    public DatabaseAbonnementRepository(String jdbcUrl, String username, String password) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
        try {
            ensureTable();
        } catch (SQLException e) {
            logger.error("Erreur lors de l'initialisation de la base: {}", e.getMessage(), e);
        }
    }

    private Connection getConnection() throws SQLException {
        if (username == null || username.isEmpty()) {
            return DriverManager.getConnection(jdbcUrl);
        }
        return DriverManager.getConnection(jdbcUrl, username, password);
    }

    private void ensureTable() throws SQLException {
        String ddl = "CREATE TABLE IF NOT EXISTS abonnements ("
                + "id VARCHAR(64) PRIMARY KEY,"
                + "nom_service VARCHAR(255),"
                + "date_debut DATE,"
                + "date_fin DATE,"
                + "prix_mensuel DOUBLE,"
                + "client_name VARCHAR(255),"
                + "derniere_utilisation DATE,"
                + "categorie VARCHAR(255)"
                + ")";
        try (Connection c = getConnection(); Statement st = c.createStatement()) {
            st.execute(ddl);
        }
    }

    @Override
    public List<Abonnement> findAll() {
        List<Abonnement> out = new ArrayList<>();
        String sql = "SELECT id, nom_service, date_debut, date_fin, prix_mensuel, client_name, derniere_utilisation, categorie FROM abonnements ORDER BY id";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String id = rs.getString(1);
                String nom = rs.getString(2);
                Date dDeb = rs.getDate(3);
                Date dFin = rs.getDate(4);
                double prix = rs.getDouble(5);
                String client = rs.getString(6);
                Date last = rs.getDate(7);
                String cat = rs.getString(8);
                LocalDate ldDeb = dDeb != null ? dDeb.toLocalDate() : null;
                LocalDate ldFin = dFin != null ? dFin.toLocalDate() : null;
                LocalDate ldLast = last != null ? last.toLocalDate() : null;
                Abonnement a = new Abonnement(id, nom, ldDeb, ldFin, prix, client, ldLast, cat != null ? cat : "Non classé");
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
        String insertSql = "INSERT INTO abonnements (id, nom_service, date_debut, date_fin, prix_mensuel, client_name, derniere_utilisation, categorie) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = getConnection()) {
            c.setAutoCommit(false);
            try (Statement st = c.createStatement()) {
                st.execute(deleteSql);
            }
            try (PreparedStatement ps = c.prepareStatement(insertSql)) {
                for (Abonnement a : abonnements) {
                    ps.setString(1, a.getId());
                    ps.setString(2, a.getNomService());
                    ps.setDate(3, a.getDateDebut() != null ? Date.valueOf(a.getDateDebut()) : null);
                    ps.setDate(4, a.getDateFin() != null ? Date.valueOf(a.getDateFin()) : null);
                    ps.setDouble(5, a.getPrixMensuel());
                    ps.setString(6, a.getClientName());
                    ps.setDate(7, a.getDerniereUtilisation() != null ? Date.valueOf(a.getDerniereUtilisation()) : null);
                    ps.setString(8, a.getCategorie());
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
    public Optional<Abonnement> findByUuid(String uuid) {
        String sql = "SELECT id, nom_service, date_debut, date_fin, prix_mensuel, client_name, derniere_utilisation, categorie FROM abonnements WHERE id = ?";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, uuid);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String id = rs.getString(1);
                    String nom = rs.getString(2);
                    Date dDeb = rs.getDate(3);
                    Date dFin = rs.getDate(4);
                    double prix = rs.getDouble(5);
                    String client = rs.getString(6);
                    Date last = rs.getDate(7);
                    String cat = rs.getString(8);
                    LocalDate ldDeb = dDeb != null ? dDeb.toLocalDate() : null;
                    LocalDate ldFin = dFin != null ? dFin.toLocalDate() : null;
                    LocalDate ldLast = last != null ? last.toLocalDate() : null;
                    Abonnement a = new Abonnement(id, nom, ldDeb, ldFin, prix, client, ldLast, cat != null ? cat : "Non classé");
                    return Optional.of(a);
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur findByUuid DB: {}", e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public void save(Abonnement abonnement) {
        String insertSql = "MERGE INTO abonnements (id, nom_service, date_debut, date_fin, prix_mensuel, client_name, derniere_utilisation, categorie) KEY(id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        // H2 supports MERGE; for other DBs this may need to be adjusted.
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(insertSql)) {
            ps.setString(1, abonnement.getId());
            ps.setString(2, abonnement.getNomService());
            ps.setDate(3, abonnement.getDateDebut() != null ? Date.valueOf(abonnement.getDateDebut()) : null);
            ps.setDate(4, abonnement.getDateFin() != null ? Date.valueOf(abonnement.getDateFin()) : null);
            ps.setDouble(5, abonnement.getPrixMensuel());
            ps.setString(6, abonnement.getClientName());
            ps.setDate(7, abonnement.getDerniereUtilisation() != null ? Date.valueOf(abonnement.getDerniereUtilisation()) : null);
            ps.setString(8, abonnement.getCategorie());
            ps.executeUpdate();
            logger.info("Abonnement inséré/mis à jour: {} - {}", abonnement.getClientName(), abonnement.getNomService());
        } catch (SQLException e) {
            logger.error("Erreur insert/update DB: {}", e.getMessage(), e);
        }
    }

    @Override
    public void delete(Abonnement abonnement) {
        if (abonnement == null || abonnement.getId() == null) return;
        deleteByUuid(abonnement.getId());
    }

    @Override
    public void deleteByUuid(String uuid) {
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement("DELETE FROM abonnements WHERE id = ?")) {
            ps.setString(1, uuid);
            ps.executeUpdate();
            logger.info("Abonnement supprimé uuid={}", uuid);
        } catch (SQLException e) {
            logger.error("Erreur deleteByUuid DB: {}", e.getMessage(), e);
        }
    }
}
