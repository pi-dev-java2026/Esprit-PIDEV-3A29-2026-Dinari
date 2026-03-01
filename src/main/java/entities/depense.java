package entities;

import java.util.Date;
import java.util.Objects;

public class depense {

    private int id_depense;
    private float montant;
    private Date date_depense;
    private String description, mode_paiement;
    private int utilisateur_id;
    private String categorie; // 👈 ajout catégorie

    public depense() {
    }

    // Constructors anciens (ما تبدّل فيهم شي)
    public depense(int id_depense, float montant, Date date_depense, String description, String mode_paiement, int utilisateur_id) {
        this.id_depense = id_depense;
        this.montant = montant;
        this.date_depense = date_depense;
        this.description = description;
        this.mode_paiement = mode_paiement;
        this.utilisateur_id = utilisateur_id;
    }

    public depense(float montant, Date date_depense, String description, String mode_paiement, int utilisateur_id, String categorie) {
        this.montant = montant;
        this.date_depense = date_depense;
        this.description = description;
        this.mode_paiement = mode_paiement;
        this.utilisateur_id = utilisateur_id;
        this.categorie = categorie ;
    }




    public int getId_depense() {
        return id_depense;
    }

    public void setId_depense(int id_depense) {
        this.id_depense = id_depense;
    }

    public int getUtilisateur_Id() {
        return utilisateur_id;
    }

    public void setutilisateur_id(int utilisateur_id) {
        this.utilisateur_id = utilisateur_id;
    }

    public float getMontant() {
        return montant;
    }

    public void setMontant(float montant) {
        this.montant = montant;
    }

    public Date getDate_depense() {
        return date_depense;
    }

    public void setDate_depense(Date date_depense) {
        this.date_depense = date_depense;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMode_paiement() {
        return mode_paiement;
    }

    public void setMode_paiement(String mode_paiement) {
        this.mode_paiement = mode_paiement;
    }

    // Getter / Setter catégorie
    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    @Override
    public String toString() {
        return "depense{" +
                "id_depense=" + id_depense +
                ", montant=" + montant +
                ", date_depense=" + date_depense +
                ", description='" + description + '\'' +
                ", mode_paiement='" + mode_paiement + '\'' +
                ", categorie='" + categorie + '\'' +
                ", utilisateur_id='" + utilisateur_id + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        depense depense = (depense) o;
        return id_depense == depense.id_depense
                && Float.compare(montant, depense.montant) == 0
                && utilisateur_id == depense.utilisateur_id
                && Objects.equals(date_depense, depense.date_depense)
                && Objects.equals(description, depense.description)
                && Objects.equals(mode_paiement, depense.mode_paiement)
                && Objects.equals(categorie, depense.categorie);
    }
}
