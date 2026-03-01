package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import org.example.entities.Activite;
import org.example.services.ServiceActivite;
import org.example.services.ServicePlanification;

import java.io.IOException;
import java.sql.Date;
import java.util.Optional;

public class ActiviteEditController {

    @FXML private TextField descriptionTF;
    @FXML private TextField montantTF;
    @FXML private DatePicker dateDP;

    @FXML private ComboBox<String> categorieCB;


    @FXML private ComboBox<String> frequenceCB;
    @FXML private DatePicker finRecDP;

    @FXML private Label msgLabel;
    @FXML private ComboBox<String> deviseCB;

    private Activite current;
    private final ServiceActivite service = new ServiceActivite();
    private final ServicePlanification servicePlanif = new ServicePlanification();

    @FXML
    public void initialize() {
        frequenceCB.getItems().setAll("AUCUNE", "HEBDOMADAIRE", "MENSUELLE");
        frequenceCB.setValue("AUCUNE");

        try {
            categorieCB.getItems().setAll(servicePlanif.getDistinctCategories());
        } catch (Exception e) {
            e.printStackTrace();
        }

        finRecDP.setDisable(true);
        frequenceCB.valueProperty().addListener((obs, oldV, newV) -> {
            boolean recur = newV != null && !"AUCUNE".equalsIgnoreCase(newV);
            finRecDP.setDisable(!recur);
            if (!recur) finRecDP.setValue(null);
        });


        deviseCB.getItems().setAll("TND", "EUR", "USD", "GBP");
        deviseCB.setValue("TND");
    }

    public void setActivite(Activite a) {
        this.current = a;

        descriptionTF.setText(a.getDescription());
        montantTF.setText(String.valueOf(a.getMontant()));

        if (a.getDateActivite() != null) {
            dateDP.setValue(a.getDateActivite().toLocalDate());
        }

        if (a.getCategorie() != null) {
            if (!categorieCB.getItems().contains(a.getCategorie())) {
                categorieCB.getItems().add(a.getCategorie());
            }
            categorieCB.setValue(a.getCategorie());
        }

        String freq = (a.getFrequence() == null || a.getFrequence().isBlank())
                ? "AUCUNE"
                : a.getFrequence().toUpperCase();
        frequenceCB.setValue(freq);

        if (!"AUCUNE".equalsIgnoreCase(freq)) {
            finRecDP.setDisable(false);
            if (a.getDateFinRecurrence() != null) {
                finRecDP.setValue(a.getDateFinRecurrence().toLocalDate());
            }
        } else {
            finRecDP.setDisable(true);
            finRecDP.setValue(null);
        }


        String dev = (a.getDevise() == null || a.getDevise().isBlank())
                ? "TND"
                : a.getDevise().trim().toUpperCase();

        if (deviseCB != null) {
            if (!deviseCB.getItems().contains(dev)) {
                deviseCB.getItems().add(dev);
            }
            deviseCB.setValue(dev);
        }
    }

