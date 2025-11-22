package com.lgames.gamebackend.chess.repository;

import com.lgames.gamebackend.chess.entity.ChessGame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChessGameRepository extends JpaRepository<ChessGame, Long> {
    ChessGame findByGameId(String gameId);
}