package Fintech.controllers;

import Fintech.entities.User;
import Fintech.servicies.ServiceUser;
import Fintech.utils.GoogleAuthService;
import Fintech.utils.UserSession;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Map;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    private ServiceUser serviceUser;

    private int failedAttempts = 0;
    private String lastFailedEmail = "";

    public LoginController() {
        serviceUser = new ServiceUser();
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (isValid(email, password)) {
            User user = serviceUser.getUserByEmailAndPassword(email, password);
            if (user != null) {
                failedAttempts = 0;
                lastFailedEmail = "";
                UserSession.getInstance().setCurrentUser(user);
                // Record the login timestamp in the database
                serviceUser.updateLastLogin(user.getId());
                navigateToDashboard(event);
            } else {
                if (!email.equals(lastFailedEmail)) {
                    failedAttempts = 1;
                    lastFailedEmail = email;
                } else {
                    failedAttempts++;
                }

                if (failedAttempts >= 3) {
                    User userByEmail = serviceUser.getUserByEmail(email);
                    if (userByEmail != null && userByEmail.getFaceImage() != null
                            && !userByEmail.getFaceImage().trim().isEmpty()) {

                        // Désactiver le bouton et informer l'utilisateur sans ouvrir de dialog modal
                        loginButton.setDisable(true);
                        loginButton.setText("📷 Caméra en cours...");

                        final User verifyUser = userByEmail;
                        final ActionEvent loginEvent = event;

                        // Lancer la vérification dans un thread séparé pour ne pas bloquer l'UI
                        Task<Boolean> faceTask = new Task<>() {
                            @Override
                            protected Boolean call() {
                                return verifyFace(verifyUser.getFaceImage());
                            }
                        };

                        faceTask.setOnSucceeded(e -> {
                            loginButton.setDisable(false);
                            loginButton.setText("Se connecter");
                            boolean matched = faceTask.getValue();
                            if (matched) {
                                failedAttempts = 0;
                                lastFailedEmail = "";
                                UserSession.getInstance().setCurrentUser(verifyUser);
                                navigateToDashboard(loginEvent);
                            } else {
                                failedAttempts = 0;
                            }
                        });

                        faceTask.setOnFailed(e -> {
                            loginButton.setDisable(false);
                            loginButton.setText("Se connecter");
                            failedAttempts = 0;
                            showAlert(Alert.AlertType.ERROR, "Erreur",
                                    "La vérification faciale a rencontré une erreur inattendue.");
                        });

                        Thread t = new Thread(faceTask);
                        t.setDaemon(true);
                        t.start();

                    } else {
                        showAlert(Alert.AlertType.ERROR, "Compte bloqué",
                                "3 tentatives échouées et aucune image faciale configurée.");
                        failedAttempts = 0;
                    }
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur de connexion",
                            "Email ou mot de passe incorrect. Tentatives restantes : " + (3 - failedAttempts));
                }
            }
        }
    }

    private boolean verifyFace(String referenceImagePath) {
        StringBuilder errorLog = new StringBuilder();
        try {
            String pythonCmd = System.getProperty("os.name").toLowerCase().contains("win") ? "py" : "python3";
            ProcessBuilder pb = new ProcessBuilder(pythonCmd, "face_verify.py", referenceImagePath);
            pb.directory(new java.io.File(System.getProperty("user.dir")));
            pb.redirectErrorStream(true);

            Process p = pb.start();
            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(p.getInputStream()));
            String line;
            boolean match = false;

            while ((line = reader.readLine()) != null) {
                System.out.println("Python (Verify): " + line);
                errorLog.append(line).append("\n");
                if (line.equals("SUCCESS:MATCH")) {
                    match = true;
                } else if (line.equals("ERROR:NOT_MATCH")) {
                    // Running on background thread — must use Platform.runLater for UI alerts
                    Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Échec de l'authentification",
                            "Le visage capturé ne correspond pas au profil."));
                    return false;
                } else if (line.startsWith("ERROR:")) {
                    final String msg = line.substring(6).trim();
                    Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Erreur de caméra/capture", msg));
                    return false;
                }
            }

            int exitCode = p.waitFor();
            if (match) {
                return true;
            } else {
                String errMsg = errorLog.toString().trim();
                final String finalMsg = errMsg.isEmpty() ? "Aucune réponse de DeepFace." : errMsg;
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Erreur inattendue",
                        "Le processus de vérification a échoué (Code " + exitCode + ").\n" + finalMsg));
                return false;
            }

        } catch (Exception e) {
            final String exMsg = e.getMessage();
            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Erreur critique",
                    "Impossible de démarrer la reconnaissance faciale:\n" + exMsg));
            return false;
        }
    }

    @FXML
    private void handleForgotPassword(ActionEvent event) {
        new PasswordResetController().handleForgotPassword();
    }

    private boolean isValid(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Erreur de validation", "Veuillez saisir l'email et le mot de passe.");
            return false;
        }
        if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            showAlert(Alert.AlertType.WARNING, "Erreur de validation", "Veuillez saisir une adresse email valide.");
            return false;
        }
        return true;
    }

    private void navigateToDashboard(ActionEvent event) {
        try {
            boolean isAdmin = UserSession.getInstance().isAdmin();
            // Admins go to UserManagement, regular users go to the new UserDashboard
            String targetFxml = isAdmin ? "/Fintech/views/UserManagement.fxml" : "/Fintech/views/UserDashboard.fxml";

            FXMLLoader loader = new FXMLLoader(getClass().getResource(targetFxml));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur de navigation", "Impossible de charger l'interface.");
        }
    }

    /**
     * Handles Google Sign-In button click.
     * Runs the OAuth2 flow in a background thread to avoid freezing the UI.
     */
    @FXML
    private void handleGoogleLogin(ActionEvent event) {
        // Disable button to prevent double-clicks
        Button googleBtn = (Button) event.getSource();
        googleBtn.setDisable(true);
        googleBtn.setText("Connexion...");

        Task<Map<String, String>> googleTask = new Task<>() {
            @Override
            protected Map<String, String> call() {
                return GoogleAuthService.getUserInfo();
            }
        };

        googleTask.setOnSucceeded(e -> {
            Map<String, String> userInfo = googleTask.getValue();
            if (userInfo != null && !userInfo.getOrDefault("email", "").isEmpty()) {
                String name = userInfo.get("name");
                String email = userInfo.get("email");
                User user = serviceUser.createOrUpdateGoogleUser(name, email);
                if (user != null) {
                    UserSession.getInstance().setCurrentUser(user);
                    // Navigate on the JavaFX thread
                    Platform.runLater(() -> {
                        try {
                            boolean isAdmin = UserSession.getInstance().isAdmin();
                            // Admins go to UserManagement, regular users go to the new UserDashboard
                            String targetFxml = isAdmin ? "/Fintech/views/UserManagement.fxml"
                                    : "/Fintech/views/UserDashboard.fxml";

                            FXMLLoader loader = new FXMLLoader(getClass().getResource(targetFxml));
                            Parent root = loader.load();

                            Stage stage = (Stage) googleBtn.getScene().getWindow();
                            stage.setScene(new Scene(root));
                            stage.show();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger l'interface.");
                        }
                    });
                } else {
                    Platform.runLater(() -> showAlert(Alert.AlertType.ERROR,
                            "Erreur Google", "Impossible de créer le compte utilisateur."));
                }
            } else {
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR,
                        "Connexion annulée", "Aucune information reçue de Google."));
            }
            Platform.runLater(() -> {
                googleBtn.setDisable(false);
                googleBtn.setText("Google");
            });
        });

        googleTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                googleBtn.setDisable(false);
                googleBtn.setText("Google");
                showAlert(Alert.AlertType.ERROR, "Erreur Google",
                        "La connexion avec Google a échoué. Vérifiez votre connexion internet.");
            });
        });

        Thread thread = new Thread(googleTask);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void handleCreateAccount(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Fintech/views/UserForm.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur de navigation",
                    "Impossible de charger le formulaire d'inscription.");
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
