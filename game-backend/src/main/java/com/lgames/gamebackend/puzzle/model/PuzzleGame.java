package com.lgames.gamebackend.puzzle.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PuzzleGame {
    private String gameId;
    private int boardSize;
    private int[][] board;
    private GameStatus gameStatus;
    private int score;
    private long startTime;
    private long elapsedTime;
    private int moves;

    public PuzzleGame(int boardSize) {
        this.gameId = UUID.randomUUID().toString();
        this.boardSize = boardSize;
        this.gameStatus = GameStatus.PLAYING;
        this.startTime = System.currentTimeMillis();
        this.elapsedTime = 0;
        this.score = 0;
        this.moves = 0;
        this.board = new int[boardSize][boardSize];
    }

    public enum GameStatus {
        PLAYING, PAUSED, GAME_OVER, SUCCESS
    }
}
