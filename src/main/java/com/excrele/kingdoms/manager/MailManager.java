package com.excrele.kingdoms.manager;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.Kingdom;
import com.excrele.kingdoms.model.Mail;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Manages in-game mail system for kingdom members
 */
public class MailManager {
    private final KingdomsPlugin plugin;
    // recipient -> List of Mail
    private final Map<String, List<Mail>> inboxes;
    // sender -> List of Mail (sent mail)
    private final Map<String, List<Mail>> sentMail;
    
    public MailManager(KingdomsPlugin plugin) {
        this.plugin = plugin;
        this.inboxes = new ConcurrentHashMap<>();
        this.sentMail = new ConcurrentHashMap<>();
        loadAllMail();
    }
    
    /**
     * Load all mail from storage
     */
    private void loadAllMail() {
        // Load from storage adapters
        for (Kingdom kingdom : plugin.getKingdomManager().getKingdoms().values()) {
            // Load mail for all members including king
            for (String member : kingdom.getMembers()) {
                loadPlayerMail(member);
            }
            loadPlayerMail(kingdom.getKing());
        }
    }
    
    /**
     * Load mail for a specific player
     */
    private void loadPlayerMail(String playerName) {
        List<Map<String, Object>> mailData = plugin.getStorageManager().getAdapter().loadPlayerMail(playerName);
        if (mailData != null && !mailData.isEmpty()) {
            List<Mail> playerInbox = new ArrayList<>();
            for (Map<String, Object> data : mailData) {
                String mailId = (String) data.get("mailId");
                String recipient = (String) data.get("recipient");
                String sender = (String) data.get("sender");
                String kingdomName = (String) data.get("kingdomName");
                String subject = (String) data.get("subject");
                String message = (String) data.get("message");
                long sentAt = ((Number) data.getOrDefault("sentAt", System.currentTimeMillis() / 1000)).longValue();
                boolean read = (Boolean) data.getOrDefault("read", false);
                long readAt = ((Number) data.getOrDefault("readAt", 0)).longValue();
                boolean deleted = (Boolean) data.getOrDefault("deleted", false);
                
                Mail mail = new Mail(mailId, recipient, sender, kingdomName, subject, message, sentAt, read, readAt, deleted);
                if (!deleted) {
                    playerInbox.add(mail);
                }
            }
            if (!playerInbox.isEmpty()) {
                inboxes.put(playerName, playerInbox);
            }
        }
    }
    
    /**
     * Send mail to a kingdom member
     */
    public boolean sendMail(String sender, String recipient, String subject, String message) {
        // Validate sender is in a kingdom
        String senderKingdom = plugin.getKingdomManager().getKingdomOfPlayer(sender);
        if (senderKingdom == null) {
            return false; // Sender must be in a kingdom
        }
        
        // Validate recipient is in the same kingdom
        String recipientKingdom = plugin.getKingdomManager().getKingdomOfPlayer(recipient);
        if (recipientKingdom == null || !recipientKingdom.equals(senderKingdom)) {
            return false; // Recipient must be in the same kingdom
        }
        
        // Validate message length
        if (message.length() > 1000) {
            return false; // Message too long
        }
        
        if (subject.length() > 100) {
            return false; // Subject too long
        }
        
        // Create mail
        Mail mail = new Mail(recipient, sender, senderKingdom, subject, message);
        
        // Add to inbox
        inboxes.computeIfAbsent(recipient, k -> new ArrayList<>()).add(mail);
        
        // Add to sent mail
        sentMail.computeIfAbsent(sender, k -> new ArrayList<>()).add(mail);
        
        // Save to storage
        saveMail(mail);
        
        // Notify recipient if online
        Player recipientPlayer = plugin.getServer().getPlayer(recipient);
        if (recipientPlayer != null && recipientPlayer.isOnline()) {
            recipientPlayer.sendMessage("§6§l[Kingdom Mail] §r§eYou have received a new message from §e" + sender);
            recipientPlayer.sendMessage("§7Subject: §e" + subject);
            com.excrele.kingdoms.util.ActionBarManager.sendNotification(recipientPlayer, 
                "§6New mail from " + sender + "! Use §e/kingdom mail read §6to view");
        }
        
        return true;
    }
    
    /**
     * Send mail to all kingdom members (broadcast)
     */
    public boolean sendMailToKingdom(String sender, String kingdomName, String subject, String message) {
        Kingdom kingdom = plugin.getKingdomManager().getKingdom(kingdomName);
        if (kingdom == null) return false;
        
        // Check if sender is king or advisor
        if (!kingdom.getKing().equals(sender) && 
            !kingdom.hasPermission(sender, "invite")) {
            return false; // Only king/advisor can broadcast
        }
        
        int sentCount = 0;
        // Send to all members
        for (String member : kingdom.getMembers()) {
            if (!member.equals(sender)) {
                if (sendMail(sender, member, subject, message)) {
                    sentCount++;
                }
            }
        }
        
        // Also send to king if sender is not king
        if (!kingdom.getKing().equals(sender)) {
            if (sendMail(sender, kingdom.getKing(), subject, message)) {
                sentCount++;
            }
        }
        
        return sentCount > 0;
    }
    
    /**
     * Get unread mail count for a player
     */
    public int getUnreadCount(String playerName) {
        List<Mail> inbox = inboxes.getOrDefault(playerName, new ArrayList<>());
        return (int) inbox.stream().filter(m -> !m.isRead() && !m.isDeleted()).count();
    }
    
