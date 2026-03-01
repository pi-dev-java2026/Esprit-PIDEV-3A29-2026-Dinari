package Fintech.controllers;

import Fintech.entities.Reclamation;
import Fintech.servicies.ServiceReclamation;
import Fintech.utils.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

public class ReclamationManagementController implements Initializable {

    @FXML
    private TextField searchField;
    @FXML
    private Label profileNameLabel;
    @FXML
    private Label profileRoleLabel;
    @FXML
    private Label profileAvatarLabel;
    @FXML
    private Label totalReclamationsLabel;
    @FXML
    private Label reclamationCountLabel;
    @FXML
    private ListView<Reclamation> reclamationsList;

    private ServiceReclamation serviceReclamation = new ServiceReclamation();
    private ObservableList<Reclamation> reclamationsData = FXCollections.observableArrayList();
    private ObservableList<Reclamation> filteredReclamations = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set user profile info
        if (UserSession.getInstance().getCurrentUser() != null) {
            String userName = UserSession.getInstance().getCurrentUser().getName();
            String userRole = UserSession.getInstance().getCurrentUser().getRole();

            profileNameLabel.setText(userName);
            profileRoleLabel.setText(userRole);

            if (userName != null && !userName.trim().isEmpty()) {
                profileAvatarLabel.setText(userName.trim().substring(0, 1).toUpperCase());
            } else {
                profileAvatarLabel.setText("?");
            }
        }

        // Configure ListView with custom cell factory
        reclamationsList.setCellFactory(param -> new ReclamationCell());

