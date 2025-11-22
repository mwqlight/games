package com.lgames.gamebackend.model;

import java.util.ArrayList;
import java.util.List;

public class GameState {
    private ChessPiece[][] board = new ChessPiece[9][10]; // 9列10行棋盘
    private String currentPlayer; // 当前行棋方："red" | "black"
    private String gameStatus;     // 状态："PLAYING"、"RED_WIN"、"BLACK_WIN"
    private List<Move> moveHistory; // 走子记录

    // 构造函数
    public GameState() {
        this.currentPlayer = "red"; // 红方先行
        this.gameStatus = "PLAYING";
        this.moveHistory = new ArrayList<>();
        initializeBoard();
    }

    // 初始化棋盘
    private void initializeBoard() {
        // 红方棋子
        board[0][0] = new ChessPiece("rook", "red", 0, 0);
        board[1][0] = new ChessPiece("knight", "red", 1, 0);
        board[2][0] = new ChessPiece("bishop", "red", 2, 0);
        board[3][0] = new ChessPiece("advisor", "red", 3, 0);
        board[4][0] = new ChessPiece("king", "red", 4, 0);
        board[5][0] = new ChessPiece("advisor", "red", 5, 0);
        board[6][0] = new ChessPiece("bishop", "red", 6, 0);
        board[7][0] = new ChessPiece("knight", "red", 7, 0);
        board[8][0] = new ChessPiece("rook", "red", 8, 0);
        
        board[1][2] = new ChessPiece("cannon", "red", 1, 2);
        board[7][2] = new ChessPiece("cannon", "red", 7, 2);
        
        board[0][3] = new ChessPiece("pawn", "red", 0, 3);
        board[2][3] = new ChessPiece("pawn", "red", 2, 3);
        board[4][3] = new ChessPiece("pawn", "red", 4, 3);
        board[6][3] = new ChessPiece("pawn", "red", 6, 3);
        board[8][3] = new ChessPiece("pawn", "red", 8, 3);

        // 黑方棋子
        board[0][9] = new ChessPiece("rook", "black", 0, 9);
        board[1][9] = new ChessPiece("knight", "black", 1, 9);
        board[2][9] = new ChessPiece("bishop", "black", 2, 9);
        board[3][9] = new ChessPiece("advisor", "black", 3, 9);
        board[4][9] = new ChessPiece("king", "black", 4, 9);
        board[5][9] = new ChessPiece("advisor", "black", 5, 9);
        board[6][9] = new ChessPiece("bishop", "black", 6, 9);
        board[7][9] = new ChessPiece("knight", "black", 7, 9);
        board[8][9] = new ChessPiece("rook", "black", 8, 9);
        
        board[1][7] = new ChessPiece("cannon", "black", 1, 7);
        board[7][7] = new ChessPiece("cannon", "black", 7, 7);
        
        board[0][6] = new ChessPiece("pawn", "black", 0, 6);
        board[2][6] = new ChessPiece("pawn", "black", 2, 6);
        board[4][6] = new ChessPiece("pawn", "black", 4, 6);
        board[6][6] = new ChessPiece("pawn", "black", 6, 6);
        board[8][6] = new ChessPiece("pawn", "black", 8, 6);
    }

    // getter和setter方法
    public ChessPiece[][] getBoard() {
        return board;
    }

    public void setBoard(ChessPiece[][] board) {
        this.board = board;
    }

    public String getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(String currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public String getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(String gameStatus) {
        this.gameStatus = gameStatus;
    }

    public List<Move> getMoveHistory() {
        return moveHistory;
    }

    public void setMoveHistory(List<Move> moveHistory) {
        this.moveHistory = moveHistory;
    }

    // 复制游戏状态
    public GameState copy() {
        GameState newState = new GameState();
        newState.currentPlayer = this.currentPlayer;
        newState.gameStatus = this.gameStatus;
        newState.moveHistory = new ArrayList<>(this.moveHistory);
        
        // 复制棋盘
        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 10; y++) {
                if (this.board[x][y] != null) {
                    newState.board[x][y] = this.board[x][y].copy();
                }
            }
        }
        
        return newState;
    }
}