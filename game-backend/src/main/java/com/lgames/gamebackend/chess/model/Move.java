package com.lgames.gamebackend.chess.model;

public class Move {
    private Position from;
    private Position to;
    private PieceType promotion; // 兵的升变

    public Move() {
    }

    public Move(Position from, Position to) {
        this.from = from;
        this.to = to;
    }

    public Move(Position from, Position to, PieceType promotion) {
        this.from = from;
        this.to = to;
        this.promotion = promotion;
    }

    // Getters and Setters
    public Position getFrom() {
        return from;
    }

    public void setFrom(Position from) {
        this.from = from;
    }

    public Position getTo() {
        return to;
    }

    public void setTo(Position to) {
        this.to = to;
    }

    public PieceType getPromotion() {
        return promotion;
    }

    public void setPromotion(PieceType promotion) {
        this.promotion = promotion;
    }

    @Override
    public String toString() {
        return "Move{from=" + from + ", to=" + to + ", promotion=" + promotion + "}";
    }
}