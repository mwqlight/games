package com.lgames.gamebackend.sudoku.service;

import com.lgames.gamebackend.sudoku.model.SudokuGame;
import com.lgames.gamebackend.sudoku.model.Move;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;

@Service
public class SudokuService {
    private static final int SIZE = 9;
    private static final int SUBGRID_SIZE = 3;
    private static final int EMPTY_CELL = 0;
    private final Random random = new Random();

    // 生成新的数独游戏
    public SudokuGame generateNewGame(SudokuGame.Difficulty difficulty) {
        SudokuGame game = new SudokuGame();
        game.setGameId(UUID.randomUUID().toString());
        game.setDifficulty(difficulty);
        game.setGameStatus(SudokuGame.GameStatus.PLAYING);
        game.setStartTime(System.currentTimeMillis());
        game.setElapsedTime(0);
        game.setHintCount(0);
        game.setErrorCount(0);

        // 生成完整的数独解
        int[][] solution = generateSolution();
        game.setSolution(solution);

        // 根据难度生成谜题
        int[][] puzzle = generatePuzzle(solution, difficulty);
        game.setPuzzle(puzzle);

        // 初始化用户棋盘
        game.setUserBoard(copyBoard(puzzle));

        return game;
    }

    // 生成数独解
    private int[][] generateSolution() {
        int[][] board = new int[SIZE][SIZE];
        fillBoard(board);
        return board;
    }

    // 填充数独棋盘
    private boolean fillBoard(int[][] board) {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (board[row][col] == EMPTY_CELL) {
                    int[] numbers = shuffleNumbers();
                    for (int num : numbers) {
                        if (isValidMove(board, row, col, num)) {
                            board[row][col] = num;
                            if (fillBoard(board)) {
                                return true;
                            }
                            board[row][col] = EMPTY_CELL;
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }

    // 打乱数字顺序
    private int[] shuffleNumbers() {
        int[] numbers = {1, 2, 3, 4, 5, 6, 7, 8, 9};
        for (int i = numbers.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int temp = numbers[i];
            numbers[i] = numbers[j];
            numbers[j] = temp;
        }
        return numbers;
    }

    // 根据难度生成谜题
    private int[][] generatePuzzle(int[][] solution, SudokuGame.Difficulty difficulty) {
        int[][] puzzle = copyBoard(solution);
        int cellsToRemove = getCellsToRemove(difficulty);

        // 对称地移除数字
        while (cellsToRemove > 0) {
            int row = random.nextInt(SIZE);
            int col = random.nextInt(SIZE);
            int mirrorRow = SIZE - 1 - row;
            int mirrorCol = SIZE - 1 - col;

            if (puzzle[row][col] != EMPTY_CELL) {
                puzzle[row][col] = EMPTY_CELL;
                cellsToRemove--;
                if (cellsToRemove > 0 && puzzle[mirrorRow][mirrorCol] != EMPTY_CELL) {
                    puzzle[mirrorRow][mirrorCol] = EMPTY_CELL;
                    cellsToRemove--;
                }
            }
        }

        return puzzle;
    }

    // 根据难度确定要移除的单元格数量
    private int getCellsToRemove(SudokuGame.Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> 40;
            case MEDIUM -> 50;
            case HARD -> 60;
        };
    }

    // 处理玩家的移动
    public SudokuGame makeMove(SudokuGame game, Move move) {
        int row = move.getRow();
        int col = move.getCol();
        int number = move.getNumber();

        // 检查是否是预填数字
        if (game.getPuzzle()[row][col] != EMPTY_CELL) {
            return game;
        }

        // 更新用户棋盘
        game.getUserBoard()[row][col] = number;

        // 检查是否正确
        if (number != 0 && number != game.getSolution()[row][col]) {
            game.setErrorCount(game.getErrorCount() + 1);
        }

        // 检查游戏是否完成
        if (isGameComplete(game.getUserBoard(), game.getSolution())) {
            game.setGameStatus(SudokuGame.GameStatus.SUCCESS);
            game.setElapsedTime(System.currentTimeMillis() - game.getStartTime());
        }

        return game;
    }

    // 验证用户的解答
    public boolean validateSolution(SudokuGame game) {
        return isGameComplete(game.getUserBoard(), game.getSolution());
    }

    // 检查游戏是否完成
    private boolean isGameComplete(int[][] userBoard, int[][] solution) {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (userBoard[row][col] != solution[row][col]) {
                    return false;
                }
            }
        }
        return true;
    }

    // 获取提示
    public SudokuGame getHint(SudokuGame game) {
        // 找到一个空白单元格
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (game.getUserBoard()[row][col] == EMPTY_CELL) {
                    // 填充正确答案
                    game.getUserBoard()[row][col] = game.getSolution()[row][col];
                    game.setHintCount(game.getHintCount() + 1);
                    // 检查游戏是否完成
                    if (isGameComplete(game.getUserBoard(), game.getSolution())) {
                        game.setGameStatus(SudokuGame.GameStatus.SUCCESS);
                        game.setElapsedTime(System.currentTimeMillis() - game.getStartTime());
                    }
                    return game;
                }
            }
        }
        return game;
    }

    // 检查移动是否有效
    public boolean isValidMove(int[][] board, int row, int col, int number) {
        // 检查行
        for (int i = 0; i < SIZE; i++) {
            if (board[row][i] == number) {
                return false;
            }
        }

        // 检查列
        for (int i = 0; i < SIZE; i++) {
            if (board[i][col] == number) {
                return false;
            }
        }

        // 检查3x3宫格
        int subgridRow = row - row % SUBGRID_SIZE;
        int subgridCol = col - col % SUBGRID_SIZE;
        for (int i = subgridRow; i < subgridRow + SUBGRID_SIZE; i++) {
            for (int j = subgridCol; j < subgridCol + SUBGRID_SIZE; j++) {
                if (board[i][j] == number) {
                    return false;
                }
            }
        }

        return true;
    }

    // 复制棋盘
    private int[][] copyBoard(int[][] original) {
        int[][] copy = new int[SIZE][SIZE];
        for (int row = 0; row < SIZE; row++) {
            System.arraycopy(original[row], 0, copy[row], 0, SIZE);
        }
        return copy;
    }
}
