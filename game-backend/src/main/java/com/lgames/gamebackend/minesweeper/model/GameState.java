package com.lgames.gamebackend.minesweeper.model;

import com.lgames.gamebackend.minesweeper.enums.GameStatus;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GameState {
    private String gameId;
    private int rows;
    private int cols;
    private int mineCount;
    private Cell[][] board;
    private GameStatus status;
    private int flagsRemaining;

    public GameState() {
    }

    public GameState(String gameId, int rows, int cols, int mineCount, Cell[][] board, GameStatus status, int flagsRemaining) {
        this.gameId = gameId;
        this.rows = rows;
        this.cols = cols;
        this.mineCount = mineCount;
        this.board = board;
        this.status = status;
        this.flagsRemaining = flagsRemaining;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getCols() {
        return cols;
    }

    public void setCols(int cols) {
        this.cols = cols;
    }

    public int getMineCount() {
        return mineCount;
    }

    public void setMineCount(int mineCount) {
        this.mineCount = mineCount;
    }

    public Cell[][] getBoard() {
        return board;
    }

    public void setBoard(Cell[][] board) {
        this.board = board;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public int getFlagsRemaining() {
        return flagsRemaining;
    }

    public void setFlagsRemaining(int flagsRemaining) {
        this.flagsRemaining = flagsRemaining;
    }
}
