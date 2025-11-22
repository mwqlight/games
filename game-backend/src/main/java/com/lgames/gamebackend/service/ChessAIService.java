package com.lgames.gamebackend.service;

import com.lgames.gamebackend.model.GameState;

import java.util.List;
import java.util.Random;

public class ChessAIService {
    private ChessRuleService ruleService = new ChessRuleService();
    private Random random = new Random();
    private int maxDepth = 3; // 搜索深度

    // 计算AI的最佳走法
    public int[] calculateAIMove(GameState state) {
        // 使用Minimax算法与Alpha-Beta剪枝进行决策
        return minimax(state, maxDepth, Integer.MIN_VALUE, Integer.MAX_VALUE, false).getBestMove();
    }

    // Minimax算法
    private MinimaxResult minimax(GameState state, int depth, int alpha, int beta, boolean isMaximizing) {
        if (depth == 0 || !state.getGameStatus().equals("PLAYING")) {
            return new MinimaxResult(evaluateBoard(state), null);
        }

        List<int[]> validMoves = ruleService.getAllValidMoves(state, isMaximizing ? "black" : "red");

        if (validMoves.isEmpty()) {
            return new MinimaxResult(isMaximizing ? Integer.MIN_VALUE : Integer.MAX_VALUE, null);
        }

        int[] bestMove = validMoves.get(random.nextInt(validMoves.size()));
        int bestValue = isMaximizing ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        for (int[] move : validMoves) {
            GameState tempState = state.copy();
            ruleService.makeMove(tempState, move[0], move[1], move[2], move[3]);

            int currentValue = minimax(tempState, depth - 1, alpha, beta, !isMaximizing).getValue();

            if (isMaximizing) {
                if (currentValue > bestValue) {
                    bestValue = currentValue;
                    bestMove = move;
                }
                alpha = Math.max(alpha, bestValue);
            } else {
                if (currentValue < bestValue) {
                    bestValue = currentValue;
                    bestMove = move;
                }
                beta = Math.min(beta, bestValue);
            }

            if (beta <= alpha) {
                break; // Alpha-Beta剪枝
            }
        }

        return new MinimaxResult(bestValue, bestMove);
    }

    // 评估棋盘价值
    private int evaluateBoard(GameState state) {
        int score = 0;

        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 10; y++) {
                if (state.getBoard()[x][y] != null) {
                    score += getPieceValue(state.getBoard()[x][y], x, y);
                }
            }
        }

        // 检查是否被将军
        if (ruleService.isInCheck(state, "black")) {
            score -= 500;
        }
        if (ruleService.isInCheck(state, "red")) {
            score += 500;
        }

        // 检查是否将死
        if (ruleService.isCheckmate(state, "black")) {
            score -= 10000;
        }
        if (ruleService.isCheckmate(state, "red")) {
            score += 10000;
        }

        return score;
    }

    // 获取棋子价值
    private int getPieceValue(com.lgames.gamebackend.model.ChessPiece piece, int x, int y) {
        int baseValue = 0;

        switch (piece.getType()) {
            case "king":
                baseValue = 10000;
                break;
            case "rook":
                baseValue = 2000;
                break;
            case "cannon":
                baseValue = 1000;
                break;
            case "knight":
                baseValue = 800;
                break;
            case "bishop":
                baseValue = 600;
                break;
            case "advisor":
                baseValue = 500;
                break;
            case "pawn":
                baseValue = 300;
                break;
        }

        // 根据位置调整价值
        if (piece.getColor().equals("black")) {
            baseValue = -baseValue;
        }

        // 兵的位置价值
        if (piece.getType().equals("pawn")) {
            if (piece.getColor().equals("red")) {
                baseValue += y * 10; // 红兵越往前价值越高
            } else {
                baseValue -= (9 - y) * 10; // 黑兵越往前价值越高
            }
        }

        // 车、炮、马的位置价值
        if (piece.getType().equals("rook") || piece.getType().equals("cannon") || piece.getType().equals("knight")) {
            if (piece.getColor().equals("red")) {
                baseValue += (y > 4 ? 50 : 0); // 过河后价值增加
            } else {
                baseValue -= (y < 5 ? 50 : 0); // 过河后价值增加
            }
        }

        return baseValue;
    }

    // Minimax结果类
    private class MinimaxResult {
        private int value;
        private int[] bestMove;

        public MinimaxResult(int value, int[] bestMove) {
            this.value = value;
            this.bestMove = bestMove;
        }

        public int getValue() {
            return value;
        }

        public int[] getBestMove() {
            return bestMove;
        }
    }
}
