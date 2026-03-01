package user.controllers;

import com.gestion.Services.ServiceQuiz;
import com.gestion.entities.Quiz;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class UserListeQuiz {

    @FXML
    private TextField tfSearch;
    @FXML
    private ListView<Quiz> listViewQuiz;

    private final ObservableList<Quiz> data = FXCollections.observableArrayList();
    private FilteredList<Quiz> filtered;

    private final ServiceQuiz service = new ServiceQuiz();

    @FXML
    public void initialize() {

        filtered = new FilteredList<>(data, q -> true);
        listViewQuiz.setItems(filtered);

        listViewQuiz.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Quiz q, boolean empty) {
                super.updateItem(q, empty);

                if (empty || q == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                Label title = new Label(safe(q.getTitre()));
                title.getStyleClass().add("card-title");

                String modeLabel = q.isExamMode() ? " ⏱️ Exam Mode (" + q.getTimeLimit() + " min)" : " Normal";
                String info = "Score: " + q.getScoreDeQuiz() + " points" + modeLabel;
                Label sub = new Label(info);
                sub.getStyleClass().add("card-sub");

                String dateTxt = (q.getDateCreation() == null) ? "" : "Créé le: " + q.getDateCreation();
                Label date = new Label(dateTxt);
                date.getStyleClass().add("card-sub");

                VBox left = new VBox(6, title, sub, date);
                HBox.setHgrow(left, Priority.ALWAYS);

                Button btnStart = new Button("🎯 Commencer");
                btnStart.getStyleClass().add("primary-btn");
                btnStart.setOnAction(e -> startQuiz(q));

                VBox right = new VBox(8, btnStart);
                right.setMinWidth(130);

                HBox row = new HBox(12, left, right);
                VBox card = new VBox(row);
                card.getStyleClass().add("card");

                setGraphic(card);
            }
        });

        tfSearch.textProperty().addListener((obs, o, n) -> apply());

        refresh();
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    @FXML
    private void apply() {
        String key = (tfSearch.getText() == null) ? "" : tfSearch.getText().toLowerCase().trim();
        filtered.setPredicate(q -> {
            if (key.isEmpty()) return true;
            String titre = safe(q.getTitre()).toLowerCase();
            return titre.contains(key);
        });
    }

    @FXML
    private void reset() {
        tfSearch.clear();
        filtered.setPredicate(q -> true);
    }

    @FXML
    private void refresh() {
        try {
            data.setAll(service.getAll());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading quizzes: " + e.getMessage());
            new Alert(Alert.AlertType.ERROR, "Erreur de chargement des quiz: " + e.getMessage()).showAndWait();
            // Don't clear data - keep existing quizzes visible
        }
    }

    private void startQuiz(Quiz q) {
        try {
            String fxmlPath = q.isExamMode() ? "/user/UserExamQuizView.fxml" : "/user/UserQuizView.fxml";
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            if (q.isExamMode()) {
                UserExamQuizView controller = loader.getController();
                controller.setQuiz(q);
            } else {
                UserQuizView controller = loader.getController();
                controller.setQuiz(q);
            }

            Stage stage = new Stage();
            String modeLabel = q.isExamMode() ? " (Exam ⏱️)" : "";
            stage.setTitle("Quiz: " + q.getTitre() + modeLabel);
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void goListeCours() throws Exception {
        switchTo("/user/UserListeCours.fxml");
    }

    @FXML
    private void goMyQuizzes() {
        refresh();
    }

    @FXML
    private void goHome() throws Exception {
        switchTo("/user/EducationHome.fxml");
    }

    @FXML
    private void goEducation() throws Exception {
        switchTo("/user/EducationHome.fxml");
    }

    private void switchTo(String fxml) throws Exception {
        Stage stage = (Stage) listViewQuiz.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
        stage.setScene(new Scene(loader.load()));
    }
}
