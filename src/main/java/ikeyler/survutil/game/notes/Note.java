package ikeyler.survutil.game.notes;

import ikeyler.survutil.Util;
import org.bukkit.Location;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Note {
    private final String time;
    private final String playerName;
    private final String noteText;
    private final Location noteLocation;
    public Note(String playerName, String noteText, Location noteLocation) {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        this.time = LocalDateTime.now().format(timeFormatter);
        this.playerName = playerName;
        this.noteText = noteText;
        this.noteLocation = noteLocation;
    }
    public String formatNote() {
        String location = noteLocation != null ? "\n" + Util.formatCoords(noteLocation, true) : "";
        return String.format("§7%s §e%s §8-> §f%s%s", time, playerName, noteText, location);
    }
}
