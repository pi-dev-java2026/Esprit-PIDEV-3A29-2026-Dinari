package com.gestion.Services;

import com.gestion.utils.ConfigLoader;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class AiSummaryService {

    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL = "llama-3.3-70b-versatile";
    private final String apiKey;

    public AiSummaryService() {
        this.apiKey = ConfigLoader.getApiKey("GROQ_API_KEY");
        
        if (this.apiKey == null || this.apiKey.trim().isEmpty()) {
            throw new IllegalStateException(
                "GROQ_API_KEY not configured!\n\n" +
                "Please create 'local.properties' in your project root with:\n" +
                "GROQ_API_KEY=your_key_here\n\n" +
                "Get your free key at: https://console.groq.com"
            );
        }
        
        System.out.println("✓ Using model: " + MODEL);
        System.out.println("✓ API URL: " + GROQ_API_URL);
        System.out.println();
    }

    private String maskApiKey(String key) {
        if (key == null || key.length() < 12) return "***";
        return key.substring(0, 7) + "..." + key.substring(key.length() - 4);
    }

    public String generateSummary(String lessonContent) throws Exception {
        String prompt = buildPrompt(lessonContent);

        System.out.println("=== Groq API Request ===");
        System.out.println("URL: " + GROQ_API_URL);
        System.out.println("Model: " + MODEL);
        System.out.println("API Key (masked): " + maskApiKey(apiKey));
        System.out.println("API Key (first 20 chars): " + apiKey.substring(0, Math.min(20, apiKey.length())));
        System.out.println("API Key length: " + apiKey.length());
        System.out.println("Authorization header: Bearer " + maskApiKey(apiKey));

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", MODEL);

        JsonArray messages = new JsonArray();
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", prompt);
        messages.add(message);

        requestBody.add("messages", messages);
        requestBody.addProperty("temperature", 0.7);
        requestBody.addProperty("max_tokens", 1500);

        System.out.println("Request body: " + requestBody);

        URL url = new URL(GROQ_API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        
        // Log the exact authorization header being set
        String authHeader = "Bearer " + apiKey.trim();
        System.out.println("Setting Authorization header (length): " + authHeader.length());
        conn.setRequestProperty("Authorization", authHeader);
        
        conn.setDoOutput(true);
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(30000);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input);
        }

        int responseCode = conn.getResponseCode();
        System.out.println("Response Code: " + responseCode);

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        responseCode >= 400 ? conn.getErrorStream() : conn.getInputStream(),
                        StandardCharsets.UTF_8))) {

            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
        }

        System.out.println("Response Body: " + response);

        if (responseCode != 200) {
            String errorMsg = "API Error: HTTP " + responseCode;

            try {
                JsonObject errorJson = JsonParser.parseString(response.toString()).getAsJsonObject();
                if (errorJson.has("error")) {
                    JsonObject error = errorJson.getAsJsonObject("error");
                    if (error.has("message")) {
                        errorMsg += " - " + error.get("message").getAsString();
                    }
                }
            } catch (Exception ignored) {
                // keep default error message
            }

            throw new Exception(errorMsg);
        }

        JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();

        String content = jsonResponse.getAsJsonArray("choices")
                .get(0).getAsJsonObject()
                .getAsJsonObject("message")
                .get("content").getAsString();

        System.out.println("✓ Summary generated successfully");
        return content;
    }

    private String buildPrompt(String lessonContent) {
        return "You are an educational assistant. Analyze the following lesson content and provide:\n\n" +
                "1. A summary in 5 to 8 bullet points (use • for bullets)\n" +
                "2. Three key takeaways (mark with ✓)\n" +
                "3. Three short review questions (mark with ?)\n\n" +
                "Format your response clearly with section headers.\n\n" +
                "Lesson Content:\n" + lessonContent;
    }
}