package Fintech.controllers;

import Fintech.entities.User;
import Fintech.servicies.ServiceUser;
import Fintech.utils.UserSession;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class UserFormController implements Initializable {

    @FXML
    private TextField nameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField passwordField;
    @FXML
    private ComboBox<String> roleComboBox;
    @FXML
    private ImageView photoPreview;
    @FXML
    private Label photoPlaceholder;
    @FXML
    private Label photoStatusLabel;
    @FXML
    private Button capturePhotoButton;

    private ServiceUser serviceUser = new ServiceUser();
    private User user;
    private Runnable updateCallback;
    /** Path of the newly captured face image (null = no change) */
    private String newFaceImagePath = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        roleComboBox.setItems(FXCollections.observableArrayList("Admin", "User", "Manager")); // Example roles

        if (!UserSession.getInstance().isAdmin()) {
            roleComboBox.setValue("User");
            roleComboBox.setDisable(true);
        }
    }

    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            nameField.setText(user.getName());
            emailField.setText(user.getEmail());
            phoneField.setText(user.getPhone());
            passwordField.setText(user.getPassword());
            roleComboBox.setValue(user.getRole());

            boolean isAdmin = UserSession.getInstance().isAdmin();
            boolean isOwnProfile = UserSession.getInstance().getCurrentUser() != null
                    && user.getId() == UserSession.getInstance().getCurrentUser().getId();

            if (!isAdmin) {
                // Standard users cannot change their role
                roleComboBox.setDisable(true);
            } else if (!isOwnProfile) {
                // Admin editing ANOTHER user: only role is editable
                nameField.setEditable(false);
                nameField.setStyle("-fx-opacity: 0.7;");
                emailField.setEditable(false);
                emailField.setStyle("-fx-opacity: 0.7;");
                phoneField.setEditable(false);
                phoneField.setStyle("-fx-opacity: 0.7;");
                passwordField.setEditable(false);
                passwordField.setStyle("-fx-opacity: 0.7;");
                capturePhotoButton.setDisable(true);
                capturePhotoButton.setStyle("-fx-opacity: 0.5;");
            }

            // Show existing photo if available
            String existingPhoto = user.getFaceImage();
            if (existingPhoto != null && !existingPhoto.isEmpty()) {
                File imgFile = new File(existingPhoto);
                if (imgFile.exists()) {
                    photoPreview.setImage(new Image(imgFile.toURI().toString()));
                    photoPlaceholder.setVisible(false);
                    photoStatusLabel.setText("Photo existante");
                    photoStatusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #2e7d32;");
                }
            }
        }
    }

    public void setUpdateCallback(Runnable updateCallback) {
        this.updateCallback = updateCallback;
    }

    @FXML
    private void handleSave(ActionEvent event) {
        boolean isAdmin = UserSession.getInstance().isAdmin();
        boolean isOwnProfile = user != null && UserSession.getInstance().getCurrentUser() != null
                && user.getId() == UserSession.getInstance().getCurrentUser().getId();

        // ── ADMIN modifying ANOTHER user: only update the role ──────────────
        if (isAdmin && user != null && !isOwnProfile) {
            String newRole = roleComboBox.getValue();
            if (newRole == null) {
                showAlert("Erreur", "Veuillez sélectionner un rôle.", Alert.AlertType.ERROR);
                return;
            }
            try {
                user.setRole(newRole);
                serviceUser.modifier(user);
                if (updateCallback != null)
                    updateCallback.run();
                closeWindow();
            } catch (SQLException e) {
                showAlert("Erreur", "Erreur lors de la mise à jour du rôle: " + e.getMessage(), Alert.AlertType.ERROR);
            }
            return;
        }

        // ── Normal path: own profile (admin or user) or new user creation ───
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String password = passwordField.getText();
        String role = roleComboBox.getValue();

        // Force role to "User" if a non-admin is creating a new account
        if (!isAdmin) {
            role = (user == null) ? "User" : user.getRole();
        }

        // Validation des champs vides
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || role == null) {
            showAlert("Erreur", "Veuillez remplir tous les champs.", Alert.AlertType.ERROR);
            return;
        }

        if (!isValidName(name)) {
            showAlert("Erreur de validation", "Le nom doit contenir uniquement des lettres et des espaces.",
                    Alert.AlertType.ERROR);
            return;
        }
        if (!isValidEmail(email)) {
            showAlert("Erreur de validation", "L'email doit contenir le caractère '@'.", Alert.AlertType.ERROR);
            return;
        }
        if (!isValidPhone(phone)) {
            showAlert("Erreur de validation", "Le téléphone doit contenir exactement 8 chiffres.",
                    Alert.AlertType.ERROR);
            return;
        }
        if (!isValidPassword(password)) {
            showAlert("Erreur de validation", "Le mot de passe doit contenir exactement 8 caractères.",
                    Alert.AlertType.ERROR);
            return;
        }

        if (user != null && !isAdmin && !isOwnProfile) {
            showAlert("Accès refusé", "Vous ne pouvez modifier que votre propre profil.", Alert.AlertType.ERROR);
            return;
        }

        try {
            if (user == null) {
                // Lancer la capture faciale via Python (only for new users, which implies Admin
                // since non-admins can't create users)
                String faceImagePath = null;
                StringBuilder errorLog = new StringBuilder();
                try {
                    String pythonCmd = System.getProperty("os.name").toLowerCase().contains("win") ? "py" : "python3";
                    ProcessBuilder pb = new ProcessBuilder(pythonCmd, "face_capture.py");
                    pb.directory(new java.io.File(System.getProperty("user.dir")));
                    pb.redirectErrorStream(true); // Rediriger l'erreur vers la sortie standard pour lire tous les logs
                    Process p = pb.start();

                    java.io.BufferedReader reader = new java.io.BufferedReader(
                            new java.io.InputStreamReader(p.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("Python: " + line); // Pour le debug
                        errorLog.append(line).append("\n");
                        if (line.startsWith("SUCCESS:")) {
                            faceImagePath = line.substring(8).trim();
                        } else if (line.startsWith("ERROR:")) {
                            // On bloque l'inscription
                            showAlert("Erreur de reconnaissance faciale", line.substring(6).trim(),
                                    Alert.AlertType.ERROR);
                            return;
                        }
                    }

                    int exitCode = p.waitFor();
                    if (faceImagePath == null) {
                        String errMsg = errorLog.toString().trim();
                        if (errMsg.isEmpty())
                            errMsg = "Aucune réponse du script python.";
                        showAlert("Erreur de Capture (Code " + exitCode + ")", "Détails: \n" + errMsg,
                                Alert.AlertType.ERROR);
                        return;
                    }

                } catch (Exception e) {
                    showAlert("Erreur", "Impossible de lancer le module de capture faciale:\n" + e.getMessage(),
                            Alert.AlertType.ERROR);
                    return;
                }

                User newUser = new User(name, email, phone, password, role, faceImagePath);
                serviceUser.ajouter(newUser);
            } else {
                user.setName(name);
                user.setEmail(email);
                user.setPhone(phone);
                user.setPassword(password);

                // If not admin, do not allow role changes! Keep the existing role.
                if (isAdmin) {
                    user.setRole(role);
                } else {
                    user.setRole(user.getRole()); // Explicitly enforce existing role
                }

                // Update face image if a new one was captured
                if (newFaceImagePath != null) {
                    user.setFaceImage(newFaceImagePath);
                }

                serviceUser.modifier(user);

                // If editing own profile, also update the session
                if (isOwnProfile) {
                    UserSession.getInstance().setCurrentUser(user);
                }
            }

            if (updateCallback != null) {
                updateCallback.run();
            }

            closeWindow();
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de l'enregistrement de l'utilisateur: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    /**
     * Valide que le nom contient uniquement des lettres et des espaces
     */
    private boolean isValidName(String name) {
        return name.matches("[a-zA-ZÀ-ÿ\\s]+");
    }

    /**
     * Valide que l'email contient le caractère @
     */
    private boolean isValidEmail(String email) {
        return email.contains("@");
    }

    /**
     * Valide que le téléphone contient exactement 8 chiffres
     */
    private boolean isValidPhone(String phone) {
        return phone.matches("\\d{8}");
    }

    /**
     * Valide que le mot de passe contient exactement 8 caractères
     */
    private boolean isValidPassword(String password) {
        return password.length() == 8;
    }

    @FXML
    private void handleCapturePhoto(ActionEvent event) {
        capturePhotoButton.setDisable(true);
        capturePhotoButton.setText("⏳  Capture en cours...");
        photoStatusLabel.setText("Ouverture de la caméra...");
        photoStatusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #1565c0;");

        // Run face capture in a background thread so UI stays responsive
        new Thread(() -> {
            String capturedPath = null;
            String errorMsg = null;
            try {
                String pythonCmd = System.getProperty("os.name").toLowerCase().contains("win") ? "py" : "python3";
                ProcessBuilder pb = new ProcessBuilder(pythonCmd, "face_capture.py");
                pb.directory(new java.io.File(System.getProperty("user.dir")));
                pb.redirectErrorStream(true);
                Process p = pb.start();

                java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(p.getInputStream()));
                String line;
                StringBuilder log = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    System.out.println("Python: " + line);
                    log.append(line).append("\n");
                    if (line.startsWith("SUCCESS:")) {
                        capturedPath = line.substring(8).trim();
                    } else if (line.startsWith("ERROR:")) {
                        errorMsg = line.substring(6).trim();
                    }
                }
                p.waitFor();
                if (capturedPath == null && errorMsg == null) {
                    String raw = log.toString().trim();
                    errorMsg = raw.isEmpty() ? "Aucune réponse du script de capture." : raw;
                }
            } catch (Exception ex) {
                errorMsg = "Impossible de lancer la caméra:\n" + ex.getMessage();
            }

            final String finalPath = capturedPath;
            final String finalError = errorMsg;
            javafx.application.Platform.runLater(() -> {
                capturePhotoButton.setDisable(false);
                capturePhotoButton.setText("📸  Prendre une photo");
                if (finalPath != null) {
                    newFaceImagePath = finalPath;
                    java.io.File imgFile = new java.io.File(finalPath);
                    if (imgFile.exists()) {
                        photoPreview.setImage(new Image(imgFile.toURI().toString()));
                        photoPlaceholder.setVisible(false);
                    }
                    photoStatusLabel.setText("✅ Photo capturée !");
                    photoStatusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #2e7d32;");
                } else {
                    photoStatusLabel.setText("❌ Échec de la capture");
                    photoStatusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #c62828;");
                    showAlert("Erreur de capture", finalError, Alert.AlertType.ERROR);
                }
            });
        }, "face-capture-thread").start();
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        closeWindow();
    }

    @FXML
    private void handleClear(ActionEvent event) {
        nameField.clear();
        emailField.clear();
        phoneField.clear();
        passwordField.clear();
        roleComboBox.setValue(null);
    }

    private void closeWindow() {
        try {
            boolean isLoggedIn = UserSession.getInstance().isLoggedIn();

            if (!isLoggedIn) {
                // If not logged in, it means we are in the registration process.
                // Return to the login screen.
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                        getClass().getResource("/Fintech/views/login.fxml"));
                javafx.scene.Parent root = loader.load();
                javafx.stage.Stage stage = (javafx.stage.Stage) nameField.getScene().getWindow();
                javafx.scene.Scene scene = new javafx.scene.Scene(root);
                stage.setScene(scene);
                stage.show();
                return;
            }

            boolean isAdmin = UserSession.getInstance().isAdmin();
            // Admins go back to UserManagement, regular users go to the new UserDashboard
            String targetFxml = isAdmin ? "/Fintech/views/UserManagement.fxml" : "/Fintech/views/UserDashboard.fxml";

            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource(targetFxml));
            javafx.scene.Parent root = loader.load();

            javafx.stage.Stage stage = (javafx.stage.Stage) nameField.getScene().getWindow();
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
