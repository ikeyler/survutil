package ikeyler.survutil;

import ikeyler.survutil.game.Game;
import ikeyler.survutil.game.player.GamePlayer;
import ikeyler.survutil.game.player.PlayerState;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ConfigManager {
    private final Plugin plugin;
    private FileConfiguration config;
    private File configFile;
    public ConfigManager(Plugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }
    public void loadConfig() {
        configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveDefaultConfig();
            plugin.getLogger().info("created config.yml");
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }
    public void saveConfig() {
        try {
            config.save(configFile);
        }
        catch (IOException e) {
            plugin.getLogger().severe("could not save config.yml:");
            e.printStackTrace();
        }
    }
    public boolean isGameRunning() {
        return config.getBoolean("game.running");
    }
    public int getGameAttempt() {
        return config.getInt("game.attempt");
    }
    public boolean isGameHardcore() {
        return config.getBoolean("game.hardcore");
    }
    public boolean isGameRescueEnabled() {
        return config.getBoolean("game.rescueEnabled");
    }
    public Location getGameLocation() {
        return new Location(Bukkit.getWorlds().getFirst(),
                config.getDouble("game.location.x"),
                config.getDouble("game.location.y"),
                config.getDouble("game.location.z"));
    }
    public boolean isTimerRunning() {
        return config.getBoolean("timer.running");
    }
    public int getTimerSeconds() {
        return config.getInt("timer.seconds");
    }
    public List<GamePlayer> getGamePlayers() {
        List<GamePlayer> players = new ArrayList<>();
        if (!config.isConfigurationSection("players")) {
            return players;
        }
        Set<String> uuids = config.getConfigurationSection("players").getKeys(false);
        for (String uuid : uuids) {
            try {
                String path = "players." + uuid;
                UUID playerId = UUID.fromString(uuid);
                GamePlayer gamePlayer;
                Player onlinePlayer = Bukkit.getPlayer(playerId);
                if (onlinePlayer != null) {
                    gamePlayer = new GamePlayer(Game.getInstance(), onlinePlayer);
                }
                else {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerId);
                    if (!offlinePlayer.hasPlayedBefore()) {
                        Main.getLog().warning("skipping " + uuid);
                        continue;
                    }
                    gamePlayer = new GamePlayer(Game.getInstance(), offlinePlayer);
                }
                gamePlayer.setAlive(config.getBoolean(path + ".alive"));
                gamePlayer.setRescueAvailable(config.getBoolean(path + ".rescueAvailable"));
                gamePlayer.setState(PlayerState.valueOf(config.getString(path + ".state")));
                gamePlayer.setCanJoinGame(config.getBoolean(path + ".canJoinGame"));
                if (config.isConfigurationSection(path + ".pendingRespawnLoc")) {
                    World world = Bukkit.getWorld(config.getString(path + "pendingRespawnLoc.world"));
                    if (world == null) Main.getLog().warning("invalid world in config setting pendingRespawnLoc.world for " + uuid);
                    else {
                        gamePlayer.setPendingRespawn(config.getBoolean(path + ".pendingRespawn"), new Location(
                                world,
                                config.getDouble(path + "pendingRespawnLoc.x"),
                                config.getDouble(path + "pendingRespawnLoc.y"),
                                config.getDouble(path + "pendingRespawnLoc.z")
                        ));
                    }
                }
                players.add(gamePlayer);
                Main.getLog().info("added " + uuid + " to the game");
            }
            catch (Exception e) {
                Main.getLog().warning("could not add player from config: " + uuid);
                e.printStackTrace();
            }
        }
        return players;
    }
    public void saveGame(boolean running, int attempt, boolean hardcore, boolean rescueEnabled, Location location) {
        config.set("game.running", running);
        config.set("game.attempt", attempt);
        config.set("game.hardcore", hardcore);
        config.set("game.rescueEnabled", rescueEnabled);
        config.set("game.location.x", location.getX());
        config.set("game.location.y", location.getY());
        config.set("game.location.z", location.getZ());
        saveConfig();
    }
    public void saveGamePlayers(List<GamePlayer> players) {
        for (GamePlayer player : players) {
            String path = "players." + player.getPlayerUUID();
            config.set(path + ".alive", player.isAlive());
            config.set(path + ".rescueAvailable", player.isRescueAvailable());
            config.set(path + ".state", player.getState().toString());
            config.set(path + ".canJoinGame", player.canJoinGame());
            config.set(path + ".pendingRespawn", player.isPendingRespawn());
            if (player.getPendingRespawnLoc() != null) {
                config.set(path + ".pendingRespawnLoc.world", player.getPendingRespawnLoc().getWorld().getName());
                config.set(path + ".pendingRespawnLoc.x", player.getPendingRespawnLoc().getX());
                config.set(path + ".pendingRespawnLoc.y", player.getPendingRespawnLoc().getY());
                config.set(path + ".pendingRespawnLoc.z", player.getPendingRespawnLoc().getZ());
            }
            else {
                config.set(path + ".pendingRespawnLoc", null);
            }
        }
        saveConfig();
        Main.getLog().info("saved " + players.size() + " players");
    }
    public void saveTimer(boolean running, int seconds) {
        config.set("timer.running", running);
        config.set("timer.seconds", seconds);
        saveConfig();
    }
    public void resetTimer() {
        config.set("timer.running", false);
        config.set("timer.seconds", 0);
        saveConfig();
    }
    public void resetGame() {
        config.set("game.running", false);
        config.set("game.attempt", 0);
        config.set("game.hardcore", false);
        config.set("game.rescueEnabled", false);
        config.set("game.location.x", 0);
        config.set("game.location.y", 0);
        config.set("game.location.z", 0);
        saveConfig();
    }
    public void resetGamePlayers() {
        config.set("players", null);
        saveConfig();
    }
}
