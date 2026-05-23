package ikeyler.survutil.game;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.World;

public class WorldManager {
    public void resetWorlds(GameSettings gameSettings) {
        for (World world : Bukkit.getWorlds()) {
            resetWorld(world, gameSettings);
        }
    }
    private void resetWorld(World world, GameSettings gameSettings) {
        world.setTime(0L);
        world.setStorm(false);
        world.setThundering(false);
        if (gameSettings.hardcore()) {
            world.setDifficulty(Difficulty.HARD);
        }
        world.setHardcore(gameSettings.hardcore());
    }
    public void preloadChunks(World world, int chunkX, int chunkZ) {
        for (int cx = -1; cx <= 1; cx++) {
            for (int cz = -1; cz <= 1; cz++) {
                world.getChunkAt(chunkX + cx, chunkZ + cz).load(true);
            }
        }
    }
}
