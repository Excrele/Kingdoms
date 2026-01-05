package com.excrele.kingdoms.model;

import org.bukkit.Material;

/**
 * Represents a resource type and amount
 */
public class KingdomResource {
    private Material material;
    private int amount;
    private String resourceType; // For non-material resources
    
    public KingdomResource(Material material, int amount) {
        this.material = material;
        this.amount = amount;
        this.resourceType = material.name();
    }
    
    public KingdomResource(String resourceType, int amount) {
        this.resourceType = resourceType;
        this.amount = amount;
        this.material = null;
    }
    
    public Material getMaterial() { return material; }
    public String getResourceType() { return resourceType; }
    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }
    public void addAmount(int amount) { this.amount += amount; }
    public void removeAmount(int amount) { 
        this.amount = Math.max(0, this.amount - amount); 
    }
    
    /**
     * Get estimated value of resource
     */
    public double getEstimatedValue() {
        // Base values for common materials
        return switch (material != null ? material : Material.AIR) {
            case DIAMOND -> amount * 10.0;
            case EMERALD -> amount * 5.0;
            case GOLD_INGOT -> amount * 2.0;
            case IRON_INGOT -> amount * 1.0;
            case COAL -> amount * 0.5;
            default -> amount * 0.1;
        };
    }
}

