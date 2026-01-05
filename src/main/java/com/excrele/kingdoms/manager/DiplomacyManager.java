package com.excrele.kingdoms.manager;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.DiplomaticAgreement;
import com.excrele.kingdoms.model.Kingdom;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages diplomatic agreements between kingdoms
 */
public class DiplomacyManager {
    private final KingdomsPlugin plugin;
    // agreementId -> Agreement
    private final Map<String, DiplomaticAgreement> agreements;
    // kingdom -> List of agreementIds
    private final Map<String, List<String>> kingdomAgreements;
    
    public DiplomacyManager(KingdomsPlugin plugin) {
        this.plugin = plugin;
        this.agreements = new ConcurrentHashMap<>();
        this.kingdomAgreements = new ConcurrentHashMap<>();
        loadAllAgreements();
    }
    
    private void loadAllAgreements() {
        List<Map<String, Object>> agreementsData = plugin.getStorageManager().getAdapter().loadDiplomaticAgreements();
        for (Map<String, Object> data : agreementsData) {
            String agreementId = (String) data.get("agreementId");
            String kingdom1 = (String) data.get("kingdom1");
            String kingdom2 = (String) data.get("kingdom2");
            DiplomaticAgreement.AgreementType type = DiplomaticAgreement.AgreementType.valueOf(
                (String) data.get("type"));
            long establishedAt = ((Number) data.get("establishedAt")).longValue();
            long expiresAt = ((Number) data.getOrDefault("expiresAt", -1L)).longValue();
            boolean active = (Boolean) data.getOrDefault("active", true);
            String terms = (String) data.getOrDefault("terms", "");
            
            DiplomaticAgreement agreement = new DiplomaticAgreement(agreementId, kingdom1, kingdom2,
                                                                   type, establishedAt, expiresAt, active, terms);
            
            if (agreement.isActive() && !agreement.isExpired()) {
                agreements.put(agreementId, agreement);
                kingdomAgreements.computeIfAbsent(kingdom1, k -> new ArrayList<>()).add(agreementId);
                kingdomAgreements.computeIfAbsent(kingdom2, k -> new ArrayList<>()).add(agreementId);
            }
        }
    }
    
    /**
     * Propose a diplomatic agreement
     */
    public boolean proposeAgreement(String proposingKingdom, String targetKingdom, 
                                   DiplomaticAgreement.AgreementType type, long duration, Player proposer) {
        Kingdom proposingK = plugin.getKingdomManager().getKingdom(proposingKingdom);
        Kingdom targetK = plugin.getKingdomManager().getKingdom(targetKingdom);
        if (proposingK == null || targetK == null) return false;
        
        // Check permission
        if (!proposingK.hasPermission(proposer.getName(), "setflags")) {
            return false;
        }
        
        // Check if already have this type of agreement
        if (hasAgreement(proposingKingdom, targetKingdom, type)) {
            return false; // Already have this agreement
        }
        
        // Check if at war (can't make agreements during war)
        if (plugin.getWarManager().isAtWar(proposingKingdom, targetKingdom)) {
            return false; // At war
        }
        
        // For embassy, check if both kingdoms have embassy structures
        if (type == DiplomaticAgreement.AgreementType.EMBASSY) {
            if (plugin.getStructureManager() == null ||
                !plugin.getStructureManager().hasStructure(proposingKingdom, 
                    com.excrele.kingdoms.model.KingdomStructure.StructureType.EMBASSY) ||
                !plugin.getStructureManager().hasStructure(targetKingdom, 
                    com.excrele.kingdoms.model.KingdomStructure.StructureType.EMBASSY)) {
                return false; // Both need embassy structures
            }
        }
        
        // Create agreement (requires acceptance)
        DiplomaticAgreement agreement = new DiplomaticAgreement(proposingKingdom, targetKingdom, type, duration);
        agreements.put(agreement.getAgreementId(), agreement);
        kingdomAgreements.computeIfAbsent(proposingKingdom, k -> new ArrayList<>()).add(agreement.getAgreementId());
        kingdomAgreements.computeIfAbsent(targetKingdom, k -> new ArrayList<>()).add(agreement.getAgreementId());
        
        saveAgreement(agreement);
        
        // Notify target kingdom
        String message = "§6[Diplomacy] §e" + proposingKingdom + " §6proposed a §e" + 
            type.name() + " §6agreement! Use §e/kingdom diplomacy accept " + agreement.getAgreementId();
        broadcastToKingdom(targetK, message);
        
        proposer.sendMessage("§6[Diplomacy] Agreement proposed to " + targetKingdom);
        
        return true;
    }
    
