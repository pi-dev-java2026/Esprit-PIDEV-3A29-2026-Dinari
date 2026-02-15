package controllers;

import com.gestion.Services.ServiceCours;
import com.gestion.Services.ServiceQuiz;
import com.gestion.entities.Cours;
import com.gestion.entities.Quiz;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AjouterQuiz {

    @FXML private ComboBox<Cours> cbCours;
    @FXML private TextField tfTitre;
    @FXML private TextField tfReponses;
    @FXML private TextField tfCorrecte;
    @FXML private TextField tfScore;

    private final ServiceQuiz serviceQuiz = new ServiceQuiz();
    private final ServiceCours serviceCours = new ServiceCours();

    @FXML
    public void initialize() {


        cbCours.getStyleClass().add("input-combo");

        try {
            cbCours.setItems(FXCollections.observableArrayList(serviceCours.getAll()));

            cbCours.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(Cours c, boolean empty) {
                    super.updateItem(c, empty);
                    setText(empty || c == null ? null : c.getNomCours());
                }
            });

            cbCours.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Cours c, boolean empty) {
                    super.updateItem(c, empty);
                    setText(empty || c == null ? "Choisir un cours..." : c.getNomCours());
                }
            });

            cbCours.setEditable(true);

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR,
                    "Erreur chargement cours: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void ajouterQuiz() {
        try {
            Cours selected = cbCours.getValue();
            String titre = txt(tfTitre);
            String repsTxt = txt(tfReponses);
            String correcte = txt(tfCorrecte);
            String scoreTxt = txt(tfScore);

            if (selected == null || titre.isEmpty() || repsTxt.isEmpty() || correcte.isEmpty() || scoreTxt.isEmpty()) {
                new Alert(Alert.AlertType.ERROR, "Veuillez remplir tous les champs !").showAndWait();
                return;
            }

            int idCours = selected.getIdCours();
            int score = Integer.parseInt(scoreTxt);

            List<String> liste = Arrays.stream(repsTxt.split(";"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());

            if (liste.isEmpty()) {
                new Alert(Alert.AlertType.ERROR, "Liste des réponses vide !").showAndWait();
                return;
            }

            Quiz q = new Quiz(idCours, titre, liste, correcte, score, LocalDate.now());
            int id = serviceQuiz.add(q);

            new Alert(Alert.AlertType.INFORMATION, "Quiz ajouté ✅ (id=" + id + ")").showAndWait();

            cbCours.setValue(null);
            tfTitre.clear();
            tfReponses.clear();
            tfCorrecte.clear();
            tfScore.clear();

            goListeQuiz();

        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "Score لازم رقم.").showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur MySQL: " + e.getMessage()).showAndWait();
        }
    }

    private String txt(TextField tf) {
        return (tf.getText() == null) ? "" : tf.getText().trim();
    }

    @FXML
    private void goListeQuiz() throws Exception {
        Stage stage = (Stage) tfTitre.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ListeQuiz.fxml"));
        stage.setScene(new Scene(loader.load()));
    }

    @FXML
    private void goListeCours() throws Exception {
        Stage stage = (Stage) tfTitre.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ListeCours.fxml"));
        stage.setScene(new Scene(loader.load()));
    }
}
