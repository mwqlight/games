package com.lgames.gamebackend.service;

import com.lgames.gamebackend.model.GameState;
import org.springframework.stereotype.Service;

@Service
public class ChessGameService {
    private ChessRuleService ruleService = new ChessRuleService();
    private ChessAIService aiService = new ChessAIService();
    private GameState gameState = new GameState();

    // 开始新游戏
    public GameState startNewGame() {
        gameState = new GameState();
        return gameState;
    }

    // 获取当前游戏状态
    public GameState getCurrentGameState() {
        return gameState;
    }

    // 处理玩家走子
    public GameState makeMove(int fromX, int fromY, int toX, int toY) {
        if (gameState.getGameStatus().equals("PLAYING") && gameState.getCurrentPlayer().equals("red")) {
            if (ruleService.isValidMove(gameState, fromX, fromY, toX, toY)) {
                ruleService.makeMove(gameState, fromX, fromY, toX, toY);

                // 如果游戏还在进行中，AI走子
                if (gameState.getGameStatus().equals("PLAYING")) {
                    aiMove();
                }
            }
        }
        return gameState;
    }

    // AI走子
    private void aiMove() {
        int[] move = aiService.calculateAIMove(gameState);
        if (move != null) {
            ruleService.makeMove(gameState, move[0], move[1], move[2], move[3]);
        }
    }
}
