import { defineStore } from 'pinia';
import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api/game';

export const useChessStore = defineStore('chess', {
  state: () => ({
    board: [],
    currentPlayer: 'WHITE',
    gameStatus: 'playing',
    validMoves: [],
    gameId: null
  }),

  actions: {
    async startGame() {
      try {
        const response = await axios.post(`${API_BASE_URL}/start`);
        this.updateGameState(response.data);
      } catch (error) {
        console.error('Failed to start game:', error);
      }
    },

    async makeMove(fromRow, fromCol, toRow, toCol) {
      try {
        const move = {
          fromRow,
          fromCol,
          toRow,
          toCol,
          player: this.currentPlayer
        };
        const response = await axios.post(`${API_BASE_URL}/move`, move);
        this.updateGameState(response.data);
      } catch (error) {
        console.error('Failed to make move:', error);
      }
    },

    async getBoard() {
      try {
        const response = await axios.get(`${API_BASE_URL}/board`);
        this.updateGameState(response.data);
      } catch (error) {
        console.error('Failed to get board:', error);
      }
    },

    updateGameState(gameState) {
      this.board = gameState.board;
      this.currentPlayer = gameState.currentPlayer;
      this.gameStatus = gameState.status;
      this.validMoves = gameState.validMoves || [];
    },

    getValidMovesForPiece(row, col) {
      // 在前端计算合法移动（可选，也可以从后端获取）
      const piece = this.board[row][col];
      if (!piece || piece.player !== this.currentPlayer) {
        return [];
      }

      // 这里可以添加前端的合法移动计算逻辑
      // 为了简单起见，我们暂时返回空数组，实际应该从后端获取
      return [];
    }
  }
});