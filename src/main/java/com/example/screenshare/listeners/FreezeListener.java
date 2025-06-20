package com.example.screenshare.listeners;

import com.example.screenshare.ScreenSharePlugin;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.*;

public class FreezeListener implements Listener {

    private final ScreenSharePlugin plugin;

    public FreezeListener(ScreenSharePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (plugin.getFreezeManager().isFrozen(player)) {
            // Controlla se il giocatore ha effettivamente mosso la posizione (non solo la testa)
            if (event.getFrom().getX() != event.getTo().getX() ||
                    event.getFrom().getY() != event.getTo().getY() ||
                    event.getFrom().getZ() != event.getTo().getZ()) {

                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You cannot move while frozen!");
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (plugin.getFreezeManager().isFrozen(player)) {
            // Non rimuovere più il player dalla lista
            plugin.getLogger().info("Frozen player " + player.getName() + " disconnected!");

            // Opzionale: teletrasporta alla spawn location per prevenire bug
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!player.isOnline()) {
                    Location spawnLoc = plugin.getWorldManager().getScreenShareWorld().getSpawnLocation();
                    player.teleport(spawnLoc);
                }
            }, 1L);
        }
    }
    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        if (plugin.getFreezeManager().isFrozen(player)) {
            String command = event.getMessage().toLowerCase();

            // Lista di comandi permessi quando frozen
            if (!command.startsWith("/msg") &&
                    !command.startsWith("/tell") &&
                    !command.startsWith("/r") &&
                    !command.startsWith("/reply") &&
                    !command.startsWith("/helpop")) {

                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You cannot use commands while frozen!");
            }
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        if (plugin.getFreezeManager().isFrozen(player)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot drop items while frozen!");
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();

            if (plugin.getFreezeManager().isFrozen(player)) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You cannot use inventory while frozen!");
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();

            if (plugin.getFreezeManager().isFrozen(player)) {
                // Impedisce di chiudere l'inventario (opzionale)
                // Puoi commentare questa parte se vuoi permettere di chiudere l'inventario
                player.sendMessage(ChatColor.YELLOW + "You are frozen - stay where you are!");
            }
        }
    }
}