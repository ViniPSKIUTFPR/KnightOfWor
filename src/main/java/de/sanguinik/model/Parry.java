package de.sanguinik.model;

import javafx.animation.Interpolator;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.scene.Group;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import javafx.scene.media.AudioClip;

public class Parry {
    private static final String NOTE_PATH = "/de/sanguinik/model/note.png";
    private static final double TAMANHO = 50;
    private static final double DURACAO = 0.3;
    private static final double COOLDOWN = 0.5;

    private static long lastUse = 0;

    public static void play(Group root, ShootingFigure entidade) {
        Parry parry = new Parry();

        if (!parry.checaCooldown(entidade)) return;

        entidade.setBloqueando(true);

        AudioClip whoosh = new AudioClip(Parry.class.getResource("/de/sanguinik/model/woosh.mp3").toString());
        whoosh.setVolume(0.3);
        whoosh.play();

        parry.animacao(entidade, root);
    }

    public boolean checaCooldown(ShootingFigure entidade) {
        
        long now = System.nanoTime();
        double secondsSinceLast = (now - lastUse) / 1_000_000_000.0;
        if (secondsSinceLast < COOLDOWN) {
            return false;
        }
        lastUse = now;

        // Remove o efeito de brilho no fim do cooldown
        PauseTransition ready = new PauseTransition(Duration.seconds(COOLDOWN));
        ready.setOnFinished(e -> entidade.getImageView().setEffect(null));
        ready.play();

        return true;
    }

    public void animacao(ShootingFigure entidade, Group root) {

        ImageView targetView = entidade.getImageView();

        double width = targetView.getFitWidth();
        double height = targetView.getFitHeight();
        Position centerPosition = new Position((targetView.getX() + width / 2), (targetView.getY() + height / 2));

        Image noteImg = new Image(Parry.class.getResource(NOTE_PATH).toString());
        ImageView noteView = new ImageView(noteImg);
        noteView.setFitWidth(TAMANHO);
        noteView.setFitHeight(TAMANHO);
        noteView.setX(centerPosition.getX() + width / 2);
        noteView.setY(centerPosition.getY() + height / 2);

        root.getChildren().add(noteView);

        RotateTransition rotate = new RotateTransition(Duration.seconds(DURACAO), noteView);
        rotate.setByAngle(360);
        rotate.setInterpolator(Interpolator.EASE_OUT);

        javafx.animation.Timeline followTimeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(Duration.millis(10), ev -> {
                double cx = targetView.getX() + width / 2;
                double cy = targetView.getY() + height / 2;
                noteView.setX(cx + width / 2);
                noteView.setY(cy + height / 2);
            })
        );
        followTimeline.setCycleCount(RotateTransition.INDEFINITE);

        rotate.setOnFinished(ev -> {
            followTimeline.stop();
            root.getChildren().remove(noteView);
        });

        // Efeito escuro durante a animação
        ColorAdjust sombra = new ColorAdjust();
        sombra.setBrightness(-0.7);
        targetView.setEffect(sombra);

        followTimeline.play();
        rotate.play();

        // Finalizando o bloqueio e deixando um efeito de brilho pra indicar que está em cooldown
        PauseTransition pause = new PauseTransition(Duration.seconds(DURACAO));
        pause.setOnFinished(e -> {
            sombra.setBrightness(0.7);
            targetView.setEffect(sombra);
            entidade.setBloqueando(false);
        });
        pause.play();
    }
}