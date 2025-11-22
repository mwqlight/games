<template>
  <div class="game-container">
    <h1>中国象棋</h1>
    <div class="game-info">
      <div class="info-item">
        <label>游戏状态:</label>
        <span>{{ gameStatus }}</span>
      </div>
      <div class="info-item">
        <label>当前玩家:</label>
        <span>{{ currentPlayer === 'red' ? '红方' : '黑方' }}</span>
      </div>
      <div class="info-item">
        <label>游戏时间:</label>
        <span>{{ time }}秒</span>
      </div>
      <div class="info-item">
        <label>得分:</label>
        <span>{{ score }}</span>
      </div>
    </div>
    <div class="board-container">
      <div class="board">
        <div v-for="(row, rowIndex) in board" :key="rowIndex" class="row">
          <div v-for="(cell, colIndex) in row" :key="colIndex" 
               :class="[
                 'cell', 
                 { 'black': (rowIndex + colIndex) % 2 === 1 }, 
                 { 'selected': selectedCell && selectedCell.row === rowIndex && selectedCell.col === colIndex },
                 { 'valid-move': cell.validMove }
               ]"
               @click.stop="handleCellClick(rowIndex, colIndex)">
            <span v-if="cell.piece" 
                  :class="[
                    'piece', 
                    { 'red': cell.piece.color === 'red' }, 
                    { 'black': cell.piece.color === 'black' }
                  ]">
              {{ getPieceSymbol(cell.piece) }}
            </span>
          </div>
        </div>
      </div>
    </div>
    <div class="game-controls">
      <button @click="startNewGame" :disabled="gameStarted">开始游戏</button>
      <button @click="pauseGame" :disabled="!gameStarted">暂停游戏</button>
      <button @click="stopGame" :disabled="!gameStarted">停止游戏</button>
      <button @click="restartGame">重新开始</button>
    </div>
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
const currentPlayer = ref('red')
const gameStarted = ref(false)
const timer = ref(null)

// 生成棋盘
const generateBoard = () => {
  // 创建棋盘 (9列10行)
  board.value = Array(9).fill(null).map(() => Array(10).fill(null).map(() => ({
    piece: null,
    validMove: false
  })))
}

// 从后端获取游戏状态并更新棋盘
const updateBoardFromBackend = (gameState) => {
  generateBoard()
  
  // 更新棋盘
  for (let x = 0; x < 9; x++) {
    for (let y = 0; y < 10; y++) {
      board.value[x][y].piece = gameState.board[x][y]
    }
  }
  
  // 更新游戏状态
  gameStatus.value = gameState.gameStatus === 'PLAYING' 
    ? (gameState.currentPlayer === 'red' ? '红方行棋' : '黑方（电脑）思考中') 
    : (gameState.gameStatus === 'RED_WIN' ? '红方获胜' : '黑方获胜')
  
  currentPlayer.value = gameState.currentPlayer
}

// 开始新游戏
const startNewGame = async () => {
  try {
    const response = await fetch('http://localhost:8080/api/game/start', {
      method: 'POST'
    })
    const gameState = await response.json()
    updateBoardFromBackend(gameState)
    gameStarted.value = true
  } catch (error) {
    console.error('开始游戏失败:', error)
    gameStatus.value = '开始游戏失败'
  }
}

// 获取当前游戏状态
const getCurrentGameState = async () => {
  try {
    const response = await fetch('http://localhost:8080/api/game/state')
    const gameState = await response.json()
    updateBoardFromBackend(gameState)
  } catch (error) {
    console.error('获取游戏状态失败:', error)
  }
}

// 处理玩家走子
const makeMove = async (x, y) => {
  if (!gameStarted.value || currentPlayer.value !== 'red') {
    return
  }
  
  // 检查是否已经选中棋子
  if (selectedCell.value) {
    // 检查是否是有效移动
    if (board.value[x][y].validMove) {
      // 发送走子请求
      try {
        const response = await fetch(`http://localhost:8080/api/game/move?fromX=${selectedCell.value.x}&fromY=${selectedCell.value.y}&toX=${x}&toY=${y}`, {
          method: 'POST'
        })
        const gameState = await response.json()
        updateBoardFromBackend(gameState)
      } catch (error) {
        console.error('走子失败:', error)
        gameStatus.value = '走子失败'
      }
      
      // 清除选中状态
      selectedCell.value = null
      clearValidMoves()
    } else {
      // 检查是否是当前玩家的棋子
      if (board.value[x][y].piece && board.value[x][y].piece.color === 'red') {
        // 选中新的棋子
        selectedCell.value = { x, y }
        clearValidMoves()
        // TODO: 从后端获取合法走法并高亮
      } else {
        // 清除选中状态
        selectedCell.value = null
        clearValidMoves()
      }
    }
  } else {
    // 检查是否是当前玩家的棋子
    if (board.value[x][y].piece && board.value[x][y].piece.color === 'red') {
      // 选中棋子
      selectedCell.value = { x, y }
      clearValidMoves()
      // TODO: 从后端获取合法走法并高亮
    }
  }
}

// 清除所有有效移动标记
const clearValidMoves = () => {
  for (let x = 0; x < 9; x++) {
    for (let y = 0; y < 10; y++) {
      board.value[x][y].validMove = false
    }
  }
}

// 获取棋子符号
const getPieceSymbol = (piece) => {
  if (!piece) return ''
  
  switch (piece.type) {
    case 'rook':
      return piece.color === 'red' ? '车' : '車'
    case 'knight':
      return piece.color === 'red' ? '马' : '馬'
    case 'bishop':
      return piece.color === 'red' ? '相' : '象'
    case 'advisor':
      return piece.color === 'red' ? '仕' : '士'
    case 'king':
      return piece.color === 'red' ? '帅' : '将'
    case 'cannon':
      return piece.color === 'red' ? '炮' : '砲'
    case 'pawn':
      return piece.color === 'red' ? '兵' : '卒'
    default:
      return ''
  }
}

// 重新开始游戏
const restartGame = async () => {
  await startNewGame()
}

// 暂停游戏
const pauseGame = () => {
  // 前端不处理暂停，由后端控制游戏状态
  console.log('暂停游戏功能需要后端支持')
}

// 停止游戏
const stopGame = () => {
  gameStatus.value = '游戏已停止'
  gameStarted.value = false
  selectedCell.value = null
  clearValidMoves()
  generateBoard()
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
.chinese-chess-container {
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

.board-cell.red {
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