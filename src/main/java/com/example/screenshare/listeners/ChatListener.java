package com.example.screenshare.listeners;

import com.example.screenshare.ScreenSharePlugin;
import com.example.screenshare.models.ScreenShareSession;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class ChatListener implements Listener {

    private final ScreenSharePlugin plugin;

    public ChatListener(ScreenSharePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        // Controlla se il giocatore è in sessione come sospetto
        if (plugin.getSessionManager().isInSession(player)) {
            var session = plugin.getSessionManager().getSession(player);

            // Blocca chat pubblica per sospetti
            event.setCancelled(true);

            // Invia messaggio solo allo staff della sessione
            var staff = Bukkit.getPlayer(session.getStaffId());
            if (staff != null && staff.isOnline()) {
                String message = ChatColor.RED + "[SUSPECT] " + ChatColor.WHITE + player.getName() +
                        ChatColor.GRAY + ": " + event.getMessage();
                staff.sendMessage(message);

                // Conferma al sospetto che il messaggio è stato inviato
                player.sendMessage(ChatColor.GRAY + "[Message sent to staff] " + event.getMessage());
            } else {
                player.sendMessage(ChatColor.RED + "The staff is not available!");
            }

            return;
        }

        // Controlla se il giocatore è uno staff in sessione
        if (plugin.getSessionManager().isStaffInSession(player)) {
            // Trova la sessione dello staff per comunicare con il sospetto
            var session = plugin.getSessionManager().getActiveSessions().values().stream()
                    .filter(s -> s.getStaffId().equals(player.getUniqueId()))
                    .findFirst()
                    .orElse(null);

            if (session != null) {
                var suspect = Bukkit.getPlayer(session.getSuspectId());

                // Invia il messaggio dello staff al sospetto
                if (suspect != null && suspect.isOnline()) {
                    String staffMessage = ChatColor.BLUE + "[STAFF] " + ChatColor.WHITE + player.getName() +
                            ChatColor.GRAY + ": " + event.getMessage();
                    suspect.sendMessage(staffMessage);
                }

                // Modifica il formato per lo staff stesso
                String format = event.getFormat();
                format = ChatColor.BLUE + "[STAFF-SS] " + ChatColor.RESET + format;
                event.setFormat(format);

                // Cancella il messaggio dalla chat pubblica
                event.setCancelled(true);

                // Invia solo allo staff
                String formattedMessage = String.format(format, player.getDisplayName(), event.getMessage());
                player.sendMessage(formattedMessage);
            } else {
                // Staff normale non in sessione
                String format = event.getFormat();
                format = ChatColor.BLUE + "[STAFF] " + ChatColor.RESET + format;
                event.setFormat(format);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage().toLowerCase();

        // Se il giocatore è in sessione come sospetto
        if (plugin.getSessionManager().isInSession(player)) {
            // Permetti solo alcuni comandi essenziali
            if (isAllowedCommand(command)) {
                return;
            }

            // Blocca altri comandi
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot use this command during a screenshare session!");

            // Notifica lo staff
            var session = plugin.getSessionManager().getSession(player);
            var staff = Bukkit.getPlayer(session.getStaffId());
            if (staff != null && staff.isOnline()) {
                staff.sendMessage(ChatColor.YELLOW + "[INFO] " + player.getName() +
                        " attempted to use: " + event.getMessage());
            }
        }
    }

    private boolean isAllowedCommand(String command) {
        // Lista comandi permessi durante la screenshare
        String[] allowedCommands = {
                "/msg", "/tell", "/w", "/whisper", "/r", "/reply",
                "/help", "/list", "/who", "/ping"
        };

        for (String allowed : allowedCommands) {
            if (command.startsWith(allowed + " ") || command.equals(allowed)) {
                return true;
            }
        }

        return false;
    }
}