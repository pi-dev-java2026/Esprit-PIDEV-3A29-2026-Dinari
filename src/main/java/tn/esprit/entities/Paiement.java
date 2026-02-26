package tn.esprit.entities;

import java.sql.Date;

public class Paiement {
    private int id;
    private double montant;
    private Date datePaiement;
    private String statut;
    private int abonnementId;
    private String nomTitulaire;
    private String prenomTitulaire;
    private String modePaiement;
    private String numeroCarte;
    private String dateExpiration;
    private String cvv;
    private String codeTransaction;
    private String stripeTransactionId;

    public Paiement() {}

    public Paiement(double montant, Date datePaiement, String statut, int abonnementId) {
        this.montant = montant;
        this.datePaiement = datePaiement;
        this.statut = statut;
        this.abonnementId = abonnementId;
    }

    public Paiement(double montant, Date datePaiement, String statut, int abonnementId,
                    String nomTitulaire, String prenomTitulaire, String modePaiement,
                    String numeroCarte, String dateExpiration, String cvv) {
        this.montant = montant;
        this.datePaiement = datePaiement;
        this.statut = statut;
        this.abonnementId = abonnementId;
        this.nomTitulaire = nomTitulaire;
        this.prenomTitulaire = prenomTitulaire;
        this.modePaiement = modePaiement;
        this.numeroCarte = numeroCarte;
        this.dateExpiration = dateExpiration;
        this.cvv = cvv;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public double getMontant() { return montant; }
    public void setMontant(double montant) { this.montant = montant; }
    public Date getDatePaiement() { return datePaiement; }
    public void setDatePaiement(Date datePaiement) { this.datePaiement = datePaiement; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public int getAbonnementId() { return abonnementId; }
    public void setAbonnementId(int abonnementId) { this.abonnementId = abonnementId; }
    public String getNomTitulaire() { return nomTitulaire; }
    public void setNomTitulaire(String nomTitulaire) { this.nomTitulaire = nomTitulaire; }
    public String getPrenomTitulaire() { return prenomTitulaire; }
    public void setPrenomTitulaire(String prenomTitulaire) { this.prenomTitulaire = prenomTitulaire; }
    public String getModePaiement() { return modePaiement; }
    public void setModePaiement(String modePaiement) { this.modePaiement = modePaiement; }
    public String getNumeroCarte() { return numeroCarte; }
    public void setNumeroCarte(String numeroCarte) { this.numeroCarte = numeroCarte; }
    public String getDateExpiration() { return dateExpiration; }
    public void setDateExpiration(String dateExpiration) { this.dateExpiration = dateExpiration; }
    public String getCvv() { return cvv; }
    public void setCvv(String cvv) { this.cvv = cvv; }
    public String getStripeTransactionId() {
        return stripeTransactionId;
    }
    public void setStripeTransactionId(String stripeTransactionId) {
        this.stripeTransactionId = stripeTransactionId;
    }

    @Override
    public String toString() {
        return "Paiement{id=" + id + ", montant=" + montant + ", statut='" + statut + "', modePaiement='" + modePaiement + "'}";
    }
}