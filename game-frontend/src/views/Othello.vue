<template>
  <div class="othello-container">
    <header class="game-header">
      <h1>黑白棋游戏</h1>
      <div class="game-info">
        <el-button type="primary" @click="goBack">返回主页</el-button>
        <el-button type="success" @click="restartGame">重新开始</el-button>
      </div>
    </header>
    <main class="game-main">
      <div class="game-status">
        <h2>{{ gameStatus }}</h2>
        <div class="game-stats">
          <div class="stat-item">
            <span>黑棋: {{ blackCount }}</span>
          </div>
          <div class="stat-item">
            <span>白棋: {{ whiteCount }}</span>
          </div>
          <div class="stat-item">
            <span>当前玩家: {{ currentPlayer === 1 ? '黑棋' : '白棋' }}</span>
          </div>
        </div>
      </div>
      <div class="game-board">
        <div 
          class="board-cell" 
          v-for="(row, rowIndex) in board" 
          :key="rowIndex"
          :class="{
            'black': cell.value === 1,
            'white': cell.value === 2,
            'valid-move': cell.validMove
          }"
          @click="makeMove(rowIndex, colIndex)"
        >
          <div v-if="cell.value === 1">
            <span>●</span>
          </div>
          <div v-else-if="cell.value === 2">
            <span>○</span>
          </div>
          <div v-else-if="cell.validMove">
            <span>✓</span>
          </div>
        </div>
      </div>
      <div class="game-controls">
        <el-button type="primary" @click="startGame">开始游戏</el-button>
        <el-button type="warning" @click="pauseGame">暂停游戏</el-button>
        <el-button type="danger" @click="stopGame">停止游戏</el-button>
      </div>
      <div class="game-instructions">
        <p>点击棋盘上的空白位置放置棋子，翻转对方的棋子</p>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 初始化游戏状态
const board = ref([])
const gameStatus = ref('准备开始')
const blackCount = ref(0)
const whiteCount = ref(0)
const currentPlayer = ref(1)
const gameStarted = ref(false)
const gamePaused = ref(false)
const timer = ref(null)

// 游戏配置
const boardSize = ref(8)
const initialBlackCount = ref(2)
const initialWhiteCount = ref(2)
const initialPlayer = ref(1)

// 初始化游戏
const initGame = () => {
  // 清除计时器
  if (timer.value) {
    clearInterval(timer.value)
    timer.value = null
  }
  
  // 初始化游戏状态
  gameStatus.value = '准备开始'
  gameStarted.value = false
  gamePaused.value = false
  
  // 初始化棋子数量
  blackCount.value = initialBlackCount.value
  whiteCount.value = initialWhiteCount.value
  
  // 初始化当前玩家
  currentPlayer.value = initialPlayer.value
  
  // 生成棋盘
  generateBoard()
  
  // 设置初始棋子
  setInitialPieces()
  
  // 检查有效移动
  checkValidMoves()
}

// 生成棋盘
const generateBoard = () => {
  // 创建棋盘
  board.value = Array(boardSize.value).fill(null).map(() => Array(boardSize.value).fill(null).map(() => ({
    value: 0,
    validMove: false
  })))
}

// 设置初始棋子
const setInitialPieces = () => {
  // 设置中心位置的初始棋子
  const center = Math.floor(boardSize.value / 2)
  
  board.value[center - 1][center - 1].value = 1 // 黑棋
  board.value[center - 1][center].value = 2     // 白棋
  board.value[center][center - 1].value = 2     // 白棋
  board.value[center][center].value = 1         // 黑棋
}

// 检查有效移动
const checkValidMoves = () => {
  // 清除所有有效移动标记
  for (let row = 0; row < boardSize.value; row++) {
    for (let col = 0; col < boardSize.value; col++) {
      board.value[row][col].validMove = false
    }
  }
  
  // 检查所有空白位置
  for (let row = 0; row < boardSize.value; row++) {
    for (let col = 0; col < boardSize.value; col++) {
      if (board.value[row][col].value === 0) {
        // 检查是否有可以翻转的棋子
        if (hasValidMove(row, col)) {
          // 设置有效移动标记
          board.value[row][col].validMove = true
        }
      }
    }
  }
}

