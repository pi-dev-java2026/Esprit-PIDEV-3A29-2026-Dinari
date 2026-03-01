package services;
import entities.JustificatifDepense;
import entities.depense;
import utils.LoggerUtil;
import utils.MyDataBase;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


public class ServiceJutificatifDepense   implements IService<JustificatifDepense> {

    private Connection connection;

    public ServiceJutificatifDepense(){
        connection = MyDataBase.getInstance().getMyConnection();
        if(this.connection == null) {
            System.out.println("Erreur : connexion null !");
        }
    }


    @Override
    public void ajouter(JustificatifDepense de) throws SQLException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = sdf.format(de.getDateajout());

        // Requête SQL corrigée
        String sql = "INSERT INTO `justificatif_depense` " +
                "(`filepath`, `dateajout`, `typefichier`, `idDepense`) VALUES (" +
                "'" + de.getFilepath().replace("'", "\\'") + "', " +  // échapper les apostrophes
                "'" + dateString + "', " +
                "'" + de.getTypefichier().replace("'", "\\'") + "', " +
                de.getIdDepense() + ")"; // pas besoin de quotes pour INT
        Statement statement = connection.createStatement();
        statement.executeUpdate(sql);



        LoggerUtil.log(
                "ADD",
                "JUTIFICATIF",
                "FilePath=" + de.getFilepath() +
                        " | Date=" + de.getDateajout() +
                        " | Type=" + de.getTypefichier() +
                        " | IdDepense=" + de.getIdDepense()
        );
    }


    @Override
    public void modifier(JustificatifDepense de) throws SQLException {

        String sql ="UPDATE `justificatif_depense` SET `filepath`=?,`dateajout`=?,`typefichier`=? , `idDepense`=? WHERE idJustificatif  = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, de.getFilepath());
        ps.setDate(2, new java.sql.Date(de.getDateajout().getTime())); // <-- correction ici
        ps.setString(3, de.getTypefichier());
        ps.setInt(4, de.getIdDepense());
        ps.setInt(5, de.getIdJustificatif());
        ps.executeUpdate();


        LoggerUtil.log(
                "UPDATE",
                "JUTIFICATIF",
                "FilePath=" + de.getFilepath() +
                        " | Date=" + de.getDateajout() +
                        " | Type=" + de.getTypefichier() +
                        " | IdDepense=" + de.getIdDepense()
        );

    }




    public void supprimer(int id) throws SQLException {
        String sql = "Delete from justificatif_depense where idJustificatif =?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1,id);
        ps.executeUpdate();

        LoggerUtil.log(
                "DELETE",
                "JUTIFICATIF",
                "Id=" + id

        );

    }

    @Override
    public List<JustificatifDepense> afficher() throws SQLException {
        List<JustificatifDepense> Justificatifs= new ArrayList<>();
        String sql = "Select * from justificatif_depense";
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        while (resultSet.next()){
            JustificatifDepense p = new JustificatifDepense();
            p.setIdJustificatif(resultSet.getInt("idJustificatif"));
            p.setFilepath(resultSet.getString(2));
            p.setTypefichier(resultSet.getString(3));
            p.setDateajout(resultSet.getDate(4));
            p.setIdDepense(resultSet.getInt(5));
            Justificatifs.add(p);
        }
        return Justificatifs;
    }

    @Override
    public List<JustificatifDepense> afficherById(int id) throws SQLException {
        List<JustificatifDepense> Justificatifs= new ArrayList<>();
        String sql = "Select * from justificatif_depense where idDepense="+id;
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        while (resultSet.next()){
            JustificatifDepense p = new JustificatifDepense();
            p.setIdJustificatif(resultSet.getInt("idJustificatif"));
            p.setFilepath(resultSet.getString(2));
            p.setTypefichier(resultSet.getString(3));
            p.setDateajout(resultSet.getDate(4));
            p.setIdDepense(resultSet.getInt(5));
            Justificatifs.add(p);
        }
        return Justificatifs;
    }

}
