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
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

public class UserDashboardController {

    @FXML
    private Label sidebarEmailLabel;
    @FXML
    private ImageView userAvatarImage;
    @FXML
    private Label userRoleBadge;
    @FXML
    private Label userNameLabel;
    @FXML
    private Label memberSinceLabel;
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
    private Label lastConnectionLabel;
    @FXML
    private Label timelineEmailLabel;
    @FXML
    private Label timelineRoleLabel;
    @FXML
    private AreaChart<String, Number> activityChart;

    private User currentUser;
    private ServiceUser serviceUser;
    private boolean isPasswordVisible = false;

    public UserDashboardController() {
        serviceUser = new ServiceUser();
    }

    @FXML
    public void initialize() {
        currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null)
            return;

        // 1. Update last_login NOW (whether it's first time or not)
        serviceUser.updateLastLogin(currentUser.getId());

        // 2. Re-fetch fresh user data (so we get created_at and the just-updated
        // last_login)
        User fresh = serviceUser.getUserByEmail(currentUser.getEmail());
        if (fresh != null) {
            currentUser = fresh;
            UserSession.getInstance().setCurrentUser(currentUser);
        }

        displayUserDetails();
        sidebarEmailLabel.setText(currentUser.getEmail());
        setupActivityChart();
    }

    private void displayUserDetails() {
        if (currentUser == null)
            return;

        userNameLabel.setText(currentUser.getName());
        userNameDetailLabel.setText(currentUser.getName());
        userEmailLabel.setText(currentUser.getEmail());
        userPhoneLabel.setText(currentUser.getPhone());
        userRoleLabel.setText(currentUser.getRole() != null ? currentUser.getRole() : "Utilisateur");

        loadAvatar(userAvatarImage, currentUser.getFaceImage());
        userPasswordLabel.setText("••••••••");

        // Role badge
        String roleText = (currentUser.getRole() != null && !currentUser.getRole().isEmpty())
                ? currentUser.getRole()
                : "Utilisateur";
        userRoleBadge.setText("🛡️ " + roleText);

        // ── "Membre depuis" — use created_at ──
        if (memberSinceLabel != null) {
            Timestamp createdAt = currentUser.getCreatedAt();
            if (createdAt != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", java.util.Locale.FRENCH);
                memberSinceLabel.setText("Membre depuis le " + sdf.format(createdAt));
            } else {
                memberSinceLabel.setText("Membre depuis \u2014");
            }
        }

        // ── "Dernière Connexion" ── (now always up to date since we just called
        // updateLastLogin)
        if (lastConnectionLabel != null) {
            Timestamp lastLogin = currentUser.getLastLogin();
            if (lastLogin != null) {
                lastConnectionLabel.setText("\uD83D\uDCCD Dernière Connexion: " + formatLastLogin(lastLogin));
            } else {
                // Fallback: show current time (the updateLastLogin was just called but DB may
                // be slow)
                SimpleDateFormat tf = new SimpleDateFormat("HH:mm");
                lastConnectionLabel.setText("\uD83D\uDCCD Dernière Connexion: Aujourd'hui, " + tf.format(new Date()));
            }
        }

        // ── Timeline items — real user data ──
        if (timelineEmailLabel != null) {
            timelineEmailLabel.setText("Email: " + currentUser.getEmail());
        }
        if (timelineRoleLabel != null) {
            timelineRoleLabel.setText("Rôle: " + roleText);
        }
    }

    private String formatLastLogin(Timestamp ts) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", java.util.Locale.FRENCH);

        Date loginDate = new Date(ts.getTime());
        Calendar loginCal = Calendar.getInstance();
        loginCal.setTime(loginDate);
        Calendar todayCal = Calendar.getInstance();
        Calendar yesterdayCal = Calendar.getInstance();
        yesterdayCal.add(Calendar.DAY_OF_YEAR, -1);

        boolean isToday = loginCal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR)
                && loginCal.get(Calendar.DAY_OF_YEAR) == todayCal.get(Calendar.DAY_OF_YEAR);
        boolean isYesterday = loginCal.get(Calendar.YEAR) == yesterdayCal.get(Calendar.YEAR)
                && loginCal.get(Calendar.DAY_OF_YEAR) == yesterdayCal.get(Calendar.DAY_OF_YEAR);

        if (isToday)
            return "Aujourd'hui, " + timeFormat.format(loginDate);
        if (isYesterday)
            return "Hier, " + timeFormat.format(loginDate);
        return dateFormat.format(loginDate) + ", " + timeFormat.format(loginDate);
    }

    @FXML
    private void togglePasswordVisibility() {
        if (currentUser == null)
            return;
        isPasswordVisible = !isPasswordVisible;
        userPasswordLabel.setText(isPasswordVisible ? currentUser.getPassword() : "••••••••");
    }

    private void setupActivityChart() {
        if (activityChart == null)
            return;
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Lun", 3));
        series.getData().add(new XYChart.Data<>("Mar", 2));
        series.getData().add(new XYChart.Data<>("Mer", 5));
        series.getData().add(new XYChart.Data<>("Jeu", 4));
        series.getData().add(new XYChart.Data<>("Ven", 6));
        series.getData().add(new XYChart.Data<>("Sam", 8));
        series.getData().add(new XYChart.Data<>("Dim", 2));
        activityChart.getData().add(series);
        activityChart.setCreateSymbols(true);
    }

    private boolean loadAvatar(ImageView imageView, String imagePath) {
        if (imageView == null)
            return false;
        try {
            if (imagePath != null && !imagePath.trim().isEmpty()) {
                File file = new File(imagePath);
                if (file.exists()) {
                    imageView.setImage(new Image(file.toURI().toString()));
                    return true;
                }
            }
            imageView.setImage(null);
            return false;
        } catch (Exception e) {
            imageView.setImage(null);
            return false;
        }
    }

    @FXML
    private void handleEdit(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fintech/views/UserForm.fxml"));
            Parent root = loader.load();
            UserFormController controller = loader.getController();
            controller.setUser(currentUser);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger le formulaire: " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Supprimer votre compte");
        confirmAlert.setContentText(
                "Êtes-vous sûr de vouloir supprimer définitivement votre compte ? Cette action est irréversible.");
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                serviceUser.supprimer(currentUser.getId());
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Votre compte a été supprimé.");
                handleLogout(event);
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer le compte: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleUsers(ActionEvent event) {
    }

    @FXML
    private void handleProfile(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fintech/views/UserProfile.fxml"));
            Parent root = loader.load();
            UserProfileController controller = loader.getController();
            controller.setUser(currentUser);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger le profil: " + e.getMessage());
        }
    }

    @FXML
    private void handleDashboard(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Info", "Fonctionnalité Abonnements à venir");
    }

    @FXML
    private void handleTransactions(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Info", "Fonctionnalité Éducative à venir");
    }

    @FXML
    private void handleBudgets(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Info", "Fonctionnalité Dépenses à venir");
    }

    @FXML
    private void handleReclamations(ActionEvent event) {
        navigateTo(event, "/Fintech/views/ReclamationManagement.fxml");
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
            stage.setScene(new Scene(root));
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
