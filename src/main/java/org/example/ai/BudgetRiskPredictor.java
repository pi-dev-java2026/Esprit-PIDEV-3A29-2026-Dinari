package org.example.ai;

import smile.classification.LogisticRegression;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;

public class BudgetRiskPredictor {

    private final Connection cnx;
    private LogisticRegression model;

    public BudgetRiskPredictor(Connection cnx) throws Exception {
        this.cnx = cnx;
        loadModel();
    }

    private void loadModel() throws Exception {

        // ✅ charge depuis src/main/resources/models/budget_model.ser
        var is = getClass().getResourceAsStream("/models/budget_model.ser");

        if (is == null) {
            throw new RuntimeException("Modèle introuvable: /models/budget_model.ser (resources)");
        }

        try (ObjectInputStream ois = new ObjectInputStream(is)) {
            model = (LogisticRegression) ois.readObject();
        }

        System.out.println("Modèle chargé avec succès (resources).");
    }

    public double predictForCategory(String categorie) throws Exception {

        DatasetBuilder builder = new DatasetBuilder(cnx);

        YearMonth current = YearMonth.now();


        String moisStr = current.getMonth()
                .getDisplayName(TextStyle.FULL, Locale.FRENCH)
                .toLowerCase()
                .trim();

        String cat = (categorie == null) ? "" : categorie.trim();


        double budget = builder.getBudgetForCategoryMonth(cat, current);

        System.out.println("DEBUG IA: categorie=[" + cat + "]");
        System.out.println("DEBUG IA: moisCourant=[" + moisStr + "] (" + current + ")");
        System.out.println("DEBUG IA: budgetTrouve=" + budget);

        if (budget <= 0) {
            System.out.println("Pas de budget pour cette catégorie.");
            return 0.0;
        }

        int today = java.time.LocalDate.now().getDayOfMonth();
        int dayToUse = current.lengthOfMonth();

        double depensesActuelles = builder.getExpenseUntilDay(cat, current, dayToUse);
        int nbTransactions = builder.getCountUntilDay(cat, current, dayToUse);
        double moyenneHistorique = builder.getHistoricalAverage(cat, current, 3);



        double ratio = depensesActuelles / budget;
        double vitesse = depensesActuelles / dayToUse;


        double[] x = new double[]{
                budget,
                depensesActuelles,
                dayToUse,
                moyenneHistorique,
                nbTransactions,
                ratio,
                vitesse
        };

        double[] posteriori = new double[2];
        model.predict(x, posteriori);

        return posteriori[1];
    }
}