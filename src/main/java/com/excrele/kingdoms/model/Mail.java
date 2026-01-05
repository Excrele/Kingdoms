package com.excrele.kingdoms.model;

/**
 * Represents a mail message sent to a kingdom member
 */
public class Mail {
    private String mailId;
    private String recipient; // Player name
    private String sender; // Player name
    private String kingdomName; // Kingdom the sender belongs to
    private String subject;
    private String message;
    private long sentAt;
    private boolean read;
    private long readAt;
    private boolean deleted;
    
    public Mail(String recipient, String sender, String kingdomName, String subject, String message) {
        this.mailId = java.util.UUID.randomUUID().toString();
        this.recipient = recipient;
        this.sender = sender;
        this.kingdomName = kingdomName;
        this.subject = subject;
        this.message = message;
        this.sentAt = System.currentTimeMillis() / 1000;
        this.read = false;
        this.readAt = 0;
        this.deleted = false;
    }
    
    public Mail(String mailId, String recipient, String sender, String kingdomName, 
                String subject, String message, long sentAt, boolean read, long readAt, boolean deleted) {
        this.mailId = mailId;
        this.recipient = recipient;
        this.sender = sender;
        this.kingdomName = kingdomName;
        this.subject = subject;
        this.message = message;
        this.sentAt = sentAt;
        this.read = read;
        this.readAt = readAt;
        this.deleted = deleted;
    }
    
    public String getMailId() { return mailId; }
    public String getRecipient() { return recipient; }
    public String getSender() { return sender; }
    public String getKingdomName() { return kingdomName; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public long getSentAt() { return sentAt; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) {
        this.read = read;
        if (read && this.readAt == 0) {
            this.readAt = System.currentTimeMillis() / 1000;
        }
    }
    public long getReadAt() { return readAt; }
    public void setReadAt(long readAt) { this.readAt = readAt; }
    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
    
    /**
     * Get formatted time since sent
     */
    public String getFormattedTimeSince() {
        long now = System.currentTimeMillis() / 1000;
        long diff = now - sentAt;
        
        if (diff < 60) return "just now";
        if (diff < 3600) return (diff / 60) + " minutes ago";
        if (diff < 86400) return (diff / 3600) + " hours ago";
        if (diff < 604800) return (diff / 86400) + " days ago";
        return (diff / 604800) + " weeks ago";
    }
    
    /**
     * Get formatted date/time
     */
    public String getFormattedDate() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
        return sdf.format(new java.util.Date(sentAt * 1000));
    }
}

