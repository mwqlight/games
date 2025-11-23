package com.lgames.gamebackend.mahjong;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 麻将游戏状态模型
 */
public class MahjongGame {
    public enum GameStatus {
        PLAYING, // 进行中
        HU, // 已胡牌
        LIUJU // 流局
    }

    private String gameId; // 游戏ID
    private List<MahjongTile> tileWall; // 牌墙
    private MahjongPlayer player1; // 玩家1
    private MahjongPlayer player2; // 玩家2
    private List<MahjongTile> discardPool; // 牌池（已打出的牌）
    private MahjongPlayer currentTurnPlayer; // 当前回合玩家
    private GameStatus gameStatus; // 游戏状态
    private MahjongTile lastDiscardedTile; // 最后打出的牌
    private MahjongPlayer lastDiscardedPlayer; // 最后打出牌的玩家
    private long createTime; // 创建时间
    private long lastUpdateTime; // 最后更新时间

    public MahjongGame() {
        this.gameId = UUID.randomUUID().toString();
        this.tileWall = new ArrayList<>();
        this.discardPool = new ArrayList<>();
        this.gameStatus = GameStatus.PLAYING;
        this.createTime = System.currentTimeMillis();
        this.lastUpdateTime = System.currentTimeMillis();
    }

    public MahjongGame(MahjongPlayer player1, MahjongPlayer player2) {
        this();
        this.player1 = player1;
        this.player2 = player2;
    }

    // getter和setter方法
    public String getGameId() { return gameId; }
    public void setGameId(String gameId) { this.gameId = gameId; }
    public List<MahjongTile> getTileWall() { return tileWall; }
    public void setTileWall(List<MahjongTile> tileWall) { this.tileWall = tileWall; }
    public MahjongPlayer getPlayer1() { return player1; }
    public void setPlayer1(MahjongPlayer player1) { this.player1 = player1; }
    public MahjongPlayer getPlayer2() { return player2; }
    public void setPlayer2(MahjongPlayer player2) { this.player2 = player2; }
    public List<MahjongTile> getDiscardPool() { return discardPool; }
    public void setDiscardPool(List<MahjongTile> discardPool) { this.discardPool = discardPool; }
    public MahjongPlayer getCurrentTurnPlayer() { return currentTurnPlayer; }
    public void setCurrentTurnPlayer(MahjongPlayer currentTurnPlayer) { this.currentTurnPlayer = currentTurnPlayer; }
    public GameStatus getGameStatus() { return gameStatus; }
    public void setGameStatus(GameStatus gameStatus) { this.gameStatus = gameStatus; }
    public MahjongTile getLastDiscardedTile() { return lastDiscardedTile; }
    public void setLastDiscardedTile(MahjongTile lastDiscardedTile) { this.lastDiscardedTile = lastDiscardedTile; }
    public MahjongPlayer getLastDiscardedPlayer() { return lastDiscardedPlayer; }
    public void setLastDiscardedPlayer(MahjongPlayer lastDiscardedPlayer) { this.lastDiscardedPlayer = lastDiscardedPlayer; }
    public long getCreateTime() { return createTime; }
    public void setCreateTime(long createTime) { this.createTime = createTime; }
    public long getLastUpdateTime() { return lastUpdateTime; }
    public void setLastUpdateTime(long lastUpdateTime) { this.lastUpdateTime = lastUpdateTime; }

    // 从牌墙抓牌
    public MahjongTile drawTile() {
        if (tileWall.isEmpty()) {
            return null;
        }
        MahjongTile tile = tileWall.remove(tileWall.size() - 1);
        tile.setUsed(true);
        return tile;
    }

    // 打出牌
    public void discardTile(MahjongPlayer player, MahjongTile tile) {
        if (player.removeHandTile(tile)) {
            discardPool.add(tile);
            lastDiscardedTile = tile;
            lastDiscardedPlayer = player;
            switchTurn();
        }
    }

    // 切换回合
    public void switchTurn() {
        if (currentTurnPlayer == player1) {
            currentTurnPlayer = player2;
            player1.setCurrentTurn(false);
            player2.setCurrentTurn(true);
        } else {
            currentTurnPlayer = player1;
            player2.setCurrentTurn(false);
            player1.setCurrentTurn(true);
        }
        lastUpdateTime = System.currentTimeMillis();
    }

    // 获取另一个玩家
    public MahjongPlayer getOtherPlayer(MahjongPlayer player) {
        if (player == player1) {
            return player2;
        } else if (player == player2) {
            return player1;
        }
        return null;
    }

    @Override
    public String toString() {
        return "MahjongGame{" +
                "gameId='" + gameId + '\'' +
                ", tileWallSize=" + tileWall.size() +
                ", player1=" + player1 +
                ", player2=" + player2 +
                ", discardPoolSize=" + discardPool.size() +
                ", currentTurnPlayer=" + (currentTurnPlayer != null ? currentTurnPlayer.getName() : "null") +
                ", gameStatus=" + gameStatus +
                ", lastDiscardedTile=" + lastDiscardedTile +
                '}';
    }
}