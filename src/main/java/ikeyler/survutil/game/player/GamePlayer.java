package ikeyler.survutil.game.player;

import ikeyler.survutil.Main;
import ikeyler.survutil.game.Game;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class GamePlayer {
    private final Game game;
    private Player player;
    private final UUID playerUUID;
    private PlayerState state = PlayerState.PLAYING;
    private boolean canJoinGame = true;
    private boolean alive = true;
    private int rescueProgress = 0;
    private boolean rescueAvailable = true;
    private boolean rescued = false;
    public GamePlayer(Game game, Player player) {
        this.game = game;
        this.player = player;
        this.playerUUID = player.getUniqueId();
    }
    public GamePlayer(Game game, OfflinePlayer offlinePlayer) {
        this.game = game;
        this.player = null;
        this.playerUUID = offlinePlayer.getUniqueId();
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
        return alive && state == PlayerState.PLAYING;
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
    @Override
    public String toString() {
        return String.format("GamePlayer{name='%s', state='%s', alive=%s, rescueProgress=%d, rescueAvailable=%s, rescued=%s}",
                player.getName(), state.toString(), alive, rescueProgress, rescueAvailable, rescued);
    }
}
