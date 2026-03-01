package user.controllers;

import com.gestion.entities.Quiz;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;

public class UserExamQuizView {

    @FXML private Label lblTitre;
    @FXML private Label lblScore;
    @FXML private Label lblTimer;
    @FXML private Label lblQuestion;
    @FXML private VBox answersContainer;
    @FXML private Button btnSubmit;
    @FXML private Label lblResult;

    private Quiz quiz;
    private ToggleGroup answerGroup;
    private Timeline timeline;
    private int remainingSeconds;
    private boolean isSubmitted = false;

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

        startTimer();
    }

    private void startTimer() {
        remainingSeconds = quiz.getTimeLimit() * 60;
        updateTimerDisplay();

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            remainingSeconds--;
            updateTimerDisplay();

            if (remainingSeconds <= 0) {
                timeline.stop();
                autoSubmit();
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void updateTimerDisplay() {
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;
        String timeText = String.format("⏱️ Temps restant: %02d:%02d", minutes, seconds);
        
        lblTimer.setText(timeText);
        
        if (remainingSeconds <= 60) {
            lblTimer.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 18px; -fx-font-weight: bold;");
        } else if (remainingSeconds <= 180) {
            lblTimer.setStyle("-fx-text-fill: #F59E0B; -fx-font-size: 18px; -fx-font-weight: bold;");
        } else {
            lblTimer.setStyle("-fx-text-fill: #10B981; -fx-font-size: 18px; -fx-font-weight: bold;");
        }
    }

    private void autoSubmit() {
        if (isSubmitted) return;
        
        Platform.runLater(() -> {
            new Alert(Alert.AlertType.WARNING, "⏰ Temps écoulé! Le quiz est soumis automatiquement.").showAndWait();
            submitAnswer();
        });
    }

    @FXML
    private void submitAnswer() {
        if (isSubmitted) return;
        
        if (timeline != null) {
            timeline.stop();
        }

        RadioButton selected = (RadioButton) answerGroup.getSelectedToggle();
        
        if (selected == null) {
            lblResult.setText("❌ Aucune réponse sélectionnée. Score: 0 points");
            lblResult.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 16px; -fx-font-weight: bold;");
        } else {
            String userAnswer = selected.getText();
            String correctAnswer = quiz.getReponseCorrect();

            if (userAnswer.equals(correctAnswer)) {
                lblResult.setText("✅ Correct! Vous avez gagné " + quiz.getScoreDeQuiz() + " points!");
                lblResult.setStyle("-fx-text-fill: #10B981; -fx-font-size: 16px; -fx-font-weight: bold;");
            } else {
                lblResult.setText("❌ Incorrect. La bonne réponse était: " + correctAnswer);
                lblResult.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 16px; -fx-font-weight: bold;");
            }
        }

        lblResult.setVisible(true);
        btnSubmit.setDisable(true);
        isSubmitted = true;
        
        for (javafx.scene.Node node : answersContainer.getChildren()) {
            if (node instanceof RadioButton) {
                ((RadioButton) node).setDisable(true);
            }
        }
    }

    @FXML
    private void closeWindow() {
        if (timeline != null) {
            timeline.stop();
        }
        Stage stage = (Stage) lblTitre.getScene().getWindow();
        stage.close();
    }
}
