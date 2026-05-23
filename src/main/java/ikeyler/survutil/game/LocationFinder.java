package ikeyler.survutil.game;

import ikeyler.survutil.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Random;
import java.util.logging.Logger;

public class LocationFinder {
    private final Game game;
    private final Logger logger = Main.getLog();
    private final Random random = new Random();
    private final int locationSearchRadius = 30000;
    private final int locationOffsetRadius = 3500;
    public LocationFinder(Game game) {
        this.game = game;
    }
    public Location resolveLocation(Location location) {
        if (location != null) return location;
        if (game.getStartLocation() != null) return game.getStartLocation();
        return new Location(Bukkit.getWorlds().getFirst(), 0, 0, 0);
    }
    public Location findGameLocation(Location centerLoc) {
        if (centerLoc == null) return null;
        World world = centerLoc.getWorld();
        int centerX = (int) centerLoc.getX();
        int centerZ = (int) centerLoc.getZ();
        logger.info("started finding game location");
        for (int i = 0; i < 15; i++) {
            int randX = centerX + random.nextInt(-locationSearchRadius, locationSearchRadius);
            int randZ = centerZ + random.nextInt(-locationSearchRadius, locationSearchRadius);
            int dx = randX - centerX;
            int dz = randZ - centerZ;
            int distance = (int) Math.sqrt(dx * dx + dz * dz);
            if (distance < locationOffsetRadius) continue;
            world.loadChunk(randX >> 4, randZ >> 4);
            Block yBlock = world.getHighestBlockAt(randX, randZ);
            if (!yBlock.isLiquid()) {
                return new Location(world, randX, yBlock.getY() + 1, randZ);
            }
        }
        return null;
    }
}
