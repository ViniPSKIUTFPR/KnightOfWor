package de.sanguinik.persistence;

import com.google.gson.Gson;
import de.sanguinik.model.GameState;
import java.io.*;

public class GameStateManager {
    private static final String SAVE_FILE = "savegame.json";
    private static final Gson gson = new Gson();

    public static void save(GameState state) throws IOException {
        try (Writer writer = new FileWriter(SAVE_FILE)) {
            gson.toJson(state, writer);
        }
    }

    public static GameState load() throws IOException {
        File f = new File(SAVE_FILE);
        if (!f.exists()) return null;

        try (Reader reader = new FileReader(f)) {
            return gson.fromJson(reader, GameState.class);
        }
    }
}
