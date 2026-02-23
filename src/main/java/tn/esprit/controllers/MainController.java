package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import tn.esprit.entities.Abonnement;

public class MainController {

    @FXML public StackPane contenu;

    // Sidebar
    @FXML private Button btnAbonnements;
    @FXML private Button btnBudget;
    @FXML private Button btnDepenses;
    @FXML private Button btnEducation;

    // Onglets
    @FXML private Button tabAbonnements;
    @FXML private Button tabPaiements;
    @FXML private Button tabStats;

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
                    "-fx-font-weight: bold; -fx-padding: 16 25; -fx-cursor: hand; " +
                    "-fx-border-color: #1a3a7a; -fx-border-width: 0 0 3 0; -fx-background-radius: 0;";

    private final String TAB_OFF =
            "-fx-background-color: transparent; -fx-text-fill: #8899aa; -fx-font-size: 13px; " +
                    "-fx-padding: 16 25; -fx-cursor: hand; -fx-border-width: 0; -fx-background-radius: 0;";

    @FXML
    public void initialize() {
        showAbonnements();
    }

    // ===== SIDEBAR =====

    @FXML
    public void showAbonnements() {
        load("Gestionabonnements.fxml");
        setSidebar(btnAbonnements);
        if (tabAbonnements != null) setTab(tabAbonnements);
    }

    @FXML public void showBudget()    { setSidebar(btnBudget); }
    @FXML public void showDepenses()  { setSidebar(btnDepenses); }
    @FXML public void showEducation() { setSidebar(btnEducation); }

    // ===== ONGLETS =====

    @FXML
    public void switchAbonnements() {
        load("Gestionabonnements.fxml");
        setSidebar(btnAbonnements);
        setTab(tabAbonnements);
    }

    @FXML
    public void switchPaiements() {
        load("Gestionpaiements.fxml");
        setSidebar(null);
        setTab(tabPaiements);
    }

    // ══════════════════════════════════════════════════════════════════════
    // NOUVELLE MÉTHODE — Navigation vers paiements avec abonnement pré-sélectionné
    // ══════════════════════════════════════════════════════════════════════
    /**
     * Ouvre la page paiements avec un abonnement pré-sélectionné dans le formulaire.
     * Utilisé quand l'utilisateur clique sur "💳 Payer" depuis la page abonnements.
     * @param abo L'abonnement à pré-remplir dans le formulaire de paiement
     */
    public void switchPaiementsAvecAbonnement(Abonnement abo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Gestionpaiements.fxml"));
            Parent page = loader.load();

            // Récupère le controller de la page paiements
            GestionpaiementsController ctrl = loader.getController();
            ctrl.setMainController(this);

            // Affiche la page
            contenu.getChildren().setAll(page);

            // Ouvre le formulaire pré-rempli avec l'abonnement
            ctrl.ouvrirFormulaireAvecAbonnement(abo);

            // Met à jour les styles visuels
            setSidebar(null);
            setTab(tabPaiements);

        } catch (Exception e) {
            System.err.println("❌ Erreur chargement Gestionpaiements.fxml avec abonnement");
            e.printStackTrace();
        }
    }

    @FXML
    public void switchStats() {
        setSidebar(null);
        setTab(tabStats);
    }

    // ===== LOAD FXML =====

    public void load(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/" + fxml));
            Parent page = loader.load();
            Object ctrl = loader.getController();

            if (ctrl instanceof GestionabonnementsController c)
                c.setMainController(this);
            if (ctrl instanceof GestionpaiementsController c)
                c.setMainController(this);

            contenu.getChildren().setAll(page);
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement " + fxml);
            e.printStackTrace();
        }
    }

    // ===== HELPERS =====

    private void setSidebar(Button active) {
        if (btnAbonnements != null) btnAbonnements.setStyle(INACTIVE);
        if (btnBudget != null) btnBudget.setStyle(INACTIVE);
        if (btnDepenses != null) btnDepenses.setStyle(INACTIVE);
        if (btnEducation != null) btnEducation.setStyle(INACTIVE);
        if (active != null) active.setStyle(ACTIVE);
    }

    private void setTab(Button active) {
        if (tabAbonnements != null) tabAbonnements.setStyle(TAB_OFF);
        if (tabPaiements != null) tabPaiements.setStyle(TAB_OFF);
        if (tabStats != null) tabStats.setStyle(TAB_OFF);
        if (active != null) active.setStyle(TAB_ON);
    }
}