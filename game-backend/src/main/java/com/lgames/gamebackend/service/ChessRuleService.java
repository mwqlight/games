package com.lgames.gamebackend.service;

import com.lgames.gamebackend.model.ChessPiece;
import com.lgames.gamebackend.model.GameState;

import java.util.ArrayList;
import java.util.List;

public class ChessRuleService {
    // 检查走法是否合法
    public boolean isValidMove(GameState state, int fromX, int fromY, int toX, int toY) {
        // 检查边界
        if (fromX < 0 || fromX >= 9 || fromY < 0 || fromY >= 10 ||
            toX < 0 || toX >= 9 || toY < 0 || toY >= 10) {
            return false;
        }

        ChessPiece piece = state.getBoard()[fromX][fromY];
        ChessPiece target = state.getBoard()[toX][toY];

        // 检查是否是当前玩家的棋子
        if (piece == null || !piece.getColor().equals(state.getCurrentPlayer())) {
            return false;
        }

        // 检查目标位置是否是自己的棋子
        if (target != null && target.getColor().equals(piece.getColor())) {
            return false;
        }

        // 根据棋子类型检查走法
        switch (piece.getType()) {
            case "rook":
                return isValidRookMove(state, fromX, fromY, toX, toY);
            case "knight":
                return isValidKnightMove(state, fromX, fromY, toX, toY);
            case "bishop":
                return isValidBishopMove(state, fromX, fromY, toX, toY);
            case "advisor":
                return isValidAdvisorMove(state, fromX, fromY, toX, toY);
            case "king":
                return isValidKingMove(state, fromX, fromY, toX, toY);
            case "cannon":
                return isValidCannonMove(state, fromX, fromY, toX, toY);
            case "pawn":
                return isValidPawnMove(state, fromX, fromY, toX, toY);
            default:
                return false;
        }
    }

    // 检查车的走法
    private boolean isValidRookMove(GameState state, int fromX, int fromY, int toX, int toY) {
        // 车只能走直线
        if (fromX != toX && fromY != toY) {
            return false;
        }

        // 检查路径上是否有棋子
        if (fromX == toX) {
            // 水平移动
            int startY = Math.min(fromY, toY) + 1;
            int endY = Math.max(fromY, toY) - 1;
            for (int y = startY; y <= endY; y++) {
                if (state.getBoard()[fromX][y] != null) {
                    return false;
                }
            }
        } else {
            // 垂直移动
            int startX = Math.min(fromX, toX) + 1;
            int endX = Math.max(fromX, toX) - 1;
            for (int x = startX; x <= endX; x++) {
                if (state.getBoard()[x][fromY] != null) {
                    return false;
                }
            }
        }

        return true;
    }

    // 检查马的走法
    private boolean isValidKnightMove(GameState state, int fromX, int fromY, int toX, int toY) {
        // 马走"日"字
        int deltaX = Math.abs(toX - fromX);
        int deltaY = Math.abs(toY - fromY);
        if (!((deltaX == 1 && deltaY == 2) || (deltaX == 2 && deltaY == 1))) {
            return false;
        }

        // 检查是否蹩腿
        if (deltaX == 1) {
            // 横向移动1，纵向移动2
            int blockY = (fromY + toY) / 2;
            if (state.getBoard()[fromX][blockY] != null) {
                return false;
            }
        } else {
            // 横向移动2，纵向移动1
            int blockX = (fromX + toX) / 2;
            if (state.getBoard()[blockX][fromY] != null) {
                return false;
            }
        }

        return true;
    }

    // 检查象的走法
    private boolean isValidBishopMove(GameState state, int fromX, int fromY, int toX, int toY) {
        // 象走"田"字
        int deltaX = Math.abs(toX - fromX);
        int deltaY = Math.abs(toY - fromY);
        if (deltaX != 2 || deltaY != 2) {
            return false;
        }

        // 检查是否塞象眼
        int blockX = (fromX + toX) / 2;
        int blockY = (fromY + toY) / 2;
        if (state.getBoard()[blockX][blockY] != null) {
            return false;
        }

        // 检查是否过河
        ChessPiece piece = state.getBoard()[fromX][fromY];
        if (piece.getColor().equals("red")) {
            // 红象不能过河（y > 4）
            if (toY > 4) {
                return false;
            }
        } else {
            // 黑象不能过河（y < 5）
            if (toY < 5) {
                return false;
            }
        }

        return true;
    }

