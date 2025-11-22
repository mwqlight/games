package com.lgames.gamebackend.model;

import java.util.List;

public class Move {
    private Position from;
    private Position to;
    private List<Position> capturedPositions;
    private boolean isCaptureMove;
    private boolean isKingPromotion;

    public Move(Position from, Position to) {
        this.from = from;
        this.to = to;
        this.isCaptureMove = false;
        this.isKingPromotion = false;
    }

    public Move(Position from, Position to, List<Position> capturedPositions) {
        this.from = from;
        this.to = to;
        this.capturedPositions = capturedPositions;
        this.isCaptureMove = capturedPositions != null && !capturedPositions.isEmpty();
        this.isKingPromotion = false;
    }

    public Position getFrom() {
        return from;
    }

    public Position getTo() {
        return to;
    }

    public List<Position> getCapturedPositions() {
        return capturedPositions;
    }

    public boolean isCaptureMove() {
        return isCaptureMove;
    }

    public boolean isKingPromotion() {
        return isKingPromotion;
    }

    public void setKingPromotion(boolean kingPromotion) {
        isKingPromotion = kingPromotion;
    }
}
