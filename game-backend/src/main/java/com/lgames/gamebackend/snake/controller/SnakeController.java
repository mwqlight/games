package com.lgames.gamebackend.snake.controller;

import com.lgames.gamebackend.snake.model.SnakeGame;
import com.lgames.gamebackend.snake.model.SnakeGame.Direction;
import com.lgames.gamebackend.snake.service.SnakeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/snake")
public class SnakeController {
    private final SnakeService snakeService;
    private final Map<String, SnakeGame> gameStore = new HashMap<>();

    @Autowired
    public SnakeController(SnakeService snakeService) {
        this.snakeService = snakeService;
    }

    // 创建新游戏
    @PostMapping("/new")
    public SnakeGame newGame() {
        SnakeGame game = snakeService.createNewGame();
        gameStore.put(game.getGameId(), game);
        System.out.println("Created new game with ID: " + game.getGameId());
        return game;
    }

    // 处理玩家移动
    @PostMapping("/{gameId}/move")
    public SnakeGame makeMove(@PathVariable String gameId, @RequestBody Direction direction) {
        SnakeGame game = gameStore.get(gameId);
        if (game == null) {
            throw new IllegalArgumentException("Game not found");
        }
        SnakeGame updatedGame = snakeService.makeMove(game, direction);
        gameStore.put(gameId, updatedGame);
        return updatedGame;
    }

    // 暂停/继续游戏
    @PostMapping("/{gameId}/toggle-pause")
    public SnakeGame togglePause(@PathVariable String gameId) {
        SnakeGame game = gameStore.get(gameId);
        if (game == null) {
            throw new IllegalArgumentException("Game not found");
        }
        SnakeGame updatedGame = snakeService.togglePause(game);
        gameStore.put(gameId, updatedGame);
        return updatedGame;
    }

    // 重新开始游戏
    @PostMapping("/{gameId}/restart")
    public SnakeGame restartGame(@PathVariable String gameId) {
        SnakeGame game = gameStore.get(gameId);
        if (game == null) {
            throw new IllegalArgumentException("Game not found");
        }
        SnakeGame newGame = snakeService.restartGame(game);
        gameStore.put(gameId, newGame);
        return newGame;
    }

    // 获取游戏状态
    @GetMapping("/{gameId}")
    public SnakeGame getGameStatus(@PathVariable String gameId) {
        SnakeGame game = gameStore.get(gameId);
        if (game == null) {
            throw new IllegalArgumentException("Game not found");
        }
        if (game.getGameStatus() == SnakeGame.GameStatus.PLAYING) {
            game.setElapsedTime(System.currentTimeMillis() - game.getStartTime());
        }
        return game;
    }
}
