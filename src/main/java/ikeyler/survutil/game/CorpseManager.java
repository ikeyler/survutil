package ikeyler.survutil.game;

import ikeyler.survutil.Main;
import ikeyler.survutil.Util;
import ikeyler.survutil.game.player.GamePlayer;
import ikeyler.survutil.game.player.PlayerState;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Husk;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CorpseManager {
    private final Game game;
    private final List<Entity> playerCorpses = new ArrayList<>();
    private final Plugin plugin = Main.getInstance();
    private final NamespacedKey pidKey = new NamespacedKey(plugin, "player_uuid");
    public CorpseManager(Game game) {
        this.game = game;
    }
    public void createPlayerCorpse(Player player, Location location) {
        int currentId = game.getGameId();
        World world = location.getWorld();
        int chunkX = location.getBlockX() >> 4;
        int chunkZ = location.getBlockZ() >> 4;
        Chunk chunk = world.getChunkAt(chunkX, chunkZ);
        if (!chunk.isLoaded()) {
            chunk.load(true);
        }
        chunk.setForceLoaded(true);
        Husk corpse = createCorpseEntity(player, location);
        playerCorpses.add(corpse);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            removeCorpse(corpse);
            if (currentId != game.getGameId()) return;
            if (game.isRunning() && game.containsPlayer(player.getUniqueId())) {
                GamePlayer gamePlayer = game.getGamePlayer(player.getUniqueId());
                if (!gamePlayer.isAlive()) {
                    gamePlayer.setRescueAvailable(false);
                    gamePlayer.setState(PlayerState.SPECTATING);
                    Bukkit.broadcastMessage(String.format("Спасти игрока §e%s §fне удалось - никто не пришел", player.getName()));
                }
            }}, 6000L);
    }
    private Husk createCorpseEntity(Player player, Location location) {
        World world = location.getWorld();
        Husk corpse = (Husk) world.spawnEntity(location, EntityType.HUSK);
        corpse.setCustomName(player.getName());
        corpse.getPersistentDataContainer().set(pidKey, PersistentDataType.STRING, player.getUniqueId().toString());
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
        corpse.setRemoveWhenFarAway(false);
        corpse.setPersistent(true);
        return corpse;
    }
    public void loadCorpses() {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (!(entity instanceof Husk)) continue;
                if (!entity.getPersistentDataContainer().has(pidKey, PersistentDataType.STRING)) continue;
                UUID playerId = Util.getUuid(entity.getPersistentDataContainer().get(pidKey, PersistentDataType.STRING));
                if (playerId != null && game.containsPlayer(playerId) && game.getGamePlayer(playerId).isRescueAvailable()) {
                    playerCorpses.add(entity);
                }
            }
        }
    }
    public void updateCorpses() {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (!(entity instanceof Husk)) continue;
                if (!entity.getPersistentDataContainer().has(pidKey, PersistentDataType.STRING)) continue;
                UUID playerId = Util.getUuid(entity.getPersistentDataContainer().get(pidKey, PersistentDataType.STRING));
                if (!game.isRunning() || playerId == null || !game.containsPlayer(playerId) || !game.getGamePlayer(playerId).isRescueAvailable()) {
                    removeCorpse(entity);
                }
            }
        }
    }
    public void removeCorpse(Entity corpse) {
        if (corpse == null) return;
        playerCorpses.remove(corpse);
        if (!corpse.isDead()) {
            Location loc = corpse.getLocation();
            World world = loc.getWorld();
            int chunkX = loc.getBlockX() >> 4;
            int chunkZ = loc.getBlockZ() >> 4;
            corpse.remove();
            world.setChunkForceLoaded(chunkX, chunkZ, false);
        }
    }
    public List<Entity> getPlayerCorpses() {
        return playerCorpses;
    }
    public void clearPlayerCorpses() {
        for (Entity corpse : new ArrayList<>(playerCorpses)) {
            removeCorpse(corpse);
        }
    }
}
