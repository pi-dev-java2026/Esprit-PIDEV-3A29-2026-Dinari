package controllers;

import com.gestion.Services.ServiceCours;
import com.gestion.entities.Cours;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDate;

public class AjouterCours {

    @FXML private TextField tfNom;
    @FXML private TextArea taDescription;

    private final ServiceCours service = new ServiceCours();

    @FXML
    private void ajouterCours() {

        String nom = tfNom.getText() == null ? "" : tfNom.getText().trim();
        String description = taDescription.getText() == null ? "" : taDescription.getText().trim();

        if (nom.isEmpty() || description.isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "Veuillez remplir tous les champs !").showAndWait();
            return;
        }

        try {
            Cours c = new Cours(nom, description, LocalDate.now());
            int id = service.add(c);

            new Alert(Alert.AlertType.INFORMATION,
                    "Module ajouté avec succès ✅ (id=" + id + ")").showAndWait();

            tfNom.clear();
            taDescription.clear();

            goListeCours();

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