package com.lgames.gamebackend.chess.controller;

import com.lgames.gamebackend.chess.model.GameState;
import com.lgames.gamebackend.chess.model.Move;
import com.lgames.gamebackend.chess.service.ChessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/game")
public class ChessController {
    private final ChessService chessService;

    @Autowired
    public ChessController(ChessService chessService) {
        this.chessService = chessService;
    }

    // 开始新游戏
    @PostMapping("/start")
    public GameState startGame() {
        chessService.initializeBoard();
        return chessService.getGameState();
    }

    // 处理玩家走子
    @PostMapping("/move")
    public GameState makeMove(@RequestBody Move move) {
        return chessService.makeMove(move);
    }

    // 获取当前棋盘状态
    @GetMapping("/board")
    public GameState getBoard() {
        return chessService.getGameState();
    }
}