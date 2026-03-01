package Fintech.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainDashboardController implements Initializable {

    @FXML
    private StackPane contentArea;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadView("/Fintech/views/DashboardHome.fxml");
    }

    @FXML
    private void showDashboard(ActionEvent event) {
        loadView("/Fintech/views/DashboardHome.fxml");
    }

    @FXML
    private void showUsers(ActionEvent event) {
        boolean isAdmin = Fintech.utils.UserSession.getInstance().isAdmin();
        if (isAdmin) {
            loadView("/Fintech/views/UserList.fxml");
        } else {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fintech/views/UserProfile.fxml"));
                Parent view = loader.load();
                UserProfileController controller = loader.getController();
                controller.setUser(Fintech.utils.UserSession.getInstance().getCurrentUser());
                contentArea.getChildren().setAll(view);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void showReclamations(ActionEvent event) {
        loadView("/Fintech/views/ReclamationManagement.fxml");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            Fintech.utils.UserSession.getInstance().clearSession();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fintech/views/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
