package com.gestion.Services;

import com.gestion.utils.DB;
import com.gestion.entities.Quiz;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ServiceQuiz {

    private static final String SEP = ";";

    public int add(Quiz q) throws SQLException {
        String sql = "INSERT INTO quiz(id_cours, titre, liste_reponse, reponse_correct, score_quiz, date_creation) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, q.getIdCours());
            ps.setString(2, q.getTitre());
            ps.setString(3, listToDb(q.getListeDeReponse()));
            ps.setString(4, q.getReponseCorrect());
            ps.setInt(5, q.getScoreDeQuiz());
            ps.setDate(6, Date.valueOf(q.getDateCreation() != null ? q.getDateCreation() : LocalDate.now()));

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
            return 0;
        }
    }

    public List<Quiz> getAll() throws SQLException {
        String sql = "SELECT id_quiz, id_cours, titre, liste_reponse, reponse_correct, score_quiz, date_creation " +
                "FROM quiz ORDER BY id_quiz DESC";
        List<Quiz> list = new ArrayList<>();

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapQuiz(rs));
            }
        }
        return list;
    }

    public List<Quiz> getByCoursId(int idCours) throws SQLException {
        String sql = "SELECT id_quiz, id_cours, titre, liste_reponse, reponse_correct, score_quiz, date_creation " +
                "FROM quiz WHERE id_cours=? ORDER BY id_quiz DESC";
        List<Quiz> list = new ArrayList<>();

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idCours);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapQuiz(rs));
            }
        }
        return list;
    }

    public Quiz getById(int idQuiz) throws SQLException {
        String sql = "SELECT id_quiz, id_cours, titre, liste_reponse, reponse_correct, score_quiz, date_creation " +
                "FROM quiz WHERE id_quiz=?";
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idQuiz);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapQuiz(rs);
            }
        }
        return null;
    }

    public boolean update(Quiz q) throws SQLException {
        String sql = "UPDATE quiz SET id_cours=?, titre=?, liste_reponse=?, reponse_correct=?, score_quiz=?, date_creation=? " +
                "WHERE id_quiz=?";

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, q.getIdCours());
            ps.setString(2, q.getTitre());
            ps.setString(3, listToDb(q.getListeDeReponse()));
            ps.setString(4, q.getReponseCorrect());
            ps.setInt(5, q.getScoreDeQuiz());
            ps.setDate(6, Date.valueOf(q.getDateCreation() != null ? q.getDateCreation() : LocalDate.now()));
            ps.setInt(7, q.getIdQuiz());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int idQuiz) throws SQLException {
        String sql = "DELETE FROM quiz WHERE id_quiz=?";
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idQuiz);
            return ps.executeUpdate() > 0;
        }
    }

    private String listToDb(List<String> list) {
        if (list == null || list.isEmpty()) return "";
        return list.stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(SEP));
    }

    private List<String> dbToList(String value) {
        if (value == null || value.trim().isEmpty()) return new ArrayList<>();
        return Arrays.stream(value.split(SEP))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private Quiz mapQuiz(ResultSet rs) throws SQLException {
        int idQuiz = rs.getInt("id_quiz");
        int idCours = rs.getInt("id_cours");
        String titre = rs.getString("titre");
        String listeStr = rs.getString("liste_reponse");
        String repCorrect = rs.getString("reponse_correct");
        int score = rs.getInt("score_quiz");
        Date d = rs.getDate("date_creation");
        LocalDate dateCreation = (d != null) ? d.toLocalDate() : null;

        return new Quiz(idQuiz, idCours, titre, dbToList(listeStr), repCorrect, score, dateCreation);
    }
    public String getTitreById(int idQuiz) throws SQLException {
        String sql = "SELECT titre FROM quiz WHERE id_quiz=?";
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idQuiz);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("titre");
            }
        }
        return null;
    }

}
