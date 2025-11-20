package com.excrele.kingdoms.task;

import java.util.Map;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.Kingdom;

public class PerkTask extends BukkitRunnable {
    @Override
    public void run() {
        for (Player player : KingdomsPlugin.getInstance().getServer().getOnlinePlayers()) {
            String kingdomName = KingdomsPlugin.getInstance().getKingdomManager().getKingdomOfPlayer(player.getName());
            if (kingdomName == null) continue;
            Kingdom kingdom = KingdomsPlugin.getInstance().getKingdomManager().getKingdom(kingdomName);
            if (kingdom == null) continue;

            // Check if player is in a claimed chunk
            Chunk currentChunk = player.getLocation().getChunk();
            if (KingdomsPlugin.getInstance().getKingdomManager().getKingdomByChunk(currentChunk) != kingdom) {
                continue; // Player not in their kingdom's claimed territory
            }

            int level = kingdom.getLevel();
            if (level <= 0) continue;

            // Speed Perk: 5% speed per level (amplifier 0 = Speed I, 1 = Speed II, etc.)
            int speedAmplifier = Math.max(0, (level - 1) / 4); // More reasonable speed progression
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 120, speedAmplifier, false, false));

            // Regen Perk (only in non-PvP areas)
            Map<String, String> chunkFlags = kingdom.getPlotFlags(currentChunk);
            String pvpFlag = chunkFlags.getOrDefault("pvp", "false");
            if (!pvpFlag.equalsIgnoreCase("true")) {
                double healthToAdd = level * 0.5; // 0.5 hearts per level
                double maxHealth = player.getHealthScale();
                double newHealth = Math.min(player.getHealth() + healthToAdd, maxHealth);
                player.setHealth(newHealth);
            }

            // Level 5+: Jump Boost
            if (level >= 5) {
                int jumpAmplifier = Math.min(1, (level - 5) / 5); // Jump Boost I at level 5, II at level 10
                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 120, jumpAmplifier, false, false));
            }

            // Level 7+: Mining Speed (Haste)
            if (level >= 7) {
                int hasteAmplifier = Math.min(1, (level - 7) / 3); // Haste I at level 7, II at level 10
                player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 120, hasteAmplifier, false, false));
            }

            // Level 10+: Night Vision
            if (level >= 10) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 400, 0, false, false));
            }

            // Level 15+: Water Breathing
            if (level >= 15) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 400, 0, false, false));
            }

            // Level 20+: Fire Resistance
            if (level >= 20) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 400, 0, false, false));
            }
        }
    }
}