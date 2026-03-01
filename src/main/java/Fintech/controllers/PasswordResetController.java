package Fintech.controllers;

import Fintech.servicies.ServiceUser;
import Fintech.utils.EmailService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.Optional;
import java.util.Random;

/**
 * Gère le flux complet "Mot de passe oublié" avec des dialogues JavaFX.
 *
 * Étapes :
 * 1. Demande l'email
 * 2. Génère un code à 6 chiffres, l'enregistre en base, l'envoie par email
 * 3. Demande le code reçu
 * 4. Vérifie le code puis demande et confirme le nouveau mot de passe
 * 5. Met à jour la base et affiche un message de succès
 */
public class PasswordResetController {

    private final ServiceUser serviceUser = new ServiceUser();

    // ─────────────────────────── POINT D'ENTRÉE ───────────────────────────────

    /** Appelé depuis LoginController quand l'utilisateur clique "Oublié ?" */
    public void handleForgotPassword() {
        // Étape 1 : demander l'email
        Optional<String> emailOpt = showEmailDialog();
        if (emailOpt.isEmpty())
            return; // annulé

        String email = emailOpt.get().trim();

        if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            showError("Email invalide", "Veuillez saisir une adresse email valide.");
            return;
        }

        if (!serviceUser.emailExists(email)) {
            showError("Email introuvable", "Aucun compte n'est associé à cet email.");
            return;
        }

        // Étape 2 : générer + enregistrer + envoyer le code (en arrière-plan)
        String code = generateCode();
        sendCodeInBackground(email, code);
    }

    // ─────────────────────────── ÉTAPES INTERNES ──────────────────────────────

    private void sendCodeInBackground(String email, String code) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // Sauvegarde en base
                serviceUser.saveResetCode(email, code);
                // Envoi email
                EmailService.sendEmail(
                        email,
                        "🔐 Réinitialisation de votre mot de passe — IDINARI",
                        "Bonjour,\n\n" +
                                "Voici votre code de réinitialisation : " + code + "\n\n" +
                                "Ce code est valable pour une seule utilisation.\n" +
                                "Si vous n'avez pas demandé ce code, ignorez cet email.\n\n" +
                                "— L'équipe IDINARI");
                return null;
            }
        };

        // Indicateur "envoi en cours…"
        Alert sending = new Alert(Alert.AlertType.INFORMATION);
        sending.setTitle("Envoi en cours");
        sending.setHeaderText(null);
        sending.setContentText("Envoi du code à " + email + "…\nPatientez un instant.");
        // On garde un ButtonType.CANCEL caché pour que le bouton X reste actif
        sending.getButtonTypes().setAll(ButtonType.CANCEL);
        // On cache le bouton Annuler visuellement mais le X de la fenêtre reste
        // fonctionnel
        sending.getDialogPane().lookupButton(ButtonType.CANCEL).setVisible(false);
        sending.getDialogPane().lookupButton(ButtonType.CANCEL).setManaged(false);

        task.setOnSucceeded(e -> Platform.runLater(() -> {
            sending.close();
            askForCode(email);
        }));
        task.setOnFailed(e -> Platform.runLater(() -> {
            sending.close();
            Throwable ex = task.getException();
            ex.printStackTrace();
            showError("Erreur d'envoi", "Impossible d'envoyer l'email :\n" + ex.getMessage());
        }));

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();

        sending.show();
    }

    private void askForCode(String email) {
        // Étape 3 : demander le code reçu par email
        Optional<String> codeOpt = showInputDialog(
                "Code de vérification",
                "Saisissez le code à 6 chiffres envoyé à :\n" + email,
                "Code :");
        if (codeOpt.isEmpty())
            return; // annulé

        String enteredCode = codeOpt.get().trim();

        // Étape 4 : vérifier le code EN PREMIER avant de demander le nouveau mot de
        // passe
        if (!serviceUser.verifyResetCode(email, enteredCode)) {
            showError("Code incorrect", "Le code saisi est incorrect ou a déjà été utilisé.");
            return;
        }

        // Étape 5 : demander le nouveau mot de passe
        Optional<String[]> passwordOpt = showNewPasswordDialog();
        if (passwordOpt.isEmpty())
            return; // annulé

        String newPassword = passwordOpt.get()[0];
        String confirmPassword = passwordOpt.get()[1];

        if (!newPassword.equals(confirmPassword)) {
            showError("Erreur", "Les mots de passe ne correspondent pas.");
            return;
        }
        if (newPassword.length() < 6) {
            showError("Erreur", "Le mot de passe doit contenir au moins 6 caractères.");
            return;
        }

        // Étape 6 : mettre à jour le mot de passe en base
        boolean success = serviceUser.updatePasswordAndClearCode(email, newPassword);
        if (success) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès ! 🎉");
            alert.setHeaderText(null);
            alert.setContentText("Votre mot de passe a été réinitialisé avec succès.\n" +
                    "Vous pouvez maintenant vous connecter avec votre nouveau mot de passe.");
            alert.showAndWait();
        } else {
            showError("Erreur", "Impossible de mettre à jour le mot de passe. Veuillez réessayer.");
        }
    }

    // ─────────────────────────── DIALOGUES ────────────────────────────────────

    /** Affiche une boîte de dialogue pour saisir l'email. */
    private Optional<String> showEmailDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Mot de passe oublié");
        dialog.setHeaderText("Réinitialisation du mot de passe");
        dialog.setContentText("Saisissez votre adresse email :");
        dialog.getEditor().setPromptText("exemple@email.com");
        return dialog.showAndWait();
    }

    /** Affiche une boîte de dialogue avec un champ de saisie générique. */
    private Optional<String> showInputDialog(String title, String header, String label) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(label);
        return dialog.showAndWait();
    }

    /**
     * Affiche un dialogue avec deux champs mot de passe (nouveau + confirmation).
     */
    private Optional<String[]> showNewPasswordDialog() {
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Nouveau mot de passe");
        dialog.setHeaderText("Créez votre nouveau mot de passe");

        ButtonType okButton = new ButtonType("Confirmer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        PasswordField pwField = new PasswordField();
        PasswordField confirmField = new PasswordField();
        pwField.setPromptText("Nouveau mot de passe");
        confirmField.setPromptText("Confirmer le mot de passe");

        grid.add(new Label("Nouveau mot de passe :"), 0, 0);
        grid.add(pwField, 1, 0);
        grid.add(new Label("Confirmer :"), 0, 1);
        grid.add(confirmField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        Platform.runLater(pwField::requestFocus);

        dialog.setResultConverter(btn -> {
            if (btn == okButton) {
                return new String[] { pwField.getText(), confirmField.getText() };
            }
            return null;
        });

        return dialog.showAndWait();
    }

    // ─────────────────────────── UTILITAIRES ──────────────────────────────────

    private String generateCode() {
        return String.format("%06d", new Random().nextInt(1_000_000));
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
