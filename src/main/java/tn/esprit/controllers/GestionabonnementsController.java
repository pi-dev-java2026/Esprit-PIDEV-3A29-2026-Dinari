package tn.esprit.controllers;

import javafx.animation.*;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.util.Duration;
import tn.esprit.entities.Abonnement;
import tn.esprit.entities.Paiement;
import tn.esprit.services.AbonnementService;
import tn.esprit.services.GoogleCalendarService;
import tn.esprit.services.PaiementService;

import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

public class GestionabonnementsController {

    @FXML private VBox pageListe, pageDetailService, pageFormulaire, pageFormulairePaiement, pageStatistiques;
    @FXML private Label lblTotalMensuel;
    @FXML private TextField txtRecherche;
    @FXML private ComboBox<String> comboFiltreCategorie, comboTri;
    @FXML private VBox listeServices;
    @FXML private Label lblServiceNom, lblServiceSousTitre;
    @FXML private HBox statsService;
    @FXML private VBox listeAbonnementsService;
    @FXML private TextField txtNom;
    @FXML private ComboBox<String> comboCategorie, comboFrequence;
    @FXML private DatePicker datePicker;
    @FXML private Label lblServicePaiement, lblPrixPaiement, lblTotalDue, lblNomTierPaiement;
    @FXML private TextField txtNomPaiement, txtPrenomPaiement, txtNumeroCarte, txtDateExpiration, txtCvv;
    @FXML private Button btnPayerDynamique;
    @FXML private VBox carteNormalPaie, cartePremiumPaie, carteGoldPaie;
    @FXML private Label lblCarteNumero, lblCarteTitulaire, lblCarteExpiry;
    @FXML private HBox statsKpiRow;
    @FXML private VBox conteneurBarres, conteneurTop, conteneurHistorique;

    private MainController mainController;
    private final AbonnementService    aboService  = new AbonnementService();
    private final PaiementService      paieService = new PaiementService();
    private final GoogleCalendarService calService  = new GoogleCalendarService();

    private List<Abonnement> tous;
    private String nomServiceSelectionne;
    private Abonnement abonnementPourPaiement;
    private String tierSelectionnePaie = null;

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

    @FXML
    public void initialize() {
        comboCategorie.setItems(FXCollections.observableArrayList(
                "Streaming","Musique","Sport","Education","Jeux","Cloud","Autre"));
        comboFrequence.setItems(FXCollections.observableArrayList("Mensuel","Annuel"));
        comboFiltreCategorie.setItems(FXCollections.observableArrayList(
                "Toutes","Streaming","Musique","Sport","Education","Jeux","Cloud","Autre"));
        comboTri.setItems(FXCollections.observableArrayList(
                "Nom A\u2192Z","Nom Z\u2192A","Prix croissant","Prix decroissant"));
        chargerTout();
        if (txtNumeroCarte    != null) txtNumeroCarte.textProperty().addListener((o,v,n)->majCarteVisa());
        if (txtNomPaiement    != null) txtNomPaiement.textProperty().addListener((o,v,n)->majCarteVisa());
        if (txtPrenomPaiement != null) txtPrenomPaiement.textProperty().addListener((o,v,n)->majCarteVisa());
        if (txtDateExpiration != null) txtDateExpiration.textProperty().addListener((o,v,n)->majCarteVisa());
    }

    private void showPage(VBox page) {
        for (VBox v : new VBox[]{pageListe,pageDetailService,pageFormulaire,
                pageFormulairePaiement,pageStatistiques})
            if (v != null) { v.setVisible(false); v.setManaged(false); }
        page.setVisible(true); page.setManaged(true);
        FadeTransition ft = new FadeTransition(Duration.millis(180), page);
        ft.setFromValue(0); ft.setToValue(1); ft.play();
    }

    @FXML private void ouvrirFormulaire() { viderFormulaire(); showPage(pageFormulaire); }
    @FXML private void retourListe()      { chargerTout(); showPage(pageListe); }
    @FXML private void appliquerFiltres() { filtrer(); }

    private void chargerTout() { tous = aboService.afficher(); filtrer(); }

    private double enMensuel(Abonnement a) {
        if (a.getFrequence() != null && a.getFrequence().toLowerCase().contains("ann"))
            return a.getPrix() / 12.0;
        return a.getPrix();
    }

