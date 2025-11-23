package com.lgames.gamebackend.tictactoe.service;

import com.lgames.gamebackend.tictactoe.model.GameState;
import com.lgames.gamebackend.tictactoe.model.MoveRequest;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TicTacToeService {
    private Map<String, GameState> games = new HashMap<>();
    private Random random = new Random();

    public GameState startNewGame() {
        String gameId = UUID.randomUUID().toString();
        GameState gameState = new GameState();
        gameState.setGameId(gameId);
        gameState.setBoard(new String[3][3]);
        gameState.setCurrentPlayer("X");
        gameState.setGameStatus(GameState.GameStatus.PLAYING);
        games.put(gameId, gameState);
        return gameState;
    }

    public GameState makeMove(MoveRequest moveRequest) {
        GameState gameState = games.get(moveRequest.getGameId());
        if (gameState == null || gameState.getGameStatus() != GameState.GameStatus.PLAYING) {
            throw new IllegalArgumentException("Invalid game state");
        }

        int row = moveRequest.getRow();
        int col = moveRequest.getCol();

        if (row < 0 || row >= 3 || col < 0 || col >= 3) {
            throw new IllegalArgumentException("Invalid move position");
        }

        if (gameState.getBoard()[row][col] != null) {
            throw new IllegalArgumentException("Position already occupied");
        }

        gameState.getBoard()[row][col] = gameState.getCurrentPlayer();

        if (checkWin(gameState, row, col)) {
            gameState.setGameStatus(gameState.getCurrentPlayer().equals("X") ? GameState.GameStatus.X_WON : GameState.GameStatus.O_WON);
        } else if (checkDraw(gameState)) {
            gameState.setGameStatus(GameState.GameStatus.DRAW);
        } else {
            gameState.setCurrentPlayer(gameState.getCurrentPlayer().equals("X") ? "O" : "X");
            if (gameState.getCurrentPlayer().equals("O")) {
                makeAIMove(gameState);
            }
        }

        return gameState;
    }

    public GameState getGameState(String gameId) {
        GameState gameState = games.get(gameId);
        if (gameState == null) {
            throw new IllegalArgumentException("Game not found");
        }
        return gameState;
    }

    private boolean checkWin(GameState gameState, int row, int col) {
        String player = gameState.getBoard()[row][col];
        String[][] board = gameState.getBoard();

        // 检查行
        if (board[row][0] != null && board[row][0].equals(player) &&
            board[row][1] != null && board[row][1].equals(player) &&
            board[row][2] != null && board[row][2].equals(player)) {
            gameState.setWinningLine(new int[][]{{row, 0}, {row, 1}, {row, 2}});
            return true;
        }

        // 检查列
        if (board[0][col] != null && board[0][col].equals(player) &&
            board[1][col] != null && board[1][col].equals(player) &&
            board[2][col] != null && board[2][col].equals(player)) {
            gameState.setWinningLine(new int[][]{{0, col}, {1, col}, {2, col}});
            return true;
        }

        // 检查对角线（从左上到右下）
        if (row == col &&
            board[0][0] != null && board[0][0].equals(player) &&
            board[1][1] != null && board[1][1].equals(player) &&
            board[2][2] != null && board[2][2].equals(player)) {
            gameState.setWinningLine(new int[][]{{0, 0}, {1, 1}, {2, 2}});
            return true;
        }

        // 检查对角线（从右上到左下）
        if (row + col == 2 &&
            board[0][2] != null && board[0][2].equals(player) &&
            board[1][1] != null && board[1][1].equals(player) &&
            board[2][0] != null && board[2][0].equals(player)) {
            gameState.setWinningLine(new int[][]{{0, 2}, {1, 1}, {2, 0}});
            return true;
        }

        return false;
    }

    private boolean checkDraw(GameState gameState) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (gameState.getBoard()[i][j] == null) {
                    return false;
                }
            }
        }
        return true;
    }

    private void makeAIMove(GameState gameState) {
        String[][] board = gameState.getBoard();

        // 1. 检查是否有一步制胜的可能
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == null) {
                    board[i][j] = "O";
                    if (checkWin(gameState, i, j)) {
                        gameState.setGameStatus(GameState.GameStatus.O_WON);
                        return;
                    }
                    board[i][j] = null;
                }
            }
        }

        // 2. 检查是否需要阻挡玩家的制胜步
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == null) {
                    board[i][j] = "X";
                    if (checkWin(gameState, i, j)) {
                        board[i][j] = "O";
                        gameState.setCurrentPlayer("X");
                        return;
                    }
                    board[i][j] = null;
                }
            }
        }

        // 3. 占据中心
        if (board[1][1] == null) {
            board[1][1] = "O";
            gameState.setCurrentPlayer("X");
            return;
        }

        // 4. 占据角落
        List<int[]> corners = Arrays.asList(
                new int[]{0, 0}, new int[]{0, 2},
                new int[]{2, 0}, new int[]{2, 2}
        );
        Collections.shuffle(corners);
        for (int[] corner : corners) {
            if (board[corner[0]][corner[1]] == null) {
                board[corner[0]][corner[1]] = "O";
                gameState.setCurrentPlayer("X");
                return;
            }
        }

        // 5. 随机占据边格
        List<int[]> edges = Arrays.asList(
                new int[]{0, 1}, new int[]{1, 0},
                new int[]{1, 2}, new int[]{2, 1}
        );
        Collections.shuffle(edges);
        for (int[] edge : edges) {
            if (board[edge[0]][edge[1]] == null) {
                board[edge[0]][edge[1]] = "O";
                gameState.setCurrentPlayer("X");
                return;
            }
        }
    }
}
