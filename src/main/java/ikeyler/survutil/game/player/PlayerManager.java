package ikeyler.survutil.game.player;

import ikeyler.survutil.game.Game;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PlayerManager {
    private final Game game;
    private final Map<UUID, GamePlayer> playerList = new HashMap<>();
    public PlayerManager(Game game) {
        this.game = game;
    }
    public boolean addPlayer(Player player) {
        if (!game.isRunning()) return false;
        if (playerList.containsKey(player.getUniqueId())) return false;
        playerList.put(player.getUniqueId(), new GamePlayer(game, player));
        game.resetPlayer(player, game.getStartLocation());
        return true;
    }
    public void addOnlinePlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            GamePlayer gamePlayer = new GamePlayer(game, player);
            gamePlayer.setState(PlayerState.PLAYING);
            playerList.put(player.getUniqueId(), gamePlayer);
        }
    }
    public void removePlayer(Player player) {
        playerList.remove(player.getUniqueId());
    }
    public boolean leavePlayer(Player player) {
        if (!playerList.containsKey(player.getUniqueId())) return false;
        GamePlayer gamePlayer = getGamePlayer(player.getUniqueId());
        if (!gamePlayer.canJoinGame()) return false;
        gamePlayer.setCanJoinGame(false);
        gamePlayer.setRescueAvailable(false);
        gamePlayer.setAlive(false);
        gamePlayer.setState(PlayerState.SPECTATING);
        return true;
    }
    public Map<UUID, GamePlayer> getPlayerList() {
        return playerList;
    }
    public void respawnPlayer(Player player, Location location) {
        if (!playerList.containsKey(player.getUniqueId()))
            addPlayer(player);
        GamePlayer gamePlayer = getGamePlayer(player.getUniqueId());
        if (game.isRunning()) {
            player.setGameMode(GameMode.SURVIVAL);
            player.spigot().respawn();
            gamePlayer.setAlive(true);
            gamePlayer.setState(PlayerState.PLAYING);
            Bukkit.broadcastMessage(String.format("§e%s §fбыл возрожден!", player.getName()));
            if (location == null) game.resetPlayer(player, game.getStartLocation());
            else player.teleport(location);
        }
    }
    public GamePlayer getGamePlayer(UUID uuid) {
        return playerList.get(uuid);
    }
    public List<GamePlayer> filterPlayers(Predicate<GamePlayer> filter) {
        return playerList.values().stream().filter(filter).collect(Collectors.toList());
    }
    public List<GamePlayer> getOnlinePlayers() {
        return filterPlayers(GamePlayer::isPlaying);
    }
    public List<GamePlayer> getRescuingPlayers() {
        return filterPlayers(player -> player.getState() == PlayerState.RESCUING);
    }
    public List<GamePlayer> getAlivePlayers() {
        return filterPlayers(GamePlayer::isAlive);
    }
}