package com.excrele.kingdoms.manager;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Enhanced war manager with alliances, objectives, scoreboard, history, ceasefire, and reparations
 */
public class EnhancedWarManager {
    private final KingdomsPlugin plugin;
    private final WarManager baseWarManager;
    private final Map<String, WarAlliance> warAlliances; // allianceId -> alliance
    private final Map<String, List<String>> warAllianceMap; // warId -> alliance IDs
    private final Map<String, WarObjective> warObjectives; // objectiveId -> objective
    private final Map<String, List<String>> warObjectivesMap; // warId -> objective IDs
    private final Map<String, Ceasefire> ceasefires; // ceasefireId -> ceasefire
    private final Map<String, List<String>> warCeasefires; // warId -> ceasefire IDs
    private final Map<String, WarReparation> warReparations; // reparationId -> reparation
    private final Map<String, List<String>> kingdomReparations; // kingdom -> reparation IDs
    private final Map<String, List<WarHistoryEntry>> warHistory; // warId -> history entries
    
    public EnhancedWarManager(KingdomsPlugin plugin, WarManager baseWarManager) {
        this.plugin = plugin;
        this.baseWarManager = baseWarManager;
        this.warAlliances = new ConcurrentHashMap<>();
        this.warAllianceMap = new ConcurrentHashMap<>();
        this.warObjectives = new ConcurrentHashMap<>();
        this.warObjectivesMap = new ConcurrentHashMap<>();
        this.ceasefires = new ConcurrentHashMap<>();
        this.warCeasefires = new ConcurrentHashMap<>();
        this.warReparations = new ConcurrentHashMap<>();
        this.kingdomReparations = new ConcurrentHashMap<>();
        this.warHistory = new ConcurrentHashMap<>();
    }
    
    /**
     * Declare war with reason
     */
    public boolean declareWar(String declaringKingdom, String targetKingdom, long duration, 
                             String reason, String declaredBy) {
        War war = new War(declaringKingdom, targetKingdom, duration, reason, declaredBy);
        
        // Use base war manager to declare
        if (baseWarManager.declareWar(declaringKingdom, targetKingdom, duration)) {
            // Add to history
            addWarHistoryEntry(war.getWarId(), "War declared", 
                declaringKingdom + " declared war on " + targetKingdom + ". Reason: " + reason, declaredBy);
            return true;
        }
        
        return false;
    }
    
    /**
     * Create a war alliance (multiple kingdoms vs multiple)
     */
    public WarAlliance createWarAlliance(String warId, List<String> side1, List<String> side2) {
        String allianceId = UUID.randomUUID().toString().substring(0, 8);
        WarAlliance alliance = new WarAlliance(allianceId, side1, side2, warId);
        
        warAlliances.put(allianceId, alliance);
        warAllianceMap.computeIfAbsent(warId, k -> new ArrayList<>()).add(allianceId);
        
        addWarHistoryEntry(warId, "War alliance formed", 
            "Alliance formed: " + side1.size() + " kingdoms vs " + side2.size() + " kingdoms", "SYSTEM");
        
        return alliance;
    }
    
    /**
     * Add a war objective
     */
    public WarObjective addWarObjective(String warId, WarObjective.ObjectiveType type, 
                                      String description, int targetValue, String targetKingdom) {
        String objectiveId = UUID.randomUUID().toString().substring(0, 8);
        WarObjective objective = new WarObjective(objectiveId, warId, type, description, 
                                                 targetValue, targetKingdom);
        
        warObjectives.put(objectiveId, objective);
        warObjectivesMap.computeIfAbsent(warId, k -> new ArrayList<>()).add(objectiveId);
        
        return objective;
    }
    
    /**
     * Update war objective progress
     */
    public void updateObjectiveProgress(String objectiveId, int progress) {
        WarObjective objective = warObjectives.get(objectiveId);
        if (objective != null) {
            objective.addProgress(progress);
            
            if (objective.isCompleted()) {
                addWarHistoryEntry(objective.getWarId(), "Objective completed", 
                    "Objective completed: " + objective.getDescription(), "SYSTEM");
            }
        }
    }
    
