<template>
  <div class="minesweeper-container">
    <header class="game-header">
      <h1>扫雷游戏</h1>
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
            <span>剩余地雷: {{ remainingMines }}</span>
          </div>
          <div class="stat-item">
            <span>已扫雷区: {{ clearedCells }}</span>
          </div>
          <div class="stat-item">
            <span>时间: {{ time }}</span>
          </div>
        </div>
      </div>
      <div class="game-board">
        <div class="board-row" v-for="(row, rowIndex) in board" :key="rowIndex">
          <div 
            class="board-cell" 
            v-for="(cell, colIndex) in row" 
            :key="colIndex"
            :class="{
              'revealed': cell.revealed,
              'mine': cell.isMine,
              'flagged': cell.flagged,
              'number-1': cell.adjacentMines === 1,
              'number-2': cell.adjacentMines === 2,
              'number-3': cell.adjacentMines === 3,
              'number-4': cell.adjacentMines === 4,
              'number-5': cell.adjacentMines === 5,
              'number-6': cell.adjacentMines === 6,
              'number-7': cell.adjacentMines === 7,
              'number-8': cell.adjacentMines === 8
            }"
            @click="revealCell(rowIndex, colIndex)"
            @contextmenu.prevent="toggleFlag(rowIndex, colIndex)"
          >
            <div v-if="cell.revealed">
              <span v-if="cell.isMine">💣</span>
              <span v-else-if="cell.adjacentMines > 0">{{ cell.adjacentMines }}</span>
            </div>
            <div v-else-if="cell.flagged">🚩</div>
          </div>
        </div>
      </div>
      <div class="game-controls">
        <el-button type="primary" @click="newGame('easy')">简单</el-button>
        <el-button type="warning" @click="newGame('medium')">中等</el-button>
        <el-button type="danger" @click="newGame('hard')">困难</el-button>
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
const gameStatus = ref('游戏进行中')
const remainingMines = ref(0)
const clearedCells = ref(0)
const time = ref(0)
const timer = ref(null)
const gameStarted = ref(false)

// 游戏难度配置
const difficulty = ref('medium')
const difficultyConfig = {
  easy: { rows: 8, cols: 8, mines: 10 },
  medium: { rows: 16, cols: 16, mines: 40 },
  hard: { rows: 16, cols: 30, mines: 99 }
}

// 初始化游戏
const initGame = async () => {
  try {
    // 清除计时器
    if (timer.value) {
      clearInterval(timer.value)
      timer.value = null
    }
    
    // 初始化时间
    time.value = 0
    
    // 获取当前难度配置
    const config = difficultyConfig[difficulty.value]
    
    // 创建新游戏
    const response = await fetch('http://localhost:8080/api/minesweeper/new', { 
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ rows: config.rows, cols: config.cols, mines: config.mines })
    })
    const game = await response.json()
    
    // 初始化游戏状态
    gameStatus.value = '游戏进行中'
    gameStarted.value = false
    
    // 初始化棋盘
    board.value = game.board.map(row => row.map(cell => ({
      isMine: cell.mine,
      revealed: cell.revealed,
      flagged: cell.flagged,
      adjacentMines: cell.adjacentMines
    })))
    
    // 初始化剩余地雷数量
    remainingMines.value = game.remainingMines
    
    // 初始化已扫雷区数量
    clearedCells.value = game.clearedCells
    
    // 保存游戏ID
    gameId.value = game.gameId
  } catch (error) {
    console.error('初始化游戏失败:', error)
    gameStatus.value = '连接失败'
  }
}

// 放置地雷
const placeMines = (rows, cols, mines) => {
  let placedMines = 0
  
  while (placedMines < mines) {
    const row = Math.floor(Math.random() * rows)
    const col = Math.floor(Math.random() * cols)
    
    if (!board.value[row][col].isMine) {
      board.value[row][col].isMine = true
      placedMines++
    }
  }
}

// 计算相邻地雷数量
const calculateAdjacentMines = (rows, cols) => {
  for (let row = 0; row < rows; row++) {
    for (let col = 0; col < cols; col++) {
      if (board.value[row][col].isMine) {
        continue
      }
      
      let count = 0
      
      // 检查周围8个单元格
      for (let i = -1; i <= 1; i++) {
        for (let j = -1; j <= 1; j++) {
          if (i === 0 && j === 0) {
            continue
          }
          
          const newRow = row + i
          const newCol = col + j
          
          if (newRow >= 0 && newRow < rows && newCol >= 0 && newCol < cols) {
            if (board.value[newRow][newCol].isMine) {
              count++
            }
          }
        }
      }
      
      board.value[row][col].adjacentMines = count
    }
  }
}

