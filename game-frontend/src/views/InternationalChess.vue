<template>
  <div class="international-chess-container">
    <header class="game-header">
      <h1>国际象棋游戏</h1>
      <div class="game-info">
        <el-button type="primary" @click="goBack">返回主页</el-button>
        <el-button type="success" @click="restartGame">重新开始</el-button>
      </div>
    </header>
    <main class="game-main">
      <div class="game-board">
        <div class="board-row" v-for="(row, rowIndex) in gameStore.board" :key="rowIndex">
          <div 
            class="board-cell" 
            v-for="(cell, colIndex) in row" 
            :key="colIndex"
            :class="{
              'black-cell': (rowIndex + colIndex) % 2 === 1,
              'white-cell': (rowIndex + colIndex) % 2 === 0,
              'selected': gameStore.selectedPiece && gameStore.selectedPiece.x === rowIndex && gameStore.selectedPiece.y === colIndex,
              'valid-move': isValidMove(rowIndex, colIndex)
            }"
            @click="handleCellClick(rowIndex, colIndex)"
          >
            <div 
              class="piece" 
              v-if="cell !== null"
              :class="{ 'white-piece': cell.color === 'WHITE', 'black-piece': cell.color === 'BLACK' }"
            >
              {{ getPieceSymbol(cell.type) }}
            </div>
          </div>
        </div>
      </div>
      <div class="game-status">
        <h2>{{ gameStore.currentPlayer === 'WHITE' ? '白方' : '黑方' }}回合</h2>
        <p>{{ getGameStatusText() }}</p>
      </div>
    </main>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useGameStore } from '../stores/game'

const router = useRouter()
const gameStore = useGameStore()

// 初始化游戏
const initGame = async () => {
  await gameStore.startGame()
}

// 获取棋子符号
const getPieceSymbol = (type) => {
  switch (type) {
    case 'ROOK': return '♖'
    case 'KNIGHT': return '♘'
    case 'BISHOP': return '♗'
    case 'QUEEN': return '♕'
    case 'KING': return '♔'
    case 'PAWN': return '♙'
    default: return ''
  }
}

// 处理单元格点击
const handleCellClick = async (rowIndex, colIndex) => {
  // 如果已经选择了棋子
  if (gameStore.selectedPiece) {
    // 如果点击的是同一个棋子，取消选择
    if (gameStore.selectedPiece.x === rowIndex && gameStore.selectedPiece.y === colIndex) {
      gameStore.deselectPiece()
    } else {
      // 尝试移动棋子
      await gameStore.makeMove(gameStore.selectedPiece, { x: rowIndex, y: colIndex })
      gameStore.deselectPiece()
    }
  } else {
    // 如果没有选择棋子，尝试选择当前点击的棋子
    const piece = gameStore.board[rowIndex][colIndex]
    if (piece !== null && piece.color === gameStore.currentPlayer) {
      gameStore.selectPiece({ x: rowIndex, y: colIndex })
    }
  }
}

// 检查是否是合法移动
const isValidMove = (rowIndex, colIndex) => {
  if (!gameStore.selectedPiece || gameStore.validMoves.length === 0) {
    return false
  }
  
  return gameStore.validMoves.some(move => move.to.x === rowIndex && move.to.y === colIndex)
}

// 获取游戏状态文本
const getGameStatusText = () => {
  switch (gameStore.gameStatus) {
    case 'playing': return '游戏进行中'
    case 'check': return '将军'
    case 'checkmate': return '将死'
    case 'stalemate': return '和棋'
    default: return '游戏进行中'
  }
}

// 重新开始游戏
const restartGame = async () => {
  await gameStore.startGame()
}

// 返回主页
const goBack = () => {
  router.push('/')
}

// 组件挂载时初始化游戏
onMounted(() => {
  initGame()
})
</script>

<style scoped>
.international-chess-container {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.game-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
}

.game-header h1 {
  font-size: 2rem;
  margin: 0;
}

.game-info {
  display: flex;
  gap: 10px;
}

.game-main {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 50px;
}

.game-board {
  display: flex;
  flex-direction: column;
  border: 2px solid white;
  border-radius: 8px;
  overflow: hidden;
}

.board-row {
  display: flex;
}

.board-cell {
  width: 60px;
  height: 60px;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  transition: background-color 0.3s;
}

.black-cell {
  background-color: #7B3F00;
}

.white-cell {
  background-color: #FFEBCD;
}

.piece {
  width: 45px;
  height: 45px;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 2rem;
  transition: transform 0.3s;
}

.white-piece {
  color: white;
}

.black-piece {
  color: black;
}

.piece:hover {
  transform: scale(1.1);
}

.game-status {
  display: flex;
  flex-direction: column;
  gap: 10px;
  text-align: center;
}

.game-status h2 {
  font-size: 1.5rem;
  margin: 0;
}

.game-status p {
  font-size: 1.2rem;
  margin: 0;
}
</style>