package com.excrele.kingdoms.manager;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.Kingdom;
import com.excrele.kingdoms.model.TradeRoute;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages trade routes between kingdoms
 */
public class TradeRouteManager {
    private final KingdomsPlugin plugin;
    // routeId -> TradeRoute
    private final Map<String, TradeRoute> tradeRoutes;
    // kingdom -> List of routeIds
    private final Map<String, List<String>> kingdomRoutes;
    
    public TradeRouteManager(KingdomsPlugin plugin) {
        this.plugin = plugin;
        this.tradeRoutes = new ConcurrentHashMap<>();
        this.kingdomRoutes = new ConcurrentHashMap<>();
        loadTradeRoutes();
    }
    
    private void loadTradeRoutes() {
        List<Map<String, Object>> routes = plugin.getStorageManager().getAdapter().loadTradeRoutes();
        for (Map<String, Object> routeData : routes) {
            String routeId = (String) routeData.get("routeId");
            String kingdom1 = (String) routeData.get("kingdom1");
            String kingdom2 = (String) routeData.get("kingdom2");
            String world1 = (String) routeData.get("world1");
            double x1 = ((Number) routeData.get("x1")).doubleValue();
            double y1 = ((Number) routeData.get("y1")).doubleValue();
            double z1 = ((Number) routeData.get("z1")).doubleValue();
            String world2 = (String) routeData.get("world2");
            double x2 = ((Number) routeData.get("x2")).doubleValue();
            double y2 = ((Number) routeData.get("y2")).doubleValue();
            double z2 = ((Number) routeData.get("z2")).doubleValue();
            long establishedAt = ((Number) routeData.get("establishedAt")).longValue();
            boolean active = (Boolean) routeData.getOrDefault("active", true);
            double tradeVolume = ((Number) routeData.getOrDefault("tradeVolume", 0.0)).doubleValue();
            int tradeCount = ((Number) routeData.getOrDefault("tradeCount", 0)).intValue();
            long lastTradeTime = ((Number) routeData.getOrDefault("lastTradeTime", 0L)).longValue();
            
            TradeRoute route = new TradeRoute(routeId, kingdom1, kingdom2,
                                             world1, x1, y1, z1,
                                             world2, x2, y2, z2,
                                             establishedAt, active, tradeVolume, tradeCount, lastTradeTime);
            
            tradeRoutes.put(routeId, route);
            kingdomRoutes.computeIfAbsent(kingdom1, k -> new ArrayList<>()).add(routeId);
            kingdomRoutes.computeIfAbsent(kingdom2, k -> new ArrayList<>()).add(routeId);
        }
    }
    
    /**
     * Establish a trade route between two kingdoms
     */
    public boolean establishRoute(String kingdom1, String kingdom2, Location endpoint1, Location endpoint2) {
        // Check if kingdoms exist
        Kingdom k1 = plugin.getKingdomManager().getKingdom(kingdom1);
        Kingdom k2 = plugin.getKingdomManager().getKingdom(kingdom2);
        if (k1 == null || k2 == null) return false;
        
        // Check if already have a route
        if (hasRoute(kingdom1, kingdom2)) return false;
        
        // Check if kingdoms are at war
        if (plugin.getWarManager().isAtWar(kingdom1, kingdom2)) {
            return false; // Can't trade during war
        }
        
        // Create trade route
        TradeRoute route = new TradeRoute(kingdom1, kingdom2, endpoint1, endpoint2);
        tradeRoutes.put(route.getRouteId(), route);
        kingdomRoutes.computeIfAbsent(kingdom1, k -> new ArrayList<>()).add(route.getRouteId());
        kingdomRoutes.computeIfAbsent(kingdom2, k -> new ArrayList<>()).add(route.getRouteId());
        
        saveTradeRoute(route);
        broadcastRouteEstablished(route);
        
        return true;
    }
    
