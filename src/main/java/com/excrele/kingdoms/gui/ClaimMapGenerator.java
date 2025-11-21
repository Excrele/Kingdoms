package com.excrele.kingdoms.gui;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.Kingdom;

public class ClaimMapGenerator {
    public static String generateClaimMap(Player player) {
        org.bukkit.Location playerLoc = player.getLocation();
        if (playerLoc == null) return "§cUnable to generate map - invalid location!";
        
        KingdomsPlugin plugin = KingdomsPlugin.getInstance();
        if (plugin == null) return "§cPlugin not initialized!";
        
        String kingdomName = plugin.getKingdomManager().getKingdomOfPlayer(player.getName());
        Chunk center = playerLoc.getChunk();
        int radius = 5; // 11x11 grid (5 chunks each direction)
        StringBuilder map = new StringBuilder("§6Claim Map (X: §e" + center.getX() + "§6, Z: §e" + center.getZ() + "§6)\n");

        org.bukkit.World world = center.getWorld();
        
        Kingdom playerKingdom = kingdomName != null ? 
            plugin.getKingdomManager().getKingdom(kingdomName) : null;

        for (int z = center.getZ() - radius; z <= center.getZ() + radius; z++) {
            for (int x = center.getX() - radius; x <= center.getX() + radius; x++) {
                Chunk chunk = world.getChunkAt(x, z);
                Kingdom kingdom = plugin.getKingdomManager().getKingdomByChunk(chunk);
                if (x == center.getX() && z == center.getZ()) {
                    map.append("§b[P]"); // Player position
                } else if (kingdom != null) {
                    if (kingdomName != null && kingdom.getName().equals(kingdomName)) {
                        map.append("§a[K]"); // Own kingdom
                    } else if (playerKingdom != null && playerKingdom.isAllied(kingdom.getName())) {
                        map.append("§b[A]"); // Allied kingdom
                    } else {
                        map.append("§c[E]"); // Enemy kingdom
                    }
                } else {
                    map.append("§7[-]"); // Unclaimed
                }
            }
            map.append("\n");
        }
        map.append("§a[K] Your Kingdom, §b[A] Allied Kingdom, §c[E] Enemy Kingdom, §7[-] Unclaimed, §b[P] Your Position");
        return map.toString();
    }
}