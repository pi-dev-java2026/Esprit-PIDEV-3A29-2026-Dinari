package controllers;

import com.gestion.Services.ServiceAvis;
import com.gestion.Services.ServiceQuiz;
import com.gestion.entities.Avis;
import com.gestion.entities.AvisQuizView;
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

public class ListeAvis {

    @FXML private TextField tfSearch;
    @FXML private ListView<AvisQuizView> listViewAvis;

    private final ObservableList<AvisQuizView> data = FXCollections.observableArrayList();
    private FilteredList<AvisQuizView> filtered;

    private final ServiceAvis serviceAvis = new ServiceAvis();
    private final ServiceQuiz serviceQuiz = new ServiceQuiz();

    @FXML
    public void initialize() {
        filtered = new FilteredList<>(data, a -> true);
        listViewAvis.setItems(filtered);

        listViewAvis.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(AvisQuizView a, boolean empty) {
                super.updateItem(a, empty);

                if (empty || a == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                Label title = new Label("Quiz: " + safe(a.getQuizTitre()));
                title.getStyleClass().add("card-title");

                Label note = new Label("Note: " + a.getNote() + "  |  ID Quiz: " + a.getIdQuiz());
                note.getStyleClass().add("card-sub");

                Label com = new Label(safe(a.getCommentaire()));
                com.getStyleClass().add("card-sub");
                com.setWrapText(true);

                Label date = new Label(a.getDateCreation() == null ? "" : "Créé le: " + a.getDateCreation());
                date.getStyleClass().add("card-sub");

                VBox left = new VBox(6, title, note, com, date);
                HBox.setHgrow(left, Priority.ALWAYS);

                VBox card = new VBox(new HBox(12, left));
                card.getStyleClass().add("card");

                setGraphic(card);
            }
        });

        tfSearch.textProperty().addListener((obs, o, n) -> apply());

        refresh();
    }

    private String safe(String s) { return s == null ? "" : s; }

    private void apply() {
        String key = (tfSearch.getText() == null) ? "" : tfSearch.getText().toLowerCase().trim();
        filtered.setPredicate(a -> {
            if (key.isEmpty()) return true;
            return safe(a.getQuizTitre()).toLowerCase().contains(key)
                    || safe(a.getCommentaire()).toLowerCase().contains(key);
        });
    }

    @FXML
    private void reset() {
        tfSearch.clear();
        filtered.setPredicate(a -> true);
    }

    @FXML
    private void refresh() {
        try {
            data.clear();
            for (Avis a : serviceAvis.getAll()) {
                String titre = serviceQuiz.getTitreById(a.getIdQuiz());
                data.add(new AvisQuizView(
                        a.getIdAvis(),
                        a.getIdQuiz(),
                        titre == null ? "(Quiz supprimé?)" : titre,
                        a.getCommentaire(),
                        a.getNote(),
                        a.getDateCreation()
                ));
            }
            apply();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur: " + e.getMessage()).showAndWait();
        }
    }

    // Navigation
    @FXML private void goListeAvis() { refresh(); }

    @FXML private void goListeCours() throws Exception { switchTo("/ListeCours.fxml"); }

    @FXML private void goListeQuiz() throws Exception { switchTo("/ListeQuiz.fxml"); }

    private void switchTo(String fxml) throws Exception {
        Stage stage = (Stage) listViewAvis.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
        stage.setScene(new Scene(loader.load()));
    }
}
