package org.example.tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Mainfx extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainLayout.fxml"));
        Scene scene = new Scene(loader.load());

        // ✅ AJOUTE ÇA (le chemin du css dans resources)
        scene.getStylesheets().add(getClass().getResource("/dinari.css").toExternalForm());
        // ou dinari.css selon ton nom

        stage.setTitle("Dinari - Portefeuille Intelligent");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
