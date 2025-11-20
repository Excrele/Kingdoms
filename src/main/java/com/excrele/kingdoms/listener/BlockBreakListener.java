package com.excrele.kingdoms.listener;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.Challenge;
import com.excrele.kingdoms.model.PlayerChallengeData;

public class BlockBreakListener implements Listener {
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        List<Challenge> challenges = KingdomsPlugin.getInstance().getChallengeManager().getEventToChallenges().get("block_break");
        if (challenges == null) return;

        for (Challenge challenge : challenges) {
            String requiredBlock = (String) challenge.getTask().get("block");
            if (event.getBlock().getType().toString().equalsIgnoreCase(requiredBlock)) {
                KingdomsPlugin.getInstance().getChallengeManager().updateChallengeProgress(player, challenge, 1);
                PlayerChallengeData data = KingdomsPlugin.getInstance().getChallengeManager().getPlayerChallengeData(player, challenge);
                if (data != null && data.getProgress() > 0) {
                    player.sendMessage("Progress: " + data.getProgress() + "/" + challenge.getTask().get("amount") + " for " + challenge.getDescription());
                }
            }
        }
    }
}