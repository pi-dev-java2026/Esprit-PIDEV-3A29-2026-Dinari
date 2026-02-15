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
    @FXML private TextField tfDescription;
    @FXML private TextArea taContenu;

    private final ServiceCours service = new ServiceCours();

    @FXML
    private void ajouterCours() {

        String nom = tfNom.getText() == null ? "" : tfNom.getText().trim();
        String description = tfDescription.getText() == null ? "" : tfDescription.getText().trim();
        String contenu = taContenu.getText() == null ? "" : taContenu.getText().trim();

        if (nom.isEmpty() || description.isEmpty() || contenu.isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "Veuillez remplir tous les champs !").showAndWait();
            return;
        }

        try {
            Cours c = new Cours(nom, contenu, description, LocalDate.now());
            int id = service.add(c);

            new Alert(Alert.AlertType.INFORMATION,
                    "Cours ajouté avec succès ✅ (id=" + id + ")").showAndWait();

            tfNom.clear();
            tfDescription.clear();
            taContenu.clear();

            goListeCours();

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur MySQL: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void goListeCours() throws Exception {
        Stage stage = (Stage) tfNom.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ListeCours.fxml"));
        stage.setScene(new Scene(loader.load()));
    }
}
