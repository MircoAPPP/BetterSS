package com.example.screenshare.commands;

import com.example.screenshare.ScreenSharePlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class freeze implements CommandExecutor {

    private final ScreenSharePlugin plugin;

    public freeze(ScreenSharePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        // Controlla i permessi
        if (!player.hasPermission("screenshare.freeze")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /freeze <player>");
            return true;
        }

        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);

        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player '" + targetName + "' not found!");
            return true;
        }

        if (target.equals(player)) {
            player.sendMessage(ChatColor.RED + "You cannot freeze yourself!");
            return true;
        }

        // Toggle freeze status
        if (plugin.getFreezeManager().isFrozen(target)) {
            // Unfreeze player
            plugin.getFreezeManager().unfreezePlayer(target);
            target.sendMessage(ChatColor.GREEN + "You have been unfrozen by " + player.getName());
            player.sendMessage(ChatColor.GREEN + "You have unfrozen " + target.getName());
        } else {
            // Freeze player
            plugin.getFreezeManager().freezePlayer(target);
            target.sendMessage(ChatColor.RED + "You have been frozen by " + player.getName());
            target.sendMessage(ChatColor.YELLOW + "Do not disconnect or you will be banned!");
            player.sendMessage(ChatColor.GREEN + "You have frozen " + target.getName());
        }

        return true;
    }
}