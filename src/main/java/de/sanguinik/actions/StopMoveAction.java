package de.sanguinik.actions;

import javafx.scene.input.KeyEvent;
import de.sanguinik.model.KeyAction;
import de.sanguinik.model.Direction;

// REPLACE CONDITIONAL WITH POLYMORPHISM: Classe Action específica para parar movimento
// Substitui cases de teclas soltas no switch por comportamento polimórfico
public class StopMoveAction implements KeyAction {
    private final Direction direction;
    private final StopMoveHandler stopHandler;

    // Interface para evitar dependência circular
    public interface StopMoveHandler {
        void handleMoveKeyReleased(Direction direction);
    }

    public StopMoveAction(Direction direction, StopMoveHandler stopHandler) {
        this.direction = direction;
        this.stopHandler = stopHandler;
    }

    @Override
    public void execute(KeyEvent e) {
        stopHandler.handleMoveKeyReleased(direction);
    }
}
