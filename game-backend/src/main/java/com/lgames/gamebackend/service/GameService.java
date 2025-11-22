package com.lgames.gamebackend.service;

import com.lgames.gamebackend.model.Card;
import com.lgames.gamebackend.model.GameStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Service
public class GameService {
    private static final String[] SUITS = {"♠", "♥", "♦", "♣"};
    private static final String[] RANKS = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
    private List<Card> deck;
    private List<Card> playerHand;
    private List<Card> dealerHand;

    // 初始化游戏
    public GameStatus startNewGame() {
        initializeDeck();
        shuffleDeck();
        playerHand = new ArrayList<>();
        dealerHand = new ArrayList<>();

        // 发初始牌：玩家和庄家各一张暗牌
        playerHand.add(dealCard());
        dealerHand.add(dealCard());

        GameStatus gameStatus = new GameStatus();
        gameStatus.setPlayerCards(playerHand);
        gameStatus.setDealerCards(dealerHand);
        gameStatus.setCurrentTurn("player");
        gameStatus.setPlayerBusted(false);
        gameStatus.setDealerBusted(false);
        gameStatus.setResult("游戏进行中");
        gameStatus.setPayoutMultiplier(0.0);

        return gameStatus;
    }

    // 玩家要牌
    public GameStatus hit() {
        GameStatus gameStatus = new GameStatus();
        gameStatus.setPlayerCards(playerHand);
        gameStatus.setDealerCards(dealerHand);
        gameStatus.setCurrentTurn("player");

        // 玩家最多持有5张牌
        if (playerHand.size() >= 5) {
            gameStatus.setResult("玩家已持有5张牌，自动停牌");
            return stand(); // 自动停牌
        }

        // 发牌给玩家
        playerHand.add(dealCard());
        gameStatus.setPlayerCards(playerHand);

        // 检查玩家是否爆牌
        double playerScore = calculateScore(playerHand);
        if (playerScore > 10.30) {
            gameStatus.setPlayerBusted(true);
            gameStatus.setResult("玩家爆牌，庄家获胜");
            gameStatus.setCurrentTurn("gameOver");
            return gameStatus;
        }

        gameStatus.setResult("游戏进行中");
        return gameStatus;
    }

    // 玩家停牌
    public GameStatus stand() {
        GameStatus gameStatus = new GameStatus();
        gameStatus.setPlayerCards(playerHand);
        gameStatus.setDealerCards(dealerHand);
        gameStatus.setCurrentTurn("dealer");

        // 庄家按规则要牌：直到点数大于等于7.5
        while (calculateScore(dealerHand) < 7.5 && dealerHand.size() < 5) {
            dealerHand.add(dealCard());
        }

        gameStatus.setDealerCards(dealerHand);

        // 计算双方点数
        double playerScore = calculateScore(playerHand);
        double dealerScore = calculateScore(dealerHand);

        // 检查庄家是否爆牌
        if (dealerScore > 10.30) {
            gameStatus.setDealerBusted(true);
            gameStatus.setResult("庄家爆牌，玩家获胜");
            gameStatus.setPayoutMultiplier(getPayoutMultiplier(playerHand, playerScore));
        } else {
            // 比较点数
            if (playerScore > dealerScore) {
                gameStatus.setResult("玩家获胜");
                gameStatus.setPayoutMultiplier(getPayoutMultiplier(playerHand, playerScore));
            } else if (playerScore < dealerScore) {
                gameStatus.setResult("庄家获胜");
                gameStatus.setPayoutMultiplier(0.0);
            } else {
                gameStatus.setResult("平局");
                gameStatus.setPayoutMultiplier(1.0); // 平局返还本金
            }
        }

        gameStatus.setCurrentTurn("gameOver");
        return gameStatus;
    }

    // 初始化牌堆
    private void initializeDeck() {
        deck = new ArrayList<>();
        for (String suit : SUITS) {
            for (String rank : RANKS) {
                Card.CardType type;
                if (rank.equals("A")) {
                    type = Card.CardType.ACE;
                } else if (rank.equals("J") || rank.equals("Q") || rank.equals("K")) {
                    type = Card.CardType.FACE;
                } else {
                    type = Card.CardType.NUMBER;
                }
                deck.add(new Card(suit, rank, type));
            }
        }
    }

    // 洗牌
    private void shuffleDeck() {
        Collections.shuffle(deck, new Random());
    }

    // 发牌
    private Card dealCard() {
        if (deck.isEmpty()) {
            initializeDeck();
            shuffleDeck();
        }
        return deck.remove(0);
    }

    // 计算手牌点数
    public double calculateScore(List<Card> hand) {
        double score = 0.0;
        for (Card card : hand) {
            switch (card.getType()) {
                case ACE:
                    score += 1.0;
                    break;
                case NUMBER:
                    score += Double.parseDouble(card.getRank());
                    break;
                case FACE:
                    score += 0.5;
                    break;
            }
        }
        return score;
    }

    // 计算赔率
    private double getPayoutMultiplier(List<Card> hand, double score) {
        int cardCount = hand.size();

        // 检查特殊牌型
        if (cardCount == 5) {
            if (score == 10.30) {
                return 5.0; // 天王，5倍赔率
            } else if (isAllFaceCards(hand)) {
                return 4.0; // 人五小，4倍赔率
            } else if (score < 10.30) {
                return 3.0; // 五小，3倍赔率
            }
        }

        if (score == 10.30) {
            return 2.0; // 十点半，2倍赔率
        }

        return 1.0; // 普通牌型，1倍赔率
    }

    // 检查是否全是人牌
    private boolean isAllFaceCards(List<Card> hand) {
        for (Card card : hand) {
            if (card.getType() != Card.CardType.FACE) {
                return false;
            }
        }
        return true;
    }
}
