package com.lgames.gamebackend.controller;

import com.lgames.gamebackend.model.*;
import com.lgames.gamebackend.service.GameService;
import com.lgames.gamebackend.service.MoveResult;
import com.lgames.gamebackend.service.MoveValidator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/game")
public class GameController {
    private final GameService gameService;
    private final MoveValidator moveValidator;

    public GameController(GameService gameService, MoveValidator moveValidator) {
        this.gameService = gameService;
        this.moveValidator = moveValidator;
    }

    @PostMapping("/room/create")
    public ResponseEntity<GameRoom> createGameRoom() {
        GameRoom gameRoom = gameService.createGameRoom();
        return ResponseEntity.ok(gameRoom);
    }

    @PostMapping("/room/create-ai")
    public ResponseEntity<GameRoom> createAIGameRoom() {
        GameRoom gameRoom = gameService.createGameRoom();
        // 添加AI玩家
        Player aiPlayer = new Player("AI玩家");
        // 修改AI玩家的ID，以便识别
        try {
            java.lang.reflect.Field idField = Player.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(aiPlayer, "ai-player-" + System.currentTimeMillis());
        } catch (Exception e) {
            e.printStackTrace();
        }
        gameService.joinGameRoom(gameRoom.getId(), aiPlayer);
        return ResponseEntity.ok(gameRoom);
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<GameRoom> getGameRoom(@PathVariable String roomId) {
        GameRoom gameRoom = gameService.getGameRoom(roomId);
        if (gameRoom != null) {
            return ResponseEntity.ok(gameRoom);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/room/{roomId}/join")
    public ResponseEntity<GameRoom> joinGameRoom(@PathVariable String roomId, @RequestBody Player player) {
        boolean joined = gameService.joinGameRoom(roomId, player);
        if (joined) {
            return ResponseEntity.ok(gameService.getGameRoom(roomId));
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/room/{roomId}/leave")
    public ResponseEntity<Void> leaveGameRoom(@PathVariable String roomId, @RequestBody String playerId) {
        boolean left = gameService.leaveGameRoom(roomId, playerId);
        if (left) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/move")
    public ResponseEntity<MoveResult> makeMove(@RequestBody MoveRequest moveRequest) {
        MoveResult result = gameService.makeMove(moveRequest.getRoomId(), moveRequest.getFrom(), moveRequest.getTo());
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PostMapping("/room/{roomId}/restart")
    public ResponseEntity<GameRoom> restartGame(@PathVariable String roomId) {
        GameRoom gameRoom = gameService.restartGame(roomId);
        if (gameRoom != null) {
            return ResponseEntity.ok(gameRoom);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/rooms")
    public ResponseEntity<List<GameRoom>> getActiveGameRooms() {
        List<GameRoom> gameRooms = gameService.getActiveGameRooms();
        return ResponseEntity.ok(gameRooms);
    }

    @PostMapping("/moves")
    public ResponseEntity<List<Move>> getValidMoves(@RequestBody MoveRequest moveRequest) {
        GameRoom gameRoom = gameService.getGameRoom(moveRequest.getRoomId());
        if (gameRoom == null) {
            return ResponseEntity.notFound().build();
        }

        List<Move> validMoves = moveValidator.getValidMovesForPiece(gameRoom, gameRoom.getBoard().getPieceAt(moveRequest.getFrom()));
        return ResponseEntity.ok(validMoves);
    }

    // 请求体类
    public static class MoveRequest {
        private String roomId;
        private Position from;
        private Position to;

        public String getRoomId() {
            return roomId;
        }

        public void setRoomId(String roomId) {
            this.roomId = roomId;
        }

        public Position getFrom() {
            return from;
        }

        public void setFrom(Position from) {
            this.from = from;
        }

        public Position getTo() {
            return to;
        }

        public void setTo(Position to) {
            this.to = to;
        }
    }
}
