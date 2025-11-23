package com.lgames.gamebackend.puzzle.service;

import com.lgames.gamebackend.puzzle.model.PuzzleGame;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class PuzzleService {
    private static final int DEFAULT_BOARD_SIZE = 4;
    private final Random random = new Random();

    // 创建新游戏
    public PuzzleGame createNewGame() {
        PuzzleGame game = new PuzzleGame(DEFAULT_BOARD_SIZE);
        
        // 初始化棋盘
        initializeBoard(game);
        
        // 打乱棋盘
        shuffleBoard(game);
        
        return game;
    }

    // 处理玩家移动
    public PuzzleGame makeMove(PuzzleGame game, int row, int col) {
        if (game.getGameStatus() != PuzzleGame.GameStatus.PLAYING) {
            return game;
        }

        // 检查点击的单元格是否与空白格相邻
        int emptyRow = -1;
        int emptyCol = -1;
        
        // 找到空白格位置
        for (int i = 0; i < game.getBoardSize(); i++) {
            for (int j = 0; j < game.getBoardSize(); j++) {
                if (game.getBoard()[i][j] == 0) {
                    emptyRow = i;
                    emptyCol = j;
                    break;
                }
            }
        }

        // 检查是否相邻
        if (isAdjacent(row, col, emptyRow, emptyCol)) {
            // 交换位置
            int temp = game.getBoard()[row][col];
            game.getBoard()[row][col] = game.getBoard()[emptyRow][emptyCol];
            game.getBoard()[emptyRow][emptyCol] = temp;
            
            // 增加步数
            game.setMoves(game.getMoves() + 1);
            
            // 检查游戏是否完成
            if (isGameComplete(game)) {
                game.setGameStatus(PuzzleGame.GameStatus.SUCCESS);
                game.setElapsedTime(System.currentTimeMillis() - game.getStartTime());
                
                // 计算得分
                int baseScore = 1000;
                long timeBonus = Math.max(0L, 60000L - game.getElapsedTime()) / 1000L;
                int movePenalty = game.getMoves() * 5;
                game.setScore(baseScore + (int) timeBonus - movePenalty);
            }
        }

        return game;
    }

    // 暂停/继续游戏
    public PuzzleGame togglePause(PuzzleGame game) {
        if (game.getGameStatus() == PuzzleGame.GameStatus.PLAYING) {
            game.setGameStatus(PuzzleGame.GameStatus.PAUSED);
            game.setElapsedTime(System.currentTimeMillis() - game.getStartTime());
        } else if (game.getGameStatus() == PuzzleGame.GameStatus.PAUSED) {
            game.setGameStatus(PuzzleGame.GameStatus.PLAYING);
            game.setStartTime(System.currentTimeMillis() - game.getElapsedTime());
        }
        return game;
    }

    // 重新开始游戏
    public PuzzleGame restartGame(PuzzleGame game) {
        return createNewGame();
    }

    // 初始化棋盘
    private void initializeBoard(PuzzleGame game) {
        int number = 1;
        for (int i = 0; i < game.getBoardSize(); i++) {
            for (int j = 0; j < game.getBoardSize(); j++) {
                game.getBoard()[i][j] = number++;
            }
        }
        // 设置最后一个单元格为空白
        game.getBoard()[game.getBoardSize() - 1][game.getBoardSize() - 1] = 0;
    }

    // 打乱棋盘
    private void shuffleBoard(PuzzleGame game) {
        int emptyRow = game.getBoardSize() - 1;
        int emptyCol = game.getBoardSize() - 1;
        
        // 随机移动1000次
        for (int i = 0; i < 1000; i++) {
            // 获取所有可能的移动方向
            int[][] directions = {
                {-1, 0}, {1, 0}, {0, -1}, {0, 1}
            };
            
            // 随机选择一个方向
            int[] direction = directions[random.nextInt(directions.length)];
            int newRow = emptyRow + direction[0];
            int newCol = emptyCol + direction[1];
            
            // 检查是否在边界内
            if (newRow >= 0 && newRow < game.getBoardSize() &&
                newCol >= 0 && newCol < game.getBoardSize()) {
                // 交换位置
                int temp = game.getBoard()[newRow][newCol];
                game.getBoard()[newRow][newCol] = game.getBoard()[emptyRow][emptyCol];
                game.getBoard()[emptyRow][emptyCol] = temp;
                
                // 更新空白格位置
                emptyRow = newRow;
                emptyCol = newCol;
            }
        }
    }

    // 检查两个单元格是否相邻
    private boolean isAdjacent(int row1, int col1, int row2, int col2) {
        return (Math.abs(row1 - row2) == 1 && col1 == col2) ||
               (Math.abs(col1 - col2) == 1 && row1 == row2);
    }

    // 检查游戏是否完成
    private boolean isGameComplete(PuzzleGame game) {
        int number = 1;
        for (int i = 0; i < game.getBoardSize(); i++) {
            for (int j = 0; j < game.getBoardSize(); j++) {
                // 最后一个单元格应该是空白
                if (i == game.getBoardSize() - 1 && j == game.getBoardSize() - 1) {
                    return game.getBoard()[i][j] == 0;
                }
                
                if (game.getBoard()[i][j] != number++) {
                    return false;
                }
            }
        }
        return true;
    }
}
