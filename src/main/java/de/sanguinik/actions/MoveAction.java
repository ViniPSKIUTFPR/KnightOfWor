package de.sanguinik.actions;

import javafx.scene.input.KeyEvent;
import de.sanguinik.model.KeyAction;
import de.sanguinik.model.Direction;

// REPLACE CONDITIONAL WITH POLYMORPHISM: Classe Action específica para movimento
// Substitui cases de movimento no switch por comportamento polimórfico
public class MoveAction implements KeyAction {
    private final Direction direction;
    private final MoveHandler moveHandler;

    // Interface para evitar dependência circular
    public interface MoveHandler {
        void handleMoveKeyPressed(Direction direction);
    }

    public MoveAction(Direction direction, MoveHandler moveHandler) {
        this.direction = direction;
        this.moveHandler = moveHandler;
    }

    @Override
    public void execute(KeyEvent e) {
        moveHandler.handleMoveKeyPressed(direction);
    }
}
