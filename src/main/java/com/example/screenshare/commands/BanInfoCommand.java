package com.example.screenshare.commands;

import com.example.screenshare.ScreenSharePlugin;
import com.example.screenshare.utils.MessageUtils;
import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class BanInfoCommand implements CommandExecutor, TabCompleter {

    private final ScreenSharePlugin plugin;
    private final DateTimeFormatter dateFormatter;

    public BanInfoCommand(ScreenSharePlugin plugin) {
        this.plugin = plugin;
        this.dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("screenshare.baninfo")) {
            sender.sendMessage(MessageUtils.error("You don't have permission to use this command!"));
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(MessageUtils.error("Usage: /baninfo <player>"));
            return true;
        }

        String targetName = args[0];

        // Check if player is banned
        BanList banList = Bukkit.getBanList(BanList.Type.NAME);

        // Check if player is banned
        if (!banList.isBanned(targetName)) {
            sender.sendMessage(MessageUtils.info("Player '" + targetName + "' is not banned."));
            return true;
        }

        // Get ban entry
        BanEntry banEntry = banList.getBanEntry(targetName);

        if (banEntry == null) {
            sender.sendMessage(MessageUtils.error("Could not retrieve ban information for '" + targetName + "'"));
            return true;
        }

        // Display ban information
        sender.sendMessage(ChatColor.GOLD + "=== Ban Information for " + targetName + " ===");

        // Player name
        String bannedName = banEntry.getTarget();
        sender.sendMessage(ChatColor.YELLOW + "Player: " + ChatColor.WHITE + bannedName);

        // Ban reason
        String reason = banEntry.getReason();
        if (reason != null && !reason.trim().isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "Reason: " + ChatColor.WHITE + reason);
        } else {
            sender.sendMessage(ChatColor.YELLOW + "Reason: " + ChatColor.GRAY + "No reason specified");
        }

        // Banned by
        String source = banEntry.getSource();
        if (source != null && !source.trim().isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "Banned by: " + ChatColor.WHITE + source);
        } else {
            sender.sendMessage(ChatColor.YELLOW + "Banned by: " + ChatColor.GRAY + "Unknown");
        }

        // Ban date
        Date created = banEntry.getCreated();
        if (created != null) {
            LocalDateTime createdTime = LocalDateTime.ofInstant(created.toInstant(), ZoneId.systemDefault());
            sender.sendMessage(ChatColor.YELLOW + "Ban Date: " + ChatColor.WHITE +
                    dateFormatter.format(createdTime));
        } else {
            sender.sendMessage(ChatColor.YELLOW + "Ban Date: " + ChatColor.GRAY + "Unknown");
        }

        // Expiration date
        Date expiration = banEntry.getExpiration();
        if (expiration != null) {
            LocalDateTime expirationTime = LocalDateTime.ofInstant(expiration.toInstant(), ZoneId.systemDefault());
            sender.sendMessage(ChatColor.YELLOW + "Expires: " + ChatColor.WHITE +
                    dateFormatter.format(expirationTime));

            // Time remaining
            long timeRemaining = expiration.getTime() - System.currentTimeMillis();
            if (timeRemaining > 0) {
                String timeLeft = formatDuration(timeRemaining);
                sender.sendMessage(ChatColor.YELLOW + "Time Remaining: " + ChatColor.GREEN + timeLeft);
                sender.sendMessage(ChatColor.YELLOW + "Type: " + ChatColor.GOLD + "TEMPORARY BAN");
            } else {
                sender.sendMessage(ChatColor.YELLOW + "Status: " + ChatColor.RED + "Expired (should be unbanned)");
                sender.sendMessage(ChatColor.YELLOW + "Type: " + ChatColor.GOLD + "TEMPORARY BAN (EXPIRED)");
            }
        } else {
            sender.sendMessage(ChatColor.YELLOW + "Expires: " + ChatColor.RED + "Never");
            sender.sendMessage(ChatColor.YELLOW + "Type: " + ChatColor.RED + "PERMANENT BAN");
        }

        sender.sendMessage(ChatColor.GOLD + "=========================================");

        // Log the command usage
        String logMessage = String.format("BANINFO - Staff: %s, Target: %s, Banned: true",
                sender.getName(), targetName);
        plugin.getLogger().info(logMessage);

        return true;
    }

    private String formatDuration(long millis) {
        long days = millis / (24 * 60 * 60 * 1000);
        long hours = (millis % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000);
        long minutes = (millis % (60 * 60 * 1000)) / (60 * 1000);

        StringBuilder sb = new StringBuilder();

        if (days > 0) {
            sb.append(days).append(" day").append(days > 1 ? "s" : "");
        }
        if (hours > 0) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(hours).append(" hour").append(hours > 1 ? "s" : "");
        }
        if (minutes > 0) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(minutes).append(" minute").append(minutes > 1 ? "s" : "");
        }

        return sb.length() > 0 ? sb.toString() : "Less than 1 minute";
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Get banned players
            BanList banList = Bukkit.getBanList(BanList.Type.NAME);
            Set<BanEntry> banEntries = banList.getBanEntries();

            for (BanEntry entry : banEntries) {
                String name = entry.getTarget();
                if (name != null && name.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(name);
                }
            }

            // Also include online players for convenience
            for (Player player : Bukkit.getOnlinePlayers()) {
                String playerName = player.getName();
                if (playerName.toLowerCase().startsWith(args[0].toLowerCase()) &&
                        !completions.contains(playerName)) {
                    completions.add(playerName);
                }
            }
        }

        return completions;
    }
}