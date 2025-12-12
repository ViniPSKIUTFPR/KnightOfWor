package de.sanguinik.view;

public class FPSConfig {


    private static int fps = 60;

    public static int getFPS() {
        return fps;
    }

    public static void setFPS(int newFps) {

        if (newFps < 15) {
            newFps = 15;
        } else if (newFps > 240) {
            newFps = 240;
        }
        fps = newFps;
    }
}
