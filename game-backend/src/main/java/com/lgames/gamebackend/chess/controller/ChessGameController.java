package com.lgames.gamebackend.chess.controller;

import com.lgames.gamebackend.chess.entity.ChessGame;
import com.lgames.gamebackend.chess.model.ChessMove;
import com.lgames.gamebackend.chess.service.ChessGameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chess")
@RequiredArgsConstructor
public class ChessGameController {
    private final ChessGameService chessGameService;

    @PostMapping("/game")
    public ResponseEntity<ChessGame> createGame() {
        ChessGame game = chessGameService.createGame();
        return ResponseEntity.ok(game);
    }

    @GetMapping("/game/{gameId}")
    public ResponseEntity<ChessGame> getGame(@PathVariable String gameId) {
        ChessGame game = chessGameService.getGame(gameId);
        if (game == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(game);
    }

    @PostMapping("/game/{gameId}/move")
    public ResponseEntity<ChessGame> makeMove(@PathVariable String gameId, @RequestBody ChessMove move) {
        try {
            ChessGame game = chessGameService.makeMove(gameId, move);
            return ResponseEntity.ok(game);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @DeleteMapping("/game/{gameId}")
    public ResponseEntity<Void> deleteGame(@PathVariable String gameId) {
        chessGameService.deleteGame(gameId);
        return ResponseEntity.ok().build();
    }
}