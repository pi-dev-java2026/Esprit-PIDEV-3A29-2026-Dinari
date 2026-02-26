package tn.esprit.controllers;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import tn.esprit.entities.Abonnement;
import tn.esprit.services.AbonnementService;
import tn.esprit.services.GoogleCalendarService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CalendarController {

    @FXML private VBox conteneurRappels;
    @FXML private Label lblTitre;
    @FXML private Label lblSousTitre;
    @FXML private HBox statsRow;

    private MainController mainController;
    private final AbonnementService aboService = new AbonnementService();
    private final GoogleCalendarService calendarService = new GoogleCalendarService();

    private static final String[] COLORS = {
            "#e74c3c", "#6c5ce7", "#1a3a7a", "#27ae60", "#e67e22", "#3498db"
    };

    public void setMainController(MainController mc) {
        this.mainController = mc;
    }

    @FXML
    public void initialize() {
        chargerRappels();
    }

    public void chargerRappels() {
        conteneurRappels.getChildren().clear();

        List<Abonnement> abos = aboService.afficher();

        if (abos.isEmpty()) {
            afficherVide();
            return;
        }

        // Stats en haut
        long actifs = abos.stream().filter(Abonnement::isActif).count();
        long expirantBientot = abos.stream()
                .filter(a -> {
                    LocalDate dateFin = calculerDateFin(a);
                    long jours = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), dateFin);
                    return jours <= 7 && jours >= 0;
                }).count();

        buildStats(abos.size(), actifs, expirantBientot);

        // Trier par date d'expiration la plus proche
        abos.sort((a, b) -> calculerDateFin(a).compareTo(calculerDateFin(b)));

        // Afficher chaque abonnement
        for (int i = 0; i < abos.size(); i++) {
            VBox card = buildRappelCard(abos.get(i), i);
            card.setOpacity(0);
            card.setTranslateY(20);
            conteneurRappels.getChildren().add(card);

            final VBox fc = card;
            final int fi = i;
            PauseTransition pt = new PauseTransition(Duration.millis(100 + fi * 80));
            pt.setOnFinished(e -> {
                FadeTransition fade = new FadeTransition(Duration.millis(350), fc);
                fade.setFromValue(0); fade.setToValue(1); fade.play();
                TranslateTransition move = new TranslateTransition(Duration.millis(350), fc);
                move.setFromY(20); move.setToY(0);
                move.setInterpolator(Interpolator.EASE_OUT); move.play();
            });
            pt.play();
        }
    }

    private VBox buildRappelCard(Abonnement ab, int index) {
        LocalDate dateFin = calculerDateFin(ab);
        long joursRestants = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), dateFin);
        String couleur = COLORS[index % COLORS.length];
        String statutLabel = getStatutLabel(joursRestants);
        String statutColor = getStatutColor(joursRestants);

        // ── Cercle coloré avec initiales ──
        Circle cercle = new Circle(26);
        cercle.setFill(Color.web(couleur));
        Label initiales = new Label(ab.getNom().substring(0, Math.min(2, ab.getNom().length())).toUpperCase());
        initiales.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:white;");
        StackPane avatar = new StackPane(cercle, initiales);

        // ── Infos abonnement ──
        Label nomLabel = new Label(ab.getNom());
        nomLabel.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:#0f1f3d;");

        Label categorieLabel = new Label(
                (ab.getCategorie() != null ? ab.getCategorie() : "Abonnement") +
                        " • " + (ab.getFrequence() != null ? ab.getFrequence() : "Mensuel")
        );
        categorieLabel.setStyle("-fx-font-size:11px;-fx-text-fill:#94a3b8;");

        VBox infos = new VBox(3, nomLabel, categorieLabel);
        HBox.setHgrow(infos, Priority.ALWAYS);

        // ── Date d'expiration ──
        Label dateLabel = new Label(dateFin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        dateLabel.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#334155;");

        Label joursLabel = new Label(joursRestants + " jours");
        joursLabel.setStyle("-fx-font-size:11px;-fx-text-fill:" + statutColor + ";-fx-font-weight:bold;");

        VBox dateBox = new VBox(3, dateLabel, joursLabel);
        dateBox.setAlignment(Pos.CENTER_RIGHT);

        // ── Badge statut ──
        Label badge = new Label(statutLabel);
        badge.setStyle(
                "-fx-background-color:" + statutColor + "22;" +
                        "-fx-text-fill:" + statutColor + ";" +
                        "-fx-font-size:11px;-fx-font-weight:bold;" +
                        "-fx-padding:5 12;-fx-background-radius:20;"
        );

        // ── Barre de progression ──
        double maxJours = 30.0;
        double ratio = Math.max(0, Math.min(1, joursRestants / maxJours));
        Rectangle barFond = new Rectangle(0, 4);
        barFond.setFill(Color.web("#f1f5f9"));
        barFond.setArcWidth(4); barFond.setArcHeight(4);
        Rectangle barRemplie = new Rectangle(0, 4);
        barRemplie.setFill(Color.web(statutColor));
        barRemplie.setArcWidth(4); barRemplie.setArcHeight(4);
        StackPane barre = new StackPane(barFond, barRemplie);
        barre.setAlignment(Pos.CENTER_LEFT);
        barre.widthProperty().addListener((o, v, n) -> {
            barFond.setWidth(n.doubleValue());
            new Timeline(new KeyFrame(Duration.millis(600),
                    new KeyValue(barRemplie.widthProperty(), n.doubleValue() * ratio, Interpolator.EASE_OUT)
            )).play();
        });

        // ── Ligne principale ──
        HBox ligne = new HBox(14, avatar, infos, dateBox, badge);
        ligne.setAlignment(Pos.CENTER_LEFT);

        // ── Card complète ──
        VBox card = new VBox(10, ligne, barre);
        card.setStyle(
                "-fx-background-color:white;" +
                        "-fx-padding:18 22;" +
                        "-fx-background-radius:16;" +
                        "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.07),12,0,0,3);" +
                        "-fx-border-color:" + couleur + ";" +
                        "-fx-border-width:0 0 0 4;" +
                        "-fx-border-radius:0 16 16 0;"
        );

        // Hover effect
        card.setOnMouseEntered(e -> {
            card.setStyle(card.getStyle().replace(
                    "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.07),12,0,0,3);",
                    "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.15),18,0,0,6);"
            ));
            ScaleTransition sc = new ScaleTransition(Duration.millis(150), card);
            sc.setToX(1.01); sc.setToY(1.01); sc.play();
        });
        card.setOnMouseExited(e -> {
            card.setStyle(card.getStyle().replace(
                    "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.15),18,0,0,6);",
                    "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.07),12,0,0,3);"
            ));
            ScaleTransition sc = new ScaleTransition(Duration.millis(150), card);
            sc.setToX(1.0); sc.setToY(1.0); sc.play();
        });

        return card;
    }

    private void buildStats(int total, long actifs, long expirant) {
        if (statsRow == null) return;
        statsRow.getChildren().setAll(
                statCard("📅", String.valueOf(total), "Total rappels", "#1a3a7a", "#e8f0ff"),
                statCard("✅", String.valueOf(actifs), "Actifs", "#27ae60", "#e8fff4"),
                statCard("⚠️", String.valueOf(expirant), "Expirent bientôt", "#e67e22", "#fff8ee")
        );
        statsRow.setVisible(true);
        statsRow.setManaged(true);
    }

    private HBox statCard(String icon, String valeur, String label, String color, String bg) {
        Label ico = new Label(icon);
        ico.setStyle("-fx-font-size:20px;");
        Label val = new Label(valeur);
        val.setStyle("-fx-font-size:22px;-fx-font-weight:bold;-fx-text-fill:" + color + ";");
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size:10px;-fx-text-fill:#94a3b8;-fx-font-weight:bold;");
        VBox txt = new VBox(2, val, lbl);
        HBox box = new HBox(10, ico, txt);
        box.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(box, Priority.ALWAYS);
        box.setStyle(
                "-fx-background-color:" + bg + ";" +
                        "-fx-padding:16 20;-fx-background-radius:14;"
        );
        return box;
    }

    private void afficherVide() {
        Label msg = new Label("📭  Aucun abonnement trouvé\nAjoutez un abonnement pour voir vos rappels ici !");
        msg.setWrapText(true);
        msg.setAlignment(Pos.CENTER);
        msg.setStyle(
                "-fx-font-size:14px;-fx-text-fill:#94a3b8;" +
                        "-fx-padding:60;-fx-alignment:center;"
        );
        conteneurRappels.getChildren().add(msg);
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private LocalDate calculerDateFin(Abonnement ab) {
        LocalDate debut = ab.getDateDebut().toLocalDate();
        if ("Annuel".equalsIgnoreCase(ab.getFrequence())) return debut.plusYears(1);
        return debut.plusMonths(1);
    }

    private String getStatutLabel(long jours) {
        if (jours < 0)  return "Expiré";
        if (jours == 0) return "Aujourd'hui !";
        if (jours <= 3) return "Urgent";
        if (jours <= 7) return "Bientôt";
        return "OK";
    }

    private String getStatutColor(long jours) {
        if (jours < 0)  return "#95a5a6";
        if (jours <= 3) return "#e74c3c";
        if (jours <= 7) return "#e67e22";
        return "#27ae60";
    }

    @FXML
    private void retour() {
        if (mainController != null) mainController.switchAbonnements();
    }
}