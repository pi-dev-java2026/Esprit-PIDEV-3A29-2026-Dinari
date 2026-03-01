package tn.esprit.services;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;

import java.io.*;
import java.util.Collections;
import java.util.List;

/**
 * Configuration OAuth2 pour Google Calendar API
 * La première fois → ouvre le navigateur pour que l'utilisateur se connecte
 * Ensuite → le token est sauvegardé dans /tokens (pas besoin de se reconnecter)
 */
public class GoogleCalendarConfig {

    private static final String APPLICATION_NAME = "FinTech Abonnement";
    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    // Dossier où le token OAuth2 sera sauvegardé après le premier login
    private static final String TOKENS_DIR = "tokens";

    // Scope : accès en écriture au Google Calendar
    private static final List<String> SCOPES =
            Collections.singletonList(CalendarScopes.CALENDAR);

    /**
     * Retourne un service Calendar authentifié.
     * ⚠️ La première fois → ouvre le navigateur Google pour login
     * ✅ Ensuite → utilise le token sauvegardé automatiquement
     */
    public static Calendar getCalendarService() throws Exception {

        // 1. Charger credentials.json depuis src/main/resources/
        InputStream in = GoogleCalendarConfig.class
                .getResourceAsStream("/credentials.json");

        if (in == null) {
            throw new FileNotFoundException(
                    "❌ credentials.json introuvable !\n" +
                            "→ Va sur https://console.cloud.google.com\n" +
                            "→ Crée un projet, active Google Calendar API\n" +
                            "→ Crée un OAuth2 Desktop Client\n" +
                            "→ Télécharge credentials.json dans src/main/resources/"
            );
        }

        // 2. Lire les credentials
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                JSON_FACTORY, new InputStreamReader(in));

        // 3. Construire le flow OAuth2
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                clientSecrets,
                SCOPES
        )
                .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIR)))
                .setAccessType("offline")
                .build();

        // 4. Ouvrir navigateur pour login (seulement la 1ère fois)
        LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                .setPort(8888)
                .build();

        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver)
                .authorize("user");

        // 5. Retourner le service Calendar prêt à utiliser
        return new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                credential
        ).setApplicationName(APPLICATION_NAME).build();
    }
}