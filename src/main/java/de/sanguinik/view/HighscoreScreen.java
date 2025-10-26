package de.sanguinik.view;

import java.util.List;

import de.sanguinik.model.HighscoreModel;
import de.sanguinik.persistence.HighscoreImpl;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class HighscoreScreen extends Application {

	private static final int GRID_GAP = 30;
	private static final int MIN_WIDTH = 150;
	private static final int SCENE_WIDTH = 1024;
	private static final int SCENE_HEIGHT = 740;
	
	@Override
	public void start(final Stage primaryStage) {

		primaryStage.setTitle("Knight of Wor - Pontuacao");
		primaryStage.setResizable(false);

		GridPane grid = new GridPane();
		grid.setId("highscoreGrid");
		grid.setAlignment(Pos.CENTER);
		grid.setHgap(GRID_GAP);
		grid.setVgap(GRID_GAP);
		
		final Label highscoreTitle = new Label("Pontuacao");
		
		HighscoreImpl highscore = new HighscoreImpl();

		List<HighscoreModel> scores = highscore.loadHighscore();
		
		final ObservableList<HighscoreModel> dummyData = FXCollections.observableArrayList(scores);
		
		TableView highscoreTable = new TableView();
		
		highscoreTable.setId("highscoreTable");
		
		TableColumn nameCol = new TableColumn("Jogador");
		nameCol.setMinWidth(MIN_WIDTH);
		nameCol.setCellValueFactory(new PropertyValueFactory<HighscoreModel, String>("name"));
		nameCol.setSortable(false);
		nameCol.setResizable(false);

		TableColumn scoreCol = new TableColumn("Pontos");
		scoreCol.setMinWidth(MIN_WIDTH);
		scoreCol.setCellValueFactory(new PropertyValueFactory<HighscoreModel, Integer>("score"));
		scoreCol.setSortable(false);
		scoreCol.setResizable(false);
		
		highscoreTable.setItems(dummyData);
		highscoreTable.getColumns().addAll(nameCol, scoreCol);
		
		Button okBtn = new Button();
		okBtn.setText("Voltar");
		okBtn.setAlignment(Pos.CENTER);
		okBtn.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				TitleScreen title = new TitleScreen();
				title.start(primaryStage);
			}

		});

		grid.add(highscoreTitle, 0,0);
		grid.add(highscoreTable, 0,1);
		grid.add(okBtn,0,2);
		StackPane root = new StackPane();
		root.getChildren().add(grid);
		Scene scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT);
		scene.getStylesheets().add(TitleScreen.class.getResource("controls.css").toExternalForm());
		scene.getStylesheets().add(
				Credits.class.getResource("HighscoreScreen.css").toExternalForm());
		primaryStage.setScene(scene);
		primaryStage.show();

	}
}