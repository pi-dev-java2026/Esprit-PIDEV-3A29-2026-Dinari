package controllers;

import com.gestion.Services.ServiceCours;
import com.gestion.Services.ServiceQuiz;
import com.gestion.entities.Cours;
import com.gestion.entities.Quiz;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;
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

    private int forcedIdCours = -1;

    private Runnable onSaved;

    public void setIdCours(int idCours) {
        this.forcedIdCours = idCours;

        if (cbCours != null) {
            cbCours.setDisable(true);
            cbCours.setVisible(false);
            cbCours.setManaged(false);
        }
    }

    public void setOnSaved(Runnable onSaved) {
        this.onSaved = onSaved;
    }

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

            cbCours.setEditable(false);

            if (forcedIdCours > 0) {
                cbCours.setDisable(true);
                cbCours.setVisible(false);
                cbCours.setManaged(false);
            }

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR,
                    "Erreur chargement cours: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void ajouterQuiz() {
        try {
            String titre = txt(tfTitre);
            String repsTxt = txt(tfReponses);
            String correcte = txt(tfCorrecte);
            String scoreTxt = txt(tfScore);

            if (titre.isEmpty() || repsTxt.isEmpty() || correcte.isEmpty() || scoreTxt.isEmpty()) {
                new Alert(Alert.AlertType.ERROR, "Veuillez remplir tous les champs !").showAndWait();
                return;
            }

            int idCours;

            if (forcedIdCours > 0) {
                idCours = forcedIdCours;
            } else {
                Cours selected = cbCours.getValue();
                if (selected == null) {
                    new Alert(Alert.AlertType.ERROR, "Veuillez choisir un cours !").showAndWait();
                    return;
                }
                idCours = selected.getIdCours();
            }

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

            if (onSaved != null) onSaved.run();

            closeWindow();

        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "Score invalide !").showAndWait();
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur MySQL: " + e.getMessage()).showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur: " + e.getMessage()).showAndWait();
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) tfTitre.getScene().getWindow();
        stage.close();
    }

    private String txt(TextField tf) {
        return (tf.getText() == null) ? "" : tf.getText().trim();
    }

    @FXML
    private void goListeQuiz() throws Exception {
        closeWindow();
    }

    @FXML
    private void goListeCours() throws Exception {
        closeWindow();
    }
}