package com.lgames.gamebackend.mahjong;

import java.util.ArrayList;
import java.util.List;

/**
 * 麻将玩家模型
 */
public class MahjongPlayer {
    public enum PlayerType {
        HUMAN, // 人类玩家
        AI // 电脑玩家
    }

    private String id; // 玩家ID
    private String name; // 玩家名称
    private PlayerType type; // 玩家类型
    private List<MahjongTile> handTiles; // 手牌
    private List<MahjongTile> exposedTiles; // 明牌（吃、碰、杠的牌）
    private int score; // 分数
    private boolean isBanker; // 是否为庄家
    private boolean isCurrentTurn; // 是否为当前回合

    public MahjongPlayer(String id, String name, PlayerType type) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.handTiles = new ArrayList<>();
        this.exposedTiles = new ArrayList<>();
        this.score = 0;
        this.isBanker = false;
        this.isCurrentTurn = false;
    }

    // getter和setter方法
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public PlayerType getType() { return type; }
    public void setType(PlayerType type) { this.type = type; }
    public List<MahjongTile> getHandTiles() { return handTiles; }
    public void setHandTiles(List<MahjongTile> handTiles) { this.handTiles = handTiles; }
    public List<MahjongTile> getExposedTiles() { return exposedTiles; }
    public void setExposedTiles(List<MahjongTile> exposedTiles) { this.exposedTiles = exposedTiles; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public boolean isBanker() { return isBanker; }
    public void setBanker(boolean banker) { isBanker = banker; }
    public boolean isCurrentTurn() { return isCurrentTurn; }
    public void setCurrentTurn(boolean currentTurn) { isCurrentTurn = currentTurn; }

    // 添加手牌
    public void addHandTile(MahjongTile tile) {
        handTiles.add(tile);
    }

    // 移除手牌
    public boolean removeHandTile(MahjongTile tile) {
        return handTiles.remove(tile);
    }

    // 添加明牌
    public void addExposedTile(MahjongTile tile) {
        exposedTiles.add(tile);
    }

    // 清空手牌和明牌
    public void clearTiles() {
        handTiles.clear();
        exposedTiles.clear();
    }

    @Override
    public String toString() {
        return "MahjongPlayer{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", handTilesSize=" + handTiles.size() +
                ", exposedTilesSize=" + exposedTiles.size() +
                ", score=" + score +
                ", isBanker=" + isBanker +
                ", isCurrentTurn=" + isCurrentTurn +
                '}';
    }
}