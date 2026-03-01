package utils;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {

    private final String URL= "jdbc:mysql://localhost:3306/pidev";
    private final String USER= "root";
    private final String PSW ="";

    private Connection myConnection;

    private static MyDataBase instance;

    public MyDataBase(){
        try {
            myConnection = DriverManager.getConnection(URL,USER,PSW);
            if(myConnection != null) {
                System.out.println("Connexion OK !");
            } else {
                System.out.println("Erreur : connexion null !");
            }
        } catch (SQLException e) {
            System.out.println("Erreur SQL : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Connection getMyConnection() {
        return myConnection;
    }

    public static MyDataBase getInstance() {
        if(instance == null)
            instance = new MyDataBase();
        return instance;
    }


}
