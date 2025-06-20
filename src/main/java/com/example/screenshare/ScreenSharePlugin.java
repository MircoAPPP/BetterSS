package com.example.screenshare;

import com.example.screenshare.commands.*;
import com.example.screenshare.listeners.ChatListener;
import com.example.screenshare.listeners.FreezeListener;
import com.example.screenshare.listeners.IPTrackingListener;
import com.example.screenshare.listeners.PlayerReconnectListener;
import com.example.screenshare.managers.FreezeManager;
import com.example.screenshare.managers.ScoreboardManager;
import com.example.screenshare.managers.SessionManager;
import com.example.screenshare.managers.WorldManager;
import com.example.screenshare.utils.ConfigManager;
import com.example.screenshare.utils.MessageUtils;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public final class ScreenSharePlugin extends JavaPlugin {

    private ConfigManager configManager;
    private SessionManager sessionManager;
    private WorldManager worldManager;
    private FreezeManager freezeManager;
    private ScoreboardManager scoreboardManager;
    private DupeIPCommand dupeIPCommand;

    @Override
    public void onLoad() {
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "--------------------------------------------------");
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "ScreenShare Plugin is loading...");
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Author: Scalamobile");
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Version: " + getDescription().getVersion());
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "--------------------------------------------------");
    }

    @Override
    public void onEnable() {
        // Initialize config manager first
        this.configManager = new ConfigManager(this);

        // Initialize MessageUtils with plugin instance
        MessageUtils.setPlugin(this);

        // Initialize managers
        this.sessionManager = new SessionManager(this);
        this.worldManager = new WorldManager(this);
        this.freezeManager = new FreezeManager(this);
        this.scoreboardManager = new ScoreboardManager(this);
        this.dupeIPCommand = new DupeIPCommand(this);

        // Register commands
        getCommand("ss").setExecutor(new ScreenShareCommand(this));
        getCommand("freeze").setExecutor(new freeze(this));
        getCommand("tempban").setExecutor(new TempBanCommand(this));
        getCommand("ssspawn").setExecutor(new SetSpawnCommand(this));
        getCommand("baninfo").setExecutor(new BanInfoCommand(this));
        getCommand("dupeip").setExecutor(dupeIPCommand);

        // Register listeners
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new FreezeListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerReconnectListener(this), this);
        getServer().getPluginManager().registerEvents(new IPTrackingListener(this), this);

        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "--------------------------------------------------");
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "ScreenShare Plugin enabled successfully!");
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Author: Scalamobile");
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Version: " + getDescription().getVersion());
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "--------------------------------------------------");
    }

    @Override
    public void onDisable() {
        // Cleanup
        if (sessionManager != null) {
            sessionManager.endAllSessions();
        }

        if (freezeManager != null) {
            freezeManager.clearAllFrozen();
        }

        if (scoreboardManager != null) {
            scoreboardManager.cleanup();
        }

        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "ScreenShare plugin disabled!");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public WorldManager getWorldManager() {
        return worldManager;
    }

    public FreezeManager getFreezeManager() {
        return freezeManager;
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    public DupeIPCommand getDupeIPCommand() {
        return dupeIPCommand;
    }
}