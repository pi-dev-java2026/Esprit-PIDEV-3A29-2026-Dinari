package org.example.ai;

public class CategorieAnalyseRow {

    private final String categorie;
    private final double budget;
    private final double consomme;
    private final double reste;
    private final double pct; // 0..1

    public CategorieAnalyseRow(String categorie, double budget, double consomme) {
        this.categorie = categorie;
        this.budget = budget;
        this.consomme = consomme;
        this.reste = budget - consomme;
        this.pct = (budget <= 0) ? 0 : consomme / budget;
    }

    public String getCategorie() { return categorie; }
    public double getBudget() { return budget; }
    public double getConsomme() { return consomme; }
    public double getReste() { return reste; }
    public double getPct() { return pct; }
}