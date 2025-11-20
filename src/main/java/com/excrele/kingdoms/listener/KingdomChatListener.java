package com.excrele.kingdoms.listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.excrele.kingdoms.KingdomsPlugin;

public class KingdomChatListener implements Listener {
    private static final Map<UUID, Boolean> kingdomChatMode = new HashMap<>();

    public static void setKingdomChatMode(Player player, boolean enabled) {
        kingdomChatMode.put(player.getUniqueId(), enabled);
    }

    public static boolean isKingdomChatMode(Player player) {
        return kingdomChatMode.getOrDefault(player.getUniqueId(), false);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!isKingdomChatMode(player)) return;

        String kingdomName = KingdomsPlugin.getInstance().getKingdomManager().getKingdomOfPlayer(player.getName());
        if (kingdomName == null) {
            setKingdomChatMode(player, false);
            player.sendMessage("§cYou left your kingdom, kingdom chat disabled.");
            return;
        }

        // Cancel the original message
        event.setCancelled(true);

        // Send to kingdom members only
        String message = "§6[Kingdom] §e" + player.getName() + "§7: §f" + event.getMessage();
        
        // Send to king
        String kingName = KingdomsPlugin.getInstance().getKingdomManager().getKingdom(kingdomName).getKing();
        Player king = KingdomsPlugin.getInstance().getServer().getPlayer(kingName);
        if (king != null && king.isOnline()) {
            king.sendMessage(message);
        }

        // Send to all members
        for (String memberName : KingdomsPlugin.getInstance().getKingdomManager().getKingdom(kingdomName).getMembers()) {
            Player member = KingdomsPlugin.getInstance().getServer().getPlayer(memberName);
            if (member != null && member.isOnline() && !memberName.equals(kingName)) {
                member.sendMessage(message);
            }
        }
    }
}

