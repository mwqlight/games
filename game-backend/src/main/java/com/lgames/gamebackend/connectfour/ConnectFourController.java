package com.lgames.gamebackend.connectfour;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/game/connect-four")
@CrossOrigin(origins = "*")
public class ConnectFourController {
    @Autowired
    private ConnectFourService connectFourService;

    // 开始新游戏
    @PostMapping("/start")
    public GameState startNewGame() {
        return connectFourService.startNewGame();
    }

    // 处理玩家落子
    @PostMapping("/move")
    public GameState makeMove(@RequestBody MoveRequest moveRequest) {
        return connectFourService.makeMove(moveRequest);
    }

    // 处理AI落子
    @PostMapping("/ai-move")
    public GameState makeAIMove(@RequestBody MoveRequest moveRequest) {
        return connectFourService.makeAIMove(moveRequest);
    }

    // 获取游戏状态
    @GetMapping("/{gameId}/state")
    public GameState getGameState(@PathVariable String gameId) {
        return connectFourService.getGameState(gameId);
    }
}
