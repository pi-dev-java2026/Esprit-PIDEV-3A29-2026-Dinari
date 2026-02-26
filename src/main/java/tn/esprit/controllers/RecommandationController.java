package tn.esprit.controllers;

import javafx.animation.*;
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
import tn.esprit.services.GeminiService;
import tn.esprit.services.PaiementService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RecommandationController {

    @FXML private TextField txtBudget;
    @FXML private Button btnAnalyser, btnNouvelleAnalyse;
    @FXML private VBox zoneBudget, conteneurResultat;
    @FXML private Label lblStatut, lblSousTitre;
    @FXML private HBox kpiRow;
    @FXML private ScrollPane scrollResultat;
    @FXML private ProgressIndicator spinner;

    private MainController mainController;
    private final AbonnementService aboService = new AbonnementService();
    private final PaiementService paieService  = new PaiementService();
    private static final String[] CLR = {"#1a3a7a","#6c5ce7","#27ae60","#e67e22","#e74c3c","#3498db"};

    public void setMainController(MainController mc) { this.mainController = mc; }

    @FXML public void initialize() {
        setV(spinner,false); setV(scrollResultat,false); setV(kpiRow,false); setV(btnNouvelleAnalyse,false);
        if (txtBudget!=null) txtBudget.setOnAction(e->analyser());
        statut("Entrez votre budget et cliquez sur Analyser","#94a3b8");
    }

    @FXML public void nouvelleAnalyse() {
        setV(scrollResultat,false); setV(kpiRow,false); setV(btnNouvelleAnalyse,false);
        conteneurResultat.getChildren().clear();
        show(zoneBudget,true);
        if (txtBudget!=null) { txtBudget.clear(); txtBudget.requestFocus(); }
        statut("Entrez votre budget et cliquez sur Analyser","#94a3b8");
    }

    @FXML public void analyser() {
        double budget = parseBudget(); if (budget<=0) return;
        List<Abonnement> abos = aboService.afficher();
        List<Paiement> paies  = paieService.afficher();
        if (abos.isEmpty()) { statut("Aucun abonnement trouve !","#e74c3c"); return; }
        setLoad(true); statut("L'IA analyse vos donnees...","#6c5ce7");
        new Thread(() -> {
            String rep = GeminiService.envoyerPrompt(prompt(abos,paies,budget));
            javafx.application.Platform.runLater(() -> {
                setLoad(false); show(zoneBudget,false);
                buildUI(abos,paies,budget,rep);
                setV(btnNouvelleAnalyse,true);
                if (lblSousTitre!=null) lblSousTitre.setText("Budget : "+String.format("%.0f",budget)+" TND/mois");
            });
        }).start();
    }

    // ── UI ────────────────────────────────────────────────────────────────
    private void buildUI(List<Abonnement> abos, List<Paiement> paies, double budget, String ia) {
        conteneurResultat.getChildren().clear();
        double total = abos.stream().mapToDouble(this::mensuel).sum();
        int score = score(budget,total);

        kpiRow.getChildren().setAll(
                kpi("Budget",f(budget),"#1a3a7a","#e8f0ff"), kpi("Depenses",f(total),"#e74c3c","#fff0f0"),
                kpi("Reste",f(Math.abs(budget-total)),total<=budget?"#27ae60":"#e74c3c",total<=budget?"#e8fff4":"#fff0f0"),
                kpi("Abonnements",String.valueOf(abos.size()),"#6c5ce7","#f5f3ff"));
        setV(kpiRow,true); animer(kpiRow.getChildren());

        conteneurResultat.getChildren().addAll(scoreSection(score,budget,total), iaSection(ia), abosSection(abos,paies,budget), catsSection(abos));
        setV(scrollResultat,true); fade(scrollResultat,0,1,400);
    }

    private VBox scoreSection(int s, double budget, double total) {
        StackPane cercle = new StackPane();
        cercle.setPrefSize(120,120);
        Circle fond=new Circle(55); fond.setFill(Color.web(cS(s)+"22"));
        Circle bord=new Circle(55); bord.setFill(Color.TRANSPARENT); bord.setStroke(Color.web(cS(s))); bord.setStrokeWidth(7);
        Label num=new Label(s+"/10"); num.setStyle("-fx-font-size:26px;-fx-font-weight:bold;-fx-text-fill:"+cS(s)+";");
        cercle.getChildren().addAll(fond,bord,num);
        ScaleTransition sc=new ScaleTransition(Duration.millis(600),cercle);
        sc.setFromX(0.3); sc.setFromY(0.3); sc.setToX(1); sc.setToY(1); sc.setInterpolator(Interpolator.EASE_OUT); sc.play();
        VBox infos=new VBox(10,row("Budget",f(budget),"#1a3a7a"),row("Depenses",f(total),"#e74c3c"),
                row("Statut",lS(s),cS(s)),row("Utilisation",String.format("%.0f%%",Math.min(total/budget,1)*100),cS(s)));
        HBox c=new HBox(40,cercle,infos); c.setAlignment(Pos.CENTER);
        return card("Score de Sante Financiere",c);
    }

    private VBox iaSection(String rep) {
        Label txt=new Label(rep); txt.setWrapText(true);
        txt.setStyle("-fx-font-size:13px;-fx-text-fill:#334155;-fx-line-spacing:5;-fx-background-color:#f8fafc;-fx-padding:18;-fx-background-radius:12;");
        fade(txt,0,1,700);
        VBox v=card("Analyse Groq AI — Llama 3.3 70B",txt);
        v.setStyle(v.getStyle()+"-fx-border-color:#6c5ce7;-fx-border-width:0 0 0 5;-fx-border-radius:0 20 20 0;");
        return v;
    }

    private VBox abosSection(List<Abonnement> abos, List<Paiement> paies, double budget) {
        VBox section=card("Plan optimal par abonnement",null);
        for (int i=0;i<abos.size();i++) {
            Abonnement a=abos.get(i); String col=CLR[i%CLR.length], plan=planSuggere(budget,abos.size());
            long nb=paies.stream().filter(p->p.getAbonnementId()==a.getId()).count();
            Circle c=new Circle(22); c.setFill(Color.web(col));
            Label ico=new Label(a.getNom().substring(0,Math.min(2,a.getNom().length())).toUpperCase());
            ico.setStyle("-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:white;");
            VBox inf=new VBox(2,lbl(a.getNom(),"-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:#0f1f3d;"),
                    lbl((a.getCategorie()!=null?a.getCategorie():"Autre")+" • "+nb+" paiement(s)","-fx-font-size:10px;-fx-text-fill:#94a3b8;"));
            HBox.setHgrow(inf,Priority.ALWAYS);
            Label pl=new Label(plan); pl.setStyle("-fx-background-color:"+cP(plan)+"22;-fx-text-fill:"+cP(plan)+";-fx-font-size:11px;-fx-font-weight:bold;-fx-padding:6 14;-fx-background-radius:20;");
            HBox ligne=new HBox(12,new StackPane(c,ico),inf,lbl(String.format("%.0f TND/mois",mensuel(a)),"-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#64748b;"),pl);
            ligne.setAlignment(Pos.CENTER_LEFT); ligne.setStyle("-fx-background-color:#f8fafc;-fx-padding:14 18;-fx-background-radius:12;"); ligne.setOpacity(0);
            section.getChildren().add(ligne);
            final HBox fl=ligne; final int fi=i;
            PauseTransition pt=new PauseTransition(Duration.millis(300+fi*100));
            pt.setOnFinished(ev->fade(fl,0,1,300)); pt.play();
        }
        return section;
    }

    private VBox catsSection(List<Abonnement> abos) {
        VBox section=card("Depenses par categorie",null);
        Map<String,Double> map=abos.stream().collect(Collectors.groupingBy(
                a->a.getCategorie()!=null?a.getCategorie():"Autre",Collectors.summingDouble(this::mensuel)));
        double max=map.values().stream().mapToDouble(d->d).max().orElse(1);
        int[] ri={0};
        map.entrySet().stream().sorted((e1,e2)->Double.compare(e2.getValue(),e1.getValue())).forEach(e->{
            String col=CLR[ri[0]%CLR.length];
            Rectangle bg=new Rectangle(0,16); bg.setFill(Color.web("#f1f5f9")); bg.setArcWidth(8); bg.setArcHeight(8);
            Rectangle fl=new Rectangle(0,16); fl.setFill(Color.web(col)); fl.setArcWidth(8); fl.setArcHeight(8);
            StackPane barre=new StackPane(bg,fl); barre.setAlignment(Pos.CENTER_LEFT); HBox.setHgrow(barre,Priority.ALWAYS);
            final double ratio=e.getValue()/max;
            barre.widthProperty().addListener((o,v,n)->{bg.setWidth(n.doubleValue());
                new Timeline(new KeyFrame(Duration.millis(700),new KeyValue(fl.widthProperty(),n.doubleValue()*ratio,Interpolator.EASE_OUT))).play();});
            Label lc=new Label(e.getKey()); lc.setPrefWidth(110); lc.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:#334155;");
            Label vl=new Label(String.format("%.0f TND",e.getValue())); vl.setPrefWidth(80); vl.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:"+col+";");
            HBox bar=new HBox(12,lc,barre,vl); bar.setAlignment(Pos.CENTER_LEFT); bar.setOpacity(0);
            section.getChildren().add(bar);
            PauseTransition pt=new PauseTransition(Duration.millis(200+ri[0]*80));
            pt.setOnFinished(ev->fade(bar,0,1,280)); pt.play(); ri[0]++;
        });
        return section;
    }

    // ── PROMPT ────────────────────────────────────────────────────────────
    private String prompt(List<Abonnement> abos, List<Paiement> paies, double budget) {
        double total=abos.stream().mapToDouble(this::mensuel).sum();
        double paye=paies.stream().filter(p->"Paye".equals(p.getStatut())).mapToDouble(Paiement::getMontant).sum();
        StringBuilder sb=new StringBuilder("Tu es conseiller financier. Reponds en francais, sans markdown, sans **.\n\n" +
                "Budget: "+String.format("%.0f",budget)+" TND | Depenses: "+String.format("%.0f",total)+" TND/mois | Total paye: "+String.format("%.0f",paye)+" TND\n\nAbonnements:\n");
        for (int i=0;i<abos.size();i++) { Abonnement a=abos.get(i);
            sb.append(i+1).append(". ").append(a.getNom()).append(" | ").append(String.format("%.0f",mensuel(a))).append(" TND/mois | Plan: ").append(a.getTier()!=null?a.getTier():"Normal").append("\n"); }
        sb.append("\nPlans: Normal=15 TND | Premium=40 TND | Gold=80 TND\n\nReponds avec:\nSCORE: [une phrase]\nALERTE: [quoi couper ou: Situation saine]\nRECOMMANDATIONS:\n- [Nom]: [plan] car [raison]\nECONOMIES: [montant TND]\nCONSEIL: [un conseil]\n");
        return sb.toString();
    }

    // ── HELPERS ───────────────────────────────────────────────────────────
    private VBox card(String t, javafx.scene.Node n) {
        VBox v=new VBox(14); v.setStyle("-fx-background-color:white;-fx-padding:26 30;-fx-background-radius:18;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.08),14,0,0,4);");
        Label tl=new Label(t); tl.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:#0f1f3d;");
        v.getChildren().addAll(tl,new Separator()); if(n!=null)v.getChildren().add(n); return v;
    }
    private VBox kpi(String l,String v,String c,String b) {
        VBox box=new VBox(5); box.setAlignment(Pos.CENTER); HBox.setHgrow(box,Priority.ALWAYS);
        box.setStyle("-fx-background-color:"+b+";-fx-padding:18 14;-fx-background-radius:14;");
        box.getChildren().addAll(lbl(l,"-fx-font-size:10px;-fx-font-weight:bold;-fx-text-fill:#64748b;"),lbl(v,"-fx-font-size:20px;-fx-font-weight:bold;-fx-text-fill:"+c+";")); return box;
    }
    private HBox row(String k,String v,String c) {
        Label lk=new Label(k+" :"); lk.setPrefWidth(110); lk.setStyle("-fx-font-size:12px;-fx-text-fill:#64748b;");
        Label lv=new Label(v); lv.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:"+c+";");
        return new HBox(8,lk,lv);
    }
    private Label lbl(String t,String s) { Label l=new Label(t); l.setStyle(s); return l; }
    private String f(double v)            { return String.format("%.0f TND",v); }
    private double mensuel(Abonnement a)  { return "Annuel".equalsIgnoreCase(a.getFrequence())?a.getPrix()/12:a.getPrix(); }
    private int score(double b,double t)  { if(b<=0)return 5;double r=t/b;if(r<=0.2)return 10;if(r<=0.3)return 9;if(r<=0.4)return 8;if(r<=0.5)return 7;if(r<=0.6)return 6;if(r<=0.75)return 5;if(r<=0.9)return 4;if(r<=1.0)return 3;if(r<=1.2)return 2;return 1; }
    private String cS(int s)              { if(s>=8)return"#27ae60";if(s>=6)return"#f39c12";if(s>=4)return"#e67e22";return"#e74c3c"; }
    private String lS(int s)              { if(s>=8)return"Excellent";if(s>=6)return"Bon";if(s>=4)return"Attention";return"Critique"; }
    private String planSuggere(double b,int n) { double r=n>0?b/n:b;if(r>=70)return"Gold";if(r>=35)return"Premium";return"Normal"; }
    private String cP(String p)           { return switch(p!=null?p:"Normal"){case"Gold"->"#d97706";case"Premium"->"#6c5ce7";default->"#2980b9";}; }
    private void fade(javafx.scene.Node n,double f,double t,int ms) { FadeTransition ft=new FadeTransition(Duration.millis(ms),n);ft.setFromValue(f);ft.setToValue(t);ft.play(); }
    private void animer(javafx.collections.ObservableList<javafx.scene.Node> nodes) {
        for(int i=0;i<nodes.size();i++){javafx.scene.Node n=nodes.get(i);n.setOpacity(0);n.setTranslateY(16);final int fi=i;
            PauseTransition pt=new PauseTransition(Duration.millis(fi*100));
            pt.setOnFinished(ev->{fade(n,0,1,300);TranslateTransition tt=new TranslateTransition(Duration.millis(300),n);tt.setFromY(16);tt.setToY(0);tt.setInterpolator(Interpolator.EASE_OUT);tt.play();});pt.play();}
    }
    private void setV(javafx.scene.Node n,boolean v) { if(n!=null){n.setVisible(v);n.setManaged(v);} }
    private void show(javafx.scene.Node n,boolean show) {
        if(n==null)return;
        if(show){setV(n,true);fade(n,0,1,300);}
        else{fade(n,1,0,280);PauseTransition pt=new PauseTransition(Duration.millis(290));pt.setOnFinished(ev->setV(n,false));pt.play();}
    }
    private void statut(String msg,String col) { if(lblStatut!=null){lblStatut.setText(msg);lblStatut.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:"+col+";");} }
    private void setLoad(boolean a) { setV(spinner,a);if(btnAnalyser!=null){btnAnalyser.setDisable(a);btnAnalyser.setText(a?"Analyse en cours...":"Analyser avec l'IA");} }
    private double parseBudget() {
        try{double b=Double.parseDouble(txtBudget.getText().trim().replace(",","."));if(b>0)return b;statut("Budget doit etre positif !","#e74c3c");return-1;}
        catch(Exception e){statut("Budget invalide ! Ex: 150","#e74c3c");return-1;}
    }
    @FXML private void retour() { if(mainController!=null)mainController.switchAbonnements(); }
}