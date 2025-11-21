package com.excrele.kingdoms.manager;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.Kingdom;
import com.excrele.kingdoms.model.MemberNote;
import com.excrele.kingdoms.model.MemberPermission;
import com.excrele.kingdoms.model.MemberTitle;
import com.excrele.kingdoms.util.ErrorHandler;
import com.excrele.kingdoms.util.SaveQueue;

public class AdvancedMemberManager {
    private final KingdomsPlugin plugin;
    private File memberDataFile;
    private FileConfiguration memberDataConfig;
    private final ErrorHandler errorHandler;
    private SaveQueue saveQueue;
    // kingdomName -> playerName -> MemberTitle
    private final Map<String, Map<String, MemberTitle>> memberTitles;
    // kingdomName -> playerName -> MemberPermission
    private final Map<String, Map<String, MemberPermission>> memberPermissions;
    // kingdomName -> playerName -> MemberNote
    private final Map<String, Map<String, MemberNote>> memberNotes;

    public AdvancedMemberManager(KingdomsPlugin plugin) {
        this.plugin = plugin;
        this.memberTitles = new HashMap<>();
        this.memberPermissions = new HashMap<>();
        this.memberNotes = new HashMap<>();
        this.errorHandler = new ErrorHandler(plugin);
        initializeDataFile();
        loadAllData();
    }
    
    /**
     * Set the save queue for async operations
     */
    public void setSaveQueue(SaveQueue saveQueue) {
        this.saveQueue = saveQueue;
    }

    private void initializeDataFile() {
        memberDataFile = new File(plugin.getDataFolder(), "member_data.yml");
        if (!memberDataFile.exists()) {
            try {
                memberDataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create member_data.yml!");
            }
        }
        memberDataConfig = YamlConfiguration.loadConfiguration(memberDataFile);
    }

    private void loadAllData() {
        // Load member titles
        if (memberDataConfig.contains("titles")) {
            org.bukkit.configuration.ConfigurationSection titlesSection = memberDataConfig.getConfigurationSection("titles");
            if (titlesSection != null) {
                for (String kingdomName : titlesSection.getKeys(false)) {
                    org.bukkit.configuration.ConfigurationSection kingdomSection = titlesSection.getConfigurationSection(kingdomName);
                    if (kingdomSection != null) {
                        Map<String, MemberTitle> kingdomTitles = new HashMap<>();
                        for (String playerName : kingdomSection.getKeys(false)) {
                            String title = memberDataConfig.getString("titles." + kingdomName + "." + playerName + ".title");
                            String color = memberDataConfig.getString("titles." + kingdomName + "." + playerName + ".color", "§7");
                            long setAt = memberDataConfig.getLong("titles." + kingdomName + "." + playerName + ".setAt", System.currentTimeMillis() / 1000);
                            String setBy = memberDataConfig.getString("titles." + kingdomName + "." + playerName + ".setBy", "Unknown");
                            if (title != null) {
                                kingdomTitles.put(playerName, new MemberTitle(title, color, setAt, setBy));
                            }
                        }
                        if (!kingdomTitles.isEmpty()) {
                            memberTitles.put(kingdomName, kingdomTitles);
                        }
                    }
                }
            }
        }

        // Load member permissions
        if (memberDataConfig.contains("permissions")) {
            org.bukkit.configuration.ConfigurationSection permsSection = memberDataConfig.getConfigurationSection("permissions");
            if (permsSection != null) {
                for (String kingdomName : permsSection.getKeys(false)) {
                    org.bukkit.configuration.ConfigurationSection kingdomSection = permsSection.getConfigurationSection(kingdomName);
                    if (kingdomSection != null) {
                        Map<String, MemberPermission> kingdomPerms = new HashMap<>();
                        for (String playerName : kingdomSection.getKeys(false)) {
                            Set<String> allowed = new HashSet<>(memberDataConfig.getStringList("permissions." + kingdomName + "." + playerName + ".allowed"));
                            Set<String> denied = new HashSet<>(memberDataConfig.getStringList("permissions." + kingdomName + "." + playerName + ".denied"));
                            long lastModified = memberDataConfig.getLong("permissions." + kingdomName + "." + playerName + ".lastModified", System.currentTimeMillis() / 1000);
                            String modifiedBy = memberDataConfig.getString("permissions." + kingdomName + "." + playerName + ".modifiedBy", "Unknown");
                            kingdomPerms.put(playerName, new MemberPermission(allowed, denied, lastModified, modifiedBy));
                        }
                        if (!kingdomPerms.isEmpty()) {
                            memberPermissions.put(kingdomName, kingdomPerms);
                        }
                    }
                }
            }
        }

        // Load member notes
        if (memberDataConfig.contains("notes")) {
            org.bukkit.configuration.ConfigurationSection notesSection = memberDataConfig.getConfigurationSection("notes");
            if (notesSection != null) {
                for (String kingdomName : notesSection.getKeys(false)) {
                    org.bukkit.configuration.ConfigurationSection kingdomSection = notesSection.getConfigurationSection(kingdomName);
                    if (kingdomSection != null) {
                        Map<String, MemberNote> kingdomNotes = new HashMap<>();
                        for (String playerName : kingdomSection.getKeys(false)) {
                            String note = memberDataConfig.getString("notes." + kingdomName + "." + playerName + ".note");
                            String author = memberDataConfig.getString("notes." + kingdomName + "." + playerName + ".author", "Unknown");
                            long createdAt = memberDataConfig.getLong("notes." + kingdomName + "." + playerName + ".createdAt", System.currentTimeMillis() / 1000);
                            long lastModified = memberDataConfig.getLong("notes." + kingdomName + "." + playerName + ".lastModified", System.currentTimeMillis() / 1000);
                            String modifiedBy = memberDataConfig.getString("notes." + kingdomName + "." + playerName + ".modifiedBy", author);
                            if (note != null) {
                                kingdomNotes.put(playerName, new MemberNote(note, author, createdAt, lastModified, modifiedBy));
                            }
                        }
                        if (!kingdomNotes.isEmpty()) {
                            memberNotes.put(kingdomName, kingdomNotes);
                        }
                    }
                }
            }
        }
    }