    /**
     * Get war scoreboard data
     */
    public Map<String, Object> getWarScoreboard(String warId) {
        // Find war by ID
        War war = null;
        for (War w : baseWarManager.getActiveWars("")) {
            if (w.getWarId().equals(warId)) {
                war = w;
                break;
            }
        }
        if (war == null) return null;
        
        Map<String, Object> scoreboard = new HashMap<>();
        scoreboard.put("warId", warId);
        scoreboard.put("kingdom1", war.getKingdom1());
        scoreboard.put("kingdom2", war.getKingdom2());
        scoreboard.put("kingdom1Score", war.getKingdom1Score());
        scoreboard.put("kingdom2Score", war.getKingdom2Score());
        scoreboard.put("startTime", war.getStartTime());
        scoreboard.put("endTime", war.getEndTime());
        scoreboard.put("duration", war.getEndTime() - war.getStartTime());
        
        // Add objectives
        List<String> objectiveIds = warObjectivesMap.get(warId);
        if (objectiveIds != null) {
            List<Map<String, Object>> objectives = new ArrayList<>();
            for (String objId : objectiveIds) {
                WarObjective obj = warObjectives.get(objId);
                if (obj != null) {
                    Map<String, Object> objData = new HashMap<>();
                    objData.put("id", obj.getObjectiveId());
                    objData.put("type", obj.getType().name());
                    objData.put("description", obj.getDescription());
                    objData.put("progress", obj.getCurrentValue());
                    objData.put("target", obj.getTargetValue());
                    objData.put("percentage", obj.getProgressPercentage());
                    objData.put("completed", obj.isCompleted());
                    objectives.add(objData);
                }
            }
            scoreboard.put("objectives", objectives);
        }
        
        return scoreboard;
    }
    
    /**
     * Propose a ceasefire
     */
    public Ceasefire proposeCeasefire(String warId, String proposingKingdom, 
                                     String acceptingKingdom, long duration, String terms) {
        String ceasefireId = UUID.randomUUID().toString().substring(0, 8);
        Ceasefire ceasefire = new Ceasefire(ceasefireId, warId, proposingKingdom, 
                                           acceptingKingdom, duration, terms);
        
        ceasefires.put(ceasefireId, ceasefire);
        warCeasefires.computeIfAbsent(warId, k -> new ArrayList<>()).add(ceasefireId);
        
        addWarHistoryEntry(warId, "Ceasefire proposed", 
            proposingKingdom + " proposed ceasefire to " + acceptingKingdom + ". Terms: " + terms, 
            proposingKingdom);
        
        return ceasefire;
    }
    
    /**
     * Accept a ceasefire
     */
    public boolean acceptCeasefire(String ceasefireId) {
        Ceasefire ceasefire = ceasefires.get(ceasefireId);
        if (ceasefire == null || ceasefire.getStatus() != Ceasefire.CeasefireStatus.PROPOSED) {
            return false;
        }
        
        ceasefire.setStatus(Ceasefire.CeasefireStatus.ACTIVE);
        
        addWarHistoryEntry(ceasefire.getWarId(), "Ceasefire accepted", 
            ceasefire.getAcceptingKingdom() + " accepted ceasefire from " + 
            ceasefire.getProposingKingdom(), ceasefire.getAcceptingKingdom());
        
        return true;
    }
    
