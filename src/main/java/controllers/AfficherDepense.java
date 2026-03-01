package controllers;

import entities.JustificatifDepense;
import entities.depense;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;
import services.ServiceDepense;
import utils.ApiClient;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AfficherDepense {
    @FXML private VBox vboxDepenses;
    private ServiceDepense service = new ServiceDepense();
    @FXML private ComboBox<String> categorieCombo;
     @FXML private ComboBox<String> paiementBox;
    @FXML
    private TextField montantField;

    @FXML private ComboBox<String> triChampCombo;
    @FXML private ComboBox<String> triTypeCombo;

    @FXML
    public void initialize() {
        triChampCombo.getItems().addAll("Montant", "Date");
        triChampCombo.setValue("Montant"); // valeur par défaut

        triTypeCombo.getItems().addAll("ASC", "DESC");
        triTypeCombo.setValue("ASC"); // valeur par défaut
        categorieCombo.getItems().addAll("","Nourriture", "Transport", "Loyer", "Internet", "Autre");
        paiementBox.getItems().addAll("","Espèces", "Carte bancaire", "Virement");
         loadDepenses();
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

        // ----------- DETAILS ----------
        VBox details = new VBox(5);
        Label date = new Label("📅 Date dépense : " + d.getDate_depense());
        Label categorie = new Label("🏷️ Catégorie : " + d.getCategorie());
        Label montant = new Label("💰 Montant : " + d.getMontant() + " DT");
        Label paiement = new Label("💳 Mode de Paiement :" + d.getMode_paiement());
        Label desc = new Label("📝 Description : " + d.getDescription());

        details.getChildren().addAll(date, categorie, montant, paiement, desc);
        HBox.setHgrow(details, Priority.ALWAYS);

        // ----------- ACTIONS ----------
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

        // ✅ MODIFIER
        btnModifier.setOnAction(ev -> openModifierDepense(d));
        // ✅ Justificatif
        btnJustificatif.setOnAction(ev -> openAjouterJustificatif(d));

        // ✅ SUPPRIMER
        btnSupprimer.setOnAction(ev -> {
            try {
                service.supprimer(d.getId_depense());
                loadDepenses(); // refresh
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        actions.getChildren().addAll(btnModifier, btnJustificatif,btnSupprimer);

        card.getChildren().addAll(details, actions);
        return card;
    }

    private void openAjouterJustificatif(depense d){
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/ListeJustificatif.fxml")
            );
            Parent root = loader.load();

            AffichierJustificatif controller = loader.getController();
            controller.setDepense(d); // 👈 passage de la dépense

            Stage stage = new Stage();
            stage.setTitle("Liste justificatif Dépense");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

         //   loadDepenses(); // refresh après modification

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ========================
    // Ouvrir ModifierDepense.fxml
    // ========================
    private void openModifierDepense(depense d) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/ModifierDepense.fxml")
            );
            Parent root = loader.load();

            ModifierDepense controller = loader.getController();
            controller.setDepense(d); // 👈 passage de la dépense

            Stage stage = new Stage();
            stage.setTitle("Modifier Dépense");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadDepenses(); // refresh après modification

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ========================
    // Sidebar actions
    // ========================
    @FXML private void showHome() {
        System.out.println("Home clicked");
    }

    @FXML private void showDepenses() {
        System.out.println("Depenses clicked");
    }

    @FXML private void showAbonnement() {
        System.out.println("Abonnement clicked");
    }

    @FXML private void showEducative() {
        System.out.println("Educative clicked");
    }

    @FXML private void showBudget() {
        System.out.println("Budget clicked");
    }

    @FXML private void showDashboard() {
        System.out.println("Budget clicked");
    }

    @FXML
    void showDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Dashboard.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Dashboard IA");
            stage.setScene(new Scene(root));

            // Fenêtre indépendante
            stage.initModality(Modality.NONE);

            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void filterDepenses() {
        try {
            // Récupérer les valeurs saisies
            String montantText = montantField.getText().trim();
            String categorie = categorieCombo.getValue();
            String mode = paiementBox.getValue();

            Double montantValue = null;
            if (!montantText.isEmpty()) {
                try {
                    montantValue = Double.parseDouble(montantText);
                } catch (NumberFormatException e) {
                    new Alert(Alert.AlertType.WARNING, "Montant invalide").show();
                    return;
                }
            }
            final Double montant = montantValue;

            List<depense> filtered = service.afficher(); // récupérer toutes les dépenses

            // Filtrage dynamique
            List<depense> result = new ArrayList<>(
                    filtered.stream()
                            .filter(d -> (montant == null || d.getMontant() == montant))
                            .filter(d -> (categorie == null || categorie.isEmpty() || d.getCategorie().equalsIgnoreCase(categorie)))
                            .filter(d -> (mode == null || mode.isEmpty() || d.getMode_paiement().equalsIgnoreCase(mode)))
                            .toList()
            );

            // ---------------------------
            // TRI si sélectionné
            // ---------------------------
            String champ = triChampCombo.getValue(); // champ pour trier
            String type = triTypeCombo.getValue();   // ASC ou DESC

            if (champ != null && !champ.isEmpty() && type != null && !type.isEmpty()) {
                result.sort((d1, d2) -> {
                    int cmp = 0;
                    switch (champ) {
                        case "Montant":
                            cmp = Double.compare(d1.getMontant(), d2.getMontant());
                            break;
                        case "Date":
                            cmp = d1.getDate_depense().compareTo(d2.getDate_depense());
                            break;
                        case "Catégorie":
                            cmp = d1.getCategorie().compareToIgnoreCase(d2.getCategorie());
                            break;
                        case "Mode de paiement":
                            cmp = d1.getMode_paiement().compareToIgnoreCase(d2.getMode_paiement());
                            break;
                    }
                    return "DESC".equalsIgnoreCase(type) ? -cmp : cmp;
                });
            }

            // Mettre à jour l'affichage
            vboxDepenses.getChildren().clear();
            for (depense d : result) {
                vboxDepenses.getChildren().add(createDepenseCard(d));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ========================
    // Ajouter Dépense
    // ========================
    @FXML
    private void openAjouterDepense() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/AjouterDepense.fxml")
            );
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Ajouter une Dépense");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadDepenses(); // refresh après ajout

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void HistoriqueDepense() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/Historique.fxml")
            );
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Historique Dépense");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadDepenses(); // refresh après ajout

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}



