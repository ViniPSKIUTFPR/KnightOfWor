package de.sanguinik.model;

import java.util.Date;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class HighscoreModel {

	private final SimpleStringProperty name;
	private final SimpleIntegerProperty score;
	private final Date date;
	
	public HighscoreModel(String name, int score, Date date) {
		this.name = new SimpleStringProperty(name);
		this.score = new SimpleIntegerProperty(score);
		this.date = date;
	}
	
	public String getName(){
		return name.get();
	}
	
	public int getScore(){
		return score.get();
	}

	public Date getDate() {
		return date;
	}

}
