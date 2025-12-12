package de.sanguinik.util;

import javafx.scene.media.MediaPlayer;

public class VolumeManager {
    
    private static final String VOLUME_KEY = "game_volume";
    private static double currentVolume = loadVolume();
    
    /**
     * Carrega o volume salvo (padrão: 0.7 = 70%)
     */
    private static double loadVolume() {
        // Por enquanto, usamos um valor padrão
        // Depois você pode implementar salvamento em arquivo
        return 0.7;
    }
    
    /**
     * Define o volume master do jogo
     * @param volume valor entre 0.0 e 1.0
     */
    public static void setVolume(double volume) {
        if (volume < 0.0) volume = 0.0;
        if (volume > 1.0) volume = 1.0;
        
        currentVolume = volume;
        // Aqui você pode adicionar salvamento em arquivo depois
    }
    
    /**
     * Obtém o volume atual
     */
    public static double getVolume() {
        return currentVolume;
    }
    
    /**
     * Aplica o volume a um MediaPlayer
     */
    public static void applyVolumeToPlayer(MediaPlayer player) {
        if (player != null) {
            player.setVolume(currentVolume);
        }
    }
    
    /**
     * Aplica o volume a múltiplos MediaPlayers
     */
    public static void applyVolumeToPlayers(MediaPlayer... players) {
        for (MediaPlayer player : players) {
            applyVolumeToPlayer(player);
        }
    }
}