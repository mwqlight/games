package com.lgames.gamebackend.model;

public class Position {
    private int row;
    private int col;

    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public Position add(int rowOffset, int colOffset) {
        return new Position(row + rowOffset, col + colOffset);
    }

    public Position subtract(int rowOffset, int colOffset) {
        return new Position(row - rowOffset, col - colOffset);
    }

    public Position middle(Position other) {
        return new Position((row + other.row) / 2, (col + other.col) / 2);
    }

    public boolean isDiagonalTo(Position other) {
        return Math.abs(row - other.row) == Math.abs(col - other.col);
    }

    public int distanceTo(Position other) {
        if (!isDiagonalTo(other)) {
            return 0;
        }
        return Math.abs(row - other.row);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Position position = (Position) obj;
        return row == position.row && col == position.col;
    }

    @Override
    public int hashCode() {
        return 31 * row + col;
    }

    @Override
    public String toString() {
        return "(" + row + ", " + col + ")";
    }
}
