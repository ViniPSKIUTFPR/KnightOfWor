package de.sanguinik.actions;

import javafx.scene.input.KeyEvent;
import de.sanguinik.model.KeyAction;
import de.sanguinik.model.Player;
import de.sanguinik.view.PlayFieldScreen;

// REPLACE CONDITIONAL WITH POLYMORPHISM: Classe Action específica para pausar
// Substitui case P no switch por comportamento polimórfico
public class PauseAction implements KeyAction {
    private final Player player;
    private final PlayFieldScreen screen;

    public PauseAction(Player player, PlayFieldScreen screen) {
        this.player = player;
        this.screen = screen;
    }

    @Override
    public void execute(KeyEvent e) {
        screen.pauseGame();
        player.toggleMoveable();
    }
}
