package de.sanguinik.model;

import de.sanguinik.view.PlayFieldScreen;
import de.sanguinik.actions.*;
import de.sanguinik.actions.MoveAction.MoveHandler;
import de.sanguinik.actions.StopMoveAction.StopMoveHandler;
import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.util.Map;
import java.util.HashMap;
public class Keyboard implements EventHandler<KeyEvent>, MoveHandler, StopMoveHandler {

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
	
	// REPLACE CONDITIONAL WITH POLYMORPHISM: Mapa de ações por tecla
	private final Map<KeyCode, KeyAction> keyActions;
	private final Map<KeyCode, KeyAction> keyReleaseActions;

	public Keyboard(Player player, PlayFieldScreen screen) {
		this.player = player;
		this.screen = screen;
		this.keyActions = new HashMap<>();
		this.keyReleaseActions = new HashMap<>();
		initializeTimelines();
		initializeNonMoveActions();
		initializeMoveActions();
		initializeReleaseActions();
	}

	// EXTRACT METHOD: Extrai a inicialização dos timelines para melhor organização
	private void initializeTimelines() {
		// MOVE METHOD: Agora usa o método movido para Player
		upTimeline = player.createMoveTimeline(Direction.UP, IMAGE_UP, this);
		downTimeline = player.createMoveTimeline(Direction.DOWN, IMAGE_DOWN, this);
		leftTimeline = player.createMoveTimeline(Direction.LEFT, IMAGE_LEFT, this);
		rightTimeline = player.createMoveTimeline(Direction.RIGHT, IMAGE_RIGHT, this);
	}
	
	// Getter para lastDirection (necessário para o Timeline)
	public Direction getLastDirection() {
		return lastDirection;
	}
	
	// REPLACE CONDITIONAL WITH POLYMORPHISM: Inicializa ações não-movimento
	private void initializeNonMoveActions() {
		keyActions.put(KeyCode.CONTROL, new ParryAction(player, screen));
		keyActions.put(KeyCode.SPACE, new ShootAction(player));
		keyActions.put(KeyCode.P, new PauseAction(player, screen));
		keyActions.put(KeyCode.M, new MuteAction(screen));
	}
	
	// REPLACE CONDITIONAL WITH POLYMORPHISM: Inicializa ações de movimento
	private void initializeMoveActions() {
		// Agora usa polimorfismo completo para ações de movimento
		keyActions.put(KeyCode.UP, new MoveAction(Direction.UP, this));
		keyActions.put(KeyCode.W, new MoveAction(Direction.UP, this));
		keyActions.put(KeyCode.DOWN, new MoveAction(Direction.DOWN, this));
		keyActions.put(KeyCode.S, new MoveAction(Direction.DOWN, this));
		keyActions.put(KeyCode.LEFT, new MoveAction(Direction.LEFT, this));
		keyActions.put(KeyCode.A, new MoveAction(Direction.LEFT, this));
		keyActions.put(KeyCode.RIGHT, new MoveAction(Direction.RIGHT, this));
		keyActions.put(KeyCode.D, new MoveAction(Direction.RIGHT, this));
	}
	
	// REPLACE CONDITIONAL WITH POLYMORPHISM: Inicializa ações de soltar teclas
	private void initializeReleaseActions() {
		keyReleaseActions.put(KeyCode.UP, new StopMoveAction(Direction.UP, this));
		keyReleaseActions.put(KeyCode.W, new StopMoveAction(Direction.UP, this));
		keyReleaseActions.put(KeyCode.DOWN, new StopMoveAction(Direction.DOWN, this));
		keyReleaseActions.put(KeyCode.S, new StopMoveAction(Direction.DOWN, this));
		keyReleaseActions.put(KeyCode.LEFT, new StopMoveAction(Direction.LEFT, this));
		keyReleaseActions.put(KeyCode.A, new StopMoveAction(Direction.LEFT, this));
		keyReleaseActions.put(KeyCode.RIGHT, new StopMoveAction(Direction.RIGHT, this));
		keyReleaseActions.put(KeyCode.D, new StopMoveAction(Direction.RIGHT, this));
	}

	@Override
	public void handle(KeyEvent e) {
		if (e.getEventType() == KeyEvent.KEY_PRESSED) {
			handleKeyPressed(e);
		} else if (e.getEventType() == KeyEvent.KEY_RELEASED) {
			handleKeyReleased(e);
		}
	}

	// EXTRACT METHOD: Extrai o tratamento de teclas pressionadas
	private void handleKeyPressed(KeyEvent e) {
		// INLINE TEMP: Elimina variável temporária desnecessária
		// REPLACE CONDITIONAL WITH POLYMORPHISM: Usa mapa de ações para TODAS as teclas
		KeyAction action = keyActions.get(e.getCode());
		if (action != null) {
			action.execute(e);
		}
	}

	// Implementação da interface MoveHandler
	@Override
	public void handleMoveKeyPressed(Direction direction) {
		switch (direction) {
			case UP:
				if (!upPressed) {
					upPressed = true;
					upTimeline.play();
				}
				lastDirection = Direction.UP;
				break;
			case DOWN:
				if (!downPressed) {
					downPressed = true;
					downTimeline.play();
				}
				lastDirection = Direction.DOWN;
				break;
			case LEFT:
				if (!leftPressed) {
					leftPressed = true;
					leftTimeline.play();
				}
				lastDirection = Direction.LEFT;
				break;
			case RIGHT:
				if (!rightPressed) {
					rightPressed = true;
					rightTimeline.play();
				}
				lastDirection = Direction.RIGHT;
				break;
		}
	}

	// Implementação da interface StopMoveHandler  
	@Override
	public void handleMoveKeyReleased(Direction direction) {
		switch (direction) {
			case UP:
				upPressed = false;
				upTimeline.stop();
				break;
			case DOWN:
				downPressed = false;
				downTimeline.stop();
				break;
			case LEFT:
				leftPressed = false;
				leftTimeline.stop();
				break;
			case RIGHT:
				rightPressed = false;
				rightTimeline.stop();
				break;
		}
	}

	// EXTRACT METHOD: Extrai o tratamento de teclas soltas
	private void handleKeyReleased(KeyEvent e) {
		// INLINE TEMP: Elimina variável temporária desnecessária
		// REPLACE CONDITIONAL WITH POLYMORPHISM: Usa mapa de ações para teclas soltas
		KeyAction action = keyReleaseActions.get(e.getCode());
		if (action != null) {
			action.execute(e);
		}
		updateLastDirection();
	}

	// EXTRACT METHOD: Extrai a atualização da última direção
	private void updateLastDirection() {
		if (upPressed) lastDirection = Direction.UP;
		else if (downPressed) lastDirection = Direction.DOWN;
		else if (leftPressed) lastDirection = Direction.LEFT;
		else if (rightPressed) lastDirection = Direction.RIGHT;
	}
}
