package com.lgames.gamebackend.model;

import java.util.List;

public class GameState {
    private List<Card> playerCards;
    private List<Card> aiCards;
    private Turn currentTurn;
    private GameStatus gameStatus;
    private Card turtleCard;
    private String winner;

    public GameState() {
    }

    public GameState(List<Card> playerCards, List<Card> aiCards, Turn currentTurn, GameStatus gameStatus, Card turtleCard) {
        this.playerCards = playerCards;
        this.aiCards = aiCards;
        this.currentTurn = currentTurn;
        this.gameStatus = gameStatus;
        this.turtleCard = turtleCard;
    }

    public List<Card> getPlayerCards() {
        return playerCards;
    }

    public void setPlayerCards(List<Card> playerCards) {
        this.playerCards = playerCards;
    }

    public List<Card> getAiCards() {
        return aiCards;
    }

    public void setAiCards(List<Card> aiCards) {
        this.aiCards = aiCards;
    }

    public Turn getCurrentTurn() {
        return currentTurn;
    }

    public void setCurrentTurn(Turn currentTurn) {
        this.currentTurn = currentTurn;
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }

    public Card getTurtleCard() {
        return turtleCard;
    }

    public void setTurtleCard(Card turtleCard) {
        this.turtleCard = turtleCard;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public enum Turn {
        PLAYER, AI
    }

    public enum GameStatus {
        IN_PROGRESS, FINISHED
    }
}
