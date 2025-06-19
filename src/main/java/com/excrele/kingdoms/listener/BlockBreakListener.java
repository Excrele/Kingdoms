package com.excrele.kingdoms.listener;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.Challenge;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.List;

public class BlockBreakListener implements Listener {
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        List<Challenge> challenges = KingdomsPlugin.getInstance().getChallengeManager().getEventToChallenges().get("block_break");
        if (challenges == null) return;

        for (Challenge challenge : challenges) {
            if (KingdomsPlugin.getInstance().getChallengeManager().isChallengeOnCooldown(player, challenge)) continue;
            String requiredBlock = (String) challenge.getTask().get("block");
            if (event.getBlock().getType().toString().equalsIgnoreCase(requiredBlock)) {
                int amount = (int) challenge.getTask().get("amount");
                // Simplified: complete on first match; add progress tracking for multi-block challenges
                KingdomsPlugin.getInstance().getChallengeManager().completeChallenge(player, challenge);
                player.sendMessage("Challenge completed: " + challenge.getDescription() + "! +" + challenge.getXpReward() + " XP");
            }
        }
    }
}