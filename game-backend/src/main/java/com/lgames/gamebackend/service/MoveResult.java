package com.lgames.gamebackend.service;

import com.lgames.gamebackend.model.Move;

public class MoveResult {
    private boolean success;
    private String message;
    private Move move;

    public MoveResult(boolean success, String message, Move move) {
        this.success = success;
        this.message = message;
        this.move = move;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Move getMove() {
        return move;
    }
}
