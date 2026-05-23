package ikeyler.survutil.game;

import ikeyler.survutil.Main;
import ikeyler.survutil.Util;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class ItemTracker {
    private final Plugin plugin = Main.getInstance();
    private final Game game;
    private int taskId = -1;
    private boolean running = false;
    private Material material;
    private final Set<UUID> playerList = new HashSet<>();
    public ItemTracker(Game game) {
        this.game = game;
    }
    public void start() {
        if (running) return;
        if (material == null) {
            Bukkit.broadcastMessage("§cПредмет для спидрана не указан");
            return;
        }
        running = true;
        playerList.clear();
        taskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (game.getGameSettings().getType() == GameType.ITEM_SPEEDRUN)
                    checkInventory(player);
                else stop();
            }
        }, 0L, 10L).getTaskId();
    }
    public void stop() {
        if (!running) return;
        running = false;
        playerList.clear();
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }
    private void checkInventory(Player player) {
        if (player.getInventory().contains(this.material) && !playerList.contains(player.getUniqueId())) {
            playerList.add(player.getUniqueId());
            Bukkit.broadcastMessage("\n§e" + player.getName() + " §7получил предмет за §b" + Util.formatTime(game.getBarTimer().getSeconds()));
        }
    }
    public void setItem(Material material) {
        this.material = material;
    }
    public boolean isRunning() {
        return running;
    }
}
