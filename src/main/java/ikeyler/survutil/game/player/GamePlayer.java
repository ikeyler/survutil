package ikeyler.survutil.game.player;

import ikeyler.survutil.Main;
import ikeyler.survutil.game.Game;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public class GamePlayer {
    private final Game game;
    private Player player;
    private final UUID playerUUID;
    private PlayerState state;
    private boolean canJoinGame = true;
    private boolean alive = true;
    private int rescueProgress = 0;
    private boolean rescueAvailable = true;
    private boolean rescued = false;
    private boolean pendingRespawn = false;
    private Location pendingRespawnLoc = null;
    public GamePlayer(Game game, Player player) {
        this.game = game;
        this.player = player;
        this.playerUUID = player.getUniqueId();
        this.state = PlayerState.PLAYING;
    }
    public GamePlayer(Game game, OfflinePlayer offlinePlayer) {
        this.game = game;
        this.player = null;
        this.playerUUID = offlinePlayer.getUniqueId();
        this.state = PlayerState.OFFLINE;
    }
    public void updatePlayer(Player player) {
        if (!playerUUID.equals(player.getUniqueId())) {
            throw new IllegalArgumentException("original GamePlayer's uuid (" + playerUUID + ") does not match with " + player.getUniqueId());
        }
        if (this.player == null) {
            this.player = player;
            Main.getLog().info("updated " + player.getName() + "'s player instance");
        }
    }
    public Player getPlayer() {
        return player;
    }
    public Optional<Player> getPlayerOpt() {
        if (player != null && player.isOnline()) return Optional.of(player);
        return Optional.empty();
    }
    public UUID getPlayerUUID() {
        return playerUUID;
    }
    public PlayerState getState() {
        return state;
    }
    public boolean canJoinGame() {
        return canJoinGame;
    }
    public void setCanJoinGame(boolean canJoinGame) {
        this.canJoinGame = canJoinGame;
    }
    public void setAlive(boolean alive) {
        this.alive = alive;
    }
    public boolean isAlive() {
        return alive;
    }
    public boolean isPlaying() {
        return player != null && alive && state == PlayerState.PLAYING;
    }
    public void setState(PlayerState state) {
        if (!game.isRunning()) return;
        this.state = state;
    }
    public int getRescueProgress() {
        return rescueProgress;
    }
    public void setRescueProgress(int rescueProgress) {
        this.rescueProgress = rescueProgress;
    }
    public boolean isRescueAvailable() {
        return rescueAvailable;
    }
    public void setRescueAvailable(boolean rescueAvailable) {
        this.rescueAvailable = rescueAvailable;
    }
    public boolean isRescued() {
        return rescued;
    }
    public void setRescued(boolean rescued) {
        this.rescued = rescued;
    }
    public boolean isPendingRespawn() {
        return pendingRespawn;
    }
    public Location getPendingRespawnLoc() {
        return pendingRespawnLoc;
    }
    public void setPendingRespawn(boolean pendingRespawn, Location location) {
        this.pendingRespawn = pendingRespawn;
        this.pendingRespawnLoc = location;
    }
    @Override
    public String toString() {
        String name = player != null ? player.getName() : "null (" + playerUUID + ")";
        return String.format("GamePlayer{name='%s', state='%s', alive=%s, rescueProgress=%d, rescueAvailable=%s, rescued=%s, canJoinGame=%s}",
                name, state.toString(), alive, rescueProgress, rescueAvailable, rescued, canJoinGame);
    }
}
