package tn.esprit.controllers;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import tn.esprit.services.GeminiService;

public class SuggestionsController {

    @FXML private VBox pageChoix, pageNetflix, pageSpotify, pageResultat;
    @FXML private TextField txtAge, txtDejaVu, txtArtistes;
    @FXML private CheckBox cbAction, cbHorreur, cbRomance, cbSciFi;
    @FXML private CheckBox cbDoc, cbAnimation, cbComedy, cbThriller;
    @FXML private CheckBox cbFilms, cbSeries;
    @FXML private CheckBox cbPop, cbRap, cbJazz, cbElec;
    @FXML private CheckBox cbRock, cbRnb, cbClassique, cbArabic;
    @FXML private ToggleGroup tgDuree, tgHumeur, tgMoment, tgLang;
    @FXML private VBox conteneurResultat;
    @FXML private ProgressIndicator spinner;
    @FXML private Label lblTitre, lblStatut;
    @FXML private Button btnNouveauN, btnNouveauS;

    private MainController mainController;
    private String modeActuel = "";

    public void setMainController(MainController mc) { this.mainController = mc; }

    @FXML public void initialize() { afficherPage(pageChoix); }

    // ── Navigation ────────────────────────────────────────────────────────
    @FXML public void ouvrirNetflix()  { modeActuel = "netflix"; afficherPage(pageNetflix); }
    @FXML public void ouvrirSpotify() { modeActuel = "spotify"; afficherPage(pageSpotify); }
    @FXML public void retourChoix()   { afficherPage(pageChoix); }
    @FXML public void retourListe()   { if (mainController != null) mainController.switchAbonnements(); }
    @FXML public void nouveauNetflix() { conteneurResultat.getChildren().clear(); afficherPage(pageNetflix); }
    @FXML public void nouveauSpotify() { conteneurResultat.getChildren().clear(); afficherPage(pageSpotify); }

    private void afficherPage(VBox page) {
        for (VBox v : new VBox[]{pageChoix, pageNetflix, pageSpotify, pageResultat}) {
            if (v != null) { v.setVisible(false); v.setManaged(false); }
        }
        page.setVisible(true); page.setManaged(true);
        FadeTransition ft = new FadeTransition(Duration.millis(220), page);
        ft.setFromValue(0); ft.setToValue(1); ft.play();
    }

    // ── Analyser Netflix ──────────────────────────────────────────────────
    @FXML
    public void analyserNetflix() {
        String age    = txtAge    != null && !txtAge.getText().isBlank()    ? txtAge.getText()    : "25";
        String dejaVu = txtDejaVu != null && !txtDejaVu.getText().isBlank() ? txtDejaVu.getText() : "aucun";
        String genres = getGenresNetflix();
        String type   = getTypeNetflix();
        String duree  = getToggleTexte(tgDuree, "2 heures");
        String humeur = getToggleTexte(tgHumeur, "Detente");

        String prompt =
                "Tu es expert cinema. Reponds en francais sans markdown et sans asterisques.\n\n" +
                        "Profil : age=" + age + ", genres=" + genres + ", type=" + type +
                        ", duree=" + duree + ", humeur=" + humeur + ", aimes=" + dejaVu + "\n\n" +
                        "Propose 8 titres REELS disponibles sur Netflix.\n" +
                        "Format STRICT (respecte exactement) :\n" +
                        "INTRO: [phrase d'intro personnalisee]\n" +
                        "1. [Titre] — [Pourquoi en 1 phrase]\n" +
                        "2. [Titre] — [Pourquoi]\n" +
                        "3. [Titre] — [Pourquoi]\n" +
                        "4. [Titre] — [Pourquoi]\n" +
                        "5. [Titre] — [Pourquoi]\n" +
                        "6. [Titre] — [Pourquoi]\n" +
                        "7. [Titre] — [Pourquoi]\n" +
                        "8. [Titre] — [Pourquoi]\n" +
                        "CONSEIL: [conseil pratique]";

        lancerAnalyse("Netflix", "#E50914", prompt);
    }

