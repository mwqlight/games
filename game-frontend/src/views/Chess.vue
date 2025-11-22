<template>
  <div class="chess-container">
    <header class="game-header">
      <h1>国际象棋游戏</h1>
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
            <span>白棋: {{ whiteCount }}</span>
          </div>
          <div class="stat-item">
            <span>黑棋: {{ blackCount }}</span>
          </div>
          <div class="stat-item">
            <span>当前玩家: {{ currentPlayer === 1 ? '白棋' : '黑棋' }}</span>
          </div>
        </div>
      </div>
      <div class="game-board">
        <div 
          class="board-cell" 
          v-for="(row, rowIndex) in board" 
          :key="rowIndex"
          :class="{
            'white': (rowIndex + colIndex) % 2 === 0,
            'black': (rowIndex + colIndex) % 2 === 1,
            'selected': selectedPiece && selectedPiece.row === rowIndex && selectedPiece.col === colIndex,
            'valid-move': cell.validMove
          }"
          @click="handleCellClick(rowIndex, colIndex)"
        >
          <div v-if="cell.value !== 0">
            <span>{{ getPieceSymbol(cell.value) }}</span>
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
        <p>点击棋子进行移动，按照国际象棋规则进行游戏</p>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { startGame, getGameState, makeMove } from '../services/gameApi'

const router = useRouter()

// 初始化游戏状态
const board = ref([])
const selectedPiece = ref(null)
const gameStatus = ref('准备开始')
const whiteCount = ref(0)
const blackCount = ref(0)
const currentPlayer = ref('red')
const gameStarted = ref(false)
const gamePaused = ref(false)
const timer = ref(null)
const gameOver = ref(false)

// 游戏配置
const boardSize = ref(8)
const initialWhiteCount = ref(16)
const initialBlackCount = ref(16)
const initialPlayer = ref(1)

// 棋子类型
const pieceTypes = {
  pawn: 1,
  rook: 2,
  knight: 3,
  bishop: 4,
  queen: 5,
  king: 6
}

// 初始化游戏
const initGame = async () => {
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
  whiteCount.value = 16
  blackCount.value = 16
  
  // 初始化当前玩家
  currentPlayer.value = 'red'
  
  // 初始化选中单元格
  selectedPiece.value = null
  
  // 生成棋盘
  generateBoard()
  
  // 从后端获取初始游戏状态
  try {
    const gameState = await getGameState()
    updateBoard(gameState.board)
    currentPlayer.value = gameState.currentPlayer
    gameStatus.value = gameState.gameStatus
  } catch (error) {
    console.error('Failed to get game state:', error)
  }
}

// 生成棋盘
const generateBoard = () => {
  // 创建棋盘
  board.value = Array(10).fill(null).map(() => Array(9).fill(null).map(() => ({
    value: 0,
    validMove: false
  })))
}

// 更新棋盘
const updateBoard = (chessPieces) => {
  // 清除棋盘
  for (let row = 0; row < 10; row++) {
    for (let col = 0; col < 9; col++) {
      board.value[row][col].value = 0
    }
  }
  
  // 放置棋子
  chessPieces.forEach(piece => {
    if (!piece.captured) {
      const row = piece.y
      const col = piece.x
      
      const pieceType = piece.type.toLowerCase()
      const color = piece.color === 'red' ? 1 : 2
      
      switch (pieceType) {
        case 'pawn':
          board.value[row][col].value = 1 + color * 10
          break
        case 'rook':
          board.value[row][col].value = 2 + color * 10
          break
        case 'knight':
          board.value[row][col].value = 3 + color * 10
          break
        case 'bishop':
          board.value[row][col].value = 4 + color * 10
          break
        case 'queen':
          board.value[row][col].value = 5 + color * 10
          break
        case 'king':
          board.value[row][col].value = 6 + color * 10
          break
      }
    }
  })
}

// 获取棋子符号
const getPieceSymbol = (value) => {
  const pieceType = value % 10
  const color = Math.floor(value / 10)
  
  switch (pieceType) {
    case pieceTypes.pawn:
      return color === 1 ? '♙' : '♟'
    case pieceTypes.rook:
      return color === 1 ? '♖' : '♜'
    case pieceTypes.knight:
      return color === 1 ? '♘' : '♞'
    case pieceTypes.bishop:
      return color === 1 ? '♗' : '♝'
    case pieceTypes.queen:
      return color === 1 ? '♕' : '♛'
    case pieceTypes.king:
      return color === 1 ? '♔' : '♚'
    default:
      return ''
  }
}

