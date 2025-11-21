package com.excrele.kingdoms.storage;

import com.excrele.kingdoms.KingdomsPlugin;

/**
 * Manages storage adapters and provides unified access
 */
public class StorageManager {
    private final KingdomsPlugin plugin;
    private StorageAdapter adapter;
    private StorageType storageType;

    public StorageManager(KingdomsPlugin plugin) {
        this.plugin = plugin;
        initializeStorage();
    }

    private void initializeStorage() {
        String storageTypeStr = plugin.getConfig().getString("storage.type", "yaml").toUpperCase();
        
        try {
            storageType = StorageType.valueOf(storageTypeStr);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid storage type: " + storageTypeStr + ". Using YAML.");
            storageType = StorageType.YAML;
        }

        try {
            switch (storageType) {
                case MYSQL:
                    String host = plugin.getConfig().getString("storage.mysql.host", "localhost");
                    int port = plugin.getConfig().getInt("storage.mysql.port", 3306);
                    String database = plugin.getConfig().getString("storage.mysql.database", "kingdoms");
                    String username = plugin.getConfig().getString("storage.mysql.username", "root");
                    String password = plugin.getConfig().getString("storage.mysql.password", "");
                    adapter = new MySQLStorageAdapter(plugin, host, port, database, username, password);
                    adapter.initialize();
                    plugin.getLogger().info("Using MySQL storage");
                    break;
                case SQLITE:
                    adapter = new SQLiteStorageAdapter(plugin);
                    adapter.initialize();
                    plugin.getLogger().info("Using SQLite storage");
                    break;
                case YAML:
                default:
                    adapter = new YamlStorageAdapter(plugin);
                    adapter.initialize();
                    plugin.getLogger().info("Using YAML storage");
                    break;
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize " + storageType + " storage: " + e.getMessage());
            plugin.getLogger().severe("Falling back to YAML storage");
            adapter = new YamlStorageAdapter(plugin);
            adapter.initialize();
            storageType = StorageType.YAML;
        }
    }

    public StorageAdapter getAdapter() {
        return adapter;
    }

    public StorageType getStorageType() {
        return storageType;
    }

    public void close() {
        if (adapter != null) {
            adapter.close();
        }
    }
}

