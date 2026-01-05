package com.excrele.kingdoms.storage;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.Kingdom;
import org.bukkit.Chunk;

import java.io.File;
import java.sql.*;
import java.util.*;

/**
 * SQLite storage adapter (optional - requires SQLite JDBC)
 */
public class SQLiteStorageAdapter implements StorageAdapter {
    private final KingdomsPlugin plugin;
    private Connection connection;
    private File databaseFile;

    public SQLiteStorageAdapter(KingdomsPlugin plugin) {
        this.plugin = plugin;
        this.databaseFile = new File(plugin.getDataFolder(), "kingdoms.db");
    }

    @Override
    public void initialize() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
            createTables();
            plugin.getLogger().info("SQLite connection established!");
        } catch (ClassNotFoundException e) {
            plugin.getLogger().severe("SQLite driver not found! Falling back to YAML.");
            throw new RuntimeException("SQLite driver not available", e);
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to connect to SQLite: " + e.getMessage());
            plugin.getLogger().severe("Falling back to YAML storage.");
            throw new RuntimeException("SQLite connection failed", e);
        }
    }

    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Kingdoms table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS kingdoms (" +
                "name TEXT PRIMARY KEY, " +
                "king TEXT, " +
                "xp INTEGER DEFAULT 0, " +
                "level INTEGER DEFAULT 1, " +
                "current_claim_chunks INTEGER DEFAULT 0, " +
                "created_at INTEGER, " +
                "total_challenges_completed INTEGER DEFAULT 0" +
                ")");
            
            // Members table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS kingdom_members (" +
                "kingdom_name TEXT, " +
                "player TEXT, " +
                "role TEXT, " +
                "contribution INTEGER DEFAULT 0, " +
                "PRIMARY KEY (kingdom_name, player)" +
                ")");
            
            // Alliances table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS kingdom_alliances (" +
                "kingdom1 TEXT, " +
                "kingdom2 TEXT, " +
                "PRIMARY KEY (kingdom1, kingdom2)" +
                ")");
            
            // Claims table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS claims (" +
                "chunk_key TEXT PRIMARY KEY, " +
                "kingdom_name TEXT, " +
                "claim_id TEXT, " +
                "plot_type TEXT" +
                ")");
            
            // Chunk flags table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS chunk_flags (" +
                "chunk_key TEXT, " +
                "flag_name TEXT, " +
                "flag_value TEXT, " +
                "PRIMARY KEY (chunk_key, flag_name)" +
                ")");
            
            // Spawns table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS kingdom_spawns (" +
                "kingdom_name TEXT, " +
                "spawn_id INTEGER, " +
                "world TEXT, " +
                "x REAL, y REAL, z REAL, " +
                "yaw REAL, pitch REAL, " +
                "PRIMARY KEY (kingdom_name, spawn_id)" +
                ")");
            
            // Trusts table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS trusts (" +
                "kingdom_name TEXT, " +
                "player TEXT, " +
                "permission TEXT, " +
                "PRIMARY KEY (kingdom_name, player, permission)" +
                ")");
            
            // Wars table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS wars (" +
                "war_id TEXT PRIMARY KEY, " +
                "kingdom1 TEXT, " +
                "kingdom2 TEXT, " +
                "start_time INTEGER, " +
                "end_time INTEGER, " +
                "active INTEGER" +
                ")");
            
            // Bank table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS kingdom_bank (" +
                "kingdom_name TEXT PRIMARY KEY, " +
                "balance REAL DEFAULT 0" +
                ")");
            
            // Activity table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS player_activity (" +
                "player TEXT PRIMARY KEY, " +
                "kingdom_name TEXT, " +
                "last_login INTEGER, " +
                "playtime INTEGER DEFAULT 0, " +
                "last_contribution INTEGER DEFAULT 0, " +
                "contributions INTEGER DEFAULT 0, " +
                "contribution_streak INTEGER DEFAULT 0, " +
                "last_streak_day INTEGER DEFAULT 0" +
                ")");
            
            // Add new columns if they don't exist (for existing databases)
            try {
                stmt.executeUpdate("ALTER TABLE player_activity ADD COLUMN last_contribution INTEGER DEFAULT 0");
            } catch (SQLException e) {
                // Column already exists, ignore
            }
            try {
                stmt.executeUpdate("ALTER TABLE player_activity ADD COLUMN contributions INTEGER DEFAULT 0");
            } catch (SQLException e) {
                // Column already exists, ignore
            }
            try {
                stmt.executeUpdate("ALTER TABLE player_activity ADD COLUMN contribution_streak INTEGER DEFAULT 0");
            } catch (SQLException e) {
                // Column already exists, ignore
            }
            try {
                stmt.executeUpdate("ALTER TABLE player_activity ADD COLUMN last_streak_day INTEGER DEFAULT 0");
            } catch (SQLException e) {
                // Column already exists, ignore
            }
            
            // Achievements table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS player_achievements (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "kingdom_name TEXT, " +
                "player TEXT, " +
                "achievement_id TEXT, " +
                "achievement_name TEXT, " +
                "description TEXT, " +
                "unlocked_at INTEGER DEFAULT 0, " +
                "unlocked_by TEXT, " +
                "progress INTEGER DEFAULT 0, " +
                "target INTEGER DEFAULT 0, " +
                "completed INTEGER DEFAULT 0, " +
                "UNIQUE(kingdom_name, player, achievement_id)" +
                ")");
            
            // Mail table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS kingdom_mail (" +
                "mail_id TEXT PRIMARY KEY, " +
                "recipient TEXT, " +
                "sender TEXT, " +
                "kingdom_name TEXT, " +
                "subject TEXT, " +
                "message TEXT, " +
                "sent_at INTEGER, " +
                "read INTEGER DEFAULT 0, " +
                "read_at INTEGER DEFAULT 0, " +
                "deleted INTEGER DEFAULT 0" +
                ")");
            
            // Create indexes for better performance
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_mail_recipient ON kingdom_mail(recipient)");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_mail_sender ON kingdom_mail(sender)");
        }
    }

    @Override
    public void saveKingdom(Kingdom kingdom) {
        try {
            // Save kingdom
            try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT OR REPLACE INTO kingdoms (name, king, xp, level, current_claim_chunks, created_at, total_challenges_completed) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                stmt.setString(1, kingdom.getName());
                stmt.setString(2, kingdom.getKing());
                stmt.setInt(3, kingdom.getXp());
                stmt.setInt(4, kingdom.getLevel());
                stmt.setInt(5, kingdom.getCurrentClaimChunks());
                stmt.setLong(6, kingdom.getCreatedAt());
                stmt.setInt(7, kingdom.getTotalChallengesCompleted());
                stmt.executeUpdate();
            }
            
            // Save members
            try (PreparedStatement deleteStmt = connection.prepareStatement("DELETE FROM kingdom_members WHERE kingdom_name=?")) {
                deleteStmt.setString(1, kingdom.getName());
                deleteStmt.executeUpdate();
            }
            try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO kingdom_members (kingdom_name, player, role, contribution) VALUES (?, ?, ?, ?)")) {
                for (String member : kingdom.getMembers()) {
                    stmt.setString(1, kingdom.getName());
                    stmt.setString(2, member);
                    stmt.setString(3, kingdom.getRole(member).name());
                    stmt.setInt(4, kingdom.getContribution(member));
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
            
            // Save alliances
            try (PreparedStatement deleteStmt = connection.prepareStatement("DELETE FROM kingdom_alliances WHERE kingdom1=? OR kingdom2=?")) {
                deleteStmt.setString(1, kingdom.getName());
                deleteStmt.setString(2, kingdom.getName());
                deleteStmt.executeUpdate();
            }
            try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO kingdom_alliances (kingdom1, kingdom2) VALUES (?, ?)")) {
                for (String ally : kingdom.getAlliances()) {
                    stmt.setString(1, kingdom.getName());
                    stmt.setString(2, ally);
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save kingdom to SQLite: " + e.getMessage());
        }
    }

    @Override
    public Kingdom loadKingdom(String name) {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM kingdoms WHERE name=?")) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) return null;
                
                Kingdom kingdom = new Kingdom(name, rs.getString("king"));
                kingdom.setXp(rs.getInt("xp"));
                kingdom.setLevel(rs.getInt("level"));
                kingdom.setCurrentClaimChunks(rs.getInt("current_claim_chunks"));
                kingdom.setCreatedAt(rs.getLong("created_at"));
                kingdom.setTotalChallengesCompleted(rs.getInt("total_challenges_completed"));
                
                // Load members
                try (PreparedStatement memberStmt = connection.prepareStatement(
                    "SELECT * FROM kingdom_members WHERE kingdom_name=?")) {
                    memberStmt.setString(1, name);
                    try (ResultSet memberRs = memberStmt.executeQuery()) {
                        while (memberRs.next()) {
                            String player = memberRs.getString("player");
                            kingdom.addMember(player);
                            try {
                                kingdom.setRole(player, com.excrele.kingdoms.model.MemberRole.valueOf(memberRs.getString("role")));
                            } catch (IllegalArgumentException e) {
                                kingdom.setRole(player, com.excrele.kingdoms.model.MemberRole.MEMBER);
                            }
                            kingdom.addContribution(player, memberRs.getInt("contribution"));
                        }
                    }
                }
                
                // Load alliances
                try (PreparedStatement allyStmt = connection.prepareStatement(
                    "SELECT kingdom2 FROM kingdom_alliances WHERE kingdom1=?")) {
                    allyStmt.setString(1, name);
                    try (ResultSet allyRs = allyStmt.executeQuery()) {
                        while (allyRs.next()) {
                            kingdom.addAlliance(allyRs.getString("kingdom2"));
                        }
                    }
                }
                
                return kingdom;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load kingdom from SQLite: " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<String> loadAllKingdomNames() {
        List<String> names = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name FROM kingdoms")) {
            while (rs.next()) {
                names.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load kingdom names from SQLite: " + e.getMessage());
        }
        return names;
    }

    @Override
    public void deleteKingdom(String name) {
        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM kingdoms WHERE name=?")) {
            stmt.setString(1, name);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to delete kingdom from SQLite: " + e.getMessage());
        }
    }

    @Override
    public void saveClaim(String kingdomName, Chunk chunk, String claimId) {
        String chunkKey = chunk.getWorld().getName() + ":" + chunk.getX() + ":" + chunk.getZ();
        try (PreparedStatement stmt = connection.prepareStatement(
            "INSERT OR REPLACE INTO claims (chunk_key, kingdom_name, claim_id) VALUES (?, ?, ?)")) {
            stmt.setString(1, chunkKey);
            stmt.setString(2, kingdomName);
            stmt.setString(3, claimId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save claim to SQLite: " + e.getMessage());
        }
    }

    @Override
    public void deleteClaim(String kingdomName, Chunk chunk) {
        String chunkKey = chunk.getWorld().getName() + ":" + chunk.getX() + ":" + chunk.getZ();
        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM claims WHERE chunk_key=?")) {
            stmt.setString(1, chunkKey);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to delete claim from SQLite: " + e.getMessage());
        }
    }

    @Override
    public Map<String, String> loadAllClaims() {
        Map<String, String> claims = new HashMap<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT chunk_key, kingdom_name FROM claims")) {
            while (rs.next()) {
                claims.put(rs.getString("chunk_key"), rs.getString("kingdom_name"));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load claims from SQLite: " + e.getMessage());
        }
        return claims;
    }

    @Override
    public void saveTrust(String kingdomName, String player, String permission) {
        try (PreparedStatement stmt = connection.prepareStatement(
            "INSERT OR IGNORE INTO trusts (kingdom_name, player, permission) VALUES (?, ?, ?)")) {
            stmt.setString(1, kingdomName);
            stmt.setString(2, player);
            stmt.setString(3, permission);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save trust to SQLite: " + e.getMessage());
        }
    }

    @Override
    public void deleteTrust(String kingdomName, String player) {
        try (PreparedStatement stmt = connection.prepareStatement(
            "DELETE FROM trusts WHERE kingdom_name=? AND player=?")) {
            stmt.setString(1, kingdomName);
            stmt.setString(2, player);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to delete trust from SQLite: " + e.getMessage());
        }
    }

    @Override
    public Map<String, List<String>> loadTrusts(String kingdomName) {
        Map<String, List<String>> trusts = new HashMap<>();
        try (PreparedStatement stmt = connection.prepareStatement(
            "SELECT player, permission FROM trusts WHERE kingdom_name=?")) {
            stmt.setString(1, kingdomName);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String player = rs.getString("player");
                    String permission = rs.getString("permission");
                    trusts.computeIfAbsent(player, k -> new ArrayList<>()).add(permission);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load trusts from SQLite: " + e.getMessage());
        }
        return trusts;
    }

    @Override
    public void saveWar(String warId, String kingdom1, String kingdom2, long startTime, long endTime, boolean active) {
        try (PreparedStatement stmt = connection.prepareStatement(
            "INSERT OR REPLACE INTO wars (war_id, kingdom1, kingdom2, start_time, end_time, active) " +
            "VALUES (?, ?, ?, ?, ?, ?)")) {
            stmt.setString(1, warId);
            stmt.setString(2, kingdom1);
            stmt.setString(3, kingdom2);
            stmt.setLong(4, startTime);
            stmt.setLong(5, endTime);
            stmt.setInt(6, active ? 1 : 0);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save war to SQLite: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> loadWar(String warId) {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM wars WHERE war_id=?")) {
            stmt.setString(1, warId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) return null;
                Map<String, Object> war = new HashMap<>();
                war.put("warId", warId);
                war.put("kingdom1", rs.getString("kingdom1"));
                war.put("kingdom2", rs.getString("kingdom2"));
                war.put("startTime", rs.getLong("start_time"));
                war.put("endTime", rs.getLong("end_time"));
                war.put("active", rs.getInt("active") == 1);
                return war;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load war from SQLite: " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<Map<String, Object>> loadActiveWars() {
        List<Map<String, Object>> wars = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM wars WHERE active=1")) {
            while (rs.next()) {
                Map<String, Object> war = new HashMap<>();
                war.put("warId", rs.getString("war_id"));
                war.put("kingdom1", rs.getString("kingdom1"));
                war.put("kingdom2", rs.getString("kingdom2"));
                war.put("startTime", rs.getLong("start_time"));
                war.put("endTime", rs.getLong("end_time"));
                war.put("active", true);
                wars.add(war);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load active wars from SQLite: " + e.getMessage());
        }
        return wars;
    }

    @Override
    public void saveBankBalance(String kingdomName, double balance) {
        try (PreparedStatement stmt = connection.prepareStatement(
            "INSERT OR REPLACE INTO kingdom_bank (kingdom_name, balance) VALUES (?, ?)")) {
            stmt.setString(1, kingdomName);
            stmt.setDouble(2, balance);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save bank balance to SQLite: " + e.getMessage());
        }
    }

    @Override
    public double loadBankBalance(String kingdomName) {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT balance FROM kingdom_bank WHERE kingdom_name=?")) {
            stmt.setString(1, kingdomName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("balance");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load bank balance from SQLite: " + e.getMessage());
        }
        return 0.0;
    }

    @Override
    public void savePlayerActivity(String player, String kingdomName, long lastLogin, long playtime) {
        try (PreparedStatement stmt = connection.prepareStatement(
            "INSERT OR REPLACE INTO player_activity (player, kingdom_name, last_login, playtime) VALUES (?, ?, ?, ?)")) {
            stmt.setString(1, player);
            stmt.setString(2, kingdomName);
            stmt.setLong(3, lastLogin);
            stmt.setLong(4, playtime);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save player activity to SQLite: " + e.getMessage());
        }
    }
    
    @Override
    public void savePlayerActivity(String player, String kingdomName, long lastLogin, long playtime, 
                                     long lastContribution, int contributions, int contributionStreak, long lastStreakDay) {
        try (PreparedStatement stmt = connection.prepareStatement(
            "INSERT OR REPLACE INTO player_activity (player, kingdom_name, last_login, playtime, last_contribution, contributions, contribution_streak, last_streak_day) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
            stmt.setString(1, player);
            stmt.setString(2, kingdomName);
            stmt.setLong(3, lastLogin);
            stmt.setLong(4, playtime);
            stmt.setLong(5, lastContribution);
            stmt.setInt(6, contributions);
            stmt.setInt(7, contributionStreak);
            stmt.setLong(8, lastStreakDay);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save player activity to SQLite: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> loadPlayerActivity(String player) {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM player_activity WHERE player=?")) {
            stmt.setString(1, player);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) return null;
                Map<String, Object> activity = new HashMap<>();
                activity.put("kingdom", rs.getString("kingdom_name"));
                activity.put("lastLogin", rs.getLong("last_login"));
                activity.put("playtime", rs.getLong("playtime"));
                try {
                    activity.put("lastContribution", rs.getLong("last_contribution"));
                    activity.put("contributions", rs.getInt("contributions"));
                    activity.put("contributionStreak", rs.getInt("contribution_streak"));
                    activity.put("lastStreakDay", rs.getLong("last_streak_day"));
                } catch (SQLException e) {
                    // Columns might not exist in old databases
                    long currentDay = System.currentTimeMillis() / 1000 / (24 * 60 * 60);
                    activity.put("lastContribution", System.currentTimeMillis() / 1000);
                    activity.put("contributions", 0);
                    activity.put("contributionStreak", 0);
                    activity.put("lastStreakDay", currentDay);
                }
                return activity;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load player activity from SQLite: " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<Map<String, Object>> loadMemberHistory(String kingdomName) {
        // TODO: Implement member history loading from database
        return new ArrayList<>();
    }

    @Override
    public void saveVault(String kingdomName, org.bukkit.inventory.Inventory vault) {
        // TODO: Implement vault saving to database
        // Would require a vaults table with kingdom_name, slot, item_data columns
    }

    @Override
    public Map<Integer, org.bukkit.inventory.ItemStack> loadVault(String kingdomName) {
        // TODO: Implement vault loading from database
        return new HashMap<>();
    }

    // Advanced Features operations - Stub implementations
    @Override
    public void saveWaypoint(String kingdomName, String waypointName, org.bukkit.Location location, String createdBy) {
        // TODO: Implement waypoint saving to SQLite
    }

    @Override
    public void deleteWaypoint(String kingdomName, String waypointName) {
        // TODO: Implement waypoint deletion from SQLite
    }

    @Override
    public Map<String, Map<String, Object>> loadWaypoints(String kingdomName) {
        // TODO: Implement waypoint loading from SQLite
        return new HashMap<>();
    }

    @Override
    public void saveFarm(String kingdomName, String farmName, String farmType, org.bukkit.Chunk chunk, org.bukkit.Location center, long lastHarvest, boolean isActive) {
        // TODO: Implement farm saving to SQLite
    }

    @Override
    public void deleteFarm(String kingdomName, String farmName) {
        // TODO: Implement farm deletion from SQLite
    }

    @Override
    public Map<String, Map<String, Object>> loadFarms(String kingdomName) {
        // TODO: Implement farm loading from SQLite
        return new HashMap<>();
    }

    @Override
    public void saveWorkshop(String kingdomName, String workshopName, String workshopType, org.bukkit.Chunk chunk, org.bukkit.Location location, double bonusMultiplier, boolean isActive) {
        // TODO: Implement workshop saving to SQLite
    }

    @Override
    public void deleteWorkshop(String kingdomName, String workshopName) {
        // TODO: Implement workshop deletion from SQLite
    }

    @Override
    public Map<String, Map<String, Object>> loadWorkshops(String kingdomName) {
        // TODO: Implement workshop loading from SQLite
        return new HashMap<>();
    }

    @Override
    public void saveLibrary(String kingdomName, String libraryName, org.bukkit.Chunk chunk, org.bukkit.Location location, int maxBooks, boolean isPublic) {
        // TODO: Implement library saving to SQLite
    }

    @Override
    public void deleteLibrary(String kingdomName, String libraryName) {
        // TODO: Implement library deletion from SQLite
    }

    @Override
    public Map<String, Map<String, Object>> loadLibraries(String kingdomName) {
        // TODO: Implement library loading from SQLite
        return new HashMap<>();
    }

    @Override
    public void saveStable(String kingdomName, String stableName, org.bukkit.Chunk chunk, org.bukkit.Location location, int maxMounts, boolean isPublic) {
        // TODO: Implement stable saving to SQLite
    }

    @Override
    public void deleteStable(String kingdomName, String stableName) {
        // TODO: Implement stable deletion from SQLite
    }

    @Override
    public Map<String, Map<String, Object>> loadStables(String kingdomName) {
        // TODO: Implement stable loading from SQLite
        return new HashMap<>();
    }

    // Statistics operations - Stub implementations
    @Override
    public void saveClaimAnalytics(String chunkKey, String kingdomName, long claimedAt, long lastActivity, int playerVisits, int blockInteractions, int entityInteractions, double estimatedValue) {
        // TODO: Implement claim analytics saving to SQLite
    }

    @Override
    public Map<String, Object> loadClaimAnalytics(String chunkKey) {
        // TODO: Implement claim analytics loading from SQLite
        return null;
    }

    @Override
    public void saveKingdomHistory(String kingdomName, long timestamp, String type, String description, String actor) {
        // TODO: Implement kingdom history saving to SQLite
    }

    @Override
    public List<Map<String, Object>> loadKingdomHistory(String kingdomName) {
        // TODO: Implement kingdom history loading from SQLite
        return new ArrayList<>();
    }

    @Override
    public void saveGrowthData(String kingdomName, long timestamp, int level, int xp, int claims, int members, int alliances) {
        // TODO: Implement growth data saving to SQLite
    }

    @Override
    public List<Map<String, Object>> loadGrowthData(String kingdomName) {
        // TODO: Implement growth data loading from SQLite
        return new ArrayList<>();
    }

    @Override
    public void savePlayerAchievement(String kingdomName, String playerName, String achievementId, String achievementName, 
                                      String description, long unlockedAt, String unlockedBy, int progress, int target, boolean completed) {
        try (PreparedStatement stmt = connection.prepareStatement(
            "INSERT OR REPLACE INTO player_achievements (kingdom_name, player, achievement_id, achievement_name, description, unlocked_at, unlocked_by, progress, target, completed) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            stmt.setString(1, kingdomName);
            stmt.setString(2, playerName);
            stmt.setString(3, achievementId);
            stmt.setString(4, achievementName);
            stmt.setString(5, description);
            stmt.setLong(6, unlockedAt);
            stmt.setString(7, unlockedBy);
            stmt.setInt(8, progress);
            stmt.setInt(9, target);
            stmt.setInt(10, completed ? 1 : 0);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save player achievement to SQLite: " + e.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> loadPlayerAchievements(String kingdomName, String playerName) {
        List<Map<String, Object>> achievements = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(
            "SELECT * FROM player_achievements WHERE kingdom_name=? AND player=?")) {
            stmt.setString(1, kingdomName);
            stmt.setString(2, playerName);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> achievement = new HashMap<>();
                    achievement.put("id", rs.getString("achievement_id"));
                    achievement.put("name", rs.getString("achievement_name"));
                    achievement.put("description", rs.getString("description"));
                    achievement.put("unlockedAt", rs.getLong("unlocked_at"));
                    achievement.put("unlockedBy", rs.getString("unlocked_by"));
                    achievement.put("progress", rs.getInt("progress"));
                    achievement.put("target", rs.getInt("target"));
                    achievement.put("completed", rs.getInt("completed") == 1);
                    achievements.add(achievement);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load player achievements from SQLite: " + e.getMessage());
        }
        return achievements;
    }

    @Override
    public void saveMail(String mailId, String recipient, String sender, String kingdomName, String subject, 
                         String message, long sentAt, boolean read, long readAt, boolean deleted) {
        try (PreparedStatement stmt = connection.prepareStatement(
            "INSERT OR REPLACE INTO kingdom_mail (mail_id, recipient, sender, kingdom_name, subject, message, sent_at, read, read_at, deleted) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            stmt.setString(1, mailId);
            stmt.setString(2, recipient);
            stmt.setString(3, sender);
            stmt.setString(4, kingdomName);
            stmt.setString(5, subject);
            stmt.setString(6, message);
            stmt.setLong(7, sentAt);
            stmt.setInt(8, read ? 1 : 0);
            stmt.setLong(9, readAt);
            stmt.setInt(10, deleted ? 1 : 0);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save mail to SQLite: " + e.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> loadPlayerMail(String playerName) {
        List<Map<String, Object>> mailList = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(
            "SELECT * FROM kingdom_mail WHERE recipient=? ORDER BY sent_at DESC")) {
            stmt.setString(1, playerName);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> mail = new HashMap<>();
                    mail.put("mailId", rs.getString("mail_id"));
                    mail.put("recipient", rs.getString("recipient"));
                    mail.put("sender", rs.getString("sender"));
                    mail.put("kingdomName", rs.getString("kingdom_name"));
                    mail.put("subject", rs.getString("subject"));
                    mail.put("message", rs.getString("message"));
                    mail.put("sentAt", rs.getLong("sent_at"));
                    mail.put("read", rs.getInt("read") == 1);
                    mail.put("readAt", rs.getLong("read_at"));
                    mail.put("deleted", rs.getInt("deleted") == 1);
                    mailList.add(mail);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load player mail from SQLite: " + e.getMessage());
        }
        return mailList;
    }

    @Override
    public void deleteMail(String mailId) {
        try (PreparedStatement stmt = connection.prepareStatement(
            "DELETE FROM kingdom_mail WHERE mail_id=?")) {
            stmt.setString(1, mailId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to delete mail from SQLite: " + e.getMessage());
        }
    }

    @Override
    public void saveSiege(String siegeId, String warId, String attackingKingdom, String defendingKingdom,
                          String worldName, int chunkX, int chunkZ, long startTime, long endTime,
                          int attackProgress, boolean active) {
        // TODO: Implement SQLite siege storage
    }

    @Override
    public List<Map<String, Object>> loadActiveSieges() {
        return new ArrayList<>();
    }

    @Override
    public void saveRaid(String raidId, String raidingKingdom, String targetKingdom,
                         String worldName, int chunkX, int chunkZ, long startTime, long endTime,
                         int resourcesStolen, boolean active) {
        // TODO: Implement SQLite raid storage
    }

    @Override
    public List<Map<String, Object>> loadActiveRaids() {
        return new ArrayList<>();
    }

    @Override
    public void saveTaxSettings(String kingdomName, double taxRate, long taxInterval, long lastCollection) {
        // TODO: Implement SQLite tax storage
    }

    @Override
    public Map<String, Object> loadTaxSettings(String kingdomName) {
        return new HashMap<>();
    }

    @Override
    public void saveTradeRoute(String routeId, String kingdom1, String kingdom2,
                              String world1, double x1, double y1, double z1,
                              String world2, double x2, double y2, double z2,
                              long establishedAt, boolean active, double tradeVolume,
                              int tradeCount, long lastTradeTime) {
        // TODO: Implement SQLite trade route storage
    }

    @Override
    public List<Map<String, Object>> loadTradeRoutes() {
        return new ArrayList<>();
    }

    @Override
    public void saveAdvancedChallenge(String challengeId, String name, String description, String type,
                                     int difficulty, int xpReward, long startTime, long endTime,
                                     int requiredMembers, String chainId, int chainOrder, boolean active) {
        // TODO: Implement SQLite advanced challenge storage
    }

    @Override
    public List<Map<String, Object>> loadAdvancedChallenges() {
        return new ArrayList<>();
    }

    @Override
    public void saveKingdomStructure(String structureId, String kingdomName, String type,
                                    String worldName, double x, double y, double z,
                                    long builtAt, int level, boolean active) {
        // TODO: Implement SQLite structure storage
    }

    @Override
    public List<Map<String, Object>> loadKingdomStructures() {
        return new ArrayList<>();
    }

    @Override
    public void saveKingdomResource(String kingdomName, String resourceType, int amount) {
        // TODO: Implement SQLite resource storage
    }

    @Override
    public List<Map<String, Object>> loadKingdomResources() {
        return new ArrayList<>();
    }

    @Override
    public void saveDiplomaticAgreement(String agreementId, String kingdom1, String kingdom2,
                                       String type, long establishedAt, long expiresAt,
                                       boolean active, String terms) {
        // TODO: Implement SQLite diplomacy storage
    }

    @Override
    public List<Map<String, Object>> loadDiplomaticAgreements() {
        return new ArrayList<>();
    }

    @Override
    public void saveKingdomTheme(String kingdomName, String primaryColor, String secondaryColor,
                                String accentColor, String bannerMaterial, String flagMaterial,
                                String primaryParticle, String secondaryParticle, String themeName) {
        // TODO: Implement SQLite theme storage
    }

    @Override
    public List<Map<String, Object>> loadKingdomThemes() {
        return new ArrayList<>();
    }

    @Override
    public void saveKingdomBanner(String bannerId, String kingdomName, String worldName,
                                 double x, double y, double z, String bannerMaterial) {
        // TODO: Implement SQLite banner storage
    }

    @Override
    public void deleteKingdomBanner(String bannerId) {
        // TODO: Implement SQLite banner deletion
    }

    @Override
    public List<Map<String, Object>> loadKingdomBanners() {
        return new ArrayList<>();
    }

    @Override
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to close SQLite connection: " + e.getMessage());
        }
    }

    @Override
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}

