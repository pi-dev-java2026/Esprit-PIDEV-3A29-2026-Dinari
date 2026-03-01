package com.gestion.entities;

import java.time.LocalDate;
import java.util.List;

public class Quiz {

    private int idQuiz;
    private int idCours;
    private String titre;
    private String question;
    private List<String> listeDeReponse;
    private String reponseCorrect;
    private int scoreDeQuiz;
    private LocalDate dateCreation;
    private boolean isExamMode;
    private int timeLimit; // in minutes

    public Quiz() {}


    public Quiz(int idCours, String titre, String question, List<String> listeDeReponse,
                String reponseCorrect, int scoreDeQuiz, LocalDate dateCreation) {
        this.idCours = idCours;
        this.titre = titre;
        this.question = question;
        this.listeDeReponse = listeDeReponse;
        this.reponseCorrect = reponseCorrect;
        this.scoreDeQuiz = scoreDeQuiz;
        this.dateCreation = dateCreation;
        this.isExamMode = false;
        this.timeLimit = 0;
    }

    // Backward-compatible constructor (old signature)
    public Quiz(int idCours, String titre, List<String> listeDeReponse,
                String reponseCorrect, int scoreDeQuiz, LocalDate dateCreation) {
        this(idCours, titre, titre, listeDeReponse, reponseCorrect, scoreDeQuiz, dateCreation, false, 0);
    }

    public Quiz(int idQuiz, int idCours, String titre, String question, List<String> listeDeReponse,
                String reponseCorrect, int scoreDeQuiz, LocalDate dateCreation) {
        this.idQuiz = idQuiz;
        this.idCours = idCours;
        this.titre = titre;
        this.question = question;
        this.listeDeReponse = listeDeReponse;
        this.reponseCorrect = reponseCorrect;
        this.scoreDeQuiz = scoreDeQuiz;
        this.dateCreation = dateCreation;
        this.isExamMode = false;
        this.timeLimit = 0;
    }

    public Quiz(int idCours, String titre, String question, List<String> listeDeReponse,
                String reponseCorrect, int scoreDeQuiz, LocalDate dateCreation,
                boolean isExamMode, int timeLimit) {
        this.idCours = idCours;
        this.titre = titre;
        this.question = question;
        this.listeDeReponse = listeDeReponse;
        this.reponseCorrect = reponseCorrect;
        this.scoreDeQuiz = scoreDeQuiz;
        this.dateCreation = dateCreation;
        this.isExamMode = isExamMode;
        this.timeLimit = timeLimit;
    }

    public Quiz(int idQuiz, int idCours, String titre, String question, List<String> listeDeReponse,
                String reponseCorrect, int scoreDeQuiz, LocalDate dateCreation,
                boolean isExamMode, int timeLimit) {
        this.idQuiz = idQuiz;
        this.idCours = idCours;
        this.titre = titre;
        this.question = question;
        this.listeDeReponse = listeDeReponse;
        this.reponseCorrect = reponseCorrect;
        this.scoreDeQuiz = scoreDeQuiz;
        this.dateCreation = dateCreation;
        this.isExamMode = isExamMode;
        this.timeLimit = timeLimit;
    }

    // Constructor without question parameter (used by ServiceQuiz)
    public Quiz(int idQuiz, int idCours, String titre, List<String> listeDeReponse,
                String reponseCorrect, int scoreDeQuiz, LocalDate dateCreation,
                boolean isExamMode, int timeLimit) {
        this.idQuiz = idQuiz;
        this.idCours = idCours;
        this.titre = titre;
        this.question = titre; // Use titre as question
        this.listeDeReponse = listeDeReponse;
        this.reponseCorrect = reponseCorrect;
        this.scoreDeQuiz = scoreDeQuiz;
        this.dateCreation = dateCreation;
        this.isExamMode = isExamMode;
        this.timeLimit = timeLimit;
    }

    public int getIdQuiz() {
        return idQuiz;
    }

    public void setIdQuiz(int idQuiz) {
        this.idQuiz = idQuiz;
    }

    public int getIdCours() {
        return idCours;
    }

    public void setIdCours(int idCours) {
        this.idCours = idCours;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<String> getListeDeReponse() {
        return listeDeReponse;
    }

    public void setListeDeReponse(List<String> listeDeReponse) {
        this.listeDeReponse = listeDeReponse;
    }

    public String getReponseCorrect() {
        return reponseCorrect;
    }

    public void setReponseCorrect(String reponseCorrect) {
        this.reponseCorrect = reponseCorrect;
    }

    public int getScoreDeQuiz() {
        return scoreDeQuiz;
    }

    public void setScoreDeQuiz(int scoreDeQuiz) {
        this.scoreDeQuiz = scoreDeQuiz;
    }

    public LocalDate getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDate dateCreation) {
        this.dateCreation = dateCreation;
    }

    public boolean isExamMode() {
        return isExamMode;
    }

    public void setExamMode(boolean examMode) {
        isExamMode = examMode;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }
}
