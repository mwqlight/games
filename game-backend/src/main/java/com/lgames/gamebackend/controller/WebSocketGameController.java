package com.lgames.gamebackend.controller;

import com.lgames.gamebackend.model.*;
import com.lgames.gamebackend.service.GameService;
import com.lgames.gamebackend.service.MoveResult;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketGameController {
    private final GameService gameService;

    public WebSocketGameController(GameService gameService) {
        this.gameService = gameService;
    }

    @MessageMapping("/game/{roomId}/join")
    @SendTo("/topic/game/{roomId}")
    public GameState joinGame(@DestinationVariable String roomId, Player player, SimpMessageHeaderAccessor headerAccessor) {
        gameService.joinGameRoom(roomId, player);
        headerAccessor.getSessionAttributes().put("playerId", player.getId());
        headerAccessor.getSessionAttributes().put("roomId", roomId);
        return getGameState(roomId);
    }

    @MessageMapping("/game/{roomId}/move")
    @SendTo("/topic/game/{roomId}")
    public GameState makeMove(@DestinationVariable String roomId, MoveRequest moveRequest) {
        MoveResult result = gameService.makeMove(roomId, moveRequest.getFrom(), moveRequest.getTo());
        return getGameState(roomId);
    }

    @MessageMapping("/game/{roomId}/restart")
    @SendTo("/topic/game/{roomId}")
    public GameState restartGame(@DestinationVariable String roomId) {
        gameService.restartGame(roomId);
        return getGameState(roomId);
    }

    @MessageMapping("/game/{roomId}/leave")
    @SendTo("/topic/game/{roomId}")
    public GameState leaveGame(@DestinationVariable String roomId, SimpMessageHeaderAccessor headerAccessor) {
        String playerId = (String) headerAccessor.getSessionAttributes().get("playerId");
        if (playerId != null) {
            gameService.leaveGameRoom(roomId, playerId);
        }
        return getGameState(roomId);
    }

    private GameState getGameState(String roomId) {
        GameRoom gameRoom = gameService.getGameRoom(roomId);
        if (gameRoom != null) {
            return new GameState(gameRoom);
        }
        return null;
    }

    // WebSocket消息类
    public static class GameState {
        private String roomId;
        private Piece[][] boardState;
        private PlayerColor currentPlayer;
        private GameStatus gameStatus;
        private Move lastMove;
        private int redPieceCount;
        private int blackPieceCount;

        public GameState(GameRoom gameRoom) {
            this.roomId = gameRoom.getId();
            this.boardState = gameRoom.getBoard().getGrid();
            this.currentPlayer = gameRoom.getCurrentTurn();
            this.gameStatus = gameRoom.getGameStatus();
            this.lastMove = gameRoom.getLastMove();
            this.redPieceCount = gameRoom.getBoard().getPiecesByColor(PlayerColor.RED).size();
            this.blackPieceCount = gameRoom.getBoard().getPiecesByColor(PlayerColor.BLACK).size();
        }

        // getter和setter方法
        public String getRoomId() { return roomId; }
        public void setRoomId(String roomId) { this.roomId = roomId; }
        public Piece[][] getBoardState() { return boardState; }
        public void setBoardState(Piece[][] boardState) { this.boardState = boardState; }
        public PlayerColor getCurrentPlayer() { return currentPlayer; }
        public void setCurrentPlayer(PlayerColor currentPlayer) { this.currentPlayer = currentPlayer; }
        public GameStatus getGameStatus() { return gameStatus; }
        public void setGameStatus(GameStatus gameStatus) { this.gameStatus = gameStatus; }
        public Move getLastMove() { return lastMove; }
        public void setLastMove(Move lastMove) { this.lastMove = lastMove; }
        public int getRedPieceCount() { return redPieceCount; }
        public void setRedPieceCount(int redPieceCount) { this.redPieceCount = redPieceCount; }
        public int getBlackPieceCount() { return blackPieceCount; }
        public void setBlackPieceCount(int blackPieceCount) { this.blackPieceCount = blackPieceCount; }
    }

    public static class MoveRequest {
        private Position from;
        private Position to;

        public Position getFrom() { return from; }
        public void setFrom(Position from) { this.from = from; }
        public Position getTo() { return to; }
        public void setTo(Position to) { this.to = to; }
    }
}
