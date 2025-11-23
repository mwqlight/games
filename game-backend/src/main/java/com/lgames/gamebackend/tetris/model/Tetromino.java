package com.lgames.gamebackend.tetris.model;

import java.util.Arrays;

public class Tetromino {
    private TetrominoType type;
    private int[][] shape;
    private int x;
    private int y;
    private int rotation;

    public enum TetrominoType {
        I, O, T, S, Z, J, L
    }

    public Tetromino(TetrominoType type) {
        this.type = type;
        this.shape = getInitialShape(type);
        this.x = 3;
        this.y = 0;
        this.rotation = 0;
    }

    private int[][] getInitialShape(TetrominoType type) {
        switch (type) {
            case I:
                return new int[][]{{0, 0, 0, 0}, {1, 1, 1, 1}, {0, 0, 0, 0}, {0, 0, 0, 0}};
            case O:
                return new int[][]{{1, 1}, {1, 1}};
            case T:
                return new int[][]{{0, 1, 0}, {1, 1, 1}, {0, 0, 0}};
            case S:
                return new int[][]{{0, 1, 1}, {1, 1, 0}, {0, 0, 0}};
            case Z:
                return new int[][]{{1, 1, 0}, {0, 1, 1}, {0, 0, 0}};
            case J:
                return new int[][]{{1, 0, 0}, {1, 1, 1}, {0, 0, 0}};
            case L:
                return new int[][]{{0, 0, 1}, {1, 1, 1}, {0, 0, 0}};
            default:
                return new int[][]{{0, 0, 0}, {0, 0, 0}, {0, 0, 0}};
        }
    }

    public void rotate() {
        int size = shape.length;
        int[][] rotated = new int[size][size];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                rotated[j][size - 1 - i] = shape[i][j];
            }
        }

        shape = rotated;
        rotation = (rotation + 1) % 4;
    }

    public TetrominoType getType() {
        return type;
    }

    public int[][] getShape() {
        return shape;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getRotation() {
        return rotation;
    }

    @Override
    public String toString() {
        return "Tetromino{" +
                "type=" + type +
                ", shape=" + Arrays.deepToString(shape) +
                ", x=" + x +
                ", y=" + y +
                ", rotation=" + rotation +
                '}';
    }
}