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
    private int gridCols, gridRows;
    private Position minPosition, maxPosition;
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

    public Position pickRandomTeleportPosition(final int maxAttempts, final double width, final double height, final CollisionDetector cd) {
        if (gridCols <= 0 || gridRows <= 0) {
            // fallback: área inteira
            double startX = minPosition.getX();
            double startY = minPosition.getY();
            for (int i = 0; i < maxAttempts; i++) {
                double x = startX + random.nextDouble() * Math.max(1, (maxPosition.getX() - minPosition.getX() - width));
                double y = startY + random.nextDouble() * Math.max(1, (maxPosition.getY() - minPosition.getY() - height));
                Rectangle test = new Rectangle(x, y, width, height);
                if (!cd.isCollide(maze.getWalls(), test)) {
                    return new Position(x, y);
                }
            }
            return null;
        }

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            int col = random.nextInt(Math.max(1, gridCols));
            int row = random.nextInt(Math.max(1, gridRows));
            double x = minPosition.getX() + col * cellSize;
            double y = minPosition.getY() + row * cellSize;
            Rectangle test = new Rectangle(x, y, width, height);
            if (!cd.isCollide(maze.getWalls(), test)) {
                return new Position(x, y);
            }
        }
        return null;
    }

    private void calculatePlayableArea() {
        // Calcula a área jogável com base nas paredes do Maze
        List<Rectangle> walls = maze.getWalls();
        if (walls.isEmpty()) {
            // fallback para tela padrão
            minPosition = new Position(0, 0);
            maxPosition = new Position(1024, 740);
        } else {
            minPosition = new Position(
                    (int) walls.stream().mapToDouble(r -> r.getX()).min().orElse(0),
                    (int) walls.stream().mapToDouble(r -> r.getY()).min().orElse(0)
            );
            maxPosition = new Position(
                    (int) walls.stream().mapToDouble(r -> r.getX() + r.getWidth()).max().orElse(1024),
                    (int) walls.stream().mapToDouble(r -> r.getY() + r.getHeight()).max().orElse(740)
            );
        }
        gridCols = (int) (maxPosition.getX() - minPosition.getX()) / cellSize;
        gridRows = (int) (maxPosition.getY() - minPosition.getY()) / cellSize;
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
            int x = (int) minPosition.getX() + col * cellSize;
            final Rectangle laser = new Rectangle(x, minPosition.getY(), cellSize, maxPosition.getY() - minPosition.getY());
            handleLaserAttack(laser);
        } else if (!isVertical && gridRows > 0) {
            int row = random.nextInt(gridRows);
            int y = (int) minPosition.getY() + row * cellSize;
            final Rectangle laser = new Rectangle(minPosition.getX(), y, maxPosition.getX() - minPosition.getX(), cellSize);
            handleLaserAttack(laser);
        }
    }

    private void handleLaserAttack(final Rectangle laser) {
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

    private boolean playerColidesWithLaser(Rectangle laser) {
        return laser.getBoundsInParent().intersects(player.getRectangle().getBoundsInParent());
    }
}
