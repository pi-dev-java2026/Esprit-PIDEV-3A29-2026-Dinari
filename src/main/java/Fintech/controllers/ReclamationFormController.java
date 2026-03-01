package Fintech.controllers;

import Fintech.entities.Reclamation;
import Fintech.servicies.ServiceReclamation;
import Fintech.utils.UserSession;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ReclamationFormController implements Initializable {

    @FXML
    private TextField emailField;
    @FXML
    private TextField subjectField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private ComboBox<String> statutComboBox;
    @FXML
    private VBox statutSection;
    @FXML
    private Label formTitleLabel;
    @FXML
    private Button clearButton;
    @FXML
    private Button submitButton;

    private ServiceReclamation serviceReclamation = new ServiceReclamation();
    private Reclamation reclamation;
    private Runnable updateCallback;
    /** true when admin is editing an existing reclamation (role-only mode) */
    private boolean adminEditMode = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        statutComboBox.setItems(FXCollections.observableArrayList(
                "En attente", "En cours", "Traitée", "Refusée"));

        Fintech.entities.User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser != null) {
            emailField.setText(currentUser.getEmail());
            // Standard users cannot change the email (it's their own)
            if (!UserSession.getInstance().isAdmin()) {
                emailField.setDisable(true);
            }
        }
    }

    /**
     * Définit une réclamation existante pour modification.
     * Si l'utilisateur est admin, seul le statut sera modifiable.
     */
    public void setReclamation(Reclamation reclamation) {
        this.reclamation = reclamation;
        if (reclamation != null) {
            emailField.setText(reclamation.getEmail());
            subjectField.setText(reclamation.getSubject());
            descriptionField.setText(reclamation.getDescription());
            statutComboBox.setValue(reclamation.getStatut());

            boolean isAdmin = UserSession.getInstance().isAdmin();

            if (isAdmin) {
                // Admin edit mode: only statut is modifiable
                adminEditMode = true;

                // Update header
                if (formTitleLabel != null) {
                    formTitleLabel.setText("Modifier l'état de la réclamation");
                }

                // Show statut ComboBox
                statutSection.setVisible(true);
                statutSection.setManaged(true);

                // Lock all other fields
                emailField.setEditable(false);
                emailField.setStyle("-fx-opacity: 0.7;");
                subjectField.setEditable(false);
                subjectField.setStyle("-fx-opacity: 0.7;");
                descriptionField.setEditable(false);
                descriptionField.setStyle("-fx-opacity: 0.7;");

                // Hide clear button, update submit label
                clearButton.setVisible(false);
                clearButton.setManaged(false);
                submitButton.setText("Enregistrer");
            } else {
                // User edit mode: subject and description are modifiable, status is hidden
                adminEditMode = false;

                if (formTitleLabel != null) {
                    formTitleLabel.setText("Modifier ma réclamation");
                }

                // Keep statut hidden
                statutSection.setVisible(false);
                statutSection.setManaged(false);

                // Lock email field
                emailField.setEditable(false);
                emailField.setStyle("-fx-opacity: 0.7;");

                // Allow editing subject & description
                subjectField.setEditable(true);
                subjectField.setStyle("-fx-opacity: 1.0;");
                descriptionField.setEditable(true);
                descriptionField.setStyle("-fx-opacity: 1.0;");

                submitButton.setText("Mettre à jour");
            }
        }
    }

    public void setUpdateCallback(Runnable updateCallback) {
        this.updateCallback = updateCallback;
    }

    @FXML
    private void handleSubmit(ActionEvent event) {

        // ── ADMIN edit mode: only save the statut ────────────────────────────
        if (adminEditMode && reclamation != null) {
            String newStatut = statutComboBox.getValue();
            if (newStatut == null) {
                showAlert("Erreur", "Veuillez sélectionner un état.", Alert.AlertType.ERROR);
                return;
            }
            try {
                reclamation.setStatut(newStatut);
                serviceReclamation.modifier(reclamation);
                showAlert("Succès", "État mis à jour avec succès !", Alert.AlertType.INFORMATION);
                if (updateCallback != null)
                    updateCallback.run();
                handleBack(null);
            } catch (SQLException e) {
                showAlert("Erreur", "Erreur lors de la mise à jour : " + e.getMessage(), Alert.AlertType.ERROR);
            }
            return;
        }

        // ── Normal path: user creating / editing their own reclamation ───────
        String email = emailField.getText().trim();
        String subject = subjectField.getText().trim();
        String description = descriptionField.getText().trim();

        if (email.isEmpty() || subject.isEmpty() || description.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs.", Alert.AlertType.ERROR);
            return;
        }
        if (!isValidEmail(email)) {
            showAlert("Erreur de validation", "L'email doit contenir le caractère '@'.", Alert.AlertType.ERROR);
            return;
        }
        if (subject.length() < 3) {
            showAlert("Erreur de validation", "Le sujet doit contenir au moins 3 caractères.", Alert.AlertType.ERROR);
            return;
        }
        if (description.length() < 10) {
            showAlert("Erreur de validation", "La description doit contenir au moins 10 caractères.",
                    Alert.AlertType.ERROR);
            return;
        }

        try {
            if (reclamation == null) {
                Reclamation newReclamation = new Reclamation();
                newReclamation.setEmail(email);
                newReclamation.setSubject(subject);
                newReclamation.setDescription(description);
                newReclamation.setStatut("En attente");
                serviceReclamation.ajouter(newReclamation);
                showAlert("Succès", "Réclamation soumise avec succès!", Alert.AlertType.INFORMATION);
            } else {
                reclamation.setEmail(email);
                reclamation.setSubject(subject);
                reclamation.setDescription(description);
                serviceReclamation.modifier(reclamation);
                showAlert("Succès", "Réclamation modifiée avec succès!", Alert.AlertType.INFORMATION);
            }

            if (updateCallback != null)
                updateCallback.run();
            handleClear(null);

        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de l'enregistrement : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean isValidEmail(String email) {
        return email.contains("@");
    }

    @FXML
    private void handleClear(ActionEvent event) {
        emailField.clear();
        subjectField.clear();
        descriptionField.clear();
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/Fintech/views/ReclamationManagement.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) emailField.getScene().getWindow();
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (java.io.IOException e) {
            showAlert("Erreur", "Erreur lors de la navigation: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
