package com.excrele.kingdoms.gui;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.Challenge;
import com.excrele.kingdoms.model.PlayerChallengeData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ChallengeGUI {
    public static void openChallengeGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, "Kingdom Challenges");
        int slot = 0;

        for (Challenge challenge : KingdomsPlugin.getInstance().getChallengeManager().getChallenges()) {
            if (slot >= 54) break; // Prevent overflow
            ItemStack item = createChallengeItem(player, challenge);
            gui.setItem(slot, item);
            slot++;
        }

        player.openInventory(gui);
    }

    private static ItemStack createChallengeItem(Player player, Challenge challenge) {
        String taskType = (String) challenge.getTask().get("type");
        String taskTarget = (String) challenge.getTask().getOrDefault("block", challenge.getTask().getOrDefault("entity", challenge.getTask().getOrDefault("item", "UNKNOWN")));
        Material material = getMaterialForChallenge(taskType, taskTarget);

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6" + challenge.getDescription());
            List<String> lore = new ArrayList<>();
            lore.add("§7Difficulty: §e" + challenge.getDifficulty());
            lore.add("§7XP Reward: §e" + challenge.getXpReward());
            PlayerChallengeData data = KingdomsPlugin.getInstance().getChallengeManager().getPlayerChallengeData(player, challenge);
            int progress = data != null ? data.getProgress() : 0;
            int required = (int) challenge.getTask().getOrDefault("amount", 1);
            lore.add("§7Progress: §e" + progress + "/" + required);
            if (KingdomsPlugin.getInstance().getChallengeManager().isChallengeOnCooldown(player, challenge)) {
                lore.add("§cOn Cooldown");
            } else {
                lore.add("§aAvailable");
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static Material getMaterialForChallenge(String taskType, String target) {
        try {
            switch (taskType) {
                case "block_break":
                    return Material.valueOf(target);
                case "entity_kill":
                    return Material.valueOf(target + "_SPAWN_EGG");
                case "craft_item":
                    return Material.valueOf(target);
                default:
                    return Material.BOOK;
            }
        } catch (IllegalArgumentException e) {
            return Material.BOOK; // Fallback for invalid materials
        }
    }
}