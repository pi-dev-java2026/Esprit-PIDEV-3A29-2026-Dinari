
package com.gestion;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {

        primaryStage = stage;


        FXMLLoader loader =
                new FXMLLoader(Main.class.getResource("/ListeAvis.fxml"));

        Scene scene = new Scene(loader.load());

        stage.setTitle("Gestion Educative");
        stage.setScene(scene);
        stage.show();
    }

    public static void switchScene(String fxml) throws Exception {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource(fxml));
        primaryStage.setScene(new Scene(loader.load()));
    }

    public static void main(String[] args) {
        launch();
    }
}