    // ── Analyser Spotify ──────────────────────────────────────────────────
    @FXML
    public void analyserSpotify() {
        String genres   = getGenresSpotify();
        String moment   = getToggleTexte(tgMoment, "Sport");
        String langue   = getToggleTexte(tgLang, "Anglais");
        String artistes = txtArtistes != null && !txtArtistes.getText().isBlank() ? txtArtistes.getText() : "aucun";

        String prompt =
                "Tu es expert musical. Reponds en francais sans markdown et sans asterisques.\n\n" +
                        "Profil : genres=" + genres + ", moment=" + moment + ", langue=" + langue + ", artistes=" + artistes + "\n\n" +
                        "Propose 8 recommandations REELLES disponibles sur Spotify.\n" +
                        "Format STRICT :\n" +
                        "INTRO: [phrase d'intro personnalisee]\n" +
                        "1. [Artiste ou Album] — [Pourquoi en 1 phrase]\n" +
                        "2. [Artiste ou Album] — [Pourquoi]\n" +
                        "3. [Artiste ou Album] — [Pourquoi]\n" +
                        "4. [Artiste ou Album] — [Pourquoi]\n" +
                        "5. [Artiste ou Album] — [Pourquoi]\n" +
                        "6. [Artiste ou Album] — [Pourquoi]\n" +
                        "7. [Artiste ou Album] — [Pourquoi]\n" +
                        "8. [Artiste ou Album] — [Pourquoi]\n" +
                        "CONSEIL: [conseil pratique]";

        lancerAnalyse("Spotify", "#1DB954", prompt);
    }

    // ── Appel IA ──────────────────────────────────────────────────────────
    private void lancerAnalyse(String service, String couleur, String prompt) {
        afficherPage(pageResultat);
        conteneurResultat.getChildren().clear();
        if (lblTitre  != null) lblTitre.setText("Suggestions " + service + " par l'IA");
        if (lblStatut != null) {
            lblStatut.setText("L'IA analyse vos préférences...");
            lblStatut.setStyle("-fx-font-size:12px;-fx-text-fill:" + couleur + ";-fx-font-weight:bold;");
        }
        if (spinner != null) { spinner.setVisible(true); spinner.setManaged(true); }
        setVisible(btnNouveauN, false);
        setVisible(btnNouveauS, false);

        new Thread(() -> {
            String reponse = GeminiService.envoyerPrompt(prompt);
            javafx.application.Platform.runLater(() -> {
                if (spinner   != null) { spinner.setVisible(false); spinner.setManaged(false); }
                if (lblStatut != null) {
                    lblStatut.setText("✅ " + conteneurResultat.getChildren().size() + " suggestions générées !");
                    lblStatut.setStyle("-fx-font-size:12px;-fx-text-fill:#27ae60;-fx-font-weight:bold;");
                }
                afficherResultats(reponse, couleur, service);
                if ("netflix".equals(modeActuel)) setVisible(btnNouveauN, true);
                else                              setVisible(btnNouveauS, true);
            });
        }).start();
    }

