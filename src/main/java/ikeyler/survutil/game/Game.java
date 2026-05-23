package ikeyler.survutil.game;

import ikeyler.survutil.Main;
import ikeyler.survutil.Util;
import ikeyler.survutil.game.notes.NoteManager;
import ikeyler.survutil.game.player.GamePlayer;
import ikeyler.survutil.game.player.PlayerManager;
import ikeyler.survutil.game.voting.Vote;
import ikeyler.survutil.game.voting.VoteManager;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.plugin.Plugin;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;

import static ikeyler.survutil.Util.*;

public class Game {
    private final Plugin plugin = Main.getInstance();
    private final Logger logger = Main.getLog();
    private static Game instance;
    private boolean running = false;
    private int gameId = 0;
    private int attempt = 0;
    private final WorldManager worldManager = new WorldManager();
    private final PlayerManager playerManager;
    private final CorpseManager corpseManager;
    private GameSettings gameSettings;
    private final VoteManager voteManager;
    private final NoteManager noteManager;
    private final LocationFinder locationFinder;
    private final GameEventHandler eventHandler;
    private final ActionBarTimer barTimer;
    private final ItemTracker itemTracker;
    private final GameTask gameTask;
    private final PlayerRescueTask playerRespawnTask;
    private Location lastStartLocation = null;
    private Game() {
        this.barTimer = new ActionBarTimer(this);
        this.gameSettings = new GameSettings.Survival(true, true);
        this.itemTracker = new ItemTracker(this);
        this.voteManager = new VoteManager(this);
        this.noteManager = new NoteManager();
        this.playerManager = new PlayerManager(this);
        this.corpseManager = new CorpseManager(this);
        this.eventHandler = new GameEventHandler(this);
        this.locationFinder = new LocationFinder(this);
        this.gameTask = new GameTask(this);
        this.gameTask.start();
        this.playerRespawnTask = new PlayerRescueTask(this);
        this.playerRespawnTask.start();
        eventHandler.register();
        getVote("restart").start();
    }
    public static Game getInstance() {
        if (instance == null) {
            instance = new Game();
        }
        return instance;
    }
    public void restartGame(Location centerLoc) {
        getVote("restart").start();
        Bukkit.getScheduler().runTask(plugin, () -> {
            Instant locStartTime = Instant.now();
            Location startLocation = locationFinder.findGameLocation(locationFinder.resolveLocation(centerLoc));
            if (startLocation == null) {
                Bukkit.broadcastMessage("§cНе удалось найти локацию для спавна. Попробуйте снова");
                return;
            }
            Bukkit.broadcastMessage(String.format("§7§oЛокация найдена за %s мс", Duration.between(locStartTime, Instant.now()).toMillis()));
            resetGame();
            running = true;
            gameId++;
            attempt++;
            playerManager.addOnlinePlayers();
            World world = startLocation.getWorld();
            int chunkX = (int) startLocation.getX() >> 4;
            int chunkZ = (int) startLocation.getZ() >> 4;
            worldManager.preloadChunks(world, chunkX, chunkZ);
            world.setSpawnLocation(startLocation);
            worldManager.resetWorlds(gameSettings);
            playerManager.resetPlayers(startLocation);
            if (gameSettings.getType() == GameType.ITEM_SPEEDRUN) {
                itemTracker.setItem(gameSettings.getMaterial());
                itemTracker.start();
            }
            Bukkit.broadcastMessage(buildStartMessage(startLocation));
            lastStartLocation = startLocation;
        });
    }
    public void restartGameFromConfig(boolean hardcore, boolean rescueEnabled, int attempt, Location location, int timerSeconds) {
        logger.info("restarting game from config");
        running = true;
        this.attempt = attempt;
        resetGame();
        gameSettings = new GameSettings.Survival(hardcore, rescueEnabled);
        barTimer.setTime(timerSeconds);
        playerManager.addOnlinePlayers();
        lastStartLocation = location;
    }
    public void resetGame() {
        playerManager.getPlayerList().clear();
        barTimer.reset();
        noteManager.clearNotes();
        itemTracker.stop();
        corpseManager.clearPlayerCorpses();
    }
    public void stopGame() {
        if (!running) return;
        running = false;
        int gameTime = barTimer.getSeconds();
        resetGame();
        barTimer.setInfoLabel(String.format("§7§oИгра завершена §7• §c%s §7• §e/vote", Util.formatTime(gameTime)));
    }
    private String buildStartMessage(Location location) {
        String delimiter = "§8§m----------";
        StringBuilder sb = new StringBuilder(delimiter).append("\n");
        String coords = (int) location.getX() + " " + (int) location.getY() + " " + (int) location.getZ();
        String type = gameSettings.getType().getName();
        String biome = location.getWorld().getBiome(location).getKey().getKey();
        String lastLocation = lastStartLocation != null ? "§3" + (int) location.distance(lastStartLocation) + "м §fот предыдущего спавна" : null;
        if (gameSettings.getType() == GameType.ITEM_SPEEDRUN)
            type += " §7("+gameSettings.getMaterial()+")";
        else if (gameSettings.getType() == GameType.ADVANCEMENT_SPEEDRUN)
            type += " §7("+getAdvancementName(gameSettings.getAdvancement())+")";
        String[] lines = {
                "Попытка §e#"+attempt+" §7| §b"+coords+" §7(§b"+biome+"§7)",
                lastLocation,
                "Тип: §a"+type,
                "Хардкор: "+formatBoolean(gameSettings.hardcore()),
                "Возрождения: "+formatBoolean(gameSettings.isRescueEnabled())
        };
        Arrays.stream(lines).forEach(line -> {
            if (line != null)
                sb.append("§8| §f").append(line).append("\n");
        });
        sb.append(delimiter);
        return sb.toString();
    }
    public boolean isRunning() {
        return running;
    }
    public int getAttempt() {
        return attempt;
    }
    public Location getStartLocation() {
        return lastStartLocation;
    }
    public ActionBarTimer getBarTimer() {
        return barTimer;
    }
    public GameSettings getGameSettings() {
        return gameSettings;
    }
    public ItemTracker getItemTracker() {
        return itemTracker;
    }
    public VoteManager getVoteManager() {
        return voteManager;
    }
    public NoteManager getNoteManager() {
        return noteManager;
    }
    public PlayerRescueTask getPlayerRespawnTask() {
        return playerRespawnTask;
    }
    public PlayerManager getPlayerManager() {
        return playerManager;
    }
    public CorpseManager getCorpseManager() {
        return corpseManager;
    }
    public GamePlayer getGamePlayer(UUID uuid) {
        return playerManager.getPlayerList().get(uuid);
    }
    public int getGameId() {
        return gameId;
    }
    public boolean containsPlayer(UUID uuid) {
        return playerManager.getPlayerList().containsKey(uuid);
    }
    public Vote getVote(String name) {
        return voteManager.getVote(name);
    }
    public void setGameSettings(GameSettings gameSettings) {
        if (this.gameSettings.getType() != gameSettings.getType() && isRunning()) {
            if (!itemTracker.isRunning() && gameSettings.getType() == GameType.ITEM_SPEEDRUN)
                itemTracker.start();
        }
        this.gameSettings = gameSettings;
    }
}
