<template>
  <div class="snake-container">
    <header class="game-header">
      <h1>贪吃蛇游戏</h1>
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
            <span>长度: {{ snake.length }}</span>
          </div>
          <div class="stat-item">
            <span>速度: {{ speed }}ms</span>
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
              'snake': cell === 'snake',
              'food': cell === 'food',
              'wall': cell === 'wall'
            }"
          ></div>
        </div>
      </div>
      <div class="game-controls">
        <el-button type="primary" @click="startGame" :disabled="gameStarted">开始游戏</el-button>
        <el-button type="warning" @click="pauseGame" :disabled="!gameStarted || gamePaused">暂停游戏</el-button>
        <el-button type="danger" @click="stopGame" :disabled="!gameStarted">停止游戏</el-button>
      </div>
      <div class="game-instructions">
        <p>使用方向键控制贪吃蛇移动</p>
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
const speed = ref(150)
const snake = ref([])
const food = ref({})
const direction = ref('RIGHT')
const gameStarted = ref(false)
const gamePaused = ref(false)
const gameLoop = ref(null)
const gameId = ref(null)

// 游戏配置
const boardSize = 20

// 初始化游戏
const initGame = () => {
  // 清除游戏循环
  if (gameLoop.value) {
    clearInterval(gameLoop.value)
    gameLoop.value = null
  }
  
  // 初始化游戏状态
  gameStatus.value = '准备开始'
  gameStarted.value = false
  gamePaused.value = false
  
  // 初始化得分
  score.value = 0
  
  // 初始化速度
  speed.value = 150
  
  // 初始化贪吃蛇
  snake.value = []
  
  // 初始化方向
  direction.value = 'RIGHT'
  
  // 初始化棋盘
  board.value = Array(boardSize).fill(null).map(() => Array(boardSize).fill(null))
  
  // 添加键盘事件监听
  window.addEventListener('keydown', handleKeyDown)
}

// 开始游戏
const startGame = async () => {
  try {
    const response = await fetch('/api/game/start', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      }
    })
    
    if (!response.ok) {
      throw new Error('Failed to start game')
    }
    
    const gameState = await response.json()
    gameId.value = gameState.gameId
    updateBoard(gameState)
    
    gameStatus.value = '游戏进行中'
    gameStarted.value = true
    gamePaused.value = false
    
    // 开始游戏循环
    gameLoop.value = setInterval(updateGame, speed.value)
  } catch (error) {
    console.error('Error starting game:', error)
    gameStatus.value = '游戏开始失败'
  }
}

// 暂停游戏
const pauseGame = () => {
  gameStatus.value = '游戏暂停'
  gamePaused.value = true
  
  // 清除游戏循环
  if (gameLoop.value) {
    clearInterval(gameLoop.value)
    gameLoop.value = null
  }
}

// 停止游戏
const stopGame = () => {
  gameStatus.value = '游戏结束'
  gameStarted.value = false
  gamePaused.value = false
  
  // 清除游戏循环
  if (gameLoop.value) {
    clearInterval(gameLoop.value)
    gameLoop.value = null
  }
}

// 更新游戏
const updateGame = async () => {
  if (!gameId.value || !gameStarted.value || gamePaused.value) {
    return
  }
  
  try {
    const response = await fetch('/api/game/move', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        gameId: gameId.value,
        direction: direction.value.toUpperCase()
      })
    })
    
    if (!response.ok) {
      throw new Error('Failed to move snake')
    }
    
    const gameState = await response.json()
    if (gameState) {
      updateBoard(gameState)
      
      if (gameState.gameStatus === 'GAME_OVER') {
        stopGame()
      }
    } else {
      console.error('Invalid game state received')
      stopGame()
    }
  } catch (error) {
    console.error('Error updating game:', error)
    stopGame()
  }
}

// 更新棋盘
const updateBoard = (gameState) => {
  // 重置棋盘
  board.value = Array(boardSize).fill(null).map(() => Array(boardSize).fill(null))
  
  // 更新得分
  score.value = gameState.score
  
  // 更新贪吃蛇
  snake.value = gameState.snakeBody.map(segment => ({
    row: segment.y,
    col: segment.x
  }))
  
  // 放置贪吃蛇
  for (const segment of snake.value) {
    board.value[segment.row][segment.col] = 'snake'
  }
  
  // 更新食物
  food.value = {
    row: gameState.foodPosition.y,
    col: gameState.foodPosition.x
  }
  
  // 放置食物
  board.value[food.value.row][food.value.col] = 'food'
}

// 处理键盘事件
const handleKeyDown = (event) => {
  // 检查游戏是否已经开始
  if (!gameStarted.value || gamePaused.value) {
    return
  }
  
  // 处理方向键
  switch (event.key) {
    case 'ArrowUp':
      if (direction.value !== 'DOWN') {
        direction.value = 'UP'
      }
      break
    case 'ArrowDown':
      if (direction.value !== 'UP') {
        direction.value = 'DOWN'
      }
      break
    case 'ArrowLeft':
      if (direction.value !== 'RIGHT') {
        direction.value = 'LEFT'
      }
      break
    case 'ArrowRight':
      if (direction.value !== 'LEFT') {
        direction.value = 'RIGHT'
      }
      break
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

// 组件卸载时清除事件监听和游戏循环
onUnmounted(() => {
  window.removeEventListener('keydown', handleKeyDown)
  
  if (gameLoop.value) {
    clearInterval(gameLoop.value)
    gameLoop.value = null
  }
})
</script>

<style scoped>
.snake-container {
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
  width: 25px;
  height: 25px;
  border: 1px solid #999;
  background-color: #eee;
}

.board-cell.snake {
  background-color: green;
}

.board-cell.food {
  background-color: red;
  border-radius: 50%;
}

.board-cell.wall {
  background-color: gray;
}

.game-controls {
  display: flex;
  gap: 10px;
}

.game-instructions {
  font-size: 1.1rem;
}
</style>