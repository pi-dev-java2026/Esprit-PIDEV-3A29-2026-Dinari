package Fintech.controllers;

import Fintech.entities.Conversation;
import Fintech.entities.Message;
import Fintech.entities.Reclamation;
import Fintech.entities.User;
import Fintech.servicies.ConversationService;
import Fintech.servicies.GroqService;
import Fintech.servicies.ReclamationService;
import Fintech.utils.UserSession;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.json.JSONObject;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ChatController implements Initializable {

    @FXML
    private VBox chatArea;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private TextField messageField;
    @FXML
    private Button sendButton;
    @FXML
    private Label statusLabel;

    private ConversationService conversationService;
    private GroqService groqService;
    private ReclamationService reclamationService;

    private Conversation currentConversation;
    private String userEmail;

    // State for Advice Feature
    private boolean waitingForAdviceFeedback = false;
    private String lastProblemDescription = "";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        conversationService = new ConversationService();
        groqService = new GroqService();
        reclamationService = new ReclamationService();

        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getEmail() != null) {
            userEmail = currentUser.getEmail();
        } else {
            // Fallback for tests if not logged in
            userEmail = "user@test.com";
        }

        currentConversation = conversationService.creerConversation(userEmail);

        setWaitingState(false);

        // Auto-scroll logic to keep the chat at the bottom
        chatArea.heightProperty().addListener((observable, oldValue, newValue) -> {
            scrollPane.setVvalue((Double) newValue);
        });

        // Add a small delay for the welcome message to simulate bot connection
        Platform.runLater(() -> {
            addMessageBubble(
                    "Bonjour ! Décrivez votre problème, et je m'occuperai de l'analyser et de créer une réclamation automatiquement.",
                    "BOT");
        });
    }

    @FXML
    private void handleSend() {
        String content = messageField.getText().trim();
        if (content.isEmpty() || currentConversation == null) {
            return;
        }

        // 1. Save user message and show on UI immediately
        Message userMessage = new Message(currentConversation.getId(), "USER", content, null);
        addMessageBubble(content, "USER");
        messageField.clear();
        conversationService.ajouterMessage(userMessage);

        setWaitingState(true);

        // 2. Start a new thread for API call
        new Thread(() -> {
            try {
                // Feature: Check if message contains an email
                String extractedEmail = extractEmail(content);
                if (extractedEmail != null) {
                    List<Reclamation> reclamations = reclamationService.getReclamationsByEmail(extractedEmail);

                    String botResponse;
                    if (reclamations.isEmpty()) {
                        botResponse = "Aucune réclamation trouvée pour l'adresse email : " + extractedEmail;
                    } else {
                        StringBuilder sb = new StringBuilder(
                                "Voici les réclamations liées à (" + extractedEmail + ") :\n\n");
                        for (Reclamation r : reclamations) {
                            sb.append("• ").append(r.getSubject()).append("\n")
                                    .append("  Description: ").append(r.getDescription()).append("\n")
                                    .append("  Statut: ").append(r.getStatut()).append("\n")
                                    .append("  Date: ").append("Non spécifiée").append("\n\n");
                        }
                        botResponse = sb.toString().trim();
                    }

                    Message botMessage = new Message(currentConversation.getId(), "BOT", botResponse, null);
                    conversationService.ajouterMessage(botMessage);

                    Platform.runLater(() -> {
                        addMessageBubble(botResponse, "BOT");
                        setWaitingState(false);
                    });
                    return; // Stop thread here, skip Groq AI
                }

                // Feature: Check if we are waiting for Advice Feedback
                if (waitingForAdviceFeedback) {
                    waitingForAdviceFeedback = false;
                    String lowerContent = content.toLowerCase();

                    // Simple affirmative keyword check
                    if (lowerContent.contains("oui") || lowerContent.contains("yes") || lowerContent.contains("ok")
                            || lowerContent.contains("d'accord")) {
                        Platform.runLater(() -> statusLabel.setText("Bot recherche des consignes..."));
                        String advice = groqService.getAdvice(lastProblemDescription);

                        Message botMessage = new Message(currentConversation.getId(), "BOT", advice, null);
                        conversationService.ajouterMessage(botMessage);
                        Platform.runLater(() -> {
                            addMessageBubble(advice, "BOT");
                            setWaitingState(false);
                        });
                    } else {
                        // User declined advice or said something else
                        String botResponse = "D'accord, n'hésitez pas si vous avez besoin d'autre chose.";
                        Message botMessage = new Message(currentConversation.getId(), "BOT", botResponse, null);
                        conversationService.ajouterMessage(botMessage);
                        Platform.runLater(() -> {
                            addMessageBubble(botResponse, "BOT");
                            setWaitingState(false);
                        });
                    }
                    return; // Stop thread here
                }

                // If no email detected, proceed with normal Groq AI analysis
                // Fetch the last 10 messages of this conversation
                List<Message> history = conversationService.getHistorique(currentConversation.getId(), 10);

                // Call Groq API expecting JSON
                JSONObject responseJson = groqService.analyzeConversation(history, userEmail);

                // Parse the strictly formatted JSON
                String type = responseJson.optString("type", "Technique");
                String description = responseJson.optString("description", "A besoin d'assistance");
                String priorite = responseJson.optString("priorite", "Moyenne");

                // 3. Create a Reclamation based on the extracted data
                Reclamation reclamation = new Reclamation();
                reclamation.setEmail(userEmail);
                reclamation.setSubject("Chat IA: " + type);
                reclamation.setDescription(description + "\nPriorité: " + priorite);
                reclamation.setStatut("Ouverte"); // Default state

                boolean created = reclamationService.creerReclamation(reclamation);

                // 4. Prepare Bot response message
                String botResponse;
                if (created) {
                    botResponse = "Réclamation traitée et enregistrée avec succès !\n\nType : " + type + "\nPriorité : "
                            + priorite + "\nRésumé : " + description
                            + "\n\nVoulez-vous des consignes ou des étapes pour essayer de corriger ce problème par vous-même ?";

                    this.lastProblemDescription = description;
                    this.waitingForAdviceFeedback = true;
                } else {
                    botResponse = "J'ai bien compris : " + description
                            + "\nCependant, une erreur interne empêche la création de la réclamation en base de données.";
                }

                Message botMessage = new Message(currentConversation.getId(), "BOT", botResponse, null);
                conversationService.ajouterMessage(botMessage);

                // 5. Update UI safely
                Platform.runLater(() -> {
                    addMessageBubble(botResponse, "BOT");
                    setWaitingState(false);
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    addMessageBubble("Désolé, une erreur technique est survenue lors de l'analyse : " + e.getMessage(),
                            "BOT");
                    setWaitingState(false);
                });
            }
        }).start();
    }

    private String extractEmail(String text) {
        // Simple regex for email extraction
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
        java.util.regex.Matcher m = p.matcher(text);
        if (m.find()) {
            return m.group();
        }
        return null;
    }

    private void setWaitingState(boolean waiting) {
        if (waiting) {
            sendButton.setDisable(true);
            messageField.setDisable(true);
            statusLabel.setText("Bot en train d'analyser...");
            statusLabel.setStyle("-fx-text-fill: #E67E22; -fx-font-weight: bold;"); // Orange color
        } else {
            sendButton.setDisable(false);
            messageField.setDisable(false);
            Platform.runLater(() -> messageField.requestFocus());
            statusLabel.setText("Prêt");
            statusLabel.setStyle("-fx-text-fill: #27AE60; -fx-font-weight: bold;"); // Green color
        }
    }

    private void addMessageBubble(String text, String sender) {
        HBox box = new HBox();
        box.setPadding(new javafx.geometry.Insets(5, 5, 5, 5));

        Label label = new Label(text);
        label.setWrapText(true);
        label.setMaxWidth(500); // Wrap after 500px width
        label.setStyle(
                "-fx-padding: 12 18; -fx-background-radius: 18; -fx-font-size: 14; -fx-font-family: 'Segoe UI', Arial, sans-serif;");

        if ("USER".equals(sender)) {
            box.setAlignment(Pos.CENTER_RIGHT);
            label.setStyle(label.getStyle() + "-fx-background-color: #3498DB; -fx-text-fill: white;");
        } else {
            box.setAlignment(Pos.CENTER_LEFT);
            label.setStyle(label.getStyle()
                    + "-fx-background-color: #FFFFFF; -fx-text-fill: #2C3E50; -fx-border-color: #EAECEA; -fx-border-radius: 18; -fx-border-width: 1;");
        }

        box.getChildren().add(label);
        chatArea.getChildren().add(box);
    }
}
