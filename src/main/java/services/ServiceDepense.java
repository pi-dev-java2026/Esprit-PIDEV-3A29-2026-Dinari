package services;

import entities.depense;
import utils.LoggerUtil;
import utils.MyDataBase;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.util.stream.Collectors;

public class ServiceDepense implements IService<depense>{
    private Connection connection;

    public ServiceDepense(){
        connection = MyDataBase.getInstance().getMyConnection();
        if(this.connection == null) {
            System.out.println("Erreur : connexion null !");
        }
    }

    @Override
    public void ajouter(depense de) throws SQLException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = sdf.format(de.getDate_depense());

        String sql = "INSERT INTO `depense`(`montant`, `date_depense`, `description`, `mode_paiement`,`utilisateur_id`,`categorie`) VALUES (" +
                "'" + de.getMontant() + "', " +
                "'" + dateString + "', " +
                "'" + de.getDescription() + "', " +
                "'" + de.getMode_paiement() + "', " +
                "'" + de.getUtilisateur_Id() + "', " +
                "'" + de.getCategorie() + "')";

        Statement statement = connection.createStatement();
        statement.executeUpdate(sql);

        LoggerUtil.log(
                "ADD",
                "DEPENSE",
                "Montant=" + de.getMontant() +
                        " | Mode=" + de.getMode_paiement() +
                        " | Categorie=" + de.getCategorie() +
                        " | Id=" + de.getId_depense()
        );
    }

    @Override
    public void modifier(depense de) throws SQLException {

        String sql ="UPDATE `depense` SET `montant`=?,`date_depense`=?,`description`=? ,`mode_paiement`=? ,`utilisateur_id`=?,`categorie`=? WHERE id_depense = ?";
        PreparedStatement ps = connection.prepareStatement(sql);

        ps.setFloat(1, de.getMontant());
        ps.setDate(2, new java.sql.Date(de.getDate_depense().getTime()));
        ps.setString(3, de.getDescription());
        ps.setString(4, de.getMode_paiement());
        ps.setInt(5, de.getUtilisateur_Id());
        ps.setString(6, de.getCategorie());
        ps.setInt(7, de.getId_depense());

        ps.executeUpdate();


        LoggerUtil.log(
                "UPDATE",
                "DEPENSE",
                "Montant=" + de.getMontant() +
                        " | Mode=" + de.getMode_paiement() +
                        " | Categorie=" + de.getCategorie() +
                        " | Id=" + de.getId_depense()
        );
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "Delete from depense where id_depense =?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1,id);
        ps.executeUpdate();

        LoggerUtil.log(
                "DELETE",
                "DEPENSE",
                "ID=" + id

        );
    }

    @Override
    public List<depense> afficher() throws SQLException {
        List<depense> depenses= new ArrayList<>();
        String sql = "Select * from depense";
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);

        while (resultSet.next()){
            depense p = new depense();

            p.setId_depense(resultSet.getInt("id_depense"));
            p.setMontant(resultSet.getFloat(2));
            p.setDate_depense(resultSet.getDate(3));
            p.setDescription(resultSet.getString(4));
            p.setMode_paiement(resultSet.getString(5));
            p.setutilisateur_id(resultSet.getInt(6));
            p.setCategorie(resultSet.getString(7)); // 👈 ajout catégorie

            depenses.add(p);
        }

        return depenses;
    }

    @Override
    public List<depense> afficherById(int id) throws SQLException {
        return List.of();
    }

    public List<depense> afficherTrieParModeEtMontant() throws SQLException {
        return afficher().stream()
                .sorted(Comparator
                        .comparing(depense::getMode_paiement)   // d'abord par mode de paiement
                        .thenComparing(depense::getMontant))   // ensuite par montant
                .collect(Collectors.toList());
    }

    public depense rechercherParMontant(double montant) throws SQLException {
        return afficher().stream()
                .filter(d -> d.getMontant() == montant)
                .findFirst()
                .orElse(null);
    }

    public depense rechercherParModePaiement(String mode) throws SQLException {
        return afficher().stream()
                .filter(d -> d.getMode_paiement().equalsIgnoreCase(mode))
                .findFirst()
                .orElse(null);
    }

}