    // ── Afficher résultats style Netflix sombre ───────────────────────────
    private void afficherResultats(String reponse, String couleur, String service) {
        int rang = 0;
        boolean estNetflix = "Netflix".equals(service);

        for (String ligne : reponse.split("\n")) {
            String l = ligne.trim();
            if (l.isEmpty()) continue;

            if (l.startsWith("INTRO:") || l.startsWith("CONSEIL:")) {
                // Texte intro / conseil — style discret
                Label label = new Label(l.replaceFirst("^[A-Z]+:\\s*", ""));
                label.setWrapText(true);
                String bg    = l.startsWith("INTRO:") ? "#111" : "#0d1a0d";
                String color = l.startsWith("INTRO:") ? "#999" : "#1DB954";
                label.setStyle("-fx-font-size:12px;-fx-text-fill:" + color +
                        ";-fx-font-style:italic;-fx-background-color:" + bg +
                        ";-fx-padding:12 16;-fx-background-radius:10;");
                label.setMaxWidth(Double.MAX_VALUE);
                conteneurResultat.getChildren().add(label);

            } else if (l.matches("^\\d+[.)].+")) {
                rang++;
                String corps   = l.replaceFirst("^\\d+[.)]\\s*", "");
                String[] parts = corps.split("—|:", 2);
                String titre   = parts[0].trim();
                String raison  = parts.length > 1 ? parts[1].trim() : "";

                HBox carte = creerCarteNetflix(rang, titre, raison, couleur, estNetflix);
                carte.setOpacity(0);
                conteneurResultat.getChildren().add(carte);

                final HBox carteFinal = carte;
                final int  fi         = rang;
                PauseTransition pause = new PauseTransition(Duration.millis(fi * 120));
                pause.setOnFinished(ev -> {
                    FadeTransition ft = new FadeTransition(Duration.millis(280), carteFinal);
                    ft.setFromValue(0); ft.setToValue(1);
                    TranslateTransition tt = new TranslateTransition(Duration.millis(280), carteFinal);
                    tt.setFromX(-20); tt.setToX(0); tt.setInterpolator(Interpolator.EASE_OUT);
                    new ParallelTransition(ft, tt).play();
                });
                pause.play();
            }
        }

        if (rang == 0) {
            // Fallback si format pas respecté
            Label label = new Label(reponse);
            label.setWrapText(true);
            label.setStyle("-fx-font-size:13px;-fx-text-fill:#ccc;-fx-background-color:#111;" +
                    "-fx-padding:20;-fx-background-radius:12;");
            conteneurResultat.getChildren().add(label);
        }
    }

    // ── Carte style sombre Netflix / Spotify ──────────────────────────────
    private HBox creerCarteNetflix(int rang, String titre, String raison, String couleur, boolean estNetflix) {

        // Badge numéro
        StackPane badgePane = new StackPane();
        badgePane.setPrefSize(44, 44); badgePane.setMinSize(44, 44); badgePane.setMaxSize(44, 44);
        badgePane.setStyle("-fx-background-color:" + couleur + ";-fx-background-radius:10;");
        Label numLabel = new Label(String.valueOf(rang));
        numLabel.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:white;");
        badgePane.getChildren().add(numLabel);

        // Emoji icône selon service
        String[] emojisN = {"🎬","🎥","📽️","🎞️","🍿","🎭","🎪","🌟"};
        String[] emojisS = {"🎵","🎸","🎹","🎺","🥁","🎤","🎧","🎼"};
        String[] emojis  = estNetflix ? emojisN : emojisS;
        Label emojiLabel = new Label(emojis[(rang - 1) % emojis.length]);
        emojiLabel.setStyle("-fx-font-size:28px;");

        // Infos titre + raison
        VBox infos = new VBox(6); HBox.setHgrow(infos, Priority.ALWAYS);
        Label titreLabel = new Label(titre);
        titreLabel.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:white;");
        infos.getChildren().add(titreLabel);
        if (!raison.isEmpty()) {
            Label raisonLabel = new Label(raison);
            raisonLabel.setWrapText(true);
            raisonLabel.setStyle("-fx-font-size:12px;-fx-text-fill:#888;");
            infos.getChildren().add(raisonLabel);
        }

        // Badge service (N ou ♫)
        Label serviceBadge = new Label(estNetflix ? "N" : "♫");
        serviceBadge.setStyle("-fx-background-color:" + couleur + ";" +
                "-fx-text-fill:white;-fx-font-size:11px;-fx-font-weight:bold;" +
                "-fx-font-style:italic;-fx-padding:4 8;-fx-background-radius:5;");

        HBox carte = new HBox(14, badgePane, emojiLabel, infos, serviceBadge);
        carte.setAlignment(Pos.CENTER_LEFT);
        carte.setStyle(
                "-fx-background-color:#111111;" +
                        "-fx-padding:16 20;" +
                        "-fx-background-radius:14;" +
                        "-fx-border-color:" + couleur + "22;" +
                        "-fx-border-width:1;" +
                        "-fx-border-radius:14;" +
                        "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.4),12,0,0,4);"
        );

        // Hover effect
        carte.setOnMouseEntered(e -> carte.setStyle(
                "-fx-background-color:#1a1a1a;" +
                        "-fx-padding:16 20;-fx-background-radius:14;" +
                        "-fx-border-color:" + couleur + ";" +
                        "-fx-border-width:1.5;-fx-border-radius:14;" +
                        "-fx-effect:dropshadow(gaussian," + couleur + "44,16,0,0,4);" +
                        "-fx-cursor:hand;"
        ));
        carte.setOnMouseExited(e -> carte.setStyle(
                "-fx-background-color:#111111;" +
                        "-fx-padding:16 20;-fx-background-radius:14;" +
                        "-fx-border-color:" + couleur + "22;" +
                        "-fx-border-width:1;-fx-border-radius:14;" +
                        "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.4),12,0,0,4);"
        ));

        return carte;
    }

