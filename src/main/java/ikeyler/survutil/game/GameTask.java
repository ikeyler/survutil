package ikeyler.survutil.game;

import ikeyler.survutil.Main;
import org.bukkit.scheduler.BukkitRunnable;

public class GameTask extends BukkitRunnable {
    private final Game game;
    private boolean running;
    public GameTask(Game game) {
        this.game = game;
        this.running = false;
    }
    @Override
    public void run() {
        game.getBarTimer().update();
        //game.getPlayerManager().getPlayerList().values().forEach(player -> System.out.println(player.toString()));
        if (game.isRunning() && game.getPlayerManager().getAlivePlayers().isEmpty()) {
            game.stopGame();
        }
    }
    public void start() {
        if (running) return;
        running = true;
        this.runTaskTimer(Main.getInstance(), 20L, 20L);
    }
    public void stop() {
        if (!running) return;
        running = false;
        this.cancel();
    }
}
