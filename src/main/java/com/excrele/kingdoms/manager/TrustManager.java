package com.excrele.kingdoms.manager;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.TrustPermission;
import org.bukkit.entity.Player;

import java.util.*;

public class TrustManager {
    private final KingdomsPlugin plugin;
    private final Map<String, Map<String, Set<TrustPermission>>> kingdomTrusts; // kingdom -> player -> permissions

    public TrustManager(KingdomsPlugin plugin) {
        this.plugin = plugin;
        this.kingdomTrusts = new HashMap<>();
        loadAllTrusts();
    }

    private void loadAllTrusts() {
        // Load trusts from storage
        try {
            for (String kingdomName : plugin.getKingdomManager().getKingdoms().keySet()) {
                Map<String, List<String>> trusts = plugin.getStorageManager().getAdapter().loadTrusts(kingdomName);
                for (Map.Entry<String, List<String>> entry : trusts.entrySet()) {
                    Set<TrustPermission> perms = new HashSet<>();
                    for (String permStr : entry.getValue()) {
                        TrustPermission perm = TrustPermission.fromKey(permStr);
                        if (perm != null) {
                            perms.add(perm);
                        }
                    }
                    kingdomTrusts.computeIfAbsent(kingdomName, k -> new HashMap<>()).put(entry.getKey(), perms);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load trusts: " + e.getMessage());
        }
    }

    public void trustPlayer(String kingdomName, String player, TrustPermission permission) {
        kingdomTrusts.computeIfAbsent(kingdomName, k -> new HashMap<>())
            .computeIfAbsent(player, k -> new HashSet<>())
            .add(permission);
        
        // Save to storage
        plugin.getStorageManager().getAdapter().saveTrust(kingdomName, player, permission.getKey());
    }

    public void untrustPlayer(String kingdomName, String player) {
        Map<String, Set<TrustPermission>> kingdom = kingdomTrusts.get(kingdomName);
        if (kingdom != null) {
            kingdom.remove(player);
            plugin.getStorageManager().getAdapter().deleteTrust(kingdomName, player);
        }
    }

    public void untrustPlayer(String kingdomName, String player, TrustPermission permission) {
        Map<String, Set<TrustPermission>> kingdom = kingdomTrusts.get(kingdomName);
        if (kingdom != null) {
            Set<TrustPermission> perms = kingdom.get(player);
            if (perms != null) {
                perms.remove(permission);
                if (perms.isEmpty()) {
                    kingdom.remove(player);
                    plugin.getStorageManager().getAdapter().deleteTrust(kingdomName, player);
                } else {
                    // Re-save all permissions
                    plugin.getStorageManager().getAdapter().deleteTrust(kingdomName, player);
                    for (TrustPermission perm : perms) {
                        plugin.getStorageManager().getAdapter().saveTrust(kingdomName, player, perm.getKey());
                    }
                }
            }
        }
    }

    public boolean hasTrust(String kingdomName, String player, TrustPermission permission) {
        Map<String, Set<TrustPermission>> kingdom = kingdomTrusts.get(kingdomName);
        if (kingdom == null) return false;
        
        Set<TrustPermission> perms = kingdom.get(player);
        if (perms == null) return false;
        
        return perms.contains(TrustPermission.ALL) || perms.contains(permission);
    }

    public Set<TrustPermission> getTrusts(String kingdomName, String player) {
        Map<String, Set<TrustPermission>> kingdom = kingdomTrusts.get(kingdomName);
        if (kingdom == null) return new HashSet<>();
        return kingdom.getOrDefault(player, new HashSet<>());
    }

    public Map<String, Set<TrustPermission>> getAllTrusts(String kingdomName) {
        return kingdomTrusts.getOrDefault(kingdomName, new HashMap<>());
    }

    public boolean canBuild(Player player, String kingdomName) {
        String playerKingdom = plugin.getKingdomManager().getKingdomOfPlayer(player.getName());
        if (playerKingdom != null && playerKingdom.equals(kingdomName)) {
            return true; // Member
        }
        return hasTrust(kingdomName, player.getName(), TrustPermission.BUILD) ||
               hasTrust(kingdomName, player.getName(), TrustPermission.ALL);
    }

    public boolean canInteract(Player player, String kingdomName) {
        String playerKingdom = plugin.getKingdomManager().getKingdomOfPlayer(player.getName());
        if (playerKingdom != null && playerKingdom.equals(kingdomName)) {
            return true; // Member
        }
        return hasTrust(kingdomName, player.getName(), TrustPermission.INTERACT) ||
               hasTrust(kingdomName, player.getName(), TrustPermission.ALL);
    }

    public boolean canUse(Player player, String kingdomName) {
        String playerKingdom = plugin.getKingdomManager().getKingdomOfPlayer(player.getName());
        if (playerKingdom != null && playerKingdom.equals(kingdomName)) {
            return true; // Member
        }
        return hasTrust(kingdomName, player.getName(), TrustPermission.USE) ||
               hasTrust(kingdomName, player.getName(), TrustPermission.ALL);
    }

    public boolean canAccessContainer(Player player, String kingdomName) {
        String playerKingdom = plugin.getKingdomManager().getKingdomOfPlayer(player.getName());
        if (playerKingdom != null && playerKingdom.equals(kingdomName)) {
            return true; // Member
        }
        return hasTrust(kingdomName, player.getName(), TrustPermission.CONTAINER) ||
               hasTrust(kingdomName, player.getName(), TrustPermission.ALL);
    }

    public boolean canUseRedstone(Player player, String kingdomName) {
        String playerKingdom = plugin.getKingdomManager().getKingdomOfPlayer(player.getName());
        if (playerKingdom != null && playerKingdom.equals(kingdomName)) {
            return true; // Member
        }
        return hasTrust(kingdomName, player.getName(), TrustPermission.REDSTONE) ||
               hasTrust(kingdomName, player.getName(), TrustPermission.ALL);
    }
}

