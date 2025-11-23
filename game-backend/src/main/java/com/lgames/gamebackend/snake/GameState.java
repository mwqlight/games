package com.lgames.gamebackend.snake;

import lombok.Data;

import java.util.List;

@Data
public class GameState {
    private String gameId;
    private List<Position> snakeBody;
    private Position foodPosition;
    private Direction currentDirection;
    private int score;
    private GameStatus gameStatus;
}
