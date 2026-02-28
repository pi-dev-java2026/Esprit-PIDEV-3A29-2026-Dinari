package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import tn.esprit.entities.Abonnement;
import tn.esprit.entities.Paiement;
import tn.esprit.services.AbonnementService;
import tn.esprit.services.PaiementService;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class GestionpaiementsController {

    @FXML private VBox pagePrincipale;
    @FXML private VBox pageFormulaire;
    @FXML private Label lblNbPayes, lblNbAVenir, lblTotalPaye;
    @FXML private TextField txtRecherche;
    @FXML private ComboBox<String> comboStatut, comboTri;
    @FXML private VBox listePaiements;
    @FXML private Label lblTitreForm;
    @FXML private ComboBox<String> comboService;
    @FXML private TextField txtMontant, txtNomTitulaire, txtPrenomTitulaire;
    @FXML private TextField txtNumeroCarte, txtDateExpiration, txtCvv;
    @FXML private DatePicker datePickerPaie;
    @FXML private Button btnStatutPaye, btnStatutAttente;
    @FXML private Label errService, errMontant, errNom, errPrenom;
    @FXML private Label errCarte, errExpiration, errCvv, errDate;

    private MainController mainController;
    private final PaiementService   paieService = new PaiementService();
    private final AbonnementService aboService  = new AbonnementService();
    private List<Paiement> tous;
    private String statutSelectionne = "A venir";
    private Paiement enEdition = null;

    private static final String CHAMP_OK     = "-fx-background-radius:9;-fx-border-radius:9;" +
            "-fx-border-color:#e0e8f0;-fx-border-width:1.5;-fx-padding:11 14;" +
            "-fx-font-size:13px;-fx-background-color:#fafbfc;";
    private static final String CHAMP_ERREUR = "-fx-background-radius:9;-fx-border-radius:9;" +
            "-fx-border-color:#e74c3c;-fx-border-width:2;-fx-padding:11 14;" +
            "-fx-font-size:13px;-fx-background-color:#fff5f5;";

    public void setMainController(MainController mc) { this.mainController = mc; }

    @FXML
    public void initialize() {
        if (comboStatut != null)
            comboStatut.setItems(FXCollections.observableArrayList("Tous les statuts", "Paye", "A venir"));
        if (comboTri != null)
            comboTri.setItems(FXCollections.observableArrayList("Date croissant", "Date decroissant", "Montant croissant", "Montant decroissant"));
        charger();
    }

    private void showPage(VBox page) {
        if (pagePrincipale != null) { pagePrincipale.setVisible(false); pagePrincipale.setManaged(false); }
        if (pageFormulaire != null) { pageFormulaire.setVisible(false);  pageFormulaire.setManaged(false); }
        page.setVisible(true); page.setManaged(true);
    }

    private void charger() {
        tous = paieService.afficher();
        majStats(tous);
        afficher(tous);
    }

    private void majStats(List<Paiement> l) {
        long nbPaye   = l.stream().filter(p -> "Paye".equals(p.getStatut())).count();
        long nbAvenir = l.stream().filter(p -> "A venir".equals(p.getStatut())).count();
        double total  = l.stream().filter(p -> "Paye".equals(p.getStatut()))
                .mapToDouble(Paiement::getMontant).sum();
        if (lblNbPayes   != null) lblNbPayes.setText(String.valueOf(nbPaye));
        if (lblNbAVenir  != null) lblNbAVenir.setText(String.valueOf(nbAvenir));
        if (lblTotalPaye != null) lblTotalPaye.setText(String.format("%.3f TND", total));
    }

    private void afficher(List<Paiement> l) {
        if (listePaiements == null) return;
        listePaiements.getChildren().clear();
        l.forEach(p -> listePaiements.getChildren().add(creerLigne(p)));
    }

    @FXML
    private void appliquerFiltres() {
        String r   = txtRecherche != null ? txtRecherche.getText().toLowerCase().trim() : "";
        String st  = comboStatut != null ? comboStatut.getValue() : null;
        String tri = comboTri    != null ? comboTri.getValue()    : null;

        List<Paiement> f = tous.stream()
                .filter(p -> r.isEmpty()
                        || getNomService(p.getAbonnementId()).toLowerCase().contains(r)
                        || p.getDatePaiement().toString().contains(r)
                        || p.getStatut().toLowerCase().contains(r))
                .filter(p -> st == null || "Tous les statuts".equals(st) || p.getStatut().equals(st))
                .collect(Collectors.toList());

        if (tri != null) switch (tri) {
            case "Date croissant"      -> f.sort((a, b) -> a.getDatePaiement().compareTo(b.getDatePaiement()));
            case "Date decroissant"    -> f.sort((a, b) -> b.getDatePaiement().compareTo(a.getDatePaiement()));
            case "Montant croissant"   -> f.sort((a, b) -> Double.compare(a.getMontant(), b.getMontant()));
            case "Montant decroissant" -> f.sort((a, b) -> Double.compare(b.getMontant(), a.getMontant()));
        }
        majStats(f); afficher(f);
    }

    @FXML private void rafraichir() {
        if (txtRecherche != null) txtRecherche.clear();
        if (comboStatut  != null) comboStatut.setValue(null);
        if (comboTri     != null) comboTri.setValue(null);
        charger();
    }

    @FXML
    private void ouvrirFormulaire() {
        enEdition = null;
        if (lblTitreForm != null) lblTitreForm.setText("Modifier le Paiement");
        viderForm();
        reinitialiserErreurs();
        chargerServices();
        statutSelectionne = "A venir";
        majToggle();
        showPage(pageFormulaire);
    }

    private void ouvrirModifier(Paiement p) {
        enEdition = p;
        if (lblTitreForm != null) lblTitreForm.setText("Modifier le Paiement");
        chargerServices();
        reinitialiserErreurs();
        if (comboService != null) comboService.setValue(getNomService(p.getAbonnementId()));
        if (txtMontant         != null) txtMontant.setText(String.valueOf(p.getMontant()));
        if (txtNomTitulaire    != null) txtNomTitulaire.setText(safe(p.getNomTitulaire(), ""));
        if (txtPrenomTitulaire != null) txtPrenomTitulaire.setText(safe(p.getPrenomTitulaire(), ""));
        if (txtNumeroCarte     != null) txtNumeroCarte.setText(safe(p.getNumeroCarte(), ""));
        if (txtDateExpiration  != null) txtDateExpiration.setText(safe(p.getDateExpiration(), ""));
        if (txtCvv             != null) txtCvv.setText(safe(p.getCvv(), ""));
        if (datePickerPaie     != null) datePickerPaie.setValue(p.getDatePaiement().toLocalDate());
        statutSelectionne = p.getStatut();
        majToggle();
        showPage(pageFormulaire);
    }

    @FXML private void fermerFormulaire() {
        enEdition = null;
        showPage(pagePrincipale);
    }

    @FXML private void setStatutPaye()    { statutSelectionne = "Paye";    majToggle(); }
    @FXML private void setStatutAttente() { statutSelectionne = "A venir"; majToggle(); }

    private void majToggle() {
        boolean p = "Paye".equals(statutSelectionne);
        if (btnStatutPaye != null)
            btnStatutPaye.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-padding:11 30;" +
                    "-fx-background-radius:9 0 0 9;-fx-cursor:hand;-fx-border-radius:9 0 0 9;-fx-border-width:1.5;" +
                    (p ? "-fx-background-color:#e8fff4;-fx-text-fill:#27ae60;-fx-border-color:#27ae60;"
                            : "-fx-background-color:#f5f5f5;-fx-text-fill:#8899aa;-fx-border-color:#dde3ea;"));
        if (btnStatutAttente != null)
            btnStatutAttente.setStyle("-fx-font-size:13px;-fx-padding:11 30;" +
                    "-fx-background-radius:0 9 9 0;-fx-cursor:hand;-fx-border-radius:0 9 9 0;-fx-border-width:1.5;" +
                    (!p ? "-fx-background-color:#fff8e8;-fx-text-fill:#e67e22;-fx-border-color:#e67e22;"
                            : "-fx-background-color:#f5f5f5;-fx-text-fill:#8899aa;-fx-border-color:#dde3ea;"));
    }

    private boolean validerFormulaire() {
        boolean ok = true;
        if (comboService != null) ok &= validerCombo(comboService, errService);
        ok &= validerTextField(txtMontant, errMontant);
        ok &= validerTextField(txtNomTitulaire, errNom);
        ok &= validerTextField(txtPrenomTitulaire, errPrenom);
        ok &= validerTextField(txtNumeroCarte, errCarte);
        ok &= validerTextField(txtDateExpiration, errExpiration);
        ok &= validerTextField(txtCvv, errCvv);
        if (datePickerPaie == null || datePickerPaie.getValue() == null) {
            if (errDate != null) { errDate.setVisible(true); errDate.setManaged(true); }
            ok = false;
        } else {
            if (errDate != null) { errDate.setVisible(false); errDate.setManaged(false); }
        }
        return ok;
    }

    private boolean validerTextField(TextField champ, Label errLabel) {
        if (champ == null) return true;
        if (champ.getText() == null || champ.getText().isBlank()) {
            champ.setStyle(CHAMP_ERREUR);
            if (errLabel != null) { errLabel.setVisible(true); errLabel.setManaged(true); }
            return false;
        }
        champ.setStyle(CHAMP_OK);
        if (errLabel != null) { errLabel.setVisible(false); errLabel.setManaged(false); }
        return true;
    }

    private boolean validerCombo(ComboBox<String> combo, Label errLabel) {
        if (combo == null) return true;
        if (combo.getValue() == null) {
            combo.setStyle("-fx-background-radius:9;-fx-border-radius:9;" +
                    "-fx-border-color:#e74c3c;-fx-border-width:2;-fx-font-size:13px;");
            if (errLabel != null) { errLabel.setVisible(true); errLabel.setManaged(true); }
            return false;
        }
        combo.setStyle("-fx-background-radius:9;-fx-font-size:13px;" +
                "-fx-border-color:#e0e8f0;-fx-border-width:1.5;-fx-border-radius:9;");
        if (errLabel != null) { errLabel.setVisible(false); errLabel.setManaged(false); }
        return true;
    }

    private void reinitialiserErreurs() {
        for (TextField tf : new TextField[]{txtMontant, txtNomTitulaire, txtPrenomTitulaire,
                txtNumeroCarte, txtDateExpiration, txtCvv}) {
            if (tf != null) tf.setStyle(CHAMP_OK);
        }
        if (comboService != null)
            comboService.setStyle("-fx-background-radius:9;-fx-font-size:13px;" +
                    "-fx-border-color:#e0e8f0;-fx-border-width:1.5;-fx-border-radius:9;");
        for (Label l : new Label[]{errService, errMontant, errNom, errPrenom,
                errCarte, errExpiration, errCvv, errDate}) {
            if (l != null) { l.setVisible(false); l.setManaged(false); }
        }
    }

    @FXML
    private void sauvegarder() {
        if (!validerFormulaire()) return;
        try {
            double montant = Double.parseDouble(txtMontant.getText());

            // Chercher l'abonnement
            Abonnement abo = null;
            if (comboService != null && comboService.getValue() != null) {
                abo = aboService.afficher().stream()
                        .filter(a -> a.getNom().equals(comboService.getValue()))
                        .findFirst().orElse(null);
            } else if (enEdition != null) {
                int id = enEdition.getAbonnementId();
                abo = aboService.afficher().stream()
                        .filter(a -> a.getId() == id).findFirst().orElse(null);
            }
            if (abo == null) { alerte(Alert.AlertType.ERROR, "Service introuvable !"); return; }

            String mode   = "Carte Bancaire";
            Date   date   = datePickerPaie != null ? Date.valueOf(datePickerPaie.getValue()) : Date.valueOf(LocalDate.now());
            String nom    = txtNomTitulaire    != null ? txtNomTitulaire.getText()    : "";
            String prenom = txtPrenomTitulaire != null ? txtPrenomTitulaire.getText() : "";
            String carte  = txtNumeroCarte     != null ? txtNumeroCarte.getText()     : "";
            String expir  = txtDateExpiration  != null ? txtDateExpiration.getText()  : "";
            String cvv    = txtCvv             != null ? txtCvv.getText()             : "";

            if (enEdition == null) {
                paieService.ajouter(new Paiement(
                        montant, date, statutSelectionne, abo.getId(),
                        nom, prenom, mode, carte, expir, cvv));

                // ══════════════════════════════════════════════════════
                // RENOUVELLEMENT : si statut = Paye → mettre à jour
                // la dateDebut de l'abonnement à aujourd'hui
                // → la prochaine expiration sera recalculée correctement
                // ══════════════════════════════════════════════════════
                if ("Paye".equals(statutSelectionne)) {
                    abo.setDateDebut(Date.valueOf(LocalDate.now()));
                    aboService.modifier(abo);
                    alerte(Alert.AlertType.INFORMATION,
                            "✅ Paiement confirmé !\n" +
                                    "🔄 Abonnement « " + abo.getNom() + " » renouvelé.\n" +
                                    "📅 Prochaine expiration : " + calculerProchaineExpiration(abo));
                } else {
                    alerte(Alert.AlertType.INFORMATION, "Paiement ajouté !");
                }

            } else {
                enEdition.setAbonnementId(abo.getId()); enEdition.setMontant(montant);
                enEdition.setDatePaiement(date); enEdition.setStatut(statutSelectionne);
                enEdition.setNomTitulaire(nom); enEdition.setPrenomTitulaire(prenom);
                enEdition.setModePaiement(mode); enEdition.setNumeroCarte(carte);
                enEdition.setDateExpiration(expir); enEdition.setCvv(cvv);
                paieService.modifier(enEdition);

                // Renouvellement lors d'une modification aussi
                if ("Paye".equals(statutSelectionne)) {
                    abo.setDateDebut(Date.valueOf(LocalDate.now()));
                    aboService.modifier(abo);
                    alerte(Alert.AlertType.INFORMATION,
                            "✅ Paiement modifié !\n" +
                                    "🔄 Abonnement « " + abo.getNom() + " » renouvelé.\n" +
                                    "📅 Prochaine expiration : " + calculerProchaineExpiration(abo));
                } else {
                    alerte(Alert.AlertType.INFORMATION, "Paiement modifié !");
                }
            }

            fermerFormulaire();
            charger();

        } catch (NumberFormatException e) {
            if (txtMontant != null) txtMontant.setStyle(CHAMP_ERREUR);
            if (errMontant != null) {
                errMontant.setText("Entrez un nombre valide (ex: 29.900)");
                errMontant.setVisible(true);
                errMontant.setManaged(true);
            }
        }
    }

    // ── Calcule et formate la prochaine date d'expiration ─────────────
    private String calculerProchaineExpiration(Abonnement abo) {
        LocalDate debut = abo.getDateDebut().toLocalDate();
        LocalDate fin   = "Annuel".equalsIgnoreCase(abo.getFrequence())
                ? debut.plusYears(1) : debut.plusMonths(1);
        return fin.toString();
    }

    private void supprimerPaiement(Paiement p) {
        Alert c = new Alert(Alert.AlertType.CONFIRMATION); c.setHeaderText(null);
        c.setContentText("Supprimer ce paiement de " + String.format("%.3f TND", p.getMontant()) + " ?");
        c.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) { paieService.supprimer(p.getId()); charger(); }
        });
    }

    private VBox creerLigne(Paiement p) {
        boolean isPaye = "Paye".equals(p.getStatut());
        String nom = getNomService(p.getAbonnementId());

        HBox ligne = new HBox(0); ligne.setAlignment(Pos.CENTER_LEFT);
        ligne.setStyle("-fx-background-color:white;-fx-background-radius:12;-fx-padding:14 20;" +
                "-fx-cursor:hand;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.04),6,0,0,2);");

        HBox svc = new HBox(8); svc.setPrefWidth(190); svc.setAlignment(Pos.CENTER_LEFT);
        svc.getChildren().addAll(
                lbl(isPaye ? "V" : "...", "-fx-font-size:14px;"),
                lbl(nom, "-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#0f1f3d;"));

        Label dateLbl    = lblW(p.getDatePaiement().toString(), 140,
                "-fx-font-size:12px;-fx-text-fill:#556677;");
        Label montantLbl = lblW(String.format("%.3f TND", p.getMontant()), 140,
                "-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:" +
                        (isPaye ? "#27ae60" : "#1a3a7a") + ";");
        Label stLbl = lblW(isPaye ? "Paye" : "A venir", 130,
                "-fx-background-color:" + (isPaye ? "#e8fff4" : "#fff8e8") + ";" +
                        "-fx-text-fill:" + (isPaye ? "#27ae60" : "#e67e22") + ";" +
                        "-fx-font-size:11px;-fx-font-weight:bold;-fx-padding:4 12;-fx-background-radius:20;");

        Button bE = actionBtn("Modifier", "#e8eeff"); bE.setOnAction(e -> { e.consume(); ouvrirModifier(p); });
        Button bD = actionBtn("Suppr.", "#fff0f0");   bD.setStyle(bD.getStyle() + "-fx-text-fill:#e74c3c;");
        bD.setOnAction(e -> { e.consume(); supprimerPaiement(p); });
        Label fleche = lbl(">", "-fx-font-size:20px;-fx-text-fill:#1a3a7a;-fx-font-weight:bold;" +
                "-fx-cursor:hand;-fx-padding:0 0 0 5;");

        HBox actions = new HBox(8); HBox.setHgrow(actions, Priority.ALWAYS);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.getChildren().addAll(bE, bD, fleche);
        ligne.getChildren().addAll(svc, dateLbl, montantLbl, stLbl, actions);

        VBox details = new VBox(0); details.setVisible(false); details.setManaged(false);
        details.setStyle("-fx-background-color:#f8faff;-fx-padding:18 25;" +
                "-fx-background-radius:0 0 12 12;-fx-border-color:#1a3a7a;" +
                "-fx-border-width:0 1.5 1.5 1.5;-fx-border-radius:0 0 12 12;");
        String titulaire = (safe(p.getNomTitulaire(), "") + " " + safe(p.getPrenomTitulaire(), "")).trim();
        HBox row1 = new HBox(30);
        row1.getChildren().addAll(dBox("SERVICE", nom), dBox("DATE", p.getDatePaiement().toString()),
                dBox("MONTANT", String.format("%.3f TND", p.getMontant())), dBox("STATUT", p.getStatut()));
        HBox row2 = new HBox(30); row2.setStyle("-fx-padding:10 0 0 0;");
        row2.getChildren().addAll(
                dBox("TITULAIRE", titulaire.isEmpty() ? "-" : titulaire),
                dBox("N CARTE", maskCarte(p.getNumeroCarte())),
                dBox("EXPIRATION", safe(p.getDateExpiration(), "-")),
                dBox("CVV", safe(p.getCvv(), "-")));
        details.getChildren().addAll(row1, new Separator(), row2);

        final boolean[] open = {false};
        Runnable toggle = () -> {
            open[0] = !open[0];
            details.setVisible(open[0]); details.setManaged(open[0]);
            fleche.setText(open[0] ? "v" : ">");
            ligne.setStyle(open[0]
                    ? "-fx-background-color:white;-fx-padding:14 20;-fx-background-radius:12 12 0 0;" +
                    "-fx-cursor:hand;-fx-border-color:#1a3a7a;-fx-border-width:1.5 1.5 0 1.5;-fx-border-radius:12 12 0 0;"
                    : "-fx-background-color:white;-fx-background-radius:12;-fx-padding:14 20;" +
                    "-fx-cursor:hand;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.04),6,0,0,2);");
        };
        fleche.setOnMouseClicked(e -> { e.consume(); toggle.run(); });
        ligne.setOnMouseClicked(e -> toggle.run());
        ligne.setOnMouseEntered(e -> { if (!open[0]) ligne.setStyle(
                "-fx-background-color:#f0f4ff;-fx-background-radius:12;-fx-padding:14 20;-fx-cursor:hand;"); });
        ligne.setOnMouseExited(e -> { if (!open[0]) ligne.setStyle(
                "-fx-background-color:white;-fx-background-radius:12;-fx-padding:14 20;-fx-cursor:hand;" +
                        "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.04),6,0,0,2);"); });

        VBox container = new VBox(0); container.getChildren().addAll(ligne, details);
        return container;
    }

    private void chargerServices() {
        if (comboService == null) return;
        comboService.setItems(FXCollections.observableArrayList(
                aboService.afficher().stream().map(Abonnement::getNom).distinct().sorted().toList()));
    }

    private void viderForm() {
        if (comboService       != null) { comboService.setValue(null); comboService.setDisable(false); }
        if (txtMontant         != null) { txtMontant.clear(); txtMontant.setDisable(false); }
        if (txtNomTitulaire    != null) txtNomTitulaire.clear();
        if (txtPrenomTitulaire != null) txtPrenomTitulaire.clear();
        if (txtNumeroCarte     != null) txtNumeroCarte.clear();
        if (txtDateExpiration  != null) txtDateExpiration.clear();
        if (txtCvv             != null) txtCvv.clear();
        if (datePickerPaie     != null) datePickerPaie.setValue(LocalDate.now());
    }

    private String getNomService(int id) {
        return aboService.afficher().stream().filter(a -> a.getId() == id)
                .map(Abonnement::getNom).findFirst().orElse("Service #" + id);
    }

    private String safe(String s, String def) {
        return (s == null || s.isBlank()) ? def : s;
    }
    private String maskCarte(String n) {
        if (n == null || n.isBlank()) return "-";
        return n.length() >= 4 ? "**** **** **** " + n.substring(n.length() - 4) : n;
    }
    private void alerte(Alert.AlertType t, String m) {
        Alert a = new Alert(t); a.setHeaderText(null); a.setContentText(m); a.showAndWait();
    }
    private Label lbl(String t, String s) {
        Label l = new Label(t); l.setStyle(s); return l;
    }
    private Label lblW(String t, double w, String s) {
        Label l = lbl(t, s); l.setPrefWidth(w); return l;
    }
    private Button actionBtn(String t, String bg) {
        Button b = new Button(t);
        b.setStyle("-fx-background-color:" + bg + ";-fx-font-size:11px;" +
                "-fx-padding:6 10;-fx-background-radius:7;-fx-cursor:hand;");
        return b;
    }
    private VBox dBox(String label, String val) {
        VBox v = new VBox(5);
        v.getChildren().addAll(
                lbl(label, "-fx-font-size:9px;-fx-font-weight:bold;-fx-text-fill:#8899aa;"),
                lbl(val,   "-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:#0f1f3d;"));
        return v;
    }

    @FXML private void retourAbonnements() {
        if (mainController != null) mainController.switchAbonnements();
    }

    public void ouvrirFormulaireAvecAbonnement(Abonnement abo) {
        ouvrirFormulaire();
        if (comboService != null) {
            comboService.setValue(abo.getNom());
            comboService.setDisable(true);
        }
        if (txtMontant != null) {
            txtMontant.setText(String.format("%.3f", abo.getPrix()));
            txtMontant.setDisable(true);
        }
        if (lblTitreForm != null) lblTitreForm.setText("Payer " + abo.getNom());
        // Pré-sélectionner statut "Paye" puisque c'est un renouvellement
        statutSelectionne = "Paye";
        majToggle();
    }
}