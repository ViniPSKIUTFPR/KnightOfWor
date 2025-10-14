package de.sanguinik.view;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import javafx.util.Duration;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import de.sanguinik.model.Bullet;
import de.sanguinik.model.Enemy;
import de.sanguinik.model.HighscoreEntry;
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

	private final Timeline timeline = new Timeline();
	private final List<Enemy> enemyList = new ArrayList<Enemy>();
	private final List<Bullet> bulletList = new ArrayList<Bullet>();
	private static final int ONE_SECOND = 1000;
	private static final int FPS = 30;
	private final Group root = new Group();
	private boolean gameWasPaused = true;
	private final Label pause = new Label("PAUSE");
	
	private HighscoreEntry entry;

	private Media music;
	private MediaPlayer mediaPlayer1;
	private MediaPlayer mediaPlayer2;
	private MediaPlayer currentPlayer;
	private boolean useFirstPlayer = true;

	/**
	 * Mit dieser Wahrscheinlichkeit wird ein mal pro Sekunde geschossen.
	 */
	private static final double SHOOT_LIKELIHOOD = 0.7;
	private Maze maze;

	private Player player;

	private Stage primaryStage;


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

			// Duração do crossfade em segundos 
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

			mediaPlayer1.play();
		} else {
			System.err.println("Musikdatei 'KoWLong.mp3' nicht gefunden!");
		}
		maze = new Maze("level1");

		player = new Player(maze);
		player.setShootCallback(new ShootCallbackImpl());

		createEnemy(new Target(TypeOfFigure.BURWOR, new Position(130, 130)));
		createEnemy(new Target(TypeOfFigure.GARWOR, new Position(855, 510)));
		createEnemy(new Target(TypeOfFigure.THORWOR, new Position(855, 130)));
		createEnemy(new Target(TypeOfFigure.WIZARD, new Position(500, 300)));

		// Seta a lista de inimigos no objeto de cada inimigo. Caso um projetil do inimigo seja rebatido pelo jogador, os inimigos se tornarão o target daquele projetil
		for (Enemy e : enemyList) {
			e.setInimigos(enemyList);
		}
		
		Label score = new Label("Score: " + player.getScore());

		player.setRoot(root);
		
		Keyboard keyboard = new Keyboard(player, this);

		root.getChildren().add(player.getGroup());
		root.getChildren().addAll(maze.getWalls());
		root.getChildren().add(score);
		root.getChildren().add(player.getLivesLabel());

		timeline.setCycleCount(Timeline.INDEFINITE);
		timeline.setAutoReverse(false);

		
		
		EventHandler<ActionEvent> actionPerFrame = new EventHandler<ActionEvent>() {

			@Override
			public void handle(final ActionEvent t) {
				
				introSequence();

				// Checa colisão player-inimigo
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
					score.setText("Score: " + player.getScore());
				}else{
					
					
					if(player.getLives() == 0){

						enterHighscore();
						
					}else{
						timeline.pause();
						player.loseLife();
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

		KeyFrame keyframe = new KeyFrame(Duration.millis(ONE_SECOND / FPS),
				actionPerFrame);
		timeline.getKeyFrames().add(keyframe);
		timeline.play();

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
				if (currentPlayer != null) {
					currentPlayer.stop();
				}
				System.exit(0);
			}
		});

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
		if (currentPlayer != null) {
			currentPlayer.stop();
		}
		timeline.stop();
		for (Enemy e : enemyList) {
			if (e.getType() == TypeOfFigure.WIZARD) e.stopWizardAttack();
		}
		int finalScore = player.getScore();
		String playerName = "Spieler 1";
		Label playersPoints = new Label("Du hast "+finalScore+ " Punkte!");
		playersPoints.setTextFill(Color.WHITESMOKE);
		Label enterHighscore = new Label("Trage deinen Namen ein! ");
		enterHighscore.setTextFill(Color.WHITESMOKE);
		TextField name = new TextField(playerName);
		Button ok = new Button("Ok");
		VBox highscorePopup = new VBox();
		highscorePopup.setAlignment(Pos.CENTER);
		highscorePopup.getChildren().add(playersPoints);
		highscorePopup.getChildren().add(enterHighscore);
		HBox highscoreBox = new HBox();
		highscoreBox.getChildren().add(name);
		highscoreBox.getChildren().add(ok);
		highscorePopup.getChildren().add(highscoreBox);
		highscorePopup.setLayoutX(root.getScene().getWidth()/2 - 120);
		highscorePopup.setLayoutY(root.getScene().getHeight() - 100);
		root.getChildren().add(highscorePopup);
		ok.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				//highscore eintragen
				entry = new HighscoreEntry(name.getText(), finalScore);
            
				//gameover
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
			// Pausa ataques de todos os magos
			for (Enemy e : enemyList) {
				if (e.getType() == TypeOfFigure.WIZARD) e.stopWizardAttack();
			}
			root.getChildren().add(ready);
			Timer timer = new Timer();

			timer.schedule(new TimerTask(){
				@Override
				public void run() {
					Platform.runLater(() -> {
						ready.setText("START!");
					});
				}
			}, 1000);

			timer.schedule(new TimerTask(){
				@Override
				public void run() {
					Platform.runLater(() -> {
						timeline.play();
						player.toggleMoveable();
						root.getChildren().remove(ready);
						// Retoma ataques de todos os magos
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
		List<Bullet> bulletsToDelete = new ArrayList<Bullet>();
		for (Bullet b : bulletList) {
			b.move();
			if (!b.isActive()) {
				bulletsToDelete.add(b);
			}
		}
		for (Bullet b : bulletsToDelete) {
			bulletList.remove(b);
			root.getChildren().remove(b.getGroup());
		}
	}

	private void moveAllEnemies() {
		List<Enemy> enemiesToDelete = new ArrayList<Enemy>();

		if (enemyList.isEmpty()) {
			enterHighscore();
		}

		for (Enemy e : enemyList) {
			if (e.isAlive()) {
				e.move();
				int d = (int) (FPS * (1 / SHOOT_LIKELIHOOD));
				int random = new Random().nextInt(d);
				if (random == 0) {
					e.shoot();
				}
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
		if (currentPlayer != null) {
			currentPlayer.stop();
		}
	}
	
	public void pauseGame(){
		if(gameWasPaused){
			root.getChildren().remove(pause);
			timeline.play();
			for (Enemy e : enemyList) {
    			e.startWizardAttack(); 
			}
			gameWasPaused = false;
		}else{
			pause.setLayoutX(root.getScene().getWidth()/2);
			pause.setLayoutY(root.getScene().getHeight()/2);
			root.getChildren().add(pause);
			for (Enemy e : enemyList) {
    			e.stopWizardAttack(); 
			}
			timeline.pause();

			gameWasPaused = true;
		}
	}

	public void muteMusic() {
		if(currentPlayer != null) {
			if(currentPlayer.isMute()){
				currentPlayer.setMute(false);
			}else{
				currentPlayer.setMute(true);
			}
		}
	}

	// Crossfade da música (impede que haja um corte seco entre cada loop da trilha sonora)
	private void startCrossfade(MediaPlayer fadingOut, MediaPlayer fadingIn, double durationSeconds) {
		fadingIn.seek(Duration.ZERO);
		fadingIn.play();
		Timeline fade = new Timeline(
			new KeyFrame(Duration.ZERO,
				e -> {
				},
				new javafx.animation.KeyValue(fadingOut.volumeProperty(), fadingOut.getVolume()),
				new javafx.animation.KeyValue(fadingIn.volumeProperty(), 0.0)
			),
			new KeyFrame(Duration.seconds(durationSeconds),
				e -> {
					fadingOut.pause();
					currentPlayer = fadingIn;
				},
				new javafx.animation.KeyValue(fadingOut.volumeProperty(), 0.0),
				new javafx.animation.KeyValue(fadingIn.volumeProperty(), 1.0)
			)
		);
		fade.play();
	}

	public Group getRoot() {
		return root;
	}
}
