package com.lgames.gamebackend.controller;

import com.lgames.gamebackend.model.GameStatus;
import com.lgames.gamebackend.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/game")
public class GameController {
    private final GameService gameService;

    @Autowired
    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    // 开始新游戏
    @PostMapping("/start")
    public GameStatus startNewGame() {
        return gameService.startNewGame();
    }

    // 玩家要牌
    @PostMapping("/hit")
    public GameStatus hit() {
        return gameService.hit();
    }

    // 玩家停牌
    @PostMapping("/stand")
    public GameStatus stand() {
        return gameService.stand();
    }
}
