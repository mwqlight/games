package com.lgames.gamebackend.snake;

import lombok.Data;

@Data
public class MoveRequest {
    private String gameId;
    private Direction direction;
}
