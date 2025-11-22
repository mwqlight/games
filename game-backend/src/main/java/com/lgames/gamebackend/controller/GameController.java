package com.lgames.gamebackend.controller;

import com.lgames.gamebackend.model.GameState;
import com.lgames.gamebackend.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/game")
public class GameController {
    @Autowired
    private GameService gameService;

    @PostMapping("/start")
    public ResponseEntity<GameState> startNewGame() {
        GameState gameState = gameService.startNewGame();
        return ResponseEntity.ok(gameState);
    }

    @PostMapping("/player/draw")
    public ResponseEntity<GameState> playerDraw() {
        try {
            GameState gameState = gameService.playerDraw();
            return ResponseEntity.ok(gameState);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/player/draw/{index}")
    public ResponseEntity<GameState> playerDrawFromAI(@PathVariable int index) {
        try {
            GameState gameState = gameService.playerDrawFromAI(index);
            return ResponseEntity.ok(gameState);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/state")
    public ResponseEntity<GameState> getGameState() {
        try {
            GameState gameState = gameService.getGameState();
            return ResponseEntity.ok(gameState);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}
