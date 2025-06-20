package com.example.screenshare.managers;

import com.example.screenshare.ScreenSharePlugin;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class WorldManager {

    private static final String SCREENSHARE_WORLD_NAME = "screenshare_world";

    private final ScreenSharePlugin plugin;
    private World screenShareWorld;
    private Location staffSpawn;
    private Location targetSpawn;
    private File spawnConfigFile;
    private FileConfiguration spawnConfig;

    public WorldManager(ScreenSharePlugin plugin) {
        this.plugin = plugin;
        loadSpawnConfig();
    }

    private void loadSpawnConfig() {
        spawnConfigFile = new File(plugin.getDataFolder(), "spawns.yml");
        if (!spawnConfigFile.exists()) {
            plugin.saveResource("spawns.yml", false);
        }
        spawnConfig = YamlConfiguration.loadConfiguration(spawnConfigFile);
        loadSpawnLocations();
    }

    private void loadSpawnLocations() {
        // Load staff spawn
        if (spawnConfig.contains("staff-spawn")) {
            try {
                String worldName = spawnConfig.getString("staff-spawn.world");
                if (worldName != null && worldName.equals(SCREENSHARE_WORLD_NAME)) {
                    World world = Bukkit.getWorld(worldName);
                    if (world != null) {
                        double x = spawnConfig.getDouble("staff-spawn.x");
                        double y = spawnConfig.getDouble("staff-spawn.y");
                        double z = spawnConfig.getDouble("staff-spawn.z");
                        float yaw = (float) spawnConfig.getDouble("staff-spawn.yaw");
                        float pitch = (float) spawnConfig.getDouble("staff-spawn.pitch");
                        staffSpawn = new Location(world, x, y, z, yaw, pitch);
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Error loading staff spawn location: " + e.getMessage());
            }
        }

        // Load target spawn
        if (spawnConfig.contains("target-spawn")) {
            try {
                String worldName = spawnConfig.getString("target-spawn.world");
                if (worldName != null && worldName.equals(SCREENSHARE_WORLD_NAME)) {
                    World world = Bukkit.getWorld(worldName);
                    if (world != null) {
                        double x = spawnConfig.getDouble("target-spawn.x");
                        double y = spawnConfig.getDouble("target-spawn.y");
                        double z = spawnConfig.getDouble("target-spawn.z");
                        float yaw = (float) spawnConfig.getDouble("target-spawn.yaw");
                        float pitch = (float) spawnConfig.getDouble("target-spawn.pitch");
                        targetSpawn = new Location(world, x, y, z, yaw, pitch);
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Error loading target spawn location: " + e.getMessage());
            }
        }
    }

    public void setStaffSpawn(Location location) {
        staffSpawn = location.clone();
        saveSpawnLocation("staff-spawn", location);
    }

    public void setTargetSpawn(Location location) {
        targetSpawn = location.clone();
        saveSpawnLocation("target-spawn", location);
    }

    private void saveSpawnLocation(String path, Location location) {
        spawnConfig.set(path + ".world", location.getWorld().getName());
        spawnConfig.set(path + ".x", location.getX());
        spawnConfig.set(path + ".y", location.getY());
        spawnConfig.set(path + ".z", location.getZ());
        spawnConfig.set(path + ".yaw", location.getYaw());
        spawnConfig.set(path + ".pitch", location.getPitch());

        try {
            spawnConfig.save(spawnConfigFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save spawn configuration: " + e.getMessage());
        }
    }

    public Location getStaffSpawn() {
        if (staffSpawn != null && staffSpawn.getWorld() != null) {
            return staffSpawn.clone();
        }
        return getDefaultSpawn();
    }

    public Location getTargetSpawn() {
        if (targetSpawn != null && targetSpawn.getWorld() != null) {
            return targetSpawn.clone();
        }
        return getDefaultSpawn();
    }

    private Location getDefaultSpawn() {
        World ssWorld = getScreenShareWorld();
        return ssWorld != null ? ssWorld.getSpawnLocation() : null;
    }

    public void resetSpawnLocations() {
        staffSpawn = null;
        targetSpawn = null;
        spawnConfig.set("staff-spawn", null);
        spawnConfig.set("target-spawn", null);

        try {
            spawnConfig.save(spawnConfigFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save spawn configuration: " + e.getMessage());
        }
    }

    public void createScreenShareWorld() {
        try {
            // Controlla se il mondo esiste già
            screenShareWorld = Bukkit.getWorld(SCREENSHARE_WORLD_NAME);

            if (screenShareWorld == null) {
                // Controlla se esiste una cartella mondo personalizzata nella cartella del plugin
                File pluginFolder = plugin.getDataFolder();
                File customWorldFolder = new File(pluginFolder, SCREENSHARE_WORLD_NAME);

                if (customWorldFolder.exists() && customWorldFolder.isDirectory()) {
                    plugin.getLogger().info("Found custom world in plugin folder, copying...");

                    // Copia il mondo personalizzato nella cartella principale del server
                    File serverWorldFolder = new File(Bukkit.getWorldContainer(), SCREENSHARE_WORLD_NAME);
                    if (!serverWorldFolder.exists()) {
                        copyWorld(customWorldFolder, serverWorldFolder);
                        plugin.getLogger().info("Custom world copied successfully!");
                    }
                }

                plugin.getLogger().info("Loading world screenshare...");

                // Crea/carica il mondo
                WorldCreator creator = new WorldCreator(SCREENSHARE_WORLD_NAME);

                // Se non esiste un mondo personalizzato, crea uno flat
                if (!new File(Bukkit.getWorldContainer(), SCREENSHARE_WORLD_NAME).exists()) {
                    creator.type(WorldType.FLAT);
                    creator.generateStructures(false);
                }

                screenShareWorld = creator.createWorld();

                if (screenShareWorld != null) {
                    // Configura mondo
                    screenShareWorld.setDifficulty(Difficulty.PEACEFUL);
                    screenShareWorld.setSpawnFlags(false, false);
                    screenShareWorld.setPVP(false);
                    screenShareWorld.setStorm(false);
                    screenShareWorld.setThundering(false);
                    screenShareWorld.setWeatherDuration(Integer.MAX_VALUE);
                    screenShareWorld.setTime(6000); // Mezzogiorno
                    screenShareWorld.setGameRuleValue("doDaylightCycle", "false");
                    screenShareWorld.setGameRuleValue("doMobSpawning", "false");
                    screenShareWorld.setGameRuleValue("doWeatherCycle", "false");

                    plugin.getLogger().info("Mondo screenshare caricato con successo!");

                    // Reload spawn locations after world is loaded
                    loadSpawnLocations();
                } else {
                    plugin.getLogger().info("Impossibile caricare il mondo screenshare!");
                }
            } else {
                plugin.getLogger().info("Mondo screenshare già caricato.");
                // Reload spawn locations
                loadSpawnLocations();
            }
        } catch (Exception e) {
            plugin.getLogger().info("Errore durante il caricamento del mondo screenshare: " + e.getMessage());
        }
    }

    private void copyWorld(File source, File target) {
        try {
            if (!target.exists()) {
                target.mkdirs();
            }

            File[] files = source.listFiles();
            if (files != null) {
                for (File file : files) {
                    File targetFile = new File(target, file.getName());
                    if (file.isDirectory()) {
                        copyWorld(file, targetFile);
                    } else {
                        java.nio.file.Files.copy(file.toPath(), targetFile.toPath(),
                                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().info("Errore durante la copia del mondo " + e.getMessage());
        }
    }

    public World getScreenShareWorld() {
        if (screenShareWorld == null) {
            screenShareWorld = Bukkit.getWorld(SCREENSHARE_WORLD_NAME);
        }
        return screenShareWorld;
    }

    public boolean isScreenShareWorld(World world) {
        return world != null && SCREENSHARE_WORLD_NAME.equals(world.getName());
    }

    public void createWorldInstructions() {
        File pluginFolder = plugin.getDataFolder();
        if (!pluginFolder.exists()) {
            pluginFolder.mkdirs();
        }

        File instructionsFile = new File(pluginFolder, "MONDO_PERSONALIZZATO.txt");
        if (!instructionsFile.exists()) {
            try {
                java.nio.file.Files.write(instructionsFile.toPath(),
                        ("ISTRUZIONI PER MONDO PERSONALIZZATO:\n\n" +
                                "1. Crea un mondo in single player o con WorldEdit\n" +
                                "2. Copia la cartella del mondo\n" +
                                "3. Incolla la cartella in questa directory e rinominala 'screenshare_world'\n" +
                                "4. Riavvia il server\n\n" +
                                "Il plugin caricherà automaticamente il tuo mondo personalizzato!\n" +
                                "Se non viene trovato un mondo personalizzato, verrà creato un mondo flat di default.\n\n" +
                                "SPAWN PERSONALIZZATI:\n" +
                                "Usa /ssspawn per impostare spawn personalizzati per staff e target nel mondo screenshare!").getBytes());
                plugin.getLogger().info("Istruzioni per mondo personalizzato create in: " + instructionsFile.getPath());
            } catch (Exception e) {
                plugin.getLogger().info("Errore durante la creazione delle istruzioni " + e.getMessage());
            }
        }
    }
}