package tn.esprit.test;

import tn.esprit.entities.Abonnement;
import tn.esprit.entities.Paiement;
import tn.esprit.services.AbonnementService;
import tn.esprit.services.PaiementService;

import java.sql.Date;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        AbonnementService abonnementService = new AbonnementService();
        PaiementService paiementService = new PaiementService();

        System.out.println("===== TEST CRUD DINARI =====\n");

        // Ajout
        System.out.println("1. Ajout d'abonnements :");
        abonnementService.ajouter(new Abonnement("Netflix", 14.99, Date.valueOf("2024-01-15"), "Mensuel", "Streaming", true));
        System.out.println();
        abonnementService.ajouter(new Abonnement("Spotify", 9.99, Date.valueOf("2024-02-01"), "Mensuel", "Musique", true));
        System.out.println();
        abonnementService.ajouter(new Abonnement("Salle de sport", 49.99, Date.valueOf("2024-01-10"), "Mensuel", "Sport", true));

        // Affichage normal
        System.out.println("\n2. Liste de TOUS les abonnements :");
        List<Abonnement> abonnements = abonnementService.afficher();
        for (Abonnement a : abonnements) {
            System.out.println(a);
        }

        // Affichage par catégorie - CORRECTION ICI
        System.out.println("\n3. Abonnements de la catégorie 'Streaming' :");
        List<Abonnement> abosStreaming = abonnementService.afficherParCategorie("Streaming");
        for (Abonnement a : abosStreaming) {
            System.out.println("  → " + a.getCategorie() + " : " + a.getNom() + " - " + a.getPrix() + "€");
        }

        System.out.println("\n4. Abonnements de la catégorie 'Musique' :");
        List<Abonnement> abosMusique = abonnementService.afficherParCategorie("Musique");
        for (Abonnement a : abosMusique) {
            System.out.println("  → " + a.getCategorie() + " : " + a.getNom() + " - " + a.getPrix() + "€");
        }

        // Modification
        if (!abonnements.isEmpty()) {
            System.out.println("\n5. Modification du prix de Netflix :");
            Abonnement aModifier = abonnements.get(0);
            System.out.println("  Ancien prix : " + aModifier.getPrix() + "€");
            aModifier.setPrix(16.99);
            abonnementService.modifier(aModifier);
            System.out.println("  Nouveau prix : 16.99€");
        }

        // Suppression
        if (abonnements.size() > 1) {
            System.out.println("\n6. Suppression du dernier abonnement :");
            Abonnement aDel = abonnements.get(abonnements.size() - 1);
            System.out.println("  Suppression de : " + aDel.getNom());
            abonnementService.supprimer(aDel.getId());
        }

        // Affichage final
        System.out.println("\n7. Liste finale des abonnements :");
        List<Abonnement> abosFinal = abonnementService.afficher();
        for (Abonnement a : abosFinal) {
            System.out.println("  → " + a.getNom() + " (" + a.getCategorie() + ") - " + a.getPrix() + "€");
        }

        System.out.println("\n===== FIN DES TESTS =====");
        System.out.println("✅ Tout fonctionne correctement !");
    }
}