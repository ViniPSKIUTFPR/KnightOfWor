package de.sanguinik.actions;

import javafx.scene.input.KeyEvent;
import de.sanguinik.model.KeyAction;
import de.sanguinik.model.Player;

// REPLACE CONDITIONAL WITH POLYMORPHISM: Classe Action específica para tiro
// Substitui case SPACE no switch por comportamento polimórfico
public class ShootAction implements KeyAction {
    private final Player player;

    public ShootAction(Player player) {
        this.player = player;
    }

    @Override
    public void execute(KeyEvent e) {
        if (player.isMovable()) {
            player.shoot();
        }
    }
}
