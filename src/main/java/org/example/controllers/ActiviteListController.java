package org.example.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.example.entities.Activite;
import org.example.services.ServiceActivite;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ActiviteListController {


    @FXML private ListView<Activite> activiteLV;
    @FXML private ComboBox<String> frequenceCB;
    @FXML private DatePicker dateFromDP;
    @FXML private DatePicker dateToDP;
    @FXML private TextField minTF;
    @FXML private TextField maxTF;
    @FXML private ComboBox<String> sortCB;
    @FXML private Label filterMsgLabel;

    @FXML private TextField searchTF;

    public static String successMessage = null;

    private final ServiceActivite service = new ServiceActivite();
    private List<Activite> all = new ArrayList<>();



    @FXML
    public void initialize() {
        activiteLV.setCellFactory(lv -> new ActiviteCell());

        if (frequenceCB != null) {
            frequenceCB.getItems().setAll("Tous", "AUCUNE", "HEBDOMADAIRE", "MENSUELLE");
            frequenceCB.setValue("Tous");
        }

        if (sortCB != null) {
            sortCB.getItems().setAll(
                    "Montant ↑ (croissant)",
                    "Montant ↓ (décroissant)",
                    "Date ↑",
                    "Date ↓"
            );
            sortCB.setValue("Date ↓");
        }

        if (successMessage != null && filterMsgLabel != null) {
            filterMsgLabel.setText(successMessage);
            filterMsgLabel.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
            successMessage = null; // important
        }

        refresh();
    }



    @FXML
    private void refresh() {
        try {
            all = service.afficher();
            applyFilterSort();

            if (filterMsgLabel != null && (successMessage == null)) {
                filterMsgLabel.setText("");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @FXML
    private void applyFilterSort() {
        if (filterMsgLabel != null) {
            filterMsgLabel.setText("");
            filterMsgLabel.setStyle("-fx-text-fill: #d22;");
        }

        String freq = (frequenceCB == null) ? "Tous" : frequenceCB.getValue();
        boolean hasFreq = freq != null && !"Tous".equalsIgnoreCase(freq);

        var from = (dateFromDP == null) ? null : dateFromDP.getValue();
        var to   = (dateToDP   == null) ? null : dateToDP.getValue();

        String minStr = (minTF == null || minTF.getText() == null) ? "" : minTF.getText().trim();
        String maxStr = (maxTF == null || maxTF.getText() == null) ? "" : maxTF.getText().trim();
        String q = (searchTF == null || searchTF.getText() == null) ? "" : searchTF.getText().trim().toLowerCase();
        boolean hasQ = !q.isEmpty();
        boolean hasMin      = !minStr.isEmpty();
        boolean hasMax      = !maxStr.isEmpty();
        boolean hasDateFrom = from != null;
        boolean hasDateTo   = to   != null;

        final Double min;
        final Double max;

        try {
            min = hasMin ? Double.parseDouble(minStr) : null;
            max = hasMax ? Double.parseDouble(maxStr) : null;

            if (min != null && max != null && min > max) {
                if (filterMsgLabel != null) {
                    filterMsgLabel.setText("Montant min doit être ≤ montant max.");
                }
                return;
            }
        } catch (NumberFormatException e) {
            if (filterMsgLabel != null) {
                filterMsgLabel.setText("Montant min/max invalide.");
            }
            return;
        }

        final Date dFrom = hasDateFrom ? Date.valueOf(from) : null;
        final Date dTo   = hasDateTo   ? Date.valueOf(to)   : null;

        var stream = all.stream()
                .filter(a -> !hasFreq || (a.getFrequence() != null && a.getFrequence().equalsIgnoreCase(freq)))


                .filter(a -> !hasQ || (
                        (a.getDescription() != null && a.getDescription().toLowerCase().contains(q)) ||
                                (a.getCategorie() != null && a.getCategorie().toLowerCase().contains(q))
                ))

                .filter(a -> dFrom == null || (a.getDateActivite() != null && !a.getDateActivite().before(dFrom)))
                .filter(a -> dTo   == null || (a.getDateActivite() != null && !a.getDateActivite().after(dTo)))
                .filter(a -> min   == null || a.getMontant() >= min)
                .filter(a -> max   == null || a.getMontant() <= max);


        String sort = (sortCB == null) ? "Date ↓" : sortCB.getValue();

        if ("Montant ↓ (décroissant)".equals(sort)) {
            stream = stream.sorted(Comparator.comparingDouble(Activite::getMontant).reversed());
        }
        else if ("Montant ↑ (croissant)".equals(sort)) {
            stream = stream.sorted(Comparator.comparingDouble(Activite::getMontant));
        }
        else if ("Date ↑".equals(sort)) {
            stream = stream.sorted(Comparator.comparing(
                    Activite::getDateActivite,
                    Comparator.nullsLast(Comparator.naturalOrder())
            ));
        }
        else { // "Date ↓"
            stream = stream.sorted(Comparator.comparing(
                    Activite::getDateActivite,
                    Comparator.nullsLast(Comparator.naturalOrder())
            ).reversed());
        }

        List<Activite> result = stream.toList();
        activiteLV.getItems().setAll(result);

        boolean anyFilterActive = hasFreq || hasMin || hasMax || hasDateFrom || hasDateTo;

        if (result.isEmpty() && anyFilterActive && filterMsgLabel != null) {
            filterMsgLabel.setText("Aucun résultat.");
        }
        else if (filterMsgLabel != null) {
            filterMsgLabel.setText("");
        }
    }



    @FXML
    private void resetFilterSort() {
        if (frequenceCB != null) frequenceCB.setValue("Tous");
        if (dateFromDP != null)  dateFromDP.setValue(null);
        if (dateToDP   != null)  dateToDP.setValue(null);
        if (minTF      != null)  minTF.clear();
        if (maxTF      != null)  maxTF.clear();
        if (sortCB     != null)  sortCB.setValue("Date ↓");

        if (filterMsgLabel != null) {
            filterMsgLabel.setText("");
        }
        if (searchTF != null) searchTF.clear();

        applyFilterSort();
    }


    // ─── Navigation ─────────────────────────────────────────────────
    @FXML
    private void goAdd() {
        swapCenter("/ActiviteAdd.fxml");
    }

    @FXML
    private void goCalendar() {
        swapCenter("/CalendarView.fxml");
    }

    private void swapCenter(String fxml) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxml));
            StackPane contentPane = (StackPane) ((Node) activiteLV)
                    .getScene()
                    .lookup("#contentPane");

            if (contentPane != null) {
                contentPane.getChildren().setAll(view);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openEdit(Activite a) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ActiviteEdit.fxml"));
            Parent view = loader.load();
            ActiviteEditController ctrl = loader.getController();
            ctrl.setActivite(a);

            StackPane contentPane = (StackPane) ((Node) activiteLV)
                    .getScene()
                    .lookup("#contentPane");

            if (contentPane != null) {
                contentPane.getChildren().setAll(view);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void deleteActivite(Activite a) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer cette activité ?");
        confirm.setContentText("Description: " + a.getDescription() + "\nMontant: " + a.getMontant());

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
            service.supprimer(a.getId());
            refresh();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private class ActiviteCell extends ListCell<Activite> {

        @Override
        protected void updateItem(Activite a, boolean empty) {
            super.updateItem(a, empty);

            if (empty || a == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            Label title = new Label(a.getDescription());
            title.setStyle("-fx-font-size: 15; -fx-font-weight: bold;");

            Label categorie = new Label("Catégorie: " + (a.getCategorie() == null ? "" : a.getCategorie()));
            Label montant   = new Label("Montant: " + a.getMontant());
            Label date      = new Label("Date début: " + (a.getDateActivite() == null ? "" : a.getDateActivite()));

            String freq = (a.getFrequence() == null ? "AUCUNE" : a.getFrequence());
            Label frequence = new Label("Fréquence: " + freq);

            Label fin = new Label("Fin: " + (a.getDateFinRecurrence() == null ? "-" : a.getDateFinRecurrence()));

            VBox info = new VBox(4, title, categorie, montant, date, frequence, fin);

            Button btnEdit = new Button("Modifier");
            btnEdit.getStyleClass().add("secondary-btn");
            btnEdit.setOnAction(e -> openEdit(a));

            Button btnDel = new Button("Supprimer");
            btnDel.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 10;");
            btnDel.setOnAction(e -> deleteActivite(a));

            VBox actions = new VBox(8, btnEdit, btnDel);
            actions.setMinWidth(110);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox root = new HBox(12, info, spacer, actions);
            root.setStyle(
                    "-fx-background-color: white; " +
                            "-fx-background-radius: 12; " +
                            "-fx-padding: 12; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 12, 0.2, 0, 2);"
            );

            setGraphic(root);
        }
    }
    @FXML
    private void goAnalyseMois() {
        swapCenter("/AnalyseMois.fxml");
    }
}