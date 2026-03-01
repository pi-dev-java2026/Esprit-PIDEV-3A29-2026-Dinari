package user.controllers;

import com.gestion.Services.AiSummaryService;
import com.gestion.Services.PdfService;
import com.gestion.Services.ServiceQuiz;
import com.gestion.Services.TranslationService;
import com.gestion.entities.Cours;
import com.gestion.entities.Quiz;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class UserAfficherCours {

    @FXML private Label lblNomCours;
    @FXML private Label lblDate;
    @FXML private TextArea txtDescription;
    @FXML private VBox quizContainer;
    @FXML private ComboBox<String> cmbSourceLang;
    @FXML private ComboBox<String> cmbTargetLang;
    @FXML private Button btnTranslate;
    @FXML private Button btnDownloadPdf;
    @FXML private ProgressIndicator progressIndicator;
    
    @FXML private Button btnGenerateSummary;
    @FXML private ProgressIndicator summaryProgressIndicator;
    @FXML private VBox summaryContainer;
    @FXML private TextArea txtSummary;
    @FXML private Label lblSummaryPlaceholder;

    private final ServiceQuiz serviceQuiz = new ServiceQuiz();
    private final TranslationService translationService = new TranslationService();
    private final PdfService pdfService = new PdfService();
    private AiSummaryService aiSummaryService;
    private Cours cours;
    private String currentTranslatedText = null;

    public void setCours(Cours cours) {
        this.cours = cours;

        lblNomCours.setText(cours.getNomCours());
        lblDate.setText("📅 Créé le: " + cours.getDateCreation());
        
        String formattedDescription = formatDescription(cours.getDescription());
        txtDescription.setText(formattedDescription);
        currentTranslatedText = null;

        initializeLanguageComboBoxes();
        initializeAiSummaryService();
        refreshQuizzes();
    }
    
    private void initializeAiSummaryService() {
        try {
            aiSummaryService = new AiSummaryService();
            System.out.println("✓ AI Summary Service initialized successfully");
        } catch (Exception e) {
            System.err.println("⚠ AI Summary Service initialization warning: " + e.getMessage());
        }
    }

    private void initializeLanguageComboBoxes() {
        cmbSourceLang.getItems().addAll("Français", "Anglais", "Arabe");
        cmbTargetLang.getItems().addAll("Français", "Anglais", "Arabe");
        cmbSourceLang.setValue("Français");
        cmbTargetLang.setValue("Anglais");
        
        progressIndicator.setVisible(false);
        btnDownloadPdf.setDisable(true);
    }

    @FXML
    private void handleTranslate() {
        String sourceText = txtDescription.getText();
        if (sourceText == null || sourceText.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Aucun texte à traduire");
            return;
        }

        String sourceLang = getLanguageCode(cmbSourceLang.getValue());
        String targetLang = getLanguageCode(cmbTargetLang.getValue());

        if (sourceLang.equals(targetLang)) {
            showAlert(Alert.AlertType.WARNING, "Les langues source et cible doivent être différentes");
            return;
        }

        System.out.println("Starting translation: " + sourceLang + " -> " + targetLang);

        btnTranslate.setDisable(true);
        progressIndicator.setVisible(true);

        new Thread(() -> {
            try {
                String translated = translationService.translate(sourceText, sourceLang, targetLang);
                Platform.runLater(() -> {
                    txtDescription.setText(translated);
                    currentTranslatedText = translated;
                    btnDownloadPdf.setDisable(false);
                    progressIndicator.setVisible(false);
                    btnTranslate.setDisable(false);
                    showAlert(Alert.AlertType.INFORMATION, "Traduction réussie!");
                });
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Translation error: " + e.getMessage());
                Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    btnTranslate.setDisable(false);
                    String errorMsg = "Erreur de traduction:\n" + e.getMessage() + 
                                    "\n\nVérifiez votre connexion Internet et réessayez.";
                    showAlert(Alert.AlertType.ERROR, errorMsg);
                });
            }
        }).start();
    }

    @FXML
    private void handleDownloadPdf() {
        String textToExport = currentTranslatedText != null ? currentTranslatedText : txtDescription.getText();
        
        if (textToExport == null || textToExport.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Aucun texte à exporter");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le PDF");
        fileChooser.setInitialFileName("cours_traduit.pdf");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );

        File file = fileChooser.showSaveDialog(btnDownloadPdf.getScene().getWindow());
        if (file != null) {
            progressIndicator.setVisible(true);
            btnDownloadPdf.setDisable(true);

            new Thread(() -> {
                try {
                    boolean isArabic = cmbTargetLang.getValue().equals("Arabe");
                    pdfService.exportToPdf(textToExport, file.getAbsolutePath(), isArabic);
                    
                    Platform.runLater(() -> {
                        progressIndicator.setVisible(false);
                        btnDownloadPdf.setDisable(false);
                        showAlert(Alert.AlertType.INFORMATION, "PDF créé avec succès!\n" + file.getAbsolutePath());
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        progressIndicator.setVisible(false);
                        btnDownloadPdf.setDisable(false);
                        showAlert(Alert.AlertType.ERROR, "Erreur création PDF: " + e.getMessage());
                    });
                }
            }).start();
        }
    }

    @FXML
    private void handleGenerateSummary() {
        if (aiSummaryService == null) {
            showAlert(Alert.AlertType.ERROR, 
                "Service IA non disponible.\nVeuillez configurer la variable d'environnement GROQ_API_KEY.");
            return;
        }

        String content = txtDescription.getText();
        if (content == null || content.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Aucun contenu à résumer");
            return;
        }

        btnGenerateSummary.setDisable(true);
        summaryProgressIndicator.setVisible(true);
        lblSummaryPlaceholder.setVisible(false);

        new Thread(() -> {
            try {
                String summary = aiSummaryService.generateSummary(content);
                
                Platform.runLater(() -> {
                    txtSummary.setText(summary);
                    summaryContainer.setVisible(true);
                    lblSummaryPlaceholder.setVisible(false);
                    summaryProgressIndicator.setVisible(false);
                    btnGenerateSummary.setDisable(false);
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    summaryProgressIndicator.setVisible(false);
                    btnGenerateSummary.setDisable(false);
                    lblSummaryPlaceholder.setVisible(true);
                    
                    String errorMsg = "Erreur lors de la génération du résumé:\n" + e.getMessage();
                    if (e.getMessage().contains("API Error")) {
                        errorMsg += "\n\nVérifiez votre clé API et votre connexion Internet.";
                    }
                    showAlert(Alert.AlertType.ERROR, errorMsg);
                });
            }
        }).start();
    }

    private String getLanguageCode(String language) {
        switch (language) {
            case "Français": return "fr";
            case "Anglais": return "en";
            case "Arabe": return "ar";
            default: return "en";
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type, message);
        alert.showAndWait();
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
            if (quizzes.isEmpty()) {
                Label noQuiz = new Label("Aucun quiz disponible pour ce module.");
                noQuiz.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 14px;");
                quizContainer.getChildren().add(noQuiz);
            } else {
                for (Quiz q : quizzes) {
                    quizContainer.getChildren().add(buildQuizCard(q));
                }
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

        Label score = new Label("⭐ Score: " + q.getScoreDeQuiz() + " points");
        score.setStyle("-fx-text-fill: #F59E0B; -fx-font-weight: bold;");
        Label date = new Label("📅 Créé le: " + q.getDateCreation());

        Button btnStart = new Button("🎯 Commencer Quiz");
        btnStart.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; "
                + "-fx-background-radius: 10; -fx-padding: 10 16; -fx-font-weight: bold;");
        btnStart.setOnAction(e -> startQuiz(q));

        HBox actions = new HBox(10, btnStart);
        card.getChildren().addAll(titre, score, date, actions);
        return card;
    }

    private void startQuiz(Quiz quiz) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/UserQuizView.fxml"));
            Parent root = loader.load();

            UserQuizView controller = loader.getController();
            controller.setQuiz(quiz);

            Stage stage = new Stage();
            stage.setTitle("Quiz: " + quiz.getTitre());
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur ouverture quiz: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void goListeCours() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/user/EducationHome.fxml"));
            Stage stage = (Stage) lblNomCours.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur navigation: " + e.getMessage()).showAndWait();
        }
    }
}
