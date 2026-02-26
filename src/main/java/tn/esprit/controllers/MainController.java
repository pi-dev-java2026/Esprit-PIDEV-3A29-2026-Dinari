package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import tn.esprit.entities.Abonnement;

public class MainController {

    @FXML public StackPane contenu;

    // Sidebar — NE PAS TOUCHER (boutons des collègues)
    @FXML private Button btnAbonnements;
    @FXML private Button btnBudget;
    @FXML private Button btnDepenses;
    @FXML private Button btnEducation;

    // Onglets — ton travail
    @FXML private Button tabAbonnements;
    @FXML private Button tabPaiements;
    @FXML private Button tabStats;
    @FXML private Button tabRecommandations;  // ✨ Plan optimal IA
    @FXML private Button tabSuggestions;      // 🎬 Netflix / Spotify

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

    // ===== SIDEBAR (ne pas modifier) =====
    @FXML public void showAbonnements() { load("Gestionabonnements.fxml"); setSidebar(btnAbonnements); setTab(tabAbonnements); }
    @FXML public void showBudget()      { setSidebar(btnBudget); }
    @FXML public void showDepenses()    { setSidebar(btnDepenses); }
    @FXML public void showEducation()   { setSidebar(btnEducation); }

    // ===== ONGLETS =====
    @FXML public void switchAbonnements() { load("Gestionabonnements.fxml"); setSidebar(btnAbonnements); setTab(tabAbonnements); }
    @FXML public void switchPaiements()   { load("Gestionpaiements.fxml");   setSidebar(null);           setTab(tabPaiements); }
    @FXML public void switchStats()       {                                   setSidebar(null);           setTab(tabStats); }

    // ── ✨ Recommandations plan optimal ───────────────────────────────────
    @FXML
    public void switchRecommandations() {
        load("Recommandation.fxml");
        setSidebar(null);
        setTab(tabRecommandations);
    }

    // ── 🎬 Suggestions Netflix / Spotify ──────────────────────────────────
    @FXML
    public void switchSuggestions() {
        load("Suggestions.fxml");
        setSidebar(null);
        setTab(tabSuggestions);
    }

    // ── Paiement avec abonnement pré-sélectionné ──────────────────────────
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
        if (active             != null) active.setStyle(TAB_ON);
    }
}