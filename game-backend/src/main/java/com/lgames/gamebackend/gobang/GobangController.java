package com.lgames.gamebackend.gobang;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/gobang")
@CrossOrigin(origins = "*", maxAge = 3600)
public class GobangController {
    
    @Autowired
    private GobangService gobangService;
    
    // 重置游戏
    @PostMapping("/reset")
    public GameState resetGame() {
        return gobangService.resetGame();
    }
    
    // 玩家落子
    @PostMapping("/move")
    public GameState makeMove(@RequestBody MoveRequest moveRequest) {
        return gobangService.makeMove(moveRequest.getRow(), moveRequest.getCol());
    }
    
    // 获取当前游戏状态
    @GetMapping("/state")
    public GameState getGameState() {
        return gobangService.getGameState();
    }
    
    // 内部类，用于接收前端的落子请求
    public static class MoveRequest {
        private int row;
        private int col;
        
        // getter和setter
        public int getRow() {
            return row;
        }
        
        public void setRow(int row) {
            this.row = row;
        }
        
        public int getCol() {
            return col;
        }
        
        public void setCol(int col) {
            this.col = col;
        }
    }
}
