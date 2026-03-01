package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import org.example.entities.Planification;
import org.example.services.ServicePlanification;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

public class PlanificationAddController {

    @FXML private TextField categorieTF;
    @FXML private TextField montantTF;
    @FXML private ComboBox<String> prioriteCB;
    @FXML private ComboBox<String> moisCB;
    @FXML private Label msgLabel;

    private final ServicePlanification service = new ServicePlanification();

    @FXML
    public void initialize() {

        prioriteCB.getItems().setAll("basse", "normale", "elevee");
        prioriteCB.getSelectionModel().select("normale");


        moisCB.getItems().clear();
        for (Month m : Month.values()) {
            moisCB.getItems().add(m.getDisplayName(TextStyle.FULL, Locale.FRENCH));
        }
        moisCB.getSelectionModel().selectFirst(); // par défaut: Janvier
    }

    @FXML
    private void ajouter() {
        msgLabel.setText("");
        msgLabel.setStyle("-fx-text-fill: #d22;");

        String categorie = categorieTF.getText() == null ? "" : categorieTF.getText().trim();
        String montantStr = montantTF.getText() == null ? "" : montantTF.getText().trim();
        String priorite = prioriteCB.getValue();
        String mois = moisCB.getValue();


        if (categorie.isEmpty() || montantStr.isEmpty() || priorite == null || mois == null) {
            msgLabel.setText("Veuillez remplir tous les champs obligatoires (*).");
            return;
        }

        double montant;
        try {
            montant = Double.parseDouble(montantStr);
            if (montant <= 0) {
                msgLabel.setText("Le montant doit être > 0.");
                return;
            }
        } catch (NumberFormatException e) {
            msgLabel.setText("Montant invalide (ex: 400).");
            return;
        }

        try {
            Planification p = new Planification(categorie, montant, priorite, mois);
            service.ajouter(p);

            msgLabel.setStyle("-fx-text-fill: green;");
            msgLabel.setText("Planification ajoutée");

            clear();
        } catch (Exception e) {
            e.printStackTrace();
            msgLabel.setStyle("-fx-text-fill: #d22;");
            msgLabel.setText("Erreur lors de l'ajout.");
        }
    }

    @FXML
    private void clear() {
        categorieTF.clear();
        montantTF.clear();
        prioriteCB.getSelectionModel().select("normale");
        moisCB.getSelectionModel().selectFirst();
        msgLabel.setText("");
    }

    @FXML
    private void goList() {
        swapCenter("/PlanificationList.fxml");
    }

    @FXML
    private void backToBudget() {

        swapCenter("/PlanificationList.fxml");
    }

    private void swapCenter(String fxml) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxml));
            StackPane contentPane = (StackPane) ((Node) categorieTF).getScene().lookup("#contentPane");
            if (contentPane != null) contentPane.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
