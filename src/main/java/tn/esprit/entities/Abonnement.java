package tn.esprit.entities;

import java.sql.Date;

public class Abonnement {
    private int id;
    private String nom;
    private double prix;
    private Date dateDebut;
    private String frequence;
    private String categorie;
    private boolean actif;
    private String imagePath;
    private String tier; // ← NOUVEAU : "Normal", "Premium", "Gold"

    public Abonnement() {}

    public Abonnement(String nom, double prix, Date dateDebut, String frequence, String categorie, boolean actif) {
        this.nom = nom;
        this.prix = prix;
        this.dateDebut = dateDebut;
        this.frequence = frequence;
        this.categorie = categorie;
        this.actif = actif;
    }

    public Abonnement(String nom, double prix, Date dateDebut, String frequence, String categorie, boolean actif, String imagePath) {
        this.nom = nom;
        this.prix = prix;
        this.dateDebut = dateDebut;
        this.frequence = frequence;
        this.categorie = categorie;
        this.actif = actif;
        this.imagePath = imagePath;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public double getPrix() { return prix; }
    public void setPrix(double prix) { this.prix = prix; }

    public Date getDateDebut() { return dateDebut; }
    public void setDateDebut(Date dateDebut) { this.dateDebut = dateDebut; }

    public String getFrequence() { return frequence; }
    public void setFrequence(String frequence) { this.frequence = frequence; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    // ← NOUVEAU getter/setter tier
    public String getTier() { return tier; }
    public void setTier(String tier) { this.tier = tier; }

    @Override
    public String toString() {
        return "Abonnement{id=" + id + ", nom='" + nom + "', prix=" + prix + " DT, tier='" + tier + "'}";
    }
}