package de.sanguinik.view;

import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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
import de.sanguinik.model.HighscoreModel;
import de.sanguinik.model.Keyboard;
import de.sanguinik.model.Maze;
import de.sanguinik.model.Player;
import de.sanguinik.model.Position;
import de.sanguinik.model.ShootCallback;
import de.sanguinik.model.Target;
import de.sanguinik.model.TypeOfFigure;
import de.sanguinik.persistence.HighscoreImpl;
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
	
	// MELHORIA: Contador de tempo para cada fase
	private long levelStartTime;
	private long pausedTime = 0; // Tempo total pausado
	private long pauseStartTime = 0; // Quando a pausa começou
	private long finalLevelTime = 0; // Tempo final quando a fase foi completada
	private Label timeLabel;
	private boolean levelCompleted = false;
	private boolean gamePaused = false;
	private int lastTimeBonus = 0; // MELHORIA: Último bônus de tempo calculado
	
	private HighscoreModel entry;

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

			// DuraÃ§Ã£o do crossfade em segundos 
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

		loadEnemy("level1");

		// Seta a lista de inimigos no objeto de cada inimigo. Caso um projetil do inimigo seja rebatido pelo jogador, os inimigos se tornarÃ£o o target daquele projetil
		for (Enemy e : enemyList) {
			e.setInimigos(enemyList);
		}
		
		Label score = new Label("Score: " + player.getScore());
		
		// MELHORIA: Inicialização do contador de tempo da fase
		levelStartTime = System.currentTimeMillis();
		timeLabel = new Label("Tempo: 00:00");
		timeLabel.setTextFill(Color.WHITESMOKE);
		timeLabel.setLayoutX(120);
		timeLabel.setLayoutY(0);

		player.setRoot(root);
		
		Keyboard keyboard = new Keyboard(player, this);

		root.getChildren().add(player.getGroup());
		root.getChildren().addAll(maze.getWalls());
		root.getChildren().add(score);
		root.getChildren().add(player.getLivesLabel());
		root.getChildren().add(timeLabel); // MELHORIA: Adiciona o contador de tempo à interface

		timeline.setCycleCount(Timeline.INDEFINITE);
		timeline.setAutoReverse(false);
		
		EventHandler<ActionEvent> actionPerFrame = new EventHandler<ActionEvent>() {

			@Override
			public void handle(final ActionEvent t) {
				
				introSequence();

				// Checa colisÃ£o player-inimigo
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
					
					// MELHORIA: Atualiza o contador de tempo da fase
					updateLevelTimer();
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

		Label playersPoints = new Label("Voce fez " + player.getScore() + " pontos!");
		playersPoints.setTextFill(Color.WHITESMOKE);
		
		// MELHORIA: Mostra o tempo da fase completada
		Label levelTime = new Label("Tempo da fase: " + getLevelElapsedTimeFormatted());
		levelTime.setTextFill(Color.YELLOW);
		
		// NOVO: Mostra o bônus de tempo detalhado no popup
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
		highscorePopup.setSpacing(10); // MELHORIA: Adiciona espaçamento entre elementos
		highscorePopup.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8); -fx-padding: 20; -fx-border-color: white; -fx-border-width: 2;"); // MELHORIA: Fundo escuro com borda
		highscorePopup.getChildren().add(playersPoints);
		highscorePopup.getChildren().add(levelTime); // MELHORIA: Adiciona o tempo da fase
		highscorePopup.getChildren().add(bonusInfo); // NOVO: Adiciona info do bônus
		highscorePopup.getChildren().add(enterHighscore);
		HBox highscoreBox = new HBox();
		highscoreBox.setAlignment(Pos.CENTER); // MELHORIA: Centraliza o input e botão
		highscoreBox.setSpacing(10); // MELHORIA: Espaçamento entre campo e botão
		highscoreBox.getChildren().add(name);
		highscoreBox.getChildren().add(ok);
		highscorePopup.getChildren().add(highscoreBox);
		highscorePopup.setLayoutX(root.getScene().getWidth()/2 - 150); // Mais centralizado horizontalmente
		highscorePopup.setLayoutY(root.getScene().getHeight()/2 - 100); // Centralizado verticalmente
		root.getChildren().add(highscorePopup);
		ok.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				HighscoreImpl highscore = new HighscoreImpl();
				// MELHORIA: Salva o highscore com o tempo da fase
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
			// MELHORIA: Marca a fase como completada antes de entrar no highscore
			completeLevelWithTime();
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
	
	// MELHORIA: Variável para controlar o popup de pausa
	private VBox pausePopup = null;

	public void pauseGame(){
		if(gameWasPaused){
			// Despausar o jogo
			if (pausePopup != null) {
				root.getChildren().remove(pausePopup);
				pausePopup = null;
			}
			timeline.play();
			for (Enemy e : enemyList) {
    			e.startWizardAttack(); 
			}
			// MELHORIA: Retoma o timer quando despausar
			resumeLevelTimer();
			gameWasPaused = false;
		}else{
			// Pausar o jogo com popup moderno
			createPausePopup();
			for (Enemy e : enemyList) {
    			e.stopWizardAttack(); 
			}
			timeline.pause();
			// MELHORIA: Pausa o timer
			pauseLevelTimer();
			gameWasPaused = true;
		}
	}

	// MELHORIA: Cria popup de pausa moderno similar ao de pontuação
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
		continueBtn.setOnAction(e -> pauseGame()); // Chama novamente para despausar

		Button mainMenuBtn = new Button("Menu");
		mainMenuBtn.setStyle("-fx-font-size: 14px; -fx-pref-width: 120px;");
		mainMenuBtn.setOnAction(e -> {
			// Para tudo e volta ao menu
			timeline.stop();
			if (currentPlayer != null) {
				currentPlayer.stop();
			}
			for (Enemy enemy : enemyList) {
				if (enemy.getType() == TypeOfFigure.WIZARD) enemy.stopWizardAttack();
			}
			TitleScreen titleScreen = new TitleScreen();
			titleScreen.start(primaryStage);
		});

		// Container para os botões
		HBox buttonBox = new HBox();
		buttonBox.setAlignment(Pos.CENTER);
		buttonBox.setSpacing(15);
		buttonBox.getChildren().addAll(continueBtn, mainMenuBtn);

		// Popup principal
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

		// Centraliza o popup na tela
		pausePopup.setLayoutX(root.getScene().getWidth()/2 - 150);
		pausePopup.setLayoutY(root.getScene().getHeight()/2 - 120);
		
		root.getChildren().add(pausePopup);
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
				new javafx.animation.KeyValue(fadingIn.volumeProperty(), 0.7)
			)
		);
		fade.play();
	}

	public Group getRoot() {
		return root;
	}
	
	// MELHORIA: Métodos para gerenciamento do contador de tempo da fase
	
	/**
	 * Atualiza o display do timer da fase atual
	 */
	private void updateLevelTimer() {
		if (!levelCompleted && !gamePaused) {
			long currentTime = System.currentTimeMillis();
			long elapsedTime = (currentTime - levelStartTime) - pausedTime;
			String formattedTime = formatTime(elapsedTime);
			timeLabel.setText("Tempo: " + formattedTime);
		}
	}
	
	/**
	 * Pausa o timer da fase
	 */
	private void pauseLevelTimer() {
		if (!gamePaused && !levelCompleted) {
			pauseStartTime = System.currentTimeMillis();
			gamePaused = true;
		}
	}
	
	/**
	 * Retoma o timer da fase
	 */
	private void resumeLevelTimer() {
		if (gamePaused && !levelCompleted) {
			pausedTime += System.currentTimeMillis() - pauseStartTime;
			gamePaused = false;
		}
	}
	
	/**
	 * Formata o tempo em milissegundos para formato MM:SS
	 */
	private String formatTime(long milliseconds) {
		long seconds = milliseconds / 1000;
		long minutes = seconds / 60;
		seconds = seconds % 60;
		return String.format("%02d:%02d", minutes, seconds);
	}
	
	/**
	 * Obtém o tempo decorrido da fase atual em milissegundos
	 */
	public long getLevelElapsedTime() {
		if (levelCompleted) {
			return finalLevelTime; // Retorna o tempo final preservado
		}
		long currentTime = System.currentTimeMillis();
		long totalPausedTime = pausedTime;
		
		// Se estiver pausado no momento, adiciona o tempo de pausa atual
		if (gamePaused) {
			totalPausedTime += currentTime - pauseStartTime;
		}
		
		return (currentTime - levelStartTime) - totalPausedTime;
	}
	
	/**
	 * Obtém o tempo decorrido da fase atual formatado como string
	 */
	public String getLevelElapsedTimeFormatted() {
		return formatTime(getLevelElapsedTime());
	}
	
	/**
	 * Marca a fase como completada e para o timer
	 */
	public void completeLevelWithTime() {
		if (!levelCompleted) {
			// Preserva o tempo final
			finalLevelTime = getLevelElapsedTime();
			levelCompleted = true;
			
			// NOVO: Calcula e aplica bônus de tempo
			long timeInSeconds = finalLevelTime / 1000;
			lastTimeBonus = calculateTimeBonus(timeInSeconds);
			
			if (lastTimeBonus > 0) {
				// Adiciona o bônus ao score do player
				player.setScore(player.getScore() + lastTimeBonus);
			}
			
			System.out.println("Fase completada em: " + formatTime(finalLevelTime));
			
			// Atualiza o display uma última vez com o tempo final
			timeLabel.setText("Tempo Final: " + formatTime(finalLevelTime));
			
			// NOVO: Mostra o bônus na tela (com categoria)
			Label bonusLabel = new Label(formatTimeBonus(lastTimeBonus, timeInSeconds));
			bonusLabel.setTextFill(lastTimeBonus > 0 ? Color.GOLD : Color.LIGHTGRAY);
			bonusLabel.setLayoutX(10);
			bonusLabel.setLayoutY(80);
			bonusLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
			root.getChildren().add(bonusLabel);
		}
	}
	
	/**
	 * Reinicia o timer para uma nova fase
	 */
	public void resetLevelTimer() {
		levelStartTime = System.currentTimeMillis();
		pausedTime = 0;
		pauseStartTime = 0;
		finalLevelTime = 0;
		lastTimeBonus = 0; // MELHORIA: Reseta o bônus de tempo
		levelCompleted = false;
		gamePaused = false;
		timeLabel.setText("Tempo: 00:00");
	}
	
	/**
	 * Calcula pontuação bônus com perda gradual acelerada (sistema ultra desafiador)
	 */
	private int calculateTimeBonus(long timeInSeconds) {
		final int MAX_BONUS = 2000;        // Bônus máximo: 2000 pontos
		final int PERFECT_TIME = 10;       // 10 segundos = bônus máximo
		final int TIME_INTERVAL = 2;       // Intervalos de 2 segundos
		final int NO_BONUS_TIME = 60;      // Sem bônus após 1 minuto
		
		if (timeInSeconds <= PERFECT_TIME) {
			return MAX_BONUS; // 2000 pontos para tempos ≤ 10 segundos
		}
		
		if (timeInSeconds >= NO_BONUS_TIME) {
			return 0; // Sem bônus após 1 minuto
		}
		
		// Calcula quantos intervalos de 2 segundos passou dos 10 segundos iniciais
		long extraTime = timeInSeconds - PERFECT_TIME;
		int intervals = (int) (extraTime / TIME_INTERVAL);
		
		int totalLoss = 0;
		
		// Perda gradual acelerada: 5, 10, 15, 20, 25, 30, 35, 40, 45, 50...
		for (int i = 1; i <= intervals; i++) {
			totalLoss += i * 5; // 1*5=5, 2*5=10, 3*5=15, etc.
		}
		
		// Calcula o bônus final
		int bonus = MAX_BONUS - totalLoss;
		
		// Garante que o bônus não seja negativo
		return Math.max(0, bonus);
	}
	
	/**
	 * Descrição da categoria de tempo baseada na performance (ajustada para sistema ultra desafiador)
	 */
	private String getTimeCategoryDescription(long timeInSeconds) {
		if (timeInSeconds <= 10) {
			return "(LEGENDARIO!)";
		} else if (timeInSeconds <= 15) {
			return "(PERFEITO!)";
		} else if (timeInSeconds <= 25) {
			return "(EXCELENTE!)";
		} else if (timeInSeconds <= 35) {
			return "(MUITO BOM!)";
		} else if (timeInSeconds <= 45) {
			return "(BOM!)";
		} else if (timeInSeconds <= 60) {
			return "(RAZOAVEL)";
		} else {
			return "(SEM BONUS)";
		}
	}
	
	/**
	 * Formata o bônus de tempo para exibição
	 */
	private String formatTimeBonus(int bonus, long timeInSeconds) {
		if (bonus > 0) {
			String timeCategory = getTimeCategoryDescription(timeInSeconds);
			return "Bonus de Tempo: +" + bonus + " pontos! " + timeCategory;
		}
		return "Sem bonus de tempo (mais de 1 minuto)";
	}
}
