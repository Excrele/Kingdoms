package com.excrele.kingdoms.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a war alliance (multiple kingdoms vs multiple kingdoms)
 */
public class WarAlliance {
    private final String allianceId;
    private final List<String> side1; // First side kingdoms
    private final List<String> side2; // Second side kingdoms
    private final String warId; // Associated war
    private final long startTime;
    
    public WarAlliance(String allianceId, List<String> side1, List<String> side2, String warId) {
        this.allianceId = allianceId;
        this.side1 = new ArrayList<>(side1);
        this.side2 = new ArrayList<>(side2);
        this.warId = warId;
        this.startTime = System.currentTimeMillis() / 1000;
    }
    
    public String getAllianceId() {
        return allianceId;
    }
    
    public List<String> getSide1() {
        return side1;
    }
    
    public List<String> getSide2() {
        return side2;
    }
    
    public String getWarId() {
        return warId;
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    /**
     * Check if a kingdom is on side 1
     */
    public boolean isOnSide1(String kingdomName) {
        return side1.contains(kingdomName);
    }
    
    /**
     * Check if a kingdom is on side 2
     */
    public boolean isOnSide2(String kingdomName) {
        return side2.contains(kingdomName);
    }
    
    /**
     * Check if two kingdoms are on the same side
     */
    public boolean areAllies(String kingdom1, String kingdom2) {
        return (isOnSide1(kingdom1) && isOnSide1(kingdom2)) ||
               (isOnSide2(kingdom1) && isOnSide2(kingdom2));
    }
    
    /**
     * Check if two kingdoms are enemies
     */
    public boolean areEnemies(String kingdom1, String kingdom2) {
        return (isOnSide1(kingdom1) && isOnSide2(kingdom2)) ||
               (isOnSide2(kingdom1) && isOnSide1(kingdom2));
    }
}

