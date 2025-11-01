package de.sanguinik.view;

import java.net.URL;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

public class TitleScreen extends Application {

	private static final double BUTTON_SIZE = 120.0;
	private static final int GRID_GAP = 30;
	private static final int SCENE_WIDTH = 1024;
	private static final int SCENE_HEIGHT = 740;
	private static MediaPlayer player;

	public static void main(final String[] args) {
		launch(args);
	}

	@Override
	public void start(final Stage primaryStage) {
		primaryStage.setTitle("Knight of Wor");
		primaryStage.setResizable(false);

		 // Inicia música apenas se ainda não estiver tocando
        if (player == null) {
            URL pathToTitleMusic = getClass().getResource("menu.mp3");
            if (pathToTitleMusic != null) {
                Media sound = new Media(pathToTitleMusic.toString());
                player = new MediaPlayer(sound);
                player.setVolume(1.0);
                player.setCycleCount(MediaPlayer.INDEFINITE); // loop infinito
                player.play();
            } else {
                System.err.println("Musikdatei 'menu.mp3' wurde nicht gefunden!");
            }
        } else if (player.getStatus() != MediaPlayer.Status.PLAYING) {
            player.play(); // Retoma a música se estiver pausada
        }

		GridPane grid = new GridPane();
		grid.setId("titleGrid");
		grid.setAlignment(Pos.CENTER);
		grid.setHgap(GRID_GAP);
		grid.setVgap(GRID_GAP);

		//#region Create Buttons
		Button newGame = createButton("Novo Jogo", (final ActionEvent arg) -> {
			if (player != null) {
				player.stop();
			}
			PlayFieldScreen psc = new PlayFieldScreen();
			psc.start(primaryStage);
		});

		Button options = createButton("Opcoes", (final ActionEvent arg) -> {
			Options optionsGUI = new Options();
			optionsGUI.start(primaryStage);
		});

		Button highscore = createButton("Pontuacao", (final ActionEvent arg) -> {
			HighscoreScreen highscoreScreen = new HighscoreScreen();
			highscoreScreen.start(primaryStage);
		});

		Button about = createButton("Creditos", (final ActionEvent arg) -> {
			if (player != null) {
				player.pause();
			}
			Credits credits = new Credits();
			credits.start(primaryStage);
		});

		Button close = createButton("Sair", (final ActionEvent arg) -> {
			if (player != null) {
				player.stop();
			}
			System.exit(0);
		});
		//#endregion

		grid.add(newGame, 0, 0);
		grid.add(options, 0, 1);
		grid.add(highscore, 0, 2);
		grid.add(about, 0, 3);
		grid.add(close, 0, 4);

		Scene scene = new Scene(grid, SCENE_WIDTH, SCENE_HEIGHT);
		scene.getStylesheets().add(TitleScreen.class.getResource("controls.css").toExternalForm());
		scene.getStylesheets().add(
				TitleScreen.class.getResource("TitleScreen.css")
						.toExternalForm());
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	private Button createButton(String text, EventHandler<ActionEvent> handler) {
		Button button = new Button();
		button.setText(text);
		button.setPrefWidth(BUTTON_SIZE);
		button.setOnAction(handler);
		return button;
	}

}
