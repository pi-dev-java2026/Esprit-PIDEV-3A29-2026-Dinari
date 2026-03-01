package controllers;

import com.gestion.Services.ServiceQuiz;
import com.gestion.entities.Quiz;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ListeQuiz {

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

                String modeLabel = q.isExamMode() ? " | Mode: Exam ⏱️ (" + q.getTimeLimit() + " min)" : " | Mode: Normal";
                String info = "Cours ID: " + q.getIdCours()
                        + " | Score: " + q.getScoreDeQuiz()
                        + modeLabel
                        + " | Rép. correcte: " + safe(q.getReponseCorrect());
                Label sub = new Label(info);
                sub.getStyleClass().add("card-sub");
                sub.setWrapText(true);

                String dateTxt = (q.getDateCreation() == null) ? "" : "Créé le: " + q.getDateCreation();
                Label date = new Label(dateTxt);
                date.getStyleClass().add("card-sub");

                VBox left = new VBox(6, title, sub, date);
                HBox.setHgrow(left, Priority.ALWAYS);

                Button btnEdit = new Button("Modifier");
                btnEdit.getStyleClass().add("link-btn");
                btnEdit.setOnAction(e -> onEdit(q));

                Button btnDelete = new Button("Supprimer");
                btnDelete.getStyleClass().add("danger-btn");
                btnDelete.setOnAction(e -> onDelete(q));

                VBox right = new VBox(8, btnEdit, btnDelete);
                right.setMinWidth(110);

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
            String correcte = safe(q.getReponseCorrect()).toLowerCase();
            return titre.contains(key) || correcte.contains(key);
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
            new Alert(Alert.AlertType.ERROR, "Erreur MySQL: " + e.getMessage()).showAndWait();
        }
    }

    private void onEdit(Quiz q) {
        try {
            Stage stage = (Stage) listViewQuiz.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierQuiz.fxml"));
            Scene scene = new Scene(loader.load());

            ModifierQuiz controller = loader.getController();
            controller.setQuiz(q);

            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur ouverture modifier: " + e.getMessage()).showAndWait();
        }
    }

    private void onDelete(Quiz q) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer '" + safe(q.getTitre()) + "' ?", ButtonType.YES, ButtonType.NO);

        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                try {
                    service.delete(q.getIdQuiz());
                    refresh();
                } catch (Exception e) {
                    e.printStackTrace();
                    new Alert(Alert.AlertType.ERROR, "Erreur suppression: " + e.getMessage()).showAndWait();
                }
            }
        });
    }


    @FXML
    private void goListeQuiz() {
        refresh();
    }

    @FXML
    private void goAjouterQuiz() throws Exception {
        Stage stage = (Stage) listViewQuiz.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterQuiz.fxml"));
        stage.setScene(new Scene(loader.load()));
    }

    @FXML
    private void goListeCours() throws Exception {
        Stage stage = (Stage) listViewQuiz.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ListeCours.fxml"));
        stage.setScene(new Scene(loader.load()));
    }

    @FXML
    private void goAjouterCours() throws Exception {
        Stage stage = (Stage) listViewQuiz.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterCours.fxml"));
        stage.setScene(new Scene(loader.load()));
    }
    private void switchTo(String fxml) throws Exception {
        Stage stage = (Stage) listViewQuiz.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
        stage.setScene(new Scene(loader.load()));
    }

    @FXML
    private void goAjouterAvis() {
        try {
            switchTo("/AjouterAvis.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Navigation erreur: " + e.getMessage()).showAndWait();
        }
    }
    @FXML
    private void goListeAvis() throws Exception {
        switchTo("/ListeAvis.fxml");
    }

}
