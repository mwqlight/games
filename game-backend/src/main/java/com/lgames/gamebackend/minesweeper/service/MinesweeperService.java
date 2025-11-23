package com.lgames.gamebackend.minesweeper.service;

import com.lgames.gamebackend.minesweeper.model.MinesweeperGame;
import com.lgames.gamebackend.minesweeper.model.MinesweeperGame.Cell;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class MinesweeperService {
    private final Random random = new Random();

    // 创建新游戏
    public MinesweeperGame createNewGame(String difficulty) {
        DifficultyConfig config = getDifficultyConfig(difficulty);
        MinesweeperGame game = new MinesweeperGame(config.rows, config.cols, config.mines);
        
        // 初始化棋盘
        initializeBoard(game);
        
        return game;
    }

    // 处理玩家点击单元格
    public MinesweeperGame revealCell(MinesweeperGame game, int row, int col) {
        if (game.getGameStatus() != MinesweeperGame.GameStatus.PLAYING ||
            game.getBoard()[row][col].isRevealed() ||
            game.getBoard()[row][col].isFlagged()) {
            return game;
        }

        // 揭示单元格
        game.getBoard()[row][col].setRevealed(true);

        // 检查是否是地雷
        if (game.getBoard()[row][col].isMine()) {
            game.setGameStatus(MinesweeperGame.GameStatus.GAME_OVER);
            game.setElapsedTime(System.currentTimeMillis() - game.getStartTime());
            revealAllMines(game);
            return game;
        }

        // 增加已扫雷区计数
        game.setClearedCells(game.getClearedCells() + 1);

        // 检查游戏是否完成
        if (game.getClearedCells() == game.getRows() * game.getCols() - game.getMines()) {
            game.setGameStatus(MinesweeperGame.GameStatus.SUCCESS);
            game.setElapsedTime(System.currentTimeMillis() - game.getStartTime());
            return game;
        }

        // 如果相邻地雷数为0，递归揭示周围单元格
        if (game.getBoard()[row][col].getAdjacentMines() == 0) {
            revealAdjacentCells(game, row, col);
        }

        return game;
    }

    // 处理玩家标记/取消标记单元格
    public MinesweeperGame toggleFlag(MinesweeperGame game, int row, int col) {
        if (game.getGameStatus() != MinesweeperGame.GameStatus.PLAYING ||
            game.getBoard()[row][col].isRevealed()) {
            return game;
        }

        Cell cell = game.getBoard()[row][col];
        cell.setFlagged(!cell.isFlagged());
        
        if (cell.isFlagged()) {
            game.setRemainingMines(game.getRemainingMines() - 1);
        } else {
            game.setRemainingMines(game.getRemainingMines() + 1);
        }

        return game;
    }

    // 重新开始游戏
    public MinesweeperGame restartGame(MinesweeperGame game, String difficulty) {
        return createNewGame(difficulty);
    }

    // 初始化棋盘
    private void initializeBoard(MinesweeperGame game) {
        // 随机放置地雷
        int minesPlaced = 0;
        while (minesPlaced < game.getMines()) {
            int row = random.nextInt(game.getRows());
            int col = random.nextInt(game.getCols());
            
            if (!game.getBoard()[row][col].isMine()) {
                game.getBoard()[row][col].setMine(true);
                minesPlaced++;
            }
        }

        // 计算每个单元格的相邻地雷数
        for (int i = 0; i < game.getRows(); i++) {
            for (int j = 0; j < game.getCols(); j++) {
                if (!game.getBoard()[i][j].isMine()) {
                    int count = countAdjacentMines(game, i, j);
                    game.getBoard()[i][j].setAdjacentMines(count);
                }
            }
        }
    }

    // 计算相邻地雷数
    private int countAdjacentMines(MinesweeperGame game, int row, int col) {
        int count = 0;
        
        for (int i = Math.max(0, row - 1); i <= Math.min(game.getRows() - 1, row + 1); i++) {
            for (int j = Math.max(0, col - 1); j <= Math.min(game.getCols() - 1, col + 1); j++) {
                if (game.getBoard()[i][j].isMine()) {
                    count++;
                }
            }
        }
        
        return count;
    }

    // 揭示相邻单元格
    private void revealAdjacentCells(MinesweeperGame game, int row, int col) {
        for (int i = Math.max(0, row - 1); i <= Math.min(game.getRows() - 1, row + 1); i++) {
            for (int j = Math.max(0, col - 1); j <= Math.min(game.getCols() - 1, col + 1); j++) {
                if (!game.getBoard()[i][j].isRevealed() && !game.getBoard()[i][j].isFlagged()) {
                    game.getBoard()[i][j].setRevealed(true);
                    game.setClearedCells(game.getClearedCells() + 1);
                    
                    if (game.getBoard()[i][j].getAdjacentMines() == 0) {
                        revealAdjacentCells(game, i, j);
                    }
                }
            }
        }
    }

    // 揭示所有地雷（游戏结束时）
    private void revealAllMines(MinesweeperGame game) {
        for (int i = 0; i < game.getRows(); i++) {
            for (int j = 0; j < game.getCols(); j++) {
                if (game.getBoard()[i][j].isMine()) {
                    game.getBoard()[i][j].setRevealed(true);
                }
            }
        }
    }

    // 获取难度配置
    private DifficultyConfig getDifficultyConfig(String difficulty) {
        return switch (difficulty.toLowerCase()) {
            case "easy" -> new DifficultyConfig(8, 8, 10);
            case "hard" -> new DifficultyConfig(16, 30, 99);
            default -> new DifficultyConfig(16, 16, 40); // medium
        };
    }

    // 难度配置类
    private static class DifficultyConfig {
        private final int rows;
        private final int cols;
        private final int mines;

        public DifficultyConfig(int rows, int cols, int mines) {
            this.rows = rows;
            this.cols = cols;
            this.mines = mines;
        }
    }
}
