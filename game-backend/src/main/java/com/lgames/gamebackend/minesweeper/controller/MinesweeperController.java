package com.lgames.gamebackend.minesweeper.controller;

import com.lgames.gamebackend.minesweeper.model.MinesweeperGame;
import com.lgames.gamebackend.minesweeper.service.MinesweeperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/minesweeper")
public class MinesweeperController {
    private final MinesweeperService minesweeperService;
    private final Map<String, MinesweeperGame> gameStore = new HashMap<>();

    @Autowired
    public MinesweeperController(MinesweeperService minesweeperService) {
        this.minesweeperService = minesweeperService;
    }

    // 创建新游戏
    @PostMapping("/new")
    public MinesweeperGame newGame(@RequestParam(defaultValue = "medium") String difficulty) {
        MinesweeperGame game = minesweeperService.createNewGame(difficulty);
        gameStore.put(game.getGameId(), game);
        return game;
    }

    // 处理玩家点击单元格
    @PostMapping("/{gameId}/reveal")
    public MinesweeperGame revealCell(@PathVariable String gameId, @RequestBody CellPosition position) {
        MinesweeperGame game = gameStore.get(gameId);
        if (game == null) {
            throw new IllegalArgumentException("Game not found");
        }
        MinesweeperGame updatedGame = minesweeperService.revealCell(game, position.getRow(), position.getCol());
        gameStore.put(gameId, updatedGame);
        return updatedGame;
    }

    // 处理玩家标记/取消标记单元格
    @PostMapping("/{gameId}/toggle-flag")
    public MinesweeperGame toggleFlag(@PathVariable String gameId, @RequestBody CellPosition position) {
        MinesweeperGame game = gameStore.get(gameId);
        if (game == null) {
            throw new IllegalArgumentException("Game not found");
        }
        MinesweeperGame updatedGame = minesweeperService.toggleFlag(game, position.getRow(), position.getCol());
        gameStore.put(gameId, updatedGame);
        return updatedGame;
    }

    // 重新开始游戏
    @PostMapping("/{gameId}/restart")
    public MinesweeperGame restartGame(@PathVariable String gameId, @RequestParam(defaultValue = "medium") String difficulty) {
        MinesweeperGame game = gameStore.get(gameId);
        if (game == null) {
            throw new IllegalArgumentException("Game not found");
        }
        MinesweeperGame newGame = minesweeperService.restartGame(game, difficulty);
        gameStore.put(gameId, newGame);
        return newGame;
    }

    // 获取游戏状态
    @GetMapping("/{gameId}")
    public MinesweeperGame getGameStatus(@PathVariable String gameId) {
        MinesweeperGame game = gameStore.get(gameId);
        if (game == null) {
            throw new IllegalArgumentException("Game not found");
        }
        if (game.getGameStatus() == MinesweeperGame.GameStatus.PLAYING) {
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
