package com.example.screenshare.managers;

import com.example.screenshare.ScreenSharePlugin;
import com.example.screenshare.models.ScreenShareSession;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.GameMode;
import org.bukkit.*;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SessionManager {

    private final ScreenSharePlugin plugin;
    private final Map<UUID, ScreenShareSession> activeSessions;
    private final DateTimeFormatter timeFormatter;

    public SessionManager(ScreenSharePlugin plugin) {
        this.plugin = plugin;
        this.activeSessions = new HashMap<>();
        this.timeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    }

    public boolean startSession(Player suspect, Player staff) {
        if (isInSession(suspect)) {
            return false;
        }

        // Salva posizione originale del sospetto
        var originalSuspectLocation = suspect.getLocation().clone();

        // Salva posizione originale dello staff
        var originalStaffLocation = staff.getLocation().clone();

        // Crea sessione
        var session = new ScreenShareSession(suspect, staff, originalSuspectLocation, originalStaffLocation);
        activeSessions.put(suspect.getUniqueId(), session);

        // Teletrasporta il sospetto e lo staff
        var ssWorld = plugin.getWorldManager().getScreenShareWorld();
        if (ssWorld != null) {
            // Teletrasporta il sospetto alla sua posizione spawn
            Location targetSpawn = plugin.getWorldManager().getTargetSpawn();
            suspect.teleport(targetSpawn);
            suspect.setGameMode(GameMode.ADVENTURE);

            // Teletrasporta lo staff alla sua posizione spawn
            Location staffSpawn = plugin.getWorldManager().getStaffSpawn();
            staff.teleport(staffSpawn);
        }

        // Create and show scoreboard
        plugin.getScoreboardManager().createSessionScoreboard(staff, suspect, session);

        // Log avvio sessione
        String logMessage = String.format("SS STARTED - Suspect: %s, Staff: %s, Data: %s",
                suspect.getName(), staff.getName(), session.getStartTime().format(timeFormatter));
        plugin.getLogger().info(logMessage);

        // Messaggi di conferma
        staff.sendMessage(ChatColor.GREEN + "Screenshare session started with " + suspect.getName());
        staff.sendMessage(ChatColor.YELLOW + "You have been teleported to the staff spawn location.");
        staff.sendMessage(ChatColor.GOLD + "Check the scoreboard on the right for session information!");

        suspect.sendMessage(ChatColor.YELLOW + "You were taken to screenshare by " + staff.getName());
        suspect.sendMessage(ChatColor.RED + "DO NOT DISCONNECT or you will be banned!");
        suspect.sendMessage(ChatColor.GOLD + "Check the scoreboard on the right for instructions!");

        // Invia interfaccia staff con pulsanti cliccabili
        sendStaffInterface(staff, suspect);

        return true;
    }

    public boolean endSession(UUID suspectId) {
        var session = activeSessions.remove(suspectId);
        if (session == null) {
            return false;
        }

        var suspect = Bukkit.getPlayer(suspectId);
        var staff = Bukkit.getPlayer(session.getStaffId());

        // Remove scoreboard
        plugin.getScoreboardManager().removeSessionScoreboard(suspectId, session.getStaffId());

        if (suspect != null && suspect.isOnline()) {
            suspect.setGameMode(GameMode.SURVIVAL);
            // Teletrasporta il sospetto alla posizione originale
            suspect.teleport(session.getOriginalSuspectLocation());
            suspect.sendMessage(ChatColor.GREEN + "Screenshare finished, you have been returned to your original location.");
        }

        // Teletrasporta lo staff alla sua posizione originale
        if (staff != null && staff.isOnline()) {
            staff.teleport(session.getOriginalStaffLocation());
            staff.sendMessage(ChatColor.GREEN + "Screenshare finished with " + session.getSuspectName());
            staff.sendMessage(ChatColor.YELLOW + "You have been returned to your original position.");
        }

        // Log termine sessione
        String logMessage = String.format("SS FINISHED - Suspect: %s, Staff: %s, Data: %s",
                session.getSuspectName(), session.getStaffName(),
                java.time.LocalDateTime.now().format(timeFormatter));
        plugin.getLogger().info(logMessage);

        return true;
    }

    public boolean endSession(Player suspect) {
        return endSession(suspect.getUniqueId());
    }

    public boolean isInSession(Player player) {
        return activeSessions.containsKey(player.getUniqueId());
    }

    public ScreenShareSession getSession(Player player) {
        return activeSessions.get(player.getUniqueId());
    }

    public boolean isStaffInSession(Player staff) {
        return activeSessions.values().stream()
                .anyMatch(session -> session.getStaffId().equals(staff.getUniqueId()));
    }

    public Map<UUID, ScreenShareSession> getActiveSessions() {
        return activeSessions;
    }

    public void endAllSessions() {
        var sessionsCopy = new HashMap<>(activeSessions);
        for (var suspectId : sessionsCopy.keySet()) {
            endSession(suspectId);
        }
    }

    private void sendStaffInterface(Player staff, Player suspect) {
        staff.sendMessage(ChatColor.GOLD + "=== SCREENSHARE INTERFACE ===");
        staff.sendMessage(ChatColor.WHITE + "Click the buttons to execute the commands:");
        staff.sendMessage("");

        // Pulsante CHEATING - Ban 30 giorni
        TextComponent cheatingButton = new TextComponent("[CHEATING]");
        cheatingButton.setColor(net.md_5.bungee.api.ChatColor.RED);
        cheatingButton.setBold(true);

        String cheatingCommand = "/tempban " + suspect.getName() + " 30d cheating -s";
        cheatingButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, cheatingCommand));
        cheatingButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("Click to ban for cheating (30 days)")
                        .color(net.md_5.bungee.api.ChatColor.RED).create()));

        TextComponent cheatingText = new TextComponent(" - 30 day ban for cheating");
        cheatingText.setColor(net.md_5.bungee.api.ChatColor.GRAY);

        // Pulsante ADMITTING - Ban 15 giorni
        TextComponent admittingButton = new TextComponent("[ADMITTING]");
        admittingButton.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
        admittingButton.setBold(true);

        String admittingCommand = "/tempban " + suspect.getName() + " 15d admit to cheat -s";
        admittingButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, admittingCommand));
        admittingButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("Click to ban for admission (15 days)")
                        .color(net.md_5.bungee.api.ChatColor.YELLOW).create()));

        TextComponent admittingText = new TextComponent(" - 15 day ban for admission");
        admittingText.setColor(net.md_5.bungee.api.ChatColor.GRAY);

        // Pulsante CLEAR - Termina screenshare
        TextComponent clearButton = new TextComponent("[CLEAR]");
        clearButton.setColor(net.md_5.bungee.api.ChatColor.GREEN);
        clearButton.setBold(true);

        String clearCommand = "/ss end " + suspect.getName();
        clearButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, clearCommand));
        clearButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("Click to end the session")
                        .color(net.md_5.bungee.api.ChatColor.GREEN).create()));

        TextComponent clearText = new TextComponent(" - End screenshare");
        clearText.setColor(net.md_5.bungee.api.ChatColor.GRAY);

        // Pulsante FREEZE - Congela il giocatore
        TextComponent freezeButton = new TextComponent("[FREEZE]");
        freezeButton.setColor(net.md_5.bungee.api.ChatColor.AQUA);
        freezeButton.setBold(true);

        String freezeCommand = "/freeze " + suspect.getName();
        freezeButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, freezeCommand));
        freezeButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("Click to freeze/unfreeze the player")
                        .color(net.md_5.bungee.api.ChatColor.AQUA).create()));

        TextComponent freezeText = new TextComponent(" - Freeze/Unfreeze player");
        freezeText.setColor(net.md_5.bungee.api.ChatColor.GRAY);

        // Invia i componenti
        TextComponent prefix = new TextComponent("• ");
        prefix.setColor(net.md_5.bungee.api.ChatColor.WHITE);

        // Riga CHEATING
        TextComponent cheatingLine = new TextComponent(prefix);
        cheatingLine.addExtra(cheatingButton);
        cheatingLine.addExtra(cheatingText);
        staff.spigot().sendMessage(cheatingLine);

        // Riga ADMITTING
        TextComponent admittingLine = new TextComponent(prefix);
        admittingLine.addExtra(admittingButton);
        admittingLine.addExtra(admittingText);
        staff.spigot().sendMessage(admittingLine);

        // Riga CLEAR
        TextComponent clearLine = new TextComponent(prefix);
        clearLine.addExtra(clearButton);
        clearLine.addExtra(clearText);
        staff.spigot().sendMessage(clearLine);

        // Riga FREEZE
        TextComponent freezeLine = new TextComponent(prefix);
        freezeLine.addExtra(freezeButton);
        freezeLine.addExtra(freezeText);
        staff.spigot().sendMessage(freezeLine);

        staff.sendMessage("");
        staff.sendMessage(ChatColor.GOLD + "===============================");

        // Invia anche i comandi in formato testo per riferimento
        staff.sendMessage(ChatColor.DARK_GRAY + "Comandi di riferimento:");
        staff.sendMessage(ChatColor.DARK_GRAY + "• " + cheatingCommand);
        staff.sendMessage(ChatColor.DARK_GRAY + "• " + admittingCommand);
        staff.sendMessage(ChatColor.DARK_GRAY + "• " + clearCommand);
        staff.sendMessage(ChatColor.DARK_GRAY + "• " + freezeCommand);
    }
}