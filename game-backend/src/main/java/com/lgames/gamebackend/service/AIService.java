package com.lgames.gamebackend.service;

import com.lgames.gamebackend.model.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class AIService {
    private final MoveValidator moveValidator;
    private final Random random = new Random();

    public AIService(MoveValidator moveValidator) {
        this.moveValidator = moveValidator;
    }

    /**
     * 让AI进行移动
     * @param gameRoom 游戏房间
     * @return 移动结果
     */
    public Move makeAIMove(GameRoom gameRoom) {
        if (gameRoom == null || gameRoom.getGameStatus() != GameStatus.PLAYING) {
            return null;
        }

        // 获取当前玩家的所有有效移动
        List<Move> allValidMoves = moveValidator.getAllValidMoves(gameRoom);
        if (allValidMoves.isEmpty()) {
            return null;
        }

        // 随机选择一个移动
        Move selectedMove = allValidMoves.get(random.nextInt(allValidMoves.size()));
        return selectedMove;
    }

    /**
     * 检查是否需要AI移动
     * @param gameRoom 游戏房间
     * @return 是否需要AI移动
     */
    public boolean isAIMoveNeeded(GameRoom gameRoom) {
        if (gameRoom == null || gameRoom.getGameStatus() != GameStatus.PLAYING) {
            return false;
        }

        // 检查当前玩家是否是AI
        Player currentPlayer = gameRoom.getPlayers().stream()
                .filter(p -> p.getColor() == gameRoom.getCurrentTurn())
                .findFirst()
                .orElse(null);

        return currentPlayer != null && currentPlayer.getId().startsWith("ai-player-");
    }
}