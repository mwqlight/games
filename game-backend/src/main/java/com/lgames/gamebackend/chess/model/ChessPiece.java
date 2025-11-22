package com.lgames.gamebackend.chess.model;

import lombok.Data;

public class ChessPiece {
    private ChessPieceType type;
    private ChessPlayer player;
    private int row;
    private int col;

    public ChessPiece() {
    }

    public ChessPiece(ChessPieceType type, ChessPlayer player, int row, int col) {
        this.type = type;
        this.player = player;
        this.row = row;
        this.col = col;
    }

    // Getters and Setters
    public ChessPieceType getType() {
        return type;
    }

    public void setType(ChessPieceType type) {
        this.type = type;
    }

    public ChessPlayer getPlayer() {
        return player;
    }

    public void setPlayer(ChessPlayer player) {
        this.player = player;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }
}