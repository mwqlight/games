package com.lgames.gamebackend.snake;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/game")
public class GameController {
    @Autowired
    private GameService gameService;

    @PostMapping("/start")
    public ResponseEntity<GameState> startGame() {
        GameState gameState = gameService.startGame();
        return ResponseEntity.ok(gameState);
    }

    @PostMapping("/move")
    public ResponseEntity<GameState> move(@RequestBody MoveRequest moveRequest) {
        try {
            GameState gameState = gameService.move(moveRequest.getGameId(), moveRequest.getDirection());
            return ResponseEntity.ok(gameState);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/{gameId}/state")
    public ResponseEntity<GameState> getGameState(@PathVariable String gameId) {
        try {
            GameState gameState = gameService.getGameState(gameId);
            return ResponseEntity.ok(gameState);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
