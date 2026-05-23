package ikeyler.survutil.commands;

import ikeyler.survutil.game.Game;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

public class CommandNotes implements CommandExecutor {
    private final Game game;
    public CommandNotes(Game game) {
        this.game = game;
    }
    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cКоманда доступна только игрокам");
            return true;
        }
        if (!game.isRunning()) {
            sender.sendMessage("§cИгра неактивна");
            return true;
        }
        sender.sendMessage(game.getNoteManager().formatNotes());
        return true;
    }
}
