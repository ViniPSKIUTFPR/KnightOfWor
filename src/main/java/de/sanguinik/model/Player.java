package de.sanguinik.model;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
public class Player extends ShootingFigure {

	private int score = 0;
	private int lives = 4;
	private static final int MAX_LIVES = 4;
	private static final String PATH = "/de/sanguinik/model/";
	private boolean isAllowedToMove = false;
	private final static int START_X_PLAYER_1 = 130;
	private final static int START_Y_PLAYER_1 = 510;
	public Label livesLabel;

	public Player(final Maze maze) {
		super(maze, TypeOfFigure.PLAYER, START_X_PLAYER_1, START_Y_PLAYER_1);
		Image image = new Image(
				PATH + "hannes_right.png");
		getImageView().setImage(image);
		getImageView().setX(START_X_PLAYER_1);
		getImageView().setY(START_Y_PLAYER_1);
		lives = 4;
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
			this.getRectangle().setX(START_X_PLAYER_1);
			this.getRectangle().setY(START_Y_PLAYER_1);
			this.getImageView().setX(START_X_PLAYER_1);
			this.getImageView().setY(START_Y_PLAYER_1);
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

}
