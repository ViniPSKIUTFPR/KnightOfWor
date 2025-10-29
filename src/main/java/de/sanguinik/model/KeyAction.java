package de.sanguinik.model;

import javafx.scene.input.KeyEvent;

// REPLACE CONDITIONAL WITH POLYMORPHISM: Interface para ações de teclado
// Substitui switch statements por comportamento polimórfico
public interface KeyAction {
    void execute(KeyEvent e);
}
