package ikeyler.survutil.game;

import ikeyler.survutil.Util;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;

public abstract class GameSettings {
    private final GameType type;
    private final boolean hardcore;
    private final boolean rescueEnabled;
    public GameSettings(GameType type, boolean hardcore, boolean rescueEnabled) {
        this.type = type;
        this.hardcore = hardcore;
        this.rescueEnabled = rescueEnabled;
    }
    public static class Survival extends GameSettings {
        public Survival(boolean hardcore, boolean rescueEnabled) {
            super(GameType.SURVIVAL, hardcore, rescueEnabled);
        }
    }
    public static class ItemSpeedrun extends GameSettings {
        private final Material material;
        public ItemSpeedrun(boolean hardcore, boolean rescueEnabled, Material material) {
            super(GameType.ITEM_SPEEDRUN, hardcore, rescueEnabled);
            if (material == null) throw new IllegalArgumentException("material cannot be null");
            this.material = material;
        }
        @Override
        public Material getMaterial() {
            return material;
        }
        @Override
        public String getAbarInfo() {
            return material.toString();
        }
    }
    public static class AdvancementSpeedrun extends GameSettings {
        private final String advancement;
        public AdvancementSpeedrun(boolean hardcore, boolean rescueEnabled, String advancement) {
            super(GameType.ADVANCEMENT_SPEEDRUN, hardcore, rescueEnabled);
            if (Bukkit.getAdvancement(new NamespacedKey("minecraft", advancement)) == null)
                throw new IllegalArgumentException("advancement not found");
            this.advancement = advancement;
        }
        @Override
        public String getAdvancement() {
            return advancement;
        }
        @Override
        public String getAbarInfo() {
            return Util.getAdvancementName(advancement);
        }
    }
    public GameType getType() {
        return type;
    }
    public boolean hardcore() {
        return hardcore;
    }
    public boolean isRescueEnabled() {
        return rescueEnabled;
    }
    public Material getMaterial() {
        return null;
    }
    public String getAdvancement() {
        return null;
    }
    public String getAbarInfo() {
        return null;
    }
    public boolean hasInfo() {
        return getAbarInfo() != null && !getAbarInfo().isEmpty();
    }
}