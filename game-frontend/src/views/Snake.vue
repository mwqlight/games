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
const direction = ref('right')
const gameStarted = ref(false)
const gamePaused = ref(false)
const gameLoop = ref(null)

// 游戏配置
const boardSize = 20
const initialSnake = [
  { row: 10, col: 10 },
  { row: 10, col: 9 },
  { row: 10, col: 8 }
]

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
  snake.value = JSON.parse(JSON.stringify(initialSnake))
  
  // 初始化方向
  direction.value = 'right'
  
  // 初始化棋盘
  board.value = Array(boardSize).fill(null).map(() => Array(boardSize).fill(null))
  
  // 放置贪吃蛇
  for (const segment of snake.value) {
    board.value[segment.row][segment.col] = 'snake'
  }
  
  // 放置食物
  placeFood()
  
  // 添加键盘事件监听
  window.addEventListener('keydown', handleKeyDown)
}

// 放置食物
const placeFood = () => {
  let row, col
  
  // 随机生成食物位置，确保不在贪吃蛇身上
  do {
    row = Math.floor(Math.random() * boardSize)
    col = Math.floor(Math.random() * boardSize)
  } while (board.value[row][col] === 'snake')
  
  // 放置食物
  board.value[row][col] = 'food'
  food.value = { row, col }
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
      if (direction.value !== 'down') {
        direction.value = 'up'
      }
      break
    case 'ArrowDown':
      if (direction.value !== 'up') {
        direction.value = 'down'
      }
      break
    case 'ArrowLeft':
      if (direction.value !== 'right') {
        direction.value = 'left'
      }
      break
    case 'ArrowRight':
      if (direction.value !== 'left') {
        direction.value = 'right'
      }
      break
  }
}

// 开始游戏
const startGame = () => {
  gameStatus.value = '游戏进行中'
  gameStarted.value = true
  gamePaused.value = false
  
  // 开始游戏循环
  gameLoop.value = setInterval(updateGame, speed.value)
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
const updateGame = () => {
  // 获取贪吃蛇头部
  const head = snake.value[0]
  
  // 计算新的头部位置
  let newHead
  
  switch (direction.value) {
    case 'up':
      newHead = { row: head.row - 1, col: head.col }
      break
    case 'down':
      newHead = { row: head.row + 1, col: head.col }
      break
    case 'left':
      newHead = { row: head.row, col: head.col - 1 }
      break
    case 'right':
      newHead = { row: head.row, col: head.col + 1 }
      break
  }
  
  // 检查是否撞墙
  if (newHead.row < 0 || newHead.row >= boardSize || newHead.col < 0 || newHead.col >= boardSize) {
    stopGame()
    return
  }
  
  // 检查是否撞到自己
  if (board.value[newHead.row][newHead.col] === 'snake') {
    stopGame()
    return
  }
  
  // 检查是否吃到食物
  if (board.value[newHead.row][newHead.col] === 'food') {
    // 增加得分
    score.value += 10
    
    // 增加贪吃蛇长度
    snake.value.unshift(newHead)
    board.value[newHead.row][newHead.col] = 'snake'
    
    // 放置新的食物
    placeFood()
    
    // 提高速度
    if (speed.value > 50) {
      speed.value -= 5
      
      // 重新设置游戏循环
      clearInterval(gameLoop.value)
      gameLoop.value = setInterval(updateGame, speed.value)
    }
  } else {
    // 移动贪吃蛇
    snake.value.unshift(newHead)
    board.value[newHead.row][newHead.col] = 'snake'
    
    // 移除尾部
    const tail = snake.value.pop()
    board.value[tail.row][tail.col] = null
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