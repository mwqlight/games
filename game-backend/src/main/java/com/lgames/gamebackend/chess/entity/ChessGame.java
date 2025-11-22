package com.lgames.gamebackend.chess.entity;

import com.lgames.gamebackend.chess.model.ChessPiece;
import com.lgames.gamebackend.chess.model.ChessPlayer;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "chess_game")
public class ChessGame {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String gameId;

    @Enumerated(EnumType.STRING)
    private ChessPlayer currentPlayer;

    @Enumerated(EnumType.STRING)
    private ChessGameStatus status;

    @ElementCollection
    @CollectionTable(name = "chess_board", joinColumns = @JoinColumn(name = "game_id"))
    private List<ChessPiece> board;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public ChessPlayer getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(ChessPlayer currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public ChessGameStatus getStatus() {
        return status;
    }

    public void setStatus(ChessGameStatus status) {
        this.status = status;
    }

    public List<ChessPiece> getBoard() {
        return board;
    }

    public void setBoard(List<ChessPiece> board) {
        this.board = board;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}