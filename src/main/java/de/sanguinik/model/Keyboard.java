package de.sanguinik.model;

import de.sanguinik.view.PlayFieldScreen;
import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class Keyboard implements EventHandler<KeyEvent> {

	private final Player player;
	private final PlayFieldScreen screen;
	private static final String PATH = "/de/sanguinik/model/";
	private static final Image IMAGE_UP = new Image(PATH + "hannes_up.png");
	private static final Image IMAGE_DOWN = new Image(PATH + "hannes_down.png");
	private static final Image IMAGE_LEFT = new Image(PATH + "hannes_left.png");
	private static final Image IMAGE_RIGHT = new Image(PATH
			+ "hannes_right.png");

	// Timelines para movimento contínuo
	private Timeline upTimeline, downTimeline, leftTimeline, rightTimeline;
	// Flags para evitar múltiplos timelines
	private boolean upPressed = false, downPressed = false, leftPressed = false, rightPressed = false;
	// Última direção pressionada
	private Direction lastDirection = null;

	public Keyboard(Player player, PlayFieldScreen screen) {
		this.player = player;
		this.screen = screen;

		upTimeline = createMoveTimeline(Direction.UP, IMAGE_UP);
		downTimeline = createMoveTimeline(Direction.DOWN, IMAGE_DOWN);
		leftTimeline = createMoveTimeline(Direction.LEFT, IMAGE_LEFT);
		rightTimeline = createMoveTimeline(Direction.RIGHT, IMAGE_RIGHT);
	}

	// TimeLine pra impedir o delay na movimentação depois de pressionar a tecla. Duration.millis() define a velocidade do personagem
	private Timeline createMoveTimeline(Direction dir, Image img) {
		Timeline t = new Timeline(new KeyFrame(Duration.millis(40), e -> {
			if (player.isMovable()) {
				player.setDirection(dir);
				player.move();
				// Só altera a imagem se for a última direção pressionada (pro personagem nao ficar bugado quando anda na diagonal)
				if (lastDirection == dir) {
					player.getImageView().setImage(img);
				}
			}
		}));
		t.setCycleCount(Timeline.INDEFINITE);
		return t;
	}

	@Override
	public void handle(KeyEvent e) {
		KeyCode code = e.getCode();
		if (e.getEventType() == KeyEvent.KEY_PRESSED) {
			switch (code) {
				case UP:
				case W:
					if (!upPressed) {
						upPressed = true;
						lastDirection = Direction.UP;
						upTimeline.play();
					} else {
						lastDirection = Direction.UP;
					}
					break;
				case DOWN:
				case S:
					if (!downPressed) {
						downPressed = true;
						lastDirection = Direction.DOWN;
						downTimeline.play();
					} else {
						lastDirection = Direction.DOWN;
					}
					break;
				case LEFT:
				case A:
					if (!leftPressed) {
						leftPressed = true;
						lastDirection = Direction.LEFT;
						leftTimeline.play();
					} else {
						lastDirection = Direction.LEFT;
					}
					break;
				case RIGHT:
				case D:
					if (!rightPressed) {
						rightPressed = true;
						lastDirection = Direction.RIGHT;
						rightTimeline.play();
					} else {
						lastDirection = Direction.RIGHT;
					}
					break;
				case SPACE:
					if (player.isMovable()) {
						player.shoot();
					}
					break;
				case P:
					screen.pauseGame();
					player.toggleMoveable();
					break;
				case M:
					screen.muteMusic();
					break;
				default:
					break;
			}
		} else if (e.getEventType() == KeyEvent.KEY_RELEASED) {
			switch (code) {
				case W:
				case UP:
					upPressed = false;
					upTimeline.stop();
					break;
				case S:
				case DOWN:
					downPressed = false;
					downTimeline.stop();
					break;
				case A:
				case LEFT:
					leftPressed = false;
					leftTimeline.stop();
					break;
				case D:
				case RIGHT:
					rightPressed = false;
					rightTimeline.stop();
					break;
				default:
					break;
			}
			// Atualiza a última direção para a que ainda está pressionada (se houver)
			if (upPressed) lastDirection = Direction.UP;
			else if (downPressed) lastDirection = Direction.DOWN;
			else if (leftPressed) lastDirection = Direction.LEFT;
			else if (rightPressed) lastDirection = Direction.RIGHT;
		}
	}
}
