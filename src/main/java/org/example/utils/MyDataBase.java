package org.example.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {

    private final String URL= "jdbc:mysql://localhost:3306/pidev";
    private final String USER= "root";
    private final String PSW ="";

    private Connection myConnection;

    private static MyDataBase instance;

    private MyDataBase(){
        try {
            myConnection = DriverManager.getConnection(URL,USER,PSW);
            System.out.println("Connection établie!");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
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
