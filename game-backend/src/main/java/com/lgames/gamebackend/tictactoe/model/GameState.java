package com.lgames.gamebackend.tictactoe.model;

import lombok.Data;

@Data
public class GameState {
    private String gameId;
    private String[][] board;
    private String currentPlayer;
    private GameStatus gameStatus;
    private int[][] winningLine;

    public enum GameStatus {
        PLAYING, X_WON, O_WON, DRAW
    }
}
