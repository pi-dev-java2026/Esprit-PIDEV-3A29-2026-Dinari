package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.*;
import org.example.ai.CategorieAnalyseRow;
import org.example.ai.MonthlyAnalysisService;
import org.example.utils.MyDataBase;

import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

public class AnalyseMoisController {

    @FXML private Label monthLabel;
    @FXML private VBox analysisBox;

    @FXML
    public void initialize() {
        refresh();
    }

    @FXML
    private void refresh() {
        try {
            analysisBox.getChildren().clear();

            YearMonth ym = YearMonth.now();
            String mois = ym.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH);
            mois = mois.substring(0, 1).toUpperCase() + mois.substring(1);
            monthLabel.setText(mois + " " + ym.getYear());

            var cnx = MyDataBase.getInstance().getMyConnection();
            var service = new MonthlyAnalysisService(cnx);

            List<CategorieAnalyseRow> rows = service.getCurrentMonthAnalysis();

            if (rows.isEmpty()) {
                Label empty = new Label("Aucune planification trouvée pour ce mois.");
                empty.setStyle("-fx-text-fill: #777;");
                analysisBox.getChildren().add(empty);
                return;
            }

            for (CategorieAnalyseRow r : rows) {
                analysisBox.getChildren().add(buildCard(r));
            }

        } catch (Exception e) {
            e.printStackTrace();
            analysisBox.getChildren().clear();
            Label err = new Label("Erreur lors du chargement de l'analyse.");
            err.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            analysisBox.getChildren().add(err);
        }
    }

    private Pane buildCard(CategorieAnalyseRow r) {
        String cat = r.getCategorie();
        double budget = r.getBudget();
        double conso = r.getConsomme();
        double reste = r.getReste();
        double pct = r.getPct(); // 0..1

        String label;
        String color;
        if (pct < 0.7) { label = "OK"; color = "#2ecc71"; }
        else if (pct < 1.0) { label = "Attention"; color = "#f39c12"; }
        else { label = "Dépassement"; color = "#e74c3c"; }

        Label catLabel = new Label(cat);
        catLabel.setStyle("-fx-font-size: 15; -fx-font-weight: bold;");

        Label badge = new Label(label);
        badge.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-padding: 3 10; -fx-background-radius: 999;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox top = new HBox(10, catLabel, spacer, badge);
        top.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label l1 = new Label("Budget : " + String.format("%.0f", budget) + "   |   Consommé : " + String.format("%.0f", conso));
        Label l2 = new Label("Reste : " + String.format("%.0f", reste) + "   |   " + String.format("%.0f", pct * 100) + "% utilisé");

        if (reste < 0) {
            l2.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        } else {
            l2.setStyle("-fx-text-fill: #333;");
        }

        ProgressBar pb = new ProgressBar();
        pb.setProgress(Math.min(1.0, Math.max(0.0, pct)));
        pb.setPrefWidth(520);

        VBox card = new VBox(6, top, l1, l2, pb);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 12; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 12, 0.2, 0, 2);");

        return card;
    }

    @FXML
    private void goBack() {
        try {
            Parent view = FXMLLoader.load(getClass().getResource("/ActiviteList.fxml"));
            StackPane contentPane = (StackPane) ((Node) analysisBox).getScene().lookup("#contentPane");
            if (contentPane != null) contentPane.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}