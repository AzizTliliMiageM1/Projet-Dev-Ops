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

import com.example.abonnement.Abonnement;

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
            + "id VARCHAR(36) PRIMARY KEY,"
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
                LocalDate ldDeb = dDeb != null ? dDeb.toLocalDate() : LocalDate.now();
                LocalDate ldFin = dFin != null ? dFin.toLocalDate() : LocalDate.now();
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
    public Optional<Abonnement> findById(int id) {
        // id here is treated as offset in ordered set
        String sql = "SELECT id, nom_service, date_debut, date_fin, prix_mensuel, client_name, derniere_utilisation, categorie FROM abonnements ORDER BY id LIMIT 1 OFFSET ?";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String uuid = rs.getString(1);
                    String nom = rs.getString(2);
                    Date dDeb = rs.getDate(3);
                    Date dFin = rs.getDate(4);
                    double prix = rs.getDouble(5);
                    String client = rs.getString(6);
                    Date last = rs.getDate(7);
                    String cat = rs.getString(8);
                    LocalDate ldDeb = dDeb != null ? dDeb.toLocalDate() : LocalDate.now();
                    LocalDate ldFin = dFin != null ? dFin.toLocalDate() : LocalDate.now();
                    LocalDate ldLast = last != null ? last.toLocalDate() : null;
                    Abonnement a = new Abonnement(uuid, nom, ldDeb, ldFin, prix, client, ldLast, cat != null ? cat : "Non classé");
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
        String insertSql = "INSERT INTO abonnements (id, nom_service, date_debut, date_fin, prix_mensuel, client_name, derniere_utilisation, categorie) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
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
                    String uuid = rs.getString(1);
                    try (PreparedStatement del = c.prepareStatement("DELETE FROM abonnements WHERE id = ?")) {
                        del.setString(1, uuid);
                        del.executeUpdate();
                        logger.info("Abonnement supprimé id={}", uuid);
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur delete DB: {}", e.getMessage(), e);
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
                    LocalDate ldDeb = dDeb != null ? dDeb.toLocalDate() : LocalDate.now();
                    LocalDate ldFin = dFin != null ? dFin.toLocalDate() : LocalDate.now();
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