    // 检查士的走法
    private boolean isValidAdvisorMove(GameState state, int fromX, int fromY, int toX, int toY) {
        // 士走斜线，每次一格
        int deltaX = Math.abs(toX - fromX);
        int deltaY = Math.abs(toY - fromY);
        if (deltaX != 1 || deltaY != 1) {
            return false;
        }

        // 检查是否在九宫格内
        if (!isInPalace(toX, toY)) {
            return false;
        }

        return true;
    }

    // 检查帅的走法
    private boolean isValidKingMove(GameState state, int fromX, int fromY, int toX, int toY) {
        // 帅走直线，每次一格
        int deltaX = Math.abs(toX - fromX);
        int deltaY = Math.abs(toY - fromY);
        if (!((deltaX == 1 && deltaY == 0) || (deltaX == 0 && deltaY == 1))) {
            return false;
        }

        // 检查是否在九宫格内
        if (!isInPalace(toX, toY)) {
            return false;
        }

        // 检查将帅是否照面
        if (isKingFacingKing(state, toX, toY)) {
            return false;
        }

        return true;
    }

    // 检查炮的走法
    private boolean isValidCannonMove(GameState state, int fromX, int fromY, int toX, int toY) {
        // 炮只能走直线
        if (fromX != toX && fromY != toY) {
            return false;
        }

        ChessPiece target = state.getBoard()[toX][toY];

        // 检查路径上的棋子数量
        int pieceCount = 0;
        if (fromX == toX) {
            // 水平移动
            int startY = Math.min(fromY, toY) + 1;
            int endY = Math.max(fromY, toY) - 1;
            for (int y = startY; y <= endY; y++) {
                if (state.getBoard()[fromX][y] != null) {
                    pieceCount++;
                }
            }
        } else {
            // 垂直移动
            int startX = Math.min(fromX, toX) + 1;
            int endX = Math.max(fromX, toX) - 1;
            for (int x = startX; x <= endX; x++) {
                if (state.getBoard()[x][fromY] != null) {
                    pieceCount++;
                }
            }
        }

        // 炮吃子时需要隔一个棋子
        if (target != null) {
            return pieceCount == 1;
        } else {
            // 炮移动时不能有棋子阻挡
            return pieceCount == 0;
        }
    }

    // 检查兵的走法
    private boolean isValidPawnMove(GameState state, int fromX, int fromY, int toX, int toY) {
        ChessPiece piece = state.getBoard()[fromX][fromY];
        int deltaX = Math.abs(toX - fromX);
        int deltaY = toY - fromY;

        if (piece.getColor().equals("red")) {
            // 红兵向前走（y增加）
            if (deltaY < 0) {
                return false;
            }
        } else {
            // 黑兵向前走（y减少）
            if (deltaY > 0) {
                return false;
            }
        }

        // 检查是否过河
        if (piece.getColor().equals("red") && fromY < 5 || piece.getColor().equals("black") && fromY > 4) {
            // 未过河，只能向前走一格
            return deltaX == 0 && Math.abs(deltaY) == 1;
        } else {
            // 已过河，可以向前、左、右走一格
            return (deltaX == 0 && Math.abs(deltaY) == 1) || (Math.abs(deltaX) == 1 && deltaY == 0);
        }
    }

    // 检查是否在九宫格内
    private boolean isInPalace(int x, int y) {
        // 九宫格范围：x 3-5, y 0-2（红方）或 y 7-9（黑方）
        return x >= 3 && x <= 5 && (y >= 0 && y <= 2 || y >= 7 && y <= 9);
    }

