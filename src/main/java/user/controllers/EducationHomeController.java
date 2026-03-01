package user.controllers;

import com.gestion.Services.ServiceCours;
import com.gestion.Services.ServiceQuiz;
import com.gestion.entities.Cours;
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

public class EducationHomeController {

    @FXML
    private TabPane tabPane;

    private final ServiceCours serviceCours = new ServiceCours();
    private final ServiceQuiz serviceQuiz = new ServiceQuiz();

    @FXML
    public void initialize() {
        Tab coursTab = new Tab("📚 Mes Cours");
        coursTab.setContent(createCoursContent());
        
        Tab quizTab = new Tab("📝 Mes Quiz");
        quizTab.setContent(createQuizContent());
        
        tabPane.getTabs().addAll(coursTab, quizTab);
    }

    private VBox createCoursContent() {
        VBox content = new VBox();
        content.getStyleClass().add("content");
        content.setSpacing(10);

        HBox header = new HBox(10);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label title = new Label("Modules de Formation Disponibles");
        title.getStyleClass().add("page-title");
        javafx.scene.layout.Pane spacer = new javafx.scene.layout.Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button btnRefresh = new Button("🔄 Rafraîchir");
        btnRefresh.getStyleClass().add("ghost-btn");
        header.getChildren().addAll(title, spacer, btnRefresh);

        HBox toolbar = new HBox(10);
        toolbar.getStyleClass().add("toolbar");
        toolbar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        TextField tfSearch = new TextField();
        tfSearch.setPromptText("Rechercher un module de formation...");
        tfSearch.getStyleClass().add("input");
        HBox.setHgrow(tfSearch, Priority.ALWAYS);
        Button btnSearch = new Button("Rechercher");
        btnSearch.getStyleClass().add("primary-btn");
        Button btnReset = new Button("Réinitialiser");
        btnReset.getStyleClass().add("ghost-btn");
        toolbar.getChildren().addAll(tfSearch, btnSearch, btnReset);

        ListView<Cours> listView = new ListView<>();
        VBox.setVgrow(listView, Priority.ALWAYS);

        ObservableList<Cours> data = FXCollections.observableArrayList();
        FilteredList<Cours> filtered = new FilteredList<>(data, c -> true);
        listView.setItems(filtered);

        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Cours c, boolean empty) {
                super.updateItem(c, empty);
                if (empty || c == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                Label lblTitle = new Label(safe(c.getNomCours()));
                lblTitle.getStyleClass().add("card-title");
                Label lblDesc = new Label(safe(c.getDescription()));
                lblDesc.getStyleClass().add("card-sub");
                lblDesc.setWrapText(true);
                String dateTxt = (c.getDateCreation() == null) ? "" : "Créé le: " + c.getDateCreation();
                Label lblDate = new Label(dateTxt);
                lblDate.getStyleClass().add("card-sub");

                VBox left = new VBox(6, lblTitle, lblDesc, lblDate);
                HBox.setHgrow(left, Priority.ALWAYS);

                Button btnView = new Button("📖 Voir Détails");
                btnView.getStyleClass().add("primary-btn");
                btnView.setOnAction(e -> viewCours(c));

                VBox right = new VBox(8, btnView);
                right.setMinWidth(130);

                HBox row = new HBox(12, left, right);
                VBox card = new VBox(row);
                card.getStyleClass().add("card");
                setGraphic(card);
            }
        });

        tfSearch.textProperty().addListener((obs, o, n) -> {
            String key = (tfSearch.getText() == null) ? "" : tfSearch.getText().toLowerCase().trim();
            filtered.setPredicate(c -> {
                if (key.isEmpty()) return true;
                String nom = safe(c.getNomCours()).toLowerCase();
                String desc = safe(c.getDescription()).toLowerCase();
                return nom.contains(key) || desc.contains(key);
            });
        });

        btnReset.setOnAction(e -> {
            tfSearch.clear();
            filtered.setPredicate(c -> true);
        });

        Runnable refreshCours = () -> {
            try {
                data.setAll(serviceCours.getAll());
            } catch (Exception ex) {
                ex.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Erreur: " + ex.getMessage()).showAndWait();
            }
        };

        btnRefresh.setOnAction(e -> refreshCours.run());
        btnSearch.setOnAction(e -> {
            String key = (tfSearch.getText() == null) ? "" : tfSearch.getText().toLowerCase().trim();
            filtered.setPredicate(c -> {
                if (key.isEmpty()) return true;
                String nom = safe(c.getNomCours()).toLowerCase();
                String desc = safe(c.getDescription()).toLowerCase();
                return nom.contains(key) || desc.contains(key);
            });
        });

        refreshCours.run();

        content.getChildren().addAll(header, toolbar, listView);
        return content;
    }

    private VBox createQuizContent() {
        VBox content = new VBox();
        content.getStyleClass().add("content");
        content.setSpacing(10);

        HBox header = new HBox(10);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label title = new Label("Tous les Quiz Disponibles");
        title.getStyleClass().add("page-title");
        javafx.scene.layout.Pane spacer = new javafx.scene.layout.Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button btnRefresh = new Button("Rafraîchir");
        btnRefresh.getStyleClass().add("ghost-btn");
        header.getChildren().addAll(title, spacer, btnRefresh);

        HBox toolbar = new HBox(10);
        toolbar.getStyleClass().add("toolbar");
        toolbar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        toolbar.setSpacing(10);
        TextField tfSearch = new TextField();
        tfSearch.setPromptText("Rechercher un quiz...");
        tfSearch.getStyleClass().add("input");
        HBox.setHgrow(tfSearch, Priority.ALWAYS);
        Button btnApply = new Button("Appliquer");
        btnApply.getStyleClass().add("primary-btn");
        Button btnReset = new Button("Reset");
        btnReset.getStyleClass().add("ghost-btn");
        toolbar.getChildren().addAll(tfSearch, btnApply, btnReset);

        ListView<Quiz> listView = new ListView<>();
        VBox.setVgrow(listView, Priority.ALWAYS);

        ObservableList<Quiz> data = FXCollections.observableArrayList();
        FilteredList<Quiz> filtered = new FilteredList<>(data, q -> true);
        listView.setItems(filtered);

        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Quiz q, boolean empty) {
                super.updateItem(q, empty);
                if (empty || q == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                Label lblTitle = new Label(safe(q.getTitre()));
                lblTitle.getStyleClass().add("card-title");
                String modeLabel = q.isExamMode() ? " ⏱️ Exam Mode (" + q.getTimeLimit() + " min)" : " Normal";
                String info = "Score: " + q.getScoreDeQuiz() + " points" + modeLabel;
                Label lblSub = new Label(info);
                lblSub.getStyleClass().add("card-sub");
                String dateTxt = (q.getDateCreation() == null) ? "" : "Créé le: " + q.getDateCreation();
                Label lblDate = new Label(dateTxt);
                lblDate.getStyleClass().add("card-sub");

                VBox left = new VBox(6, lblTitle, lblSub, lblDate);
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

        tfSearch.textProperty().addListener((obs, o, n) -> {
            String key = (tfSearch.getText() == null) ? "" : tfSearch.getText().toLowerCase().trim();
            filtered.setPredicate(q -> {
                if (key.isEmpty()) return true;
                String titre = safe(q.getTitre()).toLowerCase();
                return titre.contains(key);
            });
        });

        btnReset.setOnAction(e -> {
            tfSearch.clear();
            filtered.setPredicate(q -> true);
        });

        Runnable refreshQuiz = () -> {
            try {
                data.setAll(serviceQuiz.getAll());
            } catch (Exception ex) {
                ex.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Erreur: " + ex.getMessage()).showAndWait();
            }
        };

        btnRefresh.setOnAction(e -> refreshQuiz.run());
        btnApply.setOnAction(e -> {
            String key = (tfSearch.getText() == null) ? "" : tfSearch.getText().toLowerCase().trim();
            filtered.setPredicate(q -> {
                if (key.isEmpty()) return true;
                String titre = safe(q.getTitre()).toLowerCase();
                return titre.contains(key);
            });
        });

        refreshQuiz.run();

        content.getChildren().addAll(header, toolbar, listView);
        return content;
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private void viewCours(Cours c) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/UserAfficherCours.fxml"));
            Scene scene = new Scene(loader.load());
            UserAfficherCours controller = loader.getController();
            controller.setCours(c);
            Stage stage = (Stage) tabPane.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur: " + e.getMessage()).showAndWait();
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
    private void goHome() {
        try {
            UserMain.switchScene("EducationHome.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goEducation() {
        try {
            UserMain.switchScene("EducationHome.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
