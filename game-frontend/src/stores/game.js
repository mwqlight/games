import { defineStore } from 'pinia'
import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080/api/chess'

export const useGameStore = defineStore('game', {
  state: () => ({
    board: [],
    currentPlayer: 'white',
    gameStatus: 'playing', // 'check', 'checkmate', 'stalemate'
    selectedPiece: null,
    validMoves: [],
    gameId: null
  }),
  actions: {
    // 开始新游戏
    async startGame() {
      try {
        const response = await axios.post(`${API_BASE_URL}/game`)
        this.gameId = response.data.gameId
        this.updateGameState(response.data)
      } catch (error) {
        console.error('Failed to start game:', error)
      }
    },
    
    // 处理玩家走子
    async makeMove(from, to) {
      if (!this.gameId) {
        console.error('No game ID available')
        return
      }
      
      try {
        const move = {
          fromRow: from.x,
          fromCol: from.y,
          toRow: to.x,
          toCol: to.y,
          player: this.currentPlayer
        }
        const response = await axios.post(`${API_BASE_URL}/game/${this.gameId}/move`, move)
        this.updateGameState(response.data)
      } catch (error) {
        console.error('Failed to make move:', error)
      }
    },
    
    // 获取当前游戏状态
    async fetchGameState() {
      if (!this.gameId) {
        console.error('No game ID available')
        return
      }
      
      try {
        const response = await axios.get(`${API_BASE_URL}/game/${this.gameId}`)
        this.updateGameState(response.data)
      } catch (error) {
        console.error('Failed to fetch game state:', error)
      }
    },
    
    // 更新游戏状态
    updateGameState(gameState) {
      this.board = gameState.board
      this.currentPlayer = gameState.currentPlayer
      this.gameStatus = gameState.status
    },
    
    // 选择棋子
    selectPiece(position) {
      this.selectedPiece = position
      // 获取该棋子的所有合法移动
      this.validMoves = this.getValidMovesForPiece(position)
    },
    
    // 取消选择棋子
    deselectPiece() {
      this.selectedPiece = null
      this.validMoves = []
    },
    
    // 获取棋子的所有合法移动
    getValidMovesForPiece(position) {
      // 这里需要实现获取合法移动的逻辑
      // 目前暂时返回空数组，后面会根据后端返回的validMoves来更新
      return []
    }
  }
})
