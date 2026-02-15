package controllers;

import com.gestion.Services.ServiceCours;
import com.gestion.entities.Cours;
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
import com.gestion.utils.PanierStorage;


public class ListeCours {

    @FXML private TextField tfSearch;
    @FXML private ListView<Cours> listViewCours;

    private final ObservableList<Cours> data = FXCollections.observableArrayList();
    private FilteredList<Cours> filtered;

    private final ServiceCours service = new ServiceCours();

    @FXML
    public void initialize() {

        filtered = new FilteredList<>(data, c -> true);
        listViewCours.setItems(filtered);

        listViewCours.setCellFactory(lv -> new ListCell<>() {
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

                Label sub = new Label(c.getDescription() == null ? "" : c.getDescription());
                sub.getStyleClass().add("card-sub");
                sub.setWrapText(true);

                Label date = new Label(c.getDateCreation() == null ? "" : "Créé le: " + c.getDateCreation());
                date.getStyleClass().add("card-sub");

                VBox left = new VBox(6, title, sub, date);
                HBox.setHgrow(left, Priority.ALWAYS);

                Button btnEdit = new Button("Modifier");
                btnEdit.getStyleClass().add("link-btn");
                btnEdit.setOnAction(e -> onEdit(c));

                Button btnPanier = new Button("🧺 Ajouter");
                btnPanier.getStyleClass().add("primary-btn");
                btnPanier.setOnAction(e -> {
                    PanierStorage.addCoursId(c.getIdCours());
                    new Alert(Alert.AlertType.INFORMATION, "Ajouté au panier ✅").showAndWait();
                });

                Button btnDelete = new Button("Supprimer");
                btnDelete.getStyleClass().add("danger-btn");
                btnDelete.setOnAction(e -> onDelete(c));

                VBox right = new VBox(8, btnPanier, btnEdit, btnDelete);
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

    @FXML
    private void apply() {
        String key = (tfSearch.getText() == null) ? "" : tfSearch.getText().toLowerCase().trim();
        filtered.setPredicate(c -> {
            if (key.isEmpty()) return true;
            String nom = (c.getNomCours() == null) ? "" : c.getNomCours().toLowerCase();
            String desc = (c.getDescription() == null) ? "" : c.getDescription().toLowerCase();
            return nom.contains(key) || desc.contains(key);
        });
    }

    @FXML
    private void reset() {
        tfSearch.clear();
        filtered.setPredicate(c -> true);
    }

    @FXML
    private void refresh() {
        try {
            data.setAll(service.getAll());
            apply();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur MySQL: " + e.getMessage()).showAndWait();
        }
    }

    private void onEdit(Cours c) {
        try {
            Stage stage = (Stage) listViewCours.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierCours.fxml"));
            Scene scene = new Scene(loader.load());

            ModifierCours controller = loader.getController();
            controller.setCours(c);

            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur ouverture modifier: " + e.getMessage()).showAndWait();
        }
    }

    private void onDelete(Cours c) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer '" + c.getNomCours() + "' ?", ButtonType.YES, ButtonType.NO);

        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                try {
                    service.delete(c.getIdCours());
                    refresh();
                } catch (Exception e) {
                    e.printStackTrace();
                    new Alert(Alert.AlertType.ERROR, "Erreur suppression: " + e.getMessage()).showAndWait();
                }
            }
        });
    }

    @FXML
    private void goAjouterCours() throws Exception {
        switchTo("/AjouterCours.fxml");
    }

    @FXML
    private void goListeQuiz() throws Exception {
        switchTo("/ListeQuiz.fxml");
    }
    @FXML
    private void goPanier() throws Exception {
        switchTo("/Panier.fxml");
    }

    @FXML
    private void goAjouterQuiz() throws Exception {
        switchTo("/AjouterQuiz.fxml");
    }

    @FXML
    private void goHome() {
        System.out.println("Home clicked");
    }

    @FXML
    private void goListeCours() {
        refresh();
    }

    private void switchTo(String fxml) throws Exception {
        Stage stage = (Stage) listViewCours.getScene().getWindow();
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
