package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

public class MainLayoutController {

    @FXML private StackPane mainContentPane;

    @FXML
    public void initialize() {
        goBudget(); // affichage par défaut
    }

    @FXML
    private void goBudget() {
        try {
            Parent view = FXMLLoader.load(getClass().getResource("/BudgetLayout.fxml"));
            mainContentPane.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}