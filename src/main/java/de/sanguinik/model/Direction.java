package de.sanguinik.model;

public enum Direction {

	UP, DOWN, LEFT, RIGHT;
	
	public Direction oposto() {
        switch (this) {
            case UP: return DOWN;
            case DOWN: return UP;
            case LEFT: return RIGHT;
            case RIGHT: return LEFT;
            default: throw new IllegalStateException("Direção inválida: " + this);
        }
    }
}
