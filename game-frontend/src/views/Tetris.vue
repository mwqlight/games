<template>
  <div class="tetris-container">
    <header class="game-header">
      <h1>俄罗斯方块游戏</h1>
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
            <span>等级: {{ level }}</span>
          </div>
          <div class="stat-item">
            <span>行数: {{ lines }}</span>
          </div>
          <div class="stat-item">
            <span>速度: {{ speed }}ms</span>
          </div>
        </div>
      </div>
      <div class="game-board">
        <div class="board-container">
          <div class="board-row" v-for="(row, rowIndex) in board" :key="rowIndex">
            <div 
              class="board-cell" 
              v-for="(cell, colIndex) in row" 
              :key="colIndex"
              :class="{
                'filled': cell !== null,
                'type-1': cell === 1,
                'type-2': cell === 2,
                'type-3': cell === 3,
                'type-4': cell === 4,
                'type-5': cell === 5,
                'type-6': cell === 6,
                'type-7': cell === 7
              }"
            ></div>
          </div>
        </div>
        <div class="next-piece">
          <h3>下一个</h3>
          <div class="next-piece-container">
            <div 
              class="next-cell" 
              v-for="(row, rowIndex) in nextPieceShape" 
              :key="rowIndex"
            >
              <div 
                class="next-cell-inner" 
                v-for="(cell, colIndex) in row" 
                :key="colIndex"
                :class="{
                  'filled': cell !== null,
                  'type-1': cell === 1,
                  'type-2': cell === 2,
                  'type-3': cell === 3,
                  'type-4': cell === 4,
                  'type-5': cell === 5,
                  'type-6': cell === 6,
                  'type-7': cell === 7
                }"
              ></div>
            </div>
          </div>
        </div>
      </div>
      <div class="game-controls">
        <el-button type="primary" @click="startGame" :disabled="gameStarted">开始游戏</el-button>
        <el-button type="warning" @click="pauseGame" :disabled="!gameStarted || gamePaused">暂停游戏</el-button>
        <el-button type="danger" @click="stopGame" :disabled="!gameStarted">停止游戏</el-button>
      </div>
      <div class="game-instructions">
        <h3>游戏控制说明</h3>
        <ul>
          <li>← 左箭头：移动方块向左</li>
          <li>→ 右箭头：移动方块向右</li>
          <li>↓ 下箭头：加速方块下落</li>
          <li>空格键：旋转方块</li>
        </ul>
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
const level = ref(1)
const lines = ref(0)
const speed = ref(1000)
const currentPiece = ref(null)
const nextPiece = ref(null)
const nextPieceShape = ref([])
const gameStarted = ref(false)
const gamePaused = ref(false)
const gameLoop = ref(null)

// 游戏配置
const boardWidth = 10
const boardHeight = 20

// 方块形状
const tetrominoes = [
  // I型
  { shape: [[1, 1, 1, 1]], color: 1 },
  // O型
  { shape: [[2, 2], [2, 2]], color: 2 },
  // T型
  { shape: [[0, 3, 0], [3, 3, 3]], color: 3 },
  // S型
  { shape: [[0, 4, 4], [4, 4, 0]], color: 4 },
  // Z型
  { shape: [[5, 5, 0], [0, 5, 5]], color: 5 },
  // J型
  { shape: [[6, 0, 0], [6, 6, 6]], color: 6 },
  // L型
  { shape: [[0, 0, 7], [7, 7, 7]], color: 7 }
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
  
  // 初始化等级
  level.value = 1
  
  // 初始化行数
  lines.value = 0
  
  // 初始化速度
  speed.value = 1000
  
  // 初始化棋盘
  board.value = Array(boardHeight).fill(null).map(() => Array(boardWidth).fill(null))
  
  // 生成当前方块和下一个方块
  currentPiece.value = generatePiece()
  nextPiece.value = generatePiece()
  nextPieceShape.value = nextPiece.value.shape
  
  // 添加键盘事件监听
  window.addEventListener('keydown', handleKeyDown)
}

