package com.lgames.gamebackend.gobang;

import lombok.Data;

@Data
public class GameState {
    private int[][] board;
    private int currentPlayer;
    private String status;
    private int[] lastMove;
    private int[] winningLine;
    
    public GameState() {
        board = new int[15][15];
        currentPlayer = 1; // 1表示黑子（玩家），2表示白子（AI）
        status = "playing"; // playing, player_win, ai_win, draw
        lastMove = null;
        winningLine = null;
    }
}
