package de.sanguinik.model;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.media.AudioClip;

public abstract class ShootingFigure extends Figure {

	private boolean hasBullet;
	private AudioClip sound;
	private boolean invincible = false;
	private boolean bloqueando = false;
	private final List<Enemy> enemyList = new ArrayList<Enemy>();

	private ShootCallback shootCallback;

	private final List<Figure> targets = new ArrayList<Figure>();

	public ShootingFigure(final Maze maze, final TypeOfFigure type,
			final double x, final double y) {
		super(maze, type, x, y);
	}

	public void shoot() {
		URL resource = getClass().getResource("fire.mp3");
		if (resource != null) {
			sound = new AudioClip(resource.toString());
		}

		if (!hasBullet) {
			hasBullet = true;

			double x = getRectangle().getX();
			double y = getRectangle().getY();

			Bullet bullet = new Bullet(getMaze(), getDirection(), x, y, this);

			bullet.setTargets(targets);
			bullet.setInimigos(enemyList);

			if (shootCallback != null) {
				shootCallback.shootBullet(bullet);
			}
			if (!sound.isPlaying()) {
				sound.play();
			}

		}
	}

	public void setShootCallback(final ShootCallback callback) {
		shootCallback = callback;
	}

	public void bulletHasArrived() {
		hasBullet = false;
	}

	public List<Figure> getTargets() {
		return targets;
	}

	public void addTargets(final Figure... targets) {
		for (Figure f : targets) {
			this.targets.add(f);
		}
	}

	public void setInimigos(List<Enemy> inimigos) {
		for (Enemy inimigo : inimigos) {
			this.enemyList.add(inimigo);
		}
	}

	public void bulletHasHitTheMaze() {
		hasBullet = false;
	}

	public void setInvincible(boolean isInvincible){
		invincible = isInvincible;
	}
	
	public boolean isInvincible(){
		return invincible;
	}

	public void setBloqueando(boolean isBloqueando){
		bloqueando = isBloqueando;
		setInvincible(bloqueando);
	}
	
	public boolean isBloqueando(){
		return bloqueando;
	}

	/**
	 * This method can be overridden to implement logic that should happen when
	 * the bullet has hit a target.
	 * 
	 * When you override this method, please add a call to this method in the
	 * super type so that the "hasBullet" flag is set to false correctly. i.e.:
	 * 
	 * <pre>
	 * <code>
	 * 
	 * {@literal @}Override 
	 * public void bulletHasHitATarget(final Figure target){
	 *           super.bulletHasHitATarget(target);
	 * 
	 *           // your logic here 
	 * }
	 * 
	 *           </code>
	 * </pre>
	 * 
	 * @param target
	 */
	public void bulletHasHitATarget(final Figure target) {
		hasBullet = false;
	}

}
