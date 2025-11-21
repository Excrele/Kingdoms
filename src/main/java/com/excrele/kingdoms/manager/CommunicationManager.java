package com.excrele.kingdoms.manager;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.Kingdom;
import com.excrele.kingdoms.model.KingdomAnnouncement;
import com.excrele.kingdoms.model.KingdomEvent;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages kingdom communication (announcements, events, notifications)
 */
public class CommunicationManager {
    private final KingdomsPlugin plugin;
    private final Map<String, List<KingdomAnnouncement>> announcements; // kingdomName -> announcements
    private final Map<String, List<KingdomEvent>> events; // kingdomName -> events
    private final Map<String, Set<String>> notificationSettings; // player -> enabled notification types
    
    public CommunicationManager(KingdomsPlugin plugin) {
        this.plugin = plugin;
        this.announcements = new ConcurrentHashMap<>();
        this.events = new ConcurrentHashMap<>();
        this.notificationSettings = new ConcurrentHashMap<>();
        loadAllData();
    }
    
    /**
     * Load all communication data
     */
    private void loadAllData() {
        // Load from config/storage
        // This would be implemented in storage adapters
    }
    
    /**
     * Create a new announcement
     */
    public boolean createAnnouncement(Kingdom kingdom, String author, String message) {
        if (message.length() > 500) return false; // Limit length
        
        String id = UUID.randomUUID().toString();
        KingdomAnnouncement announcement = new KingdomAnnouncement(id, kingdom.getName(), author, message);
        
        announcements.computeIfAbsent(kingdom.getName(), k -> new ArrayList<>()).add(announcement);
        
        // Notify all online members
        broadcastAnnouncement(kingdom, announcement);
        saveAnnouncement(announcement);
        
        return true;
    }
    
    /**
     * Broadcast announcement to kingdom members
     */
    private void broadcastAnnouncement(Kingdom kingdom, KingdomAnnouncement announcement) {
        String message = "§6§l[Announcement] §r§e" + announcement.getMessage() + " §7- " + announcement.getAuthor();
        
        for (String member : kingdom.getMembers()) {
            Player player = plugin.getServer().getPlayer(member);
            if (player != null && player.isOnline() && shouldReceiveNotification(player, "announcements")) {
                player.sendMessage(message);
            }
        }
        
        Player king = plugin.getServer().getPlayer(kingdom.getKing());
        if (king != null && king.isOnline() && shouldReceiveNotification(king, "announcements")) {
            king.sendMessage(message);
        }
    }
    
    /**
     * Get active announcements for a kingdom
     */
    public List<KingdomAnnouncement> getAnnouncements(String kingdomName) {
        List<KingdomAnnouncement> active = new ArrayList<>();
        List<KingdomAnnouncement> all = announcements.getOrDefault(kingdomName, new ArrayList<>());
        for (KingdomAnnouncement ann : all) {
            if (ann.isActive()) {
                active.add(ann);
            }
        }
        // Sort by timestamp (newest first)
        active.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
        return active;
    }
    
    /**
     * Delete an announcement
     */
    public boolean deleteAnnouncement(Kingdom kingdom, String announcementId) {
        List<KingdomAnnouncement> anns = announcements.get(kingdom.getName());
        if (anns == null) return false;
        
        for (KingdomAnnouncement ann : anns) {
            if (ann.getId().equals(announcementId)) {
                ann.setActive(false);
                return true;
            }
        }
        return false;
    }
    
    /**
     * Create a new event
     */
    public boolean createEvent(Kingdom kingdom, String name, String description, org.bukkit.Location location, long scheduledTime) {
        if (name.length() > 50) return false;
        if (description.length() > 200) return false;
        
        String id = UUID.randomUUID().toString();
        KingdomEvent event = new KingdomEvent(id, kingdom.getName(), name, description, location, scheduledTime);
        
        events.computeIfAbsent(kingdom.getName(), k -> new ArrayList<>()).add(event);
        
        // Notify all online members
        broadcastEvent(kingdom, event);
        saveEvent(event);
        
        return true;
    }
    
