package ikeyler.survutil.game;

import ikeyler.survutil.Main;
import ikeyler.survutil.Util;
import ikeyler.survutil.game.player.GamePlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class PlayerRescueTask extends BukkitRunnable {
    private final Game game;
    private boolean running;
    private final int rescueProgressStep = 20;
    private final int maxRescueDistance = 5;
    private final Set<UUID> rescuersList = new HashSet<>();
    private final NamespacedKey pidKey = new NamespacedKey(Main.getInstance(), "player_uuid");
    public PlayerRescueTask(Game game) {
        this.game = game;
        this.running = false;
    }
    @Override
    public void run() {
        game.getPlayerManager().getOnlinePlayers().forEach(gamePlayer -> updatePlayer(gamePlayer.getPlayer()));
        List<Entity> playerCorpses = new ArrayList<>(game.getCorpseManager().getPlayerCorpses());
        if (game.isRunning() && !playerCorpses.isEmpty() && !rescuersList.isEmpty()) {
            for (UUID uuid : rescuersList) {
                Player rescuer = Bukkit.getPlayer(uuid);
                if (rescuer == null) continue;
                for (Entity corpse : playerCorpses) {
                    if (rescuer.getLocation().getWorld() != corpse.getLocation().getWorld()) continue;
                    double distance = rescuer.getLocation().distance(corpse.getLocation());
                    if (distance <= maxRescueDistance) {
                        if (!corpse.getPersistentDataContainer().has(pidKey, PersistentDataType.STRING)) continue;
                        UUID rescuedUUID = Util.getUuid(corpse.getPersistentDataContainer().get(pidKey, PersistentDataType.STRING));
                        if (rescuedUUID == null) continue;
                        GamePlayer rescuedPlayer = game.getGamePlayer(rescuedUUID);
                        if (rescuedPlayer == null || !rescuedPlayer.isRescueAvailable()) continue;
                        int progress = rescuedPlayer.getRescueProgress() + rescueProgressStep;
                        if (progress < 100) {
                            Util.drawParticleLine(rescuer.getLocation(), corpse.getLocation().add(0, 1, 0), Particle.CLOUD);
                            rescuedPlayer.setRescueProgress(progress);
                            rescuer.sendMessage(String.format("Возрождение §e%s§f... §7(%d%%)",
                                    rescuedPlayer.getPlayer().getName(), progress));
                        }
                        else {
                            rescuer.getInventory().setItemInMainHand(null);
                            rescuer.getInventory().setItemInOffHand(null);
                            rescuedPlayer.setRescueProgress(0);
                            rescuedPlayer.setRescued(true);
                            rescuedPlayer.setRescueAvailable(false);
                            game.getCorpseManager().removeCorpse(corpse);
                            game.getPlayerManager().respawnPlayer(rescuedPlayer.getPlayer(), corpse.getLocation());
                        }
                    }
                }
            }
        }
    }
    private void updatePlayer(Player player) {
        if (player.isSneaking()) {
            Material hand = player.getInventory().getItemInMainHand().getType();
            Material offHand = player.getInventory().getItemInOffHand().getType();
            if (hand == Material.TOTEM_OF_UNDYING && offHand == Material.TOTEM_OF_UNDYING) {
                rescuersList.add(player.getUniqueId());
            }
            else {
                rescuersList.remove(player.getUniqueId());
            }
        }
        else {
            rescuersList.remove(player.getUniqueId());
        }
    }
    public void start() {
        if (running) return;
        running = true;
        this.runTaskTimer(Main.getInstance(), 0L, 10L);
    }
    public void stop() {
        if (!running) return;
        running = false;
        this.cancel();
    }
}
