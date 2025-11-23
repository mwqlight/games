package com.lgames.gamebackend.snake.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SnakeGame {
    private String gameId;
    private int boardSize;
    private List<Position> snake;
    private Position food;
    private Direction direction;
    private GameStatus gameStatus;
    private int score;
    private long startTime;
    private long elapsedTime;
    private int speed;

    public SnakeGame(int boardSize) {
        this.gameId = UUID.randomUUID().toString();
        this.boardSize = boardSize;
        this.gameStatus = GameStatus.PLAYING;
        this.startTime = System.currentTimeMillis();
        this.elapsedTime = 0;
        this.score = 0;
        this.speed = 150;
    }

    public enum GameStatus {
        PLAYING, PAUSED, GAME_OVER, SUCCESS
    }

    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Position {
        private int row;
        private int col;
    }
}
