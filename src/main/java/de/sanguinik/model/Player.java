package de.sanguinik.model;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class Player extends ShootingFigure {

	private int score = 0;
	private int lives = 4;
	private static final int MAX_LIVES = 4;
	private static final String PATH = "/de/sanguinik/model/";
	private boolean isAllowedToMove = false;
	private final static Position START_POSITION_PLAYER_1 = new Position(130, 510);
	public Label livesLabel;

	public Player(final Maze maze) {
		super(maze, new Target(TypeOfFigure.PLAYER, new Position(START_POSITION_PLAYER_1.getX(), START_POSITION_PLAYER_1.getY())));
		Image image = new Image(
				PATH + "hannes_right.png");
		getImageView().setImage(image);
		getImageView().setX(START_POSITION_PLAYER_1.getX());
		getImageView().setY(START_POSITION_PLAYER_1.getY());
		livesLabel = new Label("Leben: " + lives);
		livesLabel.setLayoutY(40);
	}

	public int getScore() {
		return score;
	}

	public Label getLivesLabel() {
		return livesLabel;
	}

	public void setScore(final int score) {
		this.score = score;
	}

	@Override
	public void setAlive(final boolean alive) {
		// Se o jogador tomou dano
		if (!alive) {
			if (isBloqueando()) BloqueioAudioVisual();
			if (isInvincible()) return;
			this.getRectangle().setX(START_POSITION_PLAYER_1.getX());
			this.getRectangle().setY(START_POSITION_PLAYER_1.getY());
			this.getImageView().setX(START_POSITION_PLAYER_1.getX());
			this.getImageView().setY(START_POSITION_PLAYER_1.getY());
		}
		super.setAlive(alive);
	}

	public int getLives() {
		return lives;
	}

	public void setLives(final int lives) {
		this.lives = Math.min(lives, MAX_LIVES);
		if (livesLabel != null) {
			livesLabel.setText("Leben: " + this.lives);
		}
	}

	public void loseLife() {
		if (lives > 0) {
			setLives(lives - 1);
		}
	}

	public int getMaxLives() {
		return MAX_LIVES;
	}
	
	public boolean isMovable(){
		return isAllowedToMove;
	}

	/**
	 * We need to override the move method to check if the player hits an enemy.
	 */
	@Override
	public void move(){
		Figure enemy = checkForCollisionWithEnemies();
		
		   if(enemy == null && isAllowedToMove){
			   super.move();
		   }
	}

	private Figure checkForCollisionWithEnemies(){
		
		if(!isInvincible()){
			for(Figure enemy : getTargets()){
				if(!(enemy.getType().equals(TypeOfFigure.BULLET)) && !(enemy.getType().equals(TypeOfFigure.PLAYER))){
					if(cd.isCollide(enemy.getRectangle(), this.getRectangle())){
						return enemy;
					}
				}
			}
		}
		
		return null;
	}
	
	
	/**
	 * If the player collides with the maze, nothing happens.
	 */
	@Override
	public void onCollisionWithMaze() {
	}

	@Override
	public void bulletHasHitATarget(final Figure target) {
		super.bulletHasHitATarget(target);
		target.setAlive(false);

		int points = target.getType().getPoints();
		score += points;
	}

	public void toggleMoveable() {
		if(isAllowedToMove){
			isAllowedToMove = false;
		}else{
			isAllowedToMove = true;
		}
	}
	
	// MOVE METHOD: Movido de Keyboard - elimina Feature Envy
	// Este método está mais relacionado ao Player do que ao Keyboard
	public Timeline createMoveTimeline(Direction dir, Image img, Keyboard keyboard) {
		Timeline t = new Timeline(new KeyFrame(Duration.millis(30), e -> {
			if (this.isMovable()) {
				this.setDirection(dir);
				this.move();
				// Só altera a imagem se for a última direção pressionada
				if (keyboard.getLastDirection() == dir) {
					this.getImageView().setImage(img);
				}
			}
		}));
		t.setCycleCount(Timeline.INDEFINITE);
		return t;
	}

}
