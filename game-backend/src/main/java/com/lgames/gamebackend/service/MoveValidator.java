package com.lgames.gamebackend.service;

import com.lgames.gamebackend.model.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MoveValidator {

    public boolean isValidMove(GameRoom gameRoom, Position from, Position to) {
        Board board = gameRoom.getBoard();
        Piece piece = board.getPieceAt(from);
        PlayerColor currentTurn = gameRoom.getCurrentTurn();

        // 检查起始位置是否有棋子
        if (piece == null) {
            return false;
        }

        // 检查是否是当前玩家的回合
        if (piece.getColor() != currentTurn) {
            return false;
        }

        // 检查目标位置是否合法
        if (!board.isValidPosition(to)) {
            return false;
        }

        // 检查目标位置是否为空
        if (board.getPieceAt(to) != null) {
            return false;
        }

        // 检查是否是对角线移动
        if (!from.isDiagonalTo(to)) {
            return false;
        }

        int distance = from.distanceTo(to);

        // 检查移动距离是否合法
        if (distance != 1 && distance != 2) {
            return false;
        }

        // 检查普通移动（距离为1）
        if (distance == 1) {
            return isValidNormalMove(piece, from, to);
        }

        // 检查吃子移动（距离为2）
        if (distance == 2) {
            return isValidCaptureMove(board, piece, from, to);
        }

        return false;
    }

    private boolean isValidNormalMove(Piece piece, Position from, Position to) {
        int rowDirection = to.getRow() - from.getRow();

        // 普通棋子只能向前移动
        if (!piece.isKing()) {
            if (piece.getColor() == PlayerColor.RED && rowDirection != 1) {
                return false;
            }
            if (piece.getColor() == PlayerColor.BLACK && rowDirection != -1) {
                return false;
            }
        }

        return true;
    }

    private boolean isValidCaptureMove(Board board, Piece piece, Position from, Position to) {
        Position middlePosition = from.middle(to);
        Piece capturedPiece = board.getPieceAt(middlePosition);

        // 检查中间位置是否有对方棋子
        if (capturedPiece == null || capturedPiece.getColor() == piece.getColor()) {
            return false;
        }

        // 检查是否是合法的吃子方向
        int rowDirection = to.getRow() - from.getRow();
        if (!piece.isKing()) {
            if (piece.getColor() == PlayerColor.RED && rowDirection != 2) {
                return false;
            }
            if (piece.getColor() == PlayerColor.BLACK && rowDirection != -2) {
                return false;
            }
        }

        return true;
    }

    public List<Position> getValidMoves(GameRoom gameRoom, Position from) {
        List<Position> validMoves = new ArrayList<>();
        Board board = gameRoom.getBoard();
        Piece piece = board.getPieceAt(from);

        if (piece == null || piece.getColor() != gameRoom.getCurrentTurn()) {
            return validMoves;
        }

        // 检查四个对角线方向
        int[] directions = {-1, 1};
        for (int rowDir : directions) {
            for (int colDir : directions) {
                // 检查普通移动
                Position normalMovePos = from.add(rowDir, colDir);
                if (isValidMove(gameRoom, from, normalMovePos)) {
                    validMoves.add(normalMovePos);
                }

                // 检查吃子移动
                Position captureMovePos = from.add(rowDir * 2, colDir * 2);
                if (isValidMove(gameRoom, from, captureMovePos)) {
                    validMoves.add(captureMovePos);
                }
            }
        }

        // 如果有吃子移动，必须选择吃子移动
        List<Position> captureMoves = validMoves.stream()
                .filter(pos -> from.distanceTo(pos) == 2)
                .toList();

        return captureMoves.isEmpty() ? validMoves : captureMoves;
    }

    public List<Move> getValidMovesForPiece(GameRoom gameRoom, Piece piece) {
        List<Move> validMoves = new ArrayList<>();
        Position from = piece.getPosition();
        List<Position> validToPositions = getValidMoves(gameRoom, from);

        for (Position to : validToPositions) {
            if (from.distanceTo(to) == 2) {
                Position capturedPosition = from.middle(to);
                List<Position> capturedPositions = new ArrayList<>();
                capturedPositions.add(capturedPosition);
                validMoves.add(new Move(from, to, capturedPositions));
            } else {
                validMoves.add(new Move(from, to));
            }
        }

        return validMoves;
    }

    public List<Move> getAllValidMoves(GameRoom gameRoom) {
        List<Move> allValidMoves = new ArrayList<>();
        Board board = gameRoom.getBoard();
        PlayerColor currentTurn = gameRoom.getCurrentTurn();

        List<Piece> pieces = board.getPiecesByColor(currentTurn);
        for (Piece piece : pieces) {
            List<Move> validMoves = getValidMovesForPiece(gameRoom, piece);
            allValidMoves.addAll(validMoves);
        }

        return allValidMoves;
    }

    public boolean hasValidMoves(GameRoom gameRoom) {
        return !getAllValidMoves(gameRoom).isEmpty();
    }
}
