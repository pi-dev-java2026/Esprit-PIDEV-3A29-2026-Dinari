package org.example.entities;

import java.sql.Date;

public class Activite {

    private int id;
    private String description;
    private double montant;
    private Date dateActivite;
    private String categorie;


    private String devise;


    private String frequence;
    private Date dateFinRecurrence;

    public Activite() {
        this.devise = "TND"; // valeur par défaut
    }


    public Activite(int id,
                    String description,
                    double montant,
                    Date dateActivite,
                    String categorie,
                    String devise,
                    String frequence,
                    Date dateFinRecurrence) {

        this.id = id;
        this.description = description;
        this.montant = montant;
        this.dateActivite = dateActivite;
        this.categorie = categorie;
        this.devise = (devise == null || devise.isBlank()) ? "TND" : devise;
        this.frequence = (frequence == null || frequence.isBlank()) ? "AUCUNE" : frequence;
        this.dateFinRecurrence = dateFinRecurrence;
    }


    public Activite(String description,
                    double montant,
                    Date dateActivite,
                    String categorie,
                    String devise,
                    String frequence,
                    Date dateFinRecurrence) {

        this(0, description, montant, dateActivite, categorie, devise, frequence, dateFinRecurrence);
    }


    public Activite(String description, double montant, Date dateActivite, String categorie) {
        this(description, montant, dateActivite, categorie, "TND", "AUCUNE", null);
    }

    // ---------- Getters / Setters ----------

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    public Date getDateActivite() {
        return dateActivite;
    }

    public void setDateActivite(Date dateActivite) {
        this.dateActivite = dateActivite;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public String getDevise() {
        return devise;
    }

    public void setDevise(String devise) {
        this.devise = (devise == null || devise.isBlank()) ? "TND" : devise;
    }

    public String getFrequence() {
        return frequence;
    }

    public void setFrequence(String frequence) {
        this.frequence = (frequence == null || frequence.isBlank()) ? "AUCUNE" : frequence;
    }

    public Date getDateFinRecurrence() {
        return dateFinRecurrence;
    }

    public void setDateFinRecurrence(Date dateFinRecurrence) {
        this.dateFinRecurrence = dateFinRecurrence;
    }


    public boolean isRecurrente() {
        return frequence != null && !"AUCUNE".equalsIgnoreCase(frequence);
    }

    @Override
    public String toString() {
        return "Activite{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", montant=" + montant +
                ", dateActivite=" + dateActivite +
                ", categorie='" + categorie + '\'' +
                ", devise='" + devise + '\'' +
                ", frequence='" + frequence + '\'' +
                ", dateFinRecurrence=" + dateFinRecurrence +
                '}';
    }
}