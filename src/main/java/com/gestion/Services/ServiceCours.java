package com.gestion.Services;

import com.gestion.entities.Cours;
import com.gestion.utils.DB;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ServiceCours {

    public int add(Cours c) throws SQLException {
        String sql = "INSERT INTO cours(nom_cours, contenu, description, date_creation) VALUES (?, ?, ?, ?)";
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, c.getNomCours());
            ps.setString(2, c.getContenu());
            ps.setString(3, c.getDescription());
            ps.setDate(4, Date.valueOf(c.getDateCreation() != null ? c.getDateCreation() : LocalDate.now()));

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
            return 0;
        }
    }

    public List<Cours> getAll() throws SQLException {
        String sql = "SELECT id_cours, nom_cours, contenu, description, date_creation FROM cours ORDER BY id_cours DESC";
        List<Cours> list = new ArrayList<>();

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapCours(rs));
            }
        }
        return list;
    }

    public Cours getById(int idCours) throws SQLException {
        String sql = "SELECT id_cours, nom_cours, contenu, description, date_creation FROM cours WHERE id_cours = ?";
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idCours);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapCours(rs);
            }
        }
        return null;
    }

    public boolean update(Cours c) throws SQLException {
        String sql = "UPDATE cours SET nom_cours=?, contenu=?, description=?, date_creation=? WHERE id_cours=?";
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, c.getNomCours());
            ps.setString(2, c.getContenu());
            ps.setString(3, c.getDescription());
            ps.setDate(4, Date.valueOf(c.getDateCreation() != null ? c.getDateCreation() : LocalDate.now()));
            ps.setInt(5, c.getIdCours());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int idCours) throws SQLException {
        String sql = "DELETE FROM cours WHERE id_cours=?";
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idCours);
            return ps.executeUpdate() > 0;
        }
    }

    private Cours mapCours(ResultSet rs) throws SQLException {
        int id = rs.getInt("id_cours");
        String nom = rs.getString("nom_cours");
        String contenu = rs.getString("contenu");
        String desc = rs.getString("description");
        Date d = rs.getDate("date_creation");
        LocalDate dateCreation = (d != null) ? d.toLocalDate() : null;

        return new Cours(id, nom, contenu, desc, dateCreation);
    }
}
