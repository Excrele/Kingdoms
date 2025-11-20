package com.excrele.kingdoms.gui;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.Kingdom;

public class KingdomManagementGUI {
    public static void openKingdomGUI(Player player) {
        String kingdomName = KingdomsPlugin.getInstance().getKingdomManager().getKingdomOfPlayer(player.getName());
        if (kingdomName == null) {
            player.sendMessage("You are not in a kingdom!");
            return;
        }
        Kingdom kingdom = KingdomsPlugin.getInstance().getKingdomManager().getKingdom(kingdomName);
        Inventory gui = Bukkit.createInventory(null, 27, "Kingdom: " + kingdomName);

        // Members
        ItemStack membersItem = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta membersMeta = membersItem.getItemMeta();
        if (membersMeta != null) {
            membersMeta.setDisplayName("§6Members");
            List<String> membersLore = new ArrayList<>();
            membersLore.add("§7King: §e" + kingdom.getKing() + " §7(§6" + kingdom.getRole(kingdom.getKing()).getDisplayName() + "§7)");
            for (String member : kingdom.getMembers()) {
                com.excrele.kingdoms.model.MemberRole role = kingdom.getRole(member);
                membersLore.add("§7- §e" + member + " §7(§6" + role.getDisplayName() + "§7)");
            }
            membersLore.add("§aClick to invite");
            membersMeta.setLore(membersLore);
            membersItem.setItemMeta(membersMeta);
        }
        gui.setItem(10, membersItem);

        // Claims
        ItemStack claimsItem = new ItemStack(Material.GRASS_BLOCK);
        ItemMeta claimsMeta = claimsItem.getItemMeta();
        if (claimsMeta != null) {
            claimsMeta.setDisplayName("§6Claims");
            List<String> claimsLore = new ArrayList<>();
            claimsLore.add("§7Current: §e" + kingdom.getCurrentClaimChunks());
            claimsLore.add("§7Max: §e" + kingdom.getMaxClaimChunks());
            claimsLore.add("§aClick to unclaim (King only)");
            claimsMeta.setLore(claimsLore);
            claimsItem.setItemMeta(claimsMeta);
        }
        gui.setItem(12, claimsItem);

        // Flags
        ItemStack flagsItem = new ItemStack(Material.WHITE_BANNER);
        ItemMeta flagsMeta = flagsItem.getItemMeta();
        if (flagsMeta != null) {
            flagsMeta.setDisplayName("§6Flags");
            List<String> flagsLore = new ArrayList<>();
            kingdom.getFlags().forEach((k, v) -> flagsLore.add("§7" + k + ": §e" + v));
            flagsLore.add("§aClick to set flag (King only)");
            flagsMeta.setLore(flagsLore);
            flagsItem.setItemMeta(flagsMeta);
        }
        gui.setItem(14, flagsItem);

        // XP and Level
        ItemStack xpItem = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta xpMeta = xpItem.getItemMeta();
        if (xpMeta != null) {
            xpMeta.setDisplayName("§6Kingdom Status");
            List<String> xpLore = new ArrayList<>();
            xpLore.add("§7Level: §e" + kingdom.getLevel());
            xpLore.add("§7XP: §e" + kingdom.getXp() + "/" + (kingdom.getLevel() * kingdom.getLevel() * 1000));
            xpLore.add("§7Challenges: §e" + kingdom.getTotalChallengesCompleted());
            xpMeta.setLore(xpLore);
            xpItem.setItemMeta(xpMeta);
        }
        gui.setItem(16, xpItem);

        // Contributions
        ItemStack contribItem = new ItemStack(Material.GOLD_INGOT);
        ItemMeta contribMeta = contribItem.getItemMeta();
        if (contribMeta != null) {
            contribMeta.setDisplayName("§6Contributions");
            List<String> contribLore = new ArrayList<>();
            int playerContrib = kingdom.getContribution(player.getName());
            contribLore.add("§7Your Contribution: §e" + playerContrib + " XP");
            int totalContrib = kingdom.getMemberContributions().values().stream().mapToInt(Integer::intValue).sum();
            contribLore.add("§7Total: §e" + totalContrib + " XP");
            contribLore.add("§aClick to view all contributions");
            contribMeta.setLore(contribLore);
            contribItem.setItemMeta(contribMeta);
        }
        gui.setItem(22, contribItem);

        player.openInventory(gui);
    }
}