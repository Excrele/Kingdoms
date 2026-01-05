package com.excrele.kingdoms.model;

/**
 * Represents a diplomatic agreement between kingdoms
 */
public class DiplomaticAgreement {
    private String agreementId;
    private String kingdom1;
    private String kingdom2;
    private AgreementType type;
    private long establishedAt;
    private long expiresAt; // -1 for permanent
    private boolean active;
    private String terms; // Additional terms/notes
    
    public enum AgreementType {
        NON_AGGRESSION_PACT,  // Cannot declare war
        TRADE_AGREEMENT,      // Trade bonuses
        EMBASSY,              // Embassy establishment
        MUTUAL_DEFENSE        // Defense pact
    }
    
    public DiplomaticAgreement(String kingdom1, String kingdom2, AgreementType type, long duration) {
        this.agreementId = java.util.UUID.randomUUID().toString();
        this.kingdom1 = kingdom1;
        this.kingdom2 = kingdom2;
        this.type = type;
        this.establishedAt = System.currentTimeMillis() / 1000;
        this.expiresAt = duration > 0 ? this.establishedAt + duration : -1;
        this.active = true;
        this.terms = "";
    }
    
    public DiplomaticAgreement(String agreementId, String kingdom1, String kingdom2, 
                             AgreementType type, long establishedAt, long expiresAt, 
                             boolean active, String terms) {
        this.agreementId = agreementId;
        this.kingdom1 = kingdom1;
        this.kingdom2 = kingdom2;
        this.type = type;
        this.establishedAt = establishedAt;
        this.expiresAt = expiresAt;
        this.active = active;
        this.terms = terms;
    }
    
    public String getAgreementId() { return agreementId; }
    public String getKingdom1() { return kingdom1; }
    public String getKingdom2() { return kingdom2; }
    public AgreementType getType() { return type; }
    public long getEstablishedAt() { return establishedAt; }
    public long getExpiresAt() { return expiresAt; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getTerms() { return terms; }
    public void setTerms(String terms) { this.terms = terms; }
    
    public boolean isExpired() {
        if (expiresAt == -1) return false; // Permanent
        return System.currentTimeMillis() / 1000 >= expiresAt;
    }
    
    public boolean involvesKingdom(String kingdomName) {
        return kingdom1.equals(kingdomName) || kingdom2.equals(kingdomName);
    }
    
    public String getOtherKingdom(String kingdomName) {
        if (kingdom1.equals(kingdomName)) return kingdom2;
        if (kingdom2.equals(kingdomName)) return kingdom1;
        return null;
    }
    
    public long getTimeRemaining() {
        if (expiresAt == -1) return -1; // Permanent
        long remaining = expiresAt - (System.currentTimeMillis() / 1000);
        return Math.max(0, remaining);
    }
}

