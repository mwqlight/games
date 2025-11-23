package com.lgames.gamebackend.puzzle.controller;

import com.lgames.gamebackend.puzzle.model.PuzzleGame;
import com.lgames.gamebackend.puzzle.service.PuzzleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/puzzle")
public class PuzzleController {
    private final PuzzleService puzzleService;
    private final Map<String, PuzzleGame> gameStore = new HashMap<>();

    @Autowired
    public PuzzleController(PuzzleService puzzleService) {
        this.puzzleService = puzzleService;
    }

    // 创建新游戏
    @PostMapping("/new")
    public PuzzleGame newGame() {
        PuzzleGame game = puzzleService.createNewGame();
        gameStore.put(game.getGameId(), game);
        return game;
    }

    // 处理玩家移动
    @PostMapping("/{gameId}/move")
    public PuzzleGame makeMove(@PathVariable String gameId, @RequestBody CellPosition position) {
        PuzzleGame game = gameStore.get(gameId);
        if (game == null) {
            throw new IllegalArgumentException("Game not found");
        }
        PuzzleGame updatedGame = puzzleService.makeMove(game, position.getRow(), position.getCol());
        gameStore.put(gameId, updatedGame);
        return updatedGame;
    }

    // 暂停/继续游戏
    @PostMapping("/{gameId}/toggle-pause")
    public PuzzleGame togglePause(@PathVariable String gameId) {
        PuzzleGame game = gameStore.get(gameId);
        if (game == null) {
            throw new IllegalArgumentException("Game not found");
        }
        PuzzleGame updatedGame = puzzleService.togglePause(game);
        gameStore.put(gameId, updatedGame);
        return updatedGame;
    }

    // 重新开始游戏
    @PostMapping("/{gameId}/restart")
    public PuzzleGame restartGame(@PathVariable String gameId) {
        PuzzleGame game = gameStore.get(gameId);
        if (game == null) {
            throw new IllegalArgumentException("Game not found");
        }
        PuzzleGame newGame = puzzleService.restartGame(game);
        gameStore.put(gameId, newGame);
        return newGame;
    }

    // 获取游戏状态
    @GetMapping("/{gameId}")
    public PuzzleGame getGameStatus(@PathVariable String gameId) {
        PuzzleGame game = gameStore.get(gameId);
        if (game == null) {
            throw new IllegalArgumentException("Game not found");
        }
        if (game.getGameStatus() == PuzzleGame.GameStatus.PLAYING) {
            game.setElapsedTime(System.currentTimeMillis() - game.getStartTime());
        }
        return game;
    }

    // 单元格位置类
    private static class CellPosition {
        private int row;
        private int col;

        public int getRow() {
            return row;
        }

        public void setRow(int row) {
            this.row = row;
        }

        public int getCol() {
            return col;
        }

        public void setCol(int col) {
            this.col = col;
        }
    }
}
