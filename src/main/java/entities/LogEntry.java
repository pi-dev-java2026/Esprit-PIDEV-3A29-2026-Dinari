package entities;
public class LogEntry {

    private String date;
    private String action;
    private String entity;
    private String details;

    public void setDate(String date) {
        this.date = date;
    }

    public LogEntry() {

    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LogEntry(String date, String action, String entity, String details) {
        this.date = date;
        this.action = action;
        this.entity = entity;
        this.details = details;
    }

    public String getDate() { return date; }
    public String getAction() { return action; }
    public String getEntity() { return entity; }
    public String getDetails() { return details; }
}
