package com.lgames.gamebackend.service;

import com.lgames.gamebackend.model.Card;
import com.lgames.gamebackend.model.GameState;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Service
public class GameService {
    private GameState gameState;
    private final Random random = new Random();

    public GameState startNewGame() {
        // 创建一副标准扑克牌
        List<Card> deck = createDeck();
        
        // 随机移除一张牌作为乌龟牌
        Card turtleCard = deck.remove(random.nextInt(deck.size()));
        
        // 洗牌
        Collections.shuffle(deck);
        
        // 平均发给玩家和AI
        List<Card> playerCards = new ArrayList<>(deck.subList(0, deck.size() / 2));
        List<Card> aiCards = new ArrayList<>(deck.subList(deck.size() / 2, deck.size()));
        
        // 处理初始配对
        removePairs(playerCards);
        removePairs(aiCards);
        
        // 设置玩家的牌为正面朝上
        playerCards.forEach(card -> card.setFaceUp(true));
        
        // 初始化游戏状态
        gameState = new GameState(playerCards, aiCards, GameState.Turn.PLAYER, GameState.GameStatus.IN_PROGRESS, turtleCard);
        
        return gameState;
    }

    public GameState playerDraw() {
        if (gameState == null || gameState.getGameStatus() == GameState.GameStatus.FINISHED) {
            throw new IllegalStateException("游戏未开始或已结束");
        }
        
        if (gameState.getCurrentTurn() != GameState.Turn.PLAYER) {
            throw new IllegalStateException("不是玩家回合");
        }
        
        // 玩家从AI的手牌中随机抽取一张
        List<Card> aiCards = gameState.getAiCards();
        if (aiCards.isEmpty()) {
            // AI手牌为空，玩家获胜
            gameState.setGameStatus(GameState.GameStatus.FINISHED);
            gameState.setWinner("玩家");
            return gameState;
        }
        
        Card drawnCard = aiCards.remove(random.nextInt(aiCards.size()));
        drawnCard.setFaceUp(true);
        gameState.getPlayerCards().add(drawnCard);
        
        // 检查配对
        removePairs(gameState.getPlayerCards());
        
        // 检查游戏是否结束
        checkGameEnd();
        
        if (gameState.getGameStatus() == GameState.GameStatus.IN_PROGRESS) {
            // 切换到AI回合
            gameState.setCurrentTurn(GameState.Turn.AI);
            // AI自动抽牌
            aiDraw();
        }
        
        return gameState;
    }

    public GameState playerDrawFromAI(int index) {
        if (gameState == null || gameState.getGameStatus() == GameState.GameStatus.FINISHED) {
            throw new IllegalStateException("游戏未开始或已结束");
        }
        
        if (gameState.getCurrentTurn() != GameState.Turn.PLAYER) {
            throw new IllegalStateException("不是玩家回合");
        }
        
        // 玩家从AI的手牌中指定位置抽取一张
        List<Card> aiCards = gameState.getAiCards();
        if (aiCards.isEmpty()) {
            // AI手牌为空，玩家获胜
            gameState.setGameStatus(GameState.GameStatus.FINISHED);
            gameState.setWinner("玩家");
            return gameState;
        }
        
        if (index < 0 || index >= aiCards.size()) {
            throw new IllegalStateException("无效的抽牌位置");
        }
        
        Card drawnCard = aiCards.remove(index);
        drawnCard.setFaceUp(true);
        gameState.getPlayerCards().add(drawnCard);
        
        // 检查配对
        removePairs(gameState.getPlayerCards());
        
        // 检查游戏是否结束
        checkGameEnd();
        
        if (gameState.getGameStatus() == GameState.GameStatus.IN_PROGRESS) {
            // 切换到AI回合
            gameState.setCurrentTurn(GameState.Turn.AI);
            // AI自动抽牌
            aiDraw();
        }
        
        return gameState;
    }

    private void aiDraw() {
        // AI从玩家的手牌中随机抽取一张
        List<Card> playerCards = gameState.getPlayerCards();
        if (playerCards.isEmpty()) {
            // 玩家手牌为空，AI获胜
            gameState.setGameStatus(GameState.GameStatus.FINISHED);
            gameState.setWinner("AI");
            return;
        }
        
        Card drawnCard = playerCards.remove(random.nextInt(playerCards.size()));
        gameState.getAiCards().add(drawnCard);
        
        // 检查配对
        removePairs(gameState.getAiCards());
        
        // 检查游戏是否结束
        checkGameEnd();
        
        if (gameState.getGameStatus() == GameState.GameStatus.IN_PROGRESS) {
            // 切换回玩家回合
            gameState.setCurrentTurn(GameState.Turn.PLAYER);
        }
    }

    public GameState getGameState() {
        if (gameState == null) {
            throw new IllegalStateException("游戏未开始");
        }
        return gameState;
    }

    private List<Card> createDeck() {
        List<Card> deck = new ArrayList<>();
        for (Card.Suit suit : Card.Suit.values()) {
            for (Card.Rank rank : Card.Rank.values()) {
                deck.add(new Card(suit, rank));
            }
        }
        return deck;
    }

    private void removePairs(List<Card> cards) {
        List<Card> cardsToRemove = new ArrayList<>();
        for (int i = 0; i < cards.size(); i++) {
            if (cardsToRemove.contains(cards.get(i))) {
                continue;
            }
            for (int j = i + 1; j < cards.size(); j++) {
                if (cards.get(i).equals(cards.get(j))) {
                    cardsToRemove.add(cards.get(i));
                    cardsToRemove.add(cards.get(j));
                    break;
                }
            }
        }
        cards.removeAll(cardsToRemove);
    }

    private void checkGameEnd() {
        if (gameState.getPlayerCards().isEmpty()) {
            gameState.setGameStatus(GameState.GameStatus.FINISHED);
            gameState.setWinner("玩家");
        } else if (gameState.getAiCards().isEmpty()) {
            gameState.setGameStatus(GameState.GameStatus.FINISHED);
            gameState.setWinner("AI");
        }
    }
}
