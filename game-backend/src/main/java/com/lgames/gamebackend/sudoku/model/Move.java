package com.lgames.gamebackend.sudoku.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Move {
    private int row;
    private int col;
    private int number;
}
