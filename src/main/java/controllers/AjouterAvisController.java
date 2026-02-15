package controllers;

import com.gestion.Services.ServiceAvis;
import com.gestion.Services.ServiceQuiz;
import com.gestion.entities.Avis;
import com.gestion.entities.Quiz;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;

public class AjouterAvisController {

    @FXML private ComboBox<Quiz> cbQuiz;
    @FXML private TextField tfCommentaire;
    @FXML private TextField tfNote;

    private final ServiceQuiz serviceQuiz = new ServiceQuiz();
    private final ServiceAvis serviceAvis = new ServiceAvis();

    @FXML
    public void initialize() {
        try {
            cbQuiz.setItems(FXCollections.observableArrayList(serviceQuiz.getAll()));

            cbQuiz.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(Quiz q, boolean empty) {
                    super.updateItem(q, empty);
                    setText(empty || q == null ? "" : q.getTitre());
                }
            });

            cbQuiz.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Quiz q, boolean empty) {
                    super.updateItem(q, empty);
                    setText(empty || q == null ? "" : q.getTitre());
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur chargement Quiz: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void ajouterAvis() {

        try {
            Quiz selectedQuiz = cbQuiz.getValue();
            String commentaire = tfCommentaire.getText().trim();
            String noteTxt = tfNote.getText().trim();

            if (selectedQuiz == null || commentaire.isEmpty() || noteTxt.isEmpty()) {
                new Alert(Alert.AlertType.ERROR, "Veuillez remplir tous les champs !").showAndWait();
                return;
            }

            int note = Integer.parseInt(noteTxt);

            Avis a = new Avis(
                    selectedQuiz.getIdQuiz(),
                    commentaire,
                    note,
                    LocalDate.now()
            );

            int id = serviceAvis.add(a);

            new Alert(Alert.AlertType.INFORMATION,
                    "Avis ajouté avec succès ✅ (id=" + id + ")").showAndWait();

            tfCommentaire.clear();
            tfNote.clear();
            cbQuiz.setValue(null);

        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "Note   !").showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur MySQL: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void goListeCours() throws Exception {
        switchTo("/ListeCours.fxml");
    }

    @FXML
    private void goListeQuiz() throws Exception {
        switchTo("/ListeQuiz.fxml");
    }

    private void switchTo(String fxml) throws Exception {
        Stage stage = (Stage) cbQuiz.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
        stage.setScene(new Scene(loader.load()));
    }
}
