package com.lgames.gamebackend.mahjong;

/**
 * 玩家动作请求模型
 */
public class PlayerAction {
    public enum ActionType {
        CHUPAI, // 出牌
        CHI, // 吃
        PENG, // 碰
        GANG, // 杠
        HU // 胡
    }

    private ActionType actionType; // 动作类型
    private MahjongTile tile; // 关联的牌
    private int targetPosition; // 目标位置（用于吃牌时选择哪张牌）

    // 无参构造函数（用于JSON序列化）
    public PlayerAction() {}

    public PlayerAction(ActionType actionType, MahjongTile tile) {
        this.actionType = actionType;
        this.tile = tile;
    }

    public PlayerAction(ActionType actionType, MahjongTile tile, int targetPosition) {
        this.actionType = actionType;
        this.tile = tile;
        this.targetPosition = targetPosition;
    }

    // getter和setter方法
    public ActionType getActionType() { return actionType; }
    public void setActionType(ActionType actionType) { this.actionType = actionType; }
    public MahjongTile getTile() { return tile; }
    public void setTile(MahjongTile tile) { this.tile = tile; }
    public int getTargetPosition() { return targetPosition; }
    public void setTargetPosition(int targetPosition) { this.targetPosition = targetPosition; }

    @Override
    public String toString() {
        return "PlayerAction{" +
                "actionType=" + actionType +
                ", tile=" + tile +
                ", targetPosition=" + targetPosition +
                '}';
    }
}