    /**
     * Get all mail for a player (excluding deleted)
     */
    public List<Mail> getPlayerMail(String playerName) {
        List<Mail> inbox = inboxes.getOrDefault(playerName, new ArrayList<>());
        return inbox.stream()
            .filter(m -> !m.isDeleted())
            .sorted((a, b) -> Long.compare(b.getSentAt(), a.getSentAt())) // Newest first
            .collect(Collectors.toList());
    }
    
    /**
     * Get unread mail for a player
     */
    public List<Mail> getUnreadMail(String playerName) {
        List<Mail> inbox = inboxes.getOrDefault(playerName, new ArrayList<>());
        return inbox.stream()
            .filter(m -> !m.isRead() && !m.isDeleted())
            .sorted((a, b) -> Long.compare(b.getSentAt(), a.getSentAt()))
            .collect(Collectors.toList());
    }
    
    /**
     * Get sent mail for a player
     */
    public List<Mail> getSentMail(String playerName) {
        List<Mail> sent = sentMail.getOrDefault(playerName, new ArrayList<>());
        return sent.stream()
            .filter(m -> !m.isDeleted())
            .sorted((a, b) -> Long.compare(b.getSentAt(), a.getSentAt()))
            .collect(Collectors.toList());
    }
    
    /**
     * Mark mail as read
     */
    public boolean markAsRead(String playerName, String mailId) {
        List<Mail> inbox = inboxes.get(playerName);
        if (inbox == null) return false;
        
        for (Mail mail : inbox) {
            if (mail.getMailId().equals(mailId) && mail.getRecipient().equals(playerName)) {
                mail.setRead(true);
                saveMail(mail);
                return true;
            }
        }
        return false;
    }
    
    /**
     * Mark all mail as read for a player
     */
    public int markAllAsRead(String playerName) {
        List<Mail> inbox = inboxes.get(playerName);
        if (inbox == null) return 0;
        
        int count = 0;
        for (Mail mail : inbox) {
            if (!mail.isRead() && !mail.isDeleted() && mail.getRecipient().equals(playerName)) {
                mail.setRead(true);
                count++;
            }
        }
        
        if (count > 0) {
            // Save all updated mail
            for (Mail mail : inbox) {
                if (mail.isRead() && mail.getReadAt() > 0) {
                    saveMail(mail);
                }
            }
        }
        
        return count;
    }
    
    /**
     * Delete mail (soft delete)
     */
    public boolean deleteMail(String playerName, String mailId) {
        List<Mail> inbox = inboxes.get(playerName);
        if (inbox == null) return false;
        
        for (Mail mail : inbox) {
            if (mail.getMailId().equals(mailId) && mail.getRecipient().equals(playerName)) {
                mail.setDeleted(true);
                saveMail(mail);
                // Remove from inbox list
                inbox.remove(mail);
                return true;
            }
        }
        return false;
    }
    
    /**
     * Delete all read mail for a player
     */
    public int deleteAllRead(String playerName) {
        List<Mail> inbox = inboxes.get(playerName);
        if (inbox == null) return 0;
        
        int count = 0;
        List<Mail> toRemove = new ArrayList<>();
        for (Mail mail : inbox) {
            if (mail.isRead() && !mail.isDeleted() && mail.getRecipient().equals(playerName)) {
                mail.setDeleted(true);
                saveMail(mail);
                toRemove.add(mail);
                count++;
            }
        }
        
        inbox.removeAll(toRemove);
        return count;
    }
    
    /**
     * Get a specific mail by ID
     */
    public Mail getMail(String playerName, String mailId) {
        List<Mail> inbox = inboxes.get(playerName);
        if (inbox == null) return null;
        
        return inbox.stream()
            .filter(m -> m.getMailId().equals(mailId) && !m.isDeleted())
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Deliver pending mail to a player when they log in
     */
    public void deliverPendingMail(Player player) {
        String playerName = player.getName();
        int unreadCount = getUnreadCount(playerName);
        
        if (unreadCount > 0) {
            player.sendMessage("§6§l[Kingdom Mail] §r§eYou have §e" + unreadCount + " §eunread message(s)!");
            player.sendMessage("§7Use §e/kingdom mail read §7or §e/kingdom mail inbox §7to view your mail");
            com.excrele.kingdoms.util.ActionBarManager.sendNotification(player, 
                "§6You have " + unreadCount + " unread mail! Use §e/kingdom mail §6to view");
        }
    }
    
    /**
     * Save mail to storage
     */
    private void saveMail(Mail mail) {
        plugin.getStorageManager().getAdapter().saveMail(
            mail.getMailId(),
            mail.getRecipient(),
            mail.getSender(),
            mail.getKingdomName(),
            mail.getSubject(),
            mail.getMessage(),
            mail.getSentAt(),
            mail.isRead(),
            mail.getReadAt(),
            mail.isDeleted()
        );
    }
    
    /**
     * Clean up old deleted mail (older than 30 days)
     */
    public void cleanupOldMail() {
        long cutoff = System.currentTimeMillis() / 1000 - (30 * 86400); // 30 days
        
        for (List<Mail> inbox : inboxes.values()) {
            inbox.removeIf(m -> m.isDeleted() && m.getSentAt() < cutoff);
        }
        
        for (List<Mail> sent : sentMail.values()) {
            sent.removeIf(m -> m.isDeleted() && m.getSentAt() < cutoff);
        }
    }
}

