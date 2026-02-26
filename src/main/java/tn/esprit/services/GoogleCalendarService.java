package tn.esprit.services;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.*;
import tn.esprit.entities.Abonnement;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;

/**
 * Service Google Calendar API
 *
 * ┌─────────────────────────────────────────────────────┐
 * │  MODE_DEMO = true  → événement dans 3 minutes       │
 * │                      (pour démo devant le prof ✅)  │
 * │                                                     │
 * │  MODE_DEMO = false → événement à la vraie date      │
 * │                      d'expiration (mode réel)       │
 * └─────────────────────────────────────────────────────┘
 */
public class GoogleCalendarService {

    // ⬇️ Change à FALSE après la démo pour le mode réel
    private static final boolean MODE_DEMO = true;

    // Délai en minutes pour la démo
    private static final int DEMO_DELAI_MINUTES = 3;

    /**
     * Crée automatiquement un rappel email dans Google Calendar
     * quand un abonnement est ajouté.
     *
     * @param ab L'abonnement qui vient d'être ajouté
     * @return L'ID de l'événement Google Calendar créé
     */
    public String creerRappelExpiration(Abonnement ab) {
        try {
            Calendar service = GoogleCalendarConfig.getCalendarService();

            // ── 1. Calculer la date de l'événement ──────────────────
            DateTime dateEvenement;
            String descriptionDate;

            if (MODE_DEMO) {
                // 🎬 DÉMO : événement dans DEMO_DELAI_MINUTES minutes
                long ts = System.currentTimeMillis() + (DEMO_DELAI_MINUTES * 60 * 1000L);
                dateEvenement = new DateTime(ts);
                descriptionDate = "DÉMO — Email dans " + DEMO_DELAI_MINUTES + " minutes";
                System.out.println("🎬 MODE DÉMO : email dans " + DEMO_DELAI_MINUTES + " minutes");

            } else {
                // 🔵 RÉEL : calculer date de fin selon fréquence
                LocalDate dateFin = calculerDateFin(ab);
                long ts = dateFin.atStartOfDay(ZoneId.of("Africa/Tunis"))
                        .toInstant().toEpochMilli();
                dateEvenement = new DateTime(ts);
                descriptionDate = "Date d'expiration : " + dateFin.toString();
                System.out.println("🔵 MODE RÉEL : email le " + dateFin);
            }

            // ── 2. Créer l'événement Google Calendar ─────────────────
            Event event = new Event()
                    .setSummary("⚠️ " + ab.getNom() + " — Abonnement expire aujourd'hui !")
                    .setDescription(
                            "📋 Détails de votre abonnement :\n\n" +
                                    "Nom        : " + ab.getNom() + "\n" +
                                    "Prix       : " + String.format("%.2f", ab.getPrix()) + " TND\n" +
                                    "Fréquence  : " + ab.getFrequence() + "\n" +
                                    "Catégorie  : " + (ab.getCategorie() != null ? ab.getCategorie() : "Non définie") + "\n" +
                                    "Plan       : " + (ab.getTier() != null ? ab.getTier() : "Normal") + "\n\n" +
                                    descriptionDate + "\n\n" +
                                    "⚡ Action requise : Renouveler ou annuler votre abonnement !"
                    )
                    .setColorId("11"); // Rouge dans Google Calendar

            // Heure début = dateEvenement
            EventDateTime start = new EventDateTime()
                    .setDateTime(dateEvenement)
                    .setTimeZone("Africa/Tunis");

            // Heure fin = début + 30 minutes
            DateTime dateFin30 = new DateTime(dateEvenement.getValue() + (30 * 60 * 1000L));
            EventDateTime end = new EventDateTime()
                    .setDateTime(dateFin30)
                    .setTimeZone("Africa/Tunis");

            event.setStart(start);
            event.setEnd(end);

            // ── 3. Rappel EMAIL au moment exact de l'événement ───────
            EventReminder emailReminder = new EventReminder()
                    .setMethod("email")
                    .setMinutes(0); // 0 = email envoyé exactement à l'heure de l'événement

            event.setReminders(new Event.Reminders()
                    .setUseDefault(false)
                    .setOverrides(Collections.singletonList(emailReminder))
            );

            // ── 4. Insérer dans Google Calendar ──────────────────────
            Event created = service.events()
                    .insert("primary", event)
                    .execute();

            System.out.println("✅ Rappel créé dans Google Calendar !");
            System.out.println("🔗 Lien : " + created.getHtmlLink());

            return created.getId();

        } catch (Exception e) {
            System.err.println("❌ Erreur Google Calendar : " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Supprime un rappel (utile quand l'abonnement est renouvelé ou supprimé)
     *
     * @param eventId L'ID retourné par creerRappelExpiration()
     */
    public void supprimerRappel(String eventId) {
        if (eventId == null || eventId.isEmpty()) return;
        try {
            Calendar service = GoogleCalendarConfig.getCalendarService();
            service.events().delete("primary", eventId).execute();
            System.out.println("🗑️ Rappel Google Calendar supprimé : " + eventId);
        } catch (Exception e) {
            System.err.println("❌ Erreur suppression Calendar : " + e.getMessage());
        }
    }

    /**
     * Calcule la date de fin d'un abonnement selon sa fréquence et date de début.
     * Utilisé en mode RÉEL uniquement.
     *
     * Mensuel  → date_debut + 1 mois
     * Annuel   → date_debut + 1 an
     * Autre    → date_debut + 1 mois (défaut)
     */
    private LocalDate calculerDateFin(Abonnement ab) {
        // Convertir java.sql.Date → LocalDate
        LocalDate debut = ab.getDateDebut().toLocalDate();

        if ("Annuel".equalsIgnoreCase(ab.getFrequence())) {
            return debut.plusYears(1);
        } else {
            // Mensuel ou autre
            return debut.plusMonths(1);
        }
    }
}