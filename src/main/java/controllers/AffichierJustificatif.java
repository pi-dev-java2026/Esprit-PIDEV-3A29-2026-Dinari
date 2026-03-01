package controllers;

import entities.JustificatifDepense;
import entities.depense;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import services.ServiceJutificatifDepense;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.List;

public class AffichierJustificatif {
    private depense depenseSelectionnee;

    @FXML private VBox vboxDepenses;
    private ServiceJutificatifDepense service = new ServiceJutificatifDepense();

    @FXML
    public void initialize() {

    }

    public void setDepense(depense d) {
        this.depenseSelectionnee = d;
        loadDepenses();
    }

    private void loadDepenses() {
        try {
            List<JustificatifDepense> list = service.afficherById(depenseSelectionnee.getId_depense());
            vboxDepenses.getChildren().clear();

            for (JustificatifDepense d : list) {
                vboxDepenses.getChildren().add(createDepenseCard(d));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private HBox createDepenseCard(JustificatifDepense d) {
        HBox card = new HBox(15);
        card.setPadding(new Insets(10));
        card.setStyle("""
                -fx-background-color: #ffffff;
                -fx-background-radius: 10;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5,0,0,2);
                """);

        VBox details = new VBox(5);
        Label date = new Label("📅 Date ajout : " + d.getDateajout());
        Label type = new Label("📂 Type fichier : " + d.getTypefichier());
        Label path = new Label("🖼️ Chemin : " + d.getFilepath());

        details.getChildren().addAll(type, path, date);
        HBox.setHgrow(details, Priority.ALWAYS);

        VBox actions = new VBox(8);
        Button btnOuvrir = new Button("Ouvrir");
        Button btnDownload = new Button("Télécharger");
        Button btnSupprimer = new Button("Supprimer");

        btnOuvrir.setStyle("""
                -fx-background-color:#0078D7;
                -fx-text-fill:white;
                -fx-background-radius:5;
                """);

        btnDownload.setStyle("""
                -fx-background-color:#008000;
                -fx-text-fill:white;
                -fx-background-radius:5;
                """);

        btnSupprimer.setStyle("""
                -fx-background-color:#D32F2F;
                -fx-text-fill:white;
                -fx-background-radius:5;
                """);

        // Nouvelle fonctionnalité : Ouvrir le fichier
        btnOuvrir.setOnAction(ev -> {
            try {
                File file = new File(d.getFilepath());
                if (file.exists()) {
                    java.awt.Desktop.getDesktop().open(file);
                } else {
                    new Alert(Alert.AlertType.ERROR, "Fichier introuvable !").show();
                }
            } catch (IOException e) {
                new Alert(Alert.AlertType.ERROR, "Erreur lors de l'ouverture : " + e.getMessage()).show();
            }
        });

        // Nouvelle fonctionnalité : Télécharger le fichier
        btnDownload.setOnAction(ev -> {
            FileChooser fileChooser = new FileChooser();
            File sourceFile = new File(d.getFilepath());
            fileChooser.setInitialFileName(sourceFile.getName());

            File targetFile = fileChooser.showSaveDialog(vboxDepenses.getScene().getWindow());
            if (targetFile != null) {
                try {
                    Files.copy(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    new Alert(Alert.AlertType.INFORMATION, "Fichier téléchargé ✔").show();
                } catch (IOException e) {
                    new Alert(Alert.AlertType.ERROR, "Erreur lors du téléchargement : " + e.getMessage()).show();
                }
            }
        });

        btnSupprimer.setOnAction(ev -> {
            try {
                service.supprimer(d.getIdJustificatif());
                loadDepenses(); // refresh
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        actions.getChildren().addAll(btnOuvrir, btnDownload, btnSupprimer);
        card.getChildren().addAll(details, actions);
        return card;
    }

    @FXML private void showHome() { System.out.println("Home clicked"); }
    @FXML private void showDepenses() { System.out.println("Depenses clicked"); }
    @FXML private void showAbonnement() { System.out.println("Abonnement clicked"); }
    @FXML private void showEducative() { System.out.println("Educative clicked"); }
    @FXML private void showBudget() { System.out.println("Budget clicked"); }
    @FXML private void filterDepenses() { System.out.println("Filtrer déclenché !"); }
    @FXML
    void showDashboard(ActionEvent event) {
        try {
            // Création du loader instance pour pouvoir récupérer le root
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Dashboard.fxml"));

            // Charge le FXML
            Parent root = loader.load();

            // Remplace le root actuel de la scène
            vboxDepenses.getScene().setRoot(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void openAjouterJustifDepense() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterJustificatifDepense.fxml"));
            Parent root = loader.load();

            AjouterJustificatif controller = loader.getController();
            controller.setDepense(depenseSelectionnee);

            Stage stage = new Stage();
            stage.setTitle("Ajouter un justificatif Dépense");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadDepenses(); // refresh après ajout
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
