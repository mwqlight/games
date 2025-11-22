package com.lgames.gamebackend.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GameRoom {
    private String id;
    private List<Player> players;
    private Board board;
    private PlayerColor currentTurn;
    private GameStatus gameStatus;
    private Move lastMove;

    public GameRoom() {
        this.id = UUID.randomUUID().toString();
        this.players = new ArrayList<>();
        this.board = new Board();
        this.currentTurn = PlayerColor.RED;
        this.gameStatus = GameStatus.WAITING_FOR_PLAYERS;
    }

    public boolean addPlayer(Player player) {
        if (players.size() < 2) {
            players.add(player);
            if (players.size() == 2) {
                gameStatus = GameStatus.PLAYING;
            }
            return true;
        }
        return false;
    }

    public boolean removePlayer(String playerId) {
        Player playerToRemove = players.stream()
                .filter(player -> player.getId().equals(playerId))
                .findFirst()
                .orElse(null);
        
        if (playerToRemove != null) {
            players.remove(playerToRemove);
            gameStatus = GameStatus.WAITING_FOR_PLAYERS;
            return true;
        }
        return false;
    }

    public String getId() {
        return id;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public PlayerColor getCurrentTurn() {
        return currentTurn;
    }

    public void setCurrentTurn(PlayerColor currentTurn) {
        this.currentTurn = currentTurn;
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public Move getLastMove() {
        return lastMove;
    }

    public void setLastMove(Move lastMove) {
        this.lastMove = lastMove;
    }

    public void switchTurn() {
        currentTurn = currentTurn == PlayerColor.RED ? PlayerColor.BLACK : PlayerColor.RED;
    }

    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }

    public boolean isFull() {
        return players.size() == 2;
    }
}
