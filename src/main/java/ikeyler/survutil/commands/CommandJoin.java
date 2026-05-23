package ikeyler.survutil.commands;

import ikeyler.survutil.game.Game;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

public class CommandJoin implements CommandExecutor {
    private final Game game;
    public CommandJoin(Game game) {
        this.game = game;
    }
    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cКоманда доступна только игрокам");
            return true;
        }
        if (!game.isRunning()) {
            sender.sendMessage("§cИгра неактивна");
            return true;
        }
        if (game.getPlayerManager().addPlayer(player)) {
            Bukkit.broadcastMessage(String.format("§e%s §fвступил в игру!", sender.getName()));
        }
        else {
            sender.sendMessage("§cВы не можете войти в игру");
        }
        return true;
    }
}
