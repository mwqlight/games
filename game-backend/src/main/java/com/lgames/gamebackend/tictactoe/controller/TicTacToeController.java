package com.lgames.gamebackend.tictactoe.controller;

import com.lgames.gamebackend.tictactoe.model.GameState;
import com.lgames.gamebackend.tictactoe.model.MoveRequest;
import com.lgames.gamebackend.tictactoe.service.TicTacToeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/game")
@CrossOrigin(origins = "*")
public class TicTacToeController {
    @Autowired
    private TicTacToeService ticTacToeService;

    @PostMapping("/start")
    public ResponseEntity<GameState> startNewGame() {
        GameState gameState = ticTacToeService.startNewGame();
        return ResponseEntity.ok(gameState);
    }

    @PostMapping("/move")
    public ResponseEntity<GameState> makeMove(@RequestBody MoveRequest moveRequest) {
        try {
            GameState gameState = ticTacToeService.makeMove(moveRequest);
            return ResponseEntity.ok(gameState);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/{gameId}/state")
    public ResponseEntity<GameState> getGameState(@PathVariable String gameId) {
        try {
            GameState gameState = ticTacToeService.getGameState(gameId);
            return ResponseEntity.ok(gameState);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
