package com.lgames.gamebackend.sudoku.controller;

import com.lgames.gamebackend.sudoku.model.SudokuGame;
import com.lgames.gamebackend.sudoku.model.Move;
import com.lgames.gamebackend.sudoku.service.SudokuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/sudoku")
public class SudokuController {
    private final SudokuService sudokuService;
    private final Map<String, SudokuGame> gameStore = new HashMap<>();

    @Autowired
    public SudokuController(SudokuService sudokuService) {
        this.sudokuService = sudokuService;
    }

    // 生成新游戏
    @GetMapping("/new")
    public SudokuGame newGame(@RequestParam(defaultValue = "MEDIUM") SudokuGame.Difficulty difficulty) {
        SudokuGame game = sudokuService.generateNewGame(difficulty);
        gameStore.put(game.getGameId(), game);
        return game;
    }

    // 处理玩家移动
    @PostMapping("/{gameId}/move")
    public SudokuGame makeMove(@PathVariable String gameId, @RequestBody Move move) {
        SudokuGame game = gameStore.get(gameId);
        if (game == null) {
            throw new IllegalArgumentException("Game not found");
        }
        if (game.getGameStatus() != SudokuGame.GameStatus.PLAYING) {
            return game;
        }
        return sudokuService.makeMove(game, move);
    }

    // 验证解答
    @GetMapping("/{gameId}/validate")
    public Map<String, Boolean> validateSolution(@PathVariable String gameId) {
        SudokuGame game = gameStore.get(gameId);
        if (game == null) {
            throw new IllegalArgumentException("Game not found");
        }
        boolean isValid = sudokuService.validateSolution(game);
        Map<String, Boolean> response = new HashMap<>();
        response.put("valid", isValid);
        return response;
    }

    // 获取提示
    @GetMapping("/{gameId}/hint")
    public SudokuGame getHint(@PathVariable String gameId) {
        SudokuGame game = gameStore.get(gameId);
        if (game == null) {
            throw new IllegalArgumentException("Game not found");
        }
        if (game.getGameStatus() != SudokuGame.GameStatus.PLAYING) {
            return game;
        }
        return sudokuService.getHint(game);
    }

    // 获取游戏状态
    @GetMapping("/{gameId}")
    public SudokuGame getGameStatus(@PathVariable String gameId) {
        SudokuGame game = gameStore.get(gameId);
        if (game == null) {
            throw new IllegalArgumentException("Game not found");
        }
        if (game.getGameStatus() == SudokuGame.GameStatus.PLAYING) {
            game.setElapsedTime(System.currentTimeMillis() - game.getStartTime());
        }
        return game;
    }
}
