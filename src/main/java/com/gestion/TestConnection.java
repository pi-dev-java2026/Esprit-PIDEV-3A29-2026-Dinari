package com.gestion;

import com.gestion.utils.DB;

public class TestConnection {
    public static void main(String[] args) {
        try (var con = DB.getConnection()) {
            System.out.println(" Connexion OK: " + con.getCatalog());
        } catch (Exception e) {
            System.out.println(" Erreur connexion");
            e.printStackTrace();
        }
    }
}