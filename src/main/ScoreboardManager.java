package com.example.screenshare.managers;

import com.example.screenshare.ScreenSharePlugin;
import com.example.screenshare.models.ScreenShareSession;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScoreboardManager {

    private final ScreenSharePlugin plugin;
    private final Map<UUID, Scoreboard> playerScoreboards;
    private final Map<UUID, BukkitRunnable> updateTasks;
    private final DateTimeFormatter timeFormatter;

    public ScoreboardManager(ScreenSharePlugin plugin) {
        this.plugin = plugin;
        this.playerScoreboards = new HashMap<>();
        this.updateTasks = new HashMap<>();
        this.timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    }

    public void createSessionScoreboard(Player staff, Player suspect, ScreenShareSession session) {
        // Create scoreboard for staff
        createScoreboardForPlayer(staff, session, true);

        // Create scoreboard for suspect
        createScoreboardForPlayer(suspect, session, false);

        // Start update task
        startUpdateTask(session);
    }

    private void createScoreboardForPlayer(Player player, ScreenShareSession session, boolean isStaff) {
        ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
        Scoreboard scoreboard = scoreboardManager.getNewScoreboard();

        Objective objective = scoreboard.registerNewObjective("screenshare", "dummy",
                ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        // Store the scoreboard
        playerScoreboards.put(player.getUniqueId(), scoreboard);

        // Set the scoreboard for the player
        player.setScoreboard(scoreboard);

        // Update the scoreboard content
        updateScoreboardContent(player, session, isStaff);
    }

    private void updateScoreboardContent(Player player, ScreenShareSession session, boolean isStaff) {
        Scoreboard scoreboard = playerScoreboards.get(player.getUniqueId());
        if (scoreboard == null) return;

        Objective objective = scoreboard.getObjective("screenshare");
        if (objective == null) return;

        // Clear existing scores
        for (String entry : scoreboard.getEntries()) {
            scoreboard.resetScores(entry);
        }

        // Calculate session duration
        Duration duration = Duration.between(session.getStartTime(), LocalDateTime.now());
        String durationStr = formatDuration(duration);

        // Get players
        Player staff = Bukkit.getPlayer(session.getStaffId());
        Player suspect = Bukkit.getPlayer(session.getSuspectId());

        int line = 15;

        // Header
        objective.getScore(ChatColor.GOLD + "┃ " + ChatColor.BOLD + "SCREENSHARE INFO" + ChatColor.GOLD + " ┃").setScore(line--);
        objective.getScore(ChatColor.GRAY + "").setScore(line--);

        // Session Info
        objective.getScore(ChatColor.YELLOW + "⏰ Duration: " + ChatColor.WHITE + durationStr).setScore(line--);
        objective.getScore(ChatColor.YELLOW + "📅 Started: " + ChatColor.WHITE +
                session.getStartTime().format(timeFormatter)).setScore(line--);
        objective.getScore(" ").setScore(line--);

        // Staff Info
        String staffStatus = (staff != null && staff.isOnline()) ?
                ChatColor.GREEN + "●" : ChatColor.RED + "●";
        objective.getScore(ChatColor.BLUE + "👮 Staff: " + staffStatus + ChatColor.WHITE + " " +
                session.getStaffName()).setScore(line--);

        if (staff != null && staff.isOnline()) {
            objective.getScore(ChatColor.GRAY + "   Ping: " + getPing(staff) + "ms").setScore(line--);
        }

        objective.getScore("  ").setScore(line--);

        // Suspect Info
        String suspectStatus = (suspect != null && suspect.isOnline()) ?
                ChatColor.GREEN + "●" : ChatColor.RED + "●";
        objective.getScore(ChatColor.RED + "🎯 Suspect: " + suspectStatus + ChatColor.WHITE + " " +
                session.getSuspectName()).setScore(line--);

        if (suspect != null && suspect.isOnline()) {
            objective.getScore(ChatColor.GRAY + "   Ping: " + getPing(suspect) + "ms").setScore(line--);

            // Freeze status
            boolean isFrozen = plugin.getFreezeManager().isFrozen(suspect);
            String freezeStatus = isFrozen ? ChatColor.AQUA + "❄ FROZEN" : ChatColor.GREEN + "✓ ACTIVE";
            objective.getScore(ChatColor.GRAY + "   Status: " + freezeStatus).setScore(line--);
        }

        objective.getScore("   ").setScore(line--);

        // World Info
        if (suspect != null && suspect.isOnline()) {
            String worldName = suspect.getWorld().getName();
            objective.getScore(ChatColor.YELLOW + "🌍 World: " + ChatColor.WHITE + worldName).setScore(line--);
        }

        // Additional info based on role
        if (isStaff) {
            objective.getScore("    ").setScore(line--);
            objective.getScore(ChatColor.GOLD + "📋 STAFF CONTROLS:").setScore(line--);
            objective.getScore(ChatColor.GREEN + "• /freeze " + session.getSuspectName()).setScore(line--);
            objective.getScore(ChatColor.RED + "• /tempban " + session.getSuspectName()).setScore(line--);
            objective.getScore(ChatColor.YELLOW + "• /ss end " + session.getSuspectName()).setScore(line--);
        } else {
            objective.getScore("     ").setScore(line--);
            objective.getScore(ChatColor.GOLD + "📢 INSTRUCTIONS:").setScore(line--);
            objective.getScore(ChatColor.WHITE + "• Join Discord call").setScore(line--);
            objective.getScore(ChatColor.WHITE + "• Share your screen").setScore(line--);
            objective.getScore(ChatColor.RED + "• DO NOT disconnect!").setScore(line--);
        }

        // Footer
        objective.getScore("      ").setScore(line--);
        objective.getScore(ChatColor.GRAY + "Last update: " + LocalDateTime.now().format(timeFormatter)).setScore(line--);
    }

    private void startUpdateTask(ScreenShareSession session) {
        UUID suspectId = session.getSuspectId();
        UUID staffId = session.getStaffId();

        BukkitRunnable updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                // Check if session is still active
                if (!plugin.getSessionManager().isInSession(Bukkit.getPlayer(suspectId))) {
                    this.cancel();
                    return;
                }

                // Update scoreboards
                Player staff = Bukkit.getPlayer(staffId);
                Player suspect = Bukkit.getPlayer(suspectId);

                if (staff != null && staff.isOnline()) {
                    updateScoreboardContent(staff, session, true);
                }

                if (suspect != null && suspect.isOnline()) {
                    updateScoreboardContent(suspect, session, false);
                }
            }
        };

        updateTask.runTaskTimer(plugin, 20L, 20L); // Update every second
        updateTasks.put(suspectId, updateTask);
    }

    public void removeSessionScoreboard(UUID suspectId, UUID staffId) {
        // Cancel update task
        BukkitRunnable task = updateTasks.remove(suspectId);
        if (task != null) {
            task.cancel();
        }

        // Remove scoreboards and restore default
        Player staff = Bukkit.getPlayer(staffId);
        Player suspect = Bukkit.getPlayer(suspectId);

        if (staff != null && staff.isOnline()) {
            playerScoreboards.remove(staffId);
            staff.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }

        if (suspect != null && suspect.isOnline()) {
            playerScoreboards.remove(suspectId);
            suspect.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
    }

    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    private int getPing(Player player) {
        try {
            // Use reflection to get ping (works on most Spigot versions)
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            return (Integer) handle.getClass().getField("ping").get(handle);
        } catch (Exception e) {
            return 0; // Fallback if reflection fails
        }
    }

    public void cleanup() {
        // Cancel all update tasks
        for (BukkitRunnable task : updateTasks.values()) {
            if (task != null) {
                task.cancel();
            }
        }
        updateTasks.clear();

        // Restore all scoreboards
        for (Map.Entry<UUID, Scoreboard> entry : playerScoreboards.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player != null && player.isOnline()) {
                player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            }
        }
        playerScoreboards.clear();
    }
}