    // ── Helpers ───────────────────────────────────────────────────────────
    private String getGenresNetflix() {
        StringBuilder sb = new StringBuilder();
        if (cbAction != null && cbAction.isSelected())       sb.append("Action, ");
        if (cbHorreur != null && cbHorreur.isSelected())     sb.append("Horreur, ");
        if (cbRomance != null && cbRomance.isSelected())     sb.append("Romance, ");
        if (cbSciFi != null && cbSciFi.isSelected())         sb.append("Science-Fiction, ");
        if (cbDoc != null && cbDoc.isSelected())             sb.append("Documentaire, ");
        if (cbAnimation != null && cbAnimation.isSelected()) sb.append("Animation, ");
        if (cbComedy != null && cbComedy.isSelected())       sb.append("Comedie, ");
        if (cbThriller != null && cbThriller.isSelected())   sb.append("Thriller, ");
        return sb.length() > 0 ? sb.toString() : "tous genres";
    }

    private String getTypeNetflix() {
        StringBuilder sb = new StringBuilder();
        if (cbFilms  != null && cbFilms.isSelected())  sb.append("Films ");
        if (cbSeries != null && cbSeries.isSelected()) sb.append("Series ");
        return sb.length() > 0 ? sb.toString() : "Films et Series";
    }

    private String getGenresSpotify() {
        StringBuilder sb = new StringBuilder();
        if (cbPop       != null && cbPop.isSelected())       sb.append("Pop, ");
        if (cbRap       != null && cbRap.isSelected())       sb.append("Rap, ");
        if (cbJazz      != null && cbJazz.isSelected())      sb.append("Jazz, ");
        if (cbElec      != null && cbElec.isSelected())      sb.append("Electronic, ");
        if (cbRock      != null && cbRock.isSelected())      sb.append("Rock, ");
        if (cbRnb       != null && cbRnb.isSelected())       sb.append("R&B, ");
        if (cbClassique != null && cbClassique.isSelected()) sb.append("Classique, ");
        if (cbArabic    != null && cbArabic.isSelected())    sb.append("Musique Arabe, ");
        return sb.length() > 0 ? sb.toString() : "tous genres";
    }

    private String getToggleTexte(ToggleGroup tg, String defaut) {
        if (tg == null || tg.getSelectedToggle() == null) return defaut;
        Toggle s = tg.getSelectedToggle();
        if (s instanceof RadioButton rb) return rb.getText();
        return defaut;
    }

    private void setVisible(javafx.scene.Node n, boolean v) {
        if (n != null) { n.setVisible(v); n.setManaged(v); }
    }
}