    private void filtrer() {
        String r = txtRecherche != null ? txtRecherche.getText().toLowerCase().trim() : "";
        String c = comboFiltreCategorie != null ? comboFiltreCategorie.getValue() : null;
        String t = comboTri != null ? comboTri.getValue() : null;

        List<Abonnement> f = tous.stream()
                .filter(a -> r.isEmpty() || a.getNom().toLowerCase().contains(r)
                        || (a.getCategorie()!=null && a.getCategorie().toLowerCase().contains(r)))
                .filter(a -> c == null || "Toutes".equals(c) || c.equals(a.getCategorie()))
                .collect(Collectors.toList());

        if      ("Nom A\u2192Z".equals(t))    f.sort(Comparator.comparing(Abonnement::getNom, String.CASE_INSENSITIVE_ORDER));
        else if ("Nom Z\u2192A".equals(t))    f.sort(Comparator.comparing(Abonnement::getNom, String.CASE_INSENSITIVE_ORDER).reversed());
        else if ("Prix croissant".equals(t))   f.sort(Comparator.comparingDouble(Abonnement::getPrix));
        else if ("Prix decroissant".equals(t)) f.sort(Comparator.comparingDouble(Abonnement::getPrix).reversed());
        else                                    f.sort(Comparator.comparingDouble(Abonnement::getPrix).reversed());

        afficherListe(f);
        lblTotalMensuel.setText(String.format("%.3f TND", f.stream().mapToDouble(this::enMensuel).sum()));
    }

