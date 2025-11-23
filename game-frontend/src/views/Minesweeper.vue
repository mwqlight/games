<template>
  <div class="minesweeper-container">
    <header class="game-header">
      <h1>扫雷游戏</h1>
      <div class="game-info">
        <el-button type="primary" @click="goBack">返回主页</el-button>
        <el-button type="success" @click="restartGame" :disabled="loading">重新开始</el-button>
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
          <div class="stat-item" v-if="isMemoryPhase">
            <span>记忆时间: {{ memoryTime }}</span>
          </div>
          <div class="stat-item" v-else>
            <span>游戏时间: {{ gameTime }} / {{ timeLimit }}</span>
          </div>
        </div>
      </div>
      <div class="game-board" v-if="!loading">
        <div class="board-row" v-for="(row, rowIndex) in board" :key="rowIndex">
          <div 
            class="board-cell" 
            v-for="(cell, colIndex) in row" 
            :key="colIndex"
            :class="{
              'revealed': cell.revealed,
              'mine': cell.mine,
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
            :disabled="loading"
          >
            <div v-if="cell.revealed">
              <span v-if="cell.mine">💣</span>
              <span v-else-if="cell.adjacentMines > 0">{{ cell.adjacentMines }}</span>
            </div>
            <div v-else-if="cell.flagged">🚩</div>
          </div>
        </div>
      </div>
      <div class="loading" v-if="loading">
        <el-loading-spinner size="large" />
        <p>加载中...</p>
      </div>
      <div class="game-controls">
        <el-button type="primary" @click="newGame('BEGINNER')" :disabled="loading">简单</el-button>
        <el-button type="warning" @click="newGame('INTERMEDIATE')" :disabled="loading">中等</el-button>
        <el-button type="danger" @click="newGame('EXPERT')" :disabled="loading">困难</el-button>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const router = useRouter()

// 初始化游戏状态
const gameId = ref(null)
const board = ref([])
const gameStatus = ref('游戏进行中')
const remainingMines = ref(0)
const clearedCells = ref(0)
const gameTime = ref(0) // 游戏时间
const memoryTime = ref(0) // 记忆时间
const gameTimer = ref(null) // 游戏计时器
const memoryTimer = ref(null) // 记忆计时器
const gameStarted = ref(false)
const memoryStarted = ref(false)
const loading = ref(false)
const isMemoryPhase = ref(false) // 是否处于记忆阶段

// 不同难度的游戏时间限制（秒）
const difficultyTimeLimits = {
  BEGINNER: 60,
  INTERMEDIATE: 180,
  EXPERT: 300
}

// 当前游戏时间限制
const timeLimit = computed(() => {
  return difficultyTimeLimits[difficulty.value] || 180
})

// 游戏难度配置
const difficulty = ref('INTERMEDIATE')

// 计算已扫雷区数量
const calculateClearedCells = () => {
  let count = 0
  for (let row of board.value) {
    for (let cell of row) {
      if (cell.revealed && !cell.mine) {
        count++
      }
    }
  }
  return count
}

// 开始记忆计时器
const startMemoryTimer = () => {
  memoryTimer.value = setInterval(() => {
    memoryTime.value++
  }, 1000)
}

// 开始游戏计时器
const startGameTimer = () => {
  gameTimer.value = setInterval(() => {
    gameTime.value++
    
    // 检查游戏时间是否用完
    if (gameTime.value >= timeLimit.value) {
      endGame('TIME_OUT')
    }
  }, 1000)
}

// 结束游戏
const endGame = (status) => {
  clearInterval(gameTimer.value)
  clearInterval(memoryTimer.value)
  
  switch (status) {
    case 'WON':
      gameStatus.value = '恭喜获胜'
      ElMessage.success('恭喜你，扫雷成功！')
      break
    case 'LOST':
      gameStatus.value = '游戏结束'
      ElMessage.error('很遗憾，你触雷了！')
      // 游戏结束时揭示所有雷
      revealAllMines()
      break
    case 'TIME_OUT':
      gameStatus.value = '时间耗尽'
      ElMessage.error('很遗憾，时间耗尽了！')
      // 时间耗尽时揭示所有雷
      revealAllMines()
      break
  }
}

// 揭示所有雷
const revealAllMines = () => {
  for (let row of board.value) {
    for (let cell of row) {
      if (cell.mine) {
        cell.revealed = true
      }
    }
  }
}

