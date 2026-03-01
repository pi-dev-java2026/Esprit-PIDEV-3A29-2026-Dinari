package tn.esprit.controllers;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import tn.esprit.entities.Abonnement;

public class MainController {

    @FXML public  StackPane contenu;
    @FXML private Button    btnNotifToggle;

    // Sidebar
    @FXML private Button btnAbonnements;
    @FXML private Button btnBudget;
    @FXML private Button btnDepenses;
    @FXML private Button btnEducation;

    // Onglets
    @FXML private Button tabAbonnements;
    @FXML private Button tabPaiements;
    @FXML private Button tabStats;
    @FXML private Button tabRecommandations;
    @FXML private Button tabSuggestions;
    @FXML private Button tabCalendar;

    // État notifications
    private boolean notificationsActives = true;

    private final String ACTIVE =
            "-fx-background-color: #1a3a7a; -fx-text-fill: white; -fx-font-size: 13px; " +
                    "-fx-alignment: CENTER-LEFT; -fx-padding: 11 14; -fx-background-radius: 8; " +
                    "-fx-cursor: hand; -fx-font-weight: bold; -fx-border-color: #2ecc71; " +
                    "-fx-border-width: 0 0 0 3; -fx-border-radius: 0 8 8 0;";

    private final String INACTIVE =
            "-fx-background-color: transparent; -fx-text-fill: #6a8aaa; -fx-font-size: 13px; " +
                    "-fx-alignment: CENTER-LEFT; -fx-padding: 11 14; -fx-background-radius: 8; -fx-cursor: hand;";

    private final String TAB_ON =
            "-fx-background-color: transparent; -fx-text-fill: #1a3a7a; -fx-font-size: 13px; " +
                    "-fx-font-weight: bold; -fx-padding: 16 20; -fx-cursor: hand; " +
                    "-fx-border-color: #1a3a7a; -fx-border-width: 0 0 3 0; -fx-background-radius: 0;";

    private final String TAB_OFF =
            "-fx-background-color: transparent; -fx-text-fill: #8899aa; -fx-font-size: 13px; " +
                    "-fx-padding: 16 20; -fx-cursor: hand; -fx-border-width: 0; -fx-background-radius: 0;";

    @FXML
    public void initialize() { showAbonnements(); }

    // ===== SIDEBAR =====
    @FXML public void showAbonnements() { load("Gestionabonnements.fxml"); setSidebar(btnAbonnements); setTab(tabAbonnements); }
    @FXML public void showBudget()      { setSidebar(btnBudget); }
    @FXML public void showDepenses()    { setSidebar(btnDepenses); }
    @FXML public void showEducation()   { setSidebar(btnEducation); }

    // ===== ONGLETS =====
    @FXML public void switchAbonnements()     { load("Gestionabonnements.fxml"); setSidebar(btnAbonnements); setTab(tabAbonnements); }
    @FXML public void switchPaiements()       { load("Gestionpaiements.fxml");   setSidebar(null);           setTab(tabPaiements); }
    @FXML public void switchStats()           {                                   setSidebar(null);           setTab(tabStats); }
    @FXML public void switchRecommandations() { load("Recommandation.fxml");      setSidebar(null);           setTab(tabRecommandations); }
    @FXML public void switchSuggestions()     { load("Suggestions.fxml");         setSidebar(null);           setTab(tabSuggestions); }
    @FXML public void switchCalendar()        { load("Calendrier.fxml");          setSidebar(null);           setTab(tabCalendar); }

    // ══════════════════════════════════════════════════════════════════════
    // 🔔 TOGGLE NOTIFICATIONS
    // ══════════════════════════════════════════════════════════════════════
    @FXML
    public void toggleNotifications() {
        notificationsActives = !notificationsActives;

        if (notificationsActives) {
            btnNotifToggle.setText("🔔");
            btnNotifToggle.setStyle(
                    "-fx-background-color: transparent; -fx-text-fill: #6a8aaa;" +
                            "-fx-font-size: 18px; -fx-cursor: hand;" +
                            "-fx-border-width: 0; -fx-padding: 8 14;"
            );
            afficherMiniToast("🔔 Notifications activées", "#27ae60");
        } else {
            btnNotifToggle.setText("🔕");
            btnNotifToggle.setStyle(
                    "-fx-background-color: transparent; -fx-text-fill: #e74c3c;" +
                            "-fx-font-size: 18px; -fx-cursor: hand;" +
                            "-fx-border-width: 0; -fx-padding: 8 14;"
            );
            afficherMiniToast("🔕 Notifications désactivées", "#e74c3c");
        }
    }

