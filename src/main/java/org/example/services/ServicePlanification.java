package org.example.services;

import org.example.entities.Planification;
import org.example.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServicePlanification implements IService<Planification> {

    private Connection connection;

    public ServicePlanification() {
        connection = MyDataBase.getInstance().getMyConnection();
    }

    @Override
    public void ajouter(Planification planification) throws SQLException {

        String sql = "INSERT INTO planification (categorie, montant_alloue, priorite, mois) " +
                "VALUES (?, ?, ?, ?)";

        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, planification.getCategorie());
        ps.setDouble(2, planification.getMontantAlloue());
        ps.setString(3, planification.getPriorite());
        ps.setString(4, planification.getMois());

        ps.executeUpdate();
    }

    @Override
    public void modifier(Planification planification) throws SQLException {

        String sql = "UPDATE planification SET categorie=?, montant_alloue=?, priorite=?, mois=? " +
                "WHERE id=?";

        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, planification.getCategorie());
        ps.setDouble(2, planification.getMontantAlloue());
        ps.setString(3, planification.getPriorite());
        ps.setString(4, planification.getMois());
        ps.setInt(5, planification.getId());

        ps.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {

        String sql = "DELETE FROM planification WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public List<Planification> afficher() throws SQLException {

        List<Planification> planifications = new ArrayList<>();
        String sql = "SELECT * FROM planification";

        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(sql);

        while (rs.next()) {

            Planification p = new Planification();
            p.setId(rs.getInt("id"));
            p.setCategorie(rs.getString("categorie"));
            p.setMontantAlloue(rs.getDouble("montant_alloue"));
            p.setPriorite(rs.getString("priorite"));
            p.setMois(rs.getString("mois"));

            planifications.add(p);
        }

        return planifications;
    }
    public double getMontantAlloue(String categorie, String mois) throws SQLException {
        String sql = "SELECT montant_alloue FROM planification WHERE categorie = ? AND mois = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, categorie);
        ps.setString(2, mois);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return rs.getDouble("montant_alloue");
        return 0;
    }


    public boolean existsPlanification(String categorie, String moisYYYYMM) throws SQLException {
        String sql = "SELECT 1 FROM planification WHERE categorie = ? AND mois = ? LIMIT 1";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, categorie);
        ps.setString(2, moisYYYYMM);
        ResultSet rs = ps.executeQuery();
        return rs.next();
    }
    public List<String> getDistinctCategories() throws SQLException {
        List<String> cats = new ArrayList<>();

        String sql = "SELECT DISTINCT categorie " +
                "FROM planification " +
                "WHERE categorie IS NOT NULL AND TRIM(categorie) <> '' " +
                "ORDER BY categorie";

        PreparedStatement ps = connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            cats.add(rs.getString("categorie"));
        }
        return cats;
    }
    public List<String[]> getEvolutionCategorie(String categorie, String moisDebut, String moisFin) throws Exception {
        List<String[]> res = new ArrayList<>();

        String order =
                "FIELD(mois,'janvier','février','mars','avril','mai','juin','juillet','août','septembre','octobre','novembre','décembre')";

        String sql =
                "SELECT mois, montant_alloue " +
                        "FROM planification " +
                        "WHERE categorie = ? " +
                        "ORDER BY " + order;

        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, categorie);

        ResultSet rs = ps.executeQuery();

        boolean started = false;
        while (rs.next()) {
            String mois = rs.getString("mois");
            double montant = rs.getDouble("montant_alloue");

            if (mois.equalsIgnoreCase(moisDebut)) started = true;
            if (started) res.add(new String[]{mois, String.valueOf(montant)});
            if (mois.equalsIgnoreCase(moisFin)) break;
        }

        return res;
    }


}
