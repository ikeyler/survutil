package ikeyler.survutil.game;

import ikeyler.survutil.Main;
import ikeyler.survutil.game.player.GamePlayer;
import ikeyler.survutil.game.player.PlayerState;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Husk;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CorpseManager {
    private final Game game;
    private final List<Entity> playerCorpses = new ArrayList<>();
    private final Plugin plugin = Main.getInstance();
    public CorpseManager(Game game) {
        this.game = game;
    }
    public void createPlayerCorpse(Player player, Location location) {
        int currentId = game.getGameId();
        World world = location.getWorld();
        int chunkX = (int) location.getX() >> 4;
        int chunkZ = (int) location.getZ() >> 4;
        world.setChunkForceLoaded(chunkX, chunkZ, true);
        Husk corpse = createCorpseEntity(player, location);
        playerCorpses.add(corpse);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            corpse.remove();
            playerCorpses.remove(corpse);
            world.setChunkForceLoaded(chunkX, chunkZ, false);
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
        return corpse;
    }
    public Entity getCorpse(UUID uuid) {
        if (playerCorpses.isEmpty()) return null;
        for (Entity corpse : playerCorpses) {
            if (uuid.equals(getCorpsePlayerUUID(corpse))) return corpse;
        }
        return null;
    }
    public static UUID getCorpsePlayerUUID(Entity corpse) {
        if (!corpse.hasMetadata("player_uuid")) return null;
        try {
            return UUID.fromString(corpse.getMetadata("player_uuid").getFirst().asString());
        }
        catch (Exception e) {
            return null;
        }
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
