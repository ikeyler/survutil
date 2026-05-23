package ikeyler.survutil.game.voting;

import ikeyler.survutil.Main;
import ikeyler.survutil.game.Game;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public abstract class Vote implements Listener {
    protected final Game game;
    protected final String name;
    protected boolean active = false;
    protected final Set<UUID> participants = new HashSet<>();
    protected int requiredVotes;
    public Vote(Game game, String name) {
        this.game = game;
        this.name = name;
        Bukkit.getPluginManager().registerEvents(this, Main.getInstance());
    }
    public String getName() {
        return name;
    }
    public boolean isActive() {
        return active;
    }
    public void start() {
        if (active) return;
        active = true;
        participants.clear();
        requiredVotes = Bukkit.getOnlinePlayers().size();
        if (getStartMessage() != null)
            Bukkit.broadcastMessage(getStartMessage());
    }
    public void stop() {
        if (!active) return;
        reset();
    }
    public void reset() {
        active = false;
        participants.clear();
        requiredVotes = 0;
    }
    public void addPlayer(Player player) {
        if (!active) {
            player.sendMessage("Голосование не происходит");
            return;
        }
        if (participants.contains(player.getUniqueId())) {
            player.sendMessage("Вы уже участвуете в голосовании");
            return;
        }
        participants.add(player.getUniqueId());
        Bukkit.broadcastMessage(getVoteMessage(player));
        checkDone();
    }
    public void removePlayer(Player player) {
        participants.remove(player.getUniqueId());
    }
    public String getStartMessage() {
        return null;
    }
    public int getVotes() {
        return participants.size();
    }
    public void checkDone() {
        if (isDone()) {
            stop();
            onDone();
        }
    }
    public boolean isDone() {
        return participants.size() >= requiredVotes;
    }
    public Set<UUID> getParticipants() {
        return participants;
    }
    public int getRequiredVotes() {
        return requiredVotes;
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!active) return;
        int required = Bukkit.getOnlinePlayers().size();
        if (required != requiredVotes) {
            requiredVotes = required;
        }
        checkDone();
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!active) return;
        UUID uuid = event.getPlayer().getUniqueId();
        int totalPlayers = Bukkit.getOnlinePlayers().size() - 1;
        participants.remove(uuid);
        if (totalPlayers <= 0) return;
        requiredVotes = totalPlayers;
        checkDone();
    }
    protected abstract void onDone();
    protected abstract String getVoteMessage(Player player);
}