    /**
     * Execute a trade between two kingdoms via trade route
     */
    public boolean executeTrade(Player trader, String fromKingdom, String toKingdom, double amount) {
        TradeRoute route = getRoute(fromKingdom, toKingdom);
        if (route == null || !route.isActive()) {
            return false; // No active route
        }
        
        // Check if kingdoms are at war
        if (plugin.getWarManager().isAtWar(fromKingdom, toKingdom)) {
            return false; // Can't trade during war
        }
        
        // Check if player is in the sending kingdom
        String playerKingdom = plugin.getKingdomManager().getKingdomOfPlayer(trader.getName());
        if (!fromKingdom.equals(playerKingdom)) {
            return false; // Player not in sending kingdom
        }
        
        // Check if sending kingdom has enough money
        double senderBalance = plugin.getBankManager().getBalance(fromKingdom);
        if (senderBalance < amount) {
            return false; // Insufficient funds
        }
        
        // Execute transfer
        if (plugin.getBankManager().withdraw(fromKingdom, amount)) {
            plugin.getBankManager().deposit(toKingdom, amount);
            
            // Update route statistics
            route.addTradeVolume(amount);
            saveTradeRoute(route);
            
            trader.sendMessage("§6[Trade] Sent §e" + String.format("%.2f", amount) + 
                " §6to " + toKingdom + " via trade route");
            
            // Notify receiving kingdom
            Kingdom receivingKingdom = plugin.getKingdomManager().getKingdom(toKingdom);
            if (receivingKingdom != null) {
                String message = "§6[Trade] Received §e" + String.format("%.2f", amount) + 
                    " §6from " + fromKingdom + " via trade route";
                broadcastToKingdom(receivingKingdom, message);
            }
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Close a trade route
     */
    public boolean closeRoute(String kingdom1, String kingdom2) {
        TradeRoute route = getRoute(kingdom1, kingdom2);
        if (route == null) return false;
        
        route.setActive(false);
        saveTradeRoute(route);
        broadcastRouteClosed(route);
        
        return true;
    }
    
    /**
     * Check if two kingdoms have a trade route
     */
    public boolean hasRoute(String kingdom1, String kingdom2) {
        return getRoute(kingdom1, kingdom2) != null;
    }
    
    /**
     * Get trade route between two kingdoms
     */
    public TradeRoute getRoute(String kingdom1, String kingdom2) {
        List<String> routes1 = kingdomRoutes.get(kingdom1);
        if (routes1 == null) return null;
        
        for (String routeId : routes1) {
            TradeRoute route = tradeRoutes.get(routeId);
            if (route != null && route.isActive() && route.involvesKingdom(kingdom2)) {
                return route;
            }
        }
        return null;
    }
    
    /**
     * Get all active routes for a kingdom
     */
    public List<TradeRoute> getKingdomRoutes(String kingdomName) {
        List<TradeRoute> routes = new ArrayList<>();
        List<String> routeIds = kingdomRoutes.getOrDefault(kingdomName, new ArrayList<>());
        for (String routeId : routeIds) {
            TradeRoute route = tradeRoutes.get(routeId);
            if (route != null && route.isActive() && route.involvesKingdom(kingdomName)) {
                routes.add(route);
            }
        }
        return routes;
    }
    
    private void broadcastRouteEstablished(TradeRoute route) {
        Kingdom k1 = plugin.getKingdomManager().getKingdom(route.getKingdom1());
        Kingdom k2 = plugin.getKingdomManager().getKingdom(route.getKingdom2());
        
        String message1 = "§6[Trade] Trade route established with " + route.getKingdom2();
        String message2 = "§6[Trade] Trade route established with " + route.getKingdom1();
        
        if (k1 != null) broadcastToKingdom(k1, message1);
        if (k2 != null) broadcastToKingdom(k2, message2);
    }
    
    private void broadcastRouteClosed(TradeRoute route) {
        Kingdom k1 = plugin.getKingdomManager().getKingdom(route.getKingdom1());
        Kingdom k2 = plugin.getKingdomManager().getKingdom(route.getKingdom2());
        
        String message = "§c[Trade] Trade route closed";
        
        if (k1 != null) broadcastToKingdom(k1, message);
        if (k2 != null) broadcastToKingdom(k2, message);
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
    
    private void saveTradeRoute(TradeRoute route) {
        Location ep1 = route.getEndpoint1();
        Location ep2 = route.getEndpoint2();
        
        plugin.getStorageManager().getAdapter().saveTradeRoute(
            route.getRouteId(),
            route.getKingdom1(),
            route.getKingdom2(),
            ep1 != null ? ep1.getWorld().getName() : "",
            ep1 != null ? ep1.getX() : 0,
            ep1 != null ? ep1.getY() : 0,
            ep1 != null ? ep1.getZ() : 0,
            ep2 != null ? ep2.getWorld().getName() : "",
            ep2 != null ? ep2.getX() : 0,
            ep2 != null ? ep2.getY() : 0,
            ep2 != null ? ep2.getZ() : 0,
            route.getEstablishedAt(),
            route.isActive(),
            route.getTradeVolume(),
            route.getTradeCount(),
            route.getLastTradeTime()
        );
    }
}

