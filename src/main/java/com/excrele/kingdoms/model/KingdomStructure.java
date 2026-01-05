package com.excrele.kingdoms.model;

import org.bukkit.Location;
import org.bukkit.Material;

/**
 * Represents a special kingdom structure with bonuses
 */
public class KingdomStructure {
    private String structureId;
    private String kingdomName;
    private StructureType type;
    private Location location;
    private long builtAt;
    private int level; // Structure level (affects bonuses)
    private boolean active;
    private String worldName;
    private double x, y, z;
    
    public enum StructureType {
        THRONE,      // Increases kingdom XP gain
        WAR_ROOM,    // Improves war/siege capabilities
        TREASURY,    // Increases bank capacity and interest
        EMBASSY,     // For diplomacy (separate system)
        GRANARY,     // Resource storage bonus
        BARRACKS     // Defense bonuses
    }
    
    public KingdomStructure(String kingdomName, StructureType type, Location location) {
        this.structureId = java.util.UUID.randomUUID().toString();
        this.kingdomName = kingdomName;
        this.type = type;
        this.location = location;
        this.worldName = location.getWorld().getName();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.builtAt = System.currentTimeMillis() / 1000;
        this.level = 1;
        this.active = true;
    }
    
    public KingdomStructure(String structureId, String kingdomName, StructureType type,
                           String worldName, double x, double y, double z,
                           long builtAt, int level, boolean active) {
        this.structureId = structureId;
        this.kingdomName = kingdomName;
        this.type = type;
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        org.bukkit.World world = org.bukkit.Bukkit.getWorld(worldName);
        if (world != null) {
            this.location = new Location(world, x, y, z);
        }
        this.builtAt = builtAt;
        this.level = level;
        this.active = active;
    }
    
    public String getStructureId() { return structureId; }
    public String getKingdomName() { return kingdomName; }
    public StructureType getType() { return type; }
    public Location getLocation() { return location; }
    public long getBuiltAt() { return builtAt; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public void upgrade() { this.level++; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getWorldName() { return worldName; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    
    /**
     * Get bonus multiplier based on structure type and level
     */
    public double getBonusMultiplier() {
        double baseBonus = 1.0 + (level * 0.1); // 10% per level
        return switch (type) {
            case THRONE -> baseBonus; // XP gain bonus
            case WAR_ROOM -> baseBonus * 1.2; // War bonuses
            case TREASURY -> baseBonus; // Bank capacity
            case EMBASSY -> 1.0; // No direct bonus, enables diplomacy
            case GRANARY -> baseBonus; // Storage capacity
            case BARRACKS -> baseBonus * 1.15; // Defense bonuses
        };
    }
    
    /**
     * Get material for structure block
     */
    public Material getStructureMaterial() {
        return switch (type) {
            case THRONE -> Material.GOLDEN_APPLE;
            case WAR_ROOM -> Material.IRON_SWORD;
            case TREASURY -> Material.GOLD_BLOCK;
            case EMBASSY -> Material.ENCHANTING_TABLE; // Embassy structure
            case GRANARY -> Material.HAY_BLOCK;
            case BARRACKS -> Material.IRON_BLOCK;
        };
    }
}