// API 基础 URL
const API_BASE_URL = 'http://localhost:8080/api'

// 初始化游戏
const initGame = async () => {
  try {
    loading.value = true
    // 清除计时器
    clearInterval(gameTimer.value)
    clearInterval(memoryTimer.value)
    
    // 初始化时间
    gameTime.value = 0
    memoryTime.value = 0
    
    // 初始化游戏状态
    gameStatus.value = '记忆阶段'
    gameStarted.value = false
    memoryStarted.value = false
    isMemoryPhase.value = true
    
    // 调用后端 API 创建新游戏
    const response = await axios.post(`${API_BASE_URL}/game/minesweeper/new`, {
      difficulty: difficulty.value
    })
    
    const gameState = response.data
    gameId.value = gameState.gameId
    board.value = gameState.board
    remainingMines.value = gameState.flagsRemaining
    clearedCells.value = calculateClearedCells()
    
    // 开始记忆计时器
    startMemoryTimer()
    memoryStarted.value = true
    
    // 记忆阶段持续时间（根据难度调整）
    const memoryDuration = difficulty.value === 'BEGINNER' ? 10 : difficulty.value === 'INTERMEDIATE' ? 20 : 30
    
    // 记忆时间结束后，隐藏所有雷并开始游戏
    setTimeout(() => {
      hideAllMines()
      isMemoryPhase.value = false
      gameStatus.value = '游戏进行中'
      startGameTimer()
      gameStarted.value = true
    }, memoryDuration * 1000)
  } catch (error) {
    console.error('Failed to initialize game:', error)
    ElMessage.error('游戏初始化失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 隐藏所有雷
const hideAllMines = () => {
  for (let row of board.value) {
    for (let cell of row) {
      if (cell.mine) {
        cell.revealed = false
      }
    }
  }
}

// 揭示单元格
const revealCell = async (rowIndex, colIndex) => {
  // 检查游戏是否已经结束或处于记忆阶段
  if (gameStatus.value !== '游戏进行中' || loading.value || isMemoryPhase.value) {
    return
  }
  
  // 检查是否已经揭示或标记
  if (board.value[rowIndex][colIndex].revealed || board.value[rowIndex][colIndex].flagged) {
    return
  }
  
  try {
    loading.value = true
    
    // 调用后端 API 揭示格子
    const response = await axios.post(`${API_BASE_URL}/game/minesweeper/${gameId.value}/reveal`, {
      x: rowIndex,
      y: colIndex
    })
    
    const gameState = response.data
    board.value = gameState.board
    remainingMines.value = gameState.flagsRemaining
    clearedCells.value = calculateClearedCells()
    
    // 更新游戏状态
    switch (gameState.status) {
      case 'PLAYING':
        gameStatus.value = '游戏进行中'
        break
      case 'WON':
        endGame('WON')
        break
      case 'LOST':
        endGame('LOST')
        break
    }
  } catch (error) {
    console.error('Failed to reveal cell:', error)
    ElMessage.error('揭示格子失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 切换标记
const toggleFlag = async (rowIndex, colIndex) => {
  // 检查游戏是否已经结束或处于记忆阶段
  if (gameStatus.value !== '游戏进行中' || loading.value || isMemoryPhase.value) {
    return
  }
  
  // 检查是否已经揭示
  if (board.value[rowIndex][colIndex].revealed) {
    return
  }
  
  try {
    loading.value = true
    // 调用后端 API 标记格子
    const response = await axios.post(`${API_BASE_URL}/game/minesweeper/${gameId.value}/flag`, {
      x: rowIndex,
      y: colIndex
    })
    
    const gameState = response.data
    board.value = gameState.board
    remainingMines.value = gameState.flagsRemaining
    clearedCells.value = calculateClearedCells()
    
    // 更新游戏状态
    switch (gameState.status) {
      case 'PLAYING':
        gameStatus.value = '游戏进行中'
        break
      case 'WON':
        endGame('WON')
        break
      case 'LOST':
        endGame('LOST')
        break
    }
  } catch (error) {
    console.error('Failed to toggle flag:', error)
    ElMessage.error('标记格子失败，请稍后重试')
  } finally {
    loading.value = false
  }
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
  clearInterval(gameTimer.value)
  clearInterval(memoryTimer.value)
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

.memory-time {
  color: #4CAF50;
  font-weight: bold;
}

.game-time {
  color: #2196F3;
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

.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 20px;
}
</style>