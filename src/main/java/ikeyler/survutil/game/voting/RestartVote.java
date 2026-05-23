package ikeyler.survutil.game.voting;

import ikeyler.survutil.Util;
import ikeyler.survutil.game.Game;
import org.bukkit.entity.Player;

public class RestartVote extends Vote {
    public RestartVote(Game game) {
        super(game, "restart");
    }

    @Override
    public void onDone() {
        Util.broadcastAbar("§7§oГолосование окончено");
        game.restartGame(null);
    }
    @Override
    protected String getVoteMessage(Player player) {
        return String.format("§e%s §fпроголосовал за рестарт! §7(%s/%s)", player.getName(), getVotes(), getRequiredVotes());
    }
}