package de.sanguinik.persistence;

import java.util.Date;

public class HighscoreDTO {
    private String name;
    private int score;  
    private String levelTime; // MELHORIA: Tempo da fase
    private Date date;
    
    public HighscoreDTO(String name, int score, Date date) {
        this.name = name;
        this.score = score;
        this.levelTime = "--:--"; // Valor padrão para compatibilidade
        this.date = date;
    }
    
    // MELHORIA: Construtor com tempo da fase
    public HighscoreDTO(String name, int score, String levelTime, Date date) {
        this.name = name;
        this.score = score;
        this.levelTime = levelTime;
        this.date = date;
    }

    public String getName() {
        return name;
    }
    public int getScore() {
        return score;
    }
    // MELHORIA: Getter para tempo da fase
    public String getLevelTime() {
        return levelTime != null ? levelTime : "--:--";
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
    // MELHORIA: Setter para tempo da fase
    public void setLevelTime(String levelTime) {
        this.levelTime = levelTime;
    }
    public void setDate(Date date) {
        this.date = date;
    }

    
}
