package com.excrele.kingdoms.model;

/**
 * Represents a kingdom announcement
 */
public class KingdomAnnouncement {
    private String id;
    private String kingdomName;
    private String author;
    private String message;
    private long timestamp;
    private boolean active;
    
    public KingdomAnnouncement(String id, String kingdomName, String author, String message) {
        this.id = id;
        this.kingdomName = kingdomName;
        this.author = author;
        this.message = message;
        this.timestamp = System.currentTimeMillis() / 1000;
        this.active = true;
    }
    
    public String getId() { return id; }
    public String getKingdomName() { return kingdomName; }
    public String getAuthor() { return author; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public long getTimestamp() { return timestamp; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    
    /**
     * Get formatted timestamp string
     */
    public String getFormattedTime() {
        long now = System.currentTimeMillis() / 1000;
        long diff = now - timestamp;
        
        if (diff < 60) return "just now";
        if (diff < 3600) return (diff / 60) + " minutes ago";
        if (diff < 86400) return (diff / 3600) + " hours ago";
        return (diff / 86400) + " days ago";
    }
}

