package com.excrele.kingdoms.manager;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.Kingdom;
import com.excrele.kingdoms.model.KingdomStructure;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages kingdom structures (throne, war room, treasury, etc.)
 */
public class StructureManager {
    private final KingdomsPlugin plugin;
    // kingdom -> structureId -> Structure
    private final Map<String, Map<String, KingdomStructure>> kingdomStructures;
    // structureId -> Structure (for quick lookup)
    private final Map<String, KingdomStructure> allStructures;
    
    public StructureManager(KingdomsPlugin plugin) {
        this.plugin = plugin;
        this.kingdomStructures = new ConcurrentHashMap<>();
        this.allStructures = new ConcurrentHashMap<>();
        loadAllStructures();
    }
    
    private void loadAllStructures() {
        List<Map<String, Object>> structures = plugin.getStorageManager().getAdapter().loadKingdomStructures();
        for (Map<String, Object> data : structures) {
            String structureId = (String) data.get("structureId");
            String kingdomName = (String) data.get("kingdomName");
            KingdomStructure.StructureType type = KingdomStructure.StructureType.valueOf(
                (String) data.get("type"));
            String worldName = (String) data.get("worldName");
            double x = ((Number) data.get("x")).doubleValue();
            double y = ((Number) data.get("y")).doubleValue();
            double z = ((Number) data.get("z")).doubleValue();
            long builtAt = ((Number) data.get("builtAt")).longValue();
            int level = ((Number) data.getOrDefault("level", 1)).intValue();
            boolean active = (Boolean) data.getOrDefault("active", true);
            
            KingdomStructure structure = new KingdomStructure(structureId, kingdomName, type,
                                                             worldName, x, y, z, builtAt, level, active);
            
            kingdomStructures.computeIfAbsent(kingdomName, k -> new ConcurrentHashMap<>())
                .put(structureId, structure);
            allStructures.put(structureId, structure);
        }
    }
    
    /**
     * Build a structure at a location
     */
    public boolean buildStructure(String kingdomName, KingdomStructure.StructureType type, Location location, Player builder) {
        Kingdom kingdom = plugin.getKingdomManager().getKingdom(kingdomName);
        if (kingdom == null) return false;
        
        // Check if player has permission
        if (!kingdom.hasPermission(builder.getName(), "setflags")) {
            return false; // Need setflags permission
        }
        
        // Check if structure type already exists (one per type per kingdom)
        if (hasStructure(kingdomName, type)) {
            return false; // Already has this structure type
        }
        
        // Check if location is in kingdom claim
        org.bukkit.Chunk chunk = location.getChunk();
        Kingdom chunkKingdom = plugin.getKingdomManager().getKingdomByChunk(chunk);
        if (chunkKingdom == null || !chunkKingdom.getName().equals(kingdomName)) {
            return false; // Not in kingdom claim
        }
        
        // Create structure
        KingdomStructure structure = new KingdomStructure(kingdomName, type, location);
        kingdomStructures.computeIfAbsent(kingdomName, k -> new ConcurrentHashMap<>())
            .put(structure.getStructureId(), structure);
        allStructures.put(structure.getStructureId(), structure);
        
        // Save to storage
        saveStructure(structure);
        
        // Place structure block
        location.getBlock().setType(structure.getStructureMaterial());
        
        // Notify kingdom
        String message = "§6[Structure] §e" + type.name() + " §6built at (" + 
            (int)location.getX() + ", " + (int)location.getY() + ", " + (int)location.getZ() + ")";
        broadcastToKingdom(kingdom, message);
        
        return true;
    }
    