    // 检查将帅是否照面
    private boolean isKingFacingKing(GameState state, int kingX, int kingY) {
        ChessPiece king = state.getBoard()[kingX][kingY];
        String opponentColor = king.getColor().equals("red") ? "black" : "red";

        // 找到对方的将
        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 10; y++) {
                ChessPiece piece = state.getBoard()[x][y];
                if (piece != null && piece.getType().equals("king") && piece.getColor().equals(opponentColor)) {
                    // 检查是否在同一直线上，且中间没有棋子
                    if (x == kingX) {
                        int startY = Math.min(kingY, y) + 1;
                        int endY = Math.max(kingY, y) - 1;
                        for (int y2 = startY; y2 <= endY; y2++) {
                            if (state.getBoard()[x][y2] != null) {
                                return false;
                            }
                        }
                        return true;
                    }
                    return false;
                }
            }
        }

        return false;
    }

    // 获取所有合法走法
    public List<int[]> getAllValidMoves(GameState state, String color) {
        List<int[]> validMoves = new ArrayList<>();

        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 10; y++) {
                ChessPiece piece = state.getBoard()[x][y];
                if (piece != null && piece.getColor().equals(color)) {
                    for (int toX = 0; toX < 9; toX++) {
                        for (int toY = 0; toY < 10; toY++) {
                            if (isValidMove(state, x, y, toX, toY)) {
                                validMoves.add(new int[]{x, y, toX, toY});
                            }
                        }
                    }
                }
            }
        }

        return validMoves;
    }

    // 检查是否将死
    public boolean isCheckmate(GameState state, String color) {
        // 检查是否被将军
        if (!isInCheck(state, color)) {
            return false;
        }

        // 检查是否有任何合法走法可以解将
        List<int[]> validMoves = getAllValidMoves(state, color);
        for (int[] move : validMoves) {
            GameState tempState = state.copy();
            makeMove(tempState, move[0], move[1], move[2], move[3]);
            if (!isInCheck(tempState, color)) {
                return false;
            }
        }

        return true;
    }

    // 检查是否被将军
    public boolean isInCheck(GameState state, String color) {
        String opponentColor = color.equals("red") ? "black" : "red";
        int kingX = -1, kingY = -1;

        // 找到自己的帅
        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 10; y++) {
                ChessPiece piece = state.getBoard()[x][y];
                if (piece != null && piece.getType().equals("king") && piece.getColor().equals(color)) {
                    kingX = x;
                    kingY = y;
                    break;
                }
            }
            if (kingX != -1) {
                break;
            }
        }

        if (kingX == -1) {
            // 帅已经被吃掉
            return true;
        }

        // 检查对方是否有任何棋子可以攻击到帅
        List<int[]> opponentMoves = getAllValidMoves(state, opponentColor);
        for (int[] move : opponentMoves) {
            if (move[2] == kingX && move[3] == kingY) {
                return true;
            }
        }

        return false;
    }

    // 执行走子
    public void makeMove(GameState state, int fromX, int fromY, int toX, int toY) {
        ChessPiece piece = state.getBoard()[fromX][fromY];
        ChessPiece target = state.getBoard()[toX][toY];

        // 移动棋子
        state.getBoard()[toX][toY] = piece;
        state.getBoard()[fromX][fromY] = null;
        piece.setX(toX);
        piece.setY(toY);

        // 添加走子记录
        state.getMoveHistory().add(new com.lgames.gamebackend.model.Move(fromX, fromY, toX, toY, piece.getType(), piece.getColor(), target != null));

        // 切换玩家
        state.setCurrentPlayer(state.getCurrentPlayer().equals("red") ? "black" : "red");

        // 检查游戏是否结束
        checkGameEnd(state);
    }

    // 检查游戏是否结束
    private void checkGameEnd(GameState state) {
        String currentPlayer = state.getCurrentPlayer();
        String opponentColor = currentPlayer.equals("red") ? "black" : "red";

        if (isCheckmate(state, opponentColor)) {
            state.setGameStatus(currentPlayer.equals("red") ? "BLACK_WIN" : "RED_WIN");
        }
    }
}
