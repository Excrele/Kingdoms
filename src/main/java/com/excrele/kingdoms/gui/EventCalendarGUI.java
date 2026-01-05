package com.excrele.kingdoms.gui;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.Kingdom;
import com.excrele.kingdoms.model.KingdomEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * GUI for viewing kingdom events in a calendar format
 */
public class EventCalendarGUI {
    
    public static void openCalendar(Player player, int monthOffset) {
        String kingdomName = KingdomsPlugin.getInstance().getKingdomManager().getKingdomOfPlayer(player.getName());
        if (kingdomName == null) {
            player.sendMessage("§cYou must be in a kingdom to view the calendar!");
            return;
        }
        
        Kingdom kingdom = KingdomsPlugin.getInstance().getKingdomManager().getKingdom(kingdomName);
        if (kingdom == null) return;
        
        // Get events for the kingdom
        List<KingdomEvent> allEvents = KingdomsPlugin.getInstance().getCommunicationManager().getEvents(kingdomName);
        
        // Calculate calendar month
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, monthOffset);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        
        // Create calendar GUI
        Inventory gui = Bukkit.createInventory(null, 54, "Event Calendar - " + 
            getMonthName(month) + " " + year);
        
        // Get first day of month and days in month
        cal.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        
        // Calendar header (day names)
        String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < 7; i++) {
            ItemStack dayHeader = new ItemStack(Material.PAPER);
            ItemMeta meta = dayHeader.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§6" + dayNames[i]);
                dayHeader.setItemMeta(meta);
            }
            gui.setItem(i + 1, dayHeader);
        }
        
        // Fill calendar days
        int slot = 9; // Start after header row
        int day = 1;
        
        // Fill empty slots before first day
        for (int i = 1; i < firstDayOfWeek; i++) {
            slot++;
        }
        
        // Fill days of month
        while (day <= daysInMonth && slot < 45) {
            int col = slot % 9;
            
            if (col >= 1 && col <= 7) { // Valid day column
                ItemStack dayItem = createDayItem(day, month, year, allEvents);
                gui.setItem(slot, dayItem);
                day++;
            }
            slot++;
        }
        
        // Navigation buttons
        if (monthOffset > -12) { // Can go back 12 months
            ItemStack prevItem = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prevItem.getItemMeta();
            if (prevMeta != null) {
                prevMeta.setDisplayName("§6Previous Month");
                prevItem.setItemMeta(prevMeta);
            }
            gui.setItem(45, prevItem);
        }
        
        if (monthOffset < 12) { // Can go forward 12 months
            ItemStack nextItem = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = nextItem.getItemMeta();
            if (nextMeta != null) {
                nextMeta.setDisplayName("§6Next Month");
                nextMeta.setLore(List.of("§7" + getMonthName(month + 1) + " " + 
                    (month == 11 ? year + 1 : year)));
                nextItem.setItemMeta(nextMeta);
            }
            gui.setItem(53, nextItem);
        }
        
        // Current month button
        ItemStack currentItem = new ItemStack(Material.CLOCK);
        ItemMeta currentMeta = currentItem.getItemMeta();
        if (currentMeta != null) {
            currentMeta.setDisplayName("§6Go to Current Month");
            currentMeta.setLore(List.of("§7" + getMonthName(Calendar.getInstance().get(Calendar.MONTH)) + 
                " " + Calendar.getInstance().get(Calendar.YEAR)));
            currentItem.setItemMeta(currentMeta);
        }
        gui.setItem(49, currentItem);
        
        // Info item
        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName("§6§lCalendar Info");
            List<String> lore = new ArrayList<>();
            lore.add("§7Month: §e" + getMonthName(month) + " " + year);
            lore.add("§7Total Events: §e" + allEvents.size());
            long upcomingCount = allEvents.stream()
                .filter(e -> !e.isPast() && e.isActive())
                .count();
            lore.add("§7Upcoming: §e" + upcomingCount);
            lore.add("");
            lore.add("§7Click a day to view events");
            lore.add("§7Use §e/kingdom event create §7to add events");
            infoMeta.setLore(lore);
            infoItem.setItemMeta(infoMeta);
        }
        gui.setItem(48, infoItem);
        
        player.openInventory(gui);
    }
    
    private static ItemStack createDayItem(int day, int month, int year, List<KingdomEvent> events) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long dayStart = cal.getTimeInMillis() / 1000;
        long dayEnd = dayStart + 86400; // 24 hours
        
        // Find events on this day
        List<KingdomEvent> dayEvents = new ArrayList<>();
        for (KingdomEvent event : events) {
            if (event.isActive() && event.getScheduledTime() >= dayStart && event.getScheduledTime() < dayEnd) {
                dayEvents.add(event);
            }
        }
        
        Material material;
        String displayName;
        List<String> lore = new ArrayList<>();
        
        // Check if today
        Calendar today = Calendar.getInstance();
        boolean isToday = (day == today.get(Calendar.DAY_OF_MONTH) && 
                          month == today.get(Calendar.MONTH) && 
                          year == today.get(Calendar.YEAR));
        
        if (isToday) {
            material = Material.GOLD_BLOCK;
            displayName = "§6§lToday - " + day;
        } else if (dayEvents.isEmpty()) {
            material = Material.WHITE_STAINED_GLASS_PANE;
            displayName = "§7" + day;
        } else {
            material = Material.EMERALD_BLOCK;
            displayName = "§a" + day + " §7(" + dayEvents.size() + " event" + 
                (dayEvents.size() > 1 ? "s" : "") + ")";
        }
        
        lore.add("§7" + getDayOfWeekName(cal.get(Calendar.DAY_OF_WEEK)));
        
        if (!dayEvents.isEmpty()) {
            lore.add("");
            lore.add("§6Events:");
            for (KingdomEvent event : dayEvents) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                String timeStr = sdf.format(new Date(event.getScheduledTime() * 1000));
                lore.add("§e" + timeStr + " §7- §f" + event.getName());
            }
        } else {
            lore.add("§7No events");
        }
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private static String getMonthName(int month) {
        String[] months = {"January", "February", "March", "April", "May", "June",
                          "July", "August", "September", "October", "November", "December"};
        return months[month];
    }
    
    private static String getDayOfWeekName(int dayOfWeek) {
        String[] days = {"", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        return days[dayOfWeek];
    }
}

