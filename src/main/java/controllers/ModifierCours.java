package controllers;

import com.gestion.Services.ServiceCours;
import com.gestion.entities.Cours;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;

public class ModifierCours {

    @FXML private TextField tfNom;
    @FXML private TextField tfDescription;

    private final ServiceCours service = new ServiceCours();
    private Cours cours;

    public void setCours(Cours c) {
        this.cours = c;
        tfNom.setText(c.getNomCours());
        tfDescription.setText(c.getDescription());
    }

    @FXML
    private void save() {
        try {
            String nom = (tfNom.getText() == null) ? "" : tfNom.getText().trim();
            String desc = (tfDescription.getText() == null) ? "" : tfDescription.getText().trim();

            if (nom.isEmpty() || desc.isEmpty()) {
                new Alert(Alert.AlertType.ERROR, "Veuillez remplir tous les champs !").showAndWait();
                return;
            }

            cours.setNomCours(nom);
            cours.setDescription(desc);
            if (cours.getDateCreation() == null) cours.setDateCreation(LocalDate.now());

            boolean ok = service.update(cours);

            if (ok) {
                new Alert(Alert.AlertType.INFORMATION, "Cours modifié ✅").showAndWait();
                goListeCours();
            } else {
                new Alert(Alert.AlertType.ERROR, "Update échoué.").showAndWait();
            }

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void goListeCours() throws Exception {
        Stage stage = (Stage) tfNom.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ListeCours.fxml"));
        stage.setScene(new Scene(loader.load()));
    }
}
