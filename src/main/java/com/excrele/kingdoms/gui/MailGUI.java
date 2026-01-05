package com.excrele.kingdoms.gui;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.Mail;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI for viewing and managing kingdom mail
 */
public class MailGUI {
    
    public static void openInbox(Player player, int page) {
        KingdomsPlugin plugin = KingdomsPlugin.getInstance();
        List<Mail> allMail = plugin.getMailManager().getPlayerMail(player.getName());
        
        if (allMail.isEmpty()) {
            player.sendMessage("§7You have no mail!");
            return;
        }
        
        int itemsPerPage = 45; // 5 rows
        int totalPages = (int) Math.ceil((double) allMail.size() / itemsPerPage);
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;
        
        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, allMail.size());
        List<Mail> pageMail = allMail.subList(startIndex, endIndex);
        
        Inventory gui = Bukkit.createInventory(null, 54, "Kingdom Mail - Page " + page + "/" + totalPages);
        
        // Store mail ID in item custom data for easier retrieval
        // Add mail items
        int slot = 0;
        for (Mail mail : pageMail) {
            ItemStack mailItem = createMailItem(mail);
            // Store mail ID in item display name or use persistent data container
            org.bukkit.inventory.meta.ItemMeta meta = mailItem.getItemMeta();
            if (meta != null) {
                java.util.List<String> lore = meta.getLore();
                if (lore == null) lore = new ArrayList<>();
                lore.add("§7§k" + mail.getMailId()); // Hidden mail ID for lookup
                meta.setLore(lore);
                mailItem.setItemMeta(meta);
            }
            gui.setItem(slot, mailItem);
            slot++;
        }
        
        // Navigation buttons
        if (page > 1) {
            ItemStack prevItem = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prevItem.getItemMeta();
            if (prevMeta != null) {
                prevMeta.setDisplayName("§6Previous Page");
                prevItem.setItemMeta(prevMeta);
            }
            gui.setItem(45, prevItem);
        }
        
        if (page < totalPages) {
            ItemStack nextItem = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = nextItem.getItemMeta();
            if (nextMeta != null) {
                nextMeta.setDisplayName("§6Next Page");
                nextMeta.setLore(List.of("§7Page " + (page + 1) + " of " + totalPages));
                nextItem.setItemMeta(nextMeta);
            }
            gui.setItem(53, nextItem);
        }
        
        // Info item
        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName("§6§lMail Info");
            List<String> lore = new ArrayList<>();
            int unreadCount = plugin.getMailManager().getUnreadCount(player.getName());
            lore.add("§7Total Mail: §e" + allMail.size());
            lore.add("§7Unread: §e" + unreadCount);
            lore.add("§7Page: §e" + page + "§7/§e" + totalPages);
            lore.add("");
            lore.add("§7Click a mail to read it");
            lore.add("§7Use §e/kingdom mail delete <id> §7to delete");
            infoMeta.setLore(lore);
            infoItem.setItemMeta(infoMeta);
        }
        gui.setItem(49, infoItem);
        
        player.openInventory(gui);
    }
    
    public static void openMailView(Player player, Mail mail) {
        Inventory gui = Bukkit.createInventory(null, 27, "Mail: " + mail.getSubject());
        
        // Mail content
        ItemStack mailItem = new ItemStack(Material.WRITTEN_BOOK);
        ItemMeta mailMeta = mailItem.getItemMeta();
        if (mailMeta != null) {
            mailMeta.setDisplayName("§6§l" + mail.getSubject());
            List<String> lore = new ArrayList<>();
            lore.add("§7From: §e" + mail.getSender());
            lore.add("§7Kingdom: §e" + mail.getKingdomName());
            lore.add("§7Sent: §e" + mail.getFormattedTimeSince());
            lore.add("§7Date: §e" + mail.getFormattedDate());
            lore.add("");
            lore.add("§7Message:");
            // Split long messages into multiple lines
            String[] messageLines = splitMessage(mail.getMessage(), 40);
            for (String line : messageLines) {
                lore.add("§f" + line);
            }
            lore.add("");
            if (mail.isRead()) {
                lore.add("§a✓ Read");
            } else {
                lore.add("§7Click to mark as read");
            }
            mailMeta.setLore(lore);
            mailItem.setItemMeta(mailMeta);
        }
        gui.setItem(13, mailItem);
        
        // Action buttons
        if (!mail.isRead()) {
            ItemStack markReadItem = new ItemStack(Material.GREEN_DYE);
            ItemMeta markMeta = markReadItem.getItemMeta();
            if (markMeta != null) {
                markMeta.setDisplayName("§aMark as Read");
                markReadItem.setItemMeta(markMeta);
            }
            gui.setItem(10, markReadItem);
        }
        
        ItemStack deleteItem = new ItemStack(Material.RED_DYE);
        ItemMeta deleteMeta = deleteItem.getItemMeta();
        if (deleteMeta != null) {
            deleteMeta.setDisplayName("§cDelete Mail");
            deleteMeta.setLore(List.of("§7Permanently delete this mail"));
            deleteItem.setItemMeta(deleteMeta);
        }
        gui.setItem(16, deleteItem);
        
        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName("§6Back to Inbox");
            backItem.setItemMeta(backMeta);
        }
        gui.setItem(22, backItem);
        
        player.openInventory(gui);
    }
    
    private static ItemStack createMailItem(Mail mail) {
        Material material = mail.isRead() ? Material.WRITTEN_BOOK : Material.BOOK;
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String prefix = mail.isRead() ? "§7" : "§6§l[NEW] §r";
            meta.setDisplayName(prefix + mail.getSubject());
            List<String> lore = new ArrayList<>();
            lore.add("§7From: §e" + mail.getSender());
            lore.add("§7Sent: §e" + mail.getFormattedTimeSince());
            if (mail.getMessage().length() > 50) {
                lore.add("§7" + mail.getMessage().substring(0, 47) + "...");
            } else {
                lore.add("§7" + mail.getMessage());
            }
            lore.add("");
            if (mail.isRead()) {
                lore.add("§a✓ Read");
            } else {
                lore.add("§eClick to read");
            }
            lore.add("§7ID: §e" + mail.getMailId().substring(0, 8) + "...");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private static String[] splitMessage(String message, int maxLength) {
        List<String> lines = new ArrayList<>();
        String[] words = message.split(" ");
        StringBuilder currentLine = new StringBuilder();
        
        for (String word : words) {
            if (currentLine.length() + word.length() + 1 <= maxLength) {
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            } else {
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                }
                currentLine = new StringBuilder(word);
            }
        }
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }
        
        return lines.toArray(new String[0]);
    }
}

