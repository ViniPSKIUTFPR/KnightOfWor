package de.sanguinik.model;

public enum TypeOfFigure {

	PLAYER(1000, 1.0),

	BURWOR(100, 0.5),

	GARWOR(200, 0.7),

	THORWOR(500, 0.9),

	WORLUK(1000, 1.5),

	WIZARD(2500, 1.8),

	BULLET(0, 0.0)
	;

	private int points;
	private double speed;

	private TypeOfFigure(final int points, final double speed) {
		this.points = points;
		this.speed = speed;
	}

	public int getPoints() {
		return points;
	}

	public double getSpeed() {
		return speed;
	}
}