    /**
     * Accept a diplomatic agreement
     */
    public boolean acceptAgreement(String kingdomName, String agreementId, Player accepter) {
        Kingdom kingdom = plugin.getKingdomManager().getKingdom(kingdomName);
        if (kingdom == null) return false;
        
        DiplomaticAgreement agreement = agreements.get(agreementId);
        if (agreement == null || !agreement.involvesKingdom(kingdomName)) {
            return false;
        }
        
        // Check permission
        if (!kingdom.hasPermission(accepter.getName(), "setflags")) {
            return false;
        }
        
        // Agreement is now active
        agreement.setActive(true);
        saveAgreement(agreement);
        
        // Notify both kingdoms
        String otherKingdom = agreement.getOtherKingdom(kingdomName);
        Kingdom otherK = plugin.getKingdomManager().getKingdom(otherKingdom);
        
        String message1 = "§a[Diplomacy] §e" + agreement.getType().name() + " §aagreement with §e" + 
            otherKingdom + " §aestablished!";
        String message2 = "§a[Diplomacy] §e" + kingdomName + " §aaccepted your §e" + 
            agreement.getType().name() + " §aagreement!";
        
        broadcastToKingdom(kingdom, message1);
        if (otherK != null) {
            broadcastToKingdom(otherK, message2);
        }
        
        return true;
    }
    
    /**
     * Check if kingdoms have a specific agreement type
     */
    public boolean hasAgreement(String kingdom1, String kingdom2, DiplomaticAgreement.AgreementType type) {
        List<String> agreements1 = kingdomAgreements.get(kingdom1);
        if (agreements1 == null) return false;
        
        for (String agreementId : agreements1) {
            DiplomaticAgreement agreement = agreements.get(agreementId);
            if (agreement != null && agreement.isActive() && !agreement.isExpired() &&
                agreement.involvesKingdom(kingdom2) && agreement.getType() == type) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get all agreements for a kingdom
     */
    public List<DiplomaticAgreement> getKingdomAgreements(String kingdomName) {
        List<DiplomaticAgreement> result = new ArrayList<>();
        List<String> agreementIds = kingdomAgreements.getOrDefault(kingdomName, new ArrayList<>());
        for (String agreementId : agreementIds) {
            DiplomaticAgreement agreement = agreements.get(agreementId);
            if (agreement != null && agreement.isActive() && !agreement.isExpired() &&
                agreement.involvesKingdom(kingdomName)) {
                result.add(agreement);
            }
        }
        return result;
    }
    
    /**
     * Terminate an agreement
     */
    public boolean terminateAgreement(String kingdomName, String agreementId, Player terminator) {
        Kingdom kingdom = plugin.getKingdomManager().getKingdom(kingdomName);
        if (kingdom == null) return false;
        
        DiplomaticAgreement agreement = agreements.get(agreementId);
        if (agreement == null || !agreement.involvesKingdom(kingdomName)) {
            return false;
        }
        
        // Check permission
        if (!kingdom.hasPermission(terminator.getName(), "setflags")) {
            return false;
        }
        
        // Terminate agreement
        agreement.setActive(false);
        saveAgreement(agreement);
        
        String otherKingdom = agreement.getOtherKingdom(kingdomName);
        Kingdom otherK = plugin.getKingdomManager().getKingdom(otherKingdom);
        
        String message = "§c[Diplomacy] §e" + agreement.getType().name() + 
            " §cagreement with §e" + otherKingdom + " §cterminated";
        broadcastToKingdom(kingdom, message);
        
        if (otherK != null) {
            String message2 = "§c[Diplomacy] §e" + kingdomName + " §cterminated your §e" + 
                agreement.getType().name() + " §cagreement";
            broadcastToKingdom(otherK, message2);
        }
        
        return true;
    }
    
    /**
     * Check if kingdoms can declare war (no non-aggression pact)
     */
    public boolean canDeclareWar(String kingdom1, String kingdom2) {
        return !hasAgreement(kingdom1, kingdom2, DiplomaticAgreement.AgreementType.NON_AGGRESSION_PACT);
    }
    
    /**
     * Get trade bonus from trade agreements
     */
    public double getTradeBonus(String kingdom1, String kingdom2) {
        if (hasAgreement(kingdom1, kingdom2, DiplomaticAgreement.AgreementType.TRADE_AGREEMENT)) {
            return 1.1; // 10% bonus
        }
        return 1.0;
    }
    
    /**
     * Check and expire old agreements
     */
    public void checkExpiredAgreements() {
        List<String> toRemove = new ArrayList<>();
        for (Map.Entry<String, DiplomaticAgreement> entry : agreements.entrySet()) {
            DiplomaticAgreement agreement = entry.getValue();
            if (agreement.isExpired() || !agreement.isActive()) {
                agreement.setActive(false);
                saveAgreement(agreement);
                toRemove.add(entry.getKey());
            }
        }
        for (String agreementId : toRemove) {
            agreements.remove(agreementId);
        }
    }
    
    private void broadcastToKingdom(Kingdom kingdom, String message) {
        for (String member : kingdom.getMembers()) {
            Player player = plugin.getServer().getPlayer(member);
            if (player != null && player.isOnline()) {
                player.sendMessage(message);
            }
        }
        Player king = plugin.getServer().getPlayer(kingdom.getKing());
        if (king != null && king.isOnline()) {
            king.sendMessage(message);
        }
    }
    
    private void saveAgreement(DiplomaticAgreement agreement) {
        plugin.getStorageManager().getAdapter().saveDiplomaticAgreement(
            agreement.getAgreementId(),
            agreement.getKingdom1(),
            agreement.getKingdom2(),
            agreement.getType().name(),
            agreement.getEstablishedAt(),
            agreement.getExpiresAt(),
            agreement.isActive(),
            agreement.getTerms()
        );
    }
}

