package ikeyler.survutil.game.notes;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class NoteManager {
    private final List<Note> notes = new ArrayList<>();
    public NoteManager() {}
    public void addNote(String playerName, String noteText, Location noteLocation) {
        notes.add(new Note(playerName, noteText, noteLocation));
    }
    public List<Note> getNotes() {
        return notes;
    }
    public void clearNotes() {
        notes.clear();
    }
    public String formatNotes() {
        String totalNotes = !notes.isEmpty() ? String.format("Заметки §7(%s§7)§f:", notes.size()) : "§cНет сохраненных заметок";
        StringBuilder sb = new StringBuilder(totalNotes).append("\n");
        notes.forEach(note -> sb.append(note.formatNote()).append("\n"));
        return sb.toString();
    }
}
