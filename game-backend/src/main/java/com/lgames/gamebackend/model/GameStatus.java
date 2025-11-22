package com.lgames.gamebackend.model;

import java.util.List;

public class GameStatus {
    private List<Card> playerCards; // 玩家手牌
    private List<Card> dealerCards; // 庄家手牌
    private String currentTurn; // 当前回合状态
    private boolean playerBusted; // 玩家是否爆牌
    private boolean dealerBusted; // 庄家是否爆牌
    private String result; // 游戏结果
    private double payoutMultiplier; // 赔率

    // Getters and Setters
    public List<Card> getPlayerCards() {
        return playerCards;
    }

    public void setPlayerCards(List<Card> playerCards) {
        this.playerCards = playerCards;
    }

    public List<Card> getDealerCards() {
        return dealerCards;
    }

    public void setDealerCards(List<Card> dealerCards) {
        this.dealerCards = dealerCards;
    }

    public String getCurrentTurn() {
        return currentTurn;
    }

    public void setCurrentTurn(String currentTurn) {
        this.currentTurn = currentTurn;
    }

    public boolean isPlayerBusted() {
        return playerBusted;
    }

    public void setPlayerBusted(boolean playerBusted) {
        this.playerBusted = playerBusted;
    }

    public boolean isDealerBusted() {
        return dealerBusted;
    }

    public void setDealerBusted(boolean dealerBusted) {
        this.dealerBusted = dealerBusted;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public double getPayoutMultiplier() {
        return payoutMultiplier;
    }

    public void setPayoutMultiplier(double payoutMultiplier) {
        this.payoutMultiplier = payoutMultiplier;
    }
}
