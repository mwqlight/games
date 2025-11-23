package com.lgames.gamebackend.tetris.controller;

import com.lgames.gamebackend.tetris.model.GameState;
import com.lgames.gamebackend.tetris.service.TetrisGameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/tetris")
public class TetrisGameController {
    private final TetrisGameService tetrisGameService;
    private final Map<String, GameState> gameStates = new HashMap<>();

    @Autowired
    public TetrisGameController(TetrisGameService tetrisGameService) {
        this.tetrisGameService = tetrisGameService;
    }

    @PostMapping("/start")
    public GameState startNewGame() {
        GameState gameState = tetrisGameService.startNewGame();
        gameStates.put(gameState.getGameId(), gameState);
        return gameState;
    }

    @PostMapping("/move/{gameId}")
    public GameState movePiece(@PathVariable String gameId, @RequestBody MoveRequest moveRequest) {
        GameState gameState = gameStates.get(gameId);
        if (gameState == null) {
            throw new IllegalArgumentException("Game not found with id: " + gameId);
        }
        GameState updatedState = tetrisGameService.movePiece(gameState, moveRequest.getAction());
        gameStates.put(gameId, updatedState);
        return updatedState;
    }

    @GetMapping("/state/{gameId}")
    public GameState getGameState(@PathVariable String gameId) {
        GameState gameState = gameStates.get(gameId);
        if (gameState == null) {
            throw new IllegalArgumentException("Game not found with id: " + gameId);
        }
        return gameState;
    }

    @PostMapping("/pause/{gameId}")
    public GameState pauseGame(@PathVariable String gameId) {
        GameState gameState = gameStates.get(gameId);
        if (gameState == null) {
            throw new IllegalArgumentException("Game not found with id: " + gameId);
        }
        if (gameState.getGameStatus() == GameState.GameStatus.PLAYING) {
            gameState.setGameStatus(GameState.GameStatus.PAUSED);
        }
        return gameState;
    }

    @PostMapping("/resume/{gameId}")
    public GameState resumeGame(@PathVariable String gameId) {
        GameState gameState = gameStates.get(gameId);
        if (gameState == null) {
            throw new IllegalArgumentException("Game not found with id: " + gameId);
        }
        if (gameState.getGameStatus() == GameState.GameStatus.PAUSED) {
            gameState.setGameStatus(GameState.GameStatus.PLAYING);
        }
        return gameState;
    }

    @PostMapping("/restart/{gameId}")
    public GameState restartGame(@PathVariable String gameId) {
        GameState gameState = tetrisGameService.startNewGame();
        gameStates.put(gameId, gameState);
        return gameState;
    }

    // 内部类用于接收移动请求
    public static class MoveRequest {
        private String action;

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }
    }
}