package com.lgames.gamebackend.connectfour;

public class MoveRequest {
    private String gameId;
    private int column;

    // getter和setter方法
    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }
}
