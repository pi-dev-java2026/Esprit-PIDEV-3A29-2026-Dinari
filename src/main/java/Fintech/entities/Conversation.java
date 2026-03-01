package Fintech.entities;

import java.sql.Timestamp;

public class Conversation {
    private int id;
    private String userEmail;
    private Timestamp dateCreation;

    public Conversation() {
    }

    public Conversation(int id, String userEmail, Timestamp dateCreation) {
        this.id = id;
        this.userEmail = userEmail;
        this.dateCreation = dateCreation;
    }

    public Conversation(String userEmail, Timestamp dateCreation) {
        this.userEmail = userEmail;
        this.dateCreation = dateCreation;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public Timestamp getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Timestamp dateCreation) {
        this.dateCreation = dateCreation;
    }
}