    public boolean isNotificationsActives() {
        return notificationsActives;
    }

    // Mini toast discret en haut à droite
    private void afficherMiniToast(String texte, String couleur) {
        if (contenu == null) return;

        Label toast = new Label(texte);
        toast.setStyle(
                "-fx-background-color: " + couleur + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 10 18;" +
                        "-fx-background-radius: 20;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 4);"
        );
        StackPane.setAlignment(toast, Pos.TOP_RIGHT);
        toast.setTranslateX(-20);
        toast.setTranslateY(20);
        toast.setOpacity(0);
        contenu.getChildren().add(toast);

        FadeTransition fi = new FadeTransition(Duration.millis(250), toast);
        fi.setFromValue(0); fi.setToValue(1); fi.play();

        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(e -> {
            FadeTransition fo = new FadeTransition(Duration.millis(300), toast);
            fo.setFromValue(1); fo.setToValue(0);
            fo.setOnFinished(ev -> contenu.getChildren().remove(toast));
            fo.play();
        });
        pause.play();
    }

    // ══════════════════════════════════════════════════════════════════════
    // Paiement avec abonnement pré-sélectionné
    // ══════════════════════════════════════════════════════════════════════
    public void switchPaiementsAvecAbonnement(Abonnement abo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Gestionpaiements.fxml"));
            Parent page = loader.load();
            GestionpaiementsController ctrl = loader.getController();
            ctrl.setMainController(this);
            contenu.getChildren().setAll(page);
            ctrl.ouvrirFormulaireAvecAbonnement(abo);
            setSidebar(null);
            setTab(tabPaiements);
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement Gestionpaiements.fxml");
            e.printStackTrace();
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // 🔔 TOAST IN-APP planifié après X secondes
    // ══════════════════════════════════════════════════════════════════════
    public void planifierToast(Abonnement ab, int secondes) {
        PauseTransition attente = new PauseTransition(Duration.seconds(secondes));
        attente.setOnFinished(e -> Platform.runLater(() -> {
            if (notificationsActives) afficherToastAjout(ab);
        }));
        attente.play();
    }

    private void afficherToastAjout(Abonnement ab) {
        if (contenu == null) return;

        final String couleur = "#1a3a7a";

        Label ico   = new Label("🔔");
        ico.setStyle("-fx-font-size:22px;");

        Label titre = new Label("Rappel : " + ab.getNom());
        titre.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:white;");

        Label msg   = new Label("Votre abonnement expire bientôt !");
        msg.setStyle("-fx-font-size:11px;-fx-text-fill:rgba(255,255,255,0.85);");

        VBox textes = new VBox(3, titre, msg);
        HBox.setHgrow(textes, Priority.ALWAYS);

        Button fermer = new Button("✕");
        fermer.setStyle(
                "-fx-background-color:rgba(255,255,255,0.2);-fx-text-fill:white;" +
                        "-fx-font-size:12px;-fx-background-radius:50;-fx-padding:4 8;" +
                        "-fx-cursor:hand;-fx-border-width:0;"
        );

        Button btnVoir = new Button("👁 Voir");
        btnVoir.setStyle(
                "-fx-background-color:white;-fx-text-fill:" + couleur + ";" +
                        "-fx-font-size:11px;-fx-font-weight:bold;" +
                        "-fx-padding:5 12;-fx-background-radius:20;-fx-cursor:hand;-fx-border-width:0;"
        );

        HBox corps = new HBox(12, ico, textes, fermer);
        corps.setAlignment(Pos.CENTER_LEFT);
        corps.setStyle("-fx-background-color:" + couleur +
                ";-fx-padding:14 16;-fx-background-radius:14 14 0 0;");

        HBox actionsBar = new HBox(8);
        actionsBar.setAlignment(Pos.CENTER_RIGHT);
        actionsBar.setStyle("-fx-background-color:" + couleur + "dd;-fx-padding:6 16;");
        actionsBar.getChildren().add(btnVoir);

        Rectangle barFond  = new Rectangle(0, 4);
        barFond.setFill(Color.web("rgba(255,255,255,0.2)"));
        barFond.setArcWidth(4); barFond.setArcHeight(4);

        Rectangle barTimer = new Rectangle(0, 4);
        barTimer.setFill(Color.web("rgba(255,255,255,0.7)"));
        barTimer.setArcWidth(4); barTimer.setArcHeight(4);

        StackPane barre = new StackPane(barFond, barTimer);
        barre.setAlignment(Pos.CENTER_LEFT);
        barre.setStyle("-fx-background-color:rgba(0,0,0,0.2);-fx-background-radius:0 0 14 14;");

        VBox toast = new VBox(0, corps, actionsBar, barre);
        toast.setMaxWidth(320);
        toast.setStyle("-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.4),20,0,0,8);");
        StackPane.setAlignment(toast, Pos.BOTTOM_RIGHT);
        toast.setTranslateX(-24);
        toast.setTranslateY(-24);
        contenu.getChildren().add(toast);

        barre.widthProperty().addListener((o, v, n) -> {
            barFond.setWidth(n.doubleValue());
            barTimer.setWidth(n.doubleValue());
            new Timeline(
                    new KeyFrame(Duration.ZERO,       new KeyValue(barTimer.widthProperty(), n.doubleValue())),
                    new KeyFrame(Duration.seconds(6), new KeyValue(barTimer.widthProperty(), 0, Interpolator.LINEAR))
            ).play();
        });

        Runnable dismiss = () -> {
            FadeTransition fo = new FadeTransition(Duration.millis(350), toast);
            fo.setFromValue(1); fo.setToValue(0);
            TranslateTransition to = new TranslateTransition(Duration.millis(350), toast);
            to.setByX(360); to.setInterpolator(Interpolator.EASE_IN);
            ParallelTransition exit = new ParallelTransition(fo, to);
            exit.setOnFinished(ev -> contenu.getChildren().remove(toast));
            exit.play();
        };

        fermer.setOnAction(e -> dismiss.run());
        btnVoir.setOnAction(e -> { dismiss.run(); switchCalendar(); });

        PauseTransition autoDismiss = new PauseTransition(Duration.seconds(6));
        autoDismiss.setOnFinished(e -> dismiss.run());
        autoDismiss.play();

        // Animation entrée
        toast.setOpacity(0);
        toast.setTranslateX(360 - 24);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(400), toast);
        fadeIn.setFromValue(0); fadeIn.setToValue(1);
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(400), toast);
        slideIn.setToX(-24); slideIn.setInterpolator(Interpolator.EASE_OUT);
        new ParallelTransition(fadeIn, slideIn).play();
    }

    // ===== LOAD FXML =====
    public void load(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/" + fxml));
            Parent page = loader.load();
            Object ctrl = loader.getController();

            if (ctrl instanceof GestionabonnementsController c) c.setMainController(this);
            if (ctrl instanceof GestionpaiementsController   c) c.setMainController(this);
            if (ctrl instanceof RecommandationController     c) c.setMainController(this);
            if (ctrl instanceof SuggestionsController        c) c.setMainController(this);
            if (ctrl instanceof CalendrierController         c) c.setMainController(this);

            contenu.getChildren().setAll(page);
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement " + fxml);
            e.printStackTrace();
        }
    }

    // ===== HELPERS =====
    private void setSidebar(Button active) {
        if (btnAbonnements != null) btnAbonnements.setStyle(INACTIVE);
        if (btnBudget      != null) btnBudget.setStyle(INACTIVE);
        if (btnDepenses    != null) btnDepenses.setStyle(INACTIVE);
        if (btnEducation   != null) btnEducation.setStyle(INACTIVE);
        if (active         != null) active.setStyle(ACTIVE);
    }

    private void setTab(Button active) {
        if (tabAbonnements     != null) tabAbonnements.setStyle(TAB_OFF);
        if (tabPaiements       != null) tabPaiements.setStyle(TAB_OFF);
        if (tabStats           != null) tabStats.setStyle(TAB_OFF);
        if (tabRecommandations != null) tabRecommandations.setStyle(TAB_OFF);
        if (tabSuggestions     != null) tabSuggestions.setStyle(TAB_OFF);
        if (tabCalendar        != null) tabCalendar.setStyle(TAB_OFF);
        if (active             != null) active.setStyle(TAB_ON);
    }
}