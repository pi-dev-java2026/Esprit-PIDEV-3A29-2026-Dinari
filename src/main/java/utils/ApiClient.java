package utils;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
public class ApiClient {

    public static String predictDepense(int month) {

        try {

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(
                            "http://127.0.0.1:8000/predict?month=" + month))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            return response.body();

        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur prediction";
        }
    }

    public static void retrainModel() throws Exception {
        URL url = new URL("http://127.0.0.1:8000/retrain");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");

        int responseCode = conn.getResponseCode();
        System.out.println("Training response : " + responseCode);
    }
}
