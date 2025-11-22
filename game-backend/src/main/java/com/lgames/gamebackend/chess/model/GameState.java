package com.lgames.gamebackend.chess.model;

import java.util.List;

public class GameState {
    private Piece[][] board;
    private PieceColor currentPlayer;
    private String status; // 'playing', 'check', 'checkmate', 'stalemate'
    private List<Move> validMoves;

    public GameState() {
    }

    public GameState(Piece[][] board, PieceColor currentPlayer, String status) {
        this.board = board;
        this.currentPlayer = currentPlayer;
        this.status = status;
    }

    // Getters and Setters
    public Piece[][] getBoard() {
        return board;
    }

    public void setBoard(Piece[][] board) {
        this.board = board;
    }

    public PieceColor getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(PieceColor currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Move> getValidMoves() {
        return validMoves;
    }

    public void setValidMoves(List<Move> validMoves) {
        this.validMoves = validMoves;
    }
}