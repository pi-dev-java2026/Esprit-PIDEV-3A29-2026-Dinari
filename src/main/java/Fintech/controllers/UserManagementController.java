package Fintech.controllers;

import Fintech.entities.User;
import Fintech.servicies.ServiceUser;
import Fintech.utils.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class UserManagementController {

    @FXML
    private ListView<User> usersList;

    @FXML
    private TextField searchField;
    @FXML
    private Label userCountLabel;

    @FXML
    private Label profileNameLabel;
    @FXML
    private Label profileRoleLabel;
    @FXML
    private Label profileAvatarLabel;

    private ServiceUser serviceUser;
    private ObservableList<User> usersObservableList;
    private ObservableList<User> filteredList;

    public UserManagementController() {
        serviceUser = new ServiceUser();
    }

    @FXML
    public void initialize() {
        // Check if user is admin - only admins can view the user list
        if (!UserSession.getInstance().isAdmin()) {
            javafx.application.Platform.runLater(this::redirectToUserForm);
            return;
        }

        setupListView();
        loadUsers();
        updateUserProfile();
    }

    private void updateUserProfile() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userName = currentUser.getName();
            profileNameLabel.setText(userName);
            profileRoleLabel.setText(currentUser.getRole() != null ? currentUser.getRole().toUpperCase() : "");

            if (userName != null && !userName.trim().isEmpty()) {
                profileAvatarLabel.setText(userName.trim().substring(0, 1).toUpperCase());
            } else {
                profileAvatarLabel.setText("?");
            }
        } else {
            profileNameLabel.setText("Utilisateur");
            profileRoleLabel.setText("RÔLE");
            profileAvatarLabel.setText("U");
        }
    }

    /** Returns up to 2 uppercase initials from a name */
    private String getInitials(String name) {
        if (name == null || name.isEmpty())
            return "U";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1)
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return (parts[0].charAt(0) + "" + parts[1].charAt(0)).toUpperCase();
    }

    /** Returns an avatar background color based on role */
    private String getAvatarColor(String role) {
        if (role == null)
            return "#6b7280";
        switch (role.toLowerCase()) {
            case "admin":
                return "#1d4ed8";
            case "manager":
                return "#065f46";
            default:
                return "#3d82dc";
        }
    }

    private void setupListView() {
        usersList.setCellFactory(listView -> new ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    // ── Card Container ──
                    HBox card = new HBox(16);
                    card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    card.setPadding(new javafx.geometry.Insets(14, 20, 14, 20));
                    card.setCursor(javafx.scene.Cursor.HAND);
                    card.setStyle(
                            "-fx-background-color: white;" +
                                    "-fx-background-radius: 12px;" +
                                    "-fx-border-color: #e5e7eb;" +
                                    "-fx-border-radius: 12px;" +
                                    "-fx-border-width: 1px;" +
                                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 6, 0, 0, 2);");

                    // Hover effect (lift + blue border)
                    card.setOnMouseEntered(e -> card.setStyle(
                            "-fx-background-color: #f8faff;" +
                                    "-fx-background-radius: 12px;" +
                                    "-fx-border-color: #bfdbfe;" +
                                    "-fx-border-radius: 12px;" +
                                    "-fx-border-width: 1.5px;" +
                                    "-fx-effect: dropshadow(three-pass-box, rgba(15,98,254,0.10), 14, 0, 0, 5);"));
                    card.setOnMouseExited(e -> card.setStyle(
                            "-fx-background-color: white;" +
                                    "-fx-background-radius: 12px;" +
                                    "-fx-border-color: #e5e7eb;" +
                                    "-fx-border-radius: 12px;" +
                                    "-fx-border-width: 1px;" +
                                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 6, 0, 0, 2);"));

                    // Click → navigate to user profile
                    card.setOnMouseClicked(e -> handleViewProfile(user));

                    // ── Initials Avatar ──
                    String initials = getInitials(user.getName());
                    String avatarColor = getAvatarColor(user.getRole());
                    Label avatar = new Label(initials);
                    avatar.setStyle(
                            "-fx-background-color: " + avatarColor + ";" +
                                    "-fx-background-radius: 50%;" +
                                    "-fx-text-fill: white;" +
                                    "-fx-font-size: 14px;" +
                                    "-fx-font-weight: bold;" +
                                    "-fx-padding: 0;" +
                                    "-fx-min-width: 44px;" +
                                    "-fx-min-height: 44px;" +
                                    "-fx-max-width: 44px;" +
                                    "-fx-max-height: 44px;" +
                                    "-fx-alignment: CENTER;");

                    // ── Name + email + role badge ──
                    Label nameLabel = new Label(user.getName() != null ? user.getName() : "");
                    nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #111827;");

                    String roleBadgeStyle;
                    String role = user.getRole() != null ? user.getRole() : "user";
                    switch (role.toLowerCase()) {
                        case "admin":
                            roleBadgeStyle = "-fx-background-color: #dbeafe; -fx-text-fill: #1d4ed8;";
                            break;
                        case "manager":
                            roleBadgeStyle = "-fx-background-color: #d1fae5; -fx-text-fill: #065f46;";
                            break;
                        default:
                            roleBadgeStyle = "-fx-background-color: #f3f4f6; -fx-text-fill: #374151;";
                    }

                    Label emailPlain = new Label(user.getEmail() != null ? user.getEmail() : "");
                    emailPlain.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");

                    Label rolePill = new Label(capitalize(role));
                    rolePill.setStyle(
                            "-fx-font-size: 10.5px; -fx-font-weight: bold; -fx-padding: 2 8;" +
                                    "-fx-background-radius: 20;" + roleBadgeStyle);

                    HBox emailRow = new HBox(8, emailPlain, rolePill);
                    emailRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                    javafx.scene.layout.VBox infoBox = new javafx.scene.layout.VBox(3, nameLabel, emailRow);

                    // ── Spacer ──
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

                    // ── Registration Date ──
                    javafx.scene.layout.VBox dateBox = new javafx.scene.layout.VBox(2);
                    Label dateTitle = new Label("Inscrit le");
                    dateTitle.setStyle("-fx-font-size: 10px; -fx-text-fill: #9ca3af;");
                    Label dateValue = new Label("—");
                    dateValue.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #374151;");
                    dateBox.getChildren().addAll(dateTitle, dateValue);
                    dateBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

                    // ── Arrow indicator → ──
                    Label arrow = new Label("→");
                    arrow.setStyle("-fx-font-size: 16px; -fx-text-fill: #9ca3af; -fx-padding: 0 0 0 8;");

                    card.getChildren().addAll(avatar, infoBox, spacer, dateBox, arrow);
                    setGraphic(card);
                    setText(null);
                    setStyle("-fx-background-color: transparent; -fx-padding: 3 0;");
                }
            }
        });
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty())
            return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
    }

    private void loadUsers() {
        try {
            List<User> users = serviceUser.afficher();
            usersObservableList = FXCollections.observableArrayList(users);
            filteredList = FXCollections.observableArrayList(users);
            usersList.setItems(filteredList);
            updateUserCount();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les utilisateurs: " + e.getMessage());
        }
    }

    private void updateUserCount() {
        userCountLabel.setText(filteredList.size() + " Total");
    }

    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().toLowerCase();

        if (searchText.isEmpty()) {
            filteredList.setAll(usersObservableList);
        } else {
            filteredList.clear();
            for (User user : usersObservableList) {
                boolean match = false;
                if (user.getName() != null && user.getName().toLowerCase().contains(searchText))
                    match = true;
                else if (user.getEmail() != null && user.getEmail().toLowerCase().contains(searchText))
                    match = true;
                else if (user.getPhone() != null && user.getPhone().contains(searchText))
                    match = true;
                else if (user.getRole() != null && user.getRole().toLowerCase().contains(searchText))
                    match = true;

                if (match) {
                    filteredList.add(user);
                }
            }
        }
        updateUserCount();
    }

    @FXML
    private void handleAddUser(ActionEvent event) {
        navigateToUserForm(event, null);
    }

    @FXML
    private void handleAddReclamation(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fintech/views/ReclamationForm.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de charger le formulaire de réclamation: " + e.getMessage());
        }
    }

    @FXML
    private void handleReclamations(ActionEvent event) {
        navigateTo(event, "/Fintech/views/ReclamationManagement.fxml");
    }

    private void redirectToUserForm() {
        try {
            // Get current logged-in user
            User currentUser = UserSession.getInstance().getCurrentUser();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fintech/views/UserProfile.fxml"));
            Parent root = loader.load();

            // Pass the current user to the profile controller
            UserProfileController controller = loader.getController();
            controller.setUser(currentUser);

            Stage stage = null;
            if (usersList != null && usersList.getScene() != null) {
                stage = (Stage) usersList.getScene().getWindow();
            } else if (profileNameLabel != null && profileNameLabel.getScene() != null) {
                stage = (Stage) profileNameLabel.getScene().getWindow();
            }

            if (stage != null) {
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
            } else {
                // S'il n'y a pas de fenêtre active trouvée, on affiche une erreur au lieu
                // d'ouvrir une nouvelle fenêtre
                System.err.println("Erreur: Impossible de trouver la fenêtre principale pour la redirection.");
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger le profil: " + e.getMessage());
        }
    }

    private void handleViewProfile(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fintech/views/UserProfile.fxml"));
            Parent root = loader.load();

            // Pass the user to the profile controller
            UserProfileController controller = loader.getController();
            controller.setUser(user);

            Stage stage = (Stage) usersList.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger le profil: " + e.getMessage());
        }
    }

    private void navigateToUserForm(ActionEvent event, User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fintech/views/UserForm.fxml"));
            Parent root = loader.load();

            // If editing, pass the user to the form controller
            if (user != null) {
                UserFormController controller = loader.getController();
                controller.setUser(user);
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger le formulaire: " + e.getMessage());
        }
    }

    @FXML
    private void handleDashboard(ActionEvent event) {
        navigateTo(event, "/Fintech/views/UserManagement.fxml");
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
    private void handleSettings(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Info", "Fonctionnalité Paramètres à venir");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        // Clear the user session
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
