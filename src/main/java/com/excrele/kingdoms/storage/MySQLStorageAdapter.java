package com.excrele.kingdoms.storage;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.Kingdom;
import org.bukkit.Chunk;

import java.sql.*;
import java.util.*;

/**
 * MySQL storage adapter (optional - requires MySQL connector)
 */
public class MySQLStorageAdapter implements StorageAdapter {
    private final KingdomsPlugin plugin;
    private Connection connection;
    private String host, database, username, password;
    private int port;

    public MySQLStorageAdapter(KingdomsPlugin plugin, String host, int port, String database, String username, String password) {
        this.plugin = plugin;
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    @Override
    public void initialize() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&allowPublicKeyRetrieval=true";
            connection = DriverManager.getConnection(url, username, password);
            createTables();
            plugin.getLogger().info("MySQL connection established!");
        } catch (ClassNotFoundException e) {
            plugin.getLogger().severe("MySQL driver not found! Falling back to YAML.");
            throw new RuntimeException("MySQL driver not available", e);
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to connect to MySQL: " + e.getMessage());
            plugin.getLogger().severe("Falling back to YAML storage.");
            throw new RuntimeException("MySQL connection failed", e);
        }
    }

    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Kingdoms table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS kingdoms (" +
                "name VARCHAR(255) PRIMARY KEY, " +
                "king VARCHAR(36), " +
                "xp INT DEFAULT 0, " +
                "level INT DEFAULT 1, " +
                "current_claim_chunks INT DEFAULT 0, " +
                "created_at BIGINT, " +
                "total_challenges_completed INT DEFAULT 0" +
                ")");
            
            // Members table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS kingdom_members (" +
                "kingdom_name VARCHAR(255), " +
                "player VARCHAR(36), " +
                "role VARCHAR(20), " +
                "contribution INT DEFAULT 0, " +
                "PRIMARY KEY (kingdom_name, player)" +
                ")");
            
            // Alliances table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS kingdom_alliances (" +
                "kingdom1 VARCHAR(255), " +
                "kingdom2 VARCHAR(255), " +
                "PRIMARY KEY (kingdom1, kingdom2)" +
                ")");
            
            // Claims table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS claims (" +
                "chunk_key VARCHAR(255) PRIMARY KEY, " +
                "kingdom_name VARCHAR(255), " +
                "claim_id VARCHAR(255), " +
                "plot_type VARCHAR(50)" +
                ")");
            
            // Chunk flags table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS chunk_flags (" +
                "chunk_key VARCHAR(255), " +
                "flag_name VARCHAR(50), " +
                "flag_value TEXT, " +
                "PRIMARY KEY (chunk_key, flag_name)" +
                ")");
            
            // Spawns table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS kingdom_spawns (" +
                "kingdom_name VARCHAR(255), " +
                "spawn_id INT, " +
                "world VARCHAR(255), " +
                "x DOUBLE, y DOUBLE, z DOUBLE, " +
                "yaw FLOAT, pitch FLOAT, " +
                "PRIMARY KEY (kingdom_name, spawn_id)" +
                ")");
            
            // Trusts table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS trusts (" +
                "kingdom_name VARCHAR(255), " +
                "player VARCHAR(36), " +
                "permission VARCHAR(50), " +
                "PRIMARY KEY (kingdom_name, player, permission)" +
                ")");
            
            // Wars table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS wars (" +
                "war_id VARCHAR(255) PRIMARY KEY, " +
                "kingdom1 VARCHAR(255), " +
                "kingdom2 VARCHAR(255), " +
                "start_time BIGINT, " +
                "end_time BIGINT, " +
                "active BOOLEAN" +
                ")");
            
            // Bank table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS kingdom_bank (" +
                "kingdom_name VARCHAR(255) PRIMARY KEY, " +
                "balance DOUBLE DEFAULT 0" +
                ")");
            
            // Activity table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS player_activity (" +
                "player VARCHAR(36) PRIMARY KEY, " +
                "kingdom_name VARCHAR(255), " +
                "last_login BIGINT, " +
                "playtime BIGINT DEFAULT 0" +
                ")");
        }
    }

    @Override
    public void saveKingdom(Kingdom kingdom) {
        try {
            // Save kingdom
            try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO kingdoms (name, king, xp, level, current_claim_chunks, created_at, total_challenges_completed) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE " +
                "king=?, xp=?, level=?, current_claim_chunks=?, total_challenges_completed=?")) {
                stmt.setString(1, kingdom.getName());
                stmt.setString(2, kingdom.getKing());
                stmt.setInt(3, kingdom.getXp());
                stmt.setInt(4, kingdom.getLevel());
                stmt.setInt(5, kingdom.getCurrentClaimChunks());
                stmt.setLong(6, kingdom.getCreatedAt());
                stmt.setInt(7, kingdom.getTotalChallengesCompleted());
                stmt.setString(8, kingdom.getKing());
                stmt.setInt(9, kingdom.getXp());
                stmt.setInt(10, kingdom.getLevel());
                stmt.setInt(11, kingdom.getCurrentClaimChunks());
                stmt.setInt(12, kingdom.getTotalChallengesCompleted());
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
            plugin.getLogger().severe("Failed to save kingdom to MySQL: " + e.getMessage());
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
            plugin.getLogger().severe("Failed to load kingdom from MySQL: " + e.getMessage());
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
            plugin.getLogger().severe("Failed to load kingdom names from MySQL: " + e.getMessage());
        }
        return names;
    }

    @Override
    public void deleteKingdom(String name) {
        try {
            try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM kingdoms WHERE name=?")) {
                stmt.setString(1, name);
                stmt.executeUpdate();
            }
            // Cascade deletes handled by foreign keys or manual cleanup
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to delete kingdom from MySQL: " + e.getMessage());
        }
    }

    @Override
    public void saveClaim(String kingdomName, Chunk chunk, String claimId) {
        String chunkKey = chunk.getWorld().getName() + ":" + chunk.getX() + ":" + chunk.getZ();
        try (PreparedStatement stmt = connection.prepareStatement(
            "INSERT INTO claims (chunk_key, kingdom_name, claim_id) VALUES (?, ?, ?) " +
            "ON DUPLICATE KEY UPDATE kingdom_name=?, claim_id=?")) {
            stmt.setString(1, chunkKey);
            stmt.setString(2, kingdomName);
            stmt.setString(3, claimId);
            stmt.setString(4, kingdomName);
            stmt.setString(5, claimId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save claim to MySQL: " + e.getMessage());
        }
    }

    @Override
    public void deleteClaim(String kingdomName, Chunk chunk) {
        String chunkKey = chunk.getWorld().getName() + ":" + chunk.getX() + ":" + chunk.getZ();
        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM claims WHERE chunk_key=?")) {
            stmt.setString(1, chunkKey);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to delete claim from MySQL: " + e.getMessage());
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
            plugin.getLogger().severe("Failed to load claims from MySQL: " + e.getMessage());
        }
        return claims;
    }

    @Override
    public void saveTrust(String kingdomName, String player, String permission) {
        try (PreparedStatement stmt = connection.prepareStatement(
            "INSERT IGNORE INTO trusts (kingdom_name, player, permission) VALUES (?, ?, ?)")) {
            stmt.setString(1, kingdomName);
            stmt.setString(2, player);
            stmt.setString(3, permission);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save trust to MySQL: " + e.getMessage());
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
            plugin.getLogger().severe("Failed to delete trust from MySQL: " + e.getMessage());
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
            plugin.getLogger().severe("Failed to load trusts from MySQL: " + e.getMessage());
        }
        return trusts;
    }

    @Override
    public void saveWar(String warId, String kingdom1, String kingdom2, long startTime, long endTime, boolean active) {
        try (PreparedStatement stmt = connection.prepareStatement(
            "INSERT INTO wars (war_id, kingdom1, kingdom2, start_time, end_time, active) " +
            "VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE " +
            "kingdom1=?, kingdom2=?, start_time=?, end_time=?, active=?")) {
            stmt.setString(1, warId);
            stmt.setString(2, kingdom1);
            stmt.setString(3, kingdom2);
            stmt.setLong(4, startTime);
            stmt.setLong(5, endTime);
            stmt.setBoolean(6, active);
            stmt.setString(7, kingdom1);
            stmt.setString(8, kingdom2);
            stmt.setLong(9, startTime);
            stmt.setLong(10, endTime);
            stmt.setBoolean(11, active);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save war to MySQL: " + e.getMessage());
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
                war.put("active", rs.getBoolean("active"));
                return war;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load war from MySQL: " + e.getMessage());
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
            plugin.getLogger().severe("Failed to load active wars from MySQL: " + e.getMessage());
        }
        return wars;
    }

    @Override
    public void saveBankBalance(String kingdomName, double balance) {
        try (PreparedStatement stmt = connection.prepareStatement(
            "INSERT INTO kingdom_bank (kingdom_name, balance) VALUES (?, ?) " +
            "ON DUPLICATE KEY UPDATE balance=?")) {
            stmt.setString(1, kingdomName);
            stmt.setDouble(2, balance);
            stmt.setDouble(3, balance);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save bank balance to MySQL: " + e.getMessage());
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
            plugin.getLogger().severe("Failed to load bank balance from MySQL: " + e.getMessage());
        }
        return 0.0;
    }

    @Override
    public void savePlayerActivity(String player, String kingdomName, long lastLogin, long playtime) {
        try (PreparedStatement stmt = connection.prepareStatement(
            "INSERT INTO player_activity (player, kingdom_name, last_login, playtime) VALUES (?, ?, ?, ?) " +
            "ON DUPLICATE KEY UPDATE kingdom_name=?, last_login=?, playtime=?")) {
            stmt.setString(1, player);
            stmt.setString(2, kingdomName);
            stmt.setLong(3, lastLogin);
            stmt.setLong(4, playtime);
            stmt.setString(5, kingdomName);
            stmt.setLong(6, lastLogin);
            stmt.setLong(7, playtime);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save player activity to MySQL: " + e.getMessage());
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
                return activity;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load player activity from MySQL: " + e.getMessage());
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
        // TODO: Implement waypoint saving to MySQL
    }

    @Override
    public void deleteWaypoint(String kingdomName, String waypointName) {
        // TODO: Implement waypoint deletion from MySQL
    }

    @Override
    public Map<String, Map<String, Object>> loadWaypoints(String kingdomName) {
        // TODO: Implement waypoint loading from MySQL
        return new HashMap<>();
    }

    @Override
    public void saveFarm(String kingdomName, String farmName, String farmType, org.bukkit.Chunk chunk, org.bukkit.Location center, long lastHarvest, boolean isActive) {
        // TODO: Implement farm saving to MySQL
    }

    @Override
    public void deleteFarm(String kingdomName, String farmName) {
        // TODO: Implement farm deletion from MySQL
    }

    @Override
    public Map<String, Map<String, Object>> loadFarms(String kingdomName) {
        // TODO: Implement farm loading from MySQL
        return new HashMap<>();
    }

    @Override
    public void saveWorkshop(String kingdomName, String workshopName, String workshopType, org.bukkit.Chunk chunk, org.bukkit.Location location, double bonusMultiplier, boolean isActive) {
        // TODO: Implement workshop saving to MySQL
    }

    @Override
    public void deleteWorkshop(String kingdomName, String workshopName) {
        // TODO: Implement workshop deletion from MySQL
    }

    @Override
    public Map<String, Map<String, Object>> loadWorkshops(String kingdomName) {
        // TODO: Implement workshop loading from MySQL
        return new HashMap<>();
    }

    @Override
    public void saveLibrary(String kingdomName, String libraryName, org.bukkit.Chunk chunk, org.bukkit.Location location, int maxBooks, boolean isPublic) {
        // TODO: Implement library saving to MySQL
    }

    @Override
    public void deleteLibrary(String kingdomName, String libraryName) {
        // TODO: Implement library deletion from MySQL
    }

    @Override
    public Map<String, Map<String, Object>> loadLibraries(String kingdomName) {
        // TODO: Implement library loading from MySQL
        return new HashMap<>();
    }

    @Override
    public void saveStable(String kingdomName, String stableName, org.bukkit.Chunk chunk, org.bukkit.Location location, int maxMounts, boolean isPublic) {
        // TODO: Implement stable saving to MySQL
    }

    @Override
    public void deleteStable(String kingdomName, String stableName) {
        // TODO: Implement stable deletion from MySQL
    }

    @Override
    public Map<String, Map<String, Object>> loadStables(String kingdomName) {
        // TODO: Implement stable loading from MySQL
        return new HashMap<>();
    }

    // Statistics operations - Stub implementations
    @Override
    public void saveClaimAnalytics(String chunkKey, String kingdomName, long claimedAt, long lastActivity, int playerVisits, int blockInteractions, int entityInteractions, double estimatedValue) {
        // TODO: Implement claim analytics saving to MySQL
    }

    @Override
    public Map<String, Object> loadClaimAnalytics(String chunkKey) {
        // TODO: Implement claim analytics loading from MySQL
        return null;
    }

    @Override
    public void saveKingdomHistory(String kingdomName, long timestamp, String type, String description, String actor) {
        // TODO: Implement kingdom history saving to MySQL
    }

    @Override
    public List<Map<String, Object>> loadKingdomHistory(String kingdomName) {
        // TODO: Implement kingdom history loading from MySQL
        return new ArrayList<>();
    }

    @Override
    public void saveGrowthData(String kingdomName, long timestamp, int level, int xp, int claims, int members, int alliances) {
        // TODO: Implement growth data saving to MySQL
    }

    @Override
    public List<Map<String, Object>> loadGrowthData(String kingdomName) {
        // TODO: Implement growth data loading from MySQL
        return new ArrayList<>();
    }

    @Override
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to close MySQL connection: " + e.getMessage());
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

