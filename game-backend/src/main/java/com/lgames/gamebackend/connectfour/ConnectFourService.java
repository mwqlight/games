package com.lgames.gamebackend.connectfour;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Service
public class ConnectFourService {
    private final Map<String, GameState> games = new HashMap<>();
    private final Random random = new Random();

    // 开始新游戏
    public GameState startNewGame() {
        GameState gameState = new GameState();
        String gameId = UUID.randomUUID().toString();
        gameState.setGameId(gameId);
        games.put(gameId, gameState);
        return gameState;
    }

    // 获取游戏状态
    public GameState getGameState(String gameId) {
        return games.get(gameId);
    }

    // 处理玩家落子
    public GameState makeMove(MoveRequest moveRequest) {
        GameState gameState = games.get(moveRequest.getGameId());
        if (gameState == null || gameState.getGameStatus() != GameState.GameStatus.PLAYING || gameState.getCurrentPlayer() != 1) {
            return gameState;
        }

        int column = moveRequest.getColumn();
        if (column < 0 || column >= 7) {
            return gameState;
        }

        // 找到该列最低的空位
        int row = -1;
        for (int i = 5; i >= 0; i--) {
            if (gameState.getBoard()[i][column] == 0) {
                row = i;
                break;
            }
        }

        if (row == -1) {
            // 该列已满
            return gameState;
        }

        // 落子
        gameState.getBoard()[row][column] = gameState.getCurrentPlayer();
        gameState.setLastMoveRow(row);
        gameState.setLastMoveColumn(column);

        // 检查胜负
        if (checkWin(gameState.getBoard(), row, column, gameState.getCurrentPlayer())) {
            if (gameState.getCurrentPlayer() == 1) {
                gameState.setGameStatus(GameState.GameStatus.PLAYER_WON);
            } else {
                gameState.setGameStatus(GameState.GameStatus.AI_WON);
            }
        } else if (checkDraw(gameState.getBoard())) {
            gameState.setGameStatus(GameState.GameStatus.DRAW);
        } else {
            // 切换玩家
            gameState.setCurrentPlayer(gameState.getCurrentPlayer() == 1 ? 2 : 1);
        }

        return gameState;
    }

    // 处理AI落子
    public GameState makeAIMove(MoveRequest moveRequest) {
        GameState gameState = games.get(moveRequest.getGameId());
        if (gameState == null || gameState.getGameStatus() != GameState.GameStatus.PLAYING || gameState.getCurrentPlayer() != 2) {
            return gameState;
        }

        // AI落子
        makeAIMove(gameState);

        return gameState;
    }

    // AI落子逻辑
    private void makeAIMove(GameState gameState) {
        int column = findBestMove(gameState.getBoard());
        if (column == -1) {
            // 没有可用列，游戏结束
            gameState.setGameStatus(GameState.GameStatus.DRAW);
            return;
        }

        // 找到该列最低的空位
        int row = -1;
        for (int i = 5; i >= 0; i--) {
            if (gameState.getBoard()[i][column] == 0) {
                row = i;
                break;
            }
        }

        // 落子
        gameState.getBoard()[row][column] = 2;
        gameState.setLastMoveRow(row);
        gameState.setLastMoveColumn(column);

        // 检查胜负
        if (checkWin(gameState.getBoard(), row, column, 2)) {
            gameState.setGameStatus(GameState.GameStatus.AI_WON);
        } else if (checkDraw(gameState.getBoard())) {
            gameState.setGameStatus(GameState.GameStatus.DRAW);
        } else {
            // 切换回玩家
            gameState.setCurrentPlayer(1);
        }
    }

    // 找到最佳落子位置
    private int findBestMove(int[][] board) {
        // 1. 检查AI是否有一步制胜的可能
        for (int column = 0; column < 7; column++) {
            int row = getEmptyRow(board, column);
            if (row != -1) {
                board[row][column] = 2;
                if (checkWin(board, row, column, 2)) {
                    board[row][column] = 0;
                    return column;
                }
                board[row][column] = 0;
            }
        }

        // 2. 检查玩家是否有一步制胜的可能，若有则阻挡
        for (int column = 0; column < 7; column++) {
            int row = getEmptyRow(board, column);
            if (row != -1) {
                board[row][column] = 1;
                if (checkWin(board, row, column, 1)) {
                    board[row][column] = 0;
                    return column;
                }
                board[row][column] = 0;
            }
        }

        // 3. 优先选择中间几列
        for (int column = 3; column >= 0; column--) {
            if (getEmptyRow(board, column) != -1) {
                return column;
            }
            if (getEmptyRow(board, 6 - column) != -1) {
                return 6 - column;
            }
        }

        // 4. 随机落子
        int[] availableColumns = new int[7];
        int count = 0;
        for (int column = 0; column < 7; column++) {
            if (getEmptyRow(board, column) != -1) {
                availableColumns[count++] = column;
            }
        }

        if (count == 0) {
            return -1;
        }

        return availableColumns[random.nextInt(count)];
    }

    // 获取列中的空行
    private int getEmptyRow(int[][] board, int column) {
        for (int i = 5; i >= 0; i--) {
            if (board[i][column] == 0) {
                return i;
            }
        }
        return -1;
    }

    // 检查是否获胜
    private boolean checkWin(int[][] board, int row, int column, int player) {
        // 检查水平方向
        int count = 1;
        // 向左
        for (int c = column - 1; c >= 0 && board[row][c] == player; c--) {
            count++;
        }
        // 向右
        for (int c = column + 1; c < 7 && board[row][c] == player; c++) {
            count++;
        }
        if (count >= 4) {
            return true;
        }

        // 检查垂直方向
        count = 1;
        // 向下
        for (int r = row + 1; r < 6 && board[r][column] == player; r++) {
            count++;
        }
        if (count >= 4) {
            return true;
        }

        // 检查对角线1（左上-右下）
        count = 1;
        // 左上
        for (int r = row - 1, c = column - 1; r >= 0 && c >= 0 && board[r][c] == player; r--, c--) {
            count++;
        }
        // 右下
        for (int r = row + 1, c = column + 1; r < 6 && c < 7 && board[r][c] == player; r++, c++) {
            count++;
        }
        if (count >= 4) {
            return true;
        }

        // 检查对角线2（右上-左下）
        count = 1;
        // 右上
        for (int r = row - 1, c = column + 1; r >= 0 && c < 7 && board[r][c] == player; r--, c++) {
            count++;
        }
        // 左下
        for (int r = row + 1, c = column - 1; r < 6 && c >= 0 && board[r][c] == player; r++, c--) {
            count++;
        }
        if (count >= 4) {
            return true;
        }

        return false;
    }

    // 检查是否平局
    private boolean checkDraw(int[][] board) {
        for (int column = 0; column < 7; column++) {
            if (board[0][column] == 0) {
                return false;
            }
        }
        return true;
    }
}