    private void afficherListe(List<Abonnement> liste) {
        listeServices.getChildren().clear();
        Map<String,List<Abonnement>> par = new LinkedHashMap<>();
        for (Abonnement a : liste)
            par.computeIfAbsent(a.getNom().trim().toLowerCase(), k -> new ArrayList<>()).add(a);

        if (par.isEmpty()) {
            Label vide = new Label("Aucun abonnement. Cliquez sur + Ajouter !");
            vide.setStyle("-fx-text-fill:#94a3b8;-fx-font-size:14px;-fx-padding:40 0;");
            listeServices.getChildren().add(vide);
            return;
        }

        int[] idx = {0};
        par.forEach((k, abos) -> {
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

    private HBox creerLigne(Abonnement rep, List<Abonnement> abos) {
        HBox ligne = new HBox(16); ligne.setAlignment(Pos.CENTER_LEFT);
        String base = "-fx-background-color:white;-fx-padding:18 24;-fx-background-radius:14;-fx-cursor:hand;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.05),8,0,0,2);";
        String hov  = "-fx-background-color:#f8faff;-fx-padding:18 24;-fx-background-radius:14;-fx-cursor:hand;-fx-border-color:#c7d7f0;-fx-border-width:1.5;-fx-border-radius:14;";
        ligne.setStyle(base);

        StackPane ico = creerIcone(rep.getNom());
        VBox infos = new VBox(4); HBox.setHgrow(infos, Priority.ALWAYS);
        Label nom = new Label(rep.getNom()); nom.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:#0f1f3d;");
        Label cat = new Label(rep.getCategorie()!=null?rep.getCategorie():""); cat.setStyle("-fx-background-color:#f1f5f9;-fx-text-fill:#64748b;-fx-font-size:10px;-fx-padding:3 10;-fx-background-radius:20;");
        long nb = abos.stream().flatMap(a->paieService.afficher().stream()
                .filter(p->p.getAbonnementId()==a.getId()&&"Paye".equals(p.getStatut()))).count();
        Label desc = new Label((rep.getFrequence()!=null?rep.getFrequence():"Mensuel")+"  \u2022  "+nb+" paie(s)");
        desc.setStyle("-fx-font-size:11px;-fx-text-fill:#94a3b8;");
        infos.getChildren().addAll(nom, cat, desc);

        VBox pb = new VBox(2); pb.setAlignment(Pos.CENTER_RIGHT);
        Label pr = new Label(String.format("%.2f TND", abos.stream().mapToDouble(this::enMensuel).sum()));
        pr.setStyle("-fx-font-size:18px;-fx-font-weight:bold;-fx-text-fill:#0f1f3d;");
        Label un = new Label("par mois"); un.setStyle("-fx-font-size:10px;-fx-text-fill:#94a3b8;");
        pb.getChildren().addAll(pr, un);

        Button bp = new Button("Payer");
        bp.setStyle("-fx-background-color:#1a3a7a;-fx-text-fill:white;-fx-font-size:12px;-fx-font-weight:bold;-fx-padding:9 22;-fx-background-radius:9;-fx-cursor:hand;");
        bp.setOnAction(e -> { e.consume(); ouvrirFormulairePaiement(abos.get(0)); });

        ligne.getChildren().addAll(ico, infos, pb, bp);
        ligne.setOnMouseEntered(e -> ligne.setStyle(hov));
        ligne.setOnMouseExited(e  -> ligne.setStyle(base));
        ligne.setOnMouseClicked(e -> ouvrirDetail(rep.getNom(), abos));
        return ligne;
    }

    private void ouvrirDetail(String nom, List<Abonnement> abos) {
        nomServiceSelectionne = nom;
        lblServiceNom.setText(nom);
        lblServiceSousTitre.setText(abos.size()+" abonnement(s)");
        statsService.getChildren().clear();
        double total = abos.stream().mapToDouble(this::enMensuel).sum();
        long nb = abos.stream().mapToLong(a->paieService.afficher().stream()
                .filter(p->p.getAbonnementId()==a.getId()).count()).sum();
        statsService.getChildren().addAll(
                kCard("Mensuel",     String.format("%.3f TND",total), "#1a3a7a","#e8f0ff"),
                kCard("Abonnements", String.valueOf(abos.size()),      "#27ae60","#e8fff4"),
                kCard("Paiements",   String.valueOf(nb),               "#6c5ce7","#f5f3ff"));
        listeAbonnementsService.getChildren().clear();
        aboService.afficher().stream()
                .filter(a->a.getNom().trim().equalsIgnoreCase(nom.trim()))
                .forEach(a->listeAbonnementsService.getChildren().add(creerCarteAbo(a)));
        showPage(pageDetailService);
    }

    private VBox creerCarteAbo(Abonnement a) {
        VBox c = new VBox(14);
        c.setStyle("-fx-background-color:white;-fx-padding:22 26;-fx-background-radius:16;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.07),12,0,0,4);");
        HBox top = new HBox(12); top.setAlignment(Pos.CENTER_LEFT);
        Label nm = new Label(a.getNom()+" \u2014 "+(a.getFrequence()!=null?a.getFrequence():"Mensuel"));
        nm.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:#0f1f3d;"); HBox.setHgrow(nm,Priority.ALWAYS);
        Label st = new Label(a.isActif()?"✔ Actif":"✘ Inactif");
        st.setStyle("-fx-background-color:"+(a.isActif()?"#e8fff4":"#fff0f0")+";-fx-text-fill:"+(a.isActif()?"#27ae60":"#e74c3c")+";-fx-font-size:11px;-fx-font-weight:bold;-fx-padding:5 14;-fx-background-radius:20;");
        top.getChildren().addAll(nm, st);
        HBox inf = new HBox(40);
        inf.getChildren().addAll(
                infoBox("PRIX",   String.format("%.3f TND", a.getPrix())),
                infoBox("DEBUT",  a.getDateDebut().toString()),
                infoBox("ANNUEL", String.format("%.3f TND", a.getPrix()*12)));
        HBox bts = new HBox(10);
        Button bp = btn("Payer",     "#1a3a7a","white");   bp.setOnAction(e->ouvrirFormulairePaiement(a));
        Button be = btn("Modifier",  "#f1f5f9","#475569"); be.setOnAction(e->dialogModifier(a));
        Button bd = btn("Supprimer", "#fff0f0","#e74c3c"); bd.setOnAction(e->supprimerAbo(a));
        bts.getChildren().addAll(bp, be, bd);
        c.getChildren().addAll(top, inf, bts);
        return c;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // AJOUTER — toast in-app + email dans 2 minutes
    // ══════════════════════════════════════════════════════════════════════════
    @FXML
    private void ajouter() {
        String nom = txtNom!=null ? txtNom.getText().trim() : "";
        if (nom.isEmpty()||comboCategorie.getValue()==null
                ||comboFrequence.getValue()==null||datePicker.getValue()==null) {
            alert(Alert.AlertType.WARNING,"Remplissez tous les champs !"); return;
        }
        try {
            double prix = PRIX_NORMAL;
            Abonnement n = new Abonnement(nom, prix, Date.valueOf(datePicker.getValue()),
                    comboFrequence.getValue(), comboCategorie.getValue(), true);
            n.setTier("Normal");
            aboService.ajouter(n);

            // ── Google Calendar : email dans 2 minutes ──
            new Thread(() -> calService.creerRappelExpiration(n)).start();

            // ── Toast in-app : dans 2 minutes = 120 secondes ──
            if (mainController != null) mainController.planifierToast(n, 120);

            alert(Alert.AlertType.INFORMATION,
                    "Abonnement « " + nom + " » ajouté !\n" +
                            "🔔 Notification + email dans 2 minutes.");
            retourListe();

        } catch (Exception ex) {
            alert(Alert.AlertType.ERROR,"Erreur lors de l'ajout !");
        }
    }

    private void viderFormulaire() {
        if (txtNom!=null) txtNom.clear();
        comboCategorie.setValue(null); comboFrequence.setValue(null); datePicker.setValue(null);
    }

    private void ouvrirFormulairePaiement(Abonnement a) {
        abonnementPourPaiement = a; tierSelectionnePaie = null;
        if (carteNormalPaie !=null) carteNormalPaie.setStyle(CN_OFF);
        if (cartePremiumPaie!=null) cartePremiumPaie.setStyle(CP_OFF);
        if (carteGoldPaie   !=null) carteGoldPaie.setStyle(CG_OFF);
        if (lblServicePaiement!=null) lblServicePaiement.setText(a.getNom());
        if (lblPrixPaiement   !=null) lblPrixPaiement.setText("---");
        if (lblTotalDue       !=null) lblTotalDue.setText("---");
        if (btnPayerDynamique !=null) btnPayerDynamique.setText("Confirmer le paiement");
        switch (a.getTier()!=null ? a.getTier() : "Normal") {
            case "Premium" -> selectionnerPremiumPaie();
            case "Gold"    -> selectionnerGoldPaie();
            default        -> selectionnerNormalPaie();
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
        if(carteNormalPaie !=null) carteNormalPaie.setStyle(CN_ON);
        if(cartePremiumPaie!=null) cartePremiumPaie.setStyle(CP_OFF);
        if(carteGoldPaie   !=null) carteGoldPaie.setStyle(CG_OFF);
        majPrix(PRIX_NORMAL,"Normal","#2980b9");
    }
    @FXML private void selectionnerPremiumPaie() {
        tierSelectionnePaie="Premium";
        if(carteNormalPaie !=null) carteNormalPaie.setStyle(CN_OFF);
        if(cartePremiumPaie!=null) cartePremiumPaie.setStyle(CP_ON);
        if(carteGoldPaie   !=null) carteGoldPaie.setStyle(CG_OFF);
        majPrix(PRIX_PREMIUM,"Premium","#6c5ce7");
    }
    @FXML private void selectionnerGoldPaie() {
        tierSelectionnePaie="Gold";
        if(carteNormalPaie !=null) carteNormalPaie.setStyle(CN_OFF);
        if(cartePremiumPaie!=null) cartePremiumPaie.setStyle(CP_OFF);
        if(carteGoldPaie   !=null) carteGoldPaie.setStyle(CG_ON);
        majPrix(PRIX_GOLD,"Gold","#d97706");
    }

    private void majPrix(double prix, String tier, String col) {
        String f = String.format("%.3f TND", prix);
        if (lblPrixPaiement   !=null) lblPrixPaiement.setText(f);
        if (lblTotalDue       !=null) lblTotalDue.setText(f);
        if (lblNomTierPaiement!=null) {
            lblNomTierPaiement.setText("Plan "+tier+" selectionne");
            lblNomTierPaiement.setStyle("-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:"+col+";-fx-padding:0 18 14 18;");
        }
        if (btnPayerDynamique!=null) btnPayerDynamique.setText("Payer "+tier+" \u2014 "+f);
    }

    private void majCarteVisa() {
        if (lblCarteNumero!=null) {
            String r = txtNumeroCarte!=null ? txtNumeroCarte.getText().replaceAll("[^0-9]","") : "";
            lblCarteNumero.setText(r.length()>=4
                    ?"\u25CF\u25CF\u25CF\u25CF  \u25CF\u25CF\u25CF\u25CF  \u25CF\u25CF\u25CF\u25CF  "+r.substring(r.length()-4)
                    :"\u25CF\u25CF\u25CF\u25CF  \u25CF\u25CF\u25CF\u25CF  \u25CF\u25CF\u25CF\u25CF  \u25CF\u25CF\u25CF\u25CF");
        }
        if (lblCarteTitulaire!=null) {
            String p=txtPrenomPaiement!=null?txtPrenomPaiement.getText():"";
            String nv=txtNomPaiement!=null?txtNomPaiement.getText():"";
            String t=(p+" "+nv).trim().toUpperCase();
            lblCarteTitulaire.setText(t.isEmpty()?"VOTRE NOM":t);
        }
        if (lblCarteExpiry!=null) {
            String e=txtDateExpiration!=null?txtDateExpiration.getText():"";
            lblCarteExpiry.setText(e.isEmpty()?"MM/YY":e);
        }
    }

    @FXML
    private void confirmerPaiement() {
        StripeController.getInstance().lancerPaiement(
                abonnementPourPaiement,
                prixParTier(tierSelectionnePaie),
                tierSelectionnePaie,
                txtNomPaiement.getText(),
                txtPrenomPaiement.getText(),
                txtNumeroCarte.getText(),
                txtDateExpiration.getText(),
                txtCvv.getText(),
                btnPayerDynamique,
                new StripeController.OnPaiementTermine() {
                    @Override public void onSucces(String stripeId) {
                        if (mainController!=null) mainController.switchPaiements();
                        else retourListe();
                    }
                    @Override public void onEchec(String msg) {
                        System.err.println("Echec paiement : " + msg);
                    }
                });
    }

    @FXML
    private void annulerPaiement() {
        if (abonnementPourPaiement!=null) {
            List<Abonnement> l=aboService.afficher().stream()
                    .filter(a->a.getId()==abonnementPourPaiement.getId()).toList();
            if (!l.isEmpty()) { ouvrirDetail(abonnementPourPaiement.getNom(),l); return; }
        }
        retourListe();
    }

    private void dialogModifier(Abonnement a) {
        Dialog<ButtonType> dlg=new Dialog<>(); dlg.setTitle(null); dlg.setHeaderText(null);
        VBox root=new VBox(0); root.setPrefWidth(460);
        VBox hdr=new VBox(5); hdr.setStyle("-fx-background-color:#0f1f3d;-fx-padding:22 28;-fx-background-radius:14 14 0 0;");
        Label th=new Label("Modifier"); th.setStyle("-fx-font-size:17px;-fx-font-weight:bold;-fx-text-fill:white;");
        Label ts=new Label(a.getNom()); ts.setStyle("-fx-font-size:11px;-fx-text-fill:#6a8aaa;");
        hdr.getChildren().addAll(th,ts);
        VBox body=new VBox(16); body.setStyle("-fx-background-color:white;-fx-padding:24 28;-fx-background-radius:0 0 14 14;");
        TextField fn=new TextField(a.getNom()); styleTf(fn);
        TextField fp=new TextField(String.format("%.3f",a.getPrix())); styleTf(fp);
        ComboBox<String> ff=new ComboBox<>(); ff.setItems(FXCollections.observableArrayList("Mensuel","Annuel")); ff.setValue(a.getFrequence()); ff.setMaxWidth(Double.MAX_VALUE);
        ToggleGroup tg=new ToggleGroup();
        RadioButton rA=new RadioButton("Actif"),rI=new RadioButton("Inactif");
        rA.setToggleGroup(tg); rI.setToggleGroup(tg); rA.setSelected(a.isActif()); rI.setSelected(!a.isActif());
        Button bC=btn("Annuler","white","#64748b"), bS=btn("Enregistrer","#1a3a7a","white");
        HBox bx=new HBox(10,bC,bS); bx.setAlignment(Pos.CENTER_RIGHT);
        body.getChildren().addAll(rowForm(lbl("Nom"),fn,lbl("Prix (TND)"),fp),rowForm(lbl("Frequence"),ff,lbl("Statut"),new HBox(10,rA,rI)),new Separator(),bx);
        root.getChildren().addAll(hdr,body);
        dlg.getDialogPane().setContent(root); dlg.getDialogPane().getButtonTypes().add(ButtonType.CLOSE); dlg.getDialogPane().lookupButton(ButtonType.CLOSE).setVisible(false);
        bC.setOnAction(e->dlg.close());
        bS.setOnAction(e->{
            try {
                a.setNom(fn.getText()); a.setPrix(Double.parseDouble(fp.getText().replace(",",".")));
                a.setFrequence(ff.getValue()); a.setActif(rA.isSelected());
                aboService.modifier(a); dlg.close(); alert(Alert.AlertType.INFORMATION,"Modifie !");
                List<Abonnement> m=aboService.afficher().stream()
                        .filter(ab->ab.getNom().trim().equalsIgnoreCase(a.getNom().trim())).toList();
                ouvrirDetail(a.getNom(),m);
            } catch (Exception ex) { alert(Alert.AlertType.ERROR,"Erreur !"); }
        });
        dlg.showAndWait();
    }

    private void supprimerAbo(Abonnement a) {
        Alert c=new Alert(Alert.AlertType.CONFIRMATION); c.setHeaderText(null);
        c.setContentText("Supprimer \u00ab "+a.getNom()+" \u00bb ?");
        c.showAndWait().ifPresent(b->{
            if (b==ButtonType.OK) {
                aboService.supprimer(a.getId());
                List<Abonnement> r=aboService.afficher().stream()
                        .filter(ab->ab.getNom().trim().equalsIgnoreCase(nomServiceSelectionne.trim())).toList();
                if (r.isEmpty()) retourListe(); else ouvrirDetail(nomServiceSelectionne,r);
            }
        });
    }

    @FXML public void ouvrirStatistiques() {
        showPage(pageStatistiques);
        PauseTransition pt = new PauseTransition(Duration.millis(80));
        pt.setOnFinished(e -> construireStats());
        pt.play();
    }

    private void construireStats() {
        List<Abonnement> l  = aboService.afficher();
        List<Paiement>   ps = paieService.afficher();
        double tM=l.stream().mapToDouble(this::enMensuel).sum(), tA=tM*12;
        long nA=l.stream().filter(Abonnement::isActif).count();
        long nP=ps.stream().filter(p->"Paye".equals(p.getStatut())).count();
        double vP=ps.stream().filter(p->"Paye".equals(p.getStatut())).mapToDouble(Paiement::getMontant).sum();

        statsKpiRow.getChildren().clear();
        Object[][] kpis = {
                {"💰","Total mensuel",   tM,        "TND","#1a3a7a","#e8f0ff"},
                {"📅","Total annuel",    tA,        "TND","#6c5ce7","#f0ecff"},
                {"✅","Services actifs", (double)nA, "",  "#27ae60","#e8fff4"},
                {"💳","Paiements",       (double)nP, "",  "#e67e22","#fff3e0"},
                {"📊","Volume paye",     vP,        "TND","#e74c3c","#fff0f0"}
        };
        for (int i = 0; i < kpis.length; i++) {
            Object[] k = kpis[i];
            double   fV = (double) k[2];
            String   fU = (String) k[3];
            VBox card = kpiCard((String)k[0],(String)k[1],fV,fU,(String)k[4],(String)k[5]);
            HBox.setHgrow(card, Priority.ALWAYS);
            card.setOpacity(0); card.setTranslateY(24);
            statsKpiRow.getChildren().add(card);
            final VBox fc = card;
            PauseTransition d = new PauseTransition(Duration.millis(i * 90));
            d.setOnFinished(ev -> {
                FadeTransition ft = new FadeTransition(Duration.millis(350), fc);
                ft.setFromValue(0); ft.setToValue(1);
                TranslateTransition tt = new TranslateTransition(Duration.millis(350), fc);
                tt.setFromY(24); tt.setToY(0); tt.setInterpolator(Interpolator.EASE_OUT);
                new ParallelTransition(ft, tt).play();
                animCount(fc, fV, fU);
            });
            d.play();
        }

        conteneurBarres.getChildren().clear();
        Map<String,Double> pc = l.stream().collect(Collectors.groupingBy(
                a -> a.getCategorie()!=null ? a.getCategorie() : "Autre",
                Collectors.summingDouble(this::enMensuel)));
        if (!pc.isEmpty()) {
            double mx = pc.values().stream().mapToDouble(d->d).max().orElse(1);
            int[] ri = {0};
            pc.entrySet().stream()
                    .sorted((e1,e2)->Double.compare(e2.getValue(),e1.getValue()))
                    .forEach(e -> {
                        String col = COULEURS[ri[0] % COULEURS.length];
                        HBox bar = new HBox(14); bar.setAlignment(Pos.CENTER_LEFT);
                        Label lc = new Label(e.getKey()); lc.setPrefWidth(100);
                        lc.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:#334155;");
                        StackPane ct = new StackPane(); ct.setAlignment(Pos.CENTER_LEFT);
                        HBox.setHgrow(ct, Priority.ALWAYS);
                        Rectangle bg = new Rectangle(0,22); bg.setFill(Color.web("#f1f5f9")); bg.setArcWidth(11); bg.setArcHeight(11);
                        Rectangle fl = new Rectangle(0,22); fl.setFill(Color.web(col)); fl.setArcWidth(11); fl.setArcHeight(11);
                        ct.getChildren().addAll(bg, fl);
                        final double rt = e.getValue() / mx;
                        ct.widthProperty().addListener((o,v,n) -> bg.setWidth(n.doubleValue()));
                        Label vl = new Label(String.format("%.2f TND", e.getValue()));
                        vl.setPrefWidth(100); vl.setStyle("-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:"+col+";");
                        bar.getChildren().addAll(lc, ct, vl);
                        bar.setOpacity(0);
                        conteneurBarres.getChildren().add(bar);
                        final int rr = ri[0];
                        PauseTransition d2 = new PauseTransition(Duration.millis(200 + rr * 110));
                        d2.setOnFinished(ev -> {
                            FadeTransition ft2 = new FadeTransition(Duration.millis(280), bar);
                            ft2.setFromValue(0); ft2.setToValue(1); ft2.play();
                            double cw = ct.getWidth() > 0 ? ct.getWidth() : 400;
                            bg.setWidth(cw); fl.setWidth(0);
                            new Timeline(new KeyFrame(Duration.millis(650),
                                    new KeyValue(fl.widthProperty(), cw*rt, Interpolator.EASE_OUT))).play();
                        });
                        d2.play();
                        ri[0]++;
                    });
        } else {
            conteneurBarres.getChildren().add(new Label("Aucune donnee"));
        }

        conteneurTop.getChildren().clear();
        List<Abonnement> t5 = l.stream()
                .sorted((a,b)->Double.compare(enMensuel(b),enMensuel(a)))
                .limit(5).collect(Collectors.toList());
        for (int i = 0; i < t5.size(); i++) {
            Abonnement a = t5.get(i); String col = COULEURS[i % COULEURS.length];
            HBox rh = new HBox(12); rh.setAlignment(Pos.CENTER_LEFT);
            rh.setStyle("-fx-padding:8 0;-fx-border-color:#f1f5f9;-fx-border-width:0 0 1 0;");
            Label rg = new Label("#"+(i+1)); rg.setPrefWidth(40);
            rg.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:"+col+";-fx-background-color:"+col+"22;-fx-padding:4 10;-fx-background-radius:8;");
            VBox inf = new VBox(2); HBox.setHgrow(inf, Priority.ALWAYS);
            Label nL = new Label(a.getNom()); nL.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#0f1f3d;");
            Label cL = new Label(a.getCategorie()!=null?a.getCategorie():""); cL.setStyle("-fx-font-size:10px;-fx-text-fill:#94a3b8;");
            inf.getChildren().addAll(nL, cL);
            Label pl = new Label(String.format("%.2f TND", enMensuel(a)));
            pl.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:"+col+";");
            rh.getChildren().addAll(rg, creerIcone(a.getNom()), inf, pl);
            rh.setOpacity(0); rh.setTranslateX(-16);
            conteneurTop.getChildren().add(rh);
            final HBox fr = rh; final int fi = i;
            PauseTransition d3 = new PauseTransition(Duration.millis(280 + fi * 80));
            d3.setOnFinished(ev -> {
                FadeTransition ft3 = new FadeTransition(Duration.millis(280), fr);
                ft3.setFromValue(0); ft3.setToValue(1);
                TranslateTransition tt3 = new TranslateTransition(Duration.millis(280), fr);
                tt3.setFromX(-16); tt3.setToX(0); tt3.setInterpolator(Interpolator.EASE_OUT);
                new ParallelTransition(ft3, tt3).play();
            });
            d3.play();
        }
        if (t5.isEmpty()) conteneurTop.getChildren().add(new Label("Aucun abonnement"));

        conteneurHistorique.getChildren().clear();
        List<Paiement> recents = ps.stream()
                .sorted((a,b)->b.getDatePaiement().compareTo(a.getDatePaiement()))
                .limit(6).collect(Collectors.toList());
        for (int i = 0; i < recents.size(); i++) {
            Paiement p = recents.get(i);
            boolean py = "Paye".equals(p.getStatut());
            String nm = l.stream().filter(a->a.getId()==p.getAbonnementId())
                    .map(Abonnement::getNom).findFirst().orElse("Service");
            HBox rh = new HBox(10); rh.setAlignment(Pos.CENTER_LEFT);
            rh.setStyle("-fx-background-color:"+(py?"#f0fdf4":"#fff7ed")+";-fx-padding:10 12;-fx-background-radius:10;");
            Label ic = new Label(py?"✓":"⏳");
            ic.setStyle("-fx-font-size:13px;-fx-text-fill:"+(py?"#27ae60":"#e67e22")+";-fx-background-color:"+(py?"#dcfce7":"#ffedd5")+";-fx-background-radius:50;-fx-padding:4 7;");
            VBox ri = new VBox(2); HBox.setHgrow(ri, Priority.ALWAYS);
            Label nL = new Label(nm); nL.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:#334155;");
            Label dL = new Label(p.getDatePaiement().toString()); dL.setStyle("-fx-font-size:10px;-fx-text-fill:#94a3b8;");
            ri.getChildren().addAll(nL, dL);
            Label mt = new Label(String.format("%.2f", p.getMontant()));
            mt.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:"+(py?"#27ae60":"#e67e22")+";");
            rh.getChildren().addAll(ic, ri, mt);
            rh.setOpacity(0);
            conteneurHistorique.getChildren().add(rh);
            final HBox fr2 = rh; final int fi2 = i;
            PauseTransition d4 = new PauseTransition(Duration.millis(350 + fi2 * 60));
            d4.setOnFinished(ev -> {
                FadeTransition ft4 = new FadeTransition(Duration.millis(260), fr2);
                ft4.setFromValue(0); ft4.setToValue(1); ft4.play();
            });
            d4.play();
        }
        if (ps.isEmpty()) conteneurHistorique.getChildren().add(new Label("Aucun paiement"));
    }

    private VBox kpiCard(String icon, String lbl, double val, String unit, String col, String bg) {
        VBox c = new VBox(6); c.setAlignment(Pos.TOP_LEFT);
        c.setStyle("-fx-background-color:"+bg+";-fx-padding:20 22;-fx-background-radius:16;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),10,0,0,3);");
        Label i = new Label(icon); i.setStyle("-fx-font-size:22px;");
        Label lb= new Label(lbl);  lb.setStyle("-fx-font-size:10px;-fx-font-weight:bold;-fx-text-fill:#64748b;");
        Label v = new Label("0");  v.setStyle("-fx-font-size:24px;-fx-font-weight:bold;-fx-text-fill:"+col+";"); v.setUserData(val);
        Label u = new Label(unit); u.setStyle("-fx-font-size:10px;-fx-text-fill:#94a3b8;");
        c.getChildren().addAll(i, lb, v, u);
        return c;
    }

    private void animCount(VBox card, double target, String unit) {
        card.getChildren().stream()
                .filter(n -> n instanceof Label && n.getUserData() instanceof Double)
                .map(n -> (Label)n).findFirst().ifPresent(vl -> {
                    DoubleProperty dp = new SimpleDoubleProperty(0);
                    dp.addListener((o,ov,nv) -> vl.setText(
                            "TND".equals(unit) ? String.format("%.2f",nv.doubleValue())
                                    : String.format("%.0f",nv.doubleValue())));
                    new Timeline(new KeyFrame(Duration.millis(900),
                            new KeyValue(dp, target, Interpolator.EASE_OUT))).play();
                });
    }

    private double    prixParTier(String t)    { return switch(t!=null?t:"Normal"){case"Premium"->PRIX_PREMIUM;case"Gold"->PRIX_GOLD;default->PRIX_NORMAL;}; }
    private Label     lbl(String t)            { Label l=new Label(t); l.setStyle("-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:#475569;"); return l; }
    private void      styleTf(TextField tf)    { tf.setStyle("-fx-background-radius:9;-fx-border-radius:9;-fx-border-color:#e2e8f0;-fx-border-width:1.5;-fx-padding:11 14;-fx-font-size:13px;"); }
    private void      alert(Alert.AlertType t, String m) { Alert a=new Alert(t); a.setHeaderText(null); a.setContentText(m); a.showAndWait(); }
    private VBox      kCard(String lb, String v, String c, String b) { VBox box=new VBox(5); box.setPrefWidth(180); box.setStyle("-fx-background-color:"+b+";-fx-padding:16 20;-fx-background-radius:14;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.05),8,0,0,2);"); Label ll=new Label(lb); ll.setStyle("-fx-font-size:10px;-fx-text-fill:#64748b;-fx-font-weight:bold;"); Label vl=new Label(v); vl.setStyle("-fx-font-size:18px;-fx-font-weight:bold;-fx-text-fill:"+c+";"); box.getChildren().addAll(ll,vl); return box; }
    private VBox      infoBox(String lb, String v) { VBox b=new VBox(3); Label ll=new Label(lb); ll.setStyle("-fx-text-fill:#94a3b8;-fx-font-size:9px;-fx-font-weight:bold;"); Label vl=new Label(v); vl.setStyle("-fx-text-fill:#0f1f3d;-fx-font-size:13px;-fx-font-weight:bold;"); b.getChildren().addAll(ll,vl); return b; }
    private Button    btn(String t, String bg, String fg) { Button b=new Button(t); b.setStyle("-fx-background-color:"+bg+";-fx-text-fill:"+fg+";-fx-font-size:12px;-fx-font-weight:bold;-fx-padding:9 20;-fx-background-radius:9;-fx-cursor:hand;"); return b; }
    private HBox      rowForm(Label l1, javafx.scene.Node n1, Label l2, javafx.scene.Node n2) { VBox v1=new VBox(6,l1,n1), v2=new VBox(6,l2,n2); HBox.setHgrow(v1,Priority.ALWAYS); HBox.setHgrow(v2,Priority.ALWAYS); return new HBox(14,v1,v2); }
    private StackPane creerIcone(String n) { Circle c=new Circle(24); c.setFill(Color.web(couleur(n))); Label lb=new Label(logo(n)); lb.setStyle("-fx-font-size:12px;-fx-text-fill:white;-fx-font-weight:bold;"); return new StackPane(c,lb); }
    private String    couleur(String n) { String s=n.toLowerCase(); if(s.contains("netflix"))return"#E50914"; if(s.contains("spotify"))return"#1DB954"; if(s.contains("amazon"))return"#FF9900"; if(s.contains("disney"))return"#113CCF"; if(s.contains("youtube"))return"#FF0000"; if(s.contains("deezer"))return"#A238FF"; if(s.contains("microsoft"))return"#0078D4"; if(s.contains("google"))return"#4285F4"; if(s.contains("adobe"))return"#FF4444"; return"#1a3a7a"; }
    private String    logo(String n) { String s=n.toLowerCase(); if(s.contains("netflix"))return"N"; if(s.contains("spotify"))return"S"; if(s.contains("amazon"))return"a"; if(s.contains("disney"))return"D+"; if(s.contains("youtube"))return"Y"; if(s.contains("deezer"))return"Dz"; if(s.contains("microsoft"))return"M"; if(s.contains("google"))return"G"; if(s.contains("adobe"))return"Ai"; return n.substring(0,Math.min(2,n.length())).toUpperCase(); }
    @FXML private void ouvrirPaiements() { if(mainController!=null) mainController.switchPaiements(); }
}