// 检查是否有有效移动
const hasValidMove = (rowIndex, colIndex) => {
  // 检查八个方向
  const directions = [
    { row: -1, col: -1 }, // 左上
    { row: -1, col: 0 },  // 上
    { row: -1, col: 1 },  // 右上
    { row: 0, col: -1 },  // 左
    { row: 0, col: 1 },   // 右
    { row: 1, col: -1 },  // 左下
    { row: 1, col: 0 },   // 下
    { row: 1, col: 1 }    // 右下
  ]
  
  for (let i = 0; i < directions.length; i++) {
    if (hasValidMoveInDirection(rowIndex, colIndex, directions[i].row, directions[i].col)) {
      return true
    }
  }
  
  return false
}

// 检查特定方向是否有有效移动
const hasValidMoveInDirection = (rowIndex, colIndex, rowDelta, colDelta) => {
  // 获取对方玩家
  const opponent = currentPlayer.value === 1 ? 2 : 1
  
  // 检查相邻单元格
  let newRow = rowIndex + rowDelta
  let newCol = colIndex + colDelta
  
  if (newRow < 0 || newRow >= boardSize.value || newCol < 0 || newCol >= boardSize.value) {
    return false
  }
  
  if (board.value[newRow][newCol].value !== opponent) {
    return false
  }
  
  // 继续检查下一个单元格
  newRow += rowDelta
  newCol += colDelta
  
  while (newRow >= 0 && newRow < boardSize.value && newCol >= 0 && newCol < boardSize.value) {
    if (board.value[newRow][newCol].value === 0) {
      return false
    }
    
    if (board.value[newRow][newCol].value === currentPlayer.value) {
      return true
    }
    
    newRow += rowDelta
    newCol += colDelta
  }
  
  return false
}

// 下棋
const makeMove = (rowIndex, colIndex) => {
  if (!gameStarted.value || gamePaused.value) {
    return
  }
  
  // 检查单元格是否为空
  if (board.value[rowIndex][colIndex].value !== 0) {
    return
  }
  
  // 检查是否是有效移动
  if (!board.value[rowIndex][colIndex].validMove) {
    return
  }
  
  // 放置棋子
  board.value[rowIndex][colIndex].value = currentPlayer.value
  
  // 翻转对方的棋子
  flipPieces(rowIndex, colIndex)
  
  // 更新棋子数量
  updatePieceCount()
  
  // 切换玩家
  currentPlayer.value = currentPlayer.value === 1 ? 2 : 1
  
  // 检查有效移动
  checkValidMoves()
  
  // 检查游戏是否结束
  checkGameEnd()
}

// 翻转对方的棋子
const flipPieces = (rowIndex, colIndex) => {
  // 检查八个方向
  const directions = [
    { row: -1, col: -1 }, // 左上
    { row: -1, col: 0 },  // 上
    { row: -1, col: 1 },  // 右上
    { row: 0, col: -1 },  // 左
    { row: 0, col: 1 },   // 右
    { row: 1, col: -1 },  // 左下
    { row: 1, col: 0 },   // 下
    { row: 1, col: 1 }    // 右下
  ]
  
  for (let i = 0; i < directions.length; i++) {
    flipPiecesInDirection(rowIndex, colIndex, directions[i].row, directions[i].col)
  }
}

// 翻转特定方向的棋子
const flipPiecesInDirection = (rowIndex, colIndex, rowDelta, colDelta) => {
  // 获取对方玩家
  const opponent = currentPlayer.value === 1 ? 2 : 1
  
  // 检查相邻单元格
  let newRow = rowIndex + rowDelta
  let newCol = colIndex + colDelta
  
  if (newRow < 0 || newRow >= boardSize.value || newCol < 0 || newCol >= boardSize.value) {
    return
  }
  
  if (board.value[newRow][newCol].value !== opponent) {
    return
  }
  
  // 记录需要翻转的棋子
  const piecesToFlip = []
  
  // 继续检查下一个单元格
  while (newRow >= 0 && newRow < boardSize.value && newCol >= 0 && newCol < boardSize.value) {
    if (board.value[newRow][newCol].value === 0) {
      return
    }
    
    if (board.value[newRow][newCol].value === currentPlayer.value) {
      // 翻转棋子
      for (let i = 0; i < piecesToFlip.length; i++) {
        board.value[piecesToFlip[i].row][piecesToFlip[i].col].value = currentPlayer.value
      }
      
      return
    }
    
    // 添加到需要翻转的棋子列表
    piecesToFlip.push({ row: newRow, col: newCol })
    
    newRow += rowDelta
    newCol += colDelta
  }
}

