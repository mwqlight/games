package com.lgames.gamebackend.connectfour;

public class GameState {
    private String gameId;
    private int[][] board;
    private int currentPlayer;
    private GameStatus gameStatus;
    private int lastMoveColumn;
    private int lastMoveRow;

    public GameState() {
        // 初始化6行7列的空棋盘
        this.board = new int[6][7];
        this.currentPlayer = 1; // 1为玩家，2为AI
        this.gameStatus = GameStatus.PLAYING;
    }

    // getter和setter方法
    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public int[][] getBoard() {
        return board;
    }

    public void setBoard(int[][] board) {
        this.board = board;
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(int currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }

    public int getLastMoveColumn() {
        return lastMoveColumn;
    }

    public void setLastMoveColumn(int lastMoveColumn) {
        this.lastMoveColumn = lastMoveColumn;
    }

    public int getLastMoveRow() {
        return lastMoveRow;
    }

    public void setLastMoveRow(int lastMoveRow) {
        this.lastMoveRow = lastMoveRow;
    }

    // 游戏状态枚举
    public enum GameStatus {
        PLAYING, PLAYER_WON, AI_WON, DRAW
    }
}
