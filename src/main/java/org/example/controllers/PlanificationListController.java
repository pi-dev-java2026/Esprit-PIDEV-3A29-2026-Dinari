package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.example.entities.Planification;
import org.example.services.ServicePlanification;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;
import javafx.concurrent.Task;
import org.example.ai.BudgetRiskPredictor;
import org.example.utils.MyDataBase;

public class PlanificationListController {

    @FXML private ListView<Planification> planifLV;

    @FXML private ComboBox<String> categorieCB;
    @FXML private ComboBox<String> prioriteCB;
    @FXML private ComboBox<String> moisCB;

    @FXML private TextField minTF;
    @FXML private TextField maxTF;

    @FXML private ComboBox<String> sortCB;

    @FXML private Label filterMsgLabel;
    @FXML private TextField searchTF;

    private final ServicePlanification service = new ServicePlanification();
    private List<Planification> allPlanifs = new ArrayList<>();

    @FXML
    public void initialize() {
        planifLV.setCellFactory(lv -> new PlanifCell());

        if (prioriteCB != null) {
            prioriteCB.getItems().setAll("Tous", "basse", "normale", "elevee");
            prioriteCB.setValue("Tous");
        }

        if (moisCB != null) {
            moisCB.getItems().clear();
            moisCB.getItems().add("Tous");

            for (Month m : Month.values()) {
                String mois = m.getDisplayName(TextStyle.FULL, Locale.FRENCH);
                mois = mois.substring(0, 1).toUpperCase() + mois.substring(1);
                moisCB.getItems().add(mois);
            }
            moisCB.setValue("Tous");
        }

        if (sortCB != null) {
            sortCB.getItems().setAll("Montant ↑ (croissant)", "Montant ↓ (décroissant)");
            sortCB.setValue("Montant ↑ (croissant)");
        }

        refresh();
    }

