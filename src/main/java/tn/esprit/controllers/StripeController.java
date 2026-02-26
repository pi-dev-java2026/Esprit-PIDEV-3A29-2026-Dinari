package tn.esprit.controllers;

import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import tn.esprit.entities.Abonnement;
import tn.esprit.entities.Paiement;
import tn.esprit.services.PaiementService;
import tn.esprit.services.StripeService;
import tn.esprit.services.StripeService.ResultatPaiement;

import java.sql.Date;
import java.time.LocalDate;

/**
 * StripeController — Controller dédié à l'intégration Stripe.
 *
 * Responsabilités :
 *  - Valider les champs du formulaire de paiement
 *  - Appeler StripeService (API)
 *  - Sauvegarder le paiement en BDD si succès
 *  - Afficher le popup succès ou l'alerte d'erreur
 *
 * Utilisé par GestionabonnementsController :
 *      StripeController.getInstance().lancerPaiement(...)
 */
public class StripeController {

    // ── Singleton ─────────────────────────────────────────────────────────────
    private static StripeController instance;
    public static StripeController getInstance() {
        if (instance == null) instance = new StripeController();
        return instance;
    }

    // ── Dépendances ───────────────────────────────────────────────────────────
    private final PaiementService paieService = new PaiementService();

    // ── Callback vers le controller principal ─────────────────────────────────
    public interface OnPaiementTermine {
        void onSucces(String stripeId);
        void onEchec(String message);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // MÉTHODE PRINCIPALE — appelée depuis GestionabonnementsController
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Lance le processus de paiement Stripe complet.
     *
     * @param abonnement       L'abonnement à payer
     * @param montant          Montant sélectionné (15 / 40 / 80 TND)
     * @param tier             "Normal" / "Premium" / "Gold"
     * @param nomTitulaire     Nom saisi dans le formulaire
     * @param prenomTitulaire  Prénom saisi
     * @param numeroCarte      Numéro de carte (ex: 4242424242424242)
     * @param expiration       MM/YY ou MM/YYYY
     * @param cvv              CVV
     * @param btnPayer         Le bouton Payer (pour le désactiver pendant le traitement)
     * @param callback         Ce qui se passe après (succès ou échec)
     */
    public void lancerPaiement(
            Abonnement abonnement,
            double     montant,
            String     tier,
            String     nomTitulaire,
            String     prenomTitulaire,
            String     numeroCarte,
            String     expiration,
            String     cvv,
            Button     btnPayer,
            OnPaiementTermine callback) {

        // ── 1. Validation des champs ───────────────────────────────────────────
        String erreur = validerChamps(nomTitulaire, prenomTitulaire, numeroCarte, expiration, cvv, tier);
        if (erreur != null) {
            afficherAlerte(Alert.AlertType.WARNING, erreur);
            return;
        }

        // ── 2. Parser l'expiration MM/YY → mois + année ────────────────────────
        String[] parts  = expiration.split("/");
        String moisExp  = parts.length > 0 ? parts[0].trim() : "12";
        String anneeExp = parts.length > 1 ? parts[1].trim() : "2026";
        if (anneeExp.length() == 2) anneeExp = "20" + anneeExp;
        final String fMois = moisExp, fAnnee = anneeExp;

        // ── 3. Désactiver le bouton + texte d'attente ──────────────────────────
        String stylOriginal = btnPayer.getStyle();
        btnPayer.setText("⏳ Traitement Stripe...");
        btnPayer.setDisable(true);
        btnPayer.setStyle(stylOriginal.replaceAll("-fx-background-color:[^;]+;", "")
                + "-fx-background-color:#94a3b8;");

        // ── 4. Appel Stripe dans un thread séparé (ne bloque pas JavaFX) ────────
        String carteClean = numeroCarte.replaceAll("\\s+", "");
        String nomComplet = prenomTitulaire + " " + nomTitulaire;

        new Thread(() -> {

            // === APPEL API STRIPE ===
            ResultatPaiement resultat = StripeService.effectuerPaiement(
                    montant, carteClean, fMois, fAnnee, cvv, nomComplet);

            // ── 5. Retour sur le thread JavaFX ────────────────────────────────
            javafx.application.Platform.runLater(() -> {

                // Réactiver le bouton
                btnPayer.setDisable(false);
                btnPayer.setText("Payer " + tier + " — " + String.format("%.3f TND", montant));
                btnPayer.setStyle(stylOriginal);

                if (resultat.succes) {
                    // ── 6a. Succès → sauvegarder en BDD ───────────────────────
                    boolean sauvegarde = sauvegarderPaiement(
                            abonnement, montant, tier,
                            nomTitulaire, prenomTitulaire,
                            carteClean, expiration,
                            resultat.paymentIntentId);

                    if (sauvegarde) {
                        // Popup succès avec le vrai ID Stripe
                        afficherPopupSucces(
                                abonnement.getNom(),
                                montant,
                                resultat.paymentIntentId,
                                () -> callback.onSucces(resultat.paymentIntentId));
                    } else {
                        afficherAlerte(Alert.AlertType.ERROR,
                                "Paiement accepté par Stripe mais erreur de sauvegarde !\n" +
                                        "ID Stripe : " + resultat.paymentIntentId);
                    }

                } else {
                    // ── 6b. Échec → afficher l'erreur Stripe ──────────────────
                    afficherAlerte(Alert.AlertType.ERROR, resultat.message);
                    callback.onEchec(resultat.message);
                }
            });

        }).start();
    }

