package de.sanguinik.model;

import javafx.scene.media.AudioClip;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import java.util.Random;
import java.util.List;



public class WizardAttack {
    private final Group root;
    private final Player player;
    private final Maze maze;
    private final Random random = new Random();
    private Timeline attackTimeline;
    private final int warningMillis = 500; // tempo de aviso em ms
    private final int activeMillis = 300;  // tempo de ataque ativo em ms
    private int cellSize = 32; // grid imaginário
    private int minX, minY, maxX, maxY, gridCols, gridRows;
    private static AudioClip blastSound;


    private static AudioClip loadSound(String fileName) {
        try {
            String path = "/de/sanguinik/model/" + fileName;
            AudioClip clip = new AudioClip(WizardAttack.class.getResource(path).toString());
            clip.setVolume(0.2);
            return clip;
        } catch (Exception e) {
            System.err.println("Erro ao carregar som: " + fileName);
            return null;
        }
    }

    public WizardAttack(Group root, Player player, Maze maze) {
        this.root = root;
        this.player = player;
        this.maze = maze;
        calculatePlayableArea();
        if (blastSound == null) blastSound = loadSound("blast.mp3");
    }

    private void calculatePlayableArea() {
        // Calcula a área jogável com base nas paredes do Maze
        List<Rectangle> walls = maze.getWalls();
        if (walls.isEmpty()) {
            minX = 0; minY = 0; maxX = 1024; maxY = 740; // fallback para tela padrão
        } else {
            minX = (int) walls.stream().mapToDouble(r -> r.getX()).min().orElse(0);
            minY = (int) walls.stream().mapToDouble(r -> r.getY()).min().orElse(0);
            maxX = (int) walls.stream().mapToDouble(r -> r.getX() + r.getWidth()).max().orElse(1024);
            maxY = (int) walls.stream().mapToDouble(r -> r.getY() + r.getHeight()).max().orElse(740);
        }
        gridCols = (maxX - minX) / cellSize;
        gridRows = (maxY - minY) / cellSize;
    }

    public void start() {
        attackTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> launchAttack()));
        attackTimeline.setCycleCount(Timeline.INDEFINITE);
        attackTimeline.play();
    }

    public void stop() {
        if (attackTimeline != null) attackTimeline.stop();
    }

    private void launchAttack() {
        boolean isVertical = random.nextBoolean();
        if (isVertical && gridCols > 0) {
            int col = random.nextInt(gridCols);
            int x = minX + col * cellSize;
            Rectangle laser = new Rectangle(x, minY, cellSize, maxY - minY);
            laser.setFill(Color.RED);
            laser.setOpacity(0.3);
            root.getChildren().add(laser);
            Timeline warn = new Timeline(new KeyFrame(Duration.millis(warningMillis), ev -> {
                laser.setOpacity(0.8);
                if (blastSound != null && !blastSound.isPlaying()) blastSound.play();
                if (playerColidesWithLaser(laser)) {
                    player.setAlive(false);
                }
                Timeline remove = new Timeline(new KeyFrame(Duration.millis(activeMillis), ev2 -> root.getChildren().remove(laser)));
                remove.play();
            }));
            warn.play();
        } else if (!isVertical && gridRows > 0) {
            int row = random.nextInt(gridRows);
            int y = minY + row * cellSize;
            Rectangle laser = new Rectangle(minX, y, maxX - minX, cellSize);
            laser.setFill(Color.RED);
            laser.setOpacity(0.3);
            root.getChildren().add(laser);
            Timeline warn = new Timeline(new KeyFrame(Duration.millis(warningMillis), ev -> {
                laser.setOpacity(0.8);
                if (blastSound != null && !blastSound.isPlaying()) blastSound.play();
                if (playerColidesWithLaser(laser)) {
                    player.setAlive(false);
                }
                Timeline remove = new Timeline(new KeyFrame(Duration.millis(activeMillis), ev2 -> root.getChildren().remove(laser)));
                remove.play();
            }));
            warn.play();
        }
    }

    private boolean playerColidesWithLaser(Rectangle laser) {
        return laser.getBoundsInParent().intersects(player.getRectangle().getBoundsInParent());
    }
}
