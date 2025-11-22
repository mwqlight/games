package com.lgames.gamebackend.chess.service;

import com.lgames.gamebackend.chess.model.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChessService {
    private Piece[][] board;
    private PieceColor currentPlayer;
    private String gameStatus;

    public ChessService() {
        initializeBoard();
    }

    // 初始化棋盘
    public void initializeBoard() {
        board = new Piece[8][8];
        currentPlayer = PieceColor.WHITE;
        gameStatus = "playing";

        // 放置白方棋子
        board[0][0] = new Piece(PieceType.ROOK, PieceColor.WHITE, new Position(0, 0));
        board[0][1] = new Piece(PieceType.KNIGHT, PieceColor.WHITE, new Position(0, 1));
        board[0][2] = new Piece(PieceType.BISHOP, PieceColor.WHITE, new Position(0, 2));
        board[0][3] = new Piece(PieceType.QUEEN, PieceColor.WHITE, new Position(0, 3));
        board[0][4] = new Piece(PieceType.KING, PieceColor.WHITE, new Position(0, 4));
        board[0][5] = new Piece(PieceType.BISHOP, PieceColor.WHITE, new Position(0, 5));
        board[0][6] = new Piece(PieceType.KNIGHT, PieceColor.WHITE, new Position(0, 6));
        board[0][7] = new Piece(PieceType.ROOK, PieceColor.WHITE, new Position(0, 7));

        for (int col = 0; col < 8; col++) {
            board[1][col] = new Piece(PieceType.PAWN, PieceColor.WHITE, new Position(1, col));
        }

        // 放置黑方棋子
        board[7][0] = new Piece(PieceType.ROOK, PieceColor.BLACK, new Position(7, 0));
        board[7][1] = new Piece(PieceType.KNIGHT, PieceColor.BLACK, new Position(7, 1));
        board[7][2] = new Piece(PieceType.BISHOP, PieceColor.BLACK, new Position(7, 2));
        board[7][3] = new Piece(PieceType.QUEEN, PieceColor.BLACK, new Position(7, 3));
        board[7][4] = new Piece(PieceType.KING, PieceColor.BLACK, new Position(7, 4));
        board[7][5] = new Piece(PieceType.BISHOP, PieceColor.BLACK, new Position(7, 5));
        board[7][6] = new Piece(PieceType.KNIGHT, PieceColor.BLACK, new Position(7, 6));
        board[7][7] = new Piece(PieceType.ROOK, PieceColor.BLACK, new Position(7, 7));

        for (int col = 0; col < 8; col++) {
            board[6][col] = new Piece(PieceType.PAWN, PieceColor.BLACK, new Position(6, col));
        }
    }

    // 获取当前游戏状态
    public GameState getGameState() {
        return new GameState(board, currentPlayer, gameStatus);
    }

    // 处理玩家走子
    public GameState makeMove(Move move) {
        Position from = move.getFrom();
        Position to = move.getTo();

        // 检查移动是否合法
        if (!isValidMove(from, to)) {
            return getGameState();
        }

        // 执行移动
        Piece piece = board[from.getX()][from.getY()];
        board[from.getX()][from.getY()] = null;
        board[to.getX()][to.getY()] = piece;
        piece.setPosition(to);
        piece.setHasMoved(true);

        // 处理兵的升变
        if (piece.getType() == PieceType.PAWN && (to.getX() == 0 || to.getX() == 7)) {
            if (move.getPromotion() != null) {
                piece.setType(move.getPromotion());
            } else {
                piece.setType(PieceType.QUEEN); // 默认升变为后
            }
        }

        // 切换玩家
        currentPlayer = currentPlayer == PieceColor.WHITE ? PieceColor.BLACK : PieceColor.WHITE;

        // 检查游戏状态
        updateGameStatus();

        return getGameState();
    }

    // 检查移动是否合法
    private boolean isValidMove(Position from, Position to) {
        // 检查位置是否在棋盘范围内
        if (!isWithinBoard(from) || !isWithinBoard(to)) {
            return false;
        }

        // 检查起始位置是否有棋子
        Piece piece = board[from.getX()][from.getY()];
        if (piece == null) {
            return false;
        }

        // 检查是否是当前玩家的棋子
        if (piece.getColor() != currentPlayer) {
            return false;
        }

        // 检查目标位置是否有己方棋子
        Piece targetPiece = board[to.getX()][to.getY()];
        if (targetPiece != null && targetPiece.getColor() == piece.getColor()) {
            return false;
        }

        // 根据棋子类型检查移动是否合法
        switch (piece.getType()) {
            case PAWN:
                return isValidPawnMove(piece, from, to);
            case ROOK:
                return isValidRookMove(piece, from, to);
            case KNIGHT:
                return isValidKnightMove(piece, from, to);
            case BISHOP:
                return isValidBishopMove(piece, from, to);
            case QUEEN:
                return isValidQueenMove(piece, from, to);
            case KING:
                return isValidKingMove(piece, from, to);
            default:
                return false;
        }
    }

    // 检查位置是否在棋盘范围内
    private boolean isWithinBoard(Position position) {
        return position.getX() >= 0 && position.getX() < 8 && position.getY() >= 0 && position.getY() < 8;
    }

    // 检查兵的移动是否合法
    private boolean isValidPawnMove(Piece piece, Position from, Position to) {
        int direction = piece.getColor() == PieceColor.WHITE ? 1 : -1;
        int startRow = piece.getColor() == PieceColor.WHITE ? 1 : 6;

        // 向前移动一格
        if (to.getX() == from.getX() + direction && to.getY() == from.getY()) {
            return board[to.getX()][to.getY()] == null;
        }

        // 初始位置可以向前移动两格
        if (from.getX() == startRow && to.getX() == from.getX() + direction * 2 && to.getY() == from.getY()) {
            return board[from.getX() + direction][from.getY()] == null && board[to.getX()][to.getY()] == null;
        }

        // 吃子
        if (to.getX() == from.getX() + direction && Math.abs(to.getY() - from.getY()) == 1) {
            return board[to.getX()][to.getY()] != null && board[to.getX()][to.getY()].getColor() != piece.getColor();
        }

        return false;
    }

    // 检查车的移动是否合法
    private boolean isValidRookMove(Piece piece, Position from, Position to) {
        // 检查是否是直线移动
        if (from.getX() != to.getX() && from.getY() != to.getY()) {
            return false;
        }

        // 检查路径是否有障碍物
        int xStep = from.getX() == to.getX() ? 0 : (to.getX() > from.getX() ? 1 : -1);
        int yStep = from.getY() == to.getY() ? 0 : (to.getY() > from.getY() ? 1 : -1);

        int x = from.getX() + xStep;
        int y = from.getY() + yStep;

        while (x != to.getX() || y != to.getY()) {
            if (board[x][y] != null) {
                return false;
            }
            x += xStep;
            y += yStep;
        }

        return true;
    }

    // 检查马的移动是否合法
    private boolean isValidKnightMove(Piece piece, Position from, Position to) {
        // 检查是否是L形移动
        int dx = Math.abs(to.getX() - from.getX());
        int dy = Math.abs(to.getY() - from.getY());

        return (dx == 2 && dy == 1) || (dx == 1 && dy == 2);
    }

    // 检查象的移动是否合法
    private boolean isValidBishopMove(Piece piece, Position from, Position to) {
        // 检查是否是斜线移动
        if (Math.abs(to.getX() - from.getX()) != Math.abs(to.getY() - from.getY())) {
            return false;
        }

        // 检查路径是否有障碍物
        int xStep = to.getX() > from.getX() ? 1 : -1;
        int yStep = to.getY() > from.getY() ? 1 : -1;

        int x = from.getX() + xStep;
        int y = from.getY() + yStep;

        while (x != to.getX() && y != to.getY()) {
            if (board[x][y] != null) {
                return false;
            }
            x += xStep;
            y += yStep;
        }

        return true;
    }

    // 检查后的移动是否合法
    private boolean isValidQueenMove(Piece piece, Position from, Position to) {
        // 检查是否是直线或斜线移动
        if (from.getX() != to.getX() && from.getY() != to.getY() && 
            Math.abs(to.getX() - from.getX()) != Math.abs(to.getY() - from.getY())) {
            return false;
        }

        // 检查路径是否有障碍物
        int xStep = 0;
        int yStep = 0;

        if (to.getX() > from.getX()) xStep = 1;
        else if (to.getX() < from.getX()) xStep = -1;

        if (to.getY() > from.getY()) yStep = 1;
        else if (to.getY() < from.getY()) yStep = -1;

        int x = from.getX() + xStep;
        int y = from.getY() + yStep;

        while (x != to.getX() || y != to.getY()) {
            if (board[x][y] != null) {
                return false;
            }
            x += xStep;
            y += yStep;
        }

        return true;
    }

    // 检查王的移动是否合法
    private boolean isValidKingMove(Piece piece, Position from, Position to) {
        // 检查是否移动了一格
        int dx = Math.abs(to.getX() - from.getX());
        int dy = Math.abs(to.getY() - from.getY());

        if (dx <= 1 && dy <= 1) {
            return true;
        }

        // 检查王车易位
        if (!piece.isHasMoved() && dx == 0 && dy == 2) {
            // 检查车是否在正确的位置
            int rookCol = dy > 0 ? 7 : 0;
            Piece rook = board[from.getX()][rookCol];

            if (rook != null && rook.getType() == PieceType.ROOK && !rook.isHasMoved()) {
                // 检查路径是否有障碍物
                int yStep = dy > 0 ? 1 : -1;
                for (int y = from.getY() + yStep; y != rookCol; y += yStep) {
                    if (board[from.getX()][y] != null) {
                        return false;
                    }
                }

                // 检查王是否在将军状态
                if (isKingInCheck(piece.getColor())) {
                    return false;
                }

                // 检查王移动经过的位置是否在将军状态
                for (int y = from.getY(); y != to.getY() + yStep; y += yStep) {
                    if (isKingInCheckAfterMove(piece, new Position(from.getX(), y))) {
                        return false;
                    }
                }

                return true;
            }
        }

        return false;
    }

    // 检查王是否在将军状态
    private boolean isKingInCheck(PieceColor color) {
        Position kingPosition = findKingPosition(color);
        if (kingPosition == null) {
            return false;
        }

        // 检查对方所有棋子是否能攻击到王
        PieceColor opponentColor = color == PieceColor.WHITE ? PieceColor.BLACK : PieceColor.WHITE;

        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                Piece piece = board[x][y];
                if (piece != null && piece.getColor() == opponentColor) {
                    if (isValidMoveWithoutCheck(piece.getPosition(), kingPosition)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    // 找到王的位置
    private Position findKingPosition(PieceColor color) {
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                Piece piece = board[x][y];
                if (piece != null && piece.getType() == PieceType.KING && piece.getColor() == color) {
                    return piece.getPosition();
                }
            }
        }
        return null;
    }

    // 不考虑将军状态检查移动是否合法
    private boolean isValidMoveWithoutCheck(Position from, Position to) {
        // 检查位置是否在棋盘范围内
        if (!isWithinBoard(from) || !isWithinBoard(to)) {
            return false;
        }

        // 检查起始位置是否有棋子
        Piece piece = board[from.getX()][from.getY()];
        if (piece == null) {
            return false;
        }

        // 检查目标位置是否有己方棋子
        Piece targetPiece = board[to.getX()][to.getY()];
        if (targetPiece != null && targetPiece.getColor() == piece.getColor()) {
            return false;
        }

        // 根据棋子类型检查移动是否合法
        switch (piece.getType()) {
            case PAWN:
                return isValidPawnMove(piece, from, to);
            case ROOK:
                return isValidRookMove(piece, from, to);
            case KNIGHT:
                return isValidKnightMove(piece, from, to);
            case BISHOP:
                return isValidBishopMove(piece, from, to);
            case QUEEN:
                return isValidQueenMove(piece, from, to);
            case KING:
                return isValidKingMove(piece, from, to);
            default:
                return false;
        }
    }

    // 检查王移动到指定位置后是否在将军状态
    private boolean isKingInCheckAfterMove(Piece king, Position newPosition) {
        // 保存当前状态
        Piece[][] originalBoard = deepCopyBoard(board);
        Position originalPosition = king.getPosition();
        boolean originalHasMoved = king.isHasMoved();

        // 执行移动
        board[originalPosition.getX()][originalPosition.getY()] = null;
        board[newPosition.getX()][newPosition.getY()] = king;
        king.setPosition(newPosition);
        king.setHasMoved(true);

        // 检查是否在将军状态
        boolean isInCheck = isKingInCheck(king.getColor());

        // 恢复原来的状态
        board = originalBoard;
        king.setPosition(originalPosition);
        king.setHasMoved(originalHasMoved);

        return isInCheck;
    }

    // 深拷贝棋盘
    private Piece[][] deepCopyBoard(Piece[][] original) {
        Piece[][] copy = new Piece[8][8];
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                if (original[x][y] != null) {
                    copy[x][y] = new Piece(original[x][y].getType(), original[x][y].getColor(), 
                        new Position(original[x][y].getPosition().getX(), original[x][y].getPosition().getY()));
                    copy[x][y].setHasMoved(original[x][y].isHasMoved());
                }
            }
        }
        return copy;
    }

    // 更新游戏状态
    private void updateGameStatus() {
        // 检查当前玩家是否在将军状态
        if (isKingInCheck(currentPlayer)) {
            // 检查是否有合法移动
            if (!hasValidMoves(currentPlayer)) {
                gameStatus = "checkmate";
            } else {
                gameStatus = "check";
            }
        } else {
            // 检查是否有合法移动
            if (!hasValidMoves(currentPlayer)) {
                gameStatus = "stalemate";
            } else {
                gameStatus = "playing";
            }
        }
    }

    // 检查当前玩家是否有合法移动
    private boolean hasValidMoves(PieceColor color) {
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                Piece piece = board[x][y];
                if (piece != null && piece.getColor() == color) {
                    List<Move> validMoves = getValidMovesForPiece(piece);
                    if (!validMoves.isEmpty()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // 获取棋子的所有合法移动
    public List<Move> getValidMovesForPiece(Piece piece) {
        List<Move> validMoves = new ArrayList<>();
        Position from = piece.getPosition();

        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                Position to = new Position(x, y);
                if (isValidMove(from, to)) {
                    validMoves.add(new Move(from, to));
                }
            }
        }

        return validMoves;
    }
}