    // ═════════════════════════════════════════════════════════════════════════
    // SAUVEGARDE EN BASE DE DONNÉES
    // ═════════════════════════════════════════════════════════════════════════
    private boolean sauvegarderPaiement(
            Abonnement abo, double montant, String tier,
            String nom, String prenom,
            String carte, String expiration,
            String stripeId) {
        try {
            Paiement p = new Paiement();
            p.setAbonnementId(abo.getId());
            p.setMontant(montant);
            p.setDatePaiement(Date.valueOf(LocalDate.now()));
            p.setStatut("Paye");
            p.setModePaiement("Stripe — " + tier);
            p.setNomTitulaire(nom);
            p.setPrenomTitulaire(prenom);
            // Sécurité : on ne stocke que les 4 derniers chiffres
            p.setNumeroCarte("****" + carte.substring(Math.max(0, carte.length() - 4)));
            p.setDateExpiration(expiration);
            p.setCvv("***"); // Jamais stocker le vrai CVV

            // Stocker l'ID de transaction Stripe (colonne stripe_transaction_id)
            try {
                p.getClass()
                        .getMethod("setStripeTransactionId", String.class)
                        .invoke(p, stripeId);
            } catch (Exception ignored) {
                // Si le champ n'existe pas encore dans l'entité, on ignore
                System.out.println("ℹ️ Champ stripeTransactionId non trouvé dans Paiement.java");
            }

            paieService.ajouter(p);
            System.out.println("✅ Paiement sauvegardé en BDD — Stripe ID : " + stripeId);
            return true;

        } catch (Exception e) {
            System.err.println("❌ Erreur sauvegarde BDD : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // VALIDATION DES CHAMPS
    // ═════════════════════════════════════════════════════════════════════════
    private String validerChamps(String nom, String prenom, String carte,
                                 String expiration, String cvv, String tier) {
        if (tier == null)
            return "Veuillez sélectionner un plan (Normal / Premium / Gold) !";
        if (nom == null || nom.trim().isEmpty())
            return "Le nom du titulaire est requis !";
        if (prenom == null || prenom.trim().isEmpty())
            return "Le prénom du titulaire est requis !";
        if (carte == null || carte.replaceAll("\\s+","").length() < 13)
            return "Numéro de carte invalide !";
        if (expiration == null || !expiration.contains("/"))
            return "Date d'expiration invalide ! (format : MM/YY)";
        if (cvv == null || cvv.trim().length() < 3)
            return "CVV invalide !";
        return null; // tout est OK
    }

    // ═════════════════════════════════════════════════════════════════════════
    // POPUP SUCCÈS
    // ═════════════════════════════════════════════════════════════════════════
    private void afficherPopupSucces(String service, double montant, String stripeId, Runnable onFermer) {
        Stage pop = new Stage();
        pop.initModality(Modality.APPLICATION_MODAL);
        pop.initStyle(StageStyle.UNDECORATED);
        pop.setResizable(false);

        // ── Icône check animée ─────────────────────────────────────────────────
        StackPane inner = new StackPane();
        inner.setPrefSize(50,50); inner.setMaxSize(50,50);
        inner.setStyle("-fx-background-color:#22c55e;-fx-background-radius:50;");
        inner.setScaleX(0); inner.setScaleY(0);
        Label check = new Label("✓");
        check.setStyle("-fx-font-size:24px;-fx-font-weight:bold;-fx-text-fill:white;");
        inner.getChildren().add(check);
        StackPane halo = new StackPane(inner);
        halo.setPrefSize(66,66); halo.setMaxSize(66,66);
        halo.setStyle("-fx-background-color:#dcfce7;-fx-background-radius:50;");

        // ── Badge Stripe ───────────────────────────────────────────────────────
        Label badge = new Label("✓ Paiement vérifié par Stripe");
        badge.setStyle("-fx-background-color:#635bff;-fx-text-fill:white;" +
                "-fx-font-size:10px;-fx-font-weight:bold;" +
                "-fx-padding:5 14;-fx-background-radius:20;");

        // ── Textes ─────────────────────────────────────────────────────────────
        Label titre = new Label("Paiement réussi !");
        titre.setStyle("-fx-font-size:20px;-fx-font-weight:bold;-fx-text-fill:#0f1f3d;-fx-padding:12 0 2 0;");
        Label sous = new Label("Transaction validée par Stripe");
        sous.setStyle("-fx-font-size:12px;-fx-text-fill:#94a3b8;");

        VBox top = new VBox(6, halo, titre, sous, badge);
        top.setAlignment(Pos.CENTER);
        top.setStyle("-fx-padding:30 28 16 28;");

        // ── Détails transaction ────────────────────────────────────────────────
        VBox det = new VBox(0);
        det.setStyle("-fx-background-color:#f8fafc;-fx-background-radius:12;" +
                "-fx-border-color:#e2e8f0;-fx-border-width:1.5;-fx-border-radius:12;");
        det.setMaxWidth(290);
        det.getChildren().addAll(
                ligneDetail("Service",     service,                              true),
                ligneDetail("Montant",     String.format("%.3f TND", montant),  true),
                ligneDetail("ID Stripe",   stripeId,                            true),
                ligneDetail("Statut",      "✓ Paiement confirmé",               false)
        );
        HBox detW = new HBox(det); detW.setAlignment(Pos.CENTER);
        detW.setStyle("-fx-padding:14 28 0 28;");
        HBox.setHgrow(det, Priority.ALWAYS);

        // ── Bouton OK ──────────────────────────────────────────────────────────
        Button ok = new Button("OK");
        ok.setMaxWidth(290);
        ok.setStyle("-fx-background-color:#635bff;-fx-text-fill:white;" +
                "-fx-font-size:14px;-fx-font-weight:bold;" +
                "-fx-padding:14 0;-fx-background-radius:12;-fx-cursor:hand;");
        ok.setOnAction(e -> { pop.close(); if (onFermer != null) onFermer.run(); });

        HBox btnW = new HBox(ok); btnW.setAlignment(Pos.CENTER);
        btnW.setStyle("-fx-padding:16 28 28 28;");
        HBox.setHgrow(ok, Priority.ALWAYS);

        // ── Root ───────────────────────────────────────────────────────────────
        VBox root = new VBox(0, top, detW, btnW);
        root.setAlignment(Pos.TOP_CENTER); root.setPrefWidth(360);
        root.setStyle("-fx-background-color:white;-fx-background-radius:20;" +
                "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.22),40,0,0,8);");

        StackPane sr = new StackPane(root);
        sr.setStyle("-fx-background-color:transparent;-fx-padding:16;");
        Scene scene = new Scene(sr, 392, 480);
        scene.setFill(Color.TRANSPARENT);
        pop.setScene(scene);
        pop.show();

        // Animation du check
        ScaleTransition sc = new ScaleTransition(Duration.millis(450), inner);
        sc.setFromX(0); sc.setFromY(0); sc.setToX(1); sc.setToY(1);
        sc.setInterpolator(Interpolator.EASE_OUT);
        sc.play();
    }

    private HBox ligneDetail(String cle, String valeur, boolean bordure) {
        HBox row = new HBox(8); row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding:11 16;" +
                (bordure ? "-fx-border-color:#e2e8f0;-fx-border-width:0 0 1 0;" : ""));
        Label lk = new Label(cle);
        lk.setStyle("-fx-font-size:11px;-fx-text-fill:#64748b;");
        HBox.setHgrow(lk, Priority.ALWAYS);
        Label lv = new Label(valeur);
        lv.setStyle("-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:#0f1f3d;");
        row.getChildren().addAll(lk, lv);
        return row;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // ALERTE SIMPLE
    // ═════════════════════════════════════════════════════════════════════════
    private void afficherAlerte(Alert.AlertType type, String message) {
        Alert a = new Alert(type);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }
}