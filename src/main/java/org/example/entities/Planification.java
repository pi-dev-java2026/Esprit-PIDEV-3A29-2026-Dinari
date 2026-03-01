package org.example.entities;



public class Planification {

    private int id;
    private String categorie;
    private double montantAlloue;
    private String priorite;        // basse, normale, élevée
    private String mois;

    public Planification() {
    }

    public Planification(int id, String categorie, double montantAlloue,
                         String priorite, String mois) {
        this.id = id;
        this.categorie = categorie;
        this.montantAlloue = montantAlloue;
        this.priorite = priorite;
        this.mois = mois;
    }

    public Planification(String categorie, double montantAlloue, String priorite , String mois) {
        this.categorie = categorie;
        this.montantAlloue = montantAlloue;
        this.priorite = priorite;
        this.mois = mois;
    }

    public int getId() {

        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public double getMontantAlloue() {
        return montantAlloue;
    }

    public void setMontantAlloue(double montantAlloue) {
        this.montantAlloue = montantAlloue;
    }

    public String getPriorite() {
        return priorite;
    }

    public void setPriorite(String priorite) {
        this.priorite = priorite;
    }

    public String getMois() {
        return mois;
    }

    public void setMois(String mois) {
        this.mois = mois;
    }

    @Override
    public String toString() {
        return "Planification{" +
                "id=" + id +
                ", categorie='" + categorie + '\'' +
                ", montantAlloue=" + montantAlloue +
                ", priorite='" + priorite + '\'' +
                ", mois='" + mois + '\'' +
                '}';
    }

}
