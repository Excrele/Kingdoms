package com.excrele.kingdoms.model;

import org.bukkit.Location;

/**
 * Represents a trade route between two kingdoms
 */
public class TradeRoute {
    private String routeId;
    private String kingdom1;
    private String kingdom2;
    private Location endpoint1; // Kingdom 1's trade endpoint
    private Location endpoint2; // Kingdom 2's trade endpoint
    private long establishedAt;
    private boolean active;
    private double tradeVolume; // Total value traded
    private int tradeCount; // Number of trades
    private long lastTradeTime;
    
    public TradeRoute(String kingdom1, String kingdom2, Location endpoint1, Location endpoint2) {
        this.routeId = java.util.UUID.randomUUID().toString();
        this.kingdom1 = kingdom1;
        this.kingdom2 = kingdom2;
        this.endpoint1 = endpoint1;
        this.endpoint2 = endpoint2;
        this.establishedAt = System.currentTimeMillis() / 1000;
        this.active = true;
        this.tradeVolume = 0;
        this.tradeCount = 0;
        this.lastTradeTime = 0;
    }
    
    public TradeRoute(String routeId, String kingdom1, String kingdom2, 
                     String world1, double x1, double y1, double z1,
                     String world2, double x2, double y2, double z2,
                     long establishedAt, boolean active, double tradeVolume, 
                     int tradeCount, long lastTradeTime) {
        this.routeId = routeId;
        this.kingdom1 = kingdom1;
        this.kingdom2 = kingdom2;
        org.bukkit.World worldObj1 = org.bukkit.Bukkit.getWorld(world1);
        org.bukkit.World worldObj2 = org.bukkit.Bukkit.getWorld(world2);
        if (worldObj1 != null) {
            this.endpoint1 = new Location(worldObj1, x1, y1, z1);
        }
        if (worldObj2 != null) {
            this.endpoint2 = new Location(worldObj2, x2, y2, z2);
        }
        this.establishedAt = establishedAt;
        this.active = active;
        this.tradeVolume = tradeVolume;
        this.tradeCount = tradeCount;
        this.lastTradeTime = lastTradeTime;
    }
    
    public String getRouteId() { return routeId; }
    public String getKingdom1() { return kingdom1; }
    public String getKingdom2() { return kingdom2; }
    public Location getEndpoint1() { return endpoint1; }
    public Location getEndpoint2() { return endpoint2; }
    public long getEstablishedAt() { return establishedAt; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public double getTradeVolume() { return tradeVolume; }
    public void addTradeVolume(double amount) { 
        this.tradeVolume += amount;
        this.tradeCount++;
        this.lastTradeTime = System.currentTimeMillis() / 1000;
    }
    public int getTradeCount() { return tradeCount; }
    public long getLastTradeTime() { return lastTradeTime; }
    
    public boolean involvesKingdom(String kingdomName) {
        return kingdom1.equals(kingdomName) || kingdom2.equals(kingdomName);
    }
    
    public String getOtherKingdom(String kingdomName) {
        if (kingdom1.equals(kingdomName)) return kingdom2;
        if (kingdom2.equals(kingdomName)) return kingdom1;
        return null;
    }
}

