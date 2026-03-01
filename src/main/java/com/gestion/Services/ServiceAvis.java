package com.gestion.Services;

import com.gestion.entities.Avis;
import com.gestion.utils.DB;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ServiceAvis {


    public int add(Avis a) throws SQLException {
        String sql = "INSERT INTO avis (id_quiz, commentaire, note, date_creation) VALUES (?, ?, ?, ?)";

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, a.getIdQuiz());
            ps.setString(2, a.getCommentaire());
            ps.setInt(3, a.getNote());
            ps.setDate(4, Date.valueOf(a.getDateCreation() != null ? a.getDateCreation() : LocalDate.now()));

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
            return 0;
        }
    }


    public List<Avis> getAll() throws SQLException {
        String sql = "SELECT id_avis, id_quiz, commentaire, note, date_creation FROM avis ORDER BY id_avis DESC";
        List<Avis> list = new ArrayList<>();

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapAvis(rs));
            }
        }
        return list;
    }


    public Avis getById(int idAvis) throws SQLException {
        String sql = "SELECT id_avis, id_quiz, commentaire, note, date_creation FROM avis WHERE id_avis = ?";

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idAvis);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapAvis(rs);
            }
        }
        return null;
    }

    public List<Avis> getByQuizId(int idQuiz) throws SQLException {
        String sql = "SELECT id_avis, id_quiz, commentaire, note, date_creation FROM avis WHERE id_quiz = ? ORDER BY id_avis DESC";
        List<Avis> list = new ArrayList<>();

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idQuiz);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapAvis(rs));
                }
            }
        }
        return list;
    }

    public boolean update(Avis a) throws SQLException {
        String sql = "UPDATE avis SET id_quiz = ?, commentaire = ?, note = ?, date_creation = ? WHERE id_avis = ?";

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, a.getIdQuiz());
            ps.setString(2, a.getCommentaire());
            ps.setInt(3, a.getNote());
            ps.setDate(4, Date.valueOf(a.getDateCreation() != null ? a.getDateCreation() : LocalDate.now()));
            ps.setInt(5, a.getIdAvis());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int idAvis) throws SQLException {
        String sql = "DELETE FROM avis WHERE id_avis = ?";

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idAvis);
            return ps.executeUpdate() > 0;
        }
    }

    private Avis mapAvis(ResultSet rs) throws SQLException {
        int idAvis = rs.getInt("id_avis");
        int idQuiz = rs.getInt("id_quiz");
        String commentaire = rs.getString("commentaire");
        int note = rs.getInt("note");
        Date d = rs.getDate("date_creation");
        LocalDate dateCreation = (d != null) ? d.toLocalDate() : null;

        return new Avis(idAvis, idQuiz, commentaire, note, dateCreation);
    }
}
