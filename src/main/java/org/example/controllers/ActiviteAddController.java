package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import org.example.entities.Activite;
import org.example.entities.MeteoDay;
import org.example.services.ExchangeRateService;
import org.example.services.MeteoService;
import org.example.services.ServiceActivite;
import org.example.services.ServicePlanification;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Optional;
import javafx.concurrent.Task;
import javafx.stage.FileChooser;
import java.io.File;
import java.time.LocalDate;

import org.example.entities.ReceiptData;
import org.example.services.MindeeOcrService;

public class ActiviteAddController {

    @FXML private TextField descriptionTF;
    @FXML private TextField montantTF;
    @FXML private DatePicker dateDP;
    @FXML private ComboBox<String> categorieCB;
    @FXML private ComboBox<String> deviseCB;


    @FXML private ComboBox<String> frequenceCB;
    @FXML private DatePicker finRecDP;

    @FXML private Label msgLabel;

    private final ServiceActivite serviceActivite = new ServiceActivite();
    private final ServicePlanification servicePlanif = new ServicePlanification();


    private final ExchangeRateService rateService = new ExchangeRateService();

    @FXML
    public void initialize() {
        initFrequenceUI();
        loadCategories();

        if (deviseCB != null) {
            deviseCB.getItems().setAll("TND", "EUR", "USD", "GBP");
            deviseCB.setValue("TND");
        }
    }

    private void initFrequenceUI() {
        if (frequenceCB != null) {
            frequenceCB.getItems().setAll("AUCUNE", "HEBDOMADAIRE", "MENSUELLE");
            frequenceCB.setValue("AUCUNE");
            frequenceCB.valueProperty().addListener((obs, oldV, newV) -> {
                boolean recur = newV != null && !"AUCUNE".equalsIgnoreCase(newV);
                if (finRecDP != null) {
                    finRecDP.setDisable(!recur);
                    if (!recur) finRecDP.setValue(null);
                }
            });
        }
        if (finRecDP != null) {
            finRecDP.setDisable(true);
        }
    }

    private void loadCategories() {
        try {
            if (categorieCB == null) return;

            categorieCB.getItems().clear();
            categorieCB.getItems().addAll(servicePlanif.getDistinctCategories());

            if (!categorieCB.getItems().isEmpty()) {
                categorieCB.getSelectionModel().selectFirst();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void ajouter() {
        setError("");

        // 1) Lire + valider champs obligatoires
        String desc = safeText(descriptionTF);
        String montantStr = safeText(montantTF);
        LocalDate date = (dateDP == null) ? null : dateDP.getValue();
        String categorie = (categorieCB == null || categorieCB.getValue() == null) ? "" : categorieCB.getValue().trim();


        String devise = (deviseCB == null || deviseCB.getValue() == null) ? "TND" : deviseCB.getValue().trim().toUpperCase();
        if (devise.isEmpty()) devise = "TND";

        String frequence = (frequenceCB == null || frequenceCB.getValue() == null)
                ? "AUCUNE"
                : frequenceCB.getValue().trim().toUpperCase();

        if (desc.isEmpty() || montantStr.isEmpty() || date == null || categorie.isEmpty()) {
            setError("Veuillez remplir tous les champs obligatoires.");
            return;
        }

        double montantSaisi;
        try {
            montantSaisi = Double.parseDouble(montantStr);
            if (montantSaisi <= 0) {
                setError("Le montant doit être > 0.");
                return;
            }
        } catch (NumberFormatException ex) {
            setError("Montant invalide (ex: 120).");
            return;
        }


        String mois = date.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH);


        Date finSql = null;
        if (!"AUCUNE".equalsIgnoreCase(frequence)) {
            LocalDate fin = (finRecDP == null) ? null : finRecDP.getValue();

            if (fin == null) {
                setError("Choisir une date de fin de récurrence.");
                return;
            }
            if (fin.isBefore(date)) {
                setError("La date de fin doit être ≥ date début.");
                return;
            }
            finSql = Date.valueOf(fin);
        }


        double montantTNDForChecks = montantSaisi;
        try {
            if (!"TND".equals(devise)) {
                double taux = rateService.getRate(devise, "TND");
                montantTNDForChecks = montantSaisi * taux;
            }
        } catch (Exception ex) {
            setError("Erreur lors de la conversion " + devise + " → TND.");
            return;
        }

        try {

            double sommeActuelle = serviceActivite.sumMontantByCategorieAndMois(categorie, mois);
            double budgetAlloue = servicePlanif.getMontantAlloue(categorie, mois);

            if (budgetAlloue == 0) {
                boolean ok = confirm(
                        "Planification introuvable",
                        "Aucune planification trouvée pour cette catégorie et ce mois.",
                        "Catégorie: " + categorie + "\nMois: " + mois + "\n\nVoulez-vous ajouter l'activité quand même ?"
                );
                if (!ok) return;
            } else {
                double totalApresAjout = sommeActuelle + montantTNDForChecks; // ✅ EN TND
                if (totalApresAjout > budgetAlloue) {
                    boolean ok = confirm(
                            "Dépassement de budget",
                            "Budget dépassé pour " + mois + " !",
                            "Catégorie : " + categorie
                                    + "\nBudget alloué : " + budgetAlloue
                                    + "\nTotal actuel : " + sommeActuelle
                                    + "\nTotal après ajout : " + totalApresAjout
                                    + "\n\nVoulez-vous ajouter quand même ?"
                    );
                    if (!ok) return;
                }
            }


            ServiceActivite.AnomalyResult ar =
                    serviceActivite.detectAnomalyZScore(montantTNDForChecks, categorie, Date.valueOf(date), 2.0);

            if (ar.anomaly) {
                boolean okAnom = confirm(
                        "Dépense inhabituelle",
                        "Dépense inhabituelle détectée !",
                        "Catégorie : " + categorie +
                                "\nMontant (TND) : " + String.format("%.2f", montantTNDForChecks) +
                                "\nHistorique utilisé : " + ar.n + " dépenses (3 derniers mois)" +
                                "\nHabituel (≈) : " + String.format("%.2f", ar.low) + " - " + String.format("%.2f", ar.high) +
                                "\nMoyenne : " + String.format("%.2f", ar.mean) +
                                "\n(z = " + String.format("%.2f", ar.z) + ")" +
                                "\n\nVoulez-vous ajouter quand même ?"
                );
                if (!okAnom) return;
            }


            Activite a = new Activite(desc, montantSaisi, Date.valueOf(date), categorie, devise, frequence, finSql);
            serviceActivite.ajouter(a);

            ActiviteListController.successMessage = "✅ Activité ajoutée avec succès !";
            goToList();

            if (categorieCB != null && !categorieCB.getItems().contains(categorie)) {
                categorieCB.getItems().add(categorie);
            }

        } catch (Exception e) {
            e.printStackTrace();
            setError("Erreur lors de l'ajout.");
        }
    }

    @FXML
    private void clear() {
        if (descriptionTF != null) descriptionTF.clear();
        if (montantTF != null) montantTF.clear();
        if (dateDP != null) dateDP.setValue(null);

        if (deviseCB != null) deviseCB.setValue("TND");

        if (frequenceCB != null) frequenceCB.setValue("AUCUNE");
        if (finRecDP != null) {
            finRecDP.setValue(null);
            finRecDP.setDisable(true);
        }
    }

    @FXML
    private void goList() {
        swapCenter("/ActiviteList.fxml");
    }

    private void swapCenter(String fxmlPath) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));

