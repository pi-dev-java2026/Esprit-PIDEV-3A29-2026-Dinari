/*package org.example.tests;

import org.example.entities.Activite;
import org.example.entities.Planification;
import org.example.services.ServiceActivite;
import org.example.services.ServicePlanification;

import java.sql.Date;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {

        // =========================
        // ✅ TEST ACTIVITE
        // =========================
        ServiceActivite sa = new ServiceActivite();

        try {
            // 1) AJOUT
            Activite a1 = new Activite(
                    "Paiement facture STEG",
                    120.00,
                    Date.valueOf("2026-02-14"),
                    "EN_ATTENTE"
            );

            sa.ajouter(a1);
            System.out.println("✅ Activité ajoutée");

            // 2) AFFICHER
            System.out.println("📌 Liste des activités :");
            System.out.println(sa.afficher());

            // 3) MODIFIER (exemple id=1)
            Activite aMod = new Activite(
                    1,
                    "Paiement facture Internet",
                    80.00,
                    Date.valueOf("2026-02-15"),
                    "PAYEE"
            );

            sa.modifier(aMod);
            System.out.println("✅ Activité modifiée");

            // 4) SUPPRIMER (exemple id=2)
            // sa.supprimer(2);
            // System.out.println("✅ Activité supprimée");

            System.out.println("📌 Liste des activités après modification/suppression :");
            System.out.println(sa.afficher());

        } catch (SQLException e) {
            System.out.println("❌ Erreur SQL (Activité) : " + e.getMessage());
        }


        // =========================
        // ✅ TEST PLANIFICATION (optionnel)
        // =========================
        ServicePlanification sp = new ServicePlanification();

        try {
            Planification p1 = new Planification(
                    "Nourriture",
                    300.00,
                    "normale",
                    "Mars"
            );

            sp.ajouter(p1);
            System.out.println("✅ Planification ajoutée");

            System.out.println("📌 Liste des planifications :");
            System.out.println(sp.afficher());

            Planification pMod = new Planification(
                    1,
                    "Transport",
                    150.00,
                    "elevee",
                    "Février"
            );

            sp.modifier(pMod);
            System.out.println("✅ Planification modifiée");

            // sp.supprimer(2);
            // System.out.println("✅ Planification supprimée");

            System.out.println("📌 Liste des planifications après modification/suppression :");
            System.out.println(sp.afficher());

        } catch (SQLException e) {
            System.out.println("❌ Erreur SQL (Planification) : " + e.getMessage());
        }
    }
}
*/