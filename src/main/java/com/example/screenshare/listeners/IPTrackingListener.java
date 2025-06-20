package com.example.screenshare.listeners;

import com.example.screenshare.ScreenSharePlugin;
import com.example.screenshare.commands.DupeIPCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class IPTrackingListener implements Listener {

    private final ScreenSharePlugin plugin;

    public IPTrackingListener(ScreenSharePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String ip = player.getAddress().getAddress().getHostAddress();

        // Record the player's IP for dupeip command
        if (plugin.getDupeIPCommand() != null) {
            plugin.getDupeIPCommand().recordPlayerIP(player.getName(), ip);
        }

        // Log for debugging (optional)
        plugin.getLogger().info("Player " + player.getName() + " joined from IP: " + ip);
    }
}