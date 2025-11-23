package com.lgames.gamebackend.mahjong;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 麻将游戏API控制器
 */
@RestController
@RequestMapping("/api/mahjong")
public class MahjongGameController {
    @Autowired
    private MahjongGameService mahjongGameService;

    /**
     * 开始新游戏
     * @param playerName 玩家名称
     * @return 游戏状态
     */
    @PostMapping("/start")
    public ResponseEntity<MahjongGame> startNewGame(@RequestParam String playerName) {
        try {
            MahjongGame game = mahjongGameService.startNewGame(playerName);
            return ResponseEntity.ok(game);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * 处理玩家动作
     * @param gameId 游戏ID
     * @param action 玩家动作
     * @return 游戏状态
     */
    @PostMapping("/{gameId}/action")
    public ResponseEntity<MahjongGame> handlePlayerAction(@PathVariable String gameId, @RequestBody PlayerAction action) {
        try {
            MahjongGame game = mahjongGameService.handlePlayerAction(gameId, action);
            return ResponseEntity.ok(game);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(null);
        }
    }

    /**
     * 获取游戏状态
     * @param gameId 游戏ID
     * @return 游戏状态
     */
    @GetMapping("/{gameId}/state")
    public ResponseEntity<MahjongGame> getGameState(@PathVariable String gameId) {
        try {
            MahjongGame game = mahjongGameService.getGameState(gameId);
            return ResponseEntity.ok(game);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}