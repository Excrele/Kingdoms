package com.excrele.kingdoms.model;

/**
 * Represents a ceasefire agreement between warring kingdoms
 */
public class Ceasefire {
    private final String ceasefireId;
    private final String warId;
    private final String proposingKingdom;
    private final String acceptingKingdom;
    private final long startTime;
    private final long duration; // Duration in seconds
    private final String terms; // Terms of the ceasefire
    private CeasefireStatus status;
    
    public Ceasefire(String ceasefireId, String warId, String proposingKingdom, 
                    String acceptingKingdom, long duration, String terms) {
        this.ceasefireId = ceasefireId;
        this.warId = warId;
        this.proposingKingdom = proposingKingdom;
        this.acceptingKingdom = acceptingKingdom;
        this.startTime = System.currentTimeMillis() / 1000;
        this.duration = duration;
        this.terms = terms;
        this.status = CeasefireStatus.PROPOSED;
    }
    
    public String getCeasefireId() {
        return ceasefireId;
    }
    
    public String getWarId() {
        return warId;
    }
    
    public String getProposingKingdom() {
        return proposingKingdom;
    }
    
    public String getAcceptingKingdom() {
        return acceptingKingdom;
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    public long getDuration() {
        return duration;
    }
    
    public long getExpirationTime() {
        return startTime + duration;
    }
    
    public String getTerms() {
        return terms;
    }
    
    public CeasefireStatus getStatus() {
        return status;
    }
    
    public void setStatus(CeasefireStatus status) {
        this.status = status;
    }
    
    public boolean isExpired() {
        return System.currentTimeMillis() / 1000 > getExpirationTime();
    }
    
    public boolean isActive() {
        return status == CeasefireStatus.ACTIVE && !isExpired();
    }
    
    public enum CeasefireStatus {
        PROPOSED,   // Proposed but not yet accepted
        ACTIVE,     // Active ceasefire
        REJECTED,   // Rejected by other party
        EXPIRED     // Expired
    }
}

