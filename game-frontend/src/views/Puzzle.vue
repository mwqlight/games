<template>
  <div class="puzzle-container">
    <header class="game-header">
      <h1>拼图游戏</h1>
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
            <span>得分: {{ score }}</span>
          </div>
          <div class="stat-item">
            <span>时间: {{ time }}</span>
          </div>
          <div class="stat-item">
            <span>步数: {{ moves }}</span>
          </div>
        </div>
      </div>
      <div class="game-board">
        <div 
          class="board-cell" 
          v-for="(cell, index) in board" 
          :key="index"
          :class="{
            'empty': cell === 0,
            'correct': cell === index + 1
          }"
          @click="moveCell(index)"
        >
          <div class="cell-content" v-if="cell !== 0">
            {{ cell }}
          </div>
        </div>
      </div>
      <div class="game-controls">
        <el-button type="primary" @click="startGame" :disabled="gameStarted">开始游戏</el-button>
        <el-button type="warning" @click="pauseGame" :disabled="!gameStarted || gamePaused">暂停游戏</el-button>
        <el-button type="danger" @click="stopGame" :disabled="!gameStarted">停止游戏</el-button>
      </div>
      <div class="game-instructions">
        <p>点击空白格周围的数字进行移动，将数字按顺序排列</p>
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
const score = ref(0)
const time = ref(0)
const moves = ref(0)
const gameStarted = ref(false)
const gamePaused = ref(false)
const timer = ref(null)

// 游戏配置
const boardSize = 4
const totalCells = boardSize * boardSize

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
  
  // 初始化得分
  score.value = 0
  
  // 初始化时间
  time.value = 0
  
  // 初始化步数
  moves.value = 0
  
  // 生成棋盘
  generateBoard()
}

// 生成棋盘
const generateBoard = () => {
  // 创建一个包含1到15的数组
  const numbers = Array.from({ length: totalCells - 1 }, (_, i) => i + 1)
  
  // 添加空白格
  numbers.push(0)
  
  // 洗牌
  shuffleArray(numbers)
  
  // 生成棋盘
  board.value = numbers
}

// 洗牌
const shuffleArray = (array) => {
  for (let i = array.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1))
    ;[array[i], array[j]] = [array[j], array[i]]
  }
}

// 移动单元格
const moveCell = (index) => {
  // 检查游戏是否已经开始
  if (!gameStarted.value || gamePaused.value) {
    return
  }
  
  // 检查单元格是否是空白格
  if (board.value[index] === 0) {
    return
  }
  
  // 检查空白格的位置
  const emptyIndex = board.value.indexOf(0)
  
  // 检查是否可以移动
  if (canMove(index, emptyIndex)) {
    // 交换单元格
    ;[board.value[index], board.value[emptyIndex]] = [board.value[emptyIndex], board.value[index]]
    
    // 增加步数
    moves.value++
    
    // 检查游戏是否结束
    checkGameEnd()
  }
}

// 检查是否可以移动
const canMove = (index, emptyIndex) => {
  // 计算行和列
  const indexRow = Math.floor(index / boardSize)
  const indexCol = index % boardSize
  const emptyRow = Math.floor(emptyIndex / boardSize)
  const emptyCol = emptyIndex % boardSize
  
  // 检查是否在同一行或同一列
  if (indexRow !== emptyRow && indexCol !== emptyCol) {
    return false
  }
  
  // 检查是否相邻
  if (Math.abs(indexRow - emptyRow) > 1 || Math.abs(indexCol - emptyCol) > 1) {
    return false
  }
  
  return true
}

// 检查游戏是否结束
const checkGameEnd = () => {
  // 检查是否所有数字都按顺序排列
  for (let i = 0; i < totalCells - 1; i++) {
    if (board.value[i] !== i + 1) {
      return
    }
  }
  
  // 游戏结束
  gameStatus.value = '恭喜获胜'
  clearInterval(timer.value)
  
  // 计算得分
  score.value = Math.max(0, 1000 - time.value * 10 - moves.value * 5)
}

// 开始游戏
const startGame = () => {
  gameStatus.value = '游戏进行中'
  gameStarted.value = true
  gamePaused.value = false
  
  // 开始计时器
  timer.value = setInterval(() => {
    time.value++
  }, 1000)
}

// 暂停游戏
const pauseGame = () => {
  gameStatus.value = '游戏暂停'
  gamePaused.value = true
  
  // 清除计时器
  if (timer.value) {
    clearInterval(timer.value)
    timer.value = null
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
.puzzle-container {
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
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
  padding: 20px;
  border: 2px solid white;
  border-radius: 8px;
  background-color: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
}

.board-cell {
  width: 80px;
  height: 80px;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
  border: 2px solid white;
  border-radius: 8px;
  background-color: rgba(255, 255, 255, 0.2);
}

.board-cell.empty {
  background-color: rgba(255, 255, 255, 0.1);
}

.board-cell.correct {
  background-color: rgba(0, 255, 0, 0.2);
  border-color: green;
}

.cell-content {
  font-size: 2rem;
  font-weight: bold;
}

.game-controls {
  display: flex;
  gap: 10px;
}

.game-instructions {
  font-size: 1.1rem;
}
</style>