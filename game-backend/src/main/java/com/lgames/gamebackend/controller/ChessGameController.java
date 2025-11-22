package com.lgames.gamebackend.controller;

import com.lgames.gamebackend.model.GameState;
import com.lgames.gamebackend.service.ChessGameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/game")
@CrossOrigin(origins = "*") // 允许跨域请求
public class ChessGameController {
    @Autowired
    private ChessGameService gameService;

    // 开始新游戏
    @PostMapping("/start")
    public GameState startNewGame() {
        return gameService.startNewGame();
    }

    // 获取当前游戏状态
    @GetMapping("/state")
    public GameState getCurrentGameState() {
        return gameService.getCurrentGameState();
    }

    // 处理玩家走子
    @PostMapping("/move")
    public GameState makeMove(@RequestBody Move move) {
        return gameService.makeMove(move.getFromX(), move.getFromY(), move.getToX(), move.getToY());
    }
}
