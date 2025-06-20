package com.example.screenshare.managers;

import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;
import com.example.screenshare.ScreenSharePlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FreezeManager {

    private final ScreenSharePlugin plugin;
    private final Set<UUID> frozenPlayers;

    public void handlePlayerReconnect(Player player) {
        if (frozenPlayers.contains(player.getUniqueId())) {
            freezePlayer(player);
            player.sendMessage(ChatColor.RED + "You are still frozen!");
        }
    }

    public FreezeManager(ScreenSharePlugin plugin) {
        this.plugin = plugin;
        this.frozenPlayers = new HashSet<>();
        startFreezeReminder();
    }

    public void freezePlayer(Player player) {
        frozenPlayers.add(player.getUniqueId());

        // Opzionale: salva la posizione originale del giocatore
        player.setWalkSpeed(0.0f);
        player.setFlySpeed(0.0f);
    }

    public void unfreezePlayer(Player player) {
        frozenPlayers.remove(player.getUniqueId());

        // Ripristina le velocità normali
        player.setWalkSpeed(0.2f);
        player.setFlySpeed(0.1f);
    }

    public boolean isFrozen(Player player) {
        return frozenPlayers.contains(player.getUniqueId());
    }

    public Set<UUID> getFrozenPlayers() {
        return new HashSet<>(frozenPlayers);
    }

    public void clearAllFrozen() {
        // Sblocca tutti i giocatori online che sono frozen
        for (UUID uuid : frozenPlayers) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.setWalkSpeed(0.2f);
                player.setFlySpeed(0.1f);
                player.sendMessage(ChatColor.GREEN + "You have been unfrozen (server reload)");
            }
        }
        frozenPlayers.clear();
    }

    private void startFreezeReminder() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (UUID uuid : frozenPlayers) {
                    Player player = plugin.getServer().getPlayer(uuid);
                    if (player != null && player.isOnline()) {
                        player.sendMessage(ChatColor.RED + "You are currently frozen!");
                        player.sendMessage(ChatColor.YELLOW + "Do not disconnect or log out!");
                    }
                }
            }
        }.runTaskTimer(plugin, 20 * 30, 20 * 30); // Ogni 30 secondi
    }

    public void removeOfflinePlayer(UUID uuid) {
        frozenPlayers.remove(uuid);
    }
}