    @FXML
    private void refresh() {
        try {
            allPlanifs = service.afficher();

            fillCategorieCombo();


            applyFilterSort();

            if (filterMsgLabel != null) filterMsgLabel.setText("");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fillCategorieCombo() {
        if (categorieCB == null) return;

        Set<String> cats = allPlanifs.stream()
                .map(Planification::getCategorie)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(() -> new TreeSet<>(String.CASE_INSENSITIVE_ORDER)));

        categorieCB.getItems().clear();
        categorieCB.getItems().add("Tous");
        categorieCB.getItems().addAll(cats);
        categorieCB.setValue("Tous");
    }

    @FXML
    private void applyFilterSort() {

        if (filterMsgLabel != null) {
            filterMsgLabel.setText("");
            filterMsgLabel.setStyle("-fx-text-fill: #d22;");
        }

        String cat = (categorieCB == null) ? "Tous" : categorieCB.getValue();
        String pri = (prioriteCB == null) ? "Tous" : prioriteCB.getValue();
        String mois = (moisCB == null) ? "Tous" : moisCB.getValue();

        String minStr = (minTF == null || minTF.getText() == null) ? "" : minTF.getText().trim();
        String maxStr = (maxTF == null || maxTF.getText() == null) ? "" : maxTF.getText().trim();

        String q = (searchTF == null || searchTF.getText() == null) ? "" : searchTF.getText().trim().toLowerCase();
        boolean hasQ = !q.isEmpty();

        boolean hasCat = cat != null && !cat.equals("Tous");
        boolean hasPri = pri != null && !pri.equals("Tous");
        boolean hasMois = mois != null && !mois.equals("Tous");
        boolean hasMin = !minStr.isEmpty();
        boolean hasMax = !maxStr.isEmpty();


        Double minValue = null;
        Double maxValue = null;

        try {
            if (hasMin) minValue = Double.parseDouble(minStr);
            if (hasMax) maxValue = Double.parseDouble(maxStr);

            if (minValue != null && maxValue != null && minValue > maxValue) {
                if (filterMsgLabel != null) filterMsgLabel.setText("Montant min doit être ≤ montant max.");
                return;
            }
        } catch (NumberFormatException e) {
            if (filterMsgLabel != null) filterMsgLabel.setText("Montant min/max invalide.");
            return;
        }

        final Double minFinal = minValue;
        final Double maxFinal = maxValue;


        var stream = allPlanifs.stream()

                .filter(p -> !hasCat || (p.getCategorie() != null && p.getCategorie().equalsIgnoreCase(cat)))

                .filter(p -> !hasPri || (p.getPriorite() != null && p.getPriorite().equalsIgnoreCase(pri)))

                .filter(p -> !hasMois || (p.getMois() != null && p.getMois().equalsIgnoreCase(mois)))

                .filter(p -> !hasQ || (
                        (p.getCategorie() != null && p.getCategorie().toLowerCase().contains(q)) ||
                                (p.getMois() != null && p.getMois().toLowerCase().contains(q)) ||
                                (p.getPriorite() != null && p.getPriorite().toLowerCase().contains(q))
                ))
                .filter(p -> minFinal == null || p.getMontantAlloue() >= minFinal)
                .filter(p -> maxFinal == null || p.getMontantAlloue() <= maxFinal);


        String sortChoice = (sortCB == null) ? "Montant ↑ (croissant)" : sortCB.getValue();
        if ("Montant ↓ (décroissant)".equals(sortChoice)) {
            stream = stream.sorted(Comparator.comparingDouble(Planification::getMontantAlloue).reversed());
        } else {
            stream = stream.sorted(Comparator.comparingDouble(Planification::getMontantAlloue));
        }

        List<Planification> result = stream.toList();
        planifLV.getItems().setAll(result);


        boolean anyFilterActive = hasCat || hasPri || hasMois || hasMin || hasMax;
        if (result.isEmpty() && anyFilterActive && filterMsgLabel != null) {
            filterMsgLabel.setText("Aucun résultat.");
        } else if (filterMsgLabel != null) {
            filterMsgLabel.setText("");
        }
    }

    @FXML
    private void resetFilterSort() {
        if (categorieCB != null) categorieCB.setValue("Tous");
        if (prioriteCB != null) prioriteCB.setValue("Tous");
        if (moisCB != null) moisCB.setValue("Tous");

        if (minTF != null) minTF.clear();
        if (maxTF != null) maxTF.clear();

        if (sortCB != null) sortCB.setValue("Montant ↑ (croissant)");

        if (filterMsgLabel != null) filterMsgLabel.setText("");

        if (searchTF != null) searchTF.clear();


        applyFilterSort();
    }

    @FXML
    private void goAdd() {
        swapCenter("/PlanificationAdd.fxml");
    }

    private void swapCenter(String fxml) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxml));
            StackPane contentPane = (StackPane) ((Node) planifLV).getScene().lookup("#contentPane");
            if (contentPane != null) contentPane.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openEdit(Planification p) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PlanificationEdit.fxml"));
            Parent view = loader.load();

            PlanificationEditController ctrl = loader.getController();
            ctrl.setPlanification(p);

            StackPane contentPane = (StackPane) ((Node) planifLV).getScene().lookup("#contentPane");
            if (contentPane != null) contentPane.getChildren().setAll(view);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deletePlanification(Planification p) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer cette planification ?");
        confirm.setContentText("Catégorie: " + p.getCategorie() + "\nMontant: " + p.getMontantAlloue());

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        try {
            service.supprimer(p.getId());
            refresh();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class PlanifCell extends ListCell<Planification> {
        @Override
        protected void updateItem(Planification p, boolean empty) {
            super.updateItem(p, empty);

            if (empty || p == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            Label title = new Label(p.getCategorie());
            title.setStyle("-fx-font-size: 15; -fx-font-weight: bold;");

            Label montant = new Label("Montant: " + p.getMontantAlloue());
            Label priorite = new Label("Priorité: " + p.getPriorite());
            Label mois = new Label("Mois: " + (p.getMois() == null ? "" : p.getMois()));
            Label risk = new Label("Risque IA : -");
            risk.setStyle("-fx-text-fill: #666; -fx-font-style: italic;");
            VBox info = new VBox(4, title, montant, priorite, mois, risk);

            Button btnEdit = new Button("Modifier");
            btnEdit.getStyleClass().add("secondary-btn");
            btnEdit.setOnAction(e -> openEdit(p));

            Button btnDel = new Button("Supprimer");
            btnDel.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 10;");
            btnDel.setOnAction(e -> deletePlanification(p));
            Button btnIA = new Button("Analyser IA");
            btnIA.setStyle("-fx-background-color: #2d89ef; -fx-text-fill: white; -fx-background-radius: 10;");
            btnIA.setOnAction(e -> predictRiskAsync(p.getCategorie(), risk));

            VBox actions = new VBox(8, btnIA, btnEdit, btnDel);
            actions.setMinWidth(110);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox root = new HBox(12, info, spacer, actions);
            root.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 12; "
                    + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 12, 0.2, 0, 2);");

            setGraphic(root);
        }
    }

    private void loadInParent(String fxmlPath) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BudgetLayout.fxml"));

            StackPane contentPane = (StackPane) planifLV.getScene().lookup("#contentPane");
            if (contentPane == null) {
                System.out.println("contentPane introuvable");
                return;
            }

            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentPane.getChildren().setAll(view);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void predictRiskAsync(String categorie, Label riskLabel) {

        riskLabel.setText("Risque IA : calcul...");

        Task<Double> task = new Task<>() {
            @Override
            protected Double call() throws Exception {
                BudgetRiskPredictor predictor =
                        new BudgetRiskPredictor(MyDataBase.getInstance().getMyConnection());
                return predictor.predictForCategory(categorie);
            }
        };

        task.setOnSucceeded(ev -> {
            double proba = task.getValue(); // 0..1
            String level;
            String color;

            if (proba < 0.4) { level = "Faible"; color = "#2ecc71"; }
            else if (proba < 0.7) { level = "Moyen"; color = "#f39c12"; }
            else { level = "Élevé"; color = "#e74c3c"; }

            riskLabel.setText("Risque IA : " + String.format("%.2f", proba * 100) + "% (" + level + ")");
            riskLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
        });

        task.setOnFailed(ev -> {
            riskLabel.setText("Risque IA : erreur");
            riskLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            task.getException().printStackTrace();
        });

        new Thread(task).start();
    }
}
