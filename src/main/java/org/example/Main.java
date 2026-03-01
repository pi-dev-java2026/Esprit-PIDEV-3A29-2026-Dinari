package org.example;
import entities.JustificatifDepense;
import entities.depense;
import services.ServiceDepense;
import services.ServiceJutificatifDepense;
import utils.MyDataBase ;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    static void main() {

        ServiceDepense sd = new ServiceDepense();
        ServiceJutificatifDepense jd = new ServiceJutificatifDepense() ;

        try {
          //sd.ajouter(new depense(30,new Date(),"Paiement facture sport","En ligne",7));
           System.out.println("Dépense ajouter");

          // jd.ajouter(new JustificatifDepense("c:/img.jpg",new Date(),"jpg",1));
          //  System.out.println("Justificatif Dépense ajouter");

            // jd.modifier(new JustificatifDepense(1,"c:/image_cal.jpg",new Date(),"jpg",1));
              //System.out.println("Justificatif Dépense Modifier");

           //sd.modifier(new depense(1, 80,new Date(),"Paiement facture test","En ligne",5));
          // System.out.println("Depense modifier");

            //  sd.supprimer(2);

             //jd.supprimer(3);

           /*try {
               List<depense> listeTriee = sd.afficherTrieParIdStream();
               listeTriee.forEach(d -> System.out.println(d));
            } catch (SQLException e) {
                System.out.println("Erreur SQL : " + e.getMessage());
            }*/

           /* try {
                depense d = sd.rechercherParIdStream(1); // cherche id = 1
                if (d != null) {
                    System.out.println("Dépense trouvée : " + d);
                } else {
                    System.out.println("Dépense introuvable !");
                }
            } catch (SQLException e) {
                System.out.println("Erreur SQL : " + e.getMessage());
            }*/

            //System.out.println(jd.afficher());
            System.out.println(sd.afficher());

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }
}
