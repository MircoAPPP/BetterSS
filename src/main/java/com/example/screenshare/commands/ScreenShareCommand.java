package com.example.screenshare.commands;

import com.example.screenshare.ScreenSharePlugin;
import com.example.screenshare.utils.MessageUtils;
import com.example.screenshare.managers.WorldManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ScreenShareCommand implements CommandExecutor, TabCompleter {

    private final ScreenSharePlugin plugin;
    private final WorldManager worldManager;

    public ScreenShareCommand(ScreenSharePlugin plugin) {
        this.plugin = plugin;
        this.worldManager = new WorldManager(plugin);
        this.worldManager.createWorldInstructions(); // Create custom world instructions
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player staff)) {
            sender.sendMessage(MessageUtils.error("This command can only be executed by a player!"));
            return true;
        }

        if (!staff.hasPermission("ss.use")) {
            staff.sendMessage(MessageUtils.error("You don't have permission to use this command!"));
            return true;
        }

        if (args.length == 0) {
            sendUsage(staff);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "end":
                return handleEndCommand(staff, args);
            default:
                return handleStartCommand(staff, args);
        }
    }

    private boolean handleStartCommand(Player staff, String[] args) {
        if (args.length != 1) {
            staff.sendMessage(MessageUtils.error("Usage: /ss <player>"));
            return true;
        }

        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);

        if (target == null || !target.isOnline()) {
            staff.sendMessage(MessageUtils.error("Player not found or offline!"));
            return true;
        }

        if (target.equals(staff)) {
            staff.sendMessage(MessageUtils.error("You can't screenshare yourself!"));
            return true;
        }

        if (plugin.getSessionManager().isInSession(target)) {
            staff.sendMessage(MessageUtils.error("The player is already in a screenshare session!"));
            return true;
        }

        if (plugin.getSessionManager().isStaffInSession(staff)) {
            staff.sendMessage(MessageUtils.error("You already have an active screenshare session!"));
            return true;
        }

        // Load/create screenshare world
        worldManager.createScreenShareWorld();

        // Check if the world was created correctly
        if (worldManager.getScreenShareWorld() == null) {
            staff.sendMessage(MessageUtils.error("Error while creating the screenshare world!"));
            return true;
        }

        // Start session
        boolean success = plugin.getSessionManager().startSession(target, staff);

        if (!success) {
            staff.sendMessage(MessageUtils.error("Could not start the screenshare session!"));
        } else {
            staff.sendMessage(MessageUtils.success("Screenshare session started successfully!"));
        }

        return true;
    }

    private boolean handleEndCommand(Player staff, String[] args) {
        if (args.length != 2) {
            staff.sendMessage(MessageUtils.error("Usage: /ss end <player>"));
            return true;
        }

        String targetName = args[1];
        Player target = Bukkit.getPlayer(targetName);

        if (target == null) {
            staff.sendMessage(MessageUtils.error("Player not found!"));
            return true;
        }

        if (!plugin.getSessionManager().isInSession(target)) {
            staff.sendMessage(MessageUtils.error("The player is not in a screenshare session!"));
            return true;
        }

        var session = plugin.getSessionManager().getSession(target);
        if (session != null && !session.getStaffId().equals(staff.getUniqueId()) && !staff.hasPermission("ss.admin")) {
            staff.sendMessage(MessageUtils.error("You can only end the sessions you started!"));
            return true;
        }

        boolean success = plugin.getSessionManager().endSession(target);

        if (!success) {
            staff.sendMessage(MessageUtils.error("Could not end the screenshare session!"));
        } else {
            staff.sendMessage(MessageUtils.success("Screenshare session ended successfully!"));
        }

        return true;
    }

    private void sendUsage(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== ScreenShare Plugin ===");
        player.sendMessage(ChatColor.YELLOW + "/ss <player>" + ChatColor.WHITE + " - Start screenshare");
        player.sendMessage(ChatColor.YELLOW + "/ss end <player>" + ChatColor.WHITE + " - End screenshare");
        player.sendMessage(ChatColor.GOLD + "=========================");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("end");
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(player.getName());
                }
            }
        } else if (args.length == 2 && "end".equalsIgnoreCase(args[0])) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (plugin.getSessionManager().isInSession(player) &&
                        player.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                    completions.add(player.getName());
                }
            }
        }

        return completions;
    }
}
