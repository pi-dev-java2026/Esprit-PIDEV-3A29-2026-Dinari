package org.example.services;

import org.example.entities.Activite;
import org.example.utils.MyDataBase;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.example.services.ExchangeRateService;

public class ServiceActivite implements IService<Activite> {

    private final Connection connection;

    public ServiceActivite() {
        connection = MyDataBase.getInstance().getMyConnection();
    }


    private void insertOne(Activite a) throws SQLException {
        String sql = "INSERT INTO activite (description, montant, date_activite, categorie, devise, frequence, date_fin_recurrence) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, a.getDescription());
        ps.setDouble(2, a.getMontant());
        ps.setDate(3, a.getDateActivite());
        ps.setString(4, a.getCategorie());
        ps.setString(5, (a.getDevise() == null || a.getDevise().isBlank()) ? "TND" : a.getDevise().trim().toUpperCase());
        ps.setString(6, a.getFrequence() == null ? "AUCUNE" : a.getFrequence());
        ps.setDate(7, a.getDateFinRecurrence()); // peut être null
        ps.executeUpdate();
    }
    private final ExchangeRateService rateService = new ExchangeRateService();
    @Override
    public void ajouter(Activite a) throws SQLException {


        if (a.getDevise() == null || a.getDevise().isBlank()) a.setDevise("TND");
        a.setDevise(a.getDevise().trim().toUpperCase());


        try {
            if (!"TND".equals(a.getDevise())) {
                System.out.println("DEV=" + a.getDevise() + " montant_saisi=" + a.getMontant());
                double taux = rateService.getRate(a.getDevise(), "TND");
                System.out.println("TAUX_UTILISE=" + taux);
                a.setMontant(a.getMontant() * taux);
                System.out.println("montant_converti=" + a.getMontant());
            }
        } catch (Exception ex) {
            throw new SQLException("Erreur conversion " + a.getDevise() + " -> TND : " + ex.getMessage(), ex);
        }

        String freq = (a.getFrequence() == null || a.getFrequence().isBlank())
                ? "AUCUNE"
                : a.getFrequence().trim().toUpperCase();


        if ("AUCUNE".equals(freq)) {
            a.setFrequence("AUCUNE");
            a.setDateFinRecurrence(null);
            insertOne(a);
            return;
        }


        if (a.getDateFinRecurrence() == null) {
            throw new SQLException("date_fin_recurrence est obligatoire pour une activité récurrente.");
        }

        LocalDate start = a.getDateActivite().toLocalDate();
        LocalDate end = a.getDateFinRecurrence().toLocalDate();

        if (end.isBefore(start)) {
            throw new SQLException("La date de fin de récurrence doit être >= date d'activité.");
        }


        List<LocalDate> dates = new ArrayList<>();
        LocalDate cur = start;
        dates.add(cur);

        if ("HEBDOMADAIRE".equals(freq)) {
            while (true) {
                cur = cur.plusWeeks(1);
                if (cur.isAfter(end)) break;
                dates.add(cur);
            }
        } else if ("MENSUELLE".equals(freq)) {
            while (true) {
                cur = cur.plusMonths(1);
                if (cur.isAfter(end)) break;
                dates.add(cur);
            }
        } else {
            throw new SQLException("Fréquence inconnue : " + freq);
        }


        boolean oldAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            for (LocalDate d : dates) {
                Activite copy = new Activite(
                        a.getDescription(),
                        a.getMontant(),
                        Date.valueOf(d),
                        a.getCategorie(),
                        a.getDevise(),
                        freq,
                        a.getDateFinRecurrence()
                );
                insertOne(copy);
            }
            connection.commit();
        } catch (Exception ex) {
            connection.rollback();
            throw ex;
        } finally {
            connection.setAutoCommit(oldAutoCommit);
        }
    }
    @Override
    public void modifier(Activite a) throws SQLException {


        if (a.getDevise() == null || a.getDevise().isBlank()) a.setDevise("TND");
        a.setDevise(a.getDevise().trim().toUpperCase());


        try {
            if (!"TND".equals(a.getDevise())) {
                double taux = rateService.getRate(a.getDevise(), "TND");
                a.setMontant(a.getMontant() * taux); // ✅ montant stocké = TND
            }
        } catch (Exception ex) {
            throw new SQLException("Erreur conversion " + a.getDevise() + " -> TND : " + ex.getMessage(), ex);
        }


        String sql = "UPDATE activite SET description=?, montant=?, date_activite=?, categorie=?, devise=?, frequence=?, date_fin_recurrence=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, a.getDescription());
        ps.setDouble(2, a.getMontant()); // ✅ déjà converti
        ps.setDate(3, a.getDateActivite());
        ps.setString(4, a.getCategorie());
        ps.setString(5, a.getDevise());
        ps.setString(6, a.getFrequence() == null ? "AUCUNE" : a.getFrequence());
        ps.setDate(7, a.getDateFinRecurrence()); // nullable
        ps.setInt(8, a.getId());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM activite WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public List<Activite> afficher() throws SQLException {
        List<Activite> list = new ArrayList<>();
        String sql = "SELECT * FROM activite";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            Activite a = new Activite();
            a.setId(rs.getInt("id"));
            a.setDescription(rs.getString("description"));
            a.setMontant(rs.getDouble("montant"));
            a.setDateActivite(rs.getDate("date_activite"));
            a.setCategorie(rs.getString("categorie"));


            a.setDevise(rs.getString("devise"));


            a.setFrequence(rs.getString("frequence"));
            a.setDateFinRecurrence(rs.getDate("date_fin_recurrence"));

            list.add(a);
        }
        return list;
    }

    public double sumMontantByCategorieAndMois(String categorie, String moisFrancais) throws SQLException {
        double total = 0;

        for (Activite a : afficher()) {
            if (a.getCategorie() == null || a.getDateActivite() == null) continue;

            String m = a.getDateActivite().toLocalDate()
                    .getMonth()
                    .getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.FRENCH);

            if (a.getCategorie().equalsIgnoreCase(categorie)
                    && m.equalsIgnoreCase(moisFrancais)) {

                total += a.getMontant();
            }
        }
        return total;
    }

    public List<Double> getHistoryMontants(String categorie, Date dateActivite) throws SQLException {
        List<Double> res = new ArrayList<>();

        String sql =
                "SELECT montant " +
                        "FROM activite " +
                        "WHERE categorie = ? " +
                        "  AND date_activite >= DATE_SUB(?, INTERVAL 3 MONTH) " +
                        "  AND date_activite < ?";

        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, categorie);
        ps.setDate(2, dateActivite);
        ps.setDate(3, dateActivite);

        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            res.add(rs.getDouble("montant"));
        }
        return res;
    }


    public static class AnomalyResult {
        public final boolean anomaly;
        public final double mean;
        public final double std;
        public final double z;
        public final double low;
        public final double high;
        public final int n;

        public AnomalyResult(boolean anomaly, double mean, double std, double z, double low, double high, int n) {
            this.anomaly = anomaly;
            this.mean = mean;
            this.std = std;
            this.z = z;
            this.low = low;
            this.high = high;
            this.n = n;
        }
    }


    public AnomalyResult detectAnomalyZScore(double montant, String categorie, Date dateActivite, double threshold) throws SQLException {
        List<Double> history = getHistoryMontants(categorie, dateActivite);


        if (history.size() < 5) {
            return new AnomalyResult(false, 0, 0, 0, 0, 0, history.size());
        }

        double mean = history.stream().mapToDouble(v -> v).average().orElse(0);

        double sumSq = 0;
        for (double v : history) sumSq += (v - mean) * (v - mean);

        double variance = sumSq / (history.size() - 1);
        double std = Math.sqrt(variance);

        if (std == 0) {
            boolean anomaly = Math.abs(montant - mean) > 0.0001;
            return new AnomalyResult(anomaly, mean, std, 0, mean, mean, history.size());
        }

        double z = (montant - mean) / std;
        double low = mean - 2 * std;
        double high = mean + 2 * std;

        boolean anomaly = z >= threshold;
        return new AnomalyResult(anomaly, mean, std, z, low, high, history.size());
    }
}