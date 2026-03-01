package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import utils.ApiClient;

public class DashboardController {

    @FXML
    private Label predictionLabel;

    @FXML
    public void initialize() {
        loadPrediction();
    }

    @FXML
    private void loadPrediction() {
        try {
            int nextMonth = java.time.LocalDate.now().getMonthValue() + 1;
            String response = ApiClient.predictDepense(nextMonth);

            // Parsing simple JSON (sans library lourde)
            String value = response.replaceAll("[^0-9.]", "");

            predictionLabel.setText("📊 Dépense prédite mois prochain : "
                    + value + " DT");

        } catch (Exception e) {
            predictionLabel.setText("Erreur prediction");
            e.printStackTrace();
        }
    }

    @FXML
    private void retrainModel() {
        try {
            ApiClient.retrainModel();

            predictionLabel.setText("Modèle ré-entrainé ✔");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}