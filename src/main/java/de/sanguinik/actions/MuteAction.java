package de.sanguinik.actions;

import javafx.scene.input.KeyEvent;
import de.sanguinik.model.KeyAction;
import de.sanguinik.view.PlayFieldScreen;

// REPLACE CONDITIONAL WITH POLYMORPHISM: Classe Action específica para mute
// Substitui case M no switch por comportamento polimórfico
public class MuteAction implements KeyAction {
    private final PlayFieldScreen screen;

    public MuteAction(PlayFieldScreen screen) {
        this.screen = screen;
    }

    @Override
    public void execute(KeyEvent e) {
        screen.muteMusic();
    }
}
