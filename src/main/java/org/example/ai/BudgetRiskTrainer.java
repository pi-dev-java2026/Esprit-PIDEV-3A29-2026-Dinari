package org.example.ai;

import smile.classification.LogisticRegression;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.util.List;

public class BudgetRiskTrainer {

    private final Connection cnx;

    public BudgetRiskTrainer(Connection cnx) {
        this.cnx = cnx;
    }

    public void trainAndSaveModel() throws Exception {

        DatasetBuilder builder = new DatasetBuilder(cnx);


        List<FeatureRow> rows = builder.buildTrainingDataset(6);

        if (rows.isEmpty()) {
            System.out.println("Dataset vide. Impossible d'entraîner.");
            return;
        }


        int n = rows.size();
        int featureCount = rows.get(0).x.length;

        double[][] X = new double[n][featureCount];
        int[] y = new int[n];

        for (int i = 0; i < n; i++) {
            X[i] = rows.get(i).x;
            y[i] = rows.get(i).y;
        }


        LogisticRegression model = LogisticRegression.fit(X, y);
        System.out.println("Modèle entraîné avec " + n + " exemples.");


        File dir = new File("models");
        if (!dir.exists()) {
            boolean created = dir.mkdir();
            if (!created) {
                System.out.println("Impossible de créer le dossier models/");
                return;
            }
        }


        File modelFile = new File(dir, "budget_model.ser");


        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(modelFile))) {
            oos.writeObject(model);
        }


        System.out.println("Modèle sauvegardé ici :");
        System.out.println(modelFile.getAbsolutePath());
    }
}