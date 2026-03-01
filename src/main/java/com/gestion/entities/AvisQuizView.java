package com.gestion.entities;

import java.time.LocalDate;

public class AvisQuizView {
    private final int idAvis;
    private final int idQuiz;
    private final String quizTitre;
    private final String commentaire;
    private final int note;
    private final LocalDate dateCreation;

    public AvisQuizView(int idAvis, int idQuiz, String quizTitre,
                        String commentaire, int note, LocalDate dateCreation) {
        this.idAvis = idAvis;
        this.idQuiz = idQuiz;
        this.quizTitre = quizTitre;
        this.commentaire = commentaire;
        this.note = note;
        this.dateCreation = dateCreation;
    }

    public int getIdAvis() { return idAvis; }
    public int getIdQuiz() { return idQuiz; }
    public String getQuizTitre() { return quizTitre; }
    public String getCommentaire() { return commentaire; }
    public int getNote() { return note; }
    public LocalDate getDateCreation() { return dateCreation; }
}
