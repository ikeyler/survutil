package ikeyler.survutil.game;

import ikeyler.survutil.Main;
import ikeyler.survutil.Util;
import ikeyler.survutil.game.notes.NoteManager;
import ikeyler.survutil.game.player.GamePlayer;
import ikeyler.survutil.game.player.PlayerManager;
import ikeyler.survutil.game.player.PlayerState;
import ikeyler.survutil.game.voting.Vote;
import ikeyler.survutil.game.voting.VoteManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;

import static ikeyler.survutil.Util.*;

public class Game {
    private final Logger logger = Main.getLog();
    private static Game instance;
    private int gameId = 0;
    private final GameTask gameTask;
    private final PlayerRescueTask playerRespawnTask;
    private boolean running;
    private final PlayerManager playerManager;
    private GameSettings gameSettings;
    private final VoteManager voteManager;
    private final NoteManager noteManager;
    private final GameEventHandler eventHandler;
    private final Plugin plugin = Main.getInstance();
    private final Random random = new Random();
    private int attempt = 0;
    private final int locationSearchRadius = 30000;
    private final int locationOffsetRadius = 3500;
    private final List<Entity> playerCorpses = new ArrayList<>();
    private final ActionBarTimer barTimer;
    private final ItemTracker itemTracker;
    private Location lastStartLocation = null;
    private Game() {
        this.barTimer = new ActionBarTimer(this);
        this.gameSettings = new GameSettings.Survival(true, true);
        this.itemTracker = new ItemTracker(this);
        this.voteManager = new VoteManager(this);
        this.noteManager = new NoteManager();
        this.playerManager = new PlayerManager(this);
        this.eventHandler = new GameEventHandler(this);
        this.running = false;
        this.gameTask = new GameTask(this);
        this.gameTask.start();
        this.playerRespawnTask = new PlayerRescueTask(this);
        this.playerRespawnTask.start();
        getVote("restart").start();
    }
    public static Game getInstance() {
        if (instance == null) {
            instance = new Game();
        }
        return instance;
    }
    public void restartGame(Location centerLoc) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            Instant locStartTime = Instant.now();
            Location startLocation = findGameLocation(resolveLocation(centerLoc));
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
            preloadChunks(world, chunkX, chunkZ);
            world.setSpawnLocation(startLocation);
            Bukkit.getWorlds().forEach(this::resetWorld);
            Bukkit.getServer().getOnlinePlayers().forEach(player -> resetPlayer(player, startLocation));
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
        clearPlayerCorpses();
    }
    public void stopGame() {
        if (!running) return;
        running = false;
        int gameTime = barTimer.getSeconds();
        resetGame();
        barTimer.setInfoLabel(String.format("§7§oИгра завершена §7• §c%s §7• §e/vote", Util.formatTime(gameTime)));
    }
    private void preloadChunks(World world, int chunkX, int chunkZ) {
        for (int cx = -1; cx <= 1; cx++) {
            for (int cz = -1; cz <= 1; cz++) {
                world.getChunkAt(chunkX + cx, chunkZ + cz).load(true);
            }
        }
    }
    private Location resolveLocation(Location location) {
        if (location != null) return location;
        if (lastStartLocation != null) return lastStartLocation;
        return new Location(Bukkit.getWorlds().getFirst(), 0, 0, 0);
    }
    public Location findGameLocation(Location centerLoc) {
        if (centerLoc == null) return null;
        World world = centerLoc.getWorld();
        int centerX = (int) centerLoc.getX();
        int centerZ = (int) centerLoc.getZ();
        logger.info("started finding game location");
        for (int i = 0; i < 15; i++) {
            int randX = centerX + random.nextInt(-locationSearchRadius, locationSearchRadius);
            int randZ = centerZ + random.nextInt(-locationSearchRadius, locationSearchRadius);
            int dx = randX - centerX;
            int dz = randZ - centerZ;
            int distance = (int) Math.sqrt(dx * dx + dz * dz);
            if (distance < locationOffsetRadius) continue;
            world.loadChunk(randX >> 4, randZ >> 4);
            Block yBlock = world.getHighestBlockAt(randX, randZ);
            if (!yBlock.isLiquid()) {
                return new Location(world, randX, yBlock.getY() + 1, randZ);
            }
        }
        return null;
    }
    private void resetWorld(World world) {
        world.setTime(0L);
        world.setStorm(false);
        world.setThundering(false);
        if (gameSettings.hardcore()) {
            world.setDifficulty(Difficulty.HARD);
        }
        world.setHardcore(gameSettings.hardcore());
    }
    public void resetPlayer(Player player, Location location) {
        if (!containsPlayer(player.getUniqueId())) return;
        player.setGameMode(GameMode.SURVIVAL);
        player.closeInventory();
        player.setItemOnCursor(null);
        player.getInventory().setItemInOffHand(null);
        player.getInventory().clear();
        player.getEnderChest().clear();
        player.setExp(0);
        player.getActivePotionEffects().clear();
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setSaturation(20);
        player.setFallDistance(0);
        player.setFireTicks(0);
        player.teleport(location);
        revokeAdvancements(player);
    }
    public void createPlayerCorpse(Player player, Location location) {
        int currentId = this.gameId;
        World world = location.getWorld();
        int chunkX = (int) location.getX() >> 4;
        int chunkZ = (int) location.getZ() >> 4;
        world.setChunkForceLoaded(chunkX, chunkZ, true);
        Husk corpse = (Husk) world.spawnEntity(location, EntityType.HUSK);
        corpse.setCustomName(player.getName());
        corpse.setMetadata("player_uuid", new FixedMetadataValue(plugin, player.getUniqueId().toString()));
        corpse.setAdult();
        corpse.setInvisible(true);
        corpse.setInvulnerable(true);
        corpse.setAI(false);
        corpse.setSilent(true);
        corpse.setGravity(false);
        corpse.setGlowing(true);
        corpse.setCollidable(false);
        corpse.setCanPickupItems(false);
        corpse.getEquipment().clear();
        playerCorpses.add(corpse);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            corpse.remove();
            playerCorpses.remove(corpse);
            world.setChunkForceLoaded(chunkX, chunkZ, false);
            if (currentId != this.gameId) return;
            if (running && containsPlayer(player.getUniqueId())) {
                GamePlayer gamePlayer = getGamePlayer(player.getUniqueId());
                if (!gamePlayer.isAlive()) {
                    gamePlayer.setRescueAvailable(false);
                    gamePlayer.setState(PlayerState.SPECTATING);
                    Bukkit.broadcastMessage(String.format("Спасти игрока §e%s §fне удалось - никто не пришел", player.getName()));
                }
            }}, 6000L);
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
    public List<Entity> getPlayerCorpses() {
        return playerCorpses;
    }
    public void clearPlayerCorpses() {
        for (Entity corpse : playerCorpses) {
            Chunk chunk = corpse.getLocation().getChunk();
            if (!chunk.isLoaded()) {
                chunk.load();
            }
            corpse.remove();
        }
        playerCorpses.clear();
    }
}
