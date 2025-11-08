package de.sanguinik.view;

import de.sanguinik.persistence.GameStorage;
import de.sanguinik.persistence.Highscore;
import de.sanguinik.persistence.HighscoreImpl;
import de.sanguinik.model.HighscoreModel;

import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.KeyValue;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import de.sanguinik.model.Bullet;
import de.sanguinik.model.Enemy;
import de.sanguinik.model.Keyboard;
import de.sanguinik.model.Maze;
import de.sanguinik.model.Player;
import de.sanguinik.model.Position;
import de.sanguinik.model.ShootCallback;
import de.sanguinik.model.Target;
import de.sanguinik.model.TypeOfFigure;

public class PlayFieldScreen extends Application {

    private class ShootCallbackImpl implements ShootCallback {
        @Override
        public void shootBullet(final Bullet bullet) {
            bulletList.add(bullet);
            root.getChildren().add(bullet.getGroup());
        }
    }

    private int lastSavedScore = -1;

    private int currentLevel = Math.max(1, GameStorage.saveGame.currentLevel);
    private int lives        = GameStorage.saveGame.lives > 0 ? GameStorage.saveGame.lives : 3;
    private int score        = 0;

    private long levelStartMillis;
    private final Highscore highscoreRepo = new HighscoreImpl();

    private final Timeline timeline = new Timeline();
    private final List<Enemy> enemyList = new ArrayList<>();
    private final List<Bullet> bulletList = new ArrayList<>();
    private static final int ONE_SECOND = 1000;
    private static final int FPS = 30;
    private final Group root = new Group();
    private boolean gameWasPaused = true;
    private final Label pause = new Label("PAUSE");

    // Timer de fase
    private long levelStartTime;
    private long pausedTime = 0;
    private long pauseStartTime = 0;
    private long finalLevelTime = 0;
    private Label timeLabel;
    private boolean levelCompleted = false;
    private boolean gamePaused = false;
    private int lastTimeBonus = 0;

    // throttle (frame) para persistir tempo 1x por segundo
    private long lastPersistedSecond = -1;

    private HighscoreModel entry;

    private Media music;
    private MediaPlayer mediaPlayer1;
    private MediaPlayer mediaPlayer2;
    private MediaPlayer currentPlayer;
    private boolean useFirstPlayer = true;

    private ScheduledExecutorService saverExec;
    private ScheduledFuture<?> saverTask;

    private void startLevel() {
        levelStartMillis = System.currentTimeMillis();
        GameStorage.saveGame.currentLevel = currentLevel;
        GameStorage.saveGame.lives = lives;
        GameStorage.saveProgressAsync();
    }

    private void onPlayerDeath() {
        lives = Math.max(0, lives - 1);
        GameStorage.saveGame.lives = lives;
        GameStorage.saveGame.score = (player != null ? player.getScore() : 0);
        GameStorage.saveGame.levelElapsedMs = getLevelElapsedTime();
        GameStorage.saveProgressAsync();

        if (lives == 0) {
            onGameOver();
        }
    }

    private void onLevelComplete() {
        String levelTime = formatMillis(System.currentTimeMillis() - levelStartMillis);

        currentLevel++;
        GameStorage.saveGame.currentLevel = currentLevel;
        GameStorage.saveGame.lives = lives;

        GameStorage.saveGame.levelElapsedMs = 0;
        GameStorage.saveGame.score = (player != null ? player.getScore() : 0);

        GameStorage.saveProgressAsync();
        maybeSaveScore(player.getScore());
    }

    private void onGameOver() {
        String levelTime = formatMillis(System.currentTimeMillis() - levelStartMillis);
        highscoreRepo.saveHighscore(new HighscoreModel(playerName(), score, levelTime, new Date()));

        GameStorage.saveGame.currentLevel = 1;
        GameStorage.saveGame.lives = 3;
        GameStorage.saveGame.score = 0;
        GameStorage.saveGame.levelElapsedMs = 0;

        GameStorage.saveProgressAsync();
        maybeSaveScore(player.getScore());
    }

