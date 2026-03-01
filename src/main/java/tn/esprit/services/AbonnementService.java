package tn.esprit.services;

import tn.esprit.entities.Abonnement;
import tn.esprit.utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AbonnementService implements IService<Abonnement> {

    private Connection connection;

    // ← NOUVEAU : service Google Calendar
    private final GoogleCalendarService calendarService = new GoogleCalendarService();

    public AbonnementService() {
        connection = MyConnection.getInstance().getConnection();
    }

    @Override
    public void ajouter(Abonnement abonnement) {
        // Requête avec google_calendar_event_id pour sauvegarder l'ID de l'événement
        String query = "INSERT INTO abonnement (nom, prix, date_debut, frequence, categorie, actif, image_path, tier, google_calendar_event_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, abonnement.getNom());
            ps.setDouble(2, abonnement.getPrix());
            ps.setDate(3, abonnement.getDateDebut());
            ps.setString(4, abonnement.getFrequence());
            ps.setString(5, abonnement.getCategorie());
            ps.setBoolean(6, abonnement.isActif());
            ps.setString(7, abonnement.getImagePath());
            ps.setString(8, abonnement.getTier() != null ? abonnement.getTier() : "Normal");
            ps.setString(9, null); // on va update après avec l'event ID

            ps.executeUpdate();

            // Récupérer l'ID généré automatiquement
            ResultSet generatedKeys = ps.getGeneratedKeys();
            if (generatedKeys.next()) {
                abonnement.setId(generatedKeys.getInt(1));
            }

            System.out.println("✅ Abonnement ajouté avec ID : " + abonnement.getId());

            // ── NOUVEAU : Créer rappel Google Calendar dans un thread séparé ──
            // Thread séparé pour ne pas bloquer l'UI JavaFX
            new Thread(() -> {
                System.out.println("📅 Création du rappel Google Calendar pour : " + abonnement.getNom());

                String eventId = calendarService.creerRappelExpiration(abonnement);

                if (eventId != null) {
                    // Sauvegarder l'eventId dans la DB
                    sauvegarderEventId(abonnement.getId(), eventId);
                    System.out.println("✅ Event ID sauvegardé : " + eventId);
                } else {
                    System.err.println("⚠️ Rappel Calendar non créé pour : " + abonnement.getNom());
                }
            }).start();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void modifier(Abonnement abonnement) {
        String query = "UPDATE abonnement SET nom=?, prix=?, date_debut=?, frequence=?, categorie=?, actif=?, image_path=?, tier=? WHERE id=?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, abonnement.getNom());
            ps.setDouble(2, abonnement.getPrix());
            ps.setDate(3, abonnement.getDateDebut());
            ps.setString(4, abonnement.getFrequence());
            ps.setString(5, abonnement.getCategorie());
            ps.setBoolean(6, abonnement.isActif());
            ps.setString(7, abonnement.getImagePath());
            ps.setString(8, abonnement.getTier() != null ? abonnement.getTier() : "Normal");
            ps.setInt(9, abonnement.getId());

            ps.executeUpdate();
            System.out.println("✅ Abonnement modifié !");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void supprimer(int id) {
        // ── NOUVEAU : Supprimer d'abord l'événement Google Calendar ──
        new Thread(() -> {
            String eventId = getEventId(id);
            if (eventId != null) {
                calendarService.supprimerRappel(eventId);
            }
        }).start();

        String query = "DELETE FROM abonnement WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("✅ Abonnement supprimé !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Abonnement> afficher() {
        List<Abonnement> liste = new ArrayList<>();
        String query = "SELECT * FROM abonnement";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                liste.add(mapResultSetToAbonnement(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return liste;
    }

    public Abonnement findById(int id) {
        String query = "SELECT * FROM abonnement WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapResultSetToAbonnement(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Abonnement> afficherParCategorie(String categorie) {
        List<Abonnement> liste = new ArrayList<>();
        String query = "SELECT * FROM abonnement WHERE categorie = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, categorie);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(mapResultSetToAbonnement(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return liste;
    }

    public List<Abonnement> rechercher(String keyword) {
        List<Abonnement> liste = new ArrayList<>();
        String query = "SELECT * FROM abonnement WHERE nom LIKE ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(mapResultSetToAbonnement(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return liste;
    }

    // ── NOUVEAUX HELPERS Calendar ─────────────────────────────────────────

    /**
     * Sauvegarde l'ID de l'événement Google Calendar dans la DB
     */
    private void sauvegarderEventId(int abonnementId, String eventId) {
        String query = "UPDATE abonnement SET google_calendar_event_id = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, eventId);
            ps.setInt(2, abonnementId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Récupère l'ID de l'événement Google Calendar depuis la DB
     */
    private String getEventId(int abonnementId) {
        String query = "SELECT google_calendar_event_id FROM abonnement WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, abonnementId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("google_calendar_event_id");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ── MAPPING ──────────────────────────────────────────────────────────

    private Abonnement mapResultSetToAbonnement(ResultSet rs) throws SQLException {
        Abonnement a = new Abonnement();
        a.setId(rs.getInt("id"));
        a.setNom(rs.getString("nom"));
        a.setPrix(rs.getDouble("prix"));
        a.setDateDebut(rs.getDate("date_debut"));
        a.setFrequence(rs.getString("frequence"));
        a.setCategorie(rs.getString("categorie"));
        a.setActif(rs.getBoolean("actif"));
        a.setImagePath(rs.getString("image_path"));
        a.setTier(rs.getString("tier"));
        // google_calendar_event_id n'est pas dans l'entité, juste en DB
        return a;
    }
}