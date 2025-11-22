package com.lgames.gamebackend.model;

public class Card {
    private String suit; // 花色
    private String rank; // 点数
    private CardType type; // 类型

    public Card(String suit, String rank, CardType type) {
        this.suit = suit;
        this.rank = rank;
        this.type = type;
    }

    // Getters and Setters
    public String getSuit() {
        return suit;
    }

    public void setSuit(String suit) {
        this.suit = suit;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public CardType getType() {
        return type;
    }

    public void setType(CardType type) {
        this.type = type;
    }

    // 牌的类型枚举
    public enum CardType {
        NUMBER, // 数字牌
        FACE, // 人牌 (J, Q, K)
        ACE // A牌
    }
}
