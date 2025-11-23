package com.lgames.gamebackend.minesweeper.controller;

import com.lgames.gamebackend.minesweeper.enums.Difficulty;
import com.lgames.gamebackend.minesweeper.model.CellRequest;
import com.lgames.gamebackend.minesweeper.model.GameState;
import com.lgames.gamebackend.minesweeper.service.MinesweeperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/game/minesweeper")
@CrossOrigin(origins = "http://localhost:3000")
public class MinesweeperController {
    @Autowired
    private MinesweeperService minesweeperService;

    @PostMapping("/new")
    public ResponseEntity<GameState> createNewGame(@RequestBody Map<String, String> request) {
        try {
            String difficultyStr = request.getOrDefault("difficulty", "BEGINNER");
            Difficulty diff = Difficulty.valueOf(difficultyStr.toUpperCase());
            GameState gameState = minesweeperService.createNewGame(diff);
            return ResponseEntity.ok(gameState);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{gameId}/reveal")
    public ResponseEntity<GameState> revealCell(@PathVariable String gameId, @RequestBody CellRequest cellRequest) {
        GameState gameState = minesweeperService.revealCell(gameId, cellRequest.getX(), cellRequest.getY());
        if (gameState == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(gameState);
    }

    @PostMapping("/{gameId}/flag")
    public ResponseEntity<GameState> flagCell(@PathVariable String gameId, @RequestBody CellRequest cellRequest) {
        GameState gameState = minesweeperService.flagCell(gameId, cellRequest.getX(), cellRequest.getY());
        if (gameState == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(gameState);
    }

    @GetMapping("/{gameId}/state")
    public ResponseEntity<GameState> getGameState(@PathVariable String gameId) {
        GameState gameState = minesweeperService.getGameState(gameId);
        if (gameState == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(gameState);
    }
}
