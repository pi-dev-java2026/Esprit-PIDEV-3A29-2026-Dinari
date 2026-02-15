package controllers;

import com.gestion.Services.ServiceQuiz;
import com.gestion.entities.Quiz;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

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

    private final ServiceQuiz service = new ServiceQuiz();
    private Quiz current;

    public void setQuiz(Quiz q) {
        this.current = q;

        tfIdQuiz.setText(String.valueOf(q.getIdQuiz()));
        tfIdCours.setText(String.valueOf(q.getIdCours()));
        tfTitre.setText(q.getTitre() == null ? "" : q.getTitre());

        String reps = (q.getListeDeReponse() == null) ? "" : String.join(";", q.getListeDeReponse());
        tfReponses.setText(reps);

        tfCorrecte.setText(q.getReponseCorrect() == null ? "" : q.getReponseCorrect());
        tfScore.setText(String.valueOf(q.getScoreDeQuiz()));
    }

    @FXML
    private void save() {
        try {
            if (current == null) {
                new Alert(Alert.AlertType.ERROR, "Quiz not found!").showAndWait();
                return;
            }

            String idCoursTxt = txt(tfIdCours);
            String titre = txt(tfTitre);
            String repsTxt = txt(tfReponses);
            String correcte = txt(tfCorrecte);
            String scoreTxt = txt(tfScore);

            if (idCoursTxt.isEmpty() || titre.isEmpty() || repsTxt.isEmpty() || correcte.isEmpty() || scoreTxt.isEmpty()) {
                new Alert(Alert.AlertType.ERROR, "Veuillez remplir tous les champs !").showAndWait();
                return;
            }

            int idCours = Integer.parseInt(idCoursTxt);
            int score = Integer.parseInt(scoreTxt);

            List<String> liste = Arrays.stream(repsTxt.split(";"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());

            if (liste.isEmpty()) {
                new Alert(Alert.AlertType.ERROR, "Liste des réponses vide !").showAndWait();
                return;
            }

            current.setIdCours(idCours);
            current.setTitre(titre);
            current.setListeDeReponse(liste);
            current.setReponseCorrect(correcte);
            current.setScoreDeQuiz(score);
            current.setDateCreation(LocalDate.now());

            boolean ok = service.update(current);

            if (ok) {
                new Alert(Alert.AlertType.INFORMATION, "Quiz modifié ✅").showAndWait();
                goListeQuiz();
            } else {
                new Alert(Alert.AlertType.ERROR, "Update .").showAndWait();
            }

        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "ID Cours و Score .").showAndWait();
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
