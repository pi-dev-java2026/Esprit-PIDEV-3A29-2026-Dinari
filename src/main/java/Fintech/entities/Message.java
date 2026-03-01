package Fintech.entities;

import java.sql.Timestamp;

public class Message {
    private int id;
    private int conversationId;
    private String sender; // "USER" or "BOT"
    private String content;
    private Timestamp dateMessage;

    public Message() {
    }

    public Message(int id, int conversationId, String sender, String content, Timestamp dateMessage) {
        this.id = id;
        this.conversationId = conversationId;
        this.sender = sender;
        this.content = content;
        this.dateMessage = dateMessage;
    }

    public Message(int conversationId, String sender, String content, Timestamp dateMessage) {
        this.conversationId = conversationId;
        this.sender = sender;
        this.content = content;
        this.dateMessage = dateMessage;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getConversationId() {
        return conversationId;
    }

    public void setConversationId(int conversationId) {
        this.conversationId = conversationId;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Timestamp getDateMessage() {
        return dateMessage;
    }

    public void setDateMessage(Timestamp dateMessage) {
        this.dateMessage = dateMessage;
    }
}
