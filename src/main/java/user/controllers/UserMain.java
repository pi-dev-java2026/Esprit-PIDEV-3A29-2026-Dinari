package user.controllers;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class UserMain extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;

        FXMLLoader loader = new FXMLLoader(UserMain.class.getResource("/user/EducationHome.fxml"));
        Scene scene = new Scene(loader.load());

        stage.setTitle("DINARI - Student Portal");
        stage.setScene(scene);
        stage.show();
    }

    public static void switchScene(String fxml) throws Exception {
        FXMLLoader loader = new FXMLLoader(UserMain.class.getResource("/user/" + fxml));
        primaryStage.setScene(new Scene(loader.load()));
    }

    public static void main(String[] args) {
        launch();
    }
}
