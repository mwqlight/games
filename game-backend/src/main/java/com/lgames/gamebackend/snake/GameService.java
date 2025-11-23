package com.lgames.gamebackend.snake;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameService {
    private final Map<String, GameState> games = new ConcurrentHashMap<>();
    private final Random random = new Random();
    private static final int BOARD_SIZE = 20;

    public GameState startGame() {
        String gameId = UUID.randomUUID().toString();
        List<Position> initialSnake = Arrays.asList(
                new Position(10, 10),
                new Position(10, 9),
                new Position(10, 8)
        );

        GameState gameState = new GameState();
        gameState.setGameId(gameId);
        gameState.setSnakeBody(initialSnake);
        gameState.setFoodPosition(generateFood(initialSnake));
        gameState.setCurrentDirection(Direction.RIGHT);
        gameState.setScore(0);
        gameState.setGameStatus(GameStatus.PLAYING);

        games.put(gameId, gameState);
        return gameState;
    }

    public GameState move(String gameId, Direction direction) {
        GameState gameState = games.get(gameId);
        if (gameState == null || gameState.getGameStatus() == GameStatus.GAME_OVER) {
            throw new IllegalArgumentException("Game not found or already over");
        }

        // 检查是否反向移动
        if (isOppositeDirection(gameState.getCurrentDirection(), direction)) {
            direction = gameState.getCurrentDirection();
        }

        gameState.setCurrentDirection(direction);
        List<Position> snakeBody = new ArrayList<>(gameState.getSnakeBody());
        Position head = snakeBody.get(0);
        Position newHead = calculateNewHead(head, direction);

        // 碰撞检测
        if (isCollision(newHead, snakeBody)) {
            gameState.setGameStatus(GameStatus.GAME_OVER);
            return gameState;
        }

        // 添加新头部
        snakeBody.add(0, newHead);

        // 检查是否吃到食物
        if (newHead.equals(gameState.getFoodPosition())) {
            gameState.setScore(gameState.getScore() + 10);
            gameState.setFoodPosition(generateFood(snakeBody));
        } else {
            // 移除尾部
            snakeBody.remove(snakeBody.size() - 1);
        }

        gameState.setSnakeBody(snakeBody);
        return gameState;
    }

    public GameState getGameState(String gameId) {
        GameState gameState = games.get(gameId);
        if (gameState == null) {
            throw new IllegalArgumentException("Game not found");
        }
        return gameState;
    }

    private Position calculateNewHead(Position head, Direction direction) {
        switch (direction) {
            case UP:
                return new Position(head.getX(), head.getY() - 1);
            case DOWN:
                return new Position(head.getX(), head.getY() + 1);
            case LEFT:
                return new Position(head.getX() - 1, head.getY());
            case RIGHT:
                return new Position(head.getX() + 1, head.getY());
            default:
                return head;
        }
    }

    private boolean isCollision(Position head, List<Position> snakeBody) {
        // 检查边界碰撞
        if (head.getX() < 0 || head.getX() >= BOARD_SIZE || head.getY() < 0 || head.getY() >= BOARD_SIZE) {
            return true;
        }

        // 检查自身碰撞
        for (Position segment : snakeBody) {
            if (head.equals(segment)) {
                return true;
            }
        }

        return false;
    }

    private Position generateFood(List<Position> snakeBody) {
        Position food;
        do {
            food = new Position(random.nextInt(BOARD_SIZE), random.nextInt(BOARD_SIZE));
        } while (snakeBody.contains(food));
        return food;
    }

    private boolean isOppositeDirection(Direction current, Direction newDirection) {
        return (current == Direction.UP && newDirection == Direction.DOWN) ||
                (current == Direction.DOWN && newDirection == Direction.UP) ||
                (current == Direction.LEFT && newDirection == Direction.RIGHT) ||
                (current == Direction.RIGHT && newDirection == Direction.LEFT);
    }
}
