package com.lgames.gamebackend.chess.model;

import lombok.Data;

public class ChessMove {
    private int fromRow;
    private int fromCol;
    private int toRow;
    private int toCol;
    private ChessPlayer player;

    public ChessMove() {
    }

    public ChessMove(int fromRow, int fromCol, int toRow, int toCol, ChessPlayer player) {
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toRow = toRow;
        this.toCol = toCol;
        this.player = player;
    }

    // Getters and Setters
    public int getFromRow() {
        return fromRow;
    }

    public void setFromRow(int fromRow) {
        this.fromRow = fromRow;
    }

    public int getFromCol() {
        return fromCol;
    }

    public void setFromCol(int fromCol) {
        this.fromCol = fromCol;
    }

    public int getToRow() {
        return toRow;
    }

    public void setToRow(int toRow) {
        this.toRow = toRow;
    }

    public int getToCol() {
        return toCol;
    }

    public void setToCol(int toCol) {
        this.toCol = toCol;
    }

    public ChessPlayer getPlayer() {
        return player;
    }

    public void setPlayer(ChessPlayer player) {
        this.player = player;
    }
}