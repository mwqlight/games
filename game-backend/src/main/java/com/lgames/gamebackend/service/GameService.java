package com.lgames.gamebackend.service;

import com.lgames.gamebackend.model.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameService {
    private final Map<String, GameRoom> gameRooms = new ConcurrentHashMap<>();
    private final MoveValidator moveValidator;
    private final AIService aiService;

    public GameService(MoveValidator moveValidator, AIService aiService) {
        this.moveValidator = moveValidator;
        this.aiService = aiService;
    }

    public GameRoom createGameRoom() {
        GameRoom gameRoom = new GameRoom();
        gameRooms.put(gameRoom.getId(), gameRoom);
        return gameRoom;
    }

    public GameRoom getGameRoom(String roomId) {
        return gameRooms.get(roomId);
    }

    public boolean joinGameRoom(String roomId, Player player) {
        GameRoom gameRoom = gameRooms.get(roomId);
        if (gameRoom != null && !gameRoom.isFull()) {
            boolean added = gameRoom.addPlayer(player);
            if (added) {
                // 分配颜色
                if (gameRoom.getPlayers().size() == 1) {
                    player.setColor(PlayerColor.RED);
                } else {
                    player.setColor(PlayerColor.BLACK);
                }
            }
            return added;
        }
        return false;
    }

    public boolean leaveGameRoom(String roomId, String playerId) {
        GameRoom gameRoom = gameRooms.get(roomId);
        if (gameRoom != null) {
            boolean removed = gameRoom.removePlayer(playerId);
            if (removed && gameRoom.getPlayers().isEmpty()) {
                gameRooms.remove(roomId);
            }
            return removed;
        }
        return false;
    }

    public MoveResult makeMove(String roomId, Position from, Position to) {
        GameRoom gameRoom = gameRooms.get(roomId);
        if (gameRoom == null || gameRoom.getGameStatus() != GameStatus.PLAYING) {
            return new MoveResult(false, "游戏房间不存在或游戏未开始", null);
        }

        if (!moveValidator.isValidMove(gameRoom, from, to)) {
            return new MoveResult(false, "无效的移动", null);
        }

        Board board = gameRoom.getBoard();
        Piece piece = board.getPieceAt(from);
        List<Position> capturedPositions = new ArrayList<>();

        // 处理吃子
        if (from.distanceTo(to) == 2) {
            Position middlePosition = from.middle(to);
            board.removePiece(middlePosition);
            capturedPositions.add(middlePosition);
        }

        // 移动棋子
        board.movePiece(from, to);

        // 检查是否升级为王
        boolean isKingPromotion = false;
        if (piece.shouldPromote()) {
            piece.promoteToKing();
            isKingPromotion = true;
        }

        // 创建移动记录
        Move move = new Move(from, to, capturedPositions);
        move.setKingPromotion(isKingPromotion);
        gameRoom.setLastMove(move);

        // 检查游戏是否结束
        GameStatus newGameStatus = checkGameEnd(gameRoom);
        gameRoom.setGameStatus(newGameStatus);

        // 如果游戏未结束，切换回合
        if (newGameStatus == GameStatus.PLAYING) {
            // 检查是否有连续吃子
            List<Position> nextCaptureMoves = moveValidator.getValidMoves(gameRoom, to)
                    .stream()
                    .filter(pos -> to.distanceTo(pos) == 2)
                    .toList();

            // 如果有连续吃子，当前玩家继续回合
            if (nextCaptureMoves.isEmpty()) {
                gameRoom.switchTurn();
                
                // 如果是人机对战，让AI进行移动
                if (aiService.isAIMoveNeeded(gameRoom)) {
                    Move aiMove = aiService.makeAIMove(gameRoom);
                    if (aiMove != null) {
                        makeMove(roomId, aiMove.getFrom(), aiMove.getTo());
                    }
                }
            }
        }

        return new MoveResult(true, "移动成功", move);
    }

    private GameStatus checkGameEnd(GameRoom gameRoom) {
        Board board = gameRoom.getBoard();
        PlayerColor currentTurn = gameRoom.getCurrentTurn();
        PlayerColor opponentColor = currentTurn == PlayerColor.RED ? PlayerColor.BLACK : PlayerColor.RED;

        // 检查对方是否还有棋子
        List<Piece> opponentPieces = board.getPiecesByColor(opponentColor);
        if (opponentPieces.isEmpty()) {
            return GameStatus.FINISHED;
        }

        // 检查对方是否还有有效移动
        if (!moveValidator.hasValidMoves(gameRoom)) {
            return GameStatus.FINISHED;
        }

        return GameStatus.PLAYING;
    }

    public GameRoom restartGame(String roomId) {
        GameRoom gameRoom = gameRooms.get(roomId);
        if (gameRoom != null) {
            gameRoom.setBoard(new Board());
            gameRoom.setCurrentTurn(PlayerColor.RED);
            gameRoom.setGameStatus(GameStatus.PLAYING);
            gameRoom.setLastMove(null);
        }
        return gameRoom;
    }

    public List<GameRoom> getActiveGameRooms() {
        return gameRooms.values().stream()
                .filter(room -> room.getGameStatus() != GameStatus.FINISHED)
                .toList();
    }
}
