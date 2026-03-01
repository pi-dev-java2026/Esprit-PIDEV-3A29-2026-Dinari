package tn.esprit.controllers;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import tn.esprit.entities.Abonnement;
import tn.esprit.services.AbonnementService;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class CalendrierController {

    @FXML private Label lblMoisAnnee;
    @FXML private GridPane grilleCal;
    @FXML private VBox listeExpirationsVbox;
    @FXML private Label lblAucunEvent;

    private MainController mainController;
    private final AbonnementService aboService = new AbonnementService();
    private YearMonth moisActuel = YearMonth.now();
    private final Map<LocalDate, List<Abonnement>> expirations = new HashMap<>();

    public void setMainController(MainController mc) {
        this.mainController = mc;
    }

    @FXML
    public void initialize() {
        calculerExpirations();
        afficherMois();
        afficherExpirationsDuMois();

        PauseTransition attente = new PauseTransition(Duration.millis(800));
        attente.setOnFinished(e -> afficherToastsUrgents());
        attente.play();

        Timeline checker = new Timeline(
                new KeyFrame(Duration.minutes(1), e ->
                        Platform.runLater(this::afficherToastsUrgents))
        );
        checker.setCycleCount(Timeline.INDEFINITE);
        checker.play();
    }

    // ══════════════════════════════════════════════════════════════════════
    // TOASTS URGENTS
    // ══════════════════════════════════════════════════════════════════════
    private void afficherToastsUrgents() {
        List<Abonnement> urgents = aboService.afficher().stream()
                .filter(a -> { long j = joursRestants(a); return j >= 0 && j <= 7; })
                .sorted(Comparator.comparingLong(this::joursRestants))
                .limit(3)
                .collect(Collectors.toList());

        for (int i = 0; i < urgents.size(); i++) {
            final Abonnement ab = urgents.get(i);
            PauseTransition pt = new PauseTransition(Duration.millis(i * 600L));
            pt.setOnFinished(e -> afficherToast(ab));
            pt.play();
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // TOAST avec bouton Renouveler
    // ══════════════════════════════════════════════════════════════════════
    private void afficherToast(Abonnement ab) {
        if (mainController == null || mainController.contenu == null) return;

        long   jours   = joursRestants(ab);
        String emoji   = jours == 0 ? "💀" : jours <= 3 ? "🚨" : "⚠️";
        String couleur = jours == 0 ? "#e74c3c" : jours <= 3 ? "#e67e22" : "#f39c12";
        String msgTxt  = jours == 0 ? "Expire AUJOURD'HUI !" : "Expire dans " + jours + " jour(s) !";

        Label ico = new Label(emoji);
        ico.setStyle("-fx-font-size:24px;");

        Label titre = new Label("⏰ " + ab.getNom());
        titre.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:white;");

        Label msg = new Label(msgTxt);
        msg.setStyle("-fx-font-size:11px;-fx-text-fill:rgba(255,255,255,0.88);");

        VBox textes = new VBox(3, titre, msg);
        HBox.setHgrow(textes, Priority.ALWAYS);

        // ── Bouton Renouveler dans le toast ──
        Button btnRenouveler = new Button("🔄 Renouveler");
        btnRenouveler.setStyle(
                "-fx-background-color:white;-fx-text-fill:" + couleur + ";" +
                        "-fx-font-size:11px;-fx-font-weight:bold;" +
                        "-fx-padding:5 12;-fx-background-radius:20;-fx-cursor:hand;-fx-border-width:0;"
        );

        Button fermer = new Button("✕");
        fermer.setStyle(
                "-fx-background-color:rgba(255,255,255,0.2);-fx-text-fill:white;" +
                        "-fx-font-size:12px;-fx-background-radius:50;-fx-padding:4 8;" +
                        "-fx-cursor:hand;-fx-border-width:0;"
        );

        HBox corps = new HBox(12, ico, textes, fermer);
        corps.setAlignment(Pos.CENTER_LEFT);
        corps.setStyle("-fx-background-color:" + couleur +
                ";-fx-padding:14 16;-fx-background-radius:14 14 0 0;");

        HBox actionsBar = new HBox(8);
        actionsBar.setAlignment(Pos.CENTER_RIGHT);
        actionsBar.setStyle("-fx-background-color:" + couleur +
                "dd;-fx-padding:6 16;");
        actionsBar.getChildren().add(btnRenouveler);

        Rectangle barFond = new Rectangle(0, 4);
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
        mainController.contenu.getChildren().add(toast);

        // Barre timer
        barre.widthProperty().addListener((o, v, n) -> {
            barFond.setWidth(n.doubleValue());
            barTimer.setWidth(n.doubleValue());
            Timeline tl = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(barTimer.widthProperty(), n.doubleValue())),
                    new KeyFrame(Duration.seconds(8), new KeyValue(barTimer.widthProperty(), 0, Interpolator.LINEAR))
            );
            tl.play();
        });

        Runnable dismiss = () -> {
            FadeTransition fo = new FadeTransition(Duration.millis(350), toast);
            fo.setFromValue(1); fo.setToValue(0);
            TranslateTransition to = new TranslateTransition(Duration.millis(350), toast);
            to.setByX(360); to.setInterpolator(Interpolator.EASE_IN);
            ParallelTransition exit = new ParallelTransition(fo, to);
            exit.setOnFinished(ev -> mainController.contenu.getChildren().remove(toast));
            exit.play();
        };

        fermer.setOnAction(e -> dismiss.run());

        // ── Renouveler depuis le toast ──
        btnRenouveler.setOnAction(e -> {
            dismiss.run();
            redirigerVersPaiement(ab);
        });

        PauseTransition autoDismiss = new PauseTransition(Duration.seconds(8));
        autoDismiss.setOnFinished(e -> dismiss.run());
        autoDismiss.play();

        // Animation entrée
        toast.setOpacity(0);
        toast.setTranslateX(360);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(400), toast);
        fadeIn.setFromValue(0); fadeIn.setToValue(1);
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(400), toast);
        slideIn.setByX(-360); slideIn.setInterpolator(Interpolator.EASE_OUT);
        new ParallelTransition(fadeIn, slideIn).play();
    }

    // ══════════════════════════════════════════════════════════════════════
    // REDIRECTION VERS PAIEMENT
    // ══════════════════════════════════════════════════════════════════════
    private void redirigerVersPaiement(Abonnement ab) {
        if (mainController != null) {
            mainController.switchPaiementsAvecAbonnement(ab);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // CALCUL EXPIRATIONS
    // ══════════════════════════════════════════════════════════════════════
    private void calculerExpirations() {
        expirations.clear();
        for (Abonnement a : aboService.afficher()) {
            LocalDate fin = calculerDateFin(a);
            expirations.computeIfAbsent(fin, k -> new ArrayList<>()).add(a);
        }
    }

    private LocalDate calculerDateFin(Abonnement a) {
        LocalDate debut = a.getDateDebut().toLocalDate();
        return "Annuel".equalsIgnoreCase(a.getFrequence())
                ? debut.plusYears(1) : debut.plusMonths(1);
    }

    private long joursRestants(Abonnement ab) {
        return ChronoUnit.DAYS.between(LocalDate.now(), calculerDateFin(ab));
    }

    // ══════════════════════════════════════════════════════════════════════
    // GRILLE CALENDRIER
    // ══════════════════════════════════════════════════════════════════════
    private void afficherMois() {
        lblMoisAnnee.setText(
                moisActuel.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH).toUpperCase()
                        + " " + moisActuel.getYear());
        grilleCal.getChildren().clear();

        String[] jours = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"};
        for (int i = 0; i < 7; i++) {
            Label h = new Label(jours[i]);
            h.setMaxWidth(Double.MAX_VALUE);
            h.setAlignment(Pos.CENTER);
            h.setStyle("-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:#64748b;-fx-padding:8 0;");
            grilleCal.add(h, i, 0);
        }

        int col = moisActuel.atDay(1).getDayOfWeek().getValue() - 1;
        int row = 1;
        LocalDate aujourdh = LocalDate.now();

        for (int jour = 1; jour <= moisActuel.lengthOfMonth(); jour++) {
            LocalDate date = moisActuel.atDay(jour);
            List<Abonnement> abosJour = expirations.getOrDefault(date, Collections.emptyList());
            grilleCal.add(creerCellule(jour, date, abosJour, aujourdh), col, row);
            col++;
            if (col == 7) { col = 0; row++; }
        }
    }

    private StackPane creerCellule(int jour, LocalDate date,
                                   List<Abonnement> abos, LocalDate aujourdh) {
        StackPane sp = new StackPane();
        sp.setPrefSize(52, 52);
        VBox contenu = new VBox(2);
        contenu.setAlignment(Pos.TOP_CENTER);

        Label lblJour = new Label(String.valueOf(jour));
        if (date.equals(aujourdh)) {
            Circle c = new Circle(18);
            c.setFill(Color.web("#1a3a7a"));
            lblJour.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:white;");
            sp.getChildren().add(c);
        } else {
            lblJour.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#0f1f3d;");
        }
        contenu.getChildren().add(lblJour);

        if (!abos.isEmpty()) {
            HBox points = new HBox(3);
            points.setAlignment(Pos.CENTER);
            String[] dc = {"#e74c3c", "#e67e22", "#6c5ce7"};
            for (int i = 0; i < Math.min(abos.size(), 3); i++) {
                Circle pt = new Circle(4);
                pt.setFill(Color.web(dc[i]));
                points.getChildren().add(pt);
            }
            contenu.getChildren().add(points);
            String tipText = abos.stream()
                    .map(a -> "⚠️ " + a.getNom() + " (" + a.getFrequence() + ")")
                    .collect(Collectors.joining("\n"));
            Tooltip.install(sp, new Tooltip(tipText));
            sp.setStyle("-fx-background-color:#fff5f5;-fx-background-radius:10;-fx-cursor:hand;");
            sp.setOnMouseClicked(e -> afficherDetailJour(date, abos));
        }

        sp.getChildren().add(contenu);
        return sp;
    }

    // ══════════════════════════════════════════════════════════════════════
    // LISTE EXPIRATIONS avec bouton Renouveler
    // ══════════════════════════════════════════════════════════════════════
    private void afficherExpirationsDuMois() {
        listeExpirationsVbox.getChildren().clear();
        List<Map.Entry<LocalDate, List<Abonnement>>> evts = expirations.entrySet().stream()
                .filter(e -> YearMonth.from(e.getKey()).equals(moisActuel))
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toList());

        lblAucunEvent.setVisible(evts.isEmpty());
        lblAucunEvent.setManaged(evts.isEmpty());

        for (Map.Entry<LocalDate, List<Abonnement>> entry : evts)
            for (Abonnement a : entry.getValue())
                listeExpirationsVbox.getChildren().add(creerLigneExpiration(entry.getKey(), a));
    }

    private HBox creerLigneExpiration(LocalDate date, Abonnement a) {
        HBox h = new HBox(10);
        h.setAlignment(Pos.CENTER_LEFT);
        h.setStyle("-fx-background-color:white;-fx-padding:12 16;-fx-background-radius:12;" +
                "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.05),6,0,0,2);");

        // Badge date
        VBox badge = new VBox(0);
        badge.setAlignment(Pos.CENTER);
        badge.setMinWidth(44);
        badge.setStyle("-fx-background-color:#fff0f0;-fx-background-radius:10;-fx-padding:6 8;");
        Label lj = new Label(String.valueOf(date.getDayOfMonth()));
        lj.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:#e74c3c;");
        Label lm = new Label(date.getMonth().getDisplayName(TextStyle.SHORT, Locale.FRENCH).toUpperCase());
        lm.setStyle("-fx-font-size:9px;-fx-font-weight:bold;-fx-text-fill:#e74c3c;");
        badge.getChildren().addAll(lj, lm);

        // Infos
        VBox infos = new VBox(3);
        HBox.setHgrow(infos, Priority.ALWAYS);
        Label nom = new Label(a.getNom());
        nom.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#0f1f3d;");
        Label det = new Label(a.getFrequence() + "  •  " + String.format("%.2f TND", a.getPrix()));
        det.setStyle("-fx-font-size:11px;-fx-text-fill:#94a3b8;");
        infos.getChildren().addAll(nom, det);

        // Badge jours restants
        long jours = ChronoUnit.DAYS.between(LocalDate.now(), date);
        String statut = jours < 0 ? "Expiré" : jours == 0 ? "Aujourd'hui !" : "Dans " + jours + "j";
        String couleur = jours < 0 ? "#94a3b8" : jours == 0 ? "#e74c3c" : jours <= 7 ? "#e67e22" : "#27ae60";

        Label stat = new Label(statut);
        stat.setStyle("-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:" + couleur +
                ";-fx-background-color:" + couleur + "22;-fx-padding:4 10;-fx-background-radius:20;");

        // ── Bouton Renouveler ──
        Button btnRenouveler = new Button("🔄 Renouveler");
        btnRenouveler.setStyle(
                "-fx-background-color:#1a3a7a;-fx-text-fill:white;" +
                        "-fx-font-size:11px;-fx-font-weight:bold;" +
                        "-fx-padding:7 14;-fx-background-radius:20;-fx-cursor:hand;-fx-border-width:0;"
        );
        btnRenouveler.setOnMouseEntered(e -> btnRenouveler.setStyle(
                "-fx-background-color:#2a4a9a;-fx-text-fill:white;" +
                        "-fx-font-size:11px;-fx-font-weight:bold;" +
                        "-fx-padding:7 14;-fx-background-radius:20;-fx-cursor:hand;-fx-border-width:0;"
        ));
        btnRenouveler.setOnMouseExited(e -> btnRenouveler.setStyle(
                "-fx-background-color:#1a3a7a;-fx-text-fill:white;" +
                        "-fx-font-size:11px;-fx-font-weight:bold;" +
                        "-fx-padding:7 14;-fx-background-radius:20;-fx-cursor:hand;-fx-border-width:0;"
        ));
        btnRenouveler.setOnAction(e -> redirigerVersPaiement(a));

        h.getChildren().addAll(badge, infos, stat, btnRenouveler);
        return h;
    }

    private void afficherDetailJour(LocalDate date, List<Abonnement> abos) {
        listeExpirationsVbox.getChildren().clear();
        lblAucunEvent.setVisible(false);
        lblAucunEvent.setManaged(false);
        Label titre = new Label("📅 " + date.getDayOfMonth() + " " +
                date.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH));
        titre.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:#1a3a7a;-fx-padding:0 0 8 0;");
        listeExpirationsVbox.getChildren().add(titre);
        for (Abonnement a : abos)
            listeExpirationsVbox.getChildren().add(creerLigneExpiration(date, a));
    }

    // ══════════════════════════════════════════════════════════════════════
    // NAVIGATION MOIS
    // ══════════════════════════════════════════════════════════════════════
    @FXML
    private void moisPrecedent() {
        moisActuel = moisActuel.minusMonths(1);
        afficherMois();
        afficherExpirationsDuMois();
    }

    @FXML
    private void moisSuivant() {
        moisActuel = moisActuel.plusMonths(1);
        afficherMois();
        afficherExpirationsDuMois();
    }

    @FXML
    private void revenirAujourdhui() {
        moisActuel = YearMonth.now();
        afficherMois();
        afficherExpirationsDuMois();
    }
}