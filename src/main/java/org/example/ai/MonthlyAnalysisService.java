package org.example.ai;

import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;

public class MonthlyAnalysisService {

    private final Connection cnx;

    public MonthlyAnalysisService(Connection cnx) {
        this.cnx = cnx;
    }

    public List<CategorieAnalyseRow> getCurrentMonthAnalysis() throws SQLException {

        YearMonth ym = YearMonth.now();
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        String mois = ym.getMonth()
                .getDisplayName(TextStyle.FULL, Locale.FRENCH)
                .toLowerCase();

        Map<String, Double> budgets = new HashMap<>();

        String sqlBudget = "SELECT categorie, montant_alloue FROM planification WHERE LOWER(mois) = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sqlBudget)) {
            ps.setString(1, mois);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    budgets.put(
                            rs.getString("categorie"),
                            rs.getDouble("montant_alloue")
                    );
                }
            }
        }

        Map<String, Double> consommes = new HashMap<>();

        String sqlConso =
                "SELECT categorie, COALESCE(SUM(montant),0) AS total " +
                        "FROM activite WHERE date_activite BETWEEN ? AND ? " +
                        "GROUP BY categorie";

        try (PreparedStatement ps = cnx.prepareStatement(sqlConso)) {

            ps.setDate(1, java.sql.Date.valueOf(start));
            ps.setDate(2, java.sql.Date.valueOf(end));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    consommes.put(
                            rs.getString("categorie"),
                            rs.getDouble("total")
                    );
                }
            }
        }

        List<CategorieAnalyseRow> rows = new ArrayList<>();

        for (var entry : budgets.entrySet()) {
            String cat = entry.getKey();
            double budget = entry.getValue();
            double consomme = consommes.getOrDefault(cat, 0.0);

            rows.add(new CategorieAnalyseRow(cat, budget, consomme));
        }

        rows.sort(Comparator.comparingDouble(CategorieAnalyseRow::getPct).reversed());

        return rows;
    }
}