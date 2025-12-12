package de.sanguinik.view;

import de.sanguinik.model.Enemy;
import de.sanguinik.model.Player;
import de.sanguinik.model.TypeOfFigure;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.control.Slider;
import javafx.stage.Stage;
import de.sanguinik.util.VolumeManager;

public class PausedGame extends Application{

    private Group root;
    private VBox pausePopup;
    private Player player;
    private Timeline timeline;
    private MediaPlayer currentPlayer;
	private java.util.List<Enemy> enemyList;
	private String levelElapsedTime;
	private javafx.event.EventHandler<ActionEvent> pauseMethod;

    public PausedGame() {
    }

    public PausedGame(Group root, Player player, Timeline timeline, MediaPlayer currentPlayer, java.util.List<Enemy> enemyList, String levelElapsedTime, javafx.event.EventHandler<ActionEvent> pauseMethod) {
        this.root = root;
        this.player = player;
        this.timeline = timeline;
        this.currentPlayer = currentPlayer;
        this.enemyList = enemyList;
        this.levelElapsedTime = levelElapsedTime;
        this.pauseMethod = pauseMethod;
    }

    public void stop() {
        if (pausePopup != null) {
            root.getChildren().remove(pausePopup);
            pausePopup = null;
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Label pauseTitle = new Label("JOGO PAUSADO");
		pauseTitle.setTextFill(Color.WHITESMOKE);
		pauseTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

		Label currentScore = new Label("Pontuacao Atual: " + player.getScore());
		currentScore.setTextFill(Color.LIGHTBLUE);
		currentScore.setStyle("-fx-font-size: 16px;");

		Label currentTime = new Label("Tempo Atual: " + levelElapsedTime);
		currentTime.setTextFill(Color.YELLOW);
		currentTime.setStyle("-fx-font-size: 16px;");

		Label livesRemaining = new Label("Vidas Restantes: " + player.getLives());
        livesRemaining.setTextFill(Color.LIGHTGREEN);
        livesRemaining.setStyle("-fx-font-size: 16px;");

        // NOVO: Adiciona slider de volume
        Label volumeLabel = new Label("Volume:");
        volumeLabel.setTextFill(Color.WHITESMOKE);
        volumeLabel.setStyle("-fx-font-size: 14px;");
        
        Slider volumeSlider = new Slider(0.0, 1.0, VolumeManager.getVolume());
        volumeSlider.setStyle("-fx-font-size: 12px;");
        volumeSlider.setPrefWidth(200);
        
        // Quando o slider é movido, atualiza o volume
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double newVolume = newVal.doubleValue();
            VolumeManager.setVolume(newVolume);
            
            // Aplica o volume ao MediaPlayer atual
            if (currentPlayer != null) {
                currentPlayer.setVolume(newVolume);
            }
        });
        
        // Container para o volume (label + slider)
        HBox volumeBox = new HBox();
        volumeBox.setAlignment(Pos.CENTER);
        volumeBox.setSpacing(10);
        volumeBox.getChildren().addAll(volumeLabel, volumeSlider);

        Button continueBtn = new Button("Continuar");
		continueBtn.setStyle("-fx-font-size: 14px; -fx-pref-width: 120px;");
		continueBtn.setOnAction(pauseMethod); // Chama novamente para despausar

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
            volumeBox,
            buttonBox
        );

		// Centraliza o popup na tela
		pausePopup.setLayoutX(root.getScene().getWidth()/2 - 150);
		pausePopup.setLayoutY(root.getScene().getHeight()/2 - 120);
		
		root.getChildren().add(pausePopup);
    }
    
}
