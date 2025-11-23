package com.lgames.gamebackend.tetris.service;

import com.lgames.gamebackend.tetris.model.GameState;
import com.lgames.gamebackend.tetris.model.Tetromino;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;

@Service
public class TetrisGameService {
    private static final Random random = new Random();

    public GameState startNewGame() {
        GameState gameState = new GameState();
        gameState.setGameId(UUID.randomUUID().toString());
        gameState.setCurrentPiece(createRandomTetromino());
        gameState.setNextPiece(createRandomTetromino());
        return gameState;
    }

    public GameState movePiece(GameState gameState, String action) {
        if (gameState.getGameStatus() != GameState.GameStatus.PLAYING) {
            return gameState;
        }

        Tetromino currentPiece = gameState.getCurrentPiece();
        Tetromino newPiece = new Tetromino(currentPiece.getType());
        newPiece.setX(currentPiece.getX());
        newPiece.setY(currentPiece.getY());
        newPiece.getShape(); // 初始化形状

        switch (action) {
            case "left":
                newPiece.setX(currentPiece.getX() - 1);
                break;
            case "right":
                newPiece.setX(currentPiece.getX() + 1);
                break;
            case "down":
                newPiece.setY(currentPiece.getY() + 1);
                break;
            case "rotate":
                newPiece.rotate();
                break;
            case "drop":
                while (isValidPosition(gameState.getGrid(), newPiece)) {
                    newPiece.setY(newPiece.getY() + 1);
                }
                newPiece.setY(newPiece.getY() - 1);
                break;
        }

        if (isValidPosition(gameState.getGrid(), newPiece)) {
            gameState.setCurrentPiece(newPiece);
        } else if (action.equals("down") || action.equals("drop")) {
            // 固定方块
            placePiece(gameState.getGrid(), currentPiece);
            // 消除满行
            int linesCleared = clearFullLines(gameState.getGrid());
            gameState.setLinesCleared(gameState.getLinesCleared() + linesCleared);
            // 更新分数
            updateScore(gameState, linesCleared);
            // 更新等级
            updateLevel(gameState);
            // 创建新方块
            gameState.setCurrentPiece(gameState.getNextPiece());
            gameState.setNextPiece(createRandomTetromino());
            // 检查游戏是否结束
            if (!isValidPosition(gameState.getGrid(), gameState.getCurrentPiece())) {
                gameState.setGameStatus(GameState.GameStatus.GAME_OVER);
            }
        }

        return gameState;
    }

    private Tetromino createRandomTetromino() {
        Tetromino.TetrominoType[] types = Tetromino.TetrominoType.values();
        return new Tetromino(types[random.nextInt(types.length)]);
    }

    private boolean isValidPosition(int[][] grid, Tetromino piece) {
        int[][] shape = piece.getShape();
        int x = piece.getX();
        int y = piece.getY();

        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] == 1) {
                    int newX = x + j;
                    int newY = y + i;
                    if (newX < 0 || newX >= 10 || newY >= 20 || (newY >= 0 && grid[newY][newX] != 0)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void placePiece(int[][] grid, Tetromino piece) {
        int[][] shape = piece.getShape();
        int x = piece.getX();
        int y = piece.getY();

        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] == 1 && y + i < 20) {
                    grid[y + i][x + j] = getPieceColor(piece.getType());
                }
            }
        }
    }

    private int getPieceColor(Tetromino.TetrominoType type) {
        switch (type) {
            case I:
                return 1; // 青色
            case O:
                return 2; // 黄色
            case T:
                return 3; // 紫色
            case S:
                return 4; // 绿色
            case Z:
                return 5; // 红色
            case J:
                return 6; // 蓝色
            case L:
                return 7; // 橙色
            default:
                return 0;
        }
    }

    private int clearFullLines(int[][] grid) {
        int linesCleared = 0;
        for (int i = 19; i >= 0; i--) {
            if (isLineFull(grid[i])) {
                // 清除当前行
                for (int j = 0; j < 10; j++) {
                    grid[i][j] = 0;
                }
                linesCleared++;
                // 将上面的行下移
                for (int k = i; k > 0; k--) {
                    System.arraycopy(grid[k - 1], 0, grid[k], 0, 10);
                }
                // 第一行清零
                for (int j = 0; j < 10; j++) {
                    grid[0][j] = 0;
                }
                // 检查当前行是否再次满行（因为上面的行下移了）
                i++;
            }
        }
        return linesCleared;
    }

    private boolean isLineFull(int[] line) {
        for (int cell : line) {
            if (cell == 0) {
                return false;
            }
        }
        return true;
    }

    private void updateScore(GameState gameState, int linesCleared) {
        switch (linesCleared) {
            case 1:
                gameState.setScore(gameState.getScore() + 100 * gameState.getLevel());
                break;
            case 2:
                gameState.setScore(gameState.getScore() + 300 * gameState.getLevel());
                break;
            case 3:
                gameState.setScore(gameState.getScore() + 500 * gameState.getLevel());
                break;
            case 4:
                gameState.setScore(gameState.getScore() + 800 * gameState.getLevel());
                break;
        }
    }

    private void updateLevel(GameState gameState) {
        int newLevel = gameState.getLinesCleared() / 10 + 1;
        if (newLevel > gameState.getLevel()) {
            gameState.setLevel(newLevel);
        }
    }
}