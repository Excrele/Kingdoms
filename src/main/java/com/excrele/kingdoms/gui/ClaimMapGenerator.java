package com.excrele.kingdoms.gui;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.Kingdom;

public class ClaimMapGenerator {
    public static String generateClaimMap(Player player) {
        String kingdomName = KingdomsPlugin.getInstance().getKingdomManager().getKingdomOfPlayer(player.getName());
        Chunk center = player.getLocation().getChunk();
        int radius = 5; // 11x11 grid (5 chunks each direction)
        StringBuilder map = new StringBuilder("§6Claim Map (X: §e" + center.getX() + "§6, Z: §e" + center.getZ() + "§6)\n");

        for (int z = center.getZ() - radius; z <= center.getZ() + radius; z++) {
            for (int x = center.getX() - radius; x <= center.getX() + radius; x++) {
                Chunk chunk = center.getWorld().getChunkAt(x, z);
                Kingdom kingdom = KingdomsPlugin.getInstance().getKingdomManager().getKingdomByChunk(chunk);
                if (x == center.getX() && z == center.getZ()) {
                    map.append("§b[P]"); // Player position
                } else if (kingdom != null) {
                    if (kingdom.getName().equals(kingdomName)) {
                        map.append("§a[K]"); // Own kingdom
                    } else {
                        Kingdom playerKingdom = KingdomsPlugin.getInstance().getKingdomManager().getKingdom(kingdomName);
                        if (playerKingdom != null && playerKingdom.isAllied(kingdom.getName())) {
                            map.append("§b[A]"); // Allied kingdom
                        } else {
                            map.append("§c[E]"); // Enemy kingdom
                        }
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