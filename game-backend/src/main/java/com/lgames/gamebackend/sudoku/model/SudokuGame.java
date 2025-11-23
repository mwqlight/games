package com.lgames.gamebackend.sudoku.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SudokuGame {
    private String gameId;
    private int[][] puzzle;
    private int[][] userBoard;
    private int[][] solution;
    private GameStatus gameStatus;
    private Difficulty difficulty;
    private long startTime;
    private long elapsedTime;
    private int hintCount;
    private int errorCount;

    public enum GameStatus {
        PLAYING, SUCCESS, FAILED
    }

    public enum Difficulty {
        EASY, MEDIUM, HARD
    }
}
