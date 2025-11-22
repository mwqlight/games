<template>
  <div class="connect-four-container">
    <header class="game-header">
      <h1>四子棋</h1>
      <div class="game-info">
        <span class="current-player">当前玩家: {{ currentPlayer === 'red' ? '红方' : '黄方' }}</span>
        <el-button type="primary" @click="resetGame">重新开始</el-button>
      </div>
    </header>
    <main class="game-main">
      <div class="game-board">
        <div class="board-row" v-for="(row, rowIndex) in board" :key="rowIndex">
          <div 
            class="board-cell"
            v-for="(cell, colIndex) in row"
            :key="colIndex"
            :class="{ 'red': cell === 'red', 'yellow': cell === 'yellow' }"
            @click="dropPiece(colIndex)"
          >
            <div class="piece"></div>
          </div>
        </div>
      </div>
    </main>
    <footer class="game-footer">
      <p v-if="winner">玩家 {{ winner === 'red' ? '红方' : '黄方' }} 获胜！</p>
      <p v-else-if="isDraw">平局！</p>
      <p v-else>点击列顶部放置棋子</p>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

// 游戏状态
const board = ref(createEmptyBoard())
const currentPlayer = ref('red')
const winner = ref(null)
const isDraw = ref(false)

// 创建空棋盘
function createEmptyBoard() {
  return Array(6).fill(null).map(() => Array(7).fill(null))
}

// 重置游戏
function resetGame() {
  board.value = createEmptyBoard()
  currentPlayer.value = 'red'
  winner.value = null
  isDraw.value = false
}

// 放置棋子
function dropPiece(colIndex) {
  if (winner.value || isDraw.value) return
  
  // 找到该列最底部的空位
  let rowIndex = -1
  for (let i = 5; i >= 0; i--) {
    if (board.value[i][colIndex] === null) {
      rowIndex = i
      break
    }
  }
  
  if (rowIndex === -1) return // 该列已满
  
  // 放置棋子
  board.value[rowIndex][colIndex] = currentPlayer.value
  
  // 检查胜负
  if (checkWin(rowIndex, colIndex)) {
    winner.value = currentPlayer.value
  } else if (checkDraw()) {
    isDraw.value = true
  } else {
    // 切换玩家
    currentPlayer.value = currentPlayer.value === 'red' ? 'yellow' : 'red'
  }
}

// 检查胜负
function checkWin(row, col) {
  const player = board.value[row][col]
  
  // 检查横向
  for (let c = 0; c <= 3; c++) {
    if (col - c >= 0 && col - c + 3 < 7) {
      if (
        board.value[row][col - c] === player &&
        board.value[row][col - c + 1] === player &&
        board.value[row][col - c + 2] === player &&
        board.value[row][col - c + 3] === player
      ) {
        return true
      }
    }
  }
  
  // 检查纵向
  for (let r = 0; r <= 2; r++) {
    if (row - r >= 0 && row - r + 3 < 6) {
      if (
        board.value[row - r][col] === player &&
        board.value[row - r + 1][col] === player &&
        board.value[row - r + 2][col] === player &&
        board.value[row - r + 3][col] === player
      ) {
        return true
      }
    }
  }
  
  // 检查正对角线
  for (let d = 0; d <= 3; d++) {
    if (row - d >= 0 && row - d + 3 < 6 && col - d >= 0 && col - d + 3 < 7) {
      if (
        board.value[row - d][col - d] === player &&
        board.value[row - d + 1][col - d + 1] === player &&
        board.value[row - d + 2][col - d + 2] === player &&
        board.value[row - d + 3][col - d + 3] === player
      ) {
        return true
      }
    }
  }
  
  // 检查反对角线
  for (let d = 0; d <= 3; d++) {
    if (row + d < 6 && row + d - 3 >= 0 && col - d >= 0 && col - d + 3 < 7) {
      if (
        board.value[row + d][col - d] === player &&
        board.value[row + d - 1][col - d + 1] === player &&
        board.value[row + d - 2][col - d + 2] === player &&
        board.value[row + d - 3][col - d + 3] === player
      ) {
        return true
      }
    }
  }
  
  return false
}

// 检查平局
function checkDraw() {
  return board.value[0].every(cell => cell !== null)
}
</script>

<style scoped>
.connect-four-container {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.game-header {
  text-align: center;
  padding: 1rem;
}

.game-header h1 {
  font-size: 2rem;
  margin-bottom: 1rem;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
}

.game-info {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 2rem;
}

.current-player {
  font-size: 1.2rem;
  font-weight: bold;
}

.game-main {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 1rem;
}

.game-board {
  background: #2c3e50;
  padding: 1rem;
  border-radius: 12px;
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.3);
}

.board-row {
  display: flex;
}

.board-cell {
  width: 80px;
  height: 80px;
  background: #34495e;
  border: 2px solid #2c3e50;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  justify-content: center;
  align-items: center;
  transition: all 0.3s ease;
}

.board-cell:hover {
  background: #4a5f7a;
}

.board-cell.red .piece {
  width: 70px;
  height: 70px;
  background: #e74c3c;
  border-radius: 50%;
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.3);
}

.board-cell.yellow .piece {
  width: 70px;
  height: 70px;
  background: #f39c12;
  border-radius: 50%;
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.3);
}

.game-footer {
  text-align: center;
  padding: 1rem;
  font-size: 1.2rem;
  font-weight: bold;
}
</style>