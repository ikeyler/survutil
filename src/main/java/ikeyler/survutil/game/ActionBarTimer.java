package ikeyler.survutil.game;

import ikeyler.survutil.Util;
import ikeyler.survutil.game.player.GamePlayer;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

public class ActionBarTimer {
    private final Game game;
    private int seconds = 0;
    private boolean running = false;
    private boolean showInfoMode = false;
    private String deathSkullColor = "§c";
    private String abarInfo;
    public ActionBarTimer(Game game) {
        this.game = game;
    }
    public void update() {
        if (!showInfoMode)
            abarInfo = game.getGameSettings().hasInfo() ? " §7• §b" + game.getGameSettings().getAbarInfo() : "";
        if (!running || Bukkit.getOnlinePlayers().isEmpty()) return;
        Util.broadcastAbar(formatBar(seconds));
        seconds++;
    }
    private String formatBar(int seconds) {
        if (showInfoMode) return abarInfo;
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;
        String minutesColor = (minutes > 0 || hours > 0) ? "§f" : "§7";
        List<String> iconList = getIcons();
        String icons = !iconList.isEmpty() ? String.join(" §7• ", iconList) + " §7• " : "";
        String attempt = game.getAttempt() > 1 ? " §7• §e#" + game.getAttempt() : "";
        String time;
        if (hours > 0) time = String.format("§f%02d§8:%s%02d§8:§f%02d", hours, minutesColor, minutes, secs);
        else time = String.format("%s%02d§8:§f%02d", minutesColor, minutes, secs);
        return String.format("%s§b⏰ §f%s%s%s", icons, time, abarInfo, attempt);
    }

    private List<String> getIcons() {
        List<String> icons = new ArrayList<>();
        if (Util.getSleepingPlayers() > 0)
            icons.add(String.format("§3Zzz§f (%s/%s)", Util.getSleepingPlayers(), Util.getAbleToSleepPlayers()));
        if (!game.getPlayerManager().getRescuingPlayers().isEmpty()) {
            deathSkullColor = deathSkullColor.equals("§c") ? "§4" : "§c";
            String skull = deathSkullColor + "☠§7 ";
            List<GamePlayer> players = game.getPlayerManager().getRescuingPlayers();
            for (GamePlayer player : players) {
                icons.add(skull + player.getPlayer().getName());
            }
        }
        return icons;
    }
    public void start() {
        if (running) return;
        running = true;
        reset();
    }
    public void stop() {
        if (!running) return;
        running = false;
        reset();
    }
    public void reset() {
        seconds = 0;
        showInfoMode = false;
    }
    public void setTime(int secs) {
        this.seconds = secs;
    }
    public int getSeconds() {
        return this.seconds;
    }
    public boolean isRunning() {
        return this.running;
    }
    public boolean isTimerRunning() {
        return this.running && !this.showInfoMode && this.seconds > 0;
    }
    public void setShowInfoMode(boolean infoMode) {
        this.showInfoMode = infoMode;
        update();
    }
    public void setInfoLabel(String abarInfo) {
        this.showInfoMode = abarInfo != null;
        this.abarInfo = abarInfo;
    }
}
