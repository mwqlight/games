<template>
  <div class="backgammon-container">
    <header class="game-header">
      <h1>双陆棋游戏</h1>
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
            <span>白方: {{ whiteCount }}</span>
          </div>
          <div class="stat-item">
            <span>黑方: {{ blackCount }}</span>
          </div>
          <div class="stat-item">
            <span>当前玩家: {{ currentPlayer === 1 ? '白方' : '黑方' }}</span>
          </div>
        </div>
      </div>
      <div class="game-board">
        <div class="board-section">
          <div class="board-row">
            <div 
              class="board-cell" 
              v-for="(cell, index) in board" 
              :key="index"
              :class="{
                'white': (index % 2 === 0),
                'black': (index % 2 === 1),
                'selected': selectedCell === index,
                'valid-move': cell.validMove
              }"
              @click="makeMove(index)"
            >
              <div v-if="cell.value !== 0">
                <span>{{ getPieceSymbol(cell.value) }}</span>
              </div>
              <div v-else-if="cell.validMove">
                <span>✓</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="game-controls">
        <el-button type="primary" @click="startGame">开始游戏</el-button>
        <el-button type="warning" @click="pauseGame">暂停游戏</el-button>
        <el-button type="danger" @click="stopGame">停止游戏</el-button>
      </div>
      <div class="game-instructions">
        <p>点击棋子进行移动，按照双陆棋规则进行游戏</p>
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
const selectedCell = ref(null)
const gameStatus = ref('准备开始')
const whiteCount = ref(0)
const blackCount = ref(0)
const currentPlayer = ref(1)
const gameStarted = ref(false)
const gamePaused = ref(false)
const timer = ref(null)

// 游戏配置
const boardSize = ref(24)
const initialWhiteCount = ref(15)
const initialBlackCount = ref(15)
const initialPlayer = ref(1)

// 棋子类型
const pieceTypes = {
  white: 1,
  black: 2
}

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
  whiteCount.value = initialWhiteCount.value
  blackCount.value = initialBlackCount.value
  
  // 初始化当前玩家
  currentPlayer.value = initialPlayer.value
  
  // 初始化选中单元格
  selectedCell.value = null
  
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
  board.value = Array(boardSize.value).fill(null).map(() => ({
    value: 0,
    validMove: false
  }))
}

// 设置初始棋子
const setInitialPieces = () => {
  // 设置白方
  board.value[0].value = pieceTypes.white + 5 // 白方5个棋子
  board.value[11].value = pieceTypes.white + 3 // 白方3个棋子
  board.value[16].value = pieceTypes.white + 5 // 白方5个棋子
  board.value[18].value = pieceTypes.white + 2 // 白方2个棋子
  
  // 设置黑方
  board.value[5].value = pieceTypes.black + 5 // 黑方5个棋子
  board.value[7].value = pieceTypes.black + 3 // 黑方3个棋子
  board.value[12].value = pieceTypes.black + 5 // 黑方5个棋子
  board.value[19].value = pieceTypes.black + 2 // 黑方2个棋子
}

// 获取棋子符号
const getPieceSymbol = (value) => {
  const pieceType = value % 10
  const count = Math.floor(value / 10)
  
  switch (pieceType) {
    case pieceTypes.white:
      return '⚪'.repeat(count)
    case pieceTypes.black:
      return '⚫'.repeat(count)
    default:
      return ''
  }
}

// 检查有效移动
const checkValidMoves = () => {
  // 清除所有有效移动标记
  for (let i = 0; i < boardSize.value; i++) {
    board.value[i].validMove = false
  }
  
  // 检查所有棋子
  for (let i = 0; i < boardSize.value; i++) {
    if (isCurrentPlayerPiece(board.value[i].value)) {
      // 检查是否有可以移动的位置
      checkPieceValidMoves(i)
    }
  }
}

// 检查当前玩家的棋子
const isCurrentPlayerPiece = (value) => {
  if (value === 0) {
    return false
  }
  
  const pieceType = value % 10
  
  return pieceType === currentPlayer.value
}