// 生成方块
const generatePiece = () => {
  const tetromino = tetrominoes[Math.floor(Math.random() * tetrominoes.length)]
  return {
    shape: tetromino.shape,
    color: tetromino.color,
    row: 0,
    col: Math.floor(boardWidth / 2) - Math.floor(tetromino.shape[0].length / 2)
  }
}

// 处理键盘事件
const handleKeyDown = (event) => {
  // 检查游戏是否已经开始
  if (!gameStarted.value || gamePaused.value) {
    return
  }
  
  // 阻止默认事件，避免页面滚动
  event.preventDefault()
  
  // 处理方向键
  switch (event.key) {
    case 'ArrowLeft':
      movePiece(-1)
      console.log('左移方块')
      break
    case 'ArrowRight':
      movePiece(1)
      console.log('右移方块')
      break
    case 'ArrowDown':
      movePieceDown()
      console.log('加速下落')
      break
    case ' ': 
      rotatePiece()
      console.log('旋转方块')
      break
  }
}

// 移动方块
const movePiece = (direction) => {
  // 检查是否可以移动
  if (canMove(currentPiece.value.row, currentPiece.value.col + direction)) {
    // 清除当前位置的方块
    clearPiece(currentPiece.value)
    
    // 移动方块
    currentPiece.value.col += direction
    
    // 绘制方块
    drawPiece(currentPiece.value)
  }
}

// 移动方块到底部
const movePieceDown = () => {
  // 检查是否可以移动
  if (canMove(currentPiece.value.row + 1, currentPiece.value.col)) {
    // 清除当前位置的方块
    clearPiece(currentPiece.value)
    
    // 移动方块
    currentPiece.value.row += 1
    
    // 绘制方块
    drawPiece(currentPiece.value)
  } else {
    // 方块无法移动，固定方块
    fixPiece(currentPiece.value)
    
    // 检查是否有满行
    checkLines()
    
    // 生成新的方块
    currentPiece.value = nextPiece.value
    nextPiece.value = generatePiece()
    nextPieceShape.value = nextPiece.value.shape
    
    // 检查游戏是否结束
    if (!canMove(currentPiece.value.row, currentPiece.value.col)) {
      stopGame()
    }
  }
}

// 旋转方块
const rotatePiece = () => {
  // 保存当前形状
  const originalShape = currentPiece.value.shape
  
  // 旋转形状
  const rotatedShape = originalShape[0].map((_, index) => 
    originalShape.map(row => row[index]).reverse()
  )
  
  // 检查是否可以旋转
  if (canRotate(rotatedShape)) {
    // 清除当前位置的方块
    clearPiece(currentPiece.value)
    
    // 更新形状
    currentPiece.value.shape = rotatedShape
    
    // 绘制方块
    drawPiece(currentPiece.value)
  }
}

// 检查是否可以移动
const canMove = (row, col) => {
  for (let i = 0; i < currentPiece.value.shape.length; i++) {
    for (let j = 0; j < currentPiece.value.shape[i].length; j++) {
      if (currentPiece.value.shape[i][j] !== 0) {
        const newRow = row + i
        const newCol = col + j
        
        if (newRow < 0 || newRow >= boardHeight || 
            newCol < 0 || newCol >= boardWidth || 
            board.value[newRow][newCol] !== null) {
          return false
        }
      }
    }
  }
  
  return true
}

// 检查是否可以旋转
const canRotate = (shape) => {
  for (let i = 0; i < shape.length; i++) {
    for (let j = 0; j < shape[i].length; j++) {
      if (shape[i][j] !== 0) {
        const newRow = currentPiece.value.row + i
        const newCol = currentPiece.value.col + j
        
        if (newRow < 0 || newRow >= boardHeight || 
            newCol < 0 || newCol >= boardWidth || 
            board.value[newRow][newCol] !== null) {
          return false
        }
      }
    }
  }
  
  return true
}

// 清除方块
const clearPiece = (piece) => {
  for (let i = 0; i < piece.shape.length; i++) {
    for (let j = 0; j < piece.shape[i].length; j++) {
      if (piece.shape[i][j] !== 0) {
        const row = piece.row + i
        const col = piece.col + j
        
        if (row >= 0 && row < boardHeight && col >= 0 && col < boardWidth) {
          board.value[row][col] = null
        }
      }
    }
  }
}

