package tn.esprit.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Service Groq AI (Llama 3.3) — Recommandations abonnements
 */
public class GeminiService {

    private static final String API_KEY = System.getenv("GROQ_API_KEY");
    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODELE  = "llama-3.3-70b-versatile";

    public static String envoyerPrompt(String prompt) {
        try {
            String promptPropre = prompt
                    .replace("\\", "\\\\")
                    .replace("\"", "\\'")
                    .replace("\r\n", "\\n")
                    .replace("\n", "\\n")
                    .replace("\r", "\\n")
                    .replace("\t", " ");

            String corps = "{"
                    + "\"model\":\"" + MODELE + "\","
                    + "\"messages\":["
                    + "{\"role\":\"system\",\"content\":\"Tu es un conseiller financier expert. Reponds toujours en francais, de facon claire et structuree.\"},"
                    + "{\"role\":\"user\",\"content\":\"" + promptPropre + "\"}"
                    + "],"
                    + "\"temperature\":0.7,"
                    + "\"max_tokens\":1024"
                    + "}";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest requete = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(corps))
                    .build();

            HttpResponse<String> reponse = client.send(requete,
                    HttpResponse.BodyHandlers.ofString());

            if (reponse.statusCode() == 200) {
                return extraireTexte(reponse.body());
            } else {
                return "Erreur API Groq (code " + reponse.statusCode() + ") : " + reponse.body();
            }

        } catch (Exception e) {
            return "Erreur de connexion : " + e.getMessage();
        }
    }

    private static String extraireTexte(String json) {
        try {
            int idxChoices = json.indexOf("\"choices\"");
            if (idxChoices == -1) return "Format de réponse inattendu.";

            int idxContent = json.indexOf("\"content\"", idxChoices);
            if (idxContent == -1) return "Contenu vide dans la réponse.";

            int debut = idxContent + 9;
            while (debut < json.length() &&
                    (json.charAt(debut) == ':' || json.charAt(debut) == ' ')) {
                debut++;
            }

            if (debut >= json.length() || json.charAt(debut) != '"') {
                return "Erreur de parsing de la réponse.";
            }
            debut++;

            StringBuilder sb = new StringBuilder();
            int i = debut;
            while (i < json.length()) {
                char c = json.charAt(i);
                if (c == '\\' && i + 1 < json.length()) {
                    char next = json.charAt(i + 1);
                    switch (next) {
                        case 'n'  -> { sb.append('\n'); i += 2; }
                        case '"'  -> { sb.append('"');  i += 2; }
                        case '\\' -> { sb.append('\\'); i += 2; }
                        case 't'  -> { sb.append('\t'); i += 2; }
                        case 'r'  -> { sb.append('\r'); i += 2; }
                        default   -> { sb.append(c);    i++; }
                    }
                } else if (c == '"') {
                    break;
                } else {
                    sb.append(c);
                    i++;
                }
            }

            String resultat = sb.toString().trim();
            return resultat.isEmpty() ? "L'IA n'a pas retourné de réponse." : resultat;

        } catch (Exception e) {
            return "Impossible de lire la réponse de l'IA.";
        }
    }
}