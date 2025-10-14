package de.sanguinik.model;

public class Target {
    private final TypeOfFigure typeOfFigure;
    private final Position position;

    public Target(TypeOfFigure typeOfFigure, Position position) {
        this.typeOfFigure = typeOfFigure;
        this.position = position;
    }
    
    public TypeOfFigure getTypeOfFigure() { return typeOfFigure; }
    public Position getPosition() { return position; }
}
