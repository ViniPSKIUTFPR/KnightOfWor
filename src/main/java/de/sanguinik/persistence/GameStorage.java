package de.sanguinik.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.*;

public final class GameStorage {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final File SAVE_DIR  = new File(System.getProperty("user.home"), ".knightofwor");
    private static final File SAVE_FILE = new File(SAVE_DIR, "savegame.json");

    private static final ScheduledExecutorService EXEC = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "GameStorage-Saver");
        t.setDaemon(true);
        return t;
    });
    private static ScheduledFuture<?> pending;

    public static final SaveGame saveGame = new SaveGame();   // estado corrente do jogo
    public static final Settings settings = new Settings();   // preferências do usuário

    static {
        loadNow(); // carrega no primeiro acesso
    }

    private GameStorage() {}

    /** Carrega do disco (retrocompatível com saves antigos). */
    private static synchronized void loadNow() {
        try {
            if (!SAVE_FILE.exists() || SAVE_FILE.length() == 0) {
                ensureDir();
                saveNow(); // grava esqueleto inicial
                return;
            }
            try (Reader r = Files.newBufferedReader(SAVE_FILE.toPath(), StandardCharsets.UTF_8)) {
                
            	SaveContainer in = GSON.fromJson(r, SaveContainer.class);
                if (in != null) {
                    if (in.currentLevel != null) saveGame.currentLevel = in.currentLevel;
                    if (in.lives != null)        saveGame.lives        = in.lives;

                    if (in.score != null)         saveGame.score         = in.score;
                    if (in.levelElapsedMs != null)saveGame.levelElapsedMs= in.levelElapsedMs;


                    if (in.settings != null) {
                        if (in.settings.audio != null) {
                            settings.audio.musicVolume  = clamp01(in.settings.audio.musicVolume);
                            settings.audio.musicEnabled = in.settings.audio.musicEnabled != null ? in.settings.audio.musicEnabled : true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized void saveProgressAsync() {
        if (pending != null && !pending.isDone()) pending.cancel(false);
        pending = EXEC.schedule(GameStorage::saveNowSafe, 300, TimeUnit.MILLISECONDS);
    }

     public static synchronized void saveNow() throws IOException {
        ensureDir();
        SaveContainer out = SaveContainer.from(saveGame, settings);
        out.lastUpdated = System.currentTimeMillis();

        File tmp = new File(SAVE_DIR, "savegame.json.tmp");
        try (Writer w = new OutputStreamWriter(new FileOutputStream(tmp), StandardCharsets.UTF_8)) {
            GSON.toJson(out, w);
        }
        if (!tmp.renameTo(SAVE_FILE)) {
            Files.deleteIfExists(SAVE_FILE.toPath());
            Files.move(tmp.toPath(), SAVE_FILE.toPath());
        }
    }

    private static void saveNowSafe() {
        try { saveNow(); } catch (Exception e) { e.printStackTrace(); }
    }

    private static void ensureDir() throws IOException {
        if (!SAVE_DIR.exists() && !SAVE_DIR.mkdirs()) {
            throw new IOException("Não foi possível criar diretório: " + SAVE_DIR);
        }
    }

    private static double clamp01(Double v) {
        if (v == null) return 1.0;
        return Math.max(0.0, Math.min(1.0, v));
    }

    /* -----------------------  MODELOS  ----------------------- */

     private static class SaveContainer {

        Integer currentLevel;
        Integer lives;
        Long    lastUpdated;

        Integer score;           
        Long    levelElapsedMs;  

        Settings settings;

        static SaveContainer from(SaveGame sg, Settings st) {
            SaveContainer c = new SaveContainer();
            c.currentLevel   = sg.currentLevel;
            c.lives          = sg.lives;
            c.score          = sg.score;
            c.levelElapsedMs = sg.levelElapsedMs;
            c.settings       = st;
            return c;
        }
    }

       public static class SaveGame {
        public int  currentLevel   = 1;
        public int  lives          = 3;
        public int  score          = 0;
        public long levelElapsedMs = 0;
    }

       public static class Settings {
        public Audio audio = new Audio();
        public static class Audio {
            public Double  musicVolume  = 1.0; // 0..1
            public Boolean musicEnabled = true;
        }
    }
}