// 检查有效移动
const checkValidMoves = () => {
  // 清除所有有效移动标记
  for (let row = 0; row < 10; row++) {
    for (let col = 0; col < 9; col++) {
      board.value[row][col].validMove = false
    }
  }
}

// 检查当前玩家的棋子
const isCurrentPlayerPiece = (value) => {
  if (value === 0) {
    return false
  }
  
  const color = Math.floor(value / 10)
  
  return (color === 1 && currentPlayer.value === 'red') || (color === 2 && currentPlayer.value === 'black')
}

// 下棋
const makeMove = async (fromX, fromY, toX, toY) => {
  if (!gameStarted.value || gamePaused.value) { return }
  
  try {
    const gameState = await makeMove({ fromX, fromY, toX, toY })
    updateBoard(gameState.board)
    currentPlayer.value = gameState.currentPlayer
    gameStatus.value = gameState.gameStatus
    
    if (gameState.gameStatus === 'RED_WIN' || gameState.gameStatus === 'BLACK_WIN') {
      gameStarted.value = false
      gameOver.value = true
      clearInterval(timer.value)
    }
  } catch (error) {
    console.error('Failed to make move:', error)
  }
}

// 处理单元格点击
const handleCellClick = (row, col) => {
  if (gameOver.value) return
  
  // 如果点击的是当前玩家的棋子，显示其有效移动
  if (isCurrentPlayerPiece(board.value[row][col].value)) {
    selectedPiece.value = { row, col }
    checkValidMoves()
  } 
  // 如果点击的是有效移动位置，移动棋子
  else if (board.value[row][col].validMove && selectedPiece.value) {
    const fromX = selectedPiece.value.col
    const fromY = selectedPiece.value.row
    const toX = col
    const toY = row
    makeMove(fromX, fromY, toX, toY)
    selectedPiece.value = null
  }
  // 否则取消选择
  else {
    selectedPiece.value = null
    checkValidMoves()
  }
}















// 检查是否是对方的棋子
const isOpponentPiece = (value) => {
  if (value === 0) {
    return false
  }
  
  const color = Math.floor(value / 10)
  
  return (color === 1 && currentPlayer.value === 'black') || (color === 2 && currentPlayer.value === 'red')
}







// 开始游戏
const startGame = async () => {
  if (!gameStarted.value) {
    try {
      const gameState = await startGame()
      updateBoard(gameState.board)
      currentPlayer.value = gameState.currentPlayer
      gameStatus.value = gameState.gameStatus
      gameStarted.value = true
      gameOver.value = false
      
      // 开始计时器
      timer.value = setInterval(() => {
        time.value++
      }, 1000)
    } catch (error) {
      console.error('Failed to start game:', error)
    }
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
const restartGame = async () => {
  if (gameStarted.value) {
    stopGame()
  }
  
  try {
    const gameState = await startGame()
    updateBoard(gameState.board)
    currentPlayer.value = gameState.currentPlayer
    gameStatus.value = gameState.gameStatus
    gameStarted.value = true
    gamePaused.value = false
    gameOver.value = false
    
    // 重置计时器
    whiteTime.value = initialTime.value
    blackTime.value = initialTime.value
    
    // 开始计时
    startTimer()
  } catch (error) {
    console.error('Failed to restart game:', error)
  }
}

// 返回主页
const goBack = () => {
  router.push('/')
}

// 组件挂载时初始化游戏
onMounted(async () => {
  await initGame()
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
.chess-container {
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
  grid-template-columns: repeat(9, 1fr);
  grid-template-rows: repeat(10, 1fr);
  width: 450px;
  height: 500px;
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
}

.board-cell.white {
  background-color: rgba(255, 255, 255, 0.2);
}

.board-cell.black {
  background-color: rgba(0, 0, 0, 0.2);
}

.board-cell.selected {
  background-color: rgba(255, 255, 0, 0.2);
  border-color: yellow;
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