package Fintech.utils;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfo;

import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for handling Google OAuth2 authentication.
 * Opens the default browser for user consent and captures the result
 * via a local callback server on port 8888.
 *
 * FIX: Each session uses a unique ID to prevent cached credentials from
 * a previous Google account being reused (which caused "account already exists"
 * errors).
 */
public class GoogleAuthService {

        private static final String CLIENT_ID = "88482424995-oj6sueqfee04ha0r457jml5dpojhst90.apps.googleusercontent.com";
        private static final String CLIENT_SECRET = "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";
        private static final String APPLICATION_NAME = "Fintech App";
        private static final List<String> SCOPES = Arrays.asList(
                        "https://www.googleapis.com/auth/userinfo.email",
                        "https://www.googleapis.com/auth/userinfo.profile");

        /**
         * Starts the Google OAuth2 flow and returns the authenticated user's info.
         *
         * @return A Map containing "name" and "email" keys from Google profile,
         *         or null if authentication failed.
         */
        public static Map<String, String> getUserInfo() {
                try {
                        // Build client secrets from constants (no file needed)
                        String clientSecretsJson = "{"
                                        + "\"installed\":{"
                                        + "\"client_id\":\"" + CLIENT_ID + "\","
                                        + "\"client_secret\":\"" + CLIENT_SECRET + "\","
                                        + "\"redirect_uris\":[\"http://localhost\"],"
                                        + "\"auth_uri\":\"https://accounts.google.com/o/oauth2/auth\","
                                        + "\"token_uri\":\"https://oauth2.googleapis.com/token\""
                                        + "}}";

                        GsonFactory jsonFactory = GsonFactory.getDefaultInstance();
                        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                                        jsonFactory, new StringReader(clientSecretsJson));

                        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

                        // No dataStore = no credential caching, always prompts account chooser
                        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                                        httpTransport, jsonFactory, clientSecrets, SCOPES)
                                        .setAccessType("online")
                                        .build();

                        // Local server on port 8888 to capture the auth code
                        LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                                        .setPort(8888)
                                        .build();

                        // Launch browser and wait for user to complete sign-in
                        // Unique session ID per login → prevents stale credential reuse
                        String sessionId = "dinari_" + System.currentTimeMillis();
                        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize(sessionId);

                        // Build the OAuth2 service to fetch user info
                        Oauth2 oauth2 = new Oauth2.Builder(httpTransport, jsonFactory, credential)
                                        .setApplicationName(APPLICATION_NAME)
                                        .build();

                        Userinfo userInfo = oauth2.userinfo().get().execute();

                        Map<String, String> result = new HashMap<>();
                        result.put("name", userInfo.getName() != null ? userInfo.getName() : "");
                        result.put("email", userInfo.getEmail() != null ? userInfo.getEmail() : "");
                        return result;

                } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                }
        }
}
