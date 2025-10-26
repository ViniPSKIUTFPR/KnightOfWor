/**
 * 
 */
package de.sanguinik.persistence;

import java.util.List;

import de.sanguinik.model.HighscoreModel;

/**
 * @author marlene This is the interface for the Highscore. It gets and sets the
 *         score of a player.
 */
public interface Highscore {
	void saveHighscore(HighscoreModel score);

	/**
	 * This method should load the actual highscore.
	 * 
	 * @return a String with the current highscore.
	 */
	List<HighscoreModel> loadHighscore();
}
