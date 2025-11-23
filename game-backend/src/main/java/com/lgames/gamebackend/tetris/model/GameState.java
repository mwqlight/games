package com.lgames.gamebackend.tetris.model;

import java.util.Arrays;

public class GameState {
    private String gameId;
    private int[][] grid;
    private Tetromino currentPiece;
    private Tetromino nextPiece;
    private int score;
    private int linesCleared;
    private int level;
    private GameStatus gameStatus;

    public enum GameStatus {
        PLAYING, PAUSED, GAME_OVER
    }

    public GameState() {
        this.grid = new int[20][10];
        this.gameStatus = GameStatus.PLAYING;
        this.score = 0;
        this.linesCleared = 0;
        this.level = 1;
    }

    // Getters and Setters
    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public int[][] getGrid() {
        return grid;
    }

    public void setGrid(int[][] grid) {
        this.grid = grid;
    }

    public Tetromino getCurrentPiece() {
        return currentPiece;
    }

    public void setCurrentPiece(Tetromino currentPiece) {
        this.currentPiece = currentPiece;
    }

    public Tetromino getNextPiece() {
        return nextPiece;
    }

    public void setNextPiece(Tetromino nextPiece) {
        this.nextPiece = nextPiece;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getLinesCleared() {
        return linesCleared;
    }

    public void setLinesCleared(int linesCleared) {
        this.linesCleared = linesCleared;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }

    @Override
    public String toString() {
        return "GameState{" +
                "gameId='" + gameId + '\'' +
                ", grid=" + Arrays.deepToString(grid) +
                ", currentPiece=" + currentPiece +
                ", nextPiece=" + nextPiece +
                ", score=" + score +
                ", linesCleared=" + linesCleared +
                ", level=" + level +
                ", gameStatus=" + gameStatus +
                '}';
    }
}