    public void saveAllData() {
        saveAllData(false);
    }
    
    /**
     * Save all data synchronously or asynchronously
     */
    public void saveAllData(boolean async) {
        if (async && saveQueue != null) {
            saveQueue.enqueue(this::performSave);
        } else {
            performSave();
        }
    }
    
    /**
     * Perform the actual save operation (can be called async)
     */
    private void performSave() {
        // Save member titles
        memberDataConfig.set("titles", null);
        for (Map.Entry<String, Map<String, MemberTitle>> kingdomEntry : memberTitles.entrySet()) {
            for (Map.Entry<String, MemberTitle> playerEntry : kingdomEntry.getValue().entrySet()) {
                String path = "titles." + kingdomEntry.getKey() + "." + playerEntry.getKey();
                MemberTitle title = playerEntry.getValue();
                memberDataConfig.set(path + ".title", title.getTitle());
                memberDataConfig.set(path + ".color", title.getColor());
                memberDataConfig.set(path + ".setAt", title.getSetAt());
                memberDataConfig.set(path + ".setBy", title.getSetBy());
            }
        }

        // Save member permissions
        memberDataConfig.set("permissions", null);
        for (Map.Entry<String, Map<String, MemberPermission>> kingdomEntry : memberPermissions.entrySet()) {
            for (Map.Entry<String, MemberPermission> playerEntry : kingdomEntry.getValue().entrySet()) {
                String path = "permissions." + kingdomEntry.getKey() + "." + playerEntry.getKey();
                MemberPermission perm = playerEntry.getValue();
                memberDataConfig.set(path + ".allowed", new java.util.ArrayList<>(perm.getAllowedPermissions()));
                memberDataConfig.set(path + ".denied", new java.util.ArrayList<>(perm.getDeniedPermissions()));
                memberDataConfig.set(path + ".lastModified", perm.getLastModified());
                memberDataConfig.set(path + ".modifiedBy", perm.getModifiedBy());
            }
        }

        // Save member notes
        memberDataConfig.set("notes", null);
        for (Map.Entry<String, Map<String, MemberNote>> kingdomEntry : memberNotes.entrySet()) {
            for (Map.Entry<String, MemberNote> playerEntry : kingdomEntry.getValue().entrySet()) {
                String path = "notes." + kingdomEntry.getKey() + "." + playerEntry.getKey();
                MemberNote note = playerEntry.getValue();
                memberDataConfig.set(path + ".note", note.getNote());
                memberDataConfig.set(path + ".author", note.getAuthor());
                memberDataConfig.set(path + ".createdAt", note.getCreatedAt());
                memberDataConfig.set(path + ".lastModified", note.getLastModified());
                memberDataConfig.set(path + ".modifiedBy", note.getModifiedBy());
            }
        }

        try {
            memberDataConfig.save(memberDataFile);
        } catch (IOException e) {
            errorHandler.handleSaveError("save member data", e, this::performSave);
        }
    }

    // Member Titles
    public boolean setMemberTitle(String kingdomName, String playerName, String title, String color, String setBy) {
        Kingdom kingdom = plugin.getKingdomManager().getKingdom(kingdomName);
        if (kingdom == null) return false;
        
        if (!kingdom.getMembers().contains(playerName) && !kingdom.getKing().equals(playerName)) {
            return false; // Player not in kingdom
        }

        if (title != null && title.length() > 30) {
            return false; // Title too long
        }

        memberTitles.computeIfAbsent(kingdomName, k -> new HashMap<>());
        if (title == null || title.isEmpty()) {
            memberTitles.get(kingdomName).remove(playerName);
        } else {
            memberTitles.get(kingdomName).put(playerName, new MemberTitle(title, color, setBy));
        }
        saveAllData(true); // Use async save
        return true;
    }

    public MemberTitle getMemberTitle(String kingdomName, String playerName) {
        Map<String, MemberTitle> titles = memberTitles.get(kingdomName);
        return titles != null ? titles.get(playerName) : null;
    }

