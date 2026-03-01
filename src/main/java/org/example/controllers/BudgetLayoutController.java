package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

public class BudgetLayoutController {

    @FXML private StackPane contentPane;


    @FXML private Button btnPlanif, btnActivite;


    @FXML private Button tabPlanif, tabActivite;

    @FXML
    public void initialize() {

        selectPlanif();
        loadCenter("/PlanificationAdd.fxml");
    }


    @FXML
    private void goPlanificationAdd() {
        selectPlanif();
        loadCenter("/PlanificationAdd.fxml");
    }

    @FXML
    private void goPlanificationList() {
        selectPlanif();
        loadCenter("/PlanificationList.fxml");
    }


    @FXML
    private void goActiviteAdd() {
        selectActivite();
        loadCenter("/ActiviteAdd.fxml");
    }

    @FXML
    private void goActiviteList() {
        selectActivite();
        loadCenter("/ActiviteList.fxml");
    }


    private void loadCenter(String fxmlPath) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentPane.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void selectPlanif() {
        setSelected(btnPlanif, true);
        setSelected(btnActivite, false);

        setSelected(tabPlanif, true);
        setSelected(tabActivite, false);
    }

    private void selectActivite() {
        setSelected(btnPlanif, false);
        setSelected(btnActivite, true);

        setSelected(tabPlanif, false);
        setSelected(tabActivite, true);
    }

    private void setSelected(Button b, boolean selected) {
        if (b == null) return;
        if (selected) {
            if (!b.getStyleClass().contains("selected")) b.getStyleClass().add("selected");
        } else {
            b.getStyleClass().remove("selected");
        }
    }

}