    private String playerName() { return "PLAYER"; }

    private String formatMillis(long ms) {
        long s = ms / 1000;
        return String.format("%02d:%02d", (s/60), (s%60));
    }

    private static final double SHOOT_LIKELIHOOD = 0.7;
    private Maze maze;
    private Player player;
    private Stage primaryStage;

    private void loadEnemy(String level){
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader("./src/main/resources/de/sanguinik/model/"+level+".json"));
            JSONObject jsonObject = (JSONObject) obj;

            JSONObject enemys = (JSONObject) jsonObject.get("Enemys");

            for(int i = 1; i <= enemys.size(); i++){
                JSONObject enemy = (JSONObject) enemys.get("Enemy"+i);

                long type = (Long) enemy.get("type");
                TypeOfFigure typeOfFigure = TypeOfFigure.BURWOR;
                if(type == 1){
                    typeOfFigure = TypeOfFigure.BURWOR;
                }else if(type == 2){
                    typeOfFigure = TypeOfFigure.GARWOR;
                }else if(type == 3){
                    typeOfFigure = TypeOfFigure.THORWOR;
                }else if(type == 4){
                    typeOfFigure = TypeOfFigure.WIZARD;
                }

                Position positionStart = new Position((double) enemy.get("x"),(double) enemy.get("y"));
                long quantity = (Long) enemy.get("quantity");

                for(int j = 0; j < quantity; j++){
                    createEnemy(new Target(typeOfFigure, positionStart));
                }
            }
        } catch (IOException | ParseException e){
            e.printStackTrace();
        }
    }

    private Enemy createEnemy(final Target target) {
        Enemy enemy;
        if (target.getTypeOfFigure() == TypeOfFigure.WIZARD) {
            enemy = new Enemy(maze, target, root, player);
        } else {
            enemy = new Enemy(maze, target);
        }

        enemy.addTargets(player);
        enemy.setShootCallback(new ShootCallbackImpl());
        root.getChildren().add(enemy.getGroup());
        enemyList.add(enemy);

        player.getTargets().clear();
        for (Enemy e : enemyList) {
            if (e.isAlive()) player.addTargets(e);
        }
        return enemy;
    }

    @Override
    public void start(final Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Knight of Wor");
        primaryStage.setResizable(false);

        URL pathToLevelMusic = getClass().getResource("KoWLong.mp3");
        if (pathToLevelMusic != null) {
            music = new Media(pathToLevelMusic.toString());
            mediaPlayer1 = new MediaPlayer(music);
            mediaPlayer2 = new MediaPlayer(music);
            currentPlayer = mediaPlayer1;

            mediaPlayer1.setVolume(1.0);
            mediaPlayer2.setVolume(0.0);

             try {
                double musicVol = GameStorage.settings.audio.musicVolume;
                boolean musicOn = GameStorage.settings.audio.musicEnabled;

                mediaPlayer1.setVolume(musicVol);
                mediaPlayer2.setVolume(0.0);

                if (musicOn) {
                    mediaPlayer1.play();
                } else {
                    mediaPlayer1.pause();
                    mediaPlayer2.pause();
                }
            } catch (Throwable t) {
                System.err.println("Falha ao aplicar preferências de áudio na fase: " + t.getMessage());
            }

            final double crossfadeDuration = 0.2;

            mediaPlayer1.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                Duration total = mediaPlayer1.getTotalDuration();
                if (total != null && newTime != null && total.toSeconds() - newTime.toSeconds() <= crossfadeDuration && useFirstPlayer) {
                    startCrossfade(mediaPlayer1, mediaPlayer2, crossfadeDuration);
                    useFirstPlayer = false;
                }
            });
            mediaPlayer2.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                Duration total = mediaPlayer2.getTotalDuration();
                if (total != null && newTime != null && total.toSeconds() - newTime.toSeconds() <= crossfadeDuration && !useFirstPlayer) {
                    startCrossfade(mediaPlayer2, mediaPlayer1, crossfadeDuration);
                    useFirstPlayer = true;
                }
            });

            if (GameStorage.settings.audio.musicEnabled) {
                mediaPlayer1.play();
            }
        } else {
            System.err.println("Musikdatei 'KoWLong.mp3' nicht gefunden!");
        }

        String levelKey = "level" + currentLevel;

        maze = new Maze(levelKey);

        player = new Player(maze);
        player.setShootCallback(new ShootCallbackImpl());

        try { player.setLives(lives); } catch (Throwable ignore) {}

        int  savedScore     = Math.max(0,  GameStorage.saveGame.score);
        long savedElapsedMs = Math.max(0L, GameStorage.saveGame.levelElapsedMs);

        try { player.setScore(savedScore); } catch (Throwable ignore) {}

        loadEnemy(levelKey);

        for (Enemy e : enemyList) e.setInimigos(enemyList);

        Label scoreLbl = new Label("Score: " + player.getScore());

        levelStartTime = System.currentTimeMillis() - savedElapsedMs;
        timeLabel = new Label("Tempo: " + formatTime(savedElapsedMs));
        timeLabel.setTextFill(Color.WHITESMOKE);
        timeLabel.setLayoutX(120);
        timeLabel.setLayoutY(0);

        player.setRoot(root);

        Keyboard keyboard = new Keyboard(player, this);

        root.getChildren().add(player.getGroup());
        root.getChildren().addAll(maze.getWalls());
        root.getChildren().add(scoreLbl);
        root.getChildren().add(player.getLivesLabel());
        root.getChildren().add(timeLabel);

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.setAutoReverse(false);

        EventHandler<ActionEvent> actionPerFrame = new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent t) {

                introSequence();

                  for (Enemy enemy : enemyList) {
                    if (enemy.isAlive() && player.isAlive() && !player.isInvincible() &&
                        player.getRectangle().getBoundsInParent().intersects(enemy.getRectangle().getBoundsInParent())) {
                        player.setAlive(false);
                        break;
                    }
                }

                if(checkThatPlayerIsStillAlive()){
                    moveAllEnemies();
                    moveAllBullets();

                    scoreLbl.setText("Score: " + player.getScore());

                    updateLevelTimerAndPersist();
                    maybeSaveScore(player.getScore());

                }else{

                    if(player.getLives() == 0){

                        enterHighscore();

                    }else{
                        timeline.pause();
                        player.loseLife();

    
                        lives = player.getLives();
                        GameStorage.saveGame.lives = lives;
                        GameStorage.saveGame.score = player.getScore();
                        GameStorage.saveGame.levelElapsedMs = getLevelElapsedTime();
                        GameStorage.saveProgressAsync();

                        player.setInvincible(true);
                        player.setAlive(true);
                        timeline.play();
                        Timer timer = new Timer();
                        ColorAdjust sombra = new ColorAdjust();
                        sombra.setBrightness(-0.7);
                        player.getImageView().setEffect(sombra);
                        timer.schedule(new TimerTask(){
                            @Override
                            public void run() {
                                Platform.runLater(() -> {
                                    player.setInvincible(false);
                                    player.getImageView().setEffect(null);
                                });
                            }
                        }, 3000);
                    }
                }
            }
        };

        KeyFrame keyframe = new KeyFrame(Duration.millis(ONE_SECOND / FPS), actionPerFrame);
        timeline.getKeyFrames().add(keyframe);
        timeline.play();

        saverExec = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Save-Heartbeat");
            t.setDaemon(true);
            return t;
        });
        saverTask = saverExec.scheduleAtFixedRate(() -> {
            try {
                long elapsed = getLevelElapsedTime();
                int  s       = (player != null ? player.getScore() : 0);

                GameStorage.saveGame.levelElapsedMs = elapsed;
                GameStorage.saveGame.score          = s;
                GameStorage.saveProgressAsync();

 
            } catch (Throwable ignore) {}
        }, 1, 1, TimeUnit.SECONDS);

        Scene scene = new Scene(root, 1024, 740);
        scene.getStylesheets().add(TitleScreen.class.getResource("controls.css").toExternalForm());
        scene.setOnKeyPressed(keyboard);
        scene.setOnKeyReleased(keyboard);

        scene.setFill(Color.BLACK);
        primaryStage.setScene(scene);
        primaryStage.show();

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(final WindowEvent w) {
                timeline.stop();
                if (currentPlayer != null) currentPlayer.stop();

                 try {
                    if (saverTask != null) saverTask.cancel(false);
                    if (saverExec != null) saverExec.shutdownNow();
                } catch (Throwable ignore) {}

                try {
                    GameStorage.saveGame.currentLevel = currentLevel;
                    GameStorage.saveGame.lives = (player != null ? player.getLives() : lives);
                    GameStorage.saveGame.score = (player != null ? player.getScore() : 0);
                    GameStorage.saveGame.levelElapsedMs = getLevelElapsedTime();
                    GameStorage.saveProgressAsync();
                } catch (Throwable ignore) {}

                System.exit(0);
            }
        });

        currentLevel = Math.max(1, GameStorage.saveGame.currentLevel);
        lives        = GameStorage.saveGame.lives > 0 ? GameStorage.saveGame.lives : 3;

        startLevel();
        lastSavedScore = player.getScore();
        tryPersistScore(lastSavedScore);
    }

    private void maybeSaveScore(int newScore) {
        if (newScore != lastSavedScore) {
            lastSavedScore = newScore;
            tryPersistScore(newScore);              // arquivo auxiliar (como já existia)
            GameStorage.saveGame.score = newScore;  // reflete no save
            GameStorage.saveProgressAsync();
        }
    }

    private void tryPersistScore(int currentScore) {
        try (var w = new java.io.FileWriter("save_score.txt", false)) {
            w.write(Integer.toString(currentScore));
        } catch (IOException e) {
            System.err.println("Falha ao salvar score: " + e.getMessage());
        }
    }

    private boolean checkThatPlayerIsStillAlive() {
        if (!player.isAlive()) {
            gameWasPaused = true;
            player.toggleMoveable();
            return false;
        }
        return true;
    }

    private void enterHighscore(){
        if (currentPlayer != null) currentPlayer.stop();

        timeline.stop();
        for (Enemy e : enemyList) {
            if (e.getType() == TypeOfFigure.WIZARD) e.stopWizardAttack();
        }

        Label playersPoints = new Label("Voce fez " + player.getScore() + " pontos!");
        playersPoints.setTextFill(Color.WHITESMOKE);

        // Tempo da fase
        Label levelTime = new Label("Tempo da fase: " + getLevelElapsedTimeFormatted());
        levelTime.setTextFill(Color.YELLOW);

        // Bônus de tempo
        long timeInSeconds = finalLevelTime / 1000;
        int timeBonus = calculateTimeBonus(timeInSeconds);
        Label bonusInfo = new Label(formatTimeBonus(timeBonus, timeInSeconds));
        bonusInfo.setTextFill(timeBonus > 0 ? Color.GOLD : Color.LIGHTGRAY);
        bonusInfo.setStyle("-fx-font-weight: bold;");

        Label enterHighscore = new Label("Digite seu nome! ");
        enterHighscore.setTextFill(Color.WHITESMOKE);
        TextField name = new TextField("Jogador 1");
        Button ok = new Button("Ok");
        VBox highscorePopup = new VBox();
        highscorePopup.setAlignment(Pos.CENTER);
        highscorePopup.setSpacing(10);
        highscorePopup.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8); -fx-padding: 20; -fx-border-color: white; -fx-border-width: 2;");
        highscorePopup.getChildren().add(playersPoints);
        highscorePopup.getChildren().add(levelTime);
        highscorePopup.getChildren().add(bonusInfo);
        highscorePopup.getChildren().add(enterHighscore);
        HBox highscoreBox = new HBox();
        highscoreBox.setAlignment(Pos.CENTER);
        highscoreBox.setSpacing(10);
        highscoreBox.getChildren().add(name);
        highscoreBox.getChildren().add(ok);
        highscorePopup.getChildren().add(highscoreBox);
        highscorePopup.setLayoutX(root.getScene().getWidth()/2 - 150);
        highscorePopup.setLayoutY(root.getScene().getHeight()/2 - 100);
        root.getChildren().add(highscorePopup);
        ok.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
                HighscoreImpl highscore = new HighscoreImpl();
                entry = new HighscoreModel(name.getText(), player.getScore(), getLevelElapsedTimeFormatted(), new Date());
                highscore.saveHighscore(entry);
                gameOver();
            }
        });
    }

    private void introSequence() {
        Label ready = new Label("READY?");
        ready.setLayoutX(root.getScene().getWidth()/2);
        ready.setLayoutY(root.getScene().getHeight()/2);

        if(gameWasPaused){
            timeline.pause();
            
            for (Enemy e : enemyList) {
                if (e.getType() == TypeOfFigure.WIZARD) e.stopWizardAttack();
            }
            root.getChildren().add(ready);
            Timer timer = new Timer();

            timer.schedule(new TimerTask(){
                @Override
                public void run() { Platform.runLater(() -> ready.setText("START!")); }
            }, 1000);

            timer.schedule(new TimerTask(){
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        timeline.play();
                        player.toggleMoveable();
                        root.getChildren().remove(ready);
                        // retoma ataques de magos
                        for (Enemy e : enemyList) {
                            if (e.getType() == TypeOfFigure.WIZARD) e.startWizardAttack();
                        }
                    });
                }
            }, 2000);

            gameWasPaused = false;
        }
    }

    private void moveAllBullets() {
        List<Bullet> bulletsToDelete = new ArrayList<>();
        for (Bullet b : bulletList) {
            b.move();
            if (!b.isActive()) bulletsToDelete.add(b);
        }
        for (Bullet b : bulletsToDelete) {
            bulletList.remove(b);
            root.getChildren().remove(b.getGroup());
        }
    }

    private void moveAllEnemies() {
        List<Enemy> enemiesToDelete = new ArrayList<>();

        if (enemyList.isEmpty()) {
            // marca fase como completa
            completeLevelWithTime();
            enterHighscore();
        }

        for (Enemy e : enemyList) {
            if (e.isAlive()) {
                e.move();
                int d = (int) (FPS * (1 / SHOOT_LIKELIHOOD));
                int random = new Random().nextInt(d);
                if (random == 0) e.shoot();
            } else {
                enemiesToDelete.add(e);
            }
        }

        for (Enemy e : enemiesToDelete) {
            enemyList.remove(e);
            root.getChildren().remove(e.getGroup());
            player.getTargets().remove(e);
        }
    }

    private void gameOver() {
        final GameOver gameOver = new GameOver();
        gameOver.start(primaryStage);
        if (currentPlayer != null) currentPlayer.stop();
        onGameOver();
    }

    // Popup de pausa
    private VBox pausePopup = null;

    public void pauseGame(){
        if(gameWasPaused){
            // despausar
            if (pausePopup != null) {
                root.getChildren().remove(pausePopup);
                pausePopup = null;
            }
            timeline.play();
            for (Enemy e : enemyList) e.startWizardAttack();
            resumeLevelTimer();
            gameWasPaused = false;
        }else{
            // pausar
            createPausePopup();
            for (Enemy e : enemyList) e.stopWizardAttack();
            timeline.pause();
            pauseLevelTimer();
            gameWasPaused = true;
        }
    }

    private void createPausePopup() {
        Label pauseTitle = new Label("JOGO PAUSADO");
        pauseTitle.setTextFill(Color.WHITESMOKE);
        pauseTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label currentScore = new Label("Pontuacao Atual: " + player.getScore());
        currentScore.setTextFill(Color.LIGHTBLUE);
        currentScore.setStyle("-fx-font-size: 16px;");

        Label currentTime = new Label("Tempo Atual: " + getLevelElapsedTimeFormatted());
        currentTime.setTextFill(Color.YELLOW);
        currentTime.setStyle("-fx-font-size: 16px;");

        Label livesRemaining = new Label("Vidas Restantes: " + player.getLives());
        livesRemaining.setTextFill(Color.LIGHTGREEN);
        livesRemaining.setStyle("-fx-font-size: 16px;");

        Button continueBtn = new Button("Continuar");
        continueBtn.setStyle("-fx-font-size: 14px; -fx-pref-width: 120px;");
        continueBtn.setOnAction(e -> pauseGame());

        Button mainMenuBtn = new Button("Menu");
        mainMenuBtn.setStyle("-fx-font-size: 14px; -fx-pref-width: 120px;");
        mainMenuBtn.setOnAction(e -> {
            timeline.stop();
            if (currentPlayer != null) currentPlayer.stop();
            for (Enemy enemy : enemyList) {
                if (enemy.getType() == TypeOfFigure.WIZARD) enemy.stopWizardAttack();
            }
            TitleScreen titleScreen = new TitleScreen();
            titleScreen.start(primaryStage);
        });

        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setSpacing(15);
        buttonBox.getChildren().addAll(continueBtn, mainMenuBtn);

        pausePopup = new VBox();
        pausePopup.setAlignment(Pos.CENTER);
        pausePopup.setSpacing(15);
        pausePopup.setStyle("-fx-background-color: rgba(0, 0, 0, 0.9); -fx-padding: 30; -fx-border-color: white; -fx-border-width: 2; -fx-border-radius: 10; -fx-background-radius: 10;");

        pausePopup.getChildren().addAll(
            pauseTitle,
            currentScore,
            currentTime,
            livesRemaining,
            buttonBox
        );

        pausePopup.setLayoutX(root.getScene().getWidth()/2 - 150);
        pausePopup.setLayoutY(root.getScene().getHeight()/2 - 120);

        root.getChildren().add(pausePopup);
    }

    public void muteMusic() {
        if(currentPlayer != null) currentPlayer.setMute(!currentPlayer.isMute());
    }
    
    private void startCrossfade(MediaPlayer fadingOut, MediaPlayer fadingIn, double durationSeconds) {
        fadingIn.seek(Duration.ZERO);
        fadingIn.play();
        Timeline fade = new Timeline(
            new KeyFrame(Duration.ZERO,
                e -> {},
                new KeyValue(fadingOut.volumeProperty(), fadingOut.getVolume()),
                new KeyValue(fadingIn.volumeProperty(), 0.0)
            ),
            new KeyFrame(Duration.seconds(durationSeconds),
                e -> {
                    fadingOut.pause();
                    currentPlayer = fadingIn;
                    // garante volume final conforme preferência
                    try {
                        double musicVol = GameStorage.settings.audio.musicVolume;
                        fadingIn.setVolume(musicVol);
                    } catch (Throwable ignore) {}
                },
                new KeyValue(fadingOut.volumeProperty(), 0.0),
                new KeyValue(fadingIn.volumeProperty(), 1.0)
            )
        );
        fade.play();
    }

    public Group getRoot() { return root; }

    private void updateLevelTimerAndPersist() {
        if (!levelCompleted && !gamePaused) {
            long elapsed = getLevelElapsedTime();
            timeLabel.setText("Tempo: " + formatTime(elapsed));

            long sec = elapsed / 1000;
            if (sec != lastPersistedSecond) {
                lastPersistedSecond = sec;
                GameStorage.saveGame.levelElapsedMs = elapsed;
                GameStorage.saveProgressAsync();
            }
        }
    }

    private void pauseLevelTimer() {
        if (!gamePaused && !levelCompleted) {
            pauseStartTime = System.currentTimeMillis();
            gamePaused = true;
        }
    }

    private void resumeLevelTimer() {
        if (gamePaused && !levelCompleted) {
            pausedTime += System.currentTimeMillis() - pauseStartTime;
            gamePaused = false;
        }
    }

    private String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public long getLevelElapsedTime() {
        if (levelCompleted) return finalLevelTime;

        long currentTime = System.currentTimeMillis();
        long totalPausedTime = pausedTime;

        if (gamePaused) totalPausedTime += currentTime - pauseStartTime;

        return (currentTime - levelStartTime) - totalPausedTime;
    }

    public String getLevelElapsedTimeFormatted() { return formatTime(getLevelElapsedTime()); }

    public void completeLevelWithTime() {
        if (!levelCompleted) {
            finalLevelTime = getLevelElapsedTime();
            levelCompleted = true;

            long timeInSeconds = finalLevelTime / 1000;
            lastTimeBonus = calculateTimeBonus(timeInSeconds);

            if (lastTimeBonus > 0) {
                player.setScore(player.getScore() + lastTimeBonus);
                GameStorage.saveGame.score = player.getScore();
            }

            System.out.println("Fase completada em: " + formatTime(finalLevelTime));
            timeLabel.setText("Tempo Final: " + formatTime(finalLevelTime));

            Label bonusLabel = new Label(formatTimeBonus(lastTimeBonus, timeInSeconds));
            bonusLabel.setTextFill(lastTimeBonus > 0 ? Color.GOLD : Color.LIGHTGRAY);
            bonusLabel.setLayoutX(10);
            bonusLabel.setLayoutY(80);
            bonusLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
            root.getChildren().add(bonusLabel);

            // zera tempo salvo para próxima fase
            GameStorage.saveGame.levelElapsedMs = 0;
            GameStorage.saveProgressAsync();

            onLevelComplete();
        }
    }

    public void resetLevelTimer() {
        levelStartTime = System.currentTimeMillis();
        pausedTime = 0;
        pauseStartTime = 0;
        finalLevelTime = 0;
        lastTimeBonus = 0;
        levelCompleted = false;
        gamePaused = false;
        timeLabel.setText("Tempo: 00:00");
        lastPersistedSecond = -1;
    }

    private int calculateTimeBonus(long timeInSeconds) {
        final int MAX_BONUS = 2000;
        final int PERFECT_TIME = 10;
        final int TIME_INTERVAL = 2;
        final int NO_BONUS_TIME = 60;

        if (timeInSeconds <= PERFECT_TIME) return MAX_BONUS;
        if (timeInSeconds >= NO_BONUS_TIME) return 0;

        long extraTime = timeInSeconds - PERFECT_TIME;
        int intervals = (int) (extraTime / TIME_INTERVAL);

        int totalLoss = 0;
        for (int i = 1; i <= intervals; i++) totalLoss += i * 5;

        int bonus = MAX_BONUS - totalLoss;
        return Math.max(0, bonus);
    }

    private String getTimeCategoryDescription(long timeInSeconds) {
        if (timeInSeconds <= 10) return "(LEGENDARIO!)";
        else if (timeInSeconds <= 15) return "(PERFEITO!)";
        else if (timeInSeconds <= 25) return "(EXCELENTE!)";
        else if (timeInSeconds <= 35) return "(MUITO BOM!)";
        else if (timeInSeconds <= 45) return "(BOM!)";
        else if (timeInSeconds <= 60) return "(RAZOAVEL)";
        else return "(SEM BONUS)";
    }

    private String formatTimeBonus(int bonus, long timeInSeconds) {
        if (bonus > 0) {
            String timeCategory = getTimeCategoryDescription(timeInSeconds);
            return "Bonus de Tempo: +" + bonus + " pontos! " + timeCategory;
        }
        return "Sem bonus de tempo (mais de 1 minuto)";
    }
}
