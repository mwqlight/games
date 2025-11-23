package com.lgames.gamebackend.minesweeper.service;

import com.lgames.gamebackend.minesweeper.enums.Difficulty;
import com.lgames.gamebackend.minesweeper.enums.GameStatus;
import com.lgames.gamebackend.minesweeper.model.Cell;
import com.lgames.gamebackend.minesweeper.model.GameState;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Service
public class MinesweeperService {
    private final Map<String, GameState> games = new HashMap<>();
    private final Random random = new Random();

    public GameState createNewGame(Difficulty difficulty) {
        String gameId = UUID.randomUUID().toString();
        int rows = difficulty.getRows();
        int cols = difficulty.getCols();
        int mineCount = difficulty.getMines();

        Cell[][] board = new Cell[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                board[i][j] = new Cell(false, false, false, 0);
            }
        }

        GameState gameState = new GameState(gameId, rows, cols, mineCount, board, GameStatus.PLAYING, mineCount);
        games.put(gameId, gameState);
        return gameState;
    }

    public GameState revealCell(String gameId, int x, int y) {
        GameState gameState = games.get(gameId);
        if (gameState == null || gameState.getStatus() != GameStatus.PLAYING) {
            return gameState;
        }

        Cell[][] board = gameState.getBoard();
        int rows = gameState.getRows();
        int cols = gameState.getCols();

        if (x < 0 || x >= rows || y < 0 || y >= cols) {
            return gameState;
        }

        Cell cell = board[x][y];
        if (cell.isRevealed() || cell.isFlagged()) {
            return gameState;
        }

        // 首次点击，确保不触雷并布雷
        if (isFirstMove(gameState)) {
            placeMines(gameState, x, y);
            calculateAdjacentMines(gameState);
        }

        // 揭开格子
        revealCellRecursive(board, rows, cols, x, y);

        // 检查游戏状态
        checkGameStatus(gameState);

        return gameState;
    }

    public GameState flagCell(String gameId, int x, int y) {
        GameState gameState = games.get(gameId);
        if (gameState == null || gameState.getStatus() != GameStatus.PLAYING) {
            return gameState;
        }

        Cell[][] board = gameState.getBoard();
        int rows = gameState.getRows();
        int cols = gameState.getCols();

        if (x < 0 || x >= rows || y < 0 || y >= cols) {
            return gameState;
        }

        Cell cell = board[x][y];
        if (cell.isRevealed()) {
            return gameState;
        }

        if (cell.isFlagged()) {
            cell.setFlagged(false);
            gameState.setFlagsRemaining(gameState.getFlagsRemaining() + 1);
        } else if (gameState.getFlagsRemaining() > 0) {
            cell.setFlagged(true);
            gameState.setFlagsRemaining(gameState.getFlagsRemaining() - 1);
        }

        // 检查游戏状态
        checkGameStatus(gameState);

        return gameState;
    }

    public GameState getGameState(String gameId) {
        return games.get(gameId);
    }

    private boolean isFirstMove(GameState gameState) {
        Cell[][] board = gameState.getBoard();
        for (int i = 0; i < gameState.getRows(); i++) {
            for (int j = 0; j < gameState.getCols(); j++) {
                if (board[i][j].isMine()) {
                    return false;
                }
            }
        }
        return true;
    }

    private void placeMines(GameState gameState, int firstClickX, int firstClickY) {
        Cell[][] board = gameState.getBoard();
        int rows = gameState.getRows();
        int cols = gameState.getCols();
        int mineCount = gameState.getMineCount();

        int placedMines = 0;
        while (placedMines < mineCount) {
            int x = random.nextInt(rows);
            int y = random.nextInt(cols);

            // 确保不在首次点击位置及其周围布雷
            if (Math.abs(x - firstClickX) <= 1 && Math.abs(y - firstClickY) <= 1) {
                continue;
            }

            if (!board[x][y].isMine()) {
                board[x][y].setMine(true);
                placedMines++;
            }
        }
    }

    private void calculateAdjacentMines(GameState gameState) {
        Cell[][] board = gameState.getBoard();
        int rows = gameState.getRows();
        int cols = gameState.getCols();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (board[i][j].isMine()) {
                    continue;
                }

                int count = 0;
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        if (dx == 0 && dy == 0) {
                            continue;
                        }

                        int nx = i + dx;
                        int ny = j + dy;
                        if (nx >= 0 && nx < rows && ny >= 0 && ny < cols && board[nx][ny].isMine()) {
                            count++;
                        }
                    }
                }
                board[i][j].setAdjacentMines(count);
            }
        }
    }

    private void revealCellRecursive(Cell[][] board, int rows, int cols, int x, int y) {
        if (x < 0 || x >= rows || y < 0 || y >= cols) {
            return;
        }

        Cell cell = board[x][y];
        if (cell.isRevealed() || cell.isFlagged()) {
            return;
        }

        cell.setRevealed(true);

        if (cell.isMine()) {
            // 触雷，只揭示当前点击的雷
            return;
        }

        if (cell.getAdjacentMines() == 0) {
            // 空白格，递归揭开周围格子
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx == 0 && dy == 0) {
                        continue;
                    }
                    revealCellRecursive(board, rows, cols, x + dx, y + dy);
                }
            }
        }
    }

    private void checkGameStatus(GameState gameState) {
        Cell[][] board = gameState.getBoard();
        int rows = gameState.getRows();
        int cols = gameState.getCols();
        int mineCount = gameState.getMineCount();

        // 检查是否触雷
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (board[i][j].isMine() && board[i][j].isRevealed()) {
                    gameState.setStatus(GameStatus.LOST);
                    return;
                }
            }
        }

        // 检查是否所有非地雷格子都已揭开
        int revealedNonMines = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (!board[i][j].isMine() && board[i][j].isRevealed()) {
                    revealedNonMines++;
                }
            }
        }

        if (revealedNonMines == rows * cols - mineCount) {
            gameState.setStatus(GameStatus.WON);
            // 标记所有地雷
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    if (board[i][j].isMine()) {
                        board[i][j].setFlagged(true);
                    }
                }
            }
        }
    }
}
