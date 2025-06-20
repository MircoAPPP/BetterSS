package com.example.screenshare.commands;

import com.example.screenshare.ScreenSharePlugin;
import com.example.screenshare.utils.MessageUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SetSpawnCommand implements CommandExecutor, TabCompleter {

    private final ScreenSharePlugin plugin;

    public SetSpawnCommand(ScreenSharePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtils.error("This command can only be executed by a player!"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("screenshare.setspawn")) {
            player.sendMessage(MessageUtils.error("You don't have permission to use this command!"));
            return true;
        }

        if (args.length == 0) {
            sendUsage(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "staff":
                return handleSetStaffSpawn(player);
            case "target":
            case "suspect":
                return handleSetTargetSpawn(player);
            case "both":
                return handleSetBothSpawn(player);
            case "info":
                return handleShowInfo(player);
            case "reset":
                return handleReset(player);
            default:
                sendUsage(player);
                return true;
        }
    }

    private boolean handleSetStaffSpawn(Player player) {
        // Check if player is in screenshare world
        if (!plugin.getWorldManager().isScreenShareWorld(player.getWorld())) {
            player.sendMessage(MessageUtils.error("You must be in the screenshare world to set spawn locations!"));
            player.sendMessage(ChatColor.YELLOW + "Use /ss <player> to enter the screenshare world first.");
            return true;
        }

        Location location = player.getLocation();
        plugin.getWorldManager().setStaffSpawn(location);

        player.sendMessage(MessageUtils.success("Staff spawn location set successfully!"));
        player.sendMessage(ChatColor.GRAY + "Location: " + formatLocation(location));

        return true;
    }

    private boolean handleSetTargetSpawn(Player player) {
        // Check if player is in screenshare world
        if (!plugin.getWorldManager().isScreenShareWorld(player.getWorld())) {
            player.sendMessage(MessageUtils.error("You must be in the screenshare world to set spawn locations!"));
            player.sendMessage(ChatColor.YELLOW + "Use /ss <player> to enter the screenshare world first.");
            return true;
        }

        Location location = player.getLocation();
        plugin.getWorldManager().setTargetSpawn(location);

        player.sendMessage(MessageUtils.success("Target spawn location set successfully!"));
        player.sendMessage(ChatColor.GRAY + "Location: " + formatLocation(location));

        return true;
    }

    private boolean handleSetBothSpawn(Player player) {
        // Check if player is in screenshare world
        if (!plugin.getWorldManager().isScreenShareWorld(player.getWorld())) {
            player.sendMessage(MessageUtils.error("You must be in the screenshare world to set spawn locations!"));
            player.sendMessage(ChatColor.YELLOW + "Use /ss <player> to enter the screenshare world first.");
            return true;
        }

        Location location = player.getLocation();
        plugin.getWorldManager().setStaffSpawn(location);
        plugin.getWorldManager().setTargetSpawn(location);

        player.sendMessage(MessageUtils.success("Both staff and target spawn locations set successfully!"));
        player.sendMessage(ChatColor.GRAY + "Location: " + formatLocation(location));

        return true;
    }

    private boolean handleShowInfo(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== ScreenShare Spawn Information ===");

        Location staffSpawn = plugin.getWorldManager().getStaffSpawn();
        Location targetSpawn = plugin.getWorldManager().getTargetSpawn();

        if (staffSpawn != null) {
            player.sendMessage(ChatColor.YELLOW + "Staff Spawn: " + ChatColor.WHITE + formatLocation(staffSpawn));
        } else {
            player.sendMessage(ChatColor.YELLOW + "Staff Spawn: " + ChatColor.RED + "Not set (using world spawn)");
        }

        if (targetSpawn != null) {
            player.sendMessage(ChatColor.YELLOW + "Target Spawn: " + ChatColor.WHITE + formatLocation(targetSpawn));
        } else {
            player.sendMessage(ChatColor.YELLOW + "Target Spawn: " + ChatColor.RED + "Not set (using world spawn)");
        }

        player.sendMessage(ChatColor.GOLD + "=====================================");

        return true;
    }

    private boolean handleReset(Player player) {
        plugin.getWorldManager().resetSpawnLocations();
        player.sendMessage(MessageUtils.success("All spawn locations have been reset to world spawn!"));
        return true;
    }

    private void sendUsage(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== ScreenShare SetSpawn Commands ===");
        player.sendMessage(ChatColor.YELLOW + "/ssspawn staff" + ChatColor.WHITE + " - Set staff spawn location");
        player.sendMessage(ChatColor.YELLOW + "/ssspawn target" + ChatColor.WHITE + " - Set target spawn location");
        player.sendMessage(ChatColor.YELLOW + "/ssspawn both" + ChatColor.WHITE + " - Set both spawns to current location");
        player.sendMessage(ChatColor.YELLOW + "/ssspawn info" + ChatColor.WHITE + " - Show current spawn locations");
        player.sendMessage(ChatColor.YELLOW + "/ssspawn reset" + ChatColor.WHITE + " - Reset all spawns to world spawn");
        player.sendMessage(ChatColor.GOLD + "====================================");
        player.sendMessage(ChatColor.GRAY + "Note: You must be in the screenshare world to set spawn locations!");
    }

    private String formatLocation(Location loc) {
        return String.format("X: %.1f, Y: %.1f, Z: %.1f, Yaw: %.1f, Pitch: %.1f",
                loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subCommands = Arrays.asList("staff", "target", "suspect", "both", "info", "reset");
            for (String subCommand : subCommands) {
                if (subCommand.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(subCommand);
                }
            }
        }

        return completions;
    }
}