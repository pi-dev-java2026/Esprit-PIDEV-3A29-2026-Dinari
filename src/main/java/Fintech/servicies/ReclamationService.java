package Fintech.servicies;

import Fintech.entities.Reclamation;
import Fintech.utils.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ReclamationService {
    private Connection cnx;

    public ReclamationService() {
        cnx = MyDataBase.getInstance().getMyConnection();
    }

    public boolean creerReclamation(Reclamation r) {
        String query = "INSERT INTO reclamation (email, subject, description, statut) VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement pst = cnx.prepareStatement(query);
            pst.setString(1, r.getEmail());
            pst.setString(2, r.getSubject()); // The "type"
            pst.setString(3, r.getDescription()); // The "description"
            pst.setString(4, r.getStatut()); // e.g. "Ouverte"

            int result = pst.executeUpdate();
            return result > 0;
        } catch (SQLException ex) {
            System.err.println("Erreur lors de la création de la réclamation : " + ex.getMessage());
            return false;
        }
    }

    public List<Reclamation> getReclamationsByEmail(String email) {
        List<Reclamation> list = new ArrayList<>();
        String query = "SELECT * FROM reclamation WHERE email = ?";
        try {
            PreparedStatement pst = cnx.prepareStatement(query);
            pst.setString(1, email);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Reclamation r = new Reclamation();
                r.setId_reclamation(rs.getInt(1)); // Use index 1 because the DB column might be named 'id-reclamation'
                r.setEmail(rs.getString("email"));
                r.setSubject(rs.getString("subject"));
                r.setDescription(rs.getString("description"));
                r.setStatut(rs.getString("statut"));
                list.add(r);
            }
        } catch (SQLException ex) {
            System.err.println("Erreur lors de la récupération des réclamations : " + ex.getMessage());
        }
        return list;
    }
}
