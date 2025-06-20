package com.example.screenshare.utils;

import com.example.screenshare.ScreenSharePlugin;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private final ScreenSharePlugin plugin;
    private final FileConfiguration config;
    private final Map<String, String> messageCache;

    public ConfigManager(ScreenSharePlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.messageCache = new HashMap<>();

        // Save default config if it doesn't exist
        plugin.saveDefaultConfig();

        // Load messages into cache
        loadMessages();
    }

    private void loadMessages() {
        messageCache.clear();
        cacheMessagesFromSection("messages");
        cacheMessagesFromSection("usage");
        cacheMessagesFromSection("help");
        cacheMessagesFromSection("scoreboard");
        cacheMessagesFromSection("staff_interface");
    }

    private void cacheMessagesFromSection(String section) {
        if (config.getConfigurationSection(section) != null) {
            for (String key : config.getConfigurationSection(section).getKeys(true)) {
                String value = config.getString(section + "." + key);
                if (value != null) {
                    messageCache.put(section + "." + key, ChatColor.translateAlternateColorCodes('&', value));
                }
            }
        }
    }

    public String getMessage(String path) {
        String message = messageCache.get(path);
        if (message == null) {
            message = config.getString(path);
            if (message != null) {
                message = ChatColor.translateAlternateColorCodes('&', message);
                messageCache.put(path, message);
            } else {
                return ChatColor.RED + "Missing message: " + path;
            }
        }
        return message;
    }

    public String getMessage(String path, Map<String, String> placeholders) {
        String message = getMessage(path);

        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                message = message.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }

        return message;
    }

    public String getPrefix() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("plugin.prefix", "&6[ScreenShare] &r"));
    }

    public String getPrefixedMessage(String path) {
        return getPrefix() + getMessage(path);
    }

    public String getPrefixedMessage(String path, Map<String, String> placeholders) {
        return getPrefix() + getMessage(path, placeholders);
    }

    // World settings
    public String getWorldName() {
        return config.getString("plugin.world.name", "screenshare_world");
    }

    public String getWorldType() {
        return config.getString("plugin.world.type", "FLAT");
    }

    public String getWorldDifficulty() {
        return config.getString("plugin.world.difficulty", "PEACEFUL");
    }

    public boolean isWorldPvpEnabled() {
        return config.getBoolean("plugin.world.pvp", false);
    }

    public int getWorldTime() {
        return config.getInt("plugin.world.time", 6000);
    }

    public boolean isWorldWeatherEnabled() {
        return config.getBoolean("plugin.world.weather", false);
    }

    public boolean isWorldMobSpawningEnabled() {
        return config.getBoolean("plugin.world.mob_spawning", false);
    }

    // Time formats
    public String getDateTimeFormat() {
        return config.getString("time_formats.date_time", "dd/MM/yyyy HH:mm:ss");
    }

    public String getTimeOnlyFormat() {
        return config.getString("time_formats.time_only", "HH:mm:ss");
    }

    // Logging messages
    public String getLogMessage(String path, Map<String, String> placeholders) {
        String message = config.getString("logging." + path, path);

        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                message = message.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }

        return message;
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        loadMessages();
    }

    // Utility methods for common placeholder patterns
    public Map<String, String> createPlaceholders() {
        return new HashMap<>();
    }

    public Map<String, String> createPlaceholders(String key, String value) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put(key, value);
        return placeholders;
    }

    public Map<String, String> createPlaceholders(String key1, String value1, String key2, String value2) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put(key1, value1);
        placeholders.put(key2, value2);
        return placeholders;
    }
}