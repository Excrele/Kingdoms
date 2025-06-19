package com.excrele.kingdoms.manager;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.Kingdom;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

import java.util.Map;

public class FlagManager {
    private final KingdomsPlugin plugin;

    public FlagManager(KingdomsPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean canBuild(Player player, Chunk chunk) {
        Kingdom kingdom = plugin.getKingdomManager().getKingdomByChunk(chunk);
        if (kingdom == null) return true; // Unclaimed chunks are buildable
        Map<String, String> flags = kingdom.getPlotFlags();
        String buildFlag = flags.getOrDefault("build", "members");
        return switch (buildFlag) {
            case "king" -> kingdom.getKing().equals(player.getName());
            case "members" -> kingdom.getKing().equals(player.getName()) || kingdom.getMembers().contains(player.getName());
            default -> true;
        };
    }

    public boolean canBreak(Player player, Chunk chunk) {
        Kingdom kingdom = plugin.getKingdomManager().getKingdomByChunk(chunk);
        if (kingdom == null) return true;
        Map<String, String> flags = kingdom.getPlotFlags();
        String breakFlag = flags.getOrDefault("break", "members");
        return switch (breakFlag) {
            case "king" -> kingdom.getKing().equals(player.getName());
            case "members" -> kingdom.getKing().equals(player.getName()) || kingdom.getMembers().contains(player.getName());
            default -> true;
        };
    }

    public void setFlag(Player player, String flag, String value, Chunk chunk) {
        Kingdom kingdom = plugin.getKingdomManager().getKingdomByChunk(chunk);
        if (kingdom == null || !kingdom.getKing().equals(player.getName())) {
            player.sendMessage("You cannot set flags here!");
            return;
        }
        Map<String, String> flags = kingdom.getPlotFlags();
        flags.put(flag, value);
        plugin.getKingdomManager().saveKingdoms(plugin.getKingdomsConfig(), plugin.getKingdomsFile());
        player.sendMessage("Flag " + flag + " set to " + value + "!");
    }

    public void setPlotFlag(Player player, String flag, String value, Chunk chunk) {
        setFlag(player, flag, value, chunk); // Delegate to setFlag for consistency
    }
}