// 揭示单元格
const revealCell = (rowIndex, colIndex) => {
  // 检查游戏是否已经结束
  if (gameStatus.value !== '游戏进行中') {
    return
  }
  
  // 检查是否已经揭示或标记
  if (board.value[rowIndex][colIndex].revealed || board.value[rowIndex][colIndex].flagged) {
    return
  }
  
  // 开始计时器
  if (!gameStarted.value) {
    startTimer()
    gameStarted.value = true
  }
  
  // 揭示单元格
  board.value[rowIndex][colIndex].revealed = true
  
  // 检查是否是地雷
  if (board.value[rowIndex][colIndex].isMine) {
    gameStatus.value = '游戏结束'
    clearInterval(timer.value)
    revealAllMines()
    return
  }
  
  // 增加已扫雷区数量
  clearedCells.value++
  
  // 检查是否获胜
  checkWin()
  
  // 如果相邻地雷数量为0，递归揭示周围单元格
  if (board.value[rowIndex][colIndex].adjacentMines === 0) {
    revealAdjacentCells(rowIndex, colIndex)
  }
}

// 揭示相邻单元格
const revealAdjacentCells = (rowIndex, colIndex) => {
  // 检查周围8个单元格
  for (let i = -1; i <= 1; i++) {
    for (let j = -1; j <= 1; j++) {
      if (i === 0 && j === 0) {
        continue
      }
      
      const newRow = rowIndex + i
      const newCol = colIndex + j
      
      if (newRow >= 0 && newRow < board.value.length && newCol >= 0 && newCol < board.value[0].length) {
        if (!board.value[newRow][newCol].revealed && !board.value[newRow][newCol].flagged) {
          revealCell(newRow, newCol)
        }
      }
    }
  }
}

// 切换标记
const toggleFlag = (rowIndex, colIndex) => {
  // 检查游戏是否已经结束
  if (gameStatus.value !== '游戏进行中') {
    return
  }
  
  // 检查是否已经揭示
  if (board.value[rowIndex][colIndex].revealed) {
    return
  }
  
  // 切换标记
  board.value[rowIndex][colIndex].flagged = !board.value[rowIndex][colIndex].flagged
  
  // 更新剩余地雷数量
  if (board.value[rowIndex][colIndex].flagged) {
    remainingMines.value--
  } else {
    remainingMines.value++
  }
  
  // 检查是否获胜
  checkWin()
}

// 检查是否获胜
const checkWin = () => {
  // 获取当前难度配置
  const config = difficultyConfig[difficulty.value]
  
  // 检查是否所有非地雷单元格都已经揭示
  let allCleared = true
  
  for (let row = 0; row < config.rows; row++) {
    for (let col = 0; col < config.cols; col++) {
      if (!board.value[row][col].isMine && !board.value[row][col].revealed) {
        allCleared = false
        break
      }
    }
    
    if (!allCleared) {
      break
    }
  }
  
  if (allCleared) {
    gameStatus.value = '恭喜获胜'
    clearInterval(timer.value)
  }
}

// 揭示所有地雷
const revealAllMines = () => {
  // 获取当前难度配置
  const config = difficultyConfig[difficulty.value]
  
  for (let row = 0; row < config.rows; row++) {
    for (let col = 0; col < config.cols; col++) {
      if (board.value[row][col].isMine) {
        board.value[row][col].revealed = true
      }
    }
  }
}

// 开始计时器
const startTimer = () => {
  timer.value = setInterval(() => {
    time.value++
  }, 1000)
}

// 重新开始游戏
const restartGame = () => {
  initGame()
}

// 新游戏
const newGame = (level) => {
  difficulty.value = level
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
.minesweeper-container {
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
  display: flex;
  flex-direction: column;
  border: 2px solid white;
  border-radius: 8px;
  overflow: hidden;
  background-color: #ccc;
}

.board-row {
  display: flex;
}

.board-cell {
  width: 30px;
  height: 30px;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  transition: background-color 0.3s;
  border: 1px solid #999;
  background-color: #eee;
}

.board-cell.revealed {
  background-color: #ddd;
}

.board-cell.mine {
  background-color: red;
}

.board-cell.flagged {
  color: red;
}

.board-cell.number-1 {
  color: blue;
}

.board-cell.number-2 {
  color: green;
}

.board-cell.number-3 {
  color: red;
}

.board-cell.number-4 {
  color: darkblue;
}

.board-cell.number-5 {
  color: darkred;
}

.board-cell.number-6 {
  color: teal;
}

.board-cell.number-7 {
  color: black;
}

.board-cell.number-8 {
  color: gray;
}

.game-controls {
  display: flex;
  gap: 10px;
}
</style>