package org.example.services;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.example.entities.MeteoDay;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;

public class MeteoService {

    private static final double LAT = 36.8065; // Tunis
    private static final double LON = 10.1815;

    public MeteoDay getMeteoForDate(LocalDate date) throws Exception {

        String url = "https://api.open-meteo.com/v1/forecast"
                + "?latitude=" + LAT
                + "&longitude=" + LON
                + "&daily=precipitation_sum,temperature_2m_max"
                + "&timezone=Africa/Tunis";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        String json = client.send(request, HttpResponse.BodyHandlers.ofString()).body();

        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        JsonObject daily = root.getAsJsonObject("daily");

        int idx = findIndex(daily.getAsJsonArray("time"), date);
        if (idx == -1) return null; // date hors 16 jours

        double temp = daily.getAsJsonArray("temperature_2m_max").get(idx).getAsDouble();
        double rain = daily.getAsJsonArray("precipitation_sum").get(idx).getAsDouble();

        return new MeteoDay(date, temp, rain);
    }

    private int findIndex(JsonArray times, LocalDate date) {
        String target = date.toString();
        for (int i = 0; i < times.size(); i++) {
            if (times.get(i).getAsString().equals(target)) return i;
        }
        return -1;
    }
}