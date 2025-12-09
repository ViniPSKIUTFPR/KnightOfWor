package de.sanguinik.model;

import java.util.Random;
import java.util.List;
import java.util.ArrayList;

import javafx.scene.image.Image;
import javafx.scene.Group;
import javafx.scene.shape.Rectangle;

public class Enemy extends ShootingFigure {
    private final List<WizardAttack> wizardAttacks = new ArrayList<WizardAttack>();

    private int health = 1;
    private Group root;
    private Player player;

    private static final String PACKAGE_PATH = "/de/sanguinik/model/";
    private static final Image BURWOR_IMAGE = new Image(PACKAGE_PATH
            + "BURWOR.png");
    private static final Image GARWOR_IMAGE = new Image(PACKAGE_PATH
            + "GARWOR.png");
    private static final Image THORWOR_IMAGE = new Image(PACKAGE_PATH
            + "THORWOR.png");
    private static final Image WIZARD_IMAGE = new Image(PACKAGE_PATH
            + "WIZARD.png");
    private static final Image WORLUK_IMAGE = new Image(PACKAGE_PATH
            + "WORLUK.png");

    public Enemy(final Maze maze, final Target target) {
        this(maze, target, null, null, null);
    }

    // Construtor para o wizard que chama o contrutor geral
    public Enemy(final Maze maze, final Target target, Group root, Player player) {
        this(maze, target, root, player, null);
    }

    public void stopWizardAttack() {
        for (WizardAttack wa : wizardAttacks) {
            if (wa != null) wa.stop();
        }
    }
    public void startWizardAttack() {
        for (WizardAttack wa : wizardAttacks) {
            if (wa != null) wa.start();
        }
    }

    private Enemy(final Maze maze, final Target target, Group root, Player player, Object unused) {
        super(maze, target);
        this.root = root;
        this.player = player;
        setImageByMonster(target.getTypeOfFigure());
        getImageView().setX(target.getPosition().getX());
        getImageView().setY(target.getPosition().getY());
        if (target.getTypeOfFigure() == TypeOfFigure.WIZARD && root != null && player != null) {
            WizardAttack wa = new WizardAttack(root, player, maze);
            wa.start();
            wizardAttacks.add(wa);
            this.health = 3;
        }
    }

    public String getName() {
        return getType().name();
    }

    private void setImageByMonster(final TypeOfFigure type) {
        switch (type) {
        case BURWOR:
            getImageView().setImage(BURWOR_IMAGE);
            break;
        case GARWOR:
            getImageView().setImage(GARWOR_IMAGE);
            break;
        case THORWOR:
            getImageView().setImage(THORWOR_IMAGE);
            break;
        case WIZARD:
            getImageView().setImage(WIZARD_IMAGE);
            break;
        case WORLUK:
            getImageView().setImage(WORLUK_IMAGE);
            break;
        default:
            break;
        }
    }

    @Override
    public void onCollisionWithMaze() {
        changeToRandomDirection();
    }

    private void changeToRandomDirection() {
        int random = new Random().nextInt(4);

        Direction futureDirection = Direction.values()[random];
        setDirection(futureDirection);
    }
    
    @Override
    public void move(){
        if (willCollideWithMazeInFuture()) {
            onCollisionWithMaze();
        } else {
            
            int likelihood = (int) (30 * (1/0.7));
            int random = new Random().nextInt(likelihood);
            if(random == 0){
                changeToRandomDirection();
            }
            super.move();

            double speed = Math.max(0.0, getType().getSpeed());
            int whole = (int) Math.floor(speed);
            for (int k = 0; k < whole; k++) {
                super.move();
            }
        }
    }

    public void takeDamage() {
        health--;
        if (health <= 0) {
            setAlive(false);
        } else {
			if (getType() == TypeOfFigure.WIZARD){
				takeDamageWizard();
			}
        }
    }

	public void takeDamageWizard(){
		// Logica para o Wizard: ao tomar dano, se teleporta e instancia um novo ataque
		if (root != null && player != null) {
			WizardAttack newAttack = new WizardAttack(root, player, getMaze());
			newAttack.start();
			wizardAttacks.add(newAttack);

			// Usa a própria WizardAttack pra escolher uma posição segura para teleport
			final int MAX_ATTEMPTS = 50;
			double w = getRectangle().getWidth();
			double h = getRectangle().getHeight();

			Position pos = null;
			try {
				pos = newAttack.pickRandomTeleportPosition(MAX_ATTEMPTS, w, h, cd);
			} catch (Exception e) {
				pos = null;
			}

			if (pos != null) {
				getRectangle().setX(pos.getX());
				getRectangle().setY(pos.getY());
				getImageView().setX(pos.getX());
				getImageView().setY(pos.getY());
			} else {
				// fallback: try random grid cells based on maze dimensions
				Random rnd = new Random();
				int attempts = 0;
				int cellSize = getMaze().getCellSize();

				while (attempts < MAX_ATTEMPTS) {
					int col = rnd.nextInt(getMaze().getWidth());
					int row = rnd.nextInt(getMaze().getHeight());
					double newX = col * cellSize;
					double newY = row * cellSize;
					Rectangle test = new Rectangle(newX, newY, w, h);
					if (!cd.isCollide(getMaze().getWalls(), test)) {
						getRectangle().setX(newX);
						getRectangle().setY(newY);
						getImageView().setX(newX);
						getImageView().setY(newY);
						break;
					}
					attempts++;
				}
			}
		}
	}

    public int getHealth() {
        return health;
    }

    @Override
    public void bulletHasHitATarget(final Figure target) {
        super.bulletHasHitATarget(target);
        target.setAlive(false);
    }

    @Override
    public void setAlive(boolean alive) {
        super.setAlive(alive);

        // Se o mago morrer, para todos os ataques associados
        if (!alive) {
            for (WizardAttack wa : wizardAttacks) {
                if (wa != null) wa.stop();
            }
            wizardAttacks.clear();
        }
    }

}