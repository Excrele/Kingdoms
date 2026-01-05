package com.excrele.kingdoms.model;

import java.util.UUID;

public class War {
    private String warId;
    private String kingdom1;
    private String kingdom2;
    private long startTime;
    private long endTime;
    private boolean active;
    private int kingdom1Score;
    private int kingdom2Score;
    private String reason; // Reason for war declaration
    private String declaredBy; // Player who declared war

    public War(String kingdom1, String kingdom2, long duration) {
        this(kingdom1, kingdom2, duration, null, null);
    }
    
    public War(String kingdom1, String kingdom2, long duration, String reason, String declaredBy) {
        this.warId = UUID.randomUUID().toString();
        this.kingdom1 = kingdom1;
        this.kingdom2 = kingdom2;
        this.startTime = System.currentTimeMillis() / 1000;
        this.endTime = this.startTime + duration;
        this.active = true;
        this.kingdom1Score = 0;
        this.kingdom2Score = 0;
        this.reason = reason;
        this.declaredBy = declaredBy;
    }

    public String getWarId() { return warId; }
    public String getKingdom1() { return kingdom1; }
    public String getKingdom2() { return kingdom2; }
    public long getStartTime() { return startTime; }
    public long getEndTime() { return endTime; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public int getKingdom1Score() { return kingdom1Score; }
    public int getKingdom2Score() { return kingdom2Score; }
    public void addScore(String kingdom, int points) {
        if (kingdom.equals(kingdom1)) {
            kingdom1Score += points;
        } else if (kingdom.equals(kingdom2)) {
            kingdom2Score += points;
        }
    }
    public String getWinner() {
        if (kingdom1Score > kingdom2Score) return kingdom1;
        if (kingdom2Score > kingdom1Score) return kingdom2;
        return null; // Tie
    }
    public boolean isExpired() {
        return System.currentTimeMillis() / 1000 >= endTime;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public String getDeclaredBy() {
        return declaredBy;
    }
    
    public void setDeclaredBy(String declaredBy) {
        this.declaredBy = declaredBy;
    }
}

