package com.lgames.gamebackend.model;

import java.util.ArrayList;
import java.util.List;

public class Board {
    private static final int SIZE = 8;
    private Piece[][] grid;

    public Board() {
        initializeBoard();
    }

    private void initializeBoard() {
        grid = new Piece[SIZE][SIZE];
        
        // 放置红方棋子
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < SIZE; col++) {
                if ((row + col) % 2 == 1) {
                    grid[row][col] = new Piece(PlayerColor.RED, new Position(row, col));
                }
            }
        }
        
        // 放置黑方棋子
        for (int row = 5; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if ((row + col) % 2 == 1) {
                    grid[row][col] = new Piece(PlayerColor.BLACK, new Position(row, col));
                }
            }
        }
    }

    public Piece getPieceAt(Position position) {
        if (isValidPosition(position)) {
            return grid[position.getRow()][position.getCol()];
        }
        return null;
    }

    public void movePiece(Position from, Position to) {
        if (isValidPosition(from) && isValidPosition(to)) {
            Piece piece = grid[from.getRow()][from.getCol()];
            if (piece != null) {
                grid[from.getRow()][from.getCol()] = null;
                piece.setPosition(to);
                grid[to.getRow()][to.getCol()] = piece;
            }
        }
    }

    public void removePiece(Position position) {
        if (isValidPosition(position)) {
            grid[position.getRow()][position.getCol()] = null;
        }
    }

    public boolean isValidPosition(Position position) {
        int row = position.getRow();
        int col = position.getCol();
        return row >= 0 && row < SIZE && col >= 0 && col < SIZE;
    }

    public int getSize() {
        return SIZE;
    }

    public Piece[][] getGrid() {
        return grid;
    }

    public List<Piece> getPiecesByColor(PlayerColor color) {
        List<Piece> pieces = new ArrayList<>();
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                Piece piece = grid[row][col];
                if (piece != null && piece.getColor() == color) {
                    pieces.add(piece);
                }
            }
        }
        return pieces;
    }

    public Board copy() {
        Board newBoard = new Board();
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                Piece piece = grid[row][col];
                if (piece != null) {
                    newBoard.grid[row][col] = new Piece(piece.getColor(), new Position(row, col));
                } else {
                    newBoard.grid[row][col] = null;
                }
            }
        }
        return newBoard;
    }
}
