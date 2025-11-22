package com.lgames.gamebackend.model;

public class ChessPiece {
    private String type; // 如"rook"（车）、"horse"（马）
    private String color; // "red" 或 "black"
    private int x, y;     // 棋盘坐标 (0-8, 0-9)

    // 构造函数
    public ChessPiece(String type, String color, int x, int y) {
        this.type = type;
        this.color = color;
        this.x = x;
        this.y = y;
    }

    // getter和setter方法
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
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

    // 复制棋子
    public ChessPiece copy() {
        return new ChessPiece(this.type, this.color, this.x, this.y);
    }
}