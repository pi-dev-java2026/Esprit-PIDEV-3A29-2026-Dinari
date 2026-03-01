package controllers;

import entities.depense;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import services.ServiceDepense;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;

public class AjouterDepense {

    @FXML private DatePicker dateField;
    @FXML private ComboBox<String> categorieBox;
    @FXML private TextField montantField;
    @FXML private ComboBox<String> paiementBox;
    @FXML private TextArea descriptionArea;

    private ServiceDepense de = new ServiceDepense();

    @FXML
    public void initialize() {
        categorieBox.getItems().addAll("Nourriture", "Transport", "Loyer", "Internet", "Autre");
        paiementBox.getItems().addAll("Espèces", "Carte bancaire", "Virement");
        dateField.setValue(LocalDate.now());
    }

    @FXML
    public void ajouterD(ActionEvent actionEvent) {

        if (dateField.getValue() == null ||
                categorieBox.getValue() == null ||
                montantField.getText().isEmpty() ||
                paiementBox.getValue() == null ||
                descriptionArea.getText().isEmpty()) {

            new Alert(Alert.AlertType.WARNING, "Veuillez remplir tous les champs obligatoires.").show();
            return;
        }

        try {

            depense d = new depense();
            d.setMontant(Float.parseFloat(montantField.getText()));
            d.setDate_depense(Date.valueOf(dateField.getValue()));
            d.setDescription(descriptionArea.getText());
            d.setMode_paiement(paiementBox.getValue());
            d.setCategorie(categorieBox.getValue());
            d.setutilisateur_id(1); // مثال: user connecté


            de.ajouter(d);

            new Alert(Alert.AlertType.INFORMATION, "Dépense ajoutée ✔").show();


            montantField.clear();
            descriptionArea.clear();
            categorieBox.setValue(null);
            paiementBox.setValue(null);
            categorieBox.setValue(null);
            dateField.setValue(LocalDate.now());

        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Erreur SQL: " + e.getMessage()).show();
        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "Montant invalide !").show();
        }
    }
    @FXML
    void naviguer(ActionEvent event) {
        try {
            // Création du loader instance pour pouvoir récupérer le root
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherDepense.fxml"));

            // Charge le FXML
            Parent root = loader.load();

            // Remplace le root actuel de la scène
            categorieBox.getScene().setRoot(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