// 更新棋子数量
const updatePieceCount = () => {
  let black = 0
  let white = 0
  
  for (let row = 0; row < boardSize.value; row++) {
    for (let col = 0; col < boardSize.value; col++) {
      if (board.value[row][col].value === 1) {
        black++
      } else if (board.value[row][col].value === 2) {
        white++
      }
    }
  }
  
  blackCount.value = black
  whiteCount.value = white
}

// 检查游戏结束
const checkGameEnd = () => {
  // 检查是否有有效移动
  let hasValidMove = false
  
  for (let row = 0; row < boardSize.value; row++) {
    for (let col = 0; col < boardSize.value; col++) {
      if (board.value[row][col].validMove) {
        hasValidMove = true
        break
      }
    }
    
    if (hasValidMove) {
      break
    }
  }
  
  if (!hasValidMove) {
    // 切换玩家
    currentPlayer.value = currentPlayer.value === 1 ? 2 : 1
    
    // 检查是否有有效移动
    checkValidMoves()
    
    // 再次检查是否有有效移动
    hasValidMove = false
    
    for (let row = 0; row < boardSize.value; row++) {
      for (let col = 0; col < boardSize.value; col++) {
        if (board.value[row][col].validMove) {
          hasValidMove = true
          break
        }
      }
      
      if (hasValidMove) {
        break
      }
    }
    
    if (!hasValidMove) {
      // 游戏结束
      gameStatus.value = '游戏结束'
      clearInterval(timer.value)
      
      // 计算得分
      if (blackCount.value > whiteCount.value) {
        score.value = blackCount.value - whiteCount.value
      } else if (whiteCount.value > blackCount.value) {
        score.value = whiteCount.value - blackCount.value
      } else {
        score.value = 0
      }
    }
  }
}

// 开始游戏
const startGame = () => {
  if (!gameStarted.value) {
    gameStatus.value = '游戏进行中'
    gameStarted.value = true
    
    // 开始计时器
    timer.value = setInterval(() => {
      time.value++
    }, 1000)
  }
}

// 暂停游戏
const pauseGame = () => {
  if (gameStarted.value && !gamePaused.value) {
    gameStatus.value = '游戏暂停'
    gamePaused.value = true
    
    // 清除计时器
    if (timer.value) {
      clearInterval(timer.value)
      timer.value = null
    }
  }
}

// 停止游戏
const stopGame = () => {
  gameStatus.value = '游戏结束'
  gameStarted.value = false
  gamePaused.value = false
  
  // 清除计时器
  if (timer.value) {
    clearInterval(timer.value)
    timer.value = null
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

// 组件卸载时清除计时器
onUnmounted(() => {
  if (timer.value) {
    clearInterval(timer.value)
    timer.value = null
  }
})
</script>

<style scoped>
.othello-container {
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
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 20px;
  overflow-y: auto;
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

.game-stats {
  display: flex;
  gap: 20px;
  justify-content: center;
}

.stat-item {
  font-size: 1.1rem;
  font-weight: bold;
}

.game-board {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  grid-template-rows: repeat(8, 1fr);
  width: 400px;
  height: 400px;
  border: 2px solid white;
  border-radius: 8px;
  overflow: hidden;
  background-color: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
}

.board-cell {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
  border: 2px solid white;
  border-radius: 4px;
  background-color: rgba(255, 255, 255, 0.2);
}

.board-cell.black {
  color: black;
}

.board-cell.white {
  color: white;
}

.board-cell.valid-move {
  color: green;
}

.game-controls {
  display: flex;
  gap: 10px;
}

.game-instructions {
  font-size: 1.1rem;
}
</style>