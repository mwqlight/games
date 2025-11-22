<template>
  <div class="checkers-container">
    <header class="game-header">
      <h1>跳棋游戏</h1>
      <div class="game-info">
        <el-button type="primary" @click="goBack">返回主页</el-button>
        <el-button type="success" @click="restartGame">重新开始</el-button>
      </div>
    </header>
    <main class="game-main">
      <div class="game-board">
        <div class="board-row" v-for="(row, rowIndex) in board" :key="rowIndex">
          <div 
            class="board-cell" 
            v-for="(cell, colIndex) in row" 
            :key="colIndex"
            :class="{ 'black-cell': (rowIndex + colIndex) % 2 === 1, 'white-cell': (rowIndex + colIndex) % 2 === 0 }"
            @click="makeMove(rowIndex, colIndex)"
          >
            <div 
              class="piece" 
              v-if="cell !== null"
              :class="{ 'red-piece': cell === 'red', 'black-piece': cell === 'black' }"
            ></div>
          </div>
        </div>
      </div>
      <div class="game-status">
        <h2>{{ currentPlayer === 'red' ? '红方' : '黑方' }}回合</h2>
        <p>{{ gameStatus }}</p>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 初始化棋盘
const board = ref([])
const currentPlayer = ref('red')
const gameStatus = ref('游戏进行中')

// 初始化游戏
const initGame = () => {
  // 创建一个8x8的棋盘
  board.value = Array(8).fill(null).map(() => Array(8).fill(null))
  
  // 放置红方棋子
  for (let row = 0; row < 3; row++) {
    for (let col = 0; col < 8; col++) {
      if ((row + col) % 2 === 1) {
        board.value[row][col] = 'red'
      }
    }
  }
  
  // 放置黑方棋子
  for (let row = 5; row < 8; row++) {
    for (let col = 0; col < 8; col++) {
      if ((row + col) % 2 === 1) {
        board.value[row][col] = 'black'
      }
    }
  }
  
  currentPlayer.value = 'red'
  gameStatus.value = '游戏进行中'
}

// 处理移动
const makeMove = (rowIndex, colIndex) => {
  // 检查当前位置是否有棋子
  const piece = board.value[rowIndex][colIndex]
  if (piece === null || piece !== currentPlayer.value) {
    return
  }
  
  // 检查是否可以移动
  const validMoves = getValidMoves(rowIndex, colIndex)
  if (validMoves.length === 0) {
    return
  }
  
  // 移动棋子
  board.value[rowIndex][colIndex] = null
  board.value[validMoves[0].row][validMoves[0].col] = currentPlayer.value
  
  // 检查是否有吃子
  if (Math.abs(validMoves[0].row - rowIndex) === 2) {
    const capturedRow = (rowIndex + validMoves[0].row) / 2
    const capturedCol = (colIndex + validMoves[0].col) / 2
    board.value[capturedRow][capturedCol] = null
  }
  
  // 切换玩家
  currentPlayer.value = currentPlayer.value === 'red' ? 'black' : 'red'
  
  // 检查游戏是否结束
  checkGameEnd()
}

// 获取有效移动
const getValidMoves = (rowIndex, colIndex) => {
  const moves = []
  const direction = currentPlayer.value === 'red' ? 1 : -1
  
  // 检查向前移动
  if (rowIndex + direction >= 0 && rowIndex + direction < 8) {
    // 检查左前方
    if (colIndex - 1 >= 0 && board.value[rowIndex + direction][colIndex - 1] === null) {
      moves.push({ row: rowIndex + direction, col: colIndex - 1 })
    }
    
    // 检查右前方
    if (colIndex + 1 < 8 && board.value[rowIndex + direction][colIndex + 1] === null) {
      moves.push({ row: rowIndex + direction, col: colIndex + 1 })
    }
  }
  
  // 检查吃子
  if (rowIndex + direction * 2 >= 0 && rowIndex + direction * 2 < 8) {
    // 检查左前方吃子
    if (colIndex - 2 >= 0 && 
        board.value[rowIndex + direction][colIndex - 1] !== null && 
        board.value[rowIndex + direction][colIndex - 1] !== currentPlayer.value && 
        board.value[rowIndex + direction * 2][colIndex - 2] === null) {
      moves.push({ row: rowIndex + direction * 2, col: colIndex - 2 })
    }
    
    // 检查右前方吃子
    if (colIndex + 2 < 8 && 
        board.value[rowIndex + direction][colIndex + 1] !== null && 
        board.value[rowIndex + direction][colIndex + 1] !== currentPlayer.value && 
        board.value[rowIndex + direction * 2][colIndex + 2] === null) {
      moves.push({ row: rowIndex + direction * 2, col: colIndex + 2 })
    }
  }
  
  return moves
}

// 检查游戏结束
const checkGameEnd = () => {
  // 检查是否有玩家没有棋子
  let redCount = 0
  let blackCount = 0
  
  for (let row = 0; row < 8; row++) {
    for (let col = 0; col < 8; col++) {
      if (board.value[row][col] === 'red') {
        redCount++
      } else if (board.value[row][col] === 'black') {
        blackCount++
      }
    }
  }
  
  if (redCount === 0) {
    gameStatus.value = '黑方获胜'
  } else if (blackCount === 0) {
    gameStatus.value = '红方获胜'
  }
}

// 重新开始游戏
const restartGame = () => {
  initGame()
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
.checkers-container {
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
  border-radius: 50%;
  border: 2px solid white;
  transition: transform 0.3s;
}

.red-piece {
  background-color: #FF4500;
}

.black-piece {
  background-color: #000000;
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