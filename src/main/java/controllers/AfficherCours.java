package controllers;

import com.gestion.Services.ServiceQuiz;
import com.gestion.entities.Cours;
import com.gestion.entities.Quiz;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AfficherCours {

    @FXML private Label lblNomCours;
    @FXML private Label lblDate;
    @FXML private TextArea txtDescription;
    @FXML private VBox quizContainer;

    private final ServiceQuiz serviceQuiz = new ServiceQuiz();
    private Cours cours;

    public void setCours(Cours cours) {
        this.cours = cours;

        lblNomCours.setText(cours.getNomCours());
        lblDate.setText("📅 Créé le: " + cours.getDateCreation());
        
        // Format description with structured content
        String formattedDescription = formatDescription(cours.getDescription());
        txtDescription.setText(formattedDescription);

        refreshQuizzes();
    }
    
    private String formatDescription(String baseDescription) {
        StringBuilder formatted = new StringBuilder();
        
        // Base description
        if (baseDescription != null && !baseDescription.trim().isEmpty()) {
            formatted.append(baseDescription).append("\n\n");
        }
        
        // Objectifs Pédagogiques
        formatted.append("🎯 OBJECTIFS PÉDAGOGIQUES\n");
        formatted.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
        formatted.append("• Comprendre les principes fondamentaux de la gestion financière\n\n");
        formatted.append("• Maîtriser les outils et techniques de planification budgétaire\n\n");
        formatted.append("• Développer des compétences en analyse financière personnelle\n\n");
        formatted.append("• Appliquer les meilleures pratiques de gestion de patrimoine\n\n\n");
        
        // Points Clés du Module
        formatted.append("✨ POINTS CLÉS DU MODULE\n");
        formatted.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
        formatted.append("📊 Analyse Budgétaire\n");
        formatted.append("   Techniques d'analyse des revenus et dépenses\n\n");
        formatted.append("💰 Épargne Stratégique\n");
        formatted.append("   Méthodes d'optimisation de l'épargne\n\n");
        formatted.append("📈 Planification Financière\n");
        formatted.append("   Élaboration de plans financiers à court et long terme\n\n");
        formatted.append("🎓 Cas Pratiques\n");
        formatted.append("   Exercices et simulations réelles\n\n\n");
        
        // Résultats d'Apprentissage
        formatted.append("🏆 RÉSULTATS D'APPRENTISSAGE\n");
        formatted.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
        formatted.append("✓ Capacité à créer et gérer un budget personnel efficace\n\n");
        formatted.append("✓ Compétence en identification et réduction des dépenses superflues\n\n");
        formatted.append("✓ Maîtrise des outils de suivi financier et tableaux de bord\n\n");
        formatted.append("✓ Aptitude à prendre des décisions financières éclairées\n");
        
        return formatted.toString();
    }

    private void refreshQuizzes() {
        if (cours == null) return;

        quizContainer.getChildren().clear();
        try {
            List<Quiz> quizzes = serviceQuiz.getByCoursId(cours.getIdCours());
            for (Quiz q : quizzes) {
                quizContainer.getChildren().add(buildQuizCard(q));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur chargement quiz: " + e.getMessage()).showAndWait();
        }
    }

    private Node buildQuizCard(Quiz q) {
        VBox card = new VBox(6);
        card.setStyle("-fx-padding: 12; -fx-background-color: white; -fx-background-radius: 10; "
                + "-fx-border-radius: 10; -fx-border-color: #E0E0E0;");

        Label titre = new Label(q.getTitre());
        titre.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label score = new Label("⭐ Score: " + q.getScoreDeQuiz());
        score.setStyle("-fx-text-fill: #F59E0B; -fx-font-weight: bold;");
        Label date = new Label("📅 Créé le: " + q.getDateCreation());

        Button btnModifier = new Button("Modifier");
        Button btnSupprimer = new Button("Supprimer");
        btnSupprimer.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white;");

        btnModifier.setOnAction(e -> openModifierQuiz(q));
        btnSupprimer.setOnAction(e -> {
            try {
                serviceQuiz.delete(q.getIdQuiz());
                refreshQuizzes();
            } catch (SQLException ex) {
                ex.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Erreur suppression: " + ex.getMessage()).showAndWait();
            }
        });

        HBox actions = new HBox(10, btnModifier, btnSupprimer);
        card.getChildren().addAll(titre, score, date, actions);
        return card;
    }

    @FXML
    private void onAjouterQuiz() {
        openAjouterQuiz();
    }

    @FXML
    private void goListeCours() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ListeCours.fxml"));
            Stage stage = (Stage) lblNomCours.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur navigation: " + e.getMessage()).showAndWait();
        }
    }

    private void openAjouterQuiz() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ajouterQuiz.fxml"));
            Parent root = loader.load();

            AjouterQuiz controller = loader.getController();
            controller.setIdCours(cours.getIdCours());

            Stage stage = new Stage();
            stage.setTitle("Ajouter Quiz");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur ouverture AjouterQuiz: " + e.getMessage()).showAndWait();
        }
    }

    private void openModifierQuiz(Quiz quiz) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierQuiz.fxml"));
            Parent root = loader.load();

            ModifierQuiz controller = loader.getController();
            controller.setQuiz(quiz);

            Stage stage = new Stage();
            stage.setTitle("Modifier Quiz");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur ouverture ModifierQuiz: " + e.getMessage()).showAndWait();
        }
    }
}