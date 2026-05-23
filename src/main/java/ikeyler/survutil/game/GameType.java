package ikeyler.survutil.game;

public enum GameType {
    SURVIVAL("Выживание"),
    ITEM_SPEEDRUN("Спидран предмета"),
    ADVANCEMENT_SPEEDRUN("Спидран достижения");
    private final String name;
    GameType(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
    public static GameType fromString(String s) {
        if (s == null) return null;
        for (GameType type : values()) {
            if (type.toString().equalsIgnoreCase(s)) return type;
        }
        return null;
    }
}
