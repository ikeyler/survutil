package ikeyler.survutil;

import ikeyler.survutil.game.ActionBarTimer;
import ikeyler.survutil.game.Game;
import ikeyler.survutil.game.GameSettings;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class ChatListener implements Listener {

    //todo rewrite into commands

    private final Plugin plugin = Main.getInstance();
    private final Game game = Game.getInstance();
    private final ActionBarTimer barTimer = game.getBarTimer();
    private final Set<String> opCommands = Set.of(
            ".restart", ".item", ".hardcore", ".adv",
            ".time", ".hard", ".rescue", ".c", ".respawn");
    private boolean hardcore = true;
    private boolean rescueEnabled = true;
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        String cmd = event.getMessage().split(" ")[0].toLowerCase();
        String message = event.getMessage();
        if (opCommands.contains(cmd)) event.setCancelled(true);
        if (!event.getPlayer().isOp()) return;
        String[] args = event.getMessage().split(" ");
        Player player = event.getPlayer();
        switch (cmd) {
            case ".c":
                Bukkit.getScheduler().runTask(plugin, () -> game.getCorpseManager().createPlayerCorpse(player, player.getLocation()));
                break;
            case ".restart":
                Bukkit.getScheduler().runTask(plugin, () -> game.restartGame(player.getLocation()));
                break;
            case ".item":
                if (args.length < 2) return;
                Material material = Material.matchMaterial(args[1]);
                if (material != null) {
                    game.getItemTracker().setItem(material);
                    game.setGameSettings(new GameSettings.ItemSpeedrun(hardcore, rescueEnabled, material));
                    player.sendMessage("§7§oУстановлен предмет для спидрана: §b" + material);
                }
                break;
            case ".hardcore":
                game.setGameSettings(new GameSettings.Survival(hardcore, rescueEnabled));
                player.sendMessage("§7§oТип игры: §cхардкор");
                break;
            case ".hard":
                hardcore = !hardcore;
                player.sendMessage("Хардкор: " + hardcore);
                break;
            case ".rescue":
                rescueEnabled = !rescueEnabled;
                player.sendMessage("Возможность спасения: " + rescueEnabled);
                break;
            case ".adv":
                if (args.length < 2) return;
                String advancement = args[1].toLowerCase();
                if (Bukkit.getAdvancement(new NamespacedKey("minecraft", advancement)) != null) {
                    game.setGameSettings(new GameSettings.AdvancementSpeedrun(hardcore, rescueEnabled, advancement));
                    player.sendMessage("§7§oУстановлено достижение для спидрана: §b" + advancement);
                }
                break;
            case ".time":
                if (args.length < 2) return;
                try {
                    int seconds = Integer.parseInt(args[1]);
                    barTimer.setTime(seconds);
                    player.sendMessage("Время установлено: §e§l" + seconds);
                }
                catch (NumberFormatException e) {
                    player.sendMessage("Введите время в секундах");
                }
                break;
            case ".respawn":
                if (args.length < 2) return;
                String playerName = args[1];
                Player respawnPlayer = Bukkit.getPlayer(playerName);
                if (respawnPlayer != null && game.containsPlayer(respawnPlayer.getUniqueId())) {
                    Bukkit.getScheduler().runTask(plugin, () -> game.getPlayerManager().respawnPlayer(respawnPlayer, player.getLocation()));
                }
                else player.sendMessage("Игрок не найден");
                break;
        }
    }
}
