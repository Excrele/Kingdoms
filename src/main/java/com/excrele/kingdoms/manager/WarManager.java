package com.excrele.kingdoms.manager;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.War;
import org.bukkit.entity.Player;

import java.util.*;

public class WarManager {
    private final KingdomsPlugin plugin;
    private final Map<String, War> activeWars; // warId -> War
    private final Map<String, List<String>> kingdomWars; // kingdom -> list of warIds

    public WarManager(KingdomsPlugin plugin) {
        this.plugin = plugin;
        this.activeWars = new HashMap<>();
        this.kingdomWars = new HashMap<>();
        loadActiveWars();
    }

    private void loadActiveWars() {
        List<Map<String, Object>> wars = plugin.getStorageManager().getAdapter().loadActiveWars();
        for (Map<String, Object> warData : wars) {
            String warId = (String) warData.get("warId");
            String kingdom1 = (String) warData.get("kingdom1");
            String kingdom2 = (String) warData.get("kingdom2");
            long startTime = (Long) warData.get("startTime");
            long endTime = (Long) warData.get("endTime");
            
            War war = new War(kingdom1, kingdom2, endTime - startTime);
            war.getWarId(); // Initialize warId
            // Note: We'd need to add a setter or constructor that accepts warId
            // For now, we'll create new wars and let them expire naturally
            
            if (!war.isExpired()) {
                activeWars.put(warId, war);
                kingdomWars.computeIfAbsent(kingdom1, k -> new ArrayList<>()).add(warId);
                kingdomWars.computeIfAbsent(kingdom2, k -> new ArrayList<>()).add(warId);
            }
        }
    }

    public boolean declareWar(String declaringKingdom, String targetKingdom, long duration) {
        return declareWar(declaringKingdom, targetKingdom, duration, null, null);
    }
    
    public boolean declareWar(String declaringKingdom, String targetKingdom, long duration, 
                             String reason, String declaredBy) {
        if (declaringKingdom.equals(targetKingdom)) {
            return false; // Can't declare war on yourself
        }
        
        // Check if already at war
        if (isAtWar(declaringKingdom, targetKingdom)) {
            return false;
        }
        
        // Check diplomacy - non-aggression pacts prevent war
        if (plugin.getDiplomacyManager() != null) {
            if (!plugin.getDiplomacyManager().canDeclareWar(declaringKingdom, targetKingdom)) {
                return false; // Non-aggression pact in place
            }
        }
        
        War war = new War(declaringKingdom, targetKingdom, duration, reason, declaredBy);
        activeWars.put(war.getWarId(), war);
        kingdomWars.computeIfAbsent(declaringKingdom, k -> new ArrayList<>()).add(war.getWarId());
        kingdomWars.computeIfAbsent(targetKingdom, k -> new ArrayList<>()).add(war.getWarId());
        
        // Save to storage
        plugin.getStorageManager().getAdapter().saveWar(
            war.getWarId(),
            declaringKingdom,
            targetKingdom,
            war.getStartTime(),
            war.getEndTime(),
            true
        );
        
        return true;
    }

    public boolean isAtWar(String kingdom1, String kingdom2) {
        List<String> wars1 = kingdomWars.get(kingdom1);
        if (wars1 == null) return false;
        
        for (String warId : wars1) {
            War war = activeWars.get(warId);
            if (war != null && war.isActive() && !war.isExpired()) {
                if ((war.getKingdom1().equals(kingdom1) && war.getKingdom2().equals(kingdom2)) ||
                    (war.getKingdom1().equals(kingdom2) && war.getKingdom2().equals(kingdom1))) {
                    return true;
                }
            }
        }
        return false;
    }

    public War getWar(String kingdom1, String kingdom2) {
        List<String> wars1 = kingdomWars.get(kingdom1);
        if (wars1 == null) return null;
        
        for (String warId : wars1) {
            War war = activeWars.get(warId);
            if (war != null && war.isActive() && !war.isExpired()) {
                if ((war.getKingdom1().equals(kingdom1) && war.getKingdom2().equals(kingdom2)) ||
                    (war.getKingdom1().equals(kingdom2) && war.getKingdom2().equals(kingdom1))) {
                    return war;
                }
            }
        }
        return null;
    }

    public void endWar(String warId) {
        War war = activeWars.get(warId);
        if (war != null) {
            war.setActive(false);
            plugin.getStorageManager().getAdapter().saveWar(
                warId,
                war.getKingdom1(),
                war.getKingdom2(),
                war.getStartTime(),
                war.getEndTime(),
                false
            );
        }
    }

    public List<War> getActiveWars(String kingdom) {
        List<War> wars = new ArrayList<>();
        List<String> warIds = kingdomWars.get(kingdom);
        if (warIds == null) return wars;
        
        for (String warId : warIds) {
            War war = activeWars.get(warId);
            if (war != null && war.isActive() && !war.isExpired()) {
                wars.add(war);
            }
        }
        return wars;
    }

    public void checkExpiredWars() {
        List<String> toRemove = new ArrayList<>();
        for (Map.Entry<String, War> entry : activeWars.entrySet()) {
            if (entry.getValue().isExpired()) {
                endWar(entry.getKey());
                toRemove.add(entry.getKey());
            }
        }
        for (String warId : toRemove) {
            activeWars.remove(warId);
        }
    }

    public boolean canAttack(Player attacker, Player defender) {
        String attackerKingdom = plugin.getKingdomManager().getKingdomOfPlayer(attacker.getName());
        String defenderKingdom = plugin.getKingdomManager().getKingdomOfPlayer(defender.getName());
        
        if (attackerKingdom == null || defenderKingdom == null) {
            return false;
        }
        
        if (attackerKingdom.equals(defenderKingdom)) {
            return false; // Same kingdom
        }
        
        return isAtWar(attackerKingdom, defenderKingdom);
    }
}