    /**
     * End war and calculate reparations
     */
    public void endWarWithReparations(String warId, String winnerKingdom) {
        // Find war by ID
        War war = null;
        for (War w : baseWarManager.getActiveWars(winnerKingdom)) {
            if (w.getWarId().equals(warId)) {
                war = w;
                break;
            }
        }
        if (war == null) return;
        
        baseWarManager.endWar(warId);
        
        // Calculate reparations based on war score
        if (winnerKingdom != null) {
            String loserKingdom = winnerKingdom.equals(war.getKingdom1()) ? 
                                 war.getKingdom2() : war.getKingdom1();
            
            // Calculate reparation amount (based on score difference)
            int scoreDiff = Math.abs(war.getKingdom1Score() - war.getKingdom2Score());
            double reparationAmount = scoreDiff * 100.0; // 100 per point difference
            
            if (reparationAmount > 0) {
                long dueDate = System.currentTimeMillis() / 1000 + (30L * 86400L); // 30 days
                String reparationId = UUID.randomUUID().toString().substring(0, 8);
                WarReparation reparation = new WarReparation(reparationId, warId, loserKingdom, 
                                                            winnerKingdom, reparationAmount, dueDate);
                
                warReparations.put(reparationId, reparation);
                kingdomReparations.computeIfAbsent(loserKingdom, k -> new ArrayList<>()).add(reparationId);
                
                addWarHistoryEntry(warId, "War ended with reparations", 
                    "War ended. " + loserKingdom + " must pay " + reparationAmount + 
                    " to " + winnerKingdom + " within 30 days.", "SYSTEM");
            }
        }
        
        addWarHistoryEntry(warId, "War ended", 
            "War between " + war.getKingdom1() + " and " + war.getKingdom2() + " has ended.", "SYSTEM");
    }
    
    /**
     * Get war history
     */
    public List<WarHistoryEntry> getWarHistory(String warId) {
        return warHistory.getOrDefault(warId, new ArrayList<>());
    }
    
    /**
     * Add a war history entry
     */
    private void addWarHistoryEntry(String warId, String eventType, String description, String actor) {
        WarHistoryEntry entry = new WarHistoryEntry(
            System.currentTimeMillis() / 1000,
            warId,
            eventType,
            description,
            actor
        );
        
        warHistory.computeIfAbsent(warId, k -> new ArrayList<>()).add(entry);
        
        // Keep only last 100 entries per war
        List<WarHistoryEntry> entries = warHistory.get(warId);
        if (entries.size() > 100) {
            entries.remove(0);
        }
    }
    
    /**
     * Get all reparations for a kingdom
     */
    public List<WarReparation> getKingdomReparations(String kingdomName) {
        List<String> reparationIds = kingdomReparations.get(kingdomName);
        if (reparationIds == null) return new ArrayList<>();
        
        List<WarReparation> result = new ArrayList<>();
        for (String repId : reparationIds) {
            WarReparation rep = warReparations.get(repId);
            if (rep != null && !rep.isPaidOff()) {
                result.add(rep);
            }
        }
        return result;
    }
    
    /**
     * Process reparation payments
     */
    public void processReparationPayments() {
        for (WarReparation reparation : warReparations.values()) {
            if (reparation.isPaidOff() || !reparation.isOverdue()) continue;
            
            // Try to auto-pay from kingdom bank
            if (plugin.getBankManager() != null) {
                double balance = plugin.getBankManager().getBalance(reparation.getPayingKingdom());
                double remaining = reparation.getRemainingAmount();
                
                if (balance >= remaining) {
                    plugin.getBankManager().withdraw(reparation.getPayingKingdom(), remaining);
                    plugin.getBankManager().deposit(reparation.getReceivingKingdom(), remaining);
                    reparation.addPayment(remaining);
                }
            }
        }
    }
    
    /**
     * Check for expired ceasefires
     */
    public void checkExpiredCeasefires() {
        for (Ceasefire ceasefire : ceasefires.values()) {
            if (ceasefire.isExpired() && ceasefire.getStatus() == Ceasefire.CeasefireStatus.ACTIVE) {
                ceasefire.setStatus(Ceasefire.CeasefireStatus.EXPIRED);
                addWarHistoryEntry(ceasefire.getWarId(), "Ceasefire expired", 
                    "Ceasefire between " + ceasefire.getProposingKingdom() + " and " + 
                    ceasefire.getAcceptingKingdom() + " has expired.", "SYSTEM");
            }
        }
    }
}

