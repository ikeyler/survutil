package ikeyler.survutil;

import ikeyler.survutil.commands.*;
import ikeyler.survutil.game.ActionBarTimer;
import ikeyler.survutil.game.Game;
import ikeyler.survutil.game.player.GamePlayer;
import ikeyler.survutil.game.player.PlayerState;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.logging.Logger;

public final class Main extends JavaPlugin {
    private static Main instance;
    private static Logger logger;
    private Game game;
    private ConfigManager configManager;
    public static Main getInstance() {
        return instance;
    }
    public static Logger getLog() {
        return logger;
    }
    @Override
    public void onEnable() {
        instance = this;
        logger = getLogger();
        game = Game.getInstance();
        configManager = new ConfigManager(this);
        getServer().getPluginManager().registerEvents(new ChatListener(), this);
        registerCommands();
        loadGame();
    }
    private void registerCommands() {
        getCommand("join").setExecutor(new CommandJoin(game));
        getCommand("leave").setExecutor(new CommandLeave(game));
        getCommand("vote").setExecutor(new CommandVote(game));
        getCommand("note").setExecutor(new CommandNote(game));
        getCommand("notes").setExecutor(new CommandNotes(game));
    }
    private void loadGame() {
        ActionBarTimer timer = game.getBarTimer();
        timer.start();
        if (configManager.isGameRunning() && configManager.isTimerRunning()) {
            game.restartGameFromConfig(configManager.isGameHardcore(),
                    configManager.isGameRescueEnabled(),
                    configManager.getGameAttempt(),
                    configManager.getGameLocation(),
                    configManager.getTimerSeconds()
            );
            game.getPlayerManager().loadPlayers(configManager.getGamePlayers());
            configManager.resetGamePlayers();
            logger.info("added " + game.getPlayerManager().getPlayerList().size() + " players to the game");
            game.getCorpseManager().loadCorpses();
        }
        else timer.setInfoLabel("§7§oНачните игру, введя §e§o/vote");
    }
    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        game.getCorpseManager().updateCorpses();
        if (game.isRunning() && game.getBarTimer().isTimerRunning()) {
            for (GamePlayer gamePlayer : game.getPlayerManager().getOnlinePlayers()) {
                gamePlayer.setState(PlayerState.OFFLINE);
            }
            configManager.saveGame(true, game.getAttempt(), game.getGameSettings().hardcore(), game.getGameSettings().isRescueEnabled(), game.getStartLocation());
            configManager.saveGamePlayers(new ArrayList<>(game.getPlayerManager().getPlayerList().values()));
            configManager.saveTimer(true, game.getBarTimer().getSeconds());
        }
        else {
            configManager.resetGame();
            configManager.resetGamePlayers();
            configManager.resetTimer();
        }
    }
}
