package com.excrele.kingdoms.listener;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.Challenge;

public class BlockBreakListener implements Listener {
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        KingdomsPlugin plugin = KingdomsPlugin.getInstance();
        
        // Record block interaction for statistics
        if (plugin.getStatisticsManager() != null) {
            org.bukkit.Chunk chunk = event.getBlock().getChunk();
            plugin.getStatisticsManager().recordBlockInteraction(chunk);
        }
        
        List<Challenge> challenges = plugin.getChallengeManager().getEventToChallenges().get("block_break");
        if (challenges == null) return;

        for (Challenge challenge : challenges) {
            String requiredBlock = (String) challenge.getTask().get("block");
            if (event.getBlock().getType().toString().equalsIgnoreCase(requiredBlock)) {
                plugin.getChallengeManager().updateChallengeProgress(player, challenge, 1);
                // Progress message is already sent by ChallengeManager.updateChallengeProgress
            }
        }
    }
}