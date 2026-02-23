package tn.esprit.services;

import tn.esprit.entities.Abonnement;
import tn.esprit.utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AbonnementService implements IService<Abonnement> {

    private Connection connection;

    public AbonnementService() {
        connection = MyConnection.getInstance().getConnection();
    }

    @Override
    public void ajouter(Abonnement abonnement) {
        // ← MODIFIÉ : ajout de "tier" dans la requête
        String query = "INSERT INTO abonnement (nom, prix, date_debut, frequence, categorie, actif, image_path, tier) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, abonnement.getNom());
            ps.setDouble(2, abonnement.getPrix());
            ps.setDate(3, abonnement.getDateDebut());
            ps.setString(4, abonnement.getFrequence());
            ps.setString(5, abonnement.getCategorie());
            ps.setBoolean(6, abonnement.isActif());
            ps.setString(7, abonnement.getImagePath());
            ps.setString(8, abonnement.getTier() != null ? abonnement.getTier() : "Normal"); // ← NOUVEAU

            ps.executeUpdate();
            System.out.println("✅ Abonnement ajouté !");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void modifier(Abonnement abonnement) {
        // ← MODIFIÉ : ajout de "tier" dans la requête
        String query = "UPDATE abonnement SET nom=?, prix=?, date_debut=?, frequence=?, categorie=?, actif=?, image_path=?, tier=? WHERE id=?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, abonnement.getNom());
            ps.setDouble(2, abonnement.getPrix());
            ps.setDate(3, abonnement.getDateDebut());
            ps.setString(4, abonnement.getFrequence());
            ps.setString(5, abonnement.getCategorie());
            ps.setBoolean(6, abonnement.isActif());
            ps.setString(7, abonnement.getImagePath());
            ps.setString(8, abonnement.getTier() != null ? abonnement.getTier() : "Normal"); // ← NOUVEAU
            ps.setInt(9, abonnement.getId());

            ps.executeUpdate();
            System.out.println("✅ Abonnement modifié !");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void supprimer(int id) {
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

            if (rs.next()) {
                return mapResultSetToAbonnement(rs);
            }

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

            while (rs.next()) {
                liste.add(mapResultSetToAbonnement(rs));
            }

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

            while (rs.next()) {
                liste.add(mapResultSetToAbonnement(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return liste;
    }

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
        a.setTier(rs.getString("tier")); // ← NOUVEAU
        return a;
    }
}