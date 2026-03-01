package com.gestion.entities;

import java.time.LocalDate;

public class Cours {

    private int idCours;
    private String nomCours;
    private String description;
    private LocalDate dateCreation;

    public Cours() {}

    public Cours(String nomCours, String description, LocalDate dateCreation) {
        this.nomCours = nomCours;
        this.description = description;
        this.dateCreation = dateCreation;
    }

    public Cours(int idCours, String nomCours, String description, LocalDate dateCreation) {
        this.idCours = idCours;
        this.nomCours = nomCours;
        this.description = description;
        this.dateCreation = dateCreation;
    }

    public int getIdCours() { return idCours; }
    public void setIdCours(int idCours) { this.idCours = idCours; }

    public String getNomCours() { return nomCours; }
    public void setNomCours(String nomCours) { this.nomCours = nomCours; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDate dateCreation) { this.dateCreation = dateCreation; }
}
