package de.sanguinik.persistence;

import java.util.Date;

public class HighscoreDTO {
    private String name;
    private int score;  
    private Date date;
    
    public HighscoreDTO(String name, int score, Date date) {
        this.name = name;
        this.score = score;
        this.date = date;
    }

    public String getName() {
        return name;
    }
    public int getScore() {
        return score;
    }
    public Date getDate() {
        return date;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setScore(int score) {
        this.score = score;
    }
    public void setDate(Date date) {
        this.date = date;
    }

    
}
