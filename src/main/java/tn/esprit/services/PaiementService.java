package tn.esprit.services;

import tn.esprit.entities.Paiement;
import tn.esprit.utils.MyConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaiementService implements IService<Paiement> {

    private Connection connection;

    public PaiementService() {
        connection = MyConnection.getInstance().getConnection();
    }

    @Override
    public void ajouter(Paiement paiement) {
        String query = "INSERT INTO paiement (montant, date_paiement, statut, abonnement_id, " +
                "nom_titulaire, prenom_titulaire, mode_paiement, numero_carte, date_expiration, cvv) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setDouble(1, paiement.getMontant());
            ps.setDate(2, paiement.getDatePaiement());
            ps.setString(3, paiement.getStatut());
            ps.setInt(4, paiement.getAbonnementId());
            ps.setString(5, paiement.getNomTitulaire());
            ps.setString(6, paiement.getPrenomTitulaire());
            ps.setString(7, paiement.getModePaiement());
            ps.setString(8, paiement.getNumeroCarte());
            ps.setString(9, paiement.getDateExpiration());
            ps.setString(10, paiement.getCvv());

            ps.executeUpdate();
            System.out.println("✅ Paiement ajouté !");

        } catch (SQLException e) {
            System.err.println("❌ Erreur ajout paiement :");
            e.printStackTrace();
        }
    }

    @Override
    public void modifier(Paiement paiement) {
        String query = "UPDATE paiement SET montant=?, date_paiement=?, statut=?, " +
                "nom_titulaire=?, prenom_titulaire=?, mode_paiement=?, " +
                "numero_carte=?, date_expiration=?, cvv=? WHERE id=?";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setDouble(1, paiement.getMontant());
            ps.setDate(2, paiement.getDatePaiement());
            ps.setString(3, paiement.getStatut());
            ps.setString(4, paiement.getNomTitulaire());
            ps.setString(5, paiement.getPrenomTitulaire());
            ps.setString(6, paiement.getModePaiement());
            ps.setString(7, paiement.getNumeroCarte());
            ps.setString(8, paiement.getDateExpiration());
            ps.setString(9, paiement.getCvv());
            ps.setInt(10, paiement.getId());

            ps.executeUpdate();
            System.out.println("✅ Paiement modifié !");

        } catch (SQLException e) {
            System.err.println("❌ Erreur modification :");
            e.printStackTrace();
        }
    }

    @Override
    public void supprimer(int id) {
        String query = "DELETE FROM paiement WHERE id=?";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("✅ Paiement supprimé !");

        } catch (SQLException e) {
            System.err.println("❌ Erreur suppression :");
            e.printStackTrace();
        }
    }

    @Override
    public List<Paiement> afficher() {
        List<Paiement> paiements = new ArrayList<>();
        String query = "SELECT * FROM paiement";

        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query);

            while (rs.next()) {
                Paiement p = new Paiement();
                p.setId(rs.getInt("id"));
                p.setMontant(rs.getDouble("montant"));
                p.setDatePaiement(rs.getDate("date_paiement"));
                p.setStatut(rs.getString("statut"));
                p.setAbonnementId(rs.getInt("abonnement_id"));
                p.setNomTitulaire(rs.getString("nom_titulaire"));
                p.setPrenomTitulaire(rs.getString("prenom_titulaire"));
                p.setModePaiement(rs.getString("mode_paiement"));
                p.setNumeroCarte(rs.getString("numero_carte"));
                p.setDateExpiration(rs.getString("date_expiration"));
                p.setCvv(rs.getString("cvv"));

                paiements.add(p);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération paiements :");
            e.printStackTrace();
        }

        return paiements;
    }

    public Paiement findById(int id) {
        String query = "SELECT * FROM paiement WHERE id=?";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Paiement p = new Paiement();
                p.setId(rs.getInt("id"));
                p.setMontant(rs.getDouble("montant"));
                p.setDatePaiement(rs.getDate("date_paiement"));
                p.setStatut(rs.getString("statut"));
                p.setAbonnementId(rs.getInt("abonnement_id"));
                p.setNomTitulaire(rs.getString("nom_titulaire"));
                p.setPrenomTitulaire(rs.getString("prenom_titulaire"));
                p.setModePaiement(rs.getString("mode_paiement"));
                p.setNumeroCarte(rs.getString("numero_carte"));
                p.setDateExpiration(rs.getString("date_expiration"));
                p.setCvv(rs.getString("cvv"));
                return p;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}