package com.excrele.kingdoms.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Tracks historical changes to a kingdom
 */
public class KingdomHistory {
    private String kingdomName;
    private List<HistoryEntry> entries;
    
    public KingdomHistory(String kingdomName) {
        this.kingdomName = kingdomName;
        this.entries = new ArrayList<>();
    }
    
    public String getKingdomName() { return kingdomName; }
    public List<HistoryEntry> getEntries() { return entries; }
    
    public void addEntry(HistoryEntry entry) {
        entries.add(entry);
        // Keep only last 1000 entries
        if (entries.size() > 1000) {
            entries.remove(0);
        }
    }
    
    public static class HistoryEntry {
        private long timestamp;
        private HistoryType type;
        private String description;
        private String actor; // Player who made the change
        
        public HistoryEntry(HistoryType type, String description, String actor) {
            this.timestamp = System.currentTimeMillis() / 1000;
            this.type = type;
            this.description = description;
            this.actor = actor;
        }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
        public HistoryType getType() { return type; }
        public String getDescription() { return description; }
        public String getActor() { return actor; }
    }
    
    public enum HistoryType {
        CREATED, MEMBER_JOINED, MEMBER_LEFT, MEMBER_KICKED, MEMBER_PROMOTED,
        CLAIM_ADDED, CLAIM_REMOVED, LEVEL_UP, XP_GAINED, ALLIANCE_FORMED,
        ALLIANCE_BROKEN, WAR_DECLARED, WAR_ENDED, FLAG_CHANGED, SPAWN_SET,
        BANK_DEPOSIT, BANK_WITHDRAW, BANK_TRANSFER
    }
}

