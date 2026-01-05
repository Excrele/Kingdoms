package com.excrele.kingdoms.manager;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.KingdomReputation;
import com.excrele.kingdoms.model.ReputationEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages kingdom reputation system
 */
public class ReputationManager {
    private final KingdomsPlugin plugin;
    private final Map<String, KingdomReputation> reputations; // kingdom1:kingdom2 -> reputation
    private final Map<String, List<String>> kingdomReputations; // kingdom -> list of other kingdoms
    private final Map<String, List<ReputationEvent>> reputationEvents; // kingdom -> events
    private final double decayRate; // Reputation decay per day
    private final long decayInterval; // Time between decay checks in seconds
    
    public ReputationManager(KingdomsPlugin plugin) {
        this.plugin = plugin;
        this.reputations = new ConcurrentHashMap<>();
        this.kingdomReputations = new ConcurrentHashMap<>();
        this.reputationEvents = new ConcurrentHashMap<>();
        this.decayRate = plugin.getConfig().getDouble("reputation.decay_rate", 0.5); // 0.5 per day
        this.decayInterval = plugin.getConfig().getLong("reputation.decay_interval", 86400L); // 24 hours
    }
    
    /**
     * Get or create reputation between two kingdoms
     */
    public KingdomReputation getReputation(String kingdom1, String kingdom2) {
        String key = getReputationKey(kingdom1, kingdom2);
        return reputations.computeIfAbsent(key, k -> new KingdomReputation(kingdom1, kingdom2));
    }
    
    /**
     * Add reputation change from an event
     */
    public void addReputationEvent(String kingdom1, String kingdom2, ReputationEvent.EventType type, 
                                  double change, String description, String causedBy) {
        String eventId = UUID.randomUUID().toString().substring(0, 8);
        ReputationEvent event = new ReputationEvent(eventId, kingdom1, kingdom2, type, 
                                                    change, description, causedBy);
        
        // Update reputation
        KingdomReputation rep = getReputation(kingdom1, kingdom2);
        rep.addReputation(change);
        
        // Also update reverse reputation (symmetrical)
        KingdomReputation reverseRep = getReputation(kingdom2, kingdom1);
        reverseRep.addReputation(change);
        
        // Store event
        reputationEvents.computeIfAbsent(kingdom1, k -> new ArrayList<>()).add(event);
        reputationEvents.computeIfAbsent(kingdom2, k -> new ArrayList<>()).add(event);
        
        // Keep only last 100 events per kingdom
        List<ReputationEvent> events1 = reputationEvents.get(kingdom1);
        if (events1 != null && events1.size() > 100) {
            events1.remove(0);
        }
        List<ReputationEvent> events2 = reputationEvents.get(kingdom2);
        if (events2 != null && events2.size() > 100) {
            events2.remove(0);
        }
    }
    
    /**
     * Process reputation decay over time
     */
    public void processReputationDecay() {
        long currentTime = System.currentTimeMillis() / 1000;
        
        for (KingdomReputation rep : reputations.values()) {
            long timeSinceDecay = currentTime - rep.getLastDecayTime();
            if (timeSinceDecay >= decayInterval) {
                // Calculate decay amount
                double daysPassed = (double) timeSinceDecay / 86400.0;
                double decayAmount = decayRate * daysPassed;
                
                // Decay towards neutral (0)
                if (rep.getReputation() > 0) {
                    rep.addReputation(-decayAmount);
                } else if (rep.getReputation() < 0) {
                    rep.addReputation(decayAmount);
                }
                
                rep.setLastDecayTime(currentTime);
            }
        }
    }
    
    /**
     * Get trade price modifier based on reputation
     */
    public double getTradePriceModifier(String kingdom1, String kingdom2) {
        KingdomReputation rep = getReputation(kingdom1, kingdom2);
        return rep.getTradePriceModifier();
    }
    
    /**
     * Check if kingdoms can form alliance based on reputation
     */
    public boolean canFormAlliance(String kingdom1, String kingdom2) {
        KingdomReputation rep = getReputation(kingdom1, kingdom2);
        return rep.getReputation() >= 50.0; // Need at least FRIENDLY reputation
    }
    
    /**
     * Get all reputation events for a kingdom
     */
    public List<ReputationEvent> getReputationEvents(String kingdomName) {
        return reputationEvents.getOrDefault(kingdomName, new ArrayList<>());
    }
    
    /**
     * Get reputation key for two kingdoms (sorted to ensure consistency)
     */
    private String getReputationKey(String kingdom1, String kingdom2) {
        if (kingdom1.compareTo(kingdom2) < 0) {
            return kingdom1 + ":" + kingdom2;
        } else {
            return kingdom2 + ":" + kingdom1;
        }
    }
    
    /**
     * Get all kingdoms with reputation data for a kingdom
     */
    public List<String> getKnownKingdoms(String kingdomName) {
        return kingdomReputations.getOrDefault(kingdomName, new ArrayList<>());
    }
}