            Node anyNode = (descriptionTF != null) ? descriptionTF : montantTF;
            if (anyNode == null || anyNode.getScene() == null) return;

            StackPane contentPane = (StackPane) anyNode.getScene().lookup("#contentPane");
            if (contentPane != null) {
                contentPane.getChildren().setAll(view);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------- Helpers UI ----------
    private String safeText(TextField tf) {
        if (tf == null || tf.getText() == null) return "";
        return tf.getText().trim();
    }

    private void setError(String msg) {
        if (msgLabel == null) return;
        msgLabel.setStyle("-fx-text-fill: #d22;");
        msgLabel.setText(msg == null ? "" : msg);
    }

    private void setSuccess(String msg) {
        if (msgLabel == null) return;
        msgLabel.setStyle("-fx-text-fill: green;");
        msgLabel.setText(msg == null ? "" : msg);
    }

    private boolean confirm(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        Optional<ButtonType> res = alert.showAndWait();
        return res.isPresent() && res.get() == ButtonType.OK;
    }
    private final MeteoService meteoService = new MeteoService();
    @FXML
    private void checkMeteo() {
        LocalDate d = dateDP.getValue();
        if (d == null) {
            showInfo("Choisis une date d'abord.");
            return;
        }

        try {
            MeteoDay info = meteoService.getMeteoForDate(d);

            if (info == null) {
                showInfo("Prévision dispo seulement ~16 jours. Choisis une date plus proche.");
                return;
            }

            String msg = "📅 " + info.getDate()
                    + "\n🌡 Temp max : " + info.getTempMax() + "°C"
                    + "\n🌧 Pluie : " + info.getRainMm() + " mm";

            if (info.getRainMm() >= 5) msg += "\n\n💡 Pluie → prévoir + transport (taxi).";
            if (info.getTempMax() >= 30) msg += "\n\n💡 Chaleur → prévoir + électricité (clim).";

            showInfo(msg);

        } catch (Exception e) {
            e.printStackTrace();
            showInfo("Erreur météo (internet ?).");
        }
    }
    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Météo");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
    @FXML
    private void handleScanReceipt() {


        FileChooser fc = new FileChooser();
        fc.setTitle("Choisir une facture");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fc.showOpenDialog(descriptionTF.getScene().getWindow());
        if (file == null) return;

        msgLabel.setText("Scan en cours...");


        Task<ReceiptData> task = new Task<>() {
            @Override
            protected ReceiptData call() throws Exception {
                String apiKey = "md_qc2zWgW0mxfTqH7aQXVjA4lmoMnQk2iT01AsikLzvqg";
                String modelId = "7750e2ee-ce69-4932-ac09-8268ab4ca73b";

                MindeeOcrService ocr = new MindeeOcrService(apiKey, modelId);
                return ocr.scan(file.getAbsolutePath());
            }
        };


        task.setOnSucceeded(e -> {
            ReceiptData data = task.getValue();


            if (data.supplierName != null && !data.supplierName.isBlank()) {
                descriptionTF.setText(data.supplierName);
            }


            montantTF.setText(String.valueOf(data.totalAmount));


            if (data.date != null && !data.date.isBlank()) {
                dateDP.setValue(LocalDate.parse(data.date));
            }

            msgLabel.setText("✅ Scan terminé");
        });


        task.setOnFailed(e -> {
            msgLabel.setText("❌ Erreur scan: " + task.getException().getMessage());
            task.getException().printStackTrace();
        });

        new Thread(task).start();
    }
    private void goToList() {
        try {
            Parent view = FXMLLoader.load(getClass().getResource("/ActiviteList.fxml"));
            StackPane contentPane = (StackPane) descriptionTF.getScene().lookup("#contentPane");
            if (contentPane != null) {
                contentPane.getChildren().setAll(view);
            }
        } catch (Exception e) {
            e.printStackTrace();
            setError("Erreur navigation vers la liste.");
        }
    }
}