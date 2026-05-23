package ikeyler.survutil.game;

import ikeyler.survutil.Main;
import ikeyler.survutil.Util;
import ikeyler.survutil.game.player.GamePlayer;
import ikeyler.survutil.game.player.PlayerState;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class GameEventHandler implements Listener {
    private final Game game;
    public GameEventHandler(Game game) {
        this.game = game;
        Bukkit.getPluginManager().registerEvents(this, Main.getInstance());
    }
    @EventHandler
    public void onPlayerDie(PlayerDeathEvent event) {
        if (game.isRunning() && game.getGameSettings().hardcore()) {
            if (!game.containsPlayer(event.getEntity().getUniqueId())) return;
            GamePlayer gamePlayer = game.getGamePlayer(event.getEntity().getUniqueId());
            if (!gamePlayer.isAlive()) return;
            event.getEntity().setGameMode(GameMode.SPECTATOR);
            gamePlayer.setAlive(false);
            if (!game.getPlayerManager().getAlivePlayers().isEmpty()) {
                game.createPlayerCorpse(event.getEntity(), event.getEntity().getLocation());
                if (gamePlayer.isRescueAvailable() && game.getGameSettings().isRescueEnabled()) {
                    gamePlayer.setState(PlayerState.RESCUING);
                    Bukkit.broadcastMessage("Один из игроков умер! Возродите его, используя §eдва тотема бессмертия §fв течение §e5 минут");
                }
                else {
                    gamePlayer.setState(PlayerState.SPECTATING);
                    Bukkit.broadcastMessage(String.format("§e%s §fпокинул этот мир...", gamePlayer.getPlayer().getName()));
                }
            }
            else {
                Bukkit.broadcastMessage("Все игроки погибли! Введите §e§l/vote§f, чтобы проголосовать за рестарт");
            }
            game.getVoteManager().getVote("restart").start();
        }
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (game.containsPlayer(player.getUniqueId())) {
            GamePlayer gamePlayer = game.getGamePlayer(player.getUniqueId());
            if (gamePlayer.getPlayer() == null) {
                gamePlayer.updatePlayer(player);
            }
            if (gamePlayer.isAlive()) {
                gamePlayer.setState(PlayerState.PLAYING);
                player.sendMessage("Вы вернулись в игру");
            }
        }
        else if (game.isRunning()) {
            player.sendMessage("Вы не участник игры. Войдите, используя §e.join");
        }
    }
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (game.containsPlayer(player.getUniqueId())) {
            GamePlayer gamePlayer = game.getGamePlayer(player.getUniqueId());
            if (gamePlayer.isPlaying())
                game.getGamePlayer(player.getUniqueId()).setState(PlayerState.OFFLINE);
        }
    }
    @EventHandler
    public void onAdvancementGet(PlayerAdvancementDoneEvent event) {
        if (game.getGameSettings().getType() == GameType.ADVANCEMENT_SPEEDRUN) {
            if (event.getAdvancement().getKey().getKey().equals(game.getGameSettings().getAdvancement()))
                Bukkit.broadcastMessage("\n§e" + event.getPlayer().getName() + " §7получил достижение за §b" + Util.formatTime(game.getBarTimer().getSeconds()));
        }
    }
    @EventHandler
    public void onEntityDamaged(EntityDamageEvent event) {
        if (game.getPlayerCorpses().contains(event.getEntity()))
            event.setCancelled(true);
    }
}
