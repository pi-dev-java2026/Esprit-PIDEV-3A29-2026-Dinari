package controllers;

import com.gestion.Services.ServiceQuiz;
import com.gestion.entities.Quiz;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ModifierQuiz {

    @FXML private TextField tfIdQuiz;
    @FXML private TextField tfIdCours;
    @FXML private TextField tfTitre;
    @FXML private TextField tfReponses;
    @FXML private TextField tfCorrecte;
    @FXML private TextField tfScore;
    @FXML private CheckBox cbExamMode;
    @FXML private TextField tfTimeLimit;
    @FXML private Label lblTimeLimit;

    private final ServiceQuiz service = new ServiceQuiz();
    private Quiz current;

    private Runnable onSaved;

    public void setOnSaved(Runnable onSaved) {
        this.onSaved = onSaved;
    }

    public void setQuiz(Quiz q) {
        this.current = q;

        tfIdQuiz.setText(String.valueOf(q.getIdQuiz()));
        tfIdCours.setText(String.valueOf(q.getIdCours()));
        tfTitre.setText(q.getTitre() == null ? "" : q.getTitre());

        String reps = (q.getListeDeReponse() == null) ? "" : String.join(";", q.getListeDeReponse());
        tfReponses.setText(reps);

        tfCorrecte.setText(q.getReponseCorrect() == null ? "" : q.getReponseCorrect());
        tfScore.setText(String.valueOf(q.getScoreDeQuiz()));

        cbExamMode.setSelected(q.isExamMode());
        tfTimeLimit.setText(q.isExamMode() ? String.valueOf(q.getTimeLimit()) : "");
        
        tfTimeLimit.setDisable(!q.isExamMode());
        lblTimeLimit.setDisable(!q.isExamMode());

        cbExamMode.selectedProperty().addListener((obs, oldVal, newVal) -> {
            tfTimeLimit.setDisable(!newVal);
            lblTimeLimit.setDisable(!newVal);
            if (!newVal) {
                tfTimeLimit.clear();
            }
        });

        tfIdCours.setDisable(true);
    }

    @FXML
    private void save() {
        try {
            if (current == null) {
                new Alert(Alert.AlertType.ERROR, "Quiz introuvable !").showAndWait();
                return;
            }

            String titre = txt(tfTitre);
            String repsTxt = txt(tfReponses);
            String correcte = txt(tfCorrecte);
            String scoreTxt = txt(tfScore);

            if (titre.isEmpty() || repsTxt.isEmpty() || correcte.isEmpty() || scoreTxt.isEmpty()) {
                new Alert(Alert.AlertType.ERROR, "Veuillez remplir tous les champs !").showAndWait();
                return;
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

            boolean isExamMode = cbExamMode.isSelected();
            int timeLimit = 0;

            if (isExamMode) {
                String timeTxt = txt(tfTimeLimit);
                if (timeTxt.isEmpty()) {
                    new Alert(Alert.AlertType.ERROR, "Veuillez spécifier le temps limite pour le mode examen !").showAndWait();
                    return;
                }
                timeLimit = Integer.parseInt(timeTxt);
                if (timeLimit <= 0) {
                    new Alert(Alert.AlertType.ERROR, "Le temps limite doit être supérieur à 0 !").showAndWait();
                    return;
                }
            }

            current.setTitre(titre);
            current.setListeDeReponse(liste);
            current.setReponseCorrect(correcte);
            current.setScoreDeQuiz(score);
            current.setDateCreation(LocalDate.now());
            current.setExamMode(isExamMode);
            current.setTimeLimit(timeLimit);

            boolean ok = service.update(current);

            if (ok) {
                String modeText = isExamMode ? " (Mode Examen - " + timeLimit + " min)" : "";
                new Alert(Alert.AlertType.INFORMATION, "Quiz modifié ✅" + modeText).showAndWait();

                if (onSaved != null) onSaved.run();

                closeWindow();
            } else {
                new Alert(Alert.AlertType.ERROR, "Update échoué !").showAndWait();
            }

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

    // Close window methods
    @FXML
    private void goListeQuiz() throws Exception {
        closeWindow();
    }

    @FXML
    private void goListeCours() throws Exception {
        closeWindow();
    }
}