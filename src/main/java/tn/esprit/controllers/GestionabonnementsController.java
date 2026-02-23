package tn.esprit.controllers;

import javafx.animation.*;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import tn.esprit.entities.Abonnement;
import tn.esprit.entities.Paiement;
import tn.esprit.services.AbonnementService;
import tn.esprit.services.PaiementService;

import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class GestionabonnementsController {

    // ── Pages ──────────────────────────────────────────────────────────────────
    @FXML private VBox pageListe, pageDetailService, pageFormulaire;
    @FXML private VBox pageFormulairePaiement, pageStatistiques;

    // ── Liste ──────────────────────────────────────────────────────────────────
    @FXML private Label            lblTotalMensuel;
    @FXML private TextField        txtRecherche;
    @FXML private ComboBox<String> comboFiltreCategorie, comboTri;
    @FXML private VBox             listeServices;

    // ── Détail ─────────────────────────────────────────────────────────────────
    @FXML private Label lblServiceNom, lblServiceSousTitre;
    @FXML private HBox  statsService;
    @FXML private VBox  listeAbonnementsService;

    // ── Formulaire ajout ───────────────────────────────────────────────────────
    @FXML private TextField        txtNom, txtPrixVisible;
    @FXML private ComboBox<String> comboCategorie, comboFrequence;
    @FXML private DatePicker       datePicker;

    // ── Paiement ───────────────────────────────────────────────────────────────
    @FXML private Label     lblServicePaiement, lblPrixPaiement, lblTotalDue, lblNomTierPaiement;
    @FXML private TextField txtNomPaiement, txtPrenomPaiement, txtNumeroCarte, txtDateExpiration, txtCvv;
    @FXML private Button    btnPayerDynamique;
    @FXML private VBox      carteNormalPaie, cartePremiumPaie, carteGoldPaie;
    @FXML private Label     lblCarteNumero, lblCarteTitulaire, lblCarteExpiry;

    // ── Statistiques ───────────────────────────────────────────────────────────
    @FXML private HBox statsKpiRow;
    @FXML private VBox conteneurBarres, conteneurTop, conteneurHistorique;

    // ── State ──────────────────────────────────────────────────────────────────
    private MainController      mainController;
    private final AbonnementService aboService  = new AbonnementService();
    private final PaiementService   paieService = new PaiementService();
    private List<Abonnement> tous;
    private String           nomServiceSelectionne;
    private Abonnement       abonnementPourPaiement;
    private String           tierSelectionnePaie = null;

    private static final double PRIX_NORMAL  = 15.0;
    private static final double PRIX_PREMIUM = 40.0;
    private static final double PRIX_GOLD    = 80.0;

    private static final String CN_OFF = "-fx-background-color:#f0f7ff;-fx-padding:14 8;-fx-background-radius:12;-fx-border-color:#3498db;-fx-border-width:2;-fx-border-radius:12;-fx-cursor:hand;";
    private static final String CP_OFF = "-fx-background-color:#f5f3ff;-fx-padding:14 8;-fx-background-radius:12;-fx-border-color:#6c5ce7;-fx-border-width:2;-fx-border-radius:12;-fx-cursor:hand;";
    private static final String CG_OFF = "-fx-background-color:#fffbeb;-fx-padding:14 8;-fx-background-radius:12;-fx-border-color:#f39c12;-fx-border-width:2;-fx-border-radius:12;-fx-cursor:hand;";
    private static final String CN_ON  = "-fx-background-color:#bfdbfe;-fx-padding:14 8;-fx-background-radius:12;-fx-border-color:#2980b9;-fx-border-width:3.5;-fx-border-radius:12;-fx-cursor:hand;-fx-effect:dropshadow(gaussian,rgba(52,152,219,0.4),8,0,0,3);";
    private static final String CP_ON  = "-fx-background-color:#ddd6fe;-fx-padding:14 8;-fx-background-radius:12;-fx-border-color:#6c5ce7;-fx-border-width:3.5;-fx-border-radius:12;-fx-cursor:hand;-fx-effect:dropshadow(gaussian,rgba(108,92,231,0.4),8,0,0,3);";
    private static final String CG_ON  = "-fx-background-color:#fde68a;-fx-padding:14 8;-fx-background-radius:12;-fx-border-color:#d97706;-fx-border-width:3.5;-fx-border-radius:12;-fx-cursor:hand;-fx-effect:dropshadow(gaussian,rgba(243,156,18,0.4),8,0,0,3);";

    private static final String[] COULEURS = {"#1a3a7a","#6c5ce7","#27ae60","#e67e22","#e74c3c","#3498db","#f39c12"};

    public void setMainController(MainController mc) { this.mainController = mc; }

    // ══════════════════════════════════════════════════════════════════════════
    // INITIALIZE
    // ══════════════════════════════════════════════════════════════════════════
    @FXML
    public void initialize() {
        comboCategorie.setItems(FXCollections.observableArrayList(
                "Streaming","Musique","Sport","Education","Jeux","Cloud","Autre"));
        comboFrequence.setItems(FXCollections.observableArrayList("Mensuel","Annuel"));
        comboFiltreCategorie.setItems(FXCollections.observableArrayList(
                "Toutes","Streaming","Musique","Sport","Education","Jeux","Cloud","Autre"));
        comboTri.setItems(FXCollections.observableArrayList(
                "Nom A→Z","Nom Z→A","Prix croissant","Prix decroissant"));
        chargerTout();
        if (txtNumeroCarte    != null) txtNumeroCarte.textProperty().addListener((o,v,n)->majCarteVisa());
        if (txtNomPaiement    != null) txtNomPaiement.textProperty().addListener((o,v,n)->majCarteVisa());
        if (txtPrenomPaiement != null) txtPrenomPaiement.textProperty().addListener((o,v,n)->majCarteVisa());
        if (txtDateExpiration != null) txtDateExpiration.textProperty().addListener((o,v,n)->majCarteVisa());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // NAVIGATION
    // ══════════════════════════════════════════════════════════════════════════
    private void showPage(VBox page) {
        for (VBox v : new VBox[]{pageListe,pageDetailService,pageFormulaire,
                pageFormulairePaiement,pageStatistiques})
            if (v!=null) { v.setVisible(false); v.setManaged(false); }
        page.setVisible(true); page.setManaged(true);
        FadeTransition ft = new FadeTransition(Duration.millis(180), page);
        ft.setFromValue(0); ft.setToValue(1); ft.play();
    }

    @FXML private void ouvrirFormulaire()  { viderFormulaire(); showPage(pageFormulaire); }
    @FXML private void retourListe()       { chargerTout(); showPage(pageListe); }
    @FXML private void appliquerFiltres()  { filtrer(); }

    // ══════════════════════════════════════════════════════════════════════════
    // CHARGEMENT + FILTRES
    // ══════════════════════════════════════════════════════════════════════════
    private void chargerTout() {
        tous = aboService.afficher();
        filtrer();
    }

    /**
     * Convertit n'importe quel abonnement en équivalent mensuel.
     * Si la fréquence contient "ann" (Annuel, annuel, ANNUEL…) → prix/12
     * Sinon → prix tel quel
     */
    private double enMensuel(Abonnement a) {
        if (a.getFrequence() != null && a.getFrequence().toLowerCase().contains("ann"))
            return a.getPrix() / 12.0;
        return a.getPrix();
    }

    private void majTotal(List<Abonnement> liste) {
        double total = liste.stream().mapToDouble(this::enMensuel).sum();
        lblTotalMensuel.setText(String.format("%.3f TND", total));
    }

    private void filtrer() {
        String recherche = txtRecherche != null ? txtRecherche.getText().toLowerCase().trim() : "";
        String catFiltre = comboFiltreCategorie != null ? comboFiltreCategorie.getValue() : null;
        String tri       = comboTri != null ? comboTri.getValue() : null;

        List<Abonnement> f = tous.stream()
                .filter(a -> recherche.isEmpty()
                        || a.getNom().toLowerCase().contains(recherche)
                        || (a.getCategorie()!=null && a.getCategorie().toLowerCase().contains(recherche)))
                .filter(a -> catFiltre==null || "Toutes".equals(catFiltre)
                        || catFiltre.equals(a.getCategorie()))
                .collect(Collectors.toList());

        if      ("Nom A→Z".equals(tri))        f.sort(Comparator.comparing(Abonnement::getNom, String.CASE_INSENSITIVE_ORDER));
        else if ("Nom Z→A".equals(tri))        f.sort(Comparator.comparing(Abonnement::getNom, String.CASE_INSENSITIVE_ORDER).reversed());
        else if ("Prix croissant".equals(tri)) f.sort(Comparator.comparingDouble(Abonnement::getPrix));
        else if ("Prix decroissant".equals(tri))f.sort(Comparator.comparingDouble(Abonnement::getPrix).reversed());
        else                                    f.sort(Comparator.comparingDouble(Abonnement::getPrix).reversed());

        afficherListe(f);
        majTotal(f);
    }

    private void afficherListe(List<Abonnement> liste) {
        listeServices.getChildren().clear();

        // Regrouper par nom (on garde l'ordre obtenu après tri)
        Map<String,List<Abonnement>> parService = new LinkedHashMap<>();
        for (Abonnement a : liste)
            parService.computeIfAbsent(a.getNom().trim().toLowerCase(), k->new ArrayList<>()).add(a);

        if (parService.isEmpty()) {
            Label vide = new Label("Aucun abonnement. Cliquez sur + Ajouter !");
            vide.setStyle("-fx-text-fill:#94a3b8;-fx-font-size:14px;-fx-padding:40 0;");
            listeServices.getChildren().add(vide);
            return;
        }
        int[] idx = {0};
        parService.forEach((key,abos) -> {
            HBox ligne = creerLigne(abos.get(0), abos);
            ligne.setOpacity(0);
            listeServices.getChildren().add(ligne);
            PauseTransition pause = new PauseTransition(Duration.millis(idx[0]++ * 45));
            pause.setOnFinished(ev -> {
                FadeTransition ft = new FadeTransition(Duration.millis(200), ligne);
                ft.setFromValue(0); ft.setToValue(1); ft.play();
            });
            pause.play();
        });
    }

    // ══════════════════════════════════════════════════════════════════════════
    // LIGNE SERVICE
    // ══════════════════════════════════════════════════════════════════════════
    private HBox creerLigne(Abonnement rep, List<Abonnement> abos) {
        HBox ligne = new HBox(16); ligne.setAlignment(Pos.CENTER_LEFT);
        String base  = "-fx-background-color:white;-fx-padding:18 24;-fx-background-radius:14;-fx-cursor:hand;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.05),8,0,0,2);";
        String hover = "-fx-background-color:#f8faff;-fx-padding:18 24;-fx-background-radius:14;-fx-cursor:hand;-fx-border-color:#c7d7f0;-fx-border-width:1.5;-fx-border-radius:14;";
        ligne.setStyle(base);

        StackPane ico = creerIcone(rep.getNom());
        VBox infos = new VBox(4); HBox.setHgrow(infos, Priority.ALWAYS);
        Label nom = new Label(rep.getNom());
        nom.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:#0f1f3d;");
        Label cat = new Label(rep.getCategorie()!=null ? rep.getCategorie() : "");
        cat.setStyle("-fx-background-color:#f1f5f9;-fx-text-fill:#64748b;-fx-font-size:10px;-fx-padding:3 10;-fx-background-radius:20;");
        long nbPaies = abos.stream().flatMap(a->paieService.afficher().stream()
                .filter(p->p.getAbonnementId()==a.getId()&&"Paye".equals(p.getStatut()))).count();
        String freqAff = rep.getFrequence()!=null ? rep.getFrequence() : "Mensuel";
        Label desc = new Label(freqAff+"  •  "+nbPaies+" paie(s)");
        desc.setStyle("-fx-font-size:11px;-fx-text-fill:#94a3b8;");
        infos.getChildren().addAll(nom, cat, desc);

        double prixM = abos.stream().mapToDouble(this::enMensuel).sum();
        VBox prixBox = new VBox(2); prixBox.setAlignment(Pos.CENTER_RIGHT);
        Label prix = new Label(String.format("%.2f TND", prixM));
        prix.setStyle("-fx-font-size:18px;-fx-font-weight:bold;-fx-text-fill:#0f1f3d;");
        Label unit = new Label("par mois"); unit.setStyle("-fx-font-size:10px;-fx-text-fill:#94a3b8;");
        prixBox.getChildren().addAll(prix, unit);

        Button btnP = new Button("Payer");
        btnP.setStyle("-fx-background-color:#1a3a7a;-fx-text-fill:white;-fx-font-size:12px;-fx-font-weight:bold;-fx-padding:9 22;-fx-background-radius:9;-fx-cursor:hand;");
        btnP.setOnAction(e->{e.consume(); ouvrirFormulairePaiement(abos.get(0));});

        ligne.getChildren().addAll(ico, infos, prixBox, btnP);
        ligne.setOnMouseEntered(e->ligne.setStyle(hover));
        ligne.setOnMouseExited(e->ligne.setStyle(base));
        ligne.setOnMouseClicked(e->ouvrirDetail(rep.getNom(), abos));
        return ligne;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // DETAIL SERVICE
    // ══════════════════════════════════════════════════════════════════════════
    private void ouvrirDetail(String nomService, List<Abonnement> abos) {
        nomServiceSelectionne = nomService;
        lblServiceNom.setText(nomService);
        lblServiceSousTitre.setText(abos.size()+" abonnement(s)");
        statsService.getChildren().clear();
        double total = abos.stream().mapToDouble(this::enMensuel).sum();
        long nbPaie = abos.stream().mapToLong(a->paieService.afficher().stream()
                .filter(p->p.getAbonnementId()==a.getId()).count()).sum();
        statsService.getChildren().addAll(
                kCard("Mensuel",     String.format("%.3f TND",total),"#1a3a7a","#e8f0ff"),
                kCard("Abonnements", String.valueOf(abos.size()),    "#27ae60","#e8fff4"),
                kCard("Paiements",   String.valueOf(nbPaie),         "#6c5ce7","#f5f3ff"));
        listeAbonnementsService.getChildren().clear();
        aboService.afficher().stream()
                .filter(a->a.getNom().trim().equalsIgnoreCase(nomService.trim()))
                .forEach(a->listeAbonnementsService.getChildren().add(creerCarteAbo(a)));
        showPage(pageDetailService);
    }

    private VBox creerCarteAbo(Abonnement a) {
        VBox c = new VBox(14);
        c.setStyle("-fx-background-color:white;-fx-padding:22 26;-fx-background-radius:16;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.07),12,0,0,4);");
        HBox top = new HBox(12); top.setAlignment(Pos.CENTER_LEFT);
        Label nom = new Label(a.getNom()+" — "+(a.getFrequence()!=null?a.getFrequence():"Mensuel"));
        nom.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:#0f1f3d;");
        HBox.setHgrow(nom, Priority.ALWAYS);
        Label st = new Label(a.isActif()?"✔ Actif":"✘ Inactif");
        st.setStyle("-fx-background-color:"+(a.isActif()?"#e8fff4":"#fff0f0")+";-fx-text-fill:"+(a.isActif()?"#27ae60":"#e74c3c")+";-fx-font-size:11px;-fx-font-weight:bold;-fx-padding:5 14;-fx-background-radius:20;");
        top.getChildren().addAll(nom, st);
        HBox infos = new HBox(40);
        infos.getChildren().addAll(
                infoBox("PRIX",  String.format("%.3f TND",a.getPrix())),
                infoBox("DEBUT", a.getDateDebut().toString()),
                infoBox("ANNUEL",String.format("%.3f TND",a.getPrix()*12)));
        HBox boutons = new HBox(10);
        Button bP=btn("Payer","#1a3a7a","white"); bP.setOnAction(e->ouvrirFormulairePaiement(a));
        Button bE=btn("Modifier","#f1f5f9","#475569"); bE.setOnAction(e->dialogModifier(a));
        Button bD=btn("Supprimer","#fff0f0","#e74c3c"); bD.setOnAction(e->supprimerAbo(a));
        boutons.getChildren().addAll(bP,bE,bD);
        c.getChildren().addAll(top,infos,boutons);
        return c;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // FORMULAIRE AJOUT
    // ══════════════════════════════════════════════════════════════════════════
    @FXML
    private void ajouter() {
        String nom    = txtNom!=null        ? txtNom.getText().trim()        : "";
        String prixStr= txtPrixVisible!=null? txtPrixVisible.getText().trim(): "";
        if (nom.isEmpty()||prixStr.isEmpty()||comboCategorie.getValue()==null
                ||comboFrequence.getValue()==null||datePicker.getValue()==null) {
            alert(Alert.AlertType.WARNING,"Remplissez tous les champs !"); return;
        }
        try {
            double prix = Double.parseDouble(prixStr.replace(",","."));
            if (prix<=0) throw new NumberFormatException();
            Abonnement n = new Abonnement(nom,prix,Date.valueOf(datePicker.getValue()),
                    comboFrequence.getValue(),comboCategorie.getValue(),true);
            n.setTier("Normal");
            aboService.ajouter(n);
            alert(Alert.AlertType.INFORMATION,"Abonnement « "+nom+" » ajoute !");
            retourListe();
        } catch (NumberFormatException ex) { alert(Alert.AlertType.ERROR,"Prix invalide ! Ex: 25.000");
        } catch (Exception ex)             { alert(Alert.AlertType.ERROR,"Erreur lors de l'ajout !"); }
    }

    private void viderFormulaire() {
        if (txtNom!=null)         txtNom.clear();
        if (txtPrixVisible!=null) txtPrixVisible.clear();
        comboCategorie.setValue(null); comboFrequence.setValue(null); datePicker.setValue(null);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // PAIEMENT
    // ══════════════════════════════════════════════════════════════════════════
    private void ouvrirFormulairePaiement(Abonnement a) {
        abonnementPourPaiement=a; tierSelectionnePaie=null;
        if (carteNormalPaie !=null) carteNormalPaie.setStyle(CN_OFF);
        if (cartePremiumPaie!=null) cartePremiumPaie.setStyle(CP_OFF);
        if (carteGoldPaie   !=null) carteGoldPaie.setStyle(CG_OFF);
        if (lblServicePaiement!=null) lblServicePaiement.setText(a.getNom());
        if (lblPrixPaiement   !=null) lblPrixPaiement.setText("---");
        if (lblTotalDue       !=null) lblTotalDue.setText("---");
        if (btnPayerDynamique !=null) btnPayerDynamique.setText("Confirmer le paiement");
        switch (a.getTier()!=null?a.getTier():"Normal") {
            case "Premium"->selectionnerPremiumPaie();
            case "Gold"   ->selectionnerGoldPaie();
            default       ->selectionnerNormalPaie();
        }
        if (txtNomPaiement   !=null) txtNomPaiement.clear();
        if (txtPrenomPaiement!=null) txtPrenomPaiement.clear();
        if (txtNumeroCarte   !=null) txtNumeroCarte.clear();
        if (txtDateExpiration!=null) txtDateExpiration.clear();
        if (txtCvv           !=null) txtCvv.clear();
        majCarteVisa();
        showPage(pageFormulairePaiement);
    }

    @FXML private void selectionnerNormalPaie() {
        tierSelectionnePaie="Normal";
        if (carteNormalPaie !=null) carteNormalPaie.setStyle(CN_ON);
        if (cartePremiumPaie!=null) cartePremiumPaie.setStyle(CP_OFF);
        if (carteGoldPaie   !=null) carteGoldPaie.setStyle(CG_OFF);
        majPrix(PRIX_NORMAL,"Normal","#2980b9");
    }
    @FXML private void selectionnerPremiumPaie() {
        tierSelectionnePaie="Premium";
        if (carteNormalPaie !=null) carteNormalPaie.setStyle(CN_OFF);
        if (cartePremiumPaie!=null) cartePremiumPaie.setStyle(CP_ON);
        if (carteGoldPaie   !=null) carteGoldPaie.setStyle(CG_OFF);
        majPrix(PRIX_PREMIUM,"Premium","#6c5ce7");
    }
    @FXML private void selectionnerGoldPaie() {
        tierSelectionnePaie="Gold";
        if (carteNormalPaie !=null) carteNormalPaie.setStyle(CN_OFF);
        if (cartePremiumPaie!=null) cartePremiumPaie.setStyle(CP_OFF);
        if (carteGoldPaie   !=null) carteGoldPaie.setStyle(CG_ON);
        majPrix(PRIX_GOLD,"Gold","#d97706");
    }

    private void majPrix(double prix, String tier, String color) {
        String fmt = String.format("%.3f TND",prix);
        if (lblPrixPaiement   !=null) lblPrixPaiement.setText(fmt);
        if (lblTotalDue       !=null) lblTotalDue.setText(fmt);
        if (lblNomTierPaiement!=null) {
            lblNomTierPaiement.setText("Plan "+tier+" selectionne");
            lblNomTierPaiement.setStyle("-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:"+color+";-fx-padding:0 18 14 18;");
        }
        if (btnPayerDynamique!=null) btnPayerDynamique.setText("Payer "+tier+" — "+fmt);
    }

    private void majCarteVisa() {
        if (lblCarteNumero!=null) {
            String raw=txtNumeroCarte!=null?txtNumeroCarte.getText().replaceAll("[^0-9]",""):"";
            lblCarteNumero.setText(raw.length()>=4
                    ?"\u25CF\u25CF\u25CF\u25CF  \u25CF\u25CF\u25CF\u25CF  \u25CF\u25CF\u25CF\u25CF  "+raw.substring(raw.length()-4)
                    :"\u25CF\u25CF\u25CF\u25CF  \u25CF\u25CF\u25CF\u25CF  \u25CF\u25CF\u25CF\u25CF  \u25CF\u25CF\u25CF\u25CF");
        }
        if (lblCarteTitulaire!=null) {
            String p=txtPrenomPaiement!=null?txtPrenomPaiement.getText():"";
            String n=txtNomPaiement   !=null?txtNomPaiement.getText():"";
            String t=(p+" "+n).trim().toUpperCase();
            lblCarteTitulaire.setText(t.isEmpty()?"VOTRE NOM":t);
        }
        if (lblCarteExpiry!=null) {
            String e=txtDateExpiration!=null?txtDateExpiration.getText():"";
            lblCarteExpiry.setText(e.isEmpty()?"MM/YY":e);
        }
    }

    @FXML
    private void confirmerPaiement() {
        if (tierSelectionnePaie==null) { alert(Alert.AlertType.WARNING,"Selectionnez un plan !"); return; }
        if (txtNomPaiement.getText().isEmpty()||txtPrenomPaiement.getText().isEmpty()||txtNumeroCarte.getText().isEmpty()) {
            alert(Alert.AlertType.WARNING,"Remplissez les champs obligatoires !"); return;
        }
        try {
            double montant=prixParTier(tierSelectionnePaie);
            Paiement p=new Paiement();
            p.setAbonnementId(abonnementPourPaiement.getId()); p.setMontant(montant);
            p.setDatePaiement(Date.valueOf(LocalDate.now())); p.setStatut("Paye");
            p.setNomTitulaire(txtNomPaiement.getText()); p.setPrenomTitulaire(txtPrenomPaiement.getText());
            p.setModePaiement("Carte Bancaire"); p.setNumeroCarte(txtNumeroCarte.getText());
            if (txtDateExpiration!=null) p.setDateExpiration(txtDateExpiration.getText());
            if (txtCvv           !=null) p.setCvv(txtCvv.getText());
            paieService.ajouter(p);
            popupSucces(abonnementPourPaiement.getNom(),montant,"#DIN-"+(10000+(int)(Math.random()*89999))+"-TX");
        } catch (Exception e) { alert(Alert.AlertType.ERROR,"Erreur lors du paiement !"); }
    }

    @FXML
    private void annulerPaiement() {
        if (abonnementPourPaiement!=null) {
            List<Abonnement> abos=aboService.afficher().stream()
                    .filter(a->a.getId()==abonnementPourPaiement.getId()).toList();
            if (!abos.isEmpty()) { ouvrirDetail(abonnementPourPaiement.getNom(),abos); return; }
        }
        retourListe();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // POPUP SUCCÈS
    // ══════════════════════════════════════════════════════════════════════════
    private void popupSucces(String service, double montant, String txId) {
        Stage pop=new Stage(); pop.initModality(Modality.APPLICATION_MODAL);
        pop.initStyle(StageStyle.UNDECORATED); pop.setResizable(false);

        StackPane inner=new StackPane(); inner.setPrefSize(50,50); inner.setMaxSize(50,50);
        inner.setStyle("-fx-background-color:#22c55e;-fx-background-radius:50;");
        inner.setScaleX(0); inner.setScaleY(0);
        Label check=new Label("\u2713"); check.setStyle("-fx-font-size:24px;-fx-font-weight:bold;-fx-text-fill:white;");
        inner.getChildren().add(check);
        StackPane halo=new StackPane(inner); halo.setPrefSize(66,66); halo.setMaxSize(66,66);
        halo.setStyle("-fx-background-color:#dcfce7;-fx-background-radius:50;");

        Label titre=new Label("Paiement reussi !"); titre.setStyle("-fx-font-size:20px;-fx-font-weight:bold;-fx-text-fill:#0f1f3d;-fx-padding:16 0 4 0;");
        Label sous=new Label("Transaction completee avec succes."); sous.setStyle("-fx-font-size:12px;-fx-text-fill:#94a3b8;");
        VBox topZ=new VBox(0,halo,titre,sous); topZ.setAlignment(Pos.CENTER); topZ.setStyle("-fx-padding:36 28 18 28;");

        VBox det=new VBox(0);
        det.setStyle("-fx-background-color:#f8fafc;-fx-background-radius:12;-fx-border-color:#e2e8f0;-fx-border-width:1.5;-fx-border-radius:12;");
        det.setMaxWidth(290);
        det.getChildren().addAll(detRow("Service",service,true),detRow("Montant",String.format("%.3f TND",montant),true),detRow("Transaction",txId,false));
        HBox detW=new HBox(det); detW.setAlignment(Pos.CENTER); detW.setStyle("-fx-padding:18 28 0 28;"); HBox.setHgrow(det,Priority.ALWAYS);

        Button ok=new Button("OK"); ok.setMaxWidth(290);
        ok.setStyle("-fx-background-color:#1a3a7a;-fx-text-fill:white;-fx-font-size:14px;-fx-font-weight:bold;-fx-padding:14 0;-fx-background-radius:12;-fx-cursor:hand;");
        ok.setOnAction(e->{pop.close(); if(mainController!=null) mainController.switchPaiements(); else retourListe();});
        HBox btnW=new HBox(ok); btnW.setAlignment(Pos.CENTER); btnW.setStyle("-fx-padding:18 28 28 28;"); HBox.setHgrow(ok,Priority.ALWAYS);

        VBox root=new VBox(0,topZ,detW,btnW); root.setAlignment(Pos.TOP_CENTER); root.setPrefWidth(360);
        root.setStyle("-fx-background-color:white;-fx-background-radius:20;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.22),40,0,0,8);");
        StackPane sr=new StackPane(root); sr.setStyle("-fx-background-color:transparent;-fx-padding:16;");
        Scene scene=new Scene(sr,392,440); scene.setFill(Color.TRANSPARENT);
        pop.setScene(scene); pop.show();

        ScaleTransition sc=new ScaleTransition(Duration.millis(450),inner);
        sc.setFromX(0); sc.setFromY(0); sc.setToX(1); sc.setToY(1);
        sc.setInterpolator(Interpolator.SPLINE(0.34,1.56,0.64,1.0)); sc.play();
    }

    private HBox detRow(String k, String v, boolean border) {
        HBox row=new HBox(8); row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding:12 16;"+(border?"-fx-border-color:#e2e8f0;-fx-border-width:0 0 1 0;":""));
        Label lk=new Label(k); lk.setStyle("-fx-font-size:12px;-fx-text-fill:#64748b;"); HBox.setHgrow(lk,Priority.ALWAYS);
        Label lv=new Label(v); lv.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:#0f1f3d;");
        row.getChildren().addAll(lk,lv); return row;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // MODIFIER & SUPPRIMER
    // ══════════════════════════════════════════════════════════════════════════
    private void dialogModifier(Abonnement a) {
        Dialog<ButtonType> dlg=new Dialog<>(); dlg.setTitle(null); dlg.setHeaderText(null);
        VBox root=new VBox(0); root.setPrefWidth(460);
        VBox hdr=new VBox(5); hdr.setStyle("-fx-background-color:#0f1f3d;-fx-padding:22 28;-fx-background-radius:14 14 0 0;");
        Label th=new Label("Modifier"); th.setStyle("-fx-font-size:17px;-fx-font-weight:bold;-fx-text-fill:white;");
        Label ts=new Label(a.getNom()); ts.setStyle("-fx-font-size:11px;-fx-text-fill:#6a8aaa;");
        hdr.getChildren().addAll(th,ts);
        VBox body=new VBox(16); body.setStyle("-fx-background-color:white;-fx-padding:24 28;-fx-background-radius:0 0 14 14;");
        TextField fNom=new TextField(a.getNom()); styleTf(fNom);
        TextField fPrix=new TextField(String.format("%.3f",a.getPrix())); styleTf(fPrix);
        HBox r1=rowForm(lbl("Nom"),fNom,lbl("Prix (TND)"),fPrix);
        ComboBox<String> fFreq=new ComboBox<>(); fFreq.setItems(FXCollections.observableArrayList("Mensuel","Annuel")); fFreq.setValue(a.getFrequence()); fFreq.setMaxWidth(Double.MAX_VALUE);
        ToggleGroup tg=new ToggleGroup(); RadioButton rA=new RadioButton("Actif"),rI=new RadioButton("Inactif");
        rA.setToggleGroup(tg); rI.setToggleGroup(tg); rA.setSelected(a.isActif()); rI.setSelected(!a.isActif());
        HBox r2=rowForm(lbl("Frequence"),fFreq,lbl("Statut"),new HBox(10,rA,rI));
        Button bC=btn("Annuler","white","#64748b"); Button bS=btn("Enregistrer","#1a3a7a","white");
        HBox bx=new HBox(10,bC,bS); bx.setAlignment(Pos.CENTER_RIGHT);
        body.getChildren().addAll(r1,r2,new Separator(),bx); root.getChildren().addAll(hdr,body);
        dlg.getDialogPane().setContent(root); dlg.getDialogPane().getButtonTypes().add(ButtonType.CLOSE); dlg.getDialogPane().lookupButton(ButtonType.CLOSE).setVisible(false);
        bC.setOnAction(e->dlg.close());
        bS.setOnAction(e->{
            try {
                a.setNom(fNom.getText()); a.setPrix(Double.parseDouble(fPrix.getText().replace(",",".")));
                a.setFrequence(fFreq.getValue()); a.setActif(rA.isSelected());
                aboService.modifier(a); dlg.close(); alert(Alert.AlertType.INFORMATION,"Modifie !");
                List<Abonnement> maj=aboService.afficher().stream().filter(ab->ab.getNom().trim().equalsIgnoreCase(a.getNom().trim())).toList();
                ouvrirDetail(a.getNom(),maj);
            } catch (Exception ex) { alert(Alert.AlertType.ERROR,"Erreur !"); }
        });
        dlg.showAndWait();
    }

    private void supprimerAbo(Abonnement a) {
        Alert c=new Alert(Alert.AlertType.CONFIRMATION); c.setHeaderText(null); c.setContentText("Supprimer « "+a.getNom()+" » ?");
        c.showAndWait().ifPresent(btn->{
            if (btn==ButtonType.OK) {
                aboService.supprimer(a.getId());
                List<Abonnement> r=aboService.afficher().stream().filter(ab->ab.getNom().trim().equalsIgnoreCase(nomServiceSelectionne.trim())).toList();
                if (r.isEmpty()) retourListe(); else ouvrirDetail(nomServiceSelectionne,r);
            }
        });
    }

    // ══════════════════════════════════════════════════════════════════════════
    // STATISTIQUES — totaux corrigés avec enMensuel()
    // ══════════════════════════════════════════════════════════════════════════
    @FXML
    public void ouvrirStatistiques() {
        showPage(pageStatistiques);
        PauseTransition pt=new PauseTransition(Duration.millis(80));
        pt.setOnFinished(e->construireStats()); pt.play();
    }

    private void construireStats() {
        List<Abonnement> liste    =aboService.afficher();
        List<Paiement>   paiements=paieService.afficher();

        // ── Totaux corrects ───────────────────────────────────────────────────
        double totalM  = liste.stream().mapToDouble(this::enMensuel).sum();
        double totalA  = totalM * 12.0;
        long   nbActifs= liste.stream().filter(Abonnement::isActif).count();
        long   nbPaies = paiements.stream().filter(p->"Paye".equals(p.getStatut())).count();
        double volPaie = paiements.stream().filter(p->"Paye".equals(p.getStatut())).mapToDouble(Paiement::getMontant).sum();

        // ── KPI Cards ─────────────────────────────────────────────────────────
        statsKpiRow.getChildren().clear();
        Object[][] kpis={
                {"💰","Total mensuel",   totalM,         "TND","#1a3a7a","#e8f0ff"},
                {"📅","Total annuel",    totalA,         "TND","#6c5ce7","#f0ecff"},
                {"✅","Services actifs", (double)nbActifs,"",  "#27ae60","#e8fff4"},
                {"💳","Paiements",       (double)nbPaies, "",  "#e67e22","#fff3e0"},
                {"📊","Volume paye",     volPaie,        "TND","#e74c3c","#fff0f0"},
        };
        for (int i=0;i<kpis.length;i++) {
            Object[] k=kpis[i]; double fVal=(double)k[2]; String fUnit=(String)k[3];
            VBox card=kpiCard((String)k[0],(String)k[1],fVal,fUnit,(String)k[4],(String)k[5]);
            HBox.setHgrow(card,Priority.ALWAYS); card.setOpacity(0); card.setTranslateY(24);
            statsKpiRow.getChildren().add(card);
            final VBox fCard=card;
            PauseTransition delay=new PauseTransition(Duration.millis(i*90));
            delay.setOnFinished(ev->{
                FadeTransition ft=new FadeTransition(Duration.millis(350),fCard); ft.setFromValue(0); ft.setToValue(1);
                TranslateTransition tt=new TranslateTransition(Duration.millis(350),fCard); tt.setFromY(24); tt.setToY(0); tt.setInterpolator(Interpolator.EASE_OUT);
                new ParallelTransition(ft,tt).play();
                animCount(fCard,fVal,fUnit);
            });
            delay.play();
        }

        // ── Barres par catégorie ───────────────────────────────────────────────
        conteneurBarres.getChildren().clear();
        Map<String,Double> parCat=liste.stream().collect(Collectors.groupingBy(
                a->a.getCategorie()!=null?a.getCategorie():"Autre",
                Collectors.summingDouble(this::enMensuel)));
        if (!parCat.isEmpty()) {
            double max=parCat.values().stream().mapToDouble(d->d).max().orElse(1);
            int[] ri={0};
            parCat.entrySet().stream().sorted((e1,e2)->Double.compare(e2.getValue(),e1.getValue())).forEach(e->{
                String col=COULEURS[ri[0]%COULEURS.length];
                HBox bar=new HBox(14); bar.setAlignment(Pos.CENTER_LEFT);
                Label lcat=new Label(e.getKey()); lcat.setPrefWidth(100); lcat.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:#334155;");
                StackPane cont=new StackPane(); cont.setAlignment(Pos.CENTER_LEFT); HBox.setHgrow(cont,Priority.ALWAYS);
                Rectangle bg  =new Rectangle(0,22); bg.setFill(Color.web("#f1f5f9")); bg.setArcWidth(11); bg.setArcHeight(11);
                Rectangle fill=new Rectangle(0,22); fill.setFill(Color.web(col));     fill.setArcWidth(11); fill.setArcHeight(11);
                cont.getChildren().addAll(bg,fill);
                final double ratio=e.getValue()/max;
                cont.widthProperty().addListener((obs,ov,nv)->bg.setWidth(nv.doubleValue()));
                Label val=new Label(String.format("%.2f TND",e.getValue())); val.setPrefWidth(100);
                val.setStyle("-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:"+col+";");
                bar.getChildren().addAll(lcat,cont,val); bar.setOpacity(0);
                conteneurBarres.getChildren().add(bar);
                final int rr=ri[0];
                PauseTransition d2=new PauseTransition(Duration.millis(200+rr*110));
                d2.setOnFinished(ev->{
                    FadeTransition ft2=new FadeTransition(Duration.millis(280),bar); ft2.setFromValue(0); ft2.setToValue(1); ft2.play();
                    double cw=cont.getWidth()>0?cont.getWidth():400; bg.setWidth(cw); fill.setWidth(0);
                    new Timeline(new KeyFrame(Duration.millis(650),new KeyValue(fill.widthProperty(),cw*ratio,Interpolator.EASE_OUT))).play();
                });
                d2.play(); ri[0]++;
            });
        } else conteneurBarres.getChildren().add(new Label("Aucune donnee"){{setStyle("-fx-text-fill:#94a3b8;-fx-font-size:13px;");}});

        // ── Top 5 services ────────────────────────────────────────────────────
        conteneurTop.getChildren().clear();
        List<Abonnement> top5=liste.stream().sorted((a,b)->Double.compare(enMensuel(b),enMensuel(a))).limit(5).collect(Collectors.toList());
        for (int i=0;i<top5.size();i++) {
            Abonnement a=top5.get(i); String col=COULEURS[i%COULEURS.length];
            HBox rowH=new HBox(12); rowH.setAlignment(Pos.CENTER_LEFT);
            rowH.setStyle("-fx-padding:8 0;-fx-border-color:#f1f5f9;-fx-border-width:0 0 1 0;");
            Label rang=new Label("#"+(i+1)); rang.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:"+col+";-fx-background-color:"+col+"22;-fx-padding:4 10;-fx-background-radius:8;"); rang.setPrefWidth(40);
            StackPane ico2=creerIcone(a.getNom());
            VBox info=new VBox(2); HBox.setHgrow(info,Priority.ALWAYS);
            Label nL=new Label(a.getNom()); nL.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#0f1f3d;");
            Label cL=new Label(a.getCategorie()!=null?a.getCategorie():""); cL.setStyle("-fx-font-size:10px;-fx-text-fill:#94a3b8;");
            info.getChildren().addAll(nL,cL);
            Label pL=new Label(String.format("%.2f TND",enMensuel(a))); pL.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:"+col+";");
            rowH.getChildren().addAll(rang,ico2,info,pL); rowH.setOpacity(0); rowH.setTranslateX(-16);
            conteneurTop.getChildren().add(rowH);
            final HBox fRow=rowH; final int fi=i;
            PauseTransition d3=new PauseTransition(Duration.millis(280+fi*80));
            d3.setOnFinished(ev->{
                FadeTransition ft3=new FadeTransition(Duration.millis(280),fRow); ft3.setFromValue(0); ft3.setToValue(1);
                TranslateTransition tt3=new TranslateTransition(Duration.millis(280),fRow); tt3.setFromX(-16); tt3.setToX(0); tt3.setInterpolator(Interpolator.EASE_OUT);
                new ParallelTransition(ft3,tt3).play();
            });
            d3.play();
        }
        if (top5.isEmpty()) conteneurTop.getChildren().add(new Label("Aucun abonnement"){{setStyle("-fx-text-fill:#94a3b8;-fx-font-size:13px;");}});

        // ── Paiements récents ─────────────────────────────────────────────────
        conteneurHistorique.getChildren().clear();
        List<Paiement> recents=paiements.stream().sorted((a,b)->b.getDatePaiement().compareTo(a.getDatePaiement())).limit(6).collect(Collectors.toList());
        for (int i=0;i<recents.size();i++) {
            Paiement p=recents.get(i); boolean paye="Paye".equals(p.getStatut());
            String aboNom=liste.stream().filter(a->a.getId()==p.getAbonnementId()).map(Abonnement::getNom).findFirst().orElse("Service");
            HBox rowH2=new HBox(10); rowH2.setAlignment(Pos.CENTER_LEFT);
            rowH2.setStyle("-fx-background-color:"+(paye?"#f0fdf4":"#fff7ed")+";-fx-padding:10 12;-fx-background-radius:10;");
            Label ic2=new Label(paye?"✓":"\u23F3"); ic2.setStyle("-fx-font-size:13px;-fx-text-fill:"+(paye?"#27ae60":"#e67e22")+";-fx-background-color:"+(paye?"#dcfce7":"#ffedd5")+";-fx-background-radius:50;-fx-padding:4 7;");
            VBox ri2=new VBox(2); HBox.setHgrow(ri2,Priority.ALWAYS);
            Label nL2=new Label(aboNom); nL2.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:#334155;");
            Label dL2=new Label(p.getDatePaiement().toString()); dL2.setStyle("-fx-font-size:10px;-fx-text-fill:#94a3b8;");
            ri2.getChildren().addAll(nL2,dL2);
            Label mt2=new Label(String.format("%.2f",p.getMontant())); mt2.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:"+(paye?"#27ae60":"#e67e22")+";");
            rowH2.getChildren().addAll(ic2,ri2,mt2); rowH2.setOpacity(0); conteneurHistorique.getChildren().add(rowH2);
            final HBox fRow2=rowH2; final int fi2=i;
            PauseTransition d4=new PauseTransition(Duration.millis(350+fi2*60));
            d4.setOnFinished(ev->{ FadeTransition ft4=new FadeTransition(Duration.millis(260),fRow2); ft4.setFromValue(0); ft4.setToValue(1); ft4.play(); });
            d4.play();
        }
        if (recents.isEmpty()) conteneurHistorique.getChildren().add(new Label("Aucun paiement"){{setStyle("-fx-text-fill:#94a3b8;-fx-font-size:13px;");}});
    }

    // ── KPI card ──────────────────────────────────────────────────────────────
    private VBox kpiCard(String icon,String label,double val,String unit,String color,String bg) {
        VBox c=new VBox(6); c.setAlignment(Pos.TOP_LEFT);
        c.setStyle("-fx-background-color:"+bg+";-fx-padding:20 22;-fx-background-radius:16;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),10,0,0,3);");
        Label ico=new Label(icon);   ico.setStyle("-fx-font-size:22px;");
        Label lbl=new Label(label);  lbl.setStyle("-fx-font-size:10px;-fx-font-weight:bold;-fx-text-fill:#64748b;");
        Label valL=new Label("0");   valL.setStyle("-fx-font-size:24px;-fx-font-weight:bold;-fx-text-fill:"+color+";");
        valL.setUserData(val);       // valeur cible pour l'animation
        Label unitL=new Label(unit); unitL.setStyle("-fx-font-size:10px;-fx-text-fill:#94a3b8;");
        c.getChildren().addAll(ico,lbl,valL,unitL); return c;
    }

    private void animCount(VBox card, double target, String unit) {
        card.getChildren().stream()
                .filter(n->n instanceof Label && n.getUserData() instanceof Double)
                .map(n->(Label)n).findFirst().ifPresent(valL->{
                    DoubleProperty dp=new SimpleDoubleProperty(0);
                    dp.addListener((o,ov,nv)->valL.setText("TND".equals(unit)?String.format("%.2f",nv.doubleValue()):String.format("%.0f",nv.doubleValue())));
                    new Timeline(new KeyFrame(Duration.millis(900),new KeyValue(dp,target,Interpolator.EASE_OUT))).play();
                });
    }

    // ══════════════════════════════════════════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════════════════════════════════════════
    private double prixParTier(String t) { return switch(t!=null?t:"Normal"){case"Premium"->PRIX_PREMIUM;case"Gold"->PRIX_GOLD;default->PRIX_NORMAL;}; }
    private Label lbl(String t) { Label l=new Label(t); l.setStyle("-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:#475569;"); return l; }
    private void styleTf(TextField tf) { tf.setStyle("-fx-background-radius:9;-fx-border-radius:9;-fx-border-color:#e2e8f0;-fx-border-width:1.5;-fx-padding:11 14;-fx-font-size:13px;"); }
    private void alert(Alert.AlertType t,String m) { Alert a=new Alert(t); a.setHeaderText(null); a.setContentText(m); a.showAndWait(); }
    private VBox kCard(String label,String val,String color,String bg) {
        VBox c=new VBox(5); c.setPrefWidth(180); c.setStyle("-fx-background-color:"+bg+";-fx-padding:16 20;-fx-background-radius:14;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.05),8,0,0,2);");
        c.getChildren().addAll(new Label(label){{setStyle("-fx-font-size:10px;-fx-text-fill:#64748b;-fx-font-weight:bold;");}},new Label(val){{setStyle("-fx-font-size:18px;-fx-font-weight:bold;-fx-text-fill:"+color+";");}});
        return c;
    }
    private VBox infoBox(String l,String v) {
        VBox b=new VBox(3);
        b.getChildren().addAll(new Label(l){{setStyle("-fx-text-fill:#94a3b8;-fx-font-size:9px;-fx-font-weight:bold;");}},new Label(v){{setStyle("-fx-text-fill:#0f1f3d;-fx-font-size:13px;-fx-font-weight:bold;");}});
        return b;
    }
    private Button btn(String t,String bg,String fg) { Button b=new Button(t); b.setStyle("-fx-background-color:"+bg+";-fx-text-fill:"+fg+";-fx-font-size:12px;-fx-font-weight:bold;-fx-padding:9 20;-fx-background-radius:9;-fx-cursor:hand;"); return b; }
    private HBox rowForm(Label l1,javafx.scene.Node n1,Label l2,javafx.scene.Node n2) {
        VBox v1=new VBox(6,l1,n1); VBox v2=new VBox(6,l2,n2); HBox.setHgrow(v1,Priority.ALWAYS); HBox.setHgrow(v2,Priority.ALWAYS); return new HBox(14,v1,v2);
    }
    private StackPane creerIcone(String nom) {
        Circle c=new Circle(24); c.setFill(Color.web(couleur(nom)));
        Label l=new Label(logo(nom)); l.setStyle("-fx-font-size:12px;-fx-text-fill:white;-fx-font-weight:bold;");
        return new StackPane(c,l);
    }
    private String couleur(String n) {
        String s=n.toLowerCase();
        if(s.contains("netflix"))return"#E50914"; if(s.contains("spotify"))return"#1DB954"; if(s.contains("amazon"))return"#FF9900";
        if(s.contains("disney"))return"#113CCF";  if(s.contains("youtube"))return"#FF0000"; if(s.contains("deezer"))return"#A238FF";
        if(s.contains("microsoft"))return"#0078D4";if(s.contains("google"))return"#4285F4";if(s.contains("adobe"))return"#FF4444";
        return"#1a3a7a";
    }
    private String logo(String n) {
        String s=n.toLowerCase();
        if(s.contains("netflix"))return"N"; if(s.contains("spotify"))return"S"; if(s.contains("amazon"))return"a";
        if(s.contains("disney"))return"D+";if(s.contains("youtube"))return"Y"; if(s.contains("deezer"))return"Dz";
        if(s.contains("microsoft"))return"M";if(s.contains("google"))return"G";if(s.contains("adobe"))return"Ai";
        return n.substring(0,Math.min(2,n.length())).toUpperCase();
    }
    @FXML private void ouvrirPaiements() { if(mainController!=null) mainController.switchPaiements(); }
}