// 绘制方块
const drawPiece = (piece) => {
  for (let i = 0; i < piece.shape.length; i++) {
    for (let j = 0; j < piece.shape[i].length; j++) {
      if (piece.shape[i][j] !== 0) {
        const row = piece.row + i
        const col = piece.col + j
        
        if (row >= 0 && row < boardHeight && col >= 0 && col < boardWidth) {
          board.value[row][col] = piece.color
        }
      }
    }
  }
}

// 固定方块
const fixPiece = (piece) => {
  for (let i = 0; i < piece.shape.length; i++) {
    for (let j = 0; j < piece.shape[i].length; j++) {
      if (piece.shape[i][j] !== 0) {
        const row = piece.row + i
        const col = piece.col + j
        
        if (row >= 0 && row < boardHeight && col >= 0 && col < boardWidth) {
          board.value[row][col] = piece.color
        }
      }
    }
  }
}

// 检查是否有满行
const checkLines = () => {
  let linesCleared = 0
  
  // 检查每一行
  for (let row = boardHeight - 1; row >= 0; row--) {
    // 检查行是否已满
    if (board.value[row].every(cell => cell !== null)) {
      // 清除行
      board.value.splice(row, 1)
      board.value.unshift(Array(boardWidth).fill(null))
      
      // 增加行数
      linesCleared++
      
      // 回退一行，继续检查
      row++
    }
  }
  
  // 更新得分和行数
  if (linesCleared > 0) {
    lines.value += linesCleared
    score.value += linesCleared * 100 * level.value
    
    // 检查是否升级
    if (lines.value >= level.value * 10) {
      level.value++
      speed.value = Math.max(100, speed.value - 100)
      
      // 重新设置游戏循环
      clearInterval(gameLoop.value)
      gameLoop.value = setInterval(updateGame, speed.value)
    }
  }
}

// 开始游戏
const startGame = () => {
  gameStatus.value = '游戏进行中'
  gameStarted.value = true
  gamePaused.value = false
  
  // 绘制初始方块
  drawPiece(currentPiece.value)
  
  // 开始游戏循环
  if (gameLoop.value) {
    clearInterval(gameLoop.value)
  }
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
  if (gameStarted.value && !gamePaused.value) {
    movePieceDown()
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
.tetris-container {
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
  gap: 20px;
  align-items: flex-start;
}

.board-container {
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

.board-cell.filled {
  border: 1px solid #333;
}

.board-cell.type-1 {
  background-color: cyan;
}

.board-cell.type-2 {
  background-color: yellow;
}

.board-cell.type-3 {
  background-color: purple;
}

.board-cell.type-4 {
  background-color: green;
}

.board-cell.type-5 {
  background-color: red;
}

.board-cell.type-6 {
  background-color: blue;
}

.board-cell.type-7 {
  background-color: orange;
}

.next-piece {
  display: flex;
  flex-direction: column;
  gap: 10px;
  text-align: center;
}

.next-piece h3 {
  font-size: 1.2rem;
  margin: 0;
}

.next-piece-container {
  display: flex;
  flex-direction: column;
  border: 2px solid white;
  border-radius: 8px;
  overflow: hidden;
  background-color: #ccc;
  padding: 10px;
}

.next-cell {
  display: flex;
}

.next-cell-inner {
  width: 20px;
  height: 20px;
  border: 1px solid #999;
  background-color: #eee;
}

.next-cell-inner.filled {
  border: 1px solid #333;
}

.next-cell-inner.type-1 {
  background-color: cyan;
}

.next-cell-inner.type-2 {
  background-color: yellow;
}

.next-cell-inner.type-3 {
  background-color: purple;
}

.next-cell-inner.type-4 {
  background-color: green;
}

.next-cell-inner.type-5 {
  background-color: red;
}

.next-cell-inner.type-6 {
  background-color: blue;
}

.next-cell-inner.type-7 {
  background-color: orange;
}

.game-controls {
  display: flex;
  gap: 10px;
}

.game-instructions {
  font-size: 1.1rem;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 2px solid white;
  border-radius: 8px;
  padding: 20px;
  margin-top: 20px;
}
</style>