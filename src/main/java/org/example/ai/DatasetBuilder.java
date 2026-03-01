package org.example.ai;

import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.sql.Date;

public class DatasetBuilder {

    private final Connection cnx;

    public DatasetBuilder(Connection cnx) {
        this.cnx = cnx;
    }


    public List<FeatureRow> buildTrainingDataset(int monthsBack) throws SQLException {
        List<FeatureRow> rows = new ArrayList<>();

        YearMonth current = YearMonth.now();

        for (int m = 0; m < monthsBack; m++) {
            YearMonth ym = current.minusMonths(m);

            List<String> categories = getCategoriesWithBudget(ym);

            for (String cat : categories) {

                double budget = getBudgetForCategoryMonth(cat, ym);
                if (budget <= 0) continue;

                double depenseFinale = getTotalExpenseForMonth(cat, ym);
                int target = depenseFinale > budget ? 1 : 0;

                int[] checkpoints = {5, 10, 15, 20, 25};

                for (int day : checkpoints) {
                    int lastDay = Math.min(day, ym.lengthOfMonth());

                    double depensesActuelles = getExpenseUntilDay(cat, ym, lastDay);
                    int nbTransactions = getCountUntilDay(cat, ym, lastDay);
                    double moyenneHistorique = getHistoricalAverage(cat, ym, 3);

                    double ratio = depensesActuelles / budget;
                    double vitesse = depensesActuelles / lastDay;

                    double[] x = new double[] {
                            budget,
                            depensesActuelles,
                            lastDay,
                            moyenneHistorique,
                            nbTransactions,
                            ratio,
                            vitesse
                    };

                    rows.add(new FeatureRow(x, target));
                }
            }
        }

        return rows;
    }


    public List<String> getCategoriesWithBudget(YearMonth ym) throws SQLException {
        List<String> list = new ArrayList<>();

        String moisFrancais = getFrenchMonthName(ym);

        String sql = "SELECT DISTINCT categorie FROM planification WHERE LOWER(TRIM(mois)) = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, moisFrancais);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String cat = rs.getString("categorie");
                    if (cat != null && !cat.trim().isEmpty()) {
                        list.add(cat.trim());
                    }
                }
            }
        }

        return list;
    }


    public double getBudgetForCategoryMonth(String categorie, YearMonth ym) throws SQLException {
        String moisFrancais = getFrenchMonthName(ym);
        String cat = normalize(categorie);

        String sql = "SELECT montant_alloue FROM planification " +
                "WHERE LOWER(TRIM(categorie)) = ? " +
                "AND LOWER(TRIM(mois)) = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, cat);
            ps.setString(2, moisFrancais);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("montant_alloue");
                }
            }
        }

        return 0.0;
    }


    public double getTotalExpenseForMonth(String categorie, YearMonth ym) throws SQLException {
        LocalDate start = startOfMonth(ym);
        LocalDate end = endOfMonth(ym);

        String cat = normalize(categorie);

        String sql = "SELECT COALESCE(SUM(montant), 0) AS total " +
                "FROM activite " +
                "WHERE LOWER(TRIM(categorie)) = ? AND date_activite BETWEEN ? AND ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, cat);
            ps.setDate(2, toSqlDate(start));
            ps.setDate(3, toSqlDate(end));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        }

        return 0.0;
    }


    public double getExpenseUntilDay(String categorie, YearMonth ym, int day) throws SQLException {
        LocalDate start = startOfMonth(ym);
        LocalDate end = ym.atDay(day);

        String cat = normalize(categorie);

        String sql = "SELECT COALESCE(SUM(montant), 0) AS total " +
                "FROM activite " +
                "WHERE LOWER(TRIM(categorie)) = ? AND date_activite BETWEEN ? AND ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, cat);
            ps.setDate(2, toSqlDate(start));
            ps.setDate(3, toSqlDate(end));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        }

        return 0.0;
    }


    public int getCountUntilDay(String categorie, YearMonth ym, int day) throws SQLException {
        LocalDate start = startOfMonth(ym);
        LocalDate end = ym.atDay(day);

        String cat = normalize(categorie);

        String sql = "SELECT COALESCE(COUNT(*), 0) AS cnt " +
                "FROM activite " +
                "WHERE LOWER(TRIM(categorie)) = ? AND date_activite BETWEEN ? AND ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, cat);
            ps.setDate(2, toSqlDate(start));
            ps.setDate(3, toSqlDate(end));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("cnt");
                }
            }
        }

        return 0;
    }


    public double getHistoricalAverage(String categorie, YearMonth currentMonth, int nbMonths) throws SQLException {
        double sum = 0.0;
        int count = 0;

        for (int i = 1; i <= nbMonths; i++) {
            YearMonth past = currentMonth.minusMonths(i);
            double totalPast = getTotalExpenseForMonth(categorie, past);

            sum += totalPast;
            count++;
        }

        if (count == 0) return 0.0;
        return sum / count;
    }



    private String normalize(String s) {
        return (s == null) ? "" : s.trim().toLowerCase();
    }

    private String getFrenchMonthName(YearMonth ym) {
        return ym.getMonth()
                .getDisplayName(TextStyle.FULL, Locale.FRENCH)
                .toLowerCase()
                .trim();
    }

    private Date toSqlDate(LocalDate d) {
        return Date.valueOf(d);
    }

    private LocalDate startOfMonth(YearMonth ym) {
        return ym.atDay(1);
    }

    private LocalDate endOfMonth(YearMonth ym) {
        return ym.atEndOfMonth();
    }
}