package com.example.screenshare.commands;

import com.example.screenshare.ScreenSharePlugin;
import com.example.screenshare.utils.MessageUtils;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class TempBanCommand implements CommandExecutor, TabCompleter {

    private final ScreenSharePlugin plugin;
    private final DateTimeFormatter dateFormatter;

    public TempBanCommand(ScreenSharePlugin plugin) {
        this.plugin = plugin;
        this.dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("screenshare.tempban")) {
            sender.sendMessage(MessageUtils.error("You don't have permission to use this command!"));
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(MessageUtils.error("Usage: /tempban <player> <time> <reason> [-s]"));
            sender.sendMessage(ChatColor.YELLOW + "Time format: 1d, 2h, 30m, 1w (days, hours, minutes, weeks)");
            sender.sendMessage(ChatColor.YELLOW + "Add -s at the end for silent ban");
            return true;
        }

        String targetName = args[0];
        String timeString = args[1];
        boolean silent = false;

        // Check if last argument is -s for silent
        List<String> reasonArgs = new ArrayList<>();
        for (int i = 2; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("-s")) {
                silent = true;
            } else {
                reasonArgs.add(args[i]);
            }
        }

        if (reasonArgs.isEmpty()) {
            sender.sendMessage(MessageUtils.error("You must provide a reason for the ban!"));
            return true;
        }

        String reason = String.join(" ", reasonArgs);

        // Parse time
        long banDurationMillis = parseTime(timeString);
        if (banDurationMillis <= 0) {
            sender.sendMessage(MessageUtils.error("Invalid time format! Use: 1d, 2h, 30m, 1w"));
            return true;
        }

        // Get target player
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            sender.sendMessage(MessageUtils.error("Player '" + targetName + "' not found!"));
            return true;
        }

        if (target.equals(sender)) {
            sender.sendMessage(MessageUtils.error("You cannot ban yourself!"));
            return true;
        }

        // Check if target has higher permissions
        if (target.hasPermission("screenshare.tempban.exempt")) {
            sender.sendMessage(MessageUtils.error("You cannot ban this player!"));
            return true;
        }

        // Calculate expiration date
        Date expirationDate = new Date(System.currentTimeMillis() + banDurationMillis);

        // Create ban reason with staff info
        String staffName = sender instanceof Player ? sender.getName() : "Console";
        String fullReason = String.format("Banned by %s for: %s | Expires: %s",
                staffName, reason, dateFormatter.format(LocalDateTime.now().plusSeconds(banDurationMillis / 1000)));

        // Apply the ban
        Bukkit.getBanList(BanList.Type.NAME).addBan(target.getName(), fullReason, expirationDate, staffName);

        // End screenshare session if player is in one
        if (plugin.getSessionManager().isInSession(target)) {
            plugin.getSessionManager().endSession(target);
        }

        // Unfreeze if frozen
        if (plugin.getFreezeManager().isFrozen(target)) {
            plugin.getFreezeManager().unfreezePlayer(target);
        }

        // Kick the player
        String kickMessage = ChatColor.RED + "You have been temporarily banned!\n" +
                ChatColor.YELLOW + "Reason: " + ChatColor.WHITE + reason + "\n" +
                ChatColor.YELLOW + "Banned by: " + ChatColor.WHITE + staffName + "\n" +
                ChatColor.YELLOW + "Expires: " + ChatColor.WHITE +
                dateFormatter.format(LocalDateTime.now().plusSeconds(banDurationMillis / 1000)) + "\n" +
                ChatColor.GRAY + "Appeal at: your-website.com";

        target.kickPlayer(kickMessage);

        // Log the ban
        String logMessage = String.format("TEMPBAN - Player: %s, Staff: %s, Duration: %s, Reason: %s, Silent: %s",
                target.getName(), staffName, timeString, reason, silent);
        plugin.getLogger().info(logMessage);

        // Send confirmation to staff
        String timeFormatted = formatDuration(banDurationMillis);
        sender.sendMessage(MessageUtils.success("Successfully banned " + target.getName() + " for " + timeFormatted));
        sender.sendMessage(ChatColor.GRAY + "Reason: " + reason);

        // Broadcast ban if not silent
        if (!silent) {
            String broadcastMessage = ChatColor.RED + target.getName() + ChatColor.YELLOW + " has been temporarily banned for " +
                    ChatColor.WHITE + timeFormatted + ChatColor.YELLOW + " (" + reason + ")";

            for (Player online : Bukkit.getOnlinePlayers()) {
                if (online.hasPermission("screenshare.tempban.notify")) {
                    online.sendMessage(broadcastMessage);
                }
            }

            // Also send to console
            Bukkit.getConsoleSender().sendMessage(broadcastMessage);
        } else {
            // Silent ban notification only to staff
            String silentMessage = ChatColor.GRAY + "[SILENT] " + ChatColor.RED + target.getName() +
                    ChatColor.YELLOW + " has been temporarily banned for " + ChatColor.WHITE + timeFormatted +
                    ChatColor.YELLOW + " (" + reason + ")";

            for (Player online : Bukkit.getOnlinePlayers()) {
                if (online.hasPermission("screenshare.tempban.notify")) {
                    online.sendMessage(silentMessage);
                }
            }
        }

        return true;
    }

    private long parseTime(String timeString) {
        if (timeString == null || timeString.isEmpty()) {
            return -1;
        }

        timeString = timeString.toLowerCase().trim();

        try {
            char unit = timeString.charAt(timeString.length() - 1);
            String numberPart = timeString.substring(0, timeString.length() - 1);
            long number = Long.parseLong(numberPart);

            switch (unit) {
                case 'm': // minutes
                    return TimeUnit.MINUTES.toMillis(number);
                case 'h': // hours
                    return TimeUnit.HOURS.toMillis(number);
                case 'd': // days
                    return TimeUnit.DAYS.toMillis(number);
                case 'w': // weeks
                    return TimeUnit.DAYS.toMillis(number * 7);
                default:
                    return -1;
            }
        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            return -1;
        }
    }

    private String formatDuration(long millis) {
        long days = TimeUnit.MILLISECONDS.toDays(millis);
        long hours = TimeUnit.MILLISECONDS.toHours(millis) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;

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

        return sb.length() > 0 ? sb.toString() : "0 minutes";
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Player names
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(player.getName());
                }
            }
        } else if (args.length == 2) {
            // Time suggestions
            String[] timeExamples = {"1d", "2d", "7d", "30d", "1h", "2h", "12h", "1w", "2w"};
            for (String time : timeExamples) {
                if (time.startsWith(args[1].toLowerCase())) {
                    completions.add(time);
                }
            }
        } else if (args.length == 3) {
            // Common reasons
            String[] reasons = {"cheating", "hacking", "toxicity", "griefing", "spam", "inappropriate behavior"};
            for (String reason : reasons) {
                if (reason.toLowerCase().startsWith(args[2].toLowerCase())) {
                    completions.add(reason);
                }
            }
        } else if (args.length > 3) {
            // Silent flag
            if ("-s".startsWith(args[args.length - 1].toLowerCase())) {
                completions.add("-s");
            }
        }

        return completions;
    }
}