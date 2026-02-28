package tn.esprit.services;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.*;
import tn.esprit.entities.Abonnement;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class GoogleCalendarService {

    // ⬇️ Change à FALSE après la démo pour le mode réel
    private static final boolean MODE_DEMO = true;

    // Événement dans 12 min, rappel email 10 min avant → email reçu dans 2 min ✅
    private static final int DEMO_DELAI_MINUTES         = 12;
    private static final int RAPPEL_EMAIL_AVANT_MINUTES = 10;

    public String creerRappelExpiration(Abonnement ab) {
        try {
            Calendar service = GoogleCalendarConfig.getCalendarService();

            // ── Toujours calculer la VRAIE date d'expiration ──────────────
            LocalDate vraieDateFin = calculerDateFin(ab);
            String vraieDateStr = vraieDateFin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

            DateTime dateEvenement;
            String titreEvenement;
            String descriptionDate;

            if (MODE_DEMO) {
                // 🎬 DÉMO : événement dans DEMO_DELAI_MINUTES minutes
                // MAIS le titre affiche la VRAIE date d'expiration
                long ts = System.currentTimeMillis() + (DEMO_DELAI_MINUTES * 60 * 1000L);
                dateEvenement = new DateTime(ts);

                // ✅ Titre correct avec la vraie date
                titreEvenement = "⚠️ " + ab.getNom() + " — Expire le " + vraieDateStr;
                descriptionDate = "📅 Date d'expiration réelle : " + vraieDateStr +
                        "\n🎬 DÉMO : email dans ~" +
                        (DEMO_DELAI_MINUTES - RAPPEL_EMAIL_AVANT_MINUTES) + " minutes";

                System.out.println("🎬 MODE DÉMO : événement dans " + DEMO_DELAI_MINUTES +
                        " min → email dans ~" + (DEMO_DELAI_MINUTES - RAPPEL_EMAIL_AVANT_MINUTES) + " min");
                System.out.println("📅 Vraie date expiration : " + vraieDateStr);

            } else {
                // 🔵 MODE RÉEL : événement à la vraie date d'expiration
                long ts = vraieDateFin.atTime(9, 0)
                        .atZone(ZoneId.of("Africa/Tunis"))
                        .toInstant().toEpochMilli();
                dateEvenement = new DateTime(ts);

                titreEvenement = "⚠️ " + ab.getNom() + " — Abonnement expire aujourd'hui !";
                descriptionDate = "📅 Date d'expiration : " + vraieDateStr;

                System.out.println("🔵 MODE RÉEL : email le " + vraieDateStr + " à 09h00");
            }

            // ── Créer l'événement ─────────────────────────────────────────
            Event event = new Event()
                    .setSummary(titreEvenement)
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
                    .setColorId("11"); // Rouge

            EventDateTime start = new EventDateTime()
                    .setDateTime(dateEvenement)
                    .setTimeZone("Africa/Tunis");

            DateTime fin = new DateTime(dateEvenement.getValue() + (30L * 60 * 1000));
            EventDateTime end = new EventDateTime()
                    .setDateTime(fin)
                    .setTimeZone("Africa/Tunis");

            event.setStart(start);
            event.setEnd(end);

            // ── Rappels ───────────────────────────────────────────────────
            EventReminder emailReminder = new EventReminder()
                    .setMethod("email")
                    .setMinutes(RAPPEL_EMAIL_AVANT_MINUTES);

            EventReminder popupReminder = new EventReminder()
                    .setMethod("popup")
                    .setMinutes(RAPPEL_EMAIL_AVANT_MINUTES);

            event.setReminders(new Event.Reminders()
                    .setUseDefault(false)
                    .setOverrides(Arrays.asList(emailReminder, popupReminder))
            );

            Event created = service.events()
                    .insert("primary", event)
                    .execute();

            System.out.println("✅ Rappel créé dans Google Calendar !");
            System.out.println("📧 Email attendu dans ~" +
                    (DEMO_DELAI_MINUTES - RAPPEL_EMAIL_AVANT_MINUTES) + " minutes");
            System.out.println("🔗 Lien : " + created.getHtmlLink());

            return created.getId();

        } catch (Exception e) {
            System.err.println("❌ Erreur Google Calendar : " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public void supprimerRappel(String eventId) {
        if (eventId == null || eventId.isEmpty()) return;
        try {
            Calendar service = GoogleCalendarConfig.getCalendarService();
            service.events().delete("primary", eventId).execute();
            System.out.println("🗑️ Rappel supprimé : " + eventId);
        } catch (Exception e) {
            System.err.println("❌ Erreur suppression : " + e.getMessage());
        }
    }

    /**
     * Calcule la vraie date d'expiration selon fréquence
     * Mensuel  → date_debut + 1 mois
     * Annuel   → date_debut + 1 an
     */
    private LocalDate calculerDateFin(Abonnement ab) {
        LocalDate debut = ab.getDateDebut().toLocalDate();
        if ("Annuel".equalsIgnoreCase(ab.getFrequence())) return debut.plusYears(1);
        return debut.plusMonths(1);
    }
}