package controllers;

import entities.JustificatifDepense;
import entities.depense;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import services.ServiceJutificatifDepense;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;

public class AjouterJustificatif {

    @FXML private DatePicker dateField;
    @FXML private TextField typeField;
    @FXML private TextField cheminField;
    @FXML private Button btnParcourir;
    private depense depenseSelectionnee;

    private ServiceJutificatifDepense service = new ServiceJutificatifDepense();

    @FXML
    public void initialize() {
        // Valeur par défaut et désactivation des champs
        dateField.setValue(LocalDate.now());
        dateField.setDisable(true);
        typeField.setDisable(true);
        cheminField.setDisable(true);
    }

    public void setDepense(depense d) {
        this.depenseSelectionnee = d;

    }

    // ========================
    // Upload fichier
    // ========================
    @FXML
    void uploadFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner un fichier justificatif");

        // Filtrer les types de fichiers (images et PDF)
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("PDF", "*.pdf"),
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(btnParcourir.getScene().getWindow());

        if (selectedFile != null) {
            try {
                // Créer le dossier uploads s'il n'existe pas
                File uploadsDir = new File("uploads");
                if (!uploadsDir.exists()) uploadsDir.mkdirs();

                // Copier le fichier dans uploads
                Path dest = Paths.get(uploadsDir.getAbsolutePath(), selectedFile.getName());
                Files.copy(selectedFile.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);

                // Stocker le chemin relatif dans le champ
                String relativePath = "uploads/" + selectedFile.getName();
                cheminField.setText(relativePath);

                // Stocker le type de fichier
                typeField.setText(getFileExtension(selectedFile.getName()));

            } catch (IOException e) {
                new Alert(Alert.AlertType.ERROR, "Erreur lors de l'upload : " + e.getMessage()).show();
            }
        }
    }

    // ========================
    // Ajouter le justificatif dans la base
    // ========================
    @FXML
    void ajouterD(ActionEvent actionEvent) {
        if (dateField.getValue() == null ||
                typeField.getText().isEmpty() ||
                cheminField.getText().isEmpty()) {

            new Alert(Alert.AlertType.WARNING, "Veuillez remplir tous les champs obligatoires.").show();
            return;
        }

        try {
            JustificatifDepense justificatif = new JustificatifDepense();
            justificatif.setFilepath(cheminField.getText()); // chemin relatif
            justificatif.setTypefichier(typeField.getText());
            justificatif.setDateajout(Date.valueOf(dateField.getValue()));
            justificatif.setIdDepense(depenseSelectionnee.getId_depense()); // à remplacer par la vraie dépense sélectionnée

            service.ajouter(justificatif);

            new Alert(Alert.AlertType.INFORMATION, "Justificatif ajouté ✔").show();

            // Réinitialiser les champs
            cheminField.clear();
            typeField.clear();
            dateField.setValue(LocalDate.now());

        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Erreur SQL: " + e.getMessage()).show();
        }
    }

    // ========================
    // Navigation vers AfficherDepense.fxml
    // ========================
    @FXML
    void naviguer(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherDepense.fxml"));
            Parent root = loader.load();

            // Remplacer le contenu actuel par la scène AfficherDepense
            dateField.getScene().setRoot(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ========================
    // Utilitaire pour récupérer l'extension d'un fichier
    // ========================
    private String getFileExtension(String fileName) {
        int lastIndex = fileName.lastIndexOf(".");
        if (lastIndex > 0 && lastIndex < fileName.length() - 1) {
            return fileName.substring(lastIndex + 1);
        } else {
            return "";
        }
    }
}
