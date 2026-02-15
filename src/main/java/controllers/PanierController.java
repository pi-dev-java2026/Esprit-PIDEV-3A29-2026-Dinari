package controllers;

import com.gestion.Services.ServiceCours;
import com.gestion.entities.Cours;
import com.gestion.utils.PanierStorage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class PanierController {

    @FXML private BorderPane rootPane;
    @FXML private ListView<Cours> listViewPanierCours;

    private final ObservableList<Cours> data = FXCollections.observableArrayList();
    private final ServiceCours serviceCours = new ServiceCours();

    @FXML
    public void initialize() {

        listViewPanierCours.setItems(data);

        listViewPanierCours.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Cours c, boolean empty) {
                super.updateItem(c, empty);

                if (empty || c == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                Label title = new Label(c.getNomCours());
                title.getStyleClass().add("card-title");

                Label sub = new Label(
                        c.getDescription() == null ? "" : c.getDescription()
                );
                sub.getStyleClass().add("card-sub");
                sub.setWrapText(true);

                VBox left = new VBox(6, title, sub);
                HBox.setHgrow(left, Priority.ALWAYS);

                Button btnRemove = new Button("Supprimer");
                btnRemove.getStyleClass().add("danger-btn");

                btnRemove.setOnAction(e -> {
                    PanierStorage.removeCours(c.getIdCours());
                    loadPanierCours();
                });


                HBox row = new HBox(12, left, btnRemove);

                VBox card = new VBox(row);
                card.getStyleClass().add("card");

                setGraphic(card);
            }
        });

        loadPanierCours();
    }

    private void loadPanierCours() {
        try {
            data.clear();

            for (int id : PanierStorage.getCoursIds()) {
                Cours c = serviceCours.getById(id);
                if (c != null) data.add(c);
            }

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR,
                    "Erreur panier: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void clearPanierCours() {
        PanierStorage.clearCours();
        loadPanierCours();
    }

    @FXML
    private void goListeCours() throws Exception {
        switchTo("/ListeCours.fxml");
    }

    @FXML
    private void goAjouterAvis() throws Exception {
        switchTo("/AjouterAvis.fxml");
    }
    @FXML
    private void goListeQuiz() throws Exception {
        switchTo("/ListeQuiz.fxml");
    }

    @FXML
    private void goPanier() {
        loadPanierCours();
    }

    private void switchTo(String fxml) throws Exception {
        Stage stage = (Stage) rootPane.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
        stage.setScene(new Scene(loader.load()));
    }

}
