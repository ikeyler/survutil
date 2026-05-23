package ikeyler.survutil;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;

import java.util.*;

public class Util {
    public static void revokeAdvancements(Player player) {
        Iterator<Advancement> advancements = Bukkit.getServer().advancementIterator();
        while (advancements.hasNext()) {
            Advancement advancement = advancements.next();
            AdvancementProgress progress = player.getAdvancementProgress(advancement);
            for (String s : progress.getAwardedCriteria()) {
                progress.revokeCriteria(s);
            }
        }
    }
    public static void broadcastAbar(String message) {
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
        }
    }
    // todo use localization
    public static String formatBoolean(boolean bool) {
        return bool ? "§aДа" : "§cНет";
    }
    public static String formatTime(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;
        if (hours > 0) return String.format("%02d:%02d:%02d", hours, minutes, secs);
        else return String.format("%02d:%02d", minutes, secs);
    }
    public static String formatCoords(Location location, boolean includeWorld) {
        String world = includeWorld ? " §7(§b" + location.getWorld().getName() + "§7)" : "";
        return String.format("§b%s %s %s%s", (int) location.getX(), (int) location.getY(), (int) location.getZ(), world);
    }
    public static int getSleepingPlayers() {
        int sleeping = 0;
        Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        if (players.isEmpty()) return 0;
        for (Player player : players) {
            if (player.isSleeping()) sleeping++;
        }
        return sleeping;
    }
    public static int getAbleToSleepPlayers() {
        int sleepAble = 0;
        Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        if (players.isEmpty()) return 0;
        for (Player player : players) {
            if (isAbleToSleep(player)) sleepAble++;
        }
        return sleepAble;
    }
    public static boolean isAbleToSleep(Player player) {
        if (player.isDead()) return false;
        if (player.getGameMode() == GameMode.SPECTATOR) return false;
        if (player.getWorld().getEnvironment() != World.Environment.NORMAL) return false;
        return true;
    }
    public static String getAdvancementName(String advancementName) {
        Advancement advancement = Bukkit.getAdvancement(new NamespacedKey("minecraft", advancementName.toLowerCase()));
        if (advancement != null)
            return advancement.getDisplay().getTitle();
        return null;
    }
    public static void drawParticleLine(Location start, Location end, Particle particle) {
        World world = start.getWorld();
        double distance = start.distance(end);
        int particles = (int) (distance * 10);
        for (int i = 0; i <= particles; i++) {
            double t = (double) i / particles;
            double x = start.getX() + (end.getX() - start.getX()) * t;
            double y = start.getY() + (end.getY() - start.getY()) * t;
            double z = start.getZ() + (end.getZ() - start.getZ()) * t;
            Location point = new Location(world, x, y, z);
            world.spawnParticle(particle, point, 1, 0, 0, 0, 0);
        }
    }
}
