package com.lgames.gamebackend.minesweeper.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MinesweeperGame {
    private String gameId;
    private int rows;
    private int cols;
    private int mines;
    private Cell[][] board;
    private GameStatus gameStatus;
    private int remainingMines;
    private int clearedCells;
    private long startTime;
    private long elapsedTime;

    public MinesweeperGame(int rows, int cols, int mines) {
        this.gameId = UUID.randomUUID().toString();
        this.rows = rows;
        this.cols = cols;
        this.mines = mines;
        this.remainingMines = mines;
        this.clearedCells = 0;
        this.gameStatus = GameStatus.PLAYING;
        this.startTime = System.currentTimeMillis();
        this.elapsedTime = 0;
        this.board = new Cell[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                board[i][j] = new Cell();
            }
        }
    }

    public enum GameStatus {
        PLAYING, GAME_OVER, SUCCESS
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Cell {
        private boolean isMine;
        private boolean revealed;
        private boolean flagged;
        private int adjacentMines;
    }
}
