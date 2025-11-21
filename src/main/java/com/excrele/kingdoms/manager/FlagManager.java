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
        Map<String, String> flags = kingdom.getPlotFlags(chunk);
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
        Map<String, String> flags = kingdom.getPlotFlags(chunk);
        String breakFlag = flags.getOrDefault("break", "members");
        return switch (breakFlag) {
            case "king" -> kingdom.getKing().equals(player.getName());
            case "members" -> kingdom.getKing().equals(player.getName()) || kingdom.getMembers().contains(player.getName());
            default -> true;
        };
    }

    public void setFlag(Player player, String flag, String value, Chunk chunk) {
        Kingdom kingdom = plugin.getKingdomManager().getKingdomByChunk(chunk);
        if (kingdom == null) {
            player.sendMessage("You cannot set flags here!");
            return;
        }
        // Check if player has permission to set plot flags (already checked in command, but double-check here)
        if (!kingdom.hasPermission(player.getName(), "setplotflags")) {
            player.sendMessage("You don't have permission to set plot flags!");
            return;
        }
        Map<String, String> flags = kingdom.getPlotFlags(chunk);
        flags.put(flag, value);
        plugin.getKingdomManager().saveKingdoms(plugin.getKingdomsConfig(), plugin.getKingdomsFile());
        player.sendMessage("Flag " + flag + " set to " + value + " for chunk!");
    }

    public void setPlotFlag(Player player, String flag, String value, Chunk chunk) {
        setFlag(player, flag, value, chunk); // Delegate to setFlag
    }

    public boolean canUseRedstone(Player player, Chunk chunk) {
        Kingdom kingdom = plugin.getKingdomManager().getKingdomByChunk(chunk);
        if (kingdom == null) return true;
        Map<String, String> flags = kingdom.getPlotFlags(chunk);
        String redstoneFlag = flags.getOrDefault("redstone", "members");
        if (redstoneFlag.equalsIgnoreCase("true")) return true;
        if (redstoneFlag.equalsIgnoreCase("false")) return false;
        // Check if player is member or trusted
        String playerKingdom = plugin.getKingdomManager().getKingdomOfPlayer(player.getName());
        if (playerKingdom != null && playerKingdom.equals(kingdom.getName())) {
            return true; // Member
        }
        return plugin.getTrustManager().canUseRedstone(player, kingdom.getName());
    }

    public boolean canUsePiston(Player player, Chunk chunk) {
        Kingdom kingdom = plugin.getKingdomManager().getKingdomByChunk(chunk);
        if (kingdom == null) return true;
        Map<String, String> flags = kingdom.getPlotFlags(chunk);
        String pistonFlag = flags.getOrDefault("piston", "members");
        if (pistonFlag.equalsIgnoreCase("true")) return true;
        if (pistonFlag.equalsIgnoreCase("false")) return false;
        // Check if player is member or trusted
        String playerKingdom = plugin.getKingdomManager().getKingdomOfPlayer(player.getName());
        if (playerKingdom != null && playerKingdom.equals(kingdom.getName())) {
            return true; // Member
        }
        return plugin.getTrustManager().hasTrust(kingdom.getName(), player.getName(), 
            com.excrele.kingdoms.model.TrustPermission.PISTON) ||
            plugin.getTrustManager().hasTrust(kingdom.getName(), player.getName(), 
            com.excrele.kingdoms.model.TrustPermission.ALL);
    }

    public boolean canBreedAnimals(Player player, Chunk chunk) {
        Kingdom kingdom = plugin.getKingdomManager().getKingdomByChunk(chunk);
        if (kingdom == null) return true;
        Map<String, String> flags = kingdom.getPlotFlags(chunk);
        String breedFlag = flags.getOrDefault("animal-breed", "members");
        if (breedFlag.equalsIgnoreCase("true")) return true;
        if (breedFlag.equalsIgnoreCase("false")) return false;
        // Check if player is member or trusted
        String playerKingdom = plugin.getKingdomManager().getKingdomOfPlayer(player.getName());
        if (playerKingdom != null && playerKingdom.equals(kingdom.getName())) {
            return true; // Member
        }
        return plugin.getTrustManager().hasTrust(kingdom.getName(), player.getName(), 
            com.excrele.kingdoms.model.TrustPermission.ANIMAL_BREED) ||
            plugin.getTrustManager().hasTrust(kingdom.getName(), player.getName(), 
            com.excrele.kingdoms.model.TrustPermission.ALL);
    }

    public boolean canTrampleCrops(Player player, Chunk chunk) {
        Kingdom kingdom = plugin.getKingdomManager().getKingdomByChunk(chunk);
        if (kingdom == null) return true;
        Map<String, String> flags = kingdom.getPlotFlags(chunk);
        String trampleFlag = flags.getOrDefault("crop-trample", "true");
        if (trampleFlag.equalsIgnoreCase("true")) return true;
        if (trampleFlag.equalsIgnoreCase("false")) return false;
        // Check if player is member or trusted
        String playerKingdom = plugin.getKingdomManager().getKingdomOfPlayer(player.getName());
        if (playerKingdom != null && playerKingdom.equals(kingdom.getName())) {
            return true; // Member
        }
        return plugin.getTrustManager().hasTrust(kingdom.getName(), player.getName(), 
            com.excrele.kingdoms.model.TrustPermission.CROP_TRAMPLE) ||
            plugin.getTrustManager().hasTrust(kingdom.getName(), player.getName(), 
            com.excrele.kingdoms.model.TrustPermission.ALL);
    }

    public boolean isExplosionAllowed(Chunk chunk) {
        Kingdom kingdom = plugin.getKingdomManager().getKingdomByChunk(chunk);
        if (kingdom == null) return true;
        Map<String, String> flags = kingdom.getPlotFlags(chunk);
        String explosionFlag = flags.getOrDefault("explosion", "false");
        return explosionFlag.equalsIgnoreCase("true");
    }

    public boolean isFireSpreadAllowed(Chunk chunk) {
        Kingdom kingdom = plugin.getKingdomManager().getKingdomByChunk(chunk);
        if (kingdom == null) return true;
        Map<String, String> flags = kingdom.getPlotFlags(chunk);
        String fireFlag = flags.getOrDefault("fire-spread", "false");
        return fireFlag.equalsIgnoreCase("true");
    }

    public boolean isMobSpawningAllowed(Chunk chunk) {
        Kingdom kingdom = plugin.getKingdomManager().getKingdomByChunk(chunk);
        if (kingdom == null) return true;
        Map<String, String> flags = kingdom.getPlotFlags(chunk);
        String mobSpawnFlag = flags.getOrDefault("mob-spawning", "true");
        return mobSpawnFlag.equalsIgnoreCase("true");
    }

    public boolean isMobGriefAllowed(Chunk chunk) {
        Kingdom kingdom = plugin.getKingdomManager().getKingdomByChunk(chunk);
        if (kingdom == null) return true;
        Map<String, String> flags = kingdom.getPlotFlags(chunk);
        String mobGriefFlag = flags.getOrDefault("mob-grief", "false");
        return mobGriefFlag.equalsIgnoreCase("true");
    }
}