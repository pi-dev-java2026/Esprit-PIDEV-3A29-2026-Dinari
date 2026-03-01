package com.gestion;

import com.gestion.Services.ServiceCours;
import com.gestion.Services.ServiceQuiz;
import com.gestion.entities.Cours;
import com.gestion.entities.Quiz;

public class TestServices {
    public static void main(String[] args) throws Exception {
        ServiceCours coursDAO = new ServiceCours();
        int idCours = coursDAO.add(new Cours("Gestion de Budget Personnel", "Apprenez à gérer efficacement votre budget personnel et familial", java.time.LocalDate.now()));

        ServiceQuiz quizDAO = new ServiceQuiz();
        quizDAO.add(new Quiz(idCours, "Quiz Budget", "Quelle est la première étape pour créer un budget?", java.util.List.of("A","B","C"), "B", 10, java.time.LocalDate.now()));

        System.out.println("Modules: " + coursDAO.getAll().size());
        System.out.println("Quiz: " + quizDAO.getAll().size());
    }
}
