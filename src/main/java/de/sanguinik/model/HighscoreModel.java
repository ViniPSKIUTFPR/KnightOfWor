package de.sanguinik.model;

import java.util.Date;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class HighscoreModel {

	private final SimpleStringProperty name;
	private final SimpleIntegerProperty score;
	private final SimpleStringProperty levelTime; // MELHORIA: Tempo da fase
	private final Date date;
	
	public HighscoreModel(String name, int score, Date date) {
		this.name = new SimpleStringProperty(name);
		this.score = new SimpleIntegerProperty(score);
		this.levelTime = new SimpleStringProperty("--:--"); // Tempo padrão para pontuações antigas
		this.date = date;
	}
	
	// MELHORIA: Construtor com tempo da fase
	public HighscoreModel(String name, int score, String levelTime, Date date) {
		this.name = new SimpleStringProperty(name);
		this.score = new SimpleIntegerProperty(score);
		this.levelTime = new SimpleStringProperty(levelTime);
		this.date = date;
	}
	
	public String getName(){
		return name.get();
	}
	
	public int getScore(){
		return score.get();
	}

	// MELHORIA: Getter para o tempo da fase
	public String getLevelTime(){
		return levelTime.get();
	}

	public Date getDate() {
		return date;
	}

}
