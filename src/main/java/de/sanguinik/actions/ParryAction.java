package de.sanguinik.actions;

import javafx.scene.input.KeyEvent;
import de.sanguinik.model.KeyAction;
import de.sanguinik.model.Player;
import de.sanguinik.model.Parry;
import de.sanguinik.view.PlayFieldScreen;

// REPLACE CONDITIONAL WITH POLYMORPHISM: Classe Action específica para parry (defesa)
// Substitui case CONTROL no switch por comportamento polimórfico
public class ParryAction implements KeyAction {
    private final Player player;
    private final PlayFieldScreen screen;

    public ParryAction(Player player, PlayFieldScreen screen) {
        this.player = player;
        this.screen = screen;
    }

    @Override
    public void execute(KeyEvent e) {
        Parry.play(screen.getRoot(), player);
    }
}
