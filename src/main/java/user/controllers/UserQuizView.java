package user.controllers;

import com.gestion.entities.Quiz;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class UserQuizView {

    @FXML private Label lblTitre;
    @FXML private Label lblScore;
    @FXML private Label lblQuestion;
    @FXML private VBox answersContainer;
    @FXML private Button btnSubmit;
    @FXML private Label lblResult;

    private Quiz quiz;
    private ToggleGroup answerGroup;

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;

        lblTitre.setText(quiz.getTitre());
        lblScore.setText("Score possible: " + quiz.getScoreDeQuiz() + " points");
        lblQuestion.setText("Question: " + quiz.getTitre());

        answerGroup = new ToggleGroup();
        answersContainer.getChildren().clear();

        List<String> reponses = quiz.getListeDeReponse();
        if (reponses != null && !reponses.isEmpty()) {
            for (String rep : reponses) {
                RadioButton rb = new RadioButton(rep);
                rb.setToggleGroup(answerGroup);
                rb.setStyle("-fx-font-size: 14px; -fx-padding: 8;");
                answersContainer.getChildren().add(rb);
            }
        } else {
            Label noAnswers = new Label("Aucune réponse disponible.");
            noAnswers.setStyle("-fx-text-fill: #DC2626;");
            answersContainer.getChildren().add(noAnswers);
            btnSubmit.setDisable(true);
        }
    }

    @FXML
    private void submitAnswer() {
        RadioButton selected = (RadioButton) answerGroup.getSelectedToggle();
        
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Veuillez sélectionner une réponse !").showAndWait();
            return;
        }

        String userAnswer = selected.getText();
        String correctAnswer = quiz.getReponseCorrect();

        if (userAnswer.equals(correctAnswer)) {
            lblResult.setText("✅ Correct! Vous avez gagné " + quiz.getScoreDeQuiz() + " points!");
            lblResult.setStyle("-fx-text-fill: #10B981; -fx-font-size: 16px; -fx-font-weight: bold;");
        } else {
            lblResult.setText("❌ Incorrect. La bonne réponse était: " + correctAnswer);
            lblResult.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 16px; -fx-font-weight: bold;");
        }

        lblResult.setVisible(true);
        btnSubmit.setDisable(true);
        
        // Disable all radio buttons after submission
        for (javafx.scene.Node node : answersContainer.getChildren()) {
            if (node instanceof RadioButton) {
                ((RadioButton) node).setDisable(true);
            }
        }
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) lblTitre.getScene().getWindow();
        stage.close();
    }
}