        // Load reclamations
        loadReclamations();
    }

    private void loadReclamations() {
        try {
            reclamationsData.clear();
            java.util.List<Reclamation> allReclamations = serviceReclamation.afficher();

            if (UserSession.getInstance().isAdmin()) {
                reclamationsData.addAll(allReclamations);
            } else {
                Fintech.entities.User currentUser = UserSession.getInstance().getCurrentUser();
                if (currentUser != null && currentUser.getEmail() != null) {
                    for (Reclamation r : allReclamations) {
                        if (currentUser.getEmail().equalsIgnoreCase(r.getEmail())) {
                            reclamationsData.add(r);
                        }
                    }
                }
            }

            filteredReclamations.setAll(reclamationsData);
            reclamationsList.setItems(filteredReclamations);
            updateReclamationCount();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les réclamations: " + e.getMessage());
        }
    }

    private void updateReclamationCount() {
        int count = filteredReclamations.size();
        reclamationCountLabel.setText(count + " Total");
    }

    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().toLowerCase().trim();

        if (searchText.isEmpty()) {
            filteredReclamations.setAll(reclamationsData);
        } else {
            filteredReclamations.clear();
            for (Reclamation reclamation : reclamationsData) {
                boolean match = false;
                if (reclamation.getEmail() != null && reclamation.getEmail().toLowerCase().contains(searchText))
                    match = true;
                else if (reclamation.getSubject() != null
                        && reclamation.getSubject().toLowerCase().contains(searchText))
                    match = true;
                else if (reclamation.getDescription() != null
                        && reclamation.getDescription().toLowerCase().contains(searchText))
                    match = true;
                else if (reclamation.getStatut() != null && reclamation.getStatut().toLowerCase().contains(searchText))
                    match = true;
                else if (String.valueOf(reclamation.getId_reclamation()).contains(searchText))
                    match = true;

                if (match) {
                    filteredReclamations.add(reclamation);
                }
            }
        }
        updateReclamationCount();
    }

    @FXML
    private void handleCreateReclamation(ActionEvent event) {
        navigateToReclamationForm(event);
    }

    @FXML
    private void handleChat(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fintech/views/Chat.fxml"));
            Parent root = loader.load();
            Stage chatStage = new Stage();
            chatStage.setTitle("Assistant Intelligent");
            chatStage.setScene(new Scene(root));
            chatStage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger le chat: " + e.getMessage());
        }
    }

    private void navigateToReclamationForm(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fintech/views/ReclamationForm.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger le formulaire: " + e.getMessage());
        }
    }

    private void handleEditReclamation(Reclamation reclamation) {
        boolean isAdmin = UserSession.getInstance().isAdmin();
        boolean isOwner = reclamation != null
                && reclamation.getEmail().equals(UserSession.getInstance().getCurrentUser().getEmail());

        if (!isAdmin && !isOwner) {
            showAlert(Alert.AlertType.WARNING, "Accès refusé",
                    "Vous ne pouvez modifier que vos propres réclamations.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fintech/views/ReclamationForm.fxml"));
            Parent root = loader.load();

            ReclamationFormController controller = loader.getController();
            controller.setReclamation(reclamation);
            controller.setUpdateCallback(this::loadReclamations);

            Stage stage = (Stage) reclamationsList.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger le formulaire: " + e.getMessage());
        }
    }

    private void handleDeleteReclamation(Reclamation reclamation) {
        boolean isAdmin = UserSession.getInstance().isAdmin();
        boolean isOwner = reclamation != null
                && reclamation.getEmail().equals(UserSession.getInstance().getCurrentUser().getEmail());

        if (!isAdmin && !isOwner) {
            showAlert(Alert.AlertType.WARNING, "Accès refusé",
                    "Vous ne pouvez supprimer que vos propres réclamations.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Supprimer la réclamation");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer cette réclamation ?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                serviceReclamation.supprimer(reclamation.getId_reclamation());
                loadReclamations();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Réclamation supprimée avec succès.");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer la réclamation: " + e.getMessage());
            }
        }
    }

    private void handleStatusChange(Reclamation reclamation, String newStatus) {
        if (!UserSession.getInstance().isAdmin()) {
            showAlert(Alert.AlertType.WARNING, "Accès refusé",
                    "Seuls les administrateurs peuvent modifier le statut.");
            loadReclamations(); // Reload to reset the ComboBox
            return;
        }

        try {
            reclamation.setStatut(newStatus);
            serviceReclamation.modifier(reclamation);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Statut mis à jour avec succès.");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de mettre à jour le statut: " + e.getMessage());
            loadReclamations();
        }
    }

    // Navigation methods
    @FXML
    private void handleDashboard(ActionEvent event) {
        if (UserSession.getInstance().isAdmin()) {
            navigateTo(event, "/Fintech/views/UserManagement.fxml");
        } else {
            navigateTo(event, "/Fintech/views/UserDashboard.fxml");
        }
    }

    @FXML
    private void handleTransactions(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Info", "Page Transactions en cours de développement.");
    }

    @FXML
    private void handleBudgets(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Info", "Page Budgets en cours de développement.");
    }

    @FXML
    private void handleUsers(ActionEvent event) {
        navigateTo(event, "/Fintech/views/UserManagement.fxml");
    }

    @FXML
    private void handleSettings(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Info", "Page Paramètres en cours de développement.");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        UserSession.getInstance().clearSession();
        navigateTo(event, "/Fintech/views/login.fxml");
    }

    private void navigateTo(ActionEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Custom ListCell for Reclamations
    private class ReclamationCell extends ListCell<Reclamation> {
        private VBox content;
        private Label emailLabel;
        private Label subjectLabel;
        private Label descriptionLabel;
        private Label statusBadge;
        private ComboBox<String> statusComboBox;
        private Button editButton;
        private Button deleteButton;

        public ReclamationCell() {
            super();

            // Main container
            content = new VBox(10);
            content.setPadding(new Insets(15));
            content.setStyle(
                    "-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");

            // Info section
            VBox infoBox = new VBox(5);

            emailLabel = new Label();
            emailLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #333; -fx-font-weight: bold;");

            subjectLabel = new Label();
            subjectLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #1a1a1a; -fx-font-weight: bold;");

            descriptionLabel = new Label();
            descriptionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");
            descriptionLabel.setWrapText(true);
            descriptionLabel.setMaxWidth(600);

            infoBox.getChildren().addAll(emailLabel, subjectLabel, descriptionLabel);

            // Status and Actions section
            HBox bottomBox = new HBox(15);
            bottomBox.setAlignment(Pos.CENTER_LEFT);

            // Status badge
            statusBadge = new Label();
            statusBadge.setPadding(new Insets(5, 15, 5, 15));
            statusBadge.setStyle("-fx-background-radius: 15; -fx-font-size: 11px; -fx-font-weight: bold;");

            // Status ComboBox
            statusComboBox = new ComboBox<>();
            statusComboBox.getItems().addAll("En attente", "En cours", "Traitée", "Refusée");
            statusComboBox.setPrefWidth(150);
            statusComboBox.setStyle("-fx-font-size: 12px;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            // Action buttons
            HBox actionBox = new HBox(10);
            editButton = new Button("✏️ Modifier");
            editButton.setStyle(
                    "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand; -fx-font-size: 11px; -fx-padding: 5 15 5 15;");

            deleteButton = new Button("🗑️ Supprimer");
            deleteButton.setStyle(
                    "-fx-background-color: #f44336; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand; -fx-font-size: 11px; -fx-padding: 5 15 5 15;");

            actionBox.getChildren().addAll(editButton, deleteButton);

            bottomBox.getChildren().addAll(statusBadge, statusComboBox, spacer, actionBox);

            content.getChildren().addAll(infoBox, bottomBox);
        }

        @Override
        protected void updateItem(Reclamation reclamation, boolean empty) {
            super.updateItem(reclamation, empty);

            if (empty || reclamation == null) {
                setGraphic(null);
            } else {
                // Update labels
                emailLabel.setText("📧 " + reclamation.getEmail());
                subjectLabel.setText(reclamation.getSubject());

                String description = reclamation.getDescription();
                if (description.length() > 150) {
                    description = description.substring(0, 150) + "...";
                }
                descriptionLabel.setText(description);

                // Update status badge
                String status = reclamation.getStatut();
                statusBadge.setText(status);
                updateStatusBadgeStyle(statusBadge, status);

                // Remove previous listener FIRST to avoid triggering it when setting value
                statusComboBox.setOnAction(null);

                // Update ComboBox value
                statusComboBox.setValue(status);

                // Add new listener
                statusComboBox.setOnAction(e -> {
                    String newStatus = statusComboBox.getValue();
                    // Only trigger if real change (though setValue(status) won't trigger this
                    // listener anyway now)
                    if (newStatus != null && !newStatus.equals(status)) {
                        handleStatusChange(reclamation, newStatus);
                    }
                });

                // Check if user is admin or owner
                boolean isAdmin = UserSession.getInstance().isAdmin();
                boolean isOwner = reclamation.getEmail().equals(UserSession.getInstance().getCurrentUser().getEmail());

                // Enable/disable controls based on permissions
                statusComboBox.setDisable(!isAdmin); // Only admins can change status

                boolean canEditOrDelete = isAdmin || isOwner;
                editButton.setVisible(canEditOrDelete);
                deleteButton.setVisible(canEditOrDelete);

                // Set button actions
                editButton.setOnAction(e -> handleEditReclamation(reclamation));
                deleteButton.setOnAction(e -> handleDeleteReclamation(reclamation));

                setGraphic(content);
            }
        }

        private void updateStatusBadgeStyle(Label badge, String status) {
            String baseStyle = "-fx-background-radius: 15; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 5 15 5 15;";
            switch (status) {
                case "En attente":
                    badge.setStyle(baseStyle + " -fx-background-color: #FFF3CD; -fx-text-fill: #856404;");
                    break;
                case "En cours":
                    badge.setStyle(baseStyle + " -fx-background-color: #D1ECF1; -fx-text-fill: #0C5460;");
                    break;
                case "Traitée":
                    badge.setStyle(baseStyle + " -fx-background-color: #D4EDDA; -fx-text-fill: #155724;");
                    break;
                case "Refusée":
                    badge.setStyle(baseStyle + " -fx-background-color: #F8D7DA; -fx-text-fill: #721C24;");
                    break;
                default:
                    badge.setStyle(baseStyle + " -fx-background-color: #E2E3E5; -fx-text-fill: #383D41;");
            }
        }
    }
}
