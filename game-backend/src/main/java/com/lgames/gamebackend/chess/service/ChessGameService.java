package com.lgames.gamebackend.chess.service;

import com.lgames.gamebackend.chess.entity.ChessGame;
import com.lgames.gamebackend.chess.entity.ChessGameStatus;
import com.lgames.gamebackend.chess.model.ChessMove;
import com.lgames.gamebackend.chess.model.ChessPiece;
import com.lgames.gamebackend.chess.model.ChessPlayer;
import com.lgames.gamebackend.chess.repository.ChessGameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChessGameService {
    private final ChessGameRepository chessGameRepository;
    private final ChessRuleEngine chessRuleEngine;

    public ChessGame createGame() {
        ChessGame game = new ChessGame();
        game.setGameId(UUID.randomUUID().toString());
        game.setCurrentPlayer(ChessPlayer.WHITE);
        game.setStatus(ChessGameStatus.PLAYING);
        game.setBoard(chessRuleEngine.initializeBoard());
        return chessGameRepository.save(game);
    }

    public ChessGame getGame(String gameId) {
        return chessGameRepository.findByGameId(gameId);
    }

    public ChessGame makeMove(String gameId, ChessMove move) {
        ChessGame game = chessGameRepository.findByGameId(gameId);
        if (game == null) {
            throw new IllegalArgumentException("Game not found");
        }

        if (game.getStatus() != ChessGameStatus.PLAYING && game.getStatus() != ChessGameStatus.CREATED) {
            throw new IllegalStateException("Game is not in playing state");
        }

        if (game.getCurrentPlayer() != move.getPlayer()) {
            throw new IllegalStateException("It's not your turn");
        }

        if (!chessRuleEngine.isValidMove(game.getBoard(), move)) {
            throw new IllegalArgumentException("Invalid move");
        }

        chessRuleEngine.makeMove(game.getBoard(), move);
        game.setCurrentPlayer(game.getCurrentPlayer() == ChessPlayer.WHITE ? ChessPlayer.BLACK : ChessPlayer.WHITE);

        // Check game status
        if (chessRuleEngine.isCheckmate(game.getBoard(), game.getCurrentPlayer())) {
            game.setStatus(ChessGameStatus.CHECKMATE);
        } else if (chessRuleEngine.isStalemate(game.getBoard(), game.getCurrentPlayer())) {
            game.setStatus(ChessGameStatus.STALEMATE);
        } else if (chessRuleEngine.isCheck(game.getBoard(), game.getCurrentPlayer())) {
            game.setStatus(ChessGameStatus.CHECK);
        } else {
            game.setStatus(ChessGameStatus.PLAYING);
        }

        return chessGameRepository.save(game);
    }

    public void deleteGame(String gameId) {
        ChessGame game = chessGameRepository.findByGameId(gameId);
        if (game != null) {
            chessGameRepository.delete(game);
        }
    }
}