package controllers;

import entities.depense;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import services.ServiceDepense;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AfficherDepense {

    @FXML private VBox vboxDepenses;

    @FXML private ComboBox<String> categorieCombo;
    @FXML private ComboBox<String> paiementBox;
    @FXML private TextField montantField;

    @FXML private ComboBox<String> triChampCombo;
    @FXML private ComboBox<String> triTypeCombo;

    private final ServiceDepense service = new ServiceDepense();

    // ✅ debounce: filter بعد 250ms من آخر حرف
    private final PauseTransition debounce = new PauseTransition(Duration.millis(250));

    @FXML
    public void initialize() {

        triChampCombo.getItems().addAll("Montant", "Date");
        triChampCombo.setValue("Montant");

        triTypeCombo.getItems().addAll("ASC", "DESC");
        triTypeCombo.setValue("ASC");

        categorieCombo.getItems().addAll("", "Nourriture", "Transport", "Loyer", "Internet", "Autre");
        paiementBox.getItems().addAll("", "Espèces", "Carte bancaire", "Virement");

        loadDepenses();

        // ✅ auto filter
        debounce.setOnFinished(_ -> applyFilterSort());

        montantField.textProperty().addListener((obs, o, n) -> debounce.playFromStart());

        categorieCombo.valueProperty().addListener((obs, o, n) -> applyFilterSort());
        paiementBox.valueProperty().addListener((obs, o, n) -> applyFilterSort());

        triChampCombo.valueProperty().addListener((obs, o, n) -> applyFilterSort());
        triTypeCombo.valueProperty().addListener((obs, o, n) -> applyFilterSort());
    }

    // ========================
    // Charger toutes les dépenses
    // ========================
    private void loadDepenses() {
        try {
            List<depense> list = service.afficher();
            vboxDepenses.getChildren().clear();

            for (depense d : list) {
                vboxDepenses.getChildren().add(createDepenseCard(d));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ========================
    // ✅ Auto Filter + Tri (بدل زر Filtrer)
    // ========================
    private void applyFilterSort() {
        try {
            // قيم الفورم
            String montantText = (montantField.getText() == null) ? "" : montantField.getText().trim();
            String categorie = categorieCombo.getValue();
            String mode = paiementBox.getValue();

            // ✅ parse montant بدون ما نكسّر الlambda
            Double tmpMontant = null;
            if (!montantText.isEmpty()) {
                try {
                    tmpMontant = Double.parseDouble(montantText);
                } catch (NumberFormatException ex) {
                    return; // ما نعملوش alert كل حرف
                }
            }

            // ✅ لازم final للـ stream lambdas
            final Double montant = tmpMontant;
            final String cat = categorie;
            final String pay = mode;

            List<depense> all = service.afficher();

            List<depense> result = new ArrayList<>(
                    all.stream()
                            .filter(d -> (montant == null || d.getMontant() == montant))
                            .filter(d -> (cat == null || cat.isEmpty() || d.getCategorie().equalsIgnoreCase(cat)))
                            .filter(d -> (pay == null || pay.isEmpty() || d.getMode_paiement().equalsIgnoreCase(pay)))
                            .toList()
            );

            // ✅ Tri
            String champ = triChampCombo.getValue();
            String type = triTypeCombo.getValue();

            if (champ != null && !champ.isEmpty() && type != null && !type.isEmpty()) {
                result.sort((d1, d2) -> {
                    int cmp = 0;
                    switch (champ) {
                        case "Montant" -> cmp = Double.compare(d1.getMontant(), d2.getMontant());
                        case "Date" -> cmp = d1.getDate_depense().compareTo(d2.getDate_depense());
                    }
                    return "DESC".equalsIgnoreCase(type) ? -cmp : cmp;
                });
            }

            // ✅ Update UI
            vboxDepenses.getChildren().clear();
            if (result.isEmpty()) {
                Label empty = new Label("Aucune dépense trouvée ✨");
                empty.setStyle("-fx-text-fill:#7b8794; -fx-font-size:14px; -fx-padding:25;");
                vboxDepenses.getChildren().add(empty);
                return;
            }

            for (depense d : result) {
                vboxDepenses.getChildren().add(createDepenseCard(d));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ========================
    // Carte Dépense
    // ========================
    private HBox createDepenseCard(depense d) {
        HBox card = new HBox(15);
        card.setPadding(new Insets(10));
        card.setStyle("""
                -fx-background-color: #ffffff;
                -fx-background-radius: 10;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5,0,0,2);
                """);

        VBox details = new VBox(5);
        Label date = new Label("📅 Date dépense : " + d.getDate_depense());
        Label categorie = new Label("🏷️ Catégorie : " + d.getCategorie());
        Label montant = new Label("💰 Montant : " + d.getMontant() + " DT");
        Label paiement = new Label("💳 Mode de Paiement : " + d.getMode_paiement());
        Label desc = new Label("📝 Description : " + d.getDescription());

        details.getChildren().addAll(date, categorie, montant, paiement, desc);
        HBox.setHgrow(details, Priority.ALWAYS);

        VBox actions = new VBox(8);

        Button btnModifier = new Button("Modifier");
        Button btnSupprimer = new Button("Supprimer");
        Button btnJustificatif = new Button("Justificatif");

        btnModifier.setStyle("""
                -fx-background-color:#0078D7;
                -fx-text-fill:white;
                -fx-background-radius:5;
                """);

        btnJustificatif.setStyle("""
                -fx-background-color:#008000;
                -fx-text-fill:white;
                -fx-background-radius:5;
                """);

        btnSupprimer.setStyle("""
                -fx-background-color:#D32F2F;
                -fx-text-fill:white;
                -fx-background-radius:5;
                """);

        btnModifier.setOnAction(ev -> openModifierDepense(d));
        btnJustificatif.setOnAction(ev -> openAjouterJustificatif(d));

        btnSupprimer.setOnAction(ev -> {
            try {
                service.supprimer(d.getId_depense());
                loadDepenses();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        actions.getChildren().addAll(btnModifier, btnJustificatif, btnSupprimer);
        card.getChildren().addAll(details, actions);
        return card;
    }

    private void openAjouterJustificatif(depense d) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ListeJustificatif.fxml"));
            Parent root = loader.load();

            AffichierJustificatif controller = loader.getController();
            controller.setDepense(d);

            Stage stage = new Stage();
            stage.setTitle("Liste justificatif Dépense");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openModifierDepense(depense d) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierDepense.fxml"));
            Parent root = loader.load();

            ModifierDepense controller = loader.getController();
            controller.setDepense(d);

            Stage stage = new Stage();
            stage.setTitle("Modifier Dépense");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadDepenses();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ========================
    // Ajouter Dépense
    // ========================
    @FXML
    private void openAjouterDepense() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterDepense.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Ajouter une Dépense");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadDepenses();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void HistoriqueDepense() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Historique.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Historique Dépense");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadDepenses();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ✅ showDashboard وحدة برك
    @FXML
    private void showDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Dashboard.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Dashboard IA");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.NONE);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Sidebar actions
    @FXML private void showDepenses() { loadDepenses(); }
    @FXML private void showAbonnement() { System.out.println("Abonnement clicked"); }
    @FXML private void showEducative() { System.out.println("Educative clicked"); }
    @FXML private void showBudget() { System.out.println("Budget clicked"); }
}