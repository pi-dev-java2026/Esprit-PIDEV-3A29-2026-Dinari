package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import org.example.entities.Planification;
import org.example.services.ServicePlanification;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

public class PlanificationEditController {

    @FXML private TextField categorieTF;
    @FXML private TextField montantTF;
    @FXML private ComboBox<String> prioriteCB;


    @FXML private ComboBox<String> moisCB;

    @FXML private Label msgLabel;

    private Planification current;
    private final ServicePlanification service = new ServicePlanification();

    @FXML
    public void initialize() {


        prioriteCB.getItems().setAll("basse", "normale", "elevee");


        moisCB.getItems().clear();
        for (Month m : Month.values()) {
            String mois = m.getDisplayName(TextStyle.FULL, Locale.FRENCH);
            mois = mois.substring(0, 1).toUpperCase() + mois.substring(1); // Janvier...
            moisCB.getItems().add(mois);
        }
    }


    public void setPlanification(Planification p) {
        this.current = p;

        categorieTF.setText(p.getCategorie());
        montantTF.setText(String.valueOf(p.getMontantAlloue()));
        prioriteCB.setValue(p.getPriorite());


        if (p.getMois() != null && !p.getMois().isBlank()) {
            moisCB.setValue(p.getMois());
        } else {
            moisCB.getSelectionModel().selectFirst();
        }
    }

    @FXML
    private void save() {
        msgLabel.setText("");
        msgLabel.setStyle("-fx-text-fill: #d22;");

        if (current == null) {
            msgLabel.setText("Planification introuvable.");
            return;
        }

        String categorie = categorieTF.getText().trim();
        String montantStr = montantTF.getText().trim();
        String priorite = prioriteCB.getValue();
        String mois = moisCB.getValue(); // ✅

        if (categorie.isEmpty() || montantStr.isEmpty() || priorite == null || mois == null) {
            msgLabel.setText("Veuillez remplir les champs obligatoires.");
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
            msgLabel.setText("Montant invalide.");
            return;
        }


        current.setCategorie(categorie);
        current.setMontantAlloue(montant);
        current.setPriorite(priorite);
        current.setMois(mois);

        try {
            service.modifier(current);

            msgLabel.setStyle("-fx-text-fill: green;");
            msgLabel.setText("Modification enregistrée ✅");


            backToList();

        } catch (SQLException e) {
            msgLabel.setText("Erreur lors de la modification.");
            e.printStackTrace();
        }
    }

    @FXML
    private void backToList() {
        try {
            Parent view = FXMLLoader.load(getClass().getResource("/PlanificationList.fxml"));
            StackPane contentPane = (StackPane) categorieTF.getScene().lookup("#contentPane");
            if (contentPane != null) contentPane.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
