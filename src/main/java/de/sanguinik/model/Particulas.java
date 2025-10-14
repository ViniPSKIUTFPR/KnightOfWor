package de.sanguinik.model;

import javafx.animation.*;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.Random;

public class Particulas {
    private static final int NUM_PARTICLES = 12;   // quantidade de particulas
    private static final double PARTICLE_SIZE = 4; // raio inicial das particulas
    private static final double MAX_DISTANCE = 100; // distância máxima que cada particula vai
    private static final double LIFETIME = 0.6;    // duração em segundos da animacao

    public static void play(Group root, Position centerPosition) {
        Random rnd = new Random();

        for (int i = 0; i < NUM_PARTICLES; i++) {
            Circle particle = new Circle(PARTICLE_SIZE, Color.YELLOW);
            particle.setTranslateX(centerPosition.getX());
            particle.setTranslateY(centerPosition.getY());
            root.getChildren().add(particle);

            // direção aleatória
            double angle = rnd.nextDouble() * 2 * Math.PI;
            double distX = Math.cos(angle) * (rnd.nextDouble() * MAX_DISTANCE);
            double distY = Math.sin(angle) * (rnd.nextDouble() * MAX_DISTANCE);

            TranslateTransition move = new TranslateTransition(Duration.seconds(LIFETIME), particle);
            move.setByX(distX);
            move.setByY(distY);

            FadeTransition fade = new FadeTransition(Duration.seconds(LIFETIME), particle);
            fade.setFromValue(1.0);
            fade.setToValue(0.0);

            ScaleTransition scale = new ScaleTransition(Duration.seconds(LIFETIME), particle);
            scale.setFromX(1.0);
            scale.setFromY(1.0);
            scale.setToX(0.3);
            scale.setToY(0.3);

            ParallelTransition pt = new ParallelTransition(move, fade, scale);
            pt.setOnFinished(e -> root.getChildren().remove(particle));
            pt.play();
        }
    }
}
