package ikeyler.survutil.game.voting;

import ikeyler.survutil.game.Game;

import java.util.HashMap;
import java.util.Map;

public class VoteManager {
    private final Map<String, Vote> votes = new HashMap<>();
    public VoteManager(Game game) {
        RestartVote restartVote = new RestartVote(game);
        register(restartVote);
    }
    private void register(Vote vote) {
        votes.put(vote.getName(), vote);
    }
    public Vote getVote(String name) {
        return votes.get(name);
    }
}
