package com.example.screenshare.utils;

import com.example.screenshare.ScreenSharePlugin;
import org.bukkit.ChatColor;

import java.util.Map;

public class MessageUtils {

    private static ScreenSharePlugin plugin;

    public static void setPlugin(ScreenSharePlugin pluginInstance) {
        plugin = pluginInstance;
    }

    public static String success(String message) {
        if (plugin != null) {
            return plugin.getConfigManager().getPrefix() + ChatColor.GREEN + message;
        }
        return ChatColor.GOLD + "[ScreenShare] " + ChatColor.GREEN + message;
    }

    public static String error(String message) {
        if (plugin != null) {
            return plugin.getConfigManager().getPrefix() + ChatColor.RED + message;
        }
        return ChatColor.GOLD + "[ScreenShare] " + ChatColor.RED + message;
    }

    public static String info(String message) {
        if (plugin != null) {
            return plugin.getConfigManager().getPrefix() + ChatColor.YELLOW + message;
        }
        return ChatColor.GOLD + "[ScreenShare] " + ChatColor.YELLOW + message;
    }

    public static String warning(String message) {
        if (plugin != null) {
            return plugin.getConfigManager().getPrefix() + ChatColor.GOLD + message;
        }
        return ChatColor.GOLD + "[ScreenShare] " + ChatColor.GOLD + message;
    }

    // New methods using config
    public static String getMessage(String path) {
        if (plugin != null) {
            return plugin.getConfigManager().getMessage(path);
        }
        return ChatColor.RED + "Plugin not initialized!";
    }

    public static String getMessage(String path, Map<String, String> placeholders) {
        if (plugin != null) {
            return plugin.getConfigManager().getMessage(path, placeholders);
        }
        return ChatColor.RED + "Plugin not initialized!";
    }

    public static String getPrefixedMessage(String path) {
        if (plugin != null) {
            return plugin.getConfigManager().getPrefixedMessage(path);
        }
        return ChatColor.RED + "Plugin not initialized!";
    }

    public static String getPrefixedMessage(String path, Map<String, String> placeholders) {
        if (plugin != null) {
            return plugin.getConfigManager().getPrefixedMessage(path, placeholders);
        }
        return ChatColor.RED + "Plugin not initialized!";
    }

    public static String formatPlayerName(String name, boolean isSuspect) {
        if (isSuspect) {
            return ChatColor.RED + "[SUSPECT] " + ChatColor.WHITE + name;
        } else {
            return ChatColor.BLUE + "[STAFF] " + ChatColor.WHITE + name;
        }
    }
}