    /**
     * Upgrade a structure
     */
    public boolean upgradeStructure(String kingdomName, String structureId, Player upgrader) {
        Kingdom kingdom = plugin.getKingdomManager().getKingdom(kingdomName);
        if (kingdom == null) return false;
        
        KingdomStructure structure = allStructures.get(structureId);
        if (structure == null || !structure.getKingdomName().equals(kingdomName)) {
            return false;
        }
        
        // Check permission
        if (!kingdom.hasPermission(upgrader.getName(), "setflags")) {
            return false;
        }
        
        // Check max level
        int maxLevel = plugin.getConfig().getInt("structures.max-level", 5);
        if (structure.getLevel() >= maxLevel) {
            return false; // Already at max level
        }
        
        // Check cost (XP or money)
        int upgradeCost = structure.getLevel() * 1000; // XP cost
        if (kingdom.getXp() < upgradeCost) {
            return false; // Not enough XP
        }
        
        // Upgrade
        structure.upgrade();
        kingdom.setXp(kingdom.getXp() - upgradeCost);
        saveStructure(structure);
        
        String message = "§6[Structure] §e" + structure.getType().name() + 
            " §6upgraded to level §e" + structure.getLevel();
        broadcastToKingdom(kingdom, message);
        
        return true;
    }
    
    /**
     * Get structure bonus for a kingdom
     */
    public double getStructureBonus(String kingdomName, KingdomStructure.StructureType type) {
        KingdomStructure structure = getStructure(kingdomName, type);
        if (structure == null || !structure.isActive()) {
            return 1.0; // No bonus
        }
        return structure.getBonusMultiplier();
    }
    
    /**
     * Get all structure bonuses for a kingdom
     */
    public Map<KingdomStructure.StructureType, Double> getAllBonuses(String kingdomName) {
        Map<KingdomStructure.StructureType, Double> bonuses = new HashMap<>();
        Map<String, KingdomStructure> structures = kingdomStructures.get(kingdomName);
        if (structures != null) {
            for (KingdomStructure structure : structures.values()) {
                if (structure.isActive()) {
                    bonuses.put(structure.getType(), structure.getBonusMultiplier());
                }
            }
        }
        return bonuses;
    }
    
    /**
     * Check if kingdom has a structure type
     */
    public boolean hasStructure(String kingdomName, KingdomStructure.StructureType type) {
        Map<String, KingdomStructure> structures = kingdomStructures.get(kingdomName);
        if (structures == null) return false;
        
        for (KingdomStructure structure : structures.values()) {
            if (structure.getType() == type && structure.isActive()) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get structure of a type for a kingdom
     */
    public KingdomStructure getStructure(String kingdomName, KingdomStructure.StructureType type) {
        Map<String, KingdomStructure> structures = kingdomStructures.get(kingdomName);
        if (structures == null) return null;
        
        for (KingdomStructure structure : structures.values()) {
            if (structure.getType() == type && structure.isActive()) {
                return structure;
            }
        }
        return null;
    }
    
    /**
     * Get all structures for a kingdom
     */
    public List<KingdomStructure> getKingdomStructures(String kingdomName) {
        Map<String, KingdomStructure> structures = kingdomStructures.get(kingdomName);
        if (structures == null) return new ArrayList<>();
        return new ArrayList<>(structures.values());
    }
    
    /**
     * Destroy a structure
     */
    public boolean destroyStructure(String kingdomName, String structureId, Player destroyer) {
        Kingdom kingdom = plugin.getKingdomManager().getKingdom(kingdomName);
        if (kingdom == null) return false;
        
        KingdomStructure structure = allStructures.get(structureId);
        if (structure == null || !structure.getKingdomName().equals(kingdomName)) {
            return false;
        }
        
        // Check permission
        if (!kingdom.hasPermission(destroyer.getName(), "setflags")) {
            return false;
        }
        
        // Remove structure
        structure.setActive(false);
        kingdomStructures.get(kingdomName).remove(structureId);
        allStructures.remove(structureId);
        
        // Remove block
        if (structure.getLocation() != null) {
            structure.getLocation().getBlock().setType(org.bukkit.Material.AIR);
        }
        
        // Save
        saveStructure(structure);
        
        String message = "§c[Structure] §e" + structure.getType().name() + " §cdestroyed";
        broadcastToKingdom(kingdom, message);
        
        return true;
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
    
    private void saveStructure(KingdomStructure structure) {
        plugin.getStorageManager().getAdapter().saveKingdomStructure(
            structure.getStructureId(),
            structure.getKingdomName(),
            structure.getType().name(),
            structure.getWorldName(),
            structure.getX(),
            structure.getY(),
            structure.getZ(),
            structure.getBuiltAt(),
            structure.getLevel(),
            structure.isActive()
        );
    }
}

