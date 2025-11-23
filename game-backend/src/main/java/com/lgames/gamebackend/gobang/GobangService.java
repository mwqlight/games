package com.lgames.gamebackend.gobang;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class GobangService {
    private GameState gameState;
    private Random random;
    
    public GobangService() {
        gameState = new GameState();
        random = new Random();
    }
    
    // 重置游戏
    public GameState resetGame() {
        gameState = new GameState();
        return gameState;
    }
    
    // 玩家落子
    public GameState makeMove(int row, int col) {
        if (gameState.getStatus().equals("playing") && 
            row >= 0 && row < 15 && col >= 0 && col < 15 && 
            gameState.getBoard()[row][col] == 0) {
            
            // 玩家落子
            gameState.getBoard()[row][col] = gameState.getCurrentPlayer();
            gameState.setLastMove(new int[]{row, col});
            
            // 检查胜负
            if (checkWin(row, col)) {
                gameState.setStatus("player_win");
            } else if (checkDraw()) {
                gameState.setStatus("draw");
            } else {
                // 切换玩家为AI
                gameState.setCurrentPlayer(2);
                // AI落子
                aiMove();
            }
        }
        
        return gameState;
    }
    
    // AI落子
    private void aiMove() {
        if (gameState.getStatus().equals("playing")) {
            // 简单的AI策略：优先防守，然后进攻，最后随机落子
            int[] move = findWinningMove(2); // 尝试自己获胜
            if (move == null) {
                move = findWinningMove(1); // 尝试阻止玩家获胜
            }
            if (move == null) {
                move = findRandomMove(); // 随机落子
            }
            
            if (move != null) {
                gameState.getBoard()[move[0]][move[1]] = 2;
                gameState.setLastMove(move);
                
                // 检查胜负
                if (checkWin(move[0], move[1])) {
                    gameState.setStatus("ai_win");
                } else if (checkDraw()) {
                    gameState.setStatus("draw");
                } else {
                    // 切换玩家为人类
                    gameState.setCurrentPlayer(1);
                }
            }
        }
    }
    
    // 寻找获胜的落子位置
    private int[] findWinningMove(int player) {
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                if (gameState.getBoard()[i][j] == 0) {
                    gameState.getBoard()[i][j] = player;
                    if (checkWin(i, j)) {
                        gameState.getBoard()[i][j] = 0;
                        return new int[]{i, j};
                    }
                    gameState.getBoard()[i][j] = 0;
                }
            }
        }
        return null;
    }
    
    // 随机落子
    private int[] findRandomMove() {
        List<int[]> emptyCells = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                if (gameState.getBoard()[i][j] == 0) {
                    emptyCells.add(new int[]{i, j});
                }
            }
        }
        
        if (emptyCells.isEmpty()) {
            return null;
        }
        
        int randomIndex = random.nextInt(emptyCells.size());
        return emptyCells.get(randomIndex);
    }
    
    // 检查是否获胜
    private boolean checkWin(int row, int col) {
        int player = gameState.getBoard()[row][col];
        if (player == 0) {
            return false;
        }
        
        // 定义四个方向：水平、垂直、主对角线、副对角线
        int[][] directions = {{0, 1}, {1, 0}, {1, 1}, {1, -1}};
        
        for (int[] dir : directions) {
            int count = 1; // 当前棋子
            int r = row + dir[0];
            int c = col + dir[1];
            
            // 向正方向计数
            while (r >= 0 && r < 15 && c >= 0 && c < 15 && gameState.getBoard()[r][c] == player) {
                count++;
                r += dir[0];
                c += dir[1];
            }
            
            // 向负方向计数
            r = row - dir[0];
            c = col - dir[1];
            while (r >= 0 && r < 15 && c >= 0 && c < 15 && gameState.getBoard()[r][c] == player) {
                count++;
                r -= dir[0];
                c -= dir[1];
            }
            
            // 如果连续5个相同棋子，判定获胜
            if (count == 5) {
                // 记录获胜的连线
                gameState.setWinningLine(new int[]{row, col, dir[0], dir[1]});
                return true;
            }
        }
        
        return false;
    }
    
    // 检查是否平局
    private boolean checkDraw() {
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                if (gameState.getBoard()[i][j] == 0) {
                    return false;
                }
            }
        }
        return true;
    }
    
    // 获取当前游戏状态
    public GameState getGameState() {
        return gameState;
    }
}
