package org.example.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ExchangeRateService {

    private static final String API_KEY = "6dd05f438c4096167966134f";


    public double getRate(String from, String to) throws Exception {
        from = from.toUpperCase();
        to = to.toUpperCase();

        if (from.equals(to)) return 1.0;

        String urlStr = "https://v6.exchangerate-api.com/v6/" + API_KEY + "/latest/" + from;

        URL url = new URL(urlStr);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        int code = con.getResponseCode();
        if (code != 200) {
            throw new RuntimeException("Erreur API taux de change, code HTTP: " + code);
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) sb.append(line);
        in.close();

        String json = sb.toString();


        String key = "\"" + to + "\":";
        int idx = json.indexOf(key);
        if (idx == -1) throw new RuntimeException("Devise non trouvée dans la réponse: " + to);

        int start = idx + key.length();
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '.')) {
            end++;
        }

        return Double.parseDouble(json.substring(start, end));
    }
}