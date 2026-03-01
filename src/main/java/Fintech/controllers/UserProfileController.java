package Fintech.controllers;

import Fintech.entities.User;
import Fintech.servicies.ServiceUser;
import Fintech.utils.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.File;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class UserProfileController {

    @FXML
    private Label profileNameLabel;
    @FXML
    private Label profileRoleLabel;
    @FXML
    private ImageView topRightAvatar;
    @FXML
    private Label profileAvatarLabel;

    @FXML
    private ImageView userAvatarImage;
    @FXML
    private Label userNameLabel;
    @FXML
    private Label userNameDetailLabel;
    @FXML
    private Label userEmailLabel;
    @FXML
    private Label userPhoneLabel;
    @FXML
    private Label userPasswordLabel;
    @FXML
    private Label userRoleLabel;
    @FXML
    private Label userRoleBadge;

    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button backButton;

    private User currentUser;
    private ServiceUser serviceUser;

    public UserProfileController() {
        serviceUser = new ServiceUser();
    }

    @FXML
    public void initialize() {
        updateUserProfile();
        // Admin check will be done when setUser is called
    }

    private void updateUserProfile() {
        User loggedInUser = UserSession.getInstance().getCurrentUser();
        if (loggedInUser != null) {
            String userName = loggedInUser.getName();
            profileNameLabel.setText(userName);
            profileRoleLabel.setText(loggedInUser.getRole());

            // Try to load custom avatar image if available
            boolean hasImage = loadAvatar(topRightAvatar, loggedInUser.getFaceImage());

            if (!hasImage && profileAvatarLabel != null) {
                topRightAvatar.setVisible(false);
                topRightAvatar.setManaged(false);
                profileAvatarLabel.setVisible(true);
                profileAvatarLabel.setManaged(true);
                if (userName != null && !userName.trim().isEmpty()) {
                    profileAvatarLabel.setText(userName.trim().substring(0, 1).toUpperCase());
                } else {
                    profileAvatarLabel.setText("?");
                }
            } else if (hasImage && profileAvatarLabel != null) {
                topRightAvatar.setVisible(true);
                topRightAvatar.setManaged(true);
                profileAvatarLabel.setVisible(false);
                profileAvatarLabel.setManaged(false);
            }
        } else {
            profileNameLabel.setText("Utilisateur");
            profileRoleLabel.setText("Non connecté");
            loadAvatar(topRightAvatar, null);
            if (profileAvatarLabel != null) {
                profileAvatarLabel.setText("U");
                profileAvatarLabel.setVisible(true);
                profileAvatarLabel.setManaged(true);
                topRightAvatar.setVisible(false);
                topRightAvatar.setManaged(false);
            }
        }
    }

    public void setUser(User user) {
        this.currentUser = user;
        displayUserDetails();
        checkAdminPermissions();
    }

    private void displayUserDetails() {
        if (currentUser != null) {
            userNameLabel.setText(currentUser.getName());
            userNameDetailLabel.setText(currentUser.getName());
            userEmailLabel.setText(currentUser.getEmail());
            userPhoneLabel.setText(currentUser.getPhone());
            userRoleLabel.setText(currentUser.getRole());

            loadAvatar(userAvatarImage, currentUser.getFaceImage());

            // Display password based on admin status
            boolean isAdmin = UserSession.getInstance().isAdmin();
            if (isAdmin) {
                // Admin can see the real password
                userPasswordLabel.setText("🔒 " + currentUser.getPassword());
                userPasswordLabel.setStyle("-fx-text-fill: #1f2937; -fx-font-size: 14px;");
            } else {
                // Other users see masked password
                userPasswordLabel.setText("🔒 " + "*".repeat(currentUser.getPassword().length()));
                userPasswordLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 14px;");
            }

            // Set role badge
            userRoleBadge.setText(currentUser.getRole());
            String badgeStyle = "-fx-background-radius: 15px; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 6 18;";

            if (currentUser.getRole().equalsIgnoreCase("Admin")) {
                userRoleBadge.setStyle(badgeStyle + "-fx-background-color: #e3f2fd; -fx-text-fill: #1976d2;");
            } else if (currentUser.getRole().equalsIgnoreCase("Manager")) {
                userRoleBadge.setStyle(badgeStyle + "-fx-background-color: #fff3e0; -fx-text-fill: #f57c00;");
            } else {
                userRoleBadge.setStyle(badgeStyle + "-fx-background-color: #f1f8e9; -fx-text-fill: #689f38;");
            }
        }
    }

    private boolean loadAvatar(ImageView imageView, String imagePath) {
        if (imageView == null)
            return false;

        try {
            if (imagePath != null && !imagePath.trim().isEmpty()) {
                File file = new File(imagePath);
                if (file.exists()) {
                    Image image = new Image(file.toURI().toString());
                    imageView.setImage(image);
                    return true;
                }
            }

            imageView.setImage(null);
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            imageView.setImage(null);
            return false;
        }
    }

    private void checkAdminPermissions() {
        boolean isAdmin = UserSession.getInstance().isAdmin();
        boolean isOwnProfile = false;

        if (currentUser != null && UserSession.getInstance().getCurrentUser() != null) {
            isOwnProfile = currentUser.getId() == UserSession.getInstance().getCurrentUser().getId();
        }

        // Allow edit if admin OR if it's their own profile
        boolean canEdit = isAdmin || isOwnProfile;

        editButton.setDisable(!canEdit);
        deleteButton.setDisable(!isAdmin); // Only admins can delete accounts

        if (!canEdit) {
            editButton.setStyle(editButton.getStyle() + "-fx-opacity: 0.5;");
        }
        if (!isAdmin) {
            deleteButton.setStyle(deleteButton.getStyle() + "-fx-opacity: 0.5;");
            if (backButton != null) {
                backButton.setVisible(false);
                backButton.setManaged(false); // Remove it from the layout
            }
        } else if (backButton != null) {
            backButton.setVisible(true);
            backButton.setManaged(true);
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        if (!UserSession.getInstance().isAdmin()) {
            // Un utilisateur normal ne devrait pas retourner à la liste des utilisateurs ni
            // au dashboard (supprimé)
            return;
        }
        navigateTo(event, "/Fintech/views/UserManagement.fxml");
    }

    @FXML
    private void handleEdit(ActionEvent event) {
        boolean isAdmin = UserSession.getInstance().isAdmin();
        boolean isOwnProfile = currentUser != null && UserSession.getInstance().getCurrentUser() != null
                && currentUser.getId() == UserSession.getInstance().getCurrentUser().getId();

        if (!isAdmin && !isOwnProfile) {
            showAlert(Alert.AlertType.ERROR, "Accès refusé",
                    "Vous ne pouvez modifier que votre propre profil.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fintech/views/UserForm.fxml"));
            Parent root = loader.load();

            UserFormController controller = loader.getController();
            controller.setUser(currentUser);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger le formulaire: " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        if (!UserSession.getInstance().isAdmin()) {
            showAlert(Alert.AlertType.ERROR, "Accès refusé",
                    "Seuls les administrateurs peuvent supprimer les utilisateurs.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Supprimer l'utilisateur");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer " + currentUser.getName() + " ?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                serviceUser.supprimer(currentUser.getId());
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Utilisateur supprimé avec succès.");
                handleBack(event);
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer l'utilisateur: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleUsers(ActionEvent event) {
        if (!UserSession.getInstance().isAdmin()) {
            // Already on their profile, if they click users nothing else should happen
            return;
        }
        navigateTo(event, "/Fintech/views/UserManagement.fxml");
    }

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
        showAlert(Alert.AlertType.INFORMATION, "Info", "Fonctionnalité Transactions à venir");
    }

    @FXML
    private void handleBudgets(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Info", "Fonctionnalité Budgets à venir");
    }

    @FXML
    private void handleReclamations(ActionEvent event) {
        navigateTo(event, "/Fintech/views/ReclamationManagement.fxml");
    }

    @FXML
    private void handleSettings(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Info", "Fonctionnalité Paramètres à venir");
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
}
