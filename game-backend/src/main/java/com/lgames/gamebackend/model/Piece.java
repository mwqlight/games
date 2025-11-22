package com.lgames.gamebackend.model;

public class Piece {
    private PlayerColor color;
    private Position position;
    private boolean isKing;

    public Piece(PlayerColor color, Position position) {
        this.color = color;
        this.position = position;
        this.isKing = false;
    }

    public PlayerColor getColor() {
        return color;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public boolean isKing() {
        return isKing;
    }

    public void promoteToKing() {
        this.isKing = true;
    }

    public boolean shouldPromote() {
        if (isKing) {
            return false;
        }
        if (color == PlayerColor.RED) {
            return position.getRow() == 7;
        } else {
            return position.getRow() == 0;
        }
    }
}
