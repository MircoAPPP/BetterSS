package com.example.screenshare.listeners;

import com.example.screenshare.ScreenSharePlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerReconnectListener implements Listener {

    private final ScreenSharePlugin plugin;

    public PlayerReconnectListener(ScreenSharePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getFreezeManager().handlePlayerReconnect(player);
    }
}