    public boolean removeMemberTitle(String kingdomName, String playerName) {
        Map<String, MemberTitle> titles = memberTitles.get(kingdomName);
        if (titles == null) return false;
        boolean removed = titles.remove(playerName) != null;
        if (removed) {
            saveAllData();
        }
        return removed;
    }

    // Member Permissions
    public boolean setMemberPermission(String kingdomName, String playerName, String permission, boolean allow, String setBy) {
        Kingdom kingdom = plugin.getKingdomManager().getKingdom(kingdomName);
        if (kingdom == null) return false;
        
        if (!kingdom.getMembers().contains(playerName) && !kingdom.getKing().equals(playerName)) {
            return false; // Player not in kingdom
        }

        memberPermissions.computeIfAbsent(kingdomName, k -> new HashMap<>());
        memberPermissions.get(kingdomName).computeIfAbsent(playerName, k -> new MemberPermission());
        
        MemberPermission memberPerm = memberPermissions.get(kingdomName).get(playerName);
        if (allow) {
            memberPerm.allowPermission(permission);
        } else {
            memberPerm.denyPermission(permission);
        }
        memberPerm.setModifiedBy(setBy);
        saveAllData(true); // Use async save
        return true;
    }

    public boolean removeMemberPermission(String kingdomName, String playerName, String permission) {
        Map<String, MemberPermission> perms = memberPermissions.get(kingdomName);
        if (perms == null) return false;
        MemberPermission memberPerm = perms.get(playerName);
        if (memberPerm == null) return false;
        memberPerm.removePermission(permission);
        saveAllData(true); // Use async save
        return true;
    }

    public MemberPermission getMemberPermissions(String kingdomName, String playerName) {
        Map<String, MemberPermission> perms = memberPermissions.get(kingdomName);
        return perms != null ? perms.get(playerName) : null;
    }

    public boolean hasMemberPermission(String kingdomName, String playerName, String permission) {
        MemberPermission memberPerm = getMemberPermissions(kingdomName, playerName);
        if (memberPerm == null) return false;
        return memberPerm.hasPermission(permission);
    }

    public boolean hasMemberPermissionOverride(String kingdomName, String playerName, String permission) {
        MemberPermission memberPerm = getMemberPermissions(kingdomName, playerName);
        if (memberPerm == null) return false;
        return memberPerm.hasOverride(permission);
    }

    // Member Notes
    public boolean setMemberNote(String kingdomName, String playerName, String note, String author) {
        Kingdom kingdom = plugin.getKingdomManager().getKingdom(kingdomName);
        if (kingdom == null) return false;
        
        if (!kingdom.getMembers().contains(playerName) && !kingdom.getKing().equals(playerName)) {
            return false; // Player not in kingdom
        }

        if (note != null && note.length() > 500) {
            return false; // Note too long
        }

        memberNotes.computeIfAbsent(kingdomName, k -> new HashMap<>());
        if (note == null || note.isEmpty()) {
            memberNotes.get(kingdomName).remove(playerName);
        } else {
            MemberNote existingNote = memberNotes.get(kingdomName).get(playerName);
            if (existingNote != null) {
                existingNote.updateNote(note, author);
            } else {
                memberNotes.get(kingdomName).put(playerName, new MemberNote(note, author));
            }
        }
        saveAllData(true); // Use async save
        return true;
    }

    public MemberNote getMemberNote(String kingdomName, String playerName) {
        Map<String, MemberNote> notes = memberNotes.get(kingdomName);
        return notes != null ? notes.get(playerName) : null;
    }

    public boolean removeMemberNote(String kingdomName, String playerName) {
        Map<String, MemberNote> notes = memberNotes.get(kingdomName);
        if (notes == null) return false;
        boolean removed = notes.remove(playerName) != null;
        if (removed) {
            saveAllData();
        }
        return removed;
    }

    // Check permission with override support
    public boolean checkPermission(String kingdomName, String playerName, String permission) {
        // First check member permission override
        if (hasMemberPermissionOverride(kingdomName, playerName, permission)) {
            return hasMemberPermission(kingdomName, playerName, permission);
        }
        
        // Fall back to role-based permission
        Kingdom kingdom = plugin.getKingdomManager().getKingdom(kingdomName);
        if (kingdom == null) return false;
        return kingdom.hasPermission(playerName, permission);
    }

    // Getters for storage
    public Map<String, Map<String, MemberTitle>> getAllMemberTitles() {
        return memberTitles;
    }

    public Map<String, Map<String, MemberPermission>> getAllMemberPermissions() {
        return memberPermissions;
    }

    public Map<String, Map<String, MemberNote>> getAllMemberNotes() {
        return memberNotes;
    }

    // Loaders for storage
    public void loadMemberTitles(Map<String, Map<String, MemberTitle>> titles) {
        if (titles != null) {
            memberTitles.putAll(titles);
        }
    }

    public void loadMemberPermissions(Map<String, Map<String, MemberPermission>> permissions) {
        if (permissions != null) {
            memberPermissions.putAll(permissions);
        }
    }

    public void loadMemberNotes(Map<String, Map<String, MemberNote>> notes) {
        if (notes != null) {
            memberNotes.putAll(notes);
        }
    }
}

