package com.example.screenshare.commands;

import com.example.screenshare.ScreenSharePlugin;
import com.example.screenshare.utils.MessageUtils;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public class DupeIPCommand implements CommandExecutor, TabCompleter {

    private final ScreenSharePlugin plugin;
    private final Map<String, Set<String>> ipToPlayers;
    private final Map<String, String> playerToIP;

    public DupeIPCommand(ScreenSharePlugin plugin) {
        this.plugin = plugin;
        this.ipToPlayers = new HashMap<>();
        this.playerToIP = new HashMap<>();
        loadIPData();
    }

    private void loadIPData() {
        // Load IP data from server logs or player data
        // This is a simplified version - in a real implementation you might want to use a database

        // Try to load from Bukkit's player data
        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            if (offlinePlayer.hasPlayedBefore()) {
                // Get player's last known IP (this requires server access to player data)
                String playerName = offlinePlayer.getName();
                if (playerName != null) {
                    // For online players, we can get their current IP
                    if (offlinePlayer.isOnline()) {
                        Player onlinePlayer = offlinePlayer.getPlayer();
                        if (onlinePlayer != null) {
                            String ip = onlinePlayer.getAddress().getAddress().getHostAddress();
                            addPlayerIP(playerName, ip);
                        }
                    }
                }
            }
        }

        // Try to load from server logs (simplified approach)
        loadFromServerLogs();
    }

    private void loadFromServerLogs() {
        try {
            File logsDir = new File("logs");
            if (logsDir.exists() && logsDir.isDirectory()) {
                File latestLog = new File(logsDir, "latest.log");
                if (latestLog.exists()) {
                    List<String> lines = Files.readAllLines(latestLog.toPath());
                    for (String line : lines) {
                        // Look for login patterns in logs
                        // Example: [INFO]: Player[/IP:PORT] logged in
                        if (line.contains("logged in") && line.contains("[/")) {
                            try {
                                String[] parts = line.split("\\[/");
                                if (parts.length >= 2) {
                                    String playerPart = parts[0];
                                    String ipPart = parts[1].split(":")[0]; // Remove port

                                    // Extract player name
                                    String playerName = playerPart.substring(playerPart.lastIndexOf(" ") + 1);
                                    addPlayerIP(playerName, ipPart);
                                }
                            } catch (Exception e) {
                                // Ignore parsing errors
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Could not load IP data from logs: " + e.getMessage());
        }
    }

    private void addPlayerIP(String playerName, String ip) {
        playerToIP.put(playerName.toLowerCase(), ip);
        ipToPlayers.computeIfAbsent(ip, k -> new HashSet<>()).add(playerName);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("screenshare.dupeip")) {
            sender.sendMessage(MessageUtils.error("You don't have permission to use this command!"));
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(MessageUtils.error("Usage: /dupeip <player>"));
            return true;
        }

        String targetName = args[0];

        // Get target player's IP
        String targetIP = null;

        // First check if player is online
        Player onlinePlayer = Bukkit.getPlayer(targetName);
        if (onlinePlayer != null) {
            targetIP = onlinePlayer.getAddress().getAddress().getHostAddress();
            addPlayerIP(onlinePlayer.getName(), targetIP); // Update our records
        } else {
            // Check our stored data
            targetIP = playerToIP.get(targetName.toLowerCase());
        }

        if (targetIP == null) {
            sender.sendMessage(MessageUtils.error("No IP data found for player '" + targetName + "'"));
            sender.sendMessage(ChatColor.GRAY + "Note: IP data is only available for players who have joined recently or are currently online.");
            return true;
        }

        // Get all players with the same IP
        Set<String> playersWithSameIP = ipToPlayers.get(targetIP);

        if (playersWithSameIP == null || playersWithSameIP.isEmpty()) {
            sender.sendMessage(MessageUtils.info("No duplicate accounts found for " + targetName));
            return true;
        }

        // Remove the target player from the list for cleaner display
        Set<String> duplicateAccounts = new HashSet<>(playersWithSameIP);
        duplicateAccounts.remove(targetName);

        if (duplicateAccounts.isEmpty()) {
            sender.sendMessage(MessageUtils.info("No duplicate accounts found for " + targetName));
            return true;
        }

        // Display results
        sender.sendMessage(ChatColor.GOLD + "=== Duplicate IP Check for " + targetName + " ===");
        sender.sendMessage(ChatColor.YELLOW + "IP Address: " + ChatColor.WHITE + targetIP);
        sender.sendMessage(ChatColor.YELLOW + "Total Accounts: " + ChatColor.WHITE + playersWithSameIP.size());
        sender.sendMessage("");
        sender.sendMessage(ChatColor.YELLOW + "Associated Accounts:");

        BanList banList = Bukkit.getBanList(BanList.Type.NAME);

        // Sort accounts alphabetically
        List<String> sortedAccounts = new ArrayList<>(duplicateAccounts);
        sortedAccounts.sort(String.CASE_INSENSITIVE_ORDER);

        for (String accountName : sortedAccounts) {
            boolean isBanned = banList.isBanned(accountName);
            ChatColor nameColor = isBanned ? ChatColor.RED : ChatColor.GREEN;
            String status = isBanned ? " [BANNED]" : " [CLEAN]";

            sender.sendMessage("  • " + nameColor + accountName + ChatColor.GRAY + status);
        }

        sender.sendMessage("");
        sender.sendMessage(ChatColor.GOLD + "Legend: " + ChatColor.GREEN + "Clean Account " +
                ChatColor.GRAY + "| " + ChatColor.RED + "Banned Account");
        sender.sendMessage(ChatColor.GOLD + "=====================================");

        // Log the command usage
        String logMessage = String.format("DUPEIP - Staff: %s, Target: %s, IP: %s, Duplicates: %d",
                sender.getName(), targetName, targetIP, duplicateAccounts.size());
        plugin.getLogger().info(logMessage);

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Include online players
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(player.getName());
                }
            }

            // Include players from our IP records
            for (String playerName : playerToIP.keySet()) {
                if (playerName.toLowerCase().startsWith(args[0].toLowerCase())) {
                    // Find the original case version
                    for (Set<String> players : ipToPlayers.values()) {
                        for (String name : players) {
                            if (name.toLowerCase().equals(playerName)) {
                                if (!completions.contains(name)) {
                                    completions.add(name);
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }

        return completions.stream().distinct().collect(Collectors.toList());
    }

    // Method to manually add IP data (can be called from other parts of the plugin)
    public void recordPlayerIP(String playerName, String ip) {
        addPlayerIP(playerName, ip);
    }

    // Method to get duplicate accounts for a player
    public Set<String> getDuplicateAccounts(String playerName) {
        String ip = playerToIP.get(playerName.toLowerCase());
        if (ip == null) return new HashSet<>();

        Set<String> accounts = ipToPlayers.get(ip);
        if (accounts == null) return new HashSet<>();

        Set<String> duplicates = new HashSet<>(accounts);
        duplicates.remove(playerName);
        return duplicates;
    }
}