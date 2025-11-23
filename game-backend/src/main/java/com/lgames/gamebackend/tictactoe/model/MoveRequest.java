package com.lgames.gamebackend.tictactoe.model;

import lombok.Data;

@Data
public class MoveRequest {
    private String gameId;
    private int row;
    private int col;
}
