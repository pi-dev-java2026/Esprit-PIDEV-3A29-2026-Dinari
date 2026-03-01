package Fintech.servicies;

import Fintech.entities.Message;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

public class GroqService {

    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private final String apiKey;
    private final HttpClient httpClient;

    public GroqService() {
        // Clé API fournie directement par l'utilisateur
        this.apiKey = "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";

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(60)) // Increased timeout to prevent dropping slow connections
                .build();
    }

    public JSONObject analyzeConversation(List<Message> history, String userEmail) throws Exception {
        if (apiKey.isEmpty()) {
            throw new Exception("La clé API GROQ_API_KEY n'est pas définie dans l'environnement.");
        }

        JSONObject requestBody = new JSONObject();
        // Update to a currently supported model like llama-3.1-8b-instant
        requestBody.put("model", "llama-3.1-8b-instant");
        requestBody.put("response_format", new JSONObject().put("type", "json_object"));

        JSONArray messagesArray = new JSONArray();

        // System prompt to instruct JSON output
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", "Tu es un assistant support client Fintech. " +
                "Ton rôle : Analyser la demande de l'utilisateur (" + userEmail
                + ") et extraire un JSON de type 'reclamation'. " +
                "Tu dois renvoyer UNIQUEMENT un JSON, sans texte additionnel. " +
                "Le JSON doit obligatoirement inclure ces trois clés :\n" +
                "{\n" +
                "  \"type\": \"Paiement\" ou \"Technique\" ou \"Compte\",\n" +
                "  \"description\": \"Résumé court du problème avec priorite\",\n" +
                "  \"priorite\": \"Faible\" ou \"Moyenne\" ou \"Élevée\"\n" +
                "}");
        messagesArray.put(systemMessage);

        // Add the history of messages
        for (Message m : history) {
            JSONObject msg = new JSONObject();
            msg.put("role", m.getSender().equals("USER") ? "user" : "assistant"); // assistant is bot
            msg.put("content", m.getContent());
            messagesArray.put(msg);
        }

        requestBody.put("messages", messagesArray);
        requestBody.put("temperature", 0.0); // 0.0 for deterministic JSON output

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .timeout(Duration.ofSeconds(60)) // Increased request timeout to 60s
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new Exception("Erreur HTTP API Groq (" + response.statusCode() + "): " + response.body());
        }

        JSONObject responseJson = new JSONObject(response.body());
        JSONArray choices = responseJson.optJSONArray("choices");
        if (choices == null || choices.isEmpty()) {
            throw new Exception("Réponse vide de l'API Groq.");
        }

        String content = choices.getJSONObject(0).getJSONObject("message").getString("content");

        // Parse extracted content as JSON
        try {
            return new JSONObject(content);
        } catch (Exception e) {
            // Fallback content trimming if strict JSON output failed
            int start = content.indexOf('{');
            int end = content.lastIndexOf('}');
            if (start != -1 && end != -1) {
                return new JSONObject(content.substring(start, end + 1));
            }
            throw new Exception("Impossible de parser le JSON retourné par l'IA : " + content);
        }
    }

    public String getAdvice(String problemDescription) throws Exception {
        if (apiKey.isEmpty()) {
            throw new Exception("La clé API GROQ_API_KEY n'est pas définie dans l'environnement.");
        }

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "llama-3.1-8b-instant");

        JSONArray messagesArray = new JSONArray();

        // System prompt to instruct Markdown/text output
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", "Tu es un technicien expert en support client au sein d'une Fintech. " +
                "L'utilisateur vient de rencontrer un problème technique ou de paiement. " +
                "Ton rôle : Fournir 3 ou 4 étapes claires, concises et faciles à suivre en Français pour " +
                "l'aider à résoudre ce problème par lui-même. Ne sois pas trop long, va à l'essentiel.");
        messagesArray.put(systemMessage);

        // User problem description
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", "Voici mon problème : " + problemDescription);
        messagesArray.put(userMessage);

        requestBody.put("messages", messagesArray);
        requestBody.put("temperature", 0.7); // 0.7 for more conversational/helpful text

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .timeout(Duration.ofSeconds(60))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new Exception("Erreur HTTP API Groq (" + response.statusCode() + "): " + response.body());
        }

        JSONObject responseJson = new JSONObject(response.body());
        JSONArray choices = responseJson.optJSONArray("choices");
        if (choices == null || choices.isEmpty()) {
            throw new Exception("Réponse vide de l'API Groq (Conseils).");
        }

        return choices.getJSONObject(0).getJSONObject("message").getString("content");
    }
}
