package com.gestion.Services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class TranslationService {

    private static final String API_BASE_URL = "https://api.mymemory.translated.net/get";
    private static final int CONNECT_TIMEOUT = 20000;
    private static final int READ_TIMEOUT = 20000;
    private static final int MAX_CHUNK_SIZE = 400;
    private static final int DELAY_BETWEEN_REQUESTS = 500;

    public String translate(String text, String sourceLang, String targetLang) throws Exception {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Text cannot be empty");
        }

        if (!isValidLanguageCode(sourceLang) || !isValidLanguageCode(targetLang)) {
            throw new IllegalArgumentException("Invalid language code. Use: ar, fr, or en");
        }

        System.out.println("=== Translation Request ===");
        System.out.println("Source: " + sourceLang + " → Target: " + targetLang);
        System.out.println("Total text length: " + text.length() + " characters");

        List<String> chunks = splitTextIntoChunks(text);
        System.out.println("Split into " + chunks.size() + " chunk(s)");

        StringBuilder translatedText = new StringBuilder();

        for (int i = 0; i < chunks.size(); i++) {
            String chunk = chunks.get(i);
            System.out.println("\n--- Translating chunk " + (i + 1) + "/" + chunks.size() + 
                             " (" + chunk.length() + " chars) ---");

            String translatedChunk = translateChunk(chunk, sourceLang, targetLang);
            translatedText.append(translatedChunk);

            if (i < chunks.size() - 1) {
                System.out.println("Waiting " + DELAY_BETWEEN_REQUESTS + "ms before next chunk...");
                Thread.sleep(DELAY_BETWEEN_REQUESTS);
            }
        }

        System.out.println("\n✓ All chunks translated successfully!");
        return translatedText.toString();
    }

    private List<String> splitTextIntoChunks(String text) {
        List<String> chunks = new ArrayList<>();

        if (text.length() <= MAX_CHUNK_SIZE) {
            chunks.add(text);
            return chunks;
        }

        String[] paragraphs = text.split("\n\n");
        StringBuilder currentChunk = new StringBuilder();

        for (String paragraph : paragraphs) {
            if (paragraph.length() > MAX_CHUNK_SIZE) {
                if (currentChunk.length() > 0) {
                    chunks.add(currentChunk.toString());
                    currentChunk = new StringBuilder();
                }

                String[] sentences = paragraph.split("(?<=[.!?])\n|(?<=[.!?])\\s+");
                for (String sentence : sentences) {
                    if (sentence.trim().isEmpty()) continue;

                    if (sentence.length() > MAX_CHUNK_SIZE) {
                        if (currentChunk.length() > 0) {
                            chunks.add(currentChunk.toString());
                            currentChunk = new StringBuilder();
                        }
                        chunks.addAll(splitByCharacterLimit(sentence));
                    } else if (currentChunk.length() + sentence.length() + 1 > MAX_CHUNK_SIZE) {
                        chunks.add(currentChunk.toString());
                        currentChunk = new StringBuilder(sentence);
                    } else {
                        if (currentChunk.length() > 0) {
                            currentChunk.append(" ");
                        }
                        currentChunk.append(sentence);
                    }
                }
            } else {
                if (currentChunk.length() + paragraph.length() + 2 > MAX_CHUNK_SIZE) {
                    if (currentChunk.length() > 0) {
                        chunks.add(currentChunk.toString());
                        currentChunk = new StringBuilder();
                    }
                }

                if (currentChunk.length() > 0) {
                    currentChunk.append("\n\n");
                }
                currentChunk.append(paragraph);
            }
        }

        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString());
        }

        return chunks;
    }

    private List<String> splitByCharacterLimit(String text) {
        List<String> chunks = new ArrayList<>();
        int start = 0;

        while (start < text.length()) {
            int end = Math.min(start + MAX_CHUNK_SIZE, text.length());

            if (end < text.length()) {
                int lastSpace = text.lastIndexOf(' ', end);
                if (lastSpace > start) {
                    end = lastSpace;
                }
            }

            chunks.add(text.substring(start, end).trim());
            start = end + 1;
        }

        return chunks;
    }

    private String translateChunk(String text, String sourceLang, String targetLang) throws Exception {
        try {
            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8.name());
            String langPair = sourceLang + "|" + targetLang;
            String urlString = API_BASE_URL + "?q=" + encodedText + "&langpair=" + langPair;

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Accept-Charset", "UTF-8");
            conn.setRequestProperty("Accept-Language", "en-US,en;q=0.9");
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setUseCaches(false);
            conn.setDoInput(true);

            long startTime = System.currentTimeMillis();

            int responseCode = conn.getResponseCode();
            long responseTime = System.currentTimeMillis() - startTime;
            
            System.out.println("Response: " + responseCode + " (took " + responseTime + "ms)");

            if (responseCode != 200) {
                String errorBody = readStream(conn.getErrorStream());
                System.err.println("Error response: " + errorBody);
                throw new Exception("Translation API error: HTTP " + responseCode + "\n" + errorBody);
            }

            String responseBody = readStream(conn.getInputStream());

            JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
            
            if (!jsonResponse.has("responseData")) {
                throw new Exception("Invalid API response: missing 'responseData' field");
            }

            JsonObject responseData = jsonResponse.getAsJsonObject("responseData");
            
            if (!responseData.has("translatedText")) {
                throw new Exception("Invalid API response: missing 'translatedText' field");
            }

            String translatedText = responseData.get("translatedText").getAsString();
            
            if (translatedText == null || translatedText.trim().isEmpty()) {
                throw new Exception("Empty translation result");
            }

            System.out.println("✓ Chunk translated: " + translatedText.substring(0, Math.min(80, translatedText.length())) + "...");
            
            return translatedText;

        } catch (SocketTimeoutException e) {
            System.err.println("❌ Socket Timeout Exception: " + e.getMessage());
            throw new Exception("Connection timeout. Please check your internet connection and try again.");
        } catch (java.net.UnknownHostException e) {
            System.err.println("❌ Unknown Host Exception: " + e.getMessage());
            throw new Exception("Cannot reach translation server. Please check your internet connection.");
        } catch (java.io.IOException e) {
            System.err.println("❌ IO Exception: " + e.getClass().getName() + " - " + e.getMessage());
            throw new Exception("Network error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Exception: " + e.getClass().getName() + " - " + e.getMessage());
            throw e;
        }
    }

    private String readStream(java.io.InputStream stream) throws Exception {
        if (stream == null) {
            return "";
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
        }
        return response.toString();
    }

    private boolean isValidLanguageCode(String code) {
        return code != null && (code.equals("ar") || code.equals("fr") || code.equals("en"));
    }
}