    /**
     * Broadcast event to kingdom members
     */
    private void broadcastEvent(Kingdom kingdom, KingdomEvent event) {
        String message = "§6§l[Event] §r§e" + event.getName() + " §7- " + 
            formatTimeUntil(event.getTimeUntil());
        
        for (String member : kingdom.getMembers()) {
            Player player = plugin.getServer().getPlayer(member);
            if (player != null && player.isOnline() && shouldReceiveNotification(player, "events")) {
                player.sendMessage(message);
            }
        }
        
        Player king = plugin.getServer().getPlayer(kingdom.getKing());
        if (king != null && king.isOnline() && shouldReceiveNotification(king, "events")) {
            king.sendMessage(message);
        }
    }
    
    /**
     * Get active events for a kingdom
     */
    public List<KingdomEvent> getEvents(String kingdomName) {
        List<KingdomEvent> active = new ArrayList<>();
        List<KingdomEvent> all = events.getOrDefault(kingdomName, new ArrayList<>());
        for (KingdomEvent event : all) {
            if (event.isActive() && !event.isPast()) {
                active.add(event);
            }
        }
        // Sort by scheduled time
        active.sort(Comparator.comparingLong(KingdomEvent::getScheduledTime));
        return active;
    }
    
    /**
     * Delete an event
     */
    public boolean deleteEvent(Kingdom kingdom, String eventId) {
        List<KingdomEvent> evts = events.get(kingdom.getName());
        if (evts == null) return false;
        
        for (KingdomEvent event : evts) {
            if (event.getId().equals(eventId)) {
                event.setActive(false);
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check if player should receive notification
     */
    private boolean shouldReceiveNotification(Player player, String notificationType) {
        Set<String> enabled = notificationSettings.getOrDefault(player.getName(), new HashSet<>());
        // Default to enabled if not set
        return enabled.isEmpty() || enabled.contains(notificationType);
    }
    
    /**
     * Toggle notification setting for player
     */
    public void toggleNotification(Player player, String notificationType) {
        Set<String> enabled = notificationSettings.computeIfAbsent(player.getName(), k -> new HashSet<>());
        if (enabled.contains(notificationType)) {
            enabled.remove(notificationType);
        } else {
            enabled.add(notificationType);
        }
    }
    
    /**
     * Format time until event
     */
    private String formatTimeUntil(long seconds) {
        if (seconds < 0) return "now";
        if (seconds < 60) return "in " + seconds + " seconds";
        if (seconds < 3600) return "in " + (seconds / 60) + " minutes";
        if (seconds < 86400) return "in " + (seconds / 3600) + " hours";
        return "in " + (seconds / 86400) + " days";
    }
    
    /**
     * Check and notify about upcoming events
     */
    public void checkUpcomingEvents() {
        for (Map.Entry<String, List<KingdomEvent>> entry : events.entrySet()) {
            Kingdom kingdom = plugin.getKingdomManager().getKingdom(entry.getKey());
            if (kingdom == null) continue;
            
            for (KingdomEvent event : entry.getValue()) {
                if (!event.isActive() || event.isPast()) continue;
                
                long timeUntil = event.getTimeUntil();
                // Notify 1 hour before, 15 minutes before, and 1 minute before
                if (timeUntil == 3600 || timeUntil == 900 || timeUntil == 60) {
                    String message = "§6§l[Event Reminder] §r§e" + event.getName() + 
                        " §7starts " + formatTimeUntil(timeUntil);
                    
                    for (String member : kingdom.getMembers()) {
                        Player player = plugin.getServer().getPlayer(member);
                        if (player != null && player.isOnline() && shouldReceiveNotification(player, "events")) {
                            player.sendMessage(message);
                        }
                    }
                    
                    Player king = plugin.getServer().getPlayer(kingdom.getKing());
                    if (king != null && king.isOnline() && shouldReceiveNotification(king, "events")) {
                        king.sendMessage(message);
                    }
                }
            }
        }
    }
    
    /**
     * Save announcement to storage
     */
    private void saveAnnouncement(KingdomAnnouncement announcement) {
        // Save to storage adapter
        // This would be implemented in storage adapters
    }
    
    /**
     * Save event to storage
     */
    private void saveEvent(KingdomEvent event) {
        // Save to storage adapter
        // This would be implemented in storage adapters
    }
}