    @FXML
    private void save() {
        msgLabel.setText("");
        msgLabel.setStyle("-fx-text-fill: #d22;");

        if (current == null) {
            msgLabel.setText("Activité introuvable.");
            return;
        }


        double ancienMontantTND = current.getMontant();

        String desc = descriptionTF.getText() == null ? "" : descriptionTF.getText().trim();
        String montantStr = montantTF.getText() == null ? "" : montantTF.getText().trim();
        var localDate = dateDP.getValue();

        String categorie = (categorieCB == null || categorieCB.getValue() == null) ? "" : categorieCB.getValue().trim();

        String devise = (deviseCB == null || deviseCB.getValue() == null) ? "TND" : deviseCB.getValue().trim().toUpperCase();
        if (devise.isEmpty()) devise = "TND";

        String frequence = (frequenceCB == null || frequenceCB.getValue() == null)
                ? "AUCUNE"
                : frequenceCB.getValue().trim().toUpperCase();

        if (desc.isEmpty() || montantStr.isEmpty() || localDate == null || categorie.isEmpty()) {
            msgLabel.setText("Veuillez remplir tous les champs obligatoires.");
            return;
        }

        double montantSaisi;
        try {
            montantSaisi = Double.parseDouble(montantStr);
            if (montantSaisi <= 0) {
                msgLabel.setText("Le montant doit être > 0.");
                return;
            }
        } catch (NumberFormatException e) {
            msgLabel.setText("Montant invalide.");
            return;
        }

        Date finSql = null;
        if (!"AUCUNE".equalsIgnoreCase(frequence)) {
            if (finRecDP.getValue() == null) {
                msgLabel.setText("Choisir une date de fin de récurrence.");
                return;
            }
            if (finRecDP.getValue().isBefore(localDate)) {
                msgLabel.setText("La date de fin doit être ≥ date début.");
                return;
            }
            finSql = Date.valueOf(finRecDP.getValue());
        }


        double nouveauMontantTND = montantSaisi;
        try {
            if (!"TND".equals(devise)) {

                nouveauMontantTND = montantSaisi * new org.example.services.ExchangeRateService().getRate(devise, "TND");
            }
        } catch (Exception ex) {
            msgLabel.setText("Erreur lors de la conversion " + devise + " → TND.");
            return;
        }

        try {

            String mois = localDate.getMonth().getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.FRENCH);

            double sommeActuelle = service.sumMontantByCategorieAndMois(categorie, mois); // en TND
            double budgetAlloue = servicePlanif.getMontantAlloue(categorie, mois);

            if (budgetAlloue != 0) {
                double totalApresModif = sommeActuelle - ancienMontantTND + nouveauMontantTND;

                if (totalApresModif > budgetAlloue) {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Dépassement de budget");
                    alert.setHeaderText("Budget dépassé pour " + mois + " !");
                    alert.setContentText(
                            "Catégorie : " + categorie +
                                    "\nBudget alloué : " + budgetAlloue +
                                    "\nTotal actuel : " + sommeActuelle +
                                    "\nAncien montant (TND) : " + String.format("%.2f", ancienMontantTND) +
                                    "\nNouveau montant (TND) : " + String.format("%.2f", nouveauMontantTND) +
                                    "\nTotal après modification : " + String.format("%.2f", totalApresModif) +
                                    "\n\nVoulez-vous modifier quand même ?"
                    );
                    Optional<ButtonType> res = alert.showAndWait();
                    if (res.isEmpty() || res.get() != ButtonType.OK) return;
                }
            }


            ServiceActivite.AnomalyResult ar =
                    service.detectAnomalyZScore(nouveauMontantTND, categorie, Date.valueOf(localDate), 2.0);

            if (ar.anomaly) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Dépense inhabituelle");
                alert.setHeaderText("Dépense inhabituelle détectée !");
                alert.setContentText(
                        "Catégorie : " + categorie +
                                "\nNouveau montant (TND) : " + String.format("%.2f", nouveauMontantTND) +
                                "\nHistorique utilisé : " + ar.n + " dépenses (3 derniers mois)" +
                                "\nHabituel (≈) : " + String.format("%.2f", ar.low) + " - " + String.format("%.2f", ar.high) +
                                "\nMoyenne : " + String.format("%.2f", ar.mean) +
                                "\n(z = " + String.format("%.2f", ar.z) + ")" +
                                "\n\nVoulez-vous modifier quand même ?"
                );
                Optional<ButtonType> res = alert.showAndWait();
                if (res.isEmpty() || res.get() != ButtonType.OK) return;
            }


            current.setDescription(desc);
            current.setMontant(montantSaisi);
            current.setDateActivite(Date.valueOf(localDate));
            current.setCategorie(categorie);
            current.setDevise(devise);
            current.setFrequence(frequence);
            current.setDateFinRecurrence(finSql);

            service.modifier(current);
            backToList();

        } catch (Exception e) {
            e.printStackTrace();
            msgLabel.setText("Erreur lors de la modification.");
        }
    }

    @FXML
    private void backToList() {
        try {
            Parent view = FXMLLoader.load(getClass().getResource("/ActiviteList.fxml"));
            StackPane contentPane = (StackPane) descriptionTF.getScene().lookup("#contentPane");
            if (contentPane != null) contentPane.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}