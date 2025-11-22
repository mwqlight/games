package com.lgames.gamebackend.chess.model;

public class Piece {
    private PieceType type;
    private PieceColor color;
    private Position position;
    private boolean hasMoved; // 用于判断是否已经移动过，例如王和车的王车易位

    public Piece() {
    }

    public Piece(PieceType type, PieceColor color, Position position) {
        this.type = type;
        this.color = color;
        this.position = position;
        this.hasMoved = false;
    }

    // Getters and Setters
    public PieceType getType() {
        return type;
    }

    public void setType(PieceType type) {
        this.type = type;
    }

    public PieceColor getColor() {
        return color;
    }

    public void setColor(PieceColor color) {
        this.color = color;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public boolean isHasMoved() {
        return hasMoved;
    }

    public void setHasMoved(boolean hasMoved) {
        this.hasMoved = hasMoved;
    }

    @Override
    public String toString() {
        return "Piece{type=" + type + ", color=" + color + ", position=" + position + ", hasMoved=" + hasMoved + "}";
    }
}