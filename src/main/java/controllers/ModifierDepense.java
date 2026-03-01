package controllers;

import entities.depense;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.ServiceDepense;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class ModifierDepense {

    @FXML private DatePicker dateField;
    @FXML private ComboBox<String> categorieCombo;
    @FXML private TextField montantField;
    @FXML private ComboBox<String> modePaiementCombo;
    @FXML private TextField descriptionField;

    private depense depenseSelectionnee;        // dépense sélectionnée pour modification
    private ServiceDepense service = new ServiceDepense();

    @FXML
    public void initialize() {
        // Remplir les ComboBox avec les valeurs possibles
        categorieCombo.getItems().addAll(
                "Alimentation",
                "Transport",
                "Logement",
                "Santé",
                "Loisirs",
                "Autres"
        );

        modePaiementCombo.getItems().addAll(
                "Espèces",
                "Carte bancaire",
                "Virement"
        );
    }

    /**
     * Appelée depuis AfficherDepense pour initialiser la fenêtre
     */
    public void setDepense(depense d) {
        this.depenseSelectionnee = d;
        Date utilDate = d.getDate_depense();

        LocalDate localDate;

        if (utilDate instanceof java.sql.Date) {
            localDate = ((java.sql.Date) utilDate).toLocalDate();
        } else {
            localDate = utilDate.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        }

        dateField.setValue(localDate);

         categorieCombo.setValue(d.getCategorie());
        montantField.setText(String.valueOf(d.getMontant()));
        modePaiementCombo.setValue(d.getMode_paiement());
        descriptionField.setText(d.getDescription());
    }

    /**
     * Modifier la dépense et mettre à jour la base de données
     */
    @FXML
    private void modifierDepense() {
        try {
            // Vérification que tous les champs sont remplis
            if (dateField.getValue() == null ||
                    categorieCombo.getValue() == null ||
                    modePaiementCombo.getValue() == null ||
                    montantField.getText().isEmpty() ||descriptionField.getText().isEmpty()) {

                showAlert("Erreur", "Veuillez remplir tous les champs !");
                return;
            }

            // ✅ Conversion LocalDate -> java.util.Date
            LocalDate localDate = dateField.getValue();
            Date utilDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

            // Mise à jour de la dépense sélectionnée
            depenseSelectionnee.setDate_depense(utilDate);
            depenseSelectionnee.setCategorie(categorieCombo.getValue());
            depenseSelectionnee.setMontant(Float.parseFloat(montantField.getText()));
            depenseSelectionnee.setMode_paiement(modePaiementCombo.getValue());
            depenseSelectionnee.setDescription(descriptionField.getText());

            // Appel du service pour modifier dans la base
            service.modifier(depenseSelectionnee);

            // Fermer la fenêtre après modification
            closeWindow();

        } catch (NumberFormatException e) {
            showAlert("Erreur", "Montant invalide !");
        } catch (SQLException e) {
            showAlert("Erreur SQL", e.getMessage());
        }
    }

    /**
     * Fermer la fenêtre en cours
     */
    @FXML
    private void closeWindow() {
        Stage stage = (Stage) dateField.getScene().getWindow();
        stage.close();
    }

    /**
     * Afficher une alert simple avec un message d'erreur
     */
    private void showAlert(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