// 检查棋子的有效移动
const checkPieceValidMoves = (rowIndex) => {
  // 获取棋子类型
  const pieceType = board.value[rowIndex].value % 10
  
  // 根据棋子类型检查有效移动
  switch (pieceType) {
    case pieceTypes.white:
      checkWhiteValidMoves(rowIndex)
      break
    case pieceTypes.black:
      checkBlackValidMoves(rowIndex)
      break
  }
}

// 检查白方的有效移动
const checkWhiteValidMoves = (rowIndex) => {
  // 检查可以移动的位置
  for (let i = 1; i <= 6; i++) {
    // 计算新位置
    let newRow = rowIndex + i
    
    if (newRow < boardSize.value) {
      if (board.value[newRow].value === 0 || isOpponentPiece(board.value[newRow].value)) {
        // 设置有效移动标记
        board.value[newRow].validMove = true
      }
    }
  }
}

// 检查黑方的有效移动
const checkBlackValidMoves = (rowIndex) => {
  // 检查可以移动的位置
  for (let i = 1; i <= 6; i++) {
    // 计算新位置
    let newRow = rowIndex - i
    
    if (newRow >= 0) {
      if (board.value[newRow].value === 0 || isOpponentPiece(board.value[newRow].value)) {
        // 设置有效移动标记
        board.value[newRow].validMove = true
      }
    }
  }
}

// 检查是否是对方的棋子
const isOpponentPiece = (value) => {
  if (value === 0) {
    return false
  }
  
  const pieceType = value % 10
  
  return pieceType !== currentPlayer.value
}

// 下棋
const makeMove = (rowIndex) => {
  if (!gameStarted.value || gamePaused.value) {
    return
  }
  
  // 检查是否已经选中棋子
  if (selectedCell.value) {
    // 检查是否是有效移动
    if (board.value[rowIndex].validMove) {
      // 移动棋子
      movePiece(selectedCell.value, rowIndex)
      
      // 清除选中状态
      selectedCell.value = null
      
      // 检查有效移动
      checkValidMoves()
      
      // 检查游戏是否结束
      checkGameEnd()
    } else {
      // 检查是否是当前玩家的棋子
      if (isCurrentPlayerPiece(board.value[rowIndex].value)) {
        // 选中新的棋子
        selectedCell.value = rowIndex
      } else {
        // 清除选中状态
        selectedCell.value = null
      }
    }
  } else {
    // 检查是否是当前玩家的棋子
    if (isCurrentPlayerPiece(board.value[rowIndex].value)) {
      // 选中棋子
      selectedCell.value = rowIndex
    }
  }
}

// 移动棋子
const movePiece = (fromRow, toRow) => {
  // 获取棋子类型
  const pieceType = board.value[fromRow].value % 10
  const count = Math.floor(board.value[fromRow].value / 10)
  
  // 检查是否吃掉对方棋子
  if (isOpponentPiece(board.value[toRow].value)) {
    // 更新棋子数量
    if (currentPlayer.value === 1) {
      blackCount.value--
    } else {
      whiteCount.value--
    }
  }
  
  // 移动棋子
  board.value[toRow].value = pieceType + count * 10
  board.value[fromRow].value = 0
  
  // 切换玩家
  currentPlayer.value = currentPlayer.value === 1 ? 2 : 1
}

// 检查游戏结束
const checkGameEnd = () => {
  // 检查是否有有效移动
  let hasValidMove = false
  
  for (let i = 0; i < boardSize.value; i++) {
    if (board.value[i].validMove) {
      hasValidMove = true
      break
    }
  }
  
  if (!hasValidMove) {
    // 游戏结束
    gameStatus.value = '游戏结束'
    clearInterval(timer.value)
    
    // 计算得分
    if (whiteCount.value > blackCount.value) {
      score.value = whiteCount.value - blackCount.value
    } else if (blackCount.value > whiteCount.value) {
      score.value = blackCount.value - whiteCount.value
    } else {
      score.value = 0
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
.backgammon-container {
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
  width: 800px;
  height: 400px;
  border: 2px solid white;
  border-radius: 8px;
  overflow: hidden;
  background-color: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
}

.board-section {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.board-row {
  flex: 1;
  display: flex;
  flex-direction: row;
}

.board-cell {
  flex: 1;
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