package com.gestion.entities;

import java.time.LocalDate;

public class Avis {

    private int idAvis;
    private int idQuiz;
    private String commentaire;
    private int note;
    private LocalDate dateCreation;

    public Avis() {}

    public Avis(int idQuiz, String commentaire, int note, LocalDate dateCreation) {
        this.idQuiz = idQuiz;
        this.commentaire = commentaire;
        this.note = note;
        this.dateCreation = dateCreation;
    }

    public Avis(int idAvis, int idQuiz, String commentaire, int note, LocalDate dateCreation) {
        this.idAvis = idAvis;
        this.idQuiz = idQuiz;
        this.commentaire = commentaire;
        this.note = note;
        this.dateCreation = dateCreation;
    }

    public int getIdAvis() { return idAvis; }
    public void setIdAvis(int idAvis) { this.idAvis = idAvis; }

    public int getIdQuiz() { return idQuiz; }
    public void setIdQuiz(int idQuiz) { this.idQuiz = idQuiz; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    public int getNote() { return note; }
    public void setNote(int note) { this.note = note; }

    public LocalDate getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDate dateCreation) { this.dateCreation = dateCreation; }
}
