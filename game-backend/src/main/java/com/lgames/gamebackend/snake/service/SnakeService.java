package com.lgames.gamebackend.snake.service;

import com.lgames.gamebackend.snake.model.SnakeGame;
import com.lgames.gamebackend.snake.model.SnakeGame.Direction;
import com.lgames.gamebackend.snake.model.SnakeGame.Position;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class SnakeService {
    private static final int DEFAULT_BOARD_SIZE = 20;
    private final Random random = new Random();

    // 创建新游戏
    public SnakeGame createNewGame() {
        SnakeGame game = new SnakeGame(DEFAULT_BOARD_SIZE);
        
        // 初始化贪吃蛇位置
        List<Position> initialSnake = new ArrayList<>();
        initialSnake.add(new Position(10, 10));
        initialSnake.add(new Position(10, 9));
        initialSnake.add(new Position(10, 8));
        game.setSnake(initialSnake);
        
        // 初始化方向
        game.setDirection(Direction.RIGHT);
        
        // 生成食物
        generateFood(game);
        
        return game;
    }

    // 处理玩家移动
    public SnakeGame makeMove(SnakeGame game, Direction direction) {
        if (game.getGameStatus() != SnakeGame.GameStatus.PLAYING) {
            return game;
        }

        // 更新方向（不能直接掉头）
        if (!isOppositeDirection(game.getDirection(), direction)) {
            game.setDirection(direction);
        }

        // 移动贪吃蛇
        Position head = game.getSnake().get(0);
        Position newHead = getNewHeadPosition(head, game.getDirection());

        // 检查碰撞
        if (isCollision(game, newHead)) {
            game.setGameStatus(SnakeGame.GameStatus.GAME_OVER);
            game.setElapsedTime(System.currentTimeMillis() - game.getStartTime());
            return game;
        }

        // 添加新头部
        game.getSnake().add(0, newHead);

        // 检查是否吃到食物
        if (newHead.equals(game.getFood())) {
            game.setScore(game.getScore() + 10);
            generateFood(game);
            
            // 每吃5个食物增加速度
            if (game.getScore() % 50 == 0 && game.getSpeed() > 50) {
                game.setSpeed(game.getSpeed() - 10);
            }
        } else {
            // 移除尾部
            game.getSnake().remove(game.getSnake().size() - 1);
        }

        // 检查游戏是否完成（贪吃蛇填满整个棋盘）
        if (game.getSnake().size() == game.getBoardSize() * game.getBoardSize()) {
            game.setGameStatus(SnakeGame.GameStatus.SUCCESS);
            game.setElapsedTime(System.currentTimeMillis() - game.getStartTime());
        }

        return game;
    }

    // 暂停/继续游戏
    public SnakeGame togglePause(SnakeGame game) {
        if (game.getGameStatus() == SnakeGame.GameStatus.PLAYING) {
            game.setGameStatus(SnakeGame.GameStatus.PAUSED);
            game.setElapsedTime(System.currentTimeMillis() - game.getStartTime());
        } else if (game.getGameStatus() == SnakeGame.GameStatus.PAUSED) {
            game.setGameStatus(SnakeGame.GameStatus.PLAYING);
            game.setStartTime(System.currentTimeMillis() - game.getElapsedTime());
        }
        return game;
    }

    // 重新开始游戏
    public SnakeGame restartGame(SnakeGame game) {
        return createNewGame();
    }

    // 生成食物
    private void generateFood(SnakeGame game) {
        Position food;
        do {
            food = new Position(
                random.nextInt(game.getBoardSize()),
                random.nextInt(game.getBoardSize())
            );
        } while (game.getSnake().contains(food));
        game.setFood(food);
    }

    // 获取新头部位置
    private Position getNewHeadPosition(Position head, Direction direction) {
        return switch (direction) {
            case UP -> new Position(head.getRow() - 1, head.getCol());
            case DOWN -> new Position(head.getRow() + 1, head.getCol());
            case LEFT -> new Position(head.getRow(), head.getCol() - 1);
            case RIGHT -> new Position(head.getRow(), head.getCol() + 1);
        };
    }

    // 检查是否碰撞
    private boolean isCollision(SnakeGame game, Position position) {
        // 检查边界碰撞
        if (position.getRow() < 0 || position.getRow() >= game.getBoardSize() ||
            position.getCol() < 0 || position.getCol() >= game.getBoardSize()) {
            return true;
        }

        // 检查自身碰撞
        return game.getSnake().contains(position);
    }

    // 检查是否是相反方向
    private boolean isOppositeDirection(Direction current, Direction newDirection) {
        return (current == Direction.UP && newDirection == Direction.DOWN) ||
               (current == Direction.DOWN && newDirection == Direction.UP) ||
               (current == Direction.LEFT && newDirection == Direction.RIGHT) ||
               (current == Direction.RIGHT && newDirection == Direction.LEFT);
    }
}
