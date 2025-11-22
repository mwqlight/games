<template>
  <div class="game-container">
    <el-card class="game-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon class="game-icon"><Trophy /></el-icon>
          <span class="game-title">象棋游戏</span>
          <el-button 
            type="primary" 
            size="small"
            @click="goBack"
            class="back-button"
          >
            <el-icon><ArrowLeft /></el-icon>
            返回主页
          </el-button>
        </div>
      </template>

      <div class="game-content">
        <!-- 游戏信息 -->
        <div class="game-info">
          <div class="current-player">
            当前玩家: 
            <span class="player-mark" :class="currentPlayer">{{ currentPlayer === 'red' ? '红方' : '黑方' }}</span>
          </div>
          <div class="game-status" :class="gameStatusClass">
            {{ gameStatus }}
          </div>
        </div>

        <!-- 游戏棋盘 -->
        <div class="game-board">
          <!-- 棋盘坐标 -->
          <div class="coordinates">
            <div class="row-coordinates">
              <span v-for="i in 9" :key="i">{{ i }}</span>
            </div>
            <div class="board-container">
              <div class="column-coordinates">
                <span v-for="i in 10" :key="i">{{ String.fromCharCode(64 + i) }}</span>
              </div>
              <div class="chessboard">
                <div 
                  v-for="(row, rowIndex) in board" 
                  :key="rowIndex"
                  class="board-row"
                >
                  <div 
                    v-for="(cell, colIndex) in row" 
                    :key="colIndex"
                    class="board-cell"
                    :class="{ 'cell-dark': (rowIndex + colIndex) % 2 === 1, 'cell-selected': selectedCell === `${rowIndex}-${colIndex}`, 'cell-valid': validMoves.includes(`${rowIndex}-${colIndex}`) }"
                    @click="selectCell(rowIndex, colIndex)"
                  >
                    <div v-if="cell" class="chess-piece" :class="cell.color" @dragstart="handleDragStart($event, rowIndex, colIndex)" draggable="true">
                      {{ cell.name }}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 游戏控制 -->
        <div class="game-controls">
          <el-button type="primary" @click="resetGame">重新开始</el-button>
          <el-button @click="toggleAIMode">
            {{ aiMode ? '关闭AI' : '开启AI' }}
          </el-button>
          <el-button @click="undoMove" :disabled="history.length === 0">悔棋</el-button>
          <el-button @click="flipBoard">翻转棋盘</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Trophy, ArrowLeft } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 游戏状态
const board = ref([])
const currentPlayer = ref('red')
const gameStatus = ref('游戏进行中')
const gameStatusClass = ref('status-playing')
const selectedCell = ref('')
const validMoves = ref([])
const aiMode = ref(false)
const history = ref([])
const boardFlipped = ref(false)

// 初始化棋盘
const initBoard = () => {
  const newBoard = []
  
  // 初始化空棋盘
  for (let i = 0; i < 10; i++) {
    newBoard.push(new Array(9).fill(null))
  }
  
  // 放置黑方棋子
  newBoard[0] = [
    { name: '车', color: 'black', type: 'rook' },
    { name: '马', color: 'black', type: 'knight' },
    { name: '象', color: 'black', type: 'bishop' },
    { name: '士', color: 'black', type: 'advisor' },
    { name: '将', color: 'black', type: 'king' },
    { name: '士', color: 'black', type: 'advisor' },
    { name: '象', color: 'black', type: 'bishop' },
    { name: '马', color: 'black', type: 'knight' },
    { name: '车', color: 'black', type: 'rook' }
  ]
  
  newBoard[2][1] = { name: '炮', color: 'black', type: 'cannon' }
  newBoard[2][7] = { name: '炮', color: 'black', type: 'cannon' }
  
  for (let i = 0; i < 9; i++) {
    newBoard[3][i] = { name: '兵', color: 'black', type: 'pawn' }
  }
  
  // 放置红方棋子
  newBoard[9] = [
    { name: '车', color: 'red', type: 'rook' },
    { name: '马', color: 'red', type: 'knight' },
    { name: '相', color: 'red', type: 'bishop' },
    { name: '仕', color: 'red', type: 'advisor' },
    { name: '帅', color: 'red', type: 'king' },
    { name: '仕', color: 'red', type: 'advisor' },
    { name: '相', color: 'red', type: 'bishop' },
    { name: '马', color: 'red', type: 'knight' },
    { name: '车', color: 'red', type: 'rook' }
  ]
  
  newBoard[7][1] = { name: '炮', color: 'red', type: 'cannon' }
  newBoard[7][7] = { name: '炮', color: 'red', type: 'cannon' }
  
  for (let i = 0; i < 9; i++) {
    newBoard[6][i] = { name: '兵', color: 'red', type: 'pawn' }
  }
  
  board.value = newBoard
}

// 检查棋子是否可以移动到目标位置
const isValidMove = (fromRow, fromCol, toRow, toCol) => {
  const piece = board.value[fromRow][fromCol]
  if (!piece) return false
  
  // 检查是否是当前玩家的棋子
  if (piece.color !== currentPlayer.value) return false
  
  // 检查目标位置是否有己方棋子
  const targetPiece = board.value[toRow][toCol]
  if (targetPiece && targetPiece.color === piece.color) return false
  
  // 根据棋子类型检查移动规则
  switch (piece.type) {
    case 'rook': // 车
      return isValidRookMove(fromRow, fromCol, toRow, toCol)
    case 'knight': // 马
      return isValidKnightMove(fromRow, fromCol, toRow, toCol)
    case 'bishop': // 象/相
      return isValidBishopMove(fromRow, fromCol, toRow, toCol, piece.color)
    case 'advisor': // 士/仕
      return isValidAdvisorMove(fromRow, fromCol, toRow, toCol, piece.color)
    case 'king': // 将/帅
      return isValidKingMove(fromRow, fromCol, toRow, toCol, piece.color)
    case 'pawn': // 兵/卒
      return isValidPawnMove(fromRow, fromCol, toRow, toCol, piece.color)
    case 'cannon': // 炮
      return isValidCannonMove(fromRow, fromCol, toRow, toCol)
    default:
      return false
  }
}

// 车的移动规则
const isValidRookMove = (fromRow, fromCol, toRow, toCol) => {
  // 车必须走直线
  if (fromRow !== toRow && fromCol !== toCol) return false
  
  // 检查路径是否有棋子阻挡
  const stepRow = fromRow === toRow ? 0 : (toRow > fromRow ? 1 : -1)
  const stepCol = fromCol === toCol ? 0 : (toCol > fromCol ? 1 : -1)
  
  let currentRow = fromRow + stepRow
  let currentCol = fromCol + stepCol
  
  while (currentRow !== toRow || currentCol !== toCol) {
    if (board.value[currentRow][currentCol]) return false
    currentRow += stepRow
    currentCol += stepCol
  }
  
  return true
}

// 马的移动规则
const isValidKnightMove = (fromRow, fromCol, toRow, toCol) => {
  // 马走日字
  const rowDiff = Math.abs(toRow - fromRow)
  const colDiff = Math.abs(toCol - fromCol)
  
  if (!((rowDiff === 2 && colDiff === 1) || (rowDiff === 1 && colDiff === 2))) return false
  
  // 检查是否蹩马腿
  if (rowDiff === 2) {
    const middleRow = fromRow + (toRow > fromRow ? 1 : -1)
    if (board.value[middleRow][fromCol]) return false
  } else {
    const middleCol = fromCol + (toCol > fromCol ? 1 : -1)
    if (board.value[fromRow][middleCol]) return false
  }
  
  return true
}

// 象/相的移动规则
const isValidBishopMove = (fromRow, fromCol, toRow, toCol, color) => {
  // 象走田字
  const rowDiff = Math.abs(toRow - fromRow)
  const colDiff = Math.abs(toCol - fromCol)
  
  if (rowDiff !== 2 || colDiff !== 2) return false
  
  // 检查是否塞象眼
  const middleRow = (fromRow + toRow) / 2
  const middleCol = (fromCol + toCol) / 2
  
  if (board.value[middleRow][middleCol]) return false
  
  // 检查是否过河（红方不能过楚河，黑方不能过汉界）
  if (color === 'red' && toRow < 5) return false
  if (color === 'black' && toRow > 4) return false
  
  return true
}

// 士/仕的移动规则
const isValidAdvisorMove = (fromRow, fromCol, toRow, toCol, color) => {
  // 士走斜线，每次一格
  const rowDiff = Math.abs(toRow - fromRow)
  const colDiff = Math.abs(toCol - fromCol)
  
  if (rowDiff !== 1 || colDiff !== 1) return false
  
  // 检查是否在九宫格内
  if (color === 'red') {
    if (toRow < 7 || toRow > 9 || toCol < 3 || toCol > 5) return false
  } else {
    if (toRow < 0 || toRow > 2 || toCol < 3 || toCol > 5) return false
  }
  
  return true
}

// 将/帅的移动规则
const isValidKingMove = (fromRow, fromCol, toRow, toCol, color) => {
  // 将走直线，每次一格
  const rowDiff = Math.abs(toRow - fromRow)
  const colDiff = Math.abs(toCol - fromCol)
  
  if (!((rowDiff === 1 && colDiff === 0) || (rowDiff === 0 && colDiff === 1))) return false
  
  // 检查是否在九宫格内
  if (color === 'red') {
    if (toRow < 7 || toRow > 9 || toCol < 3 || toCol > 5) return false
  } else {
    if (toRow < 0 || toRow > 2 || toCol < 3 || toCol > 5) return false
  }
  
  // 检查是否对面
  return true
}

// 兵/卒的移动规则
const isValidPawnMove = (fromRow, fromCol, toRow, toCol, color) => {
  // 未过河时只能向前走
  const direction = color === 'red' ? -1 : 1
  
  if (color === 'red' && fromRow > 4 || color === 'black' && fromRow < 5) {
    // 已过河，可以左右走
    const rowDiff = toRow - fromRow
    const colDiff = Math.abs(toCol - fromCol)
    
    if (colDiff === 0) {
      // 向前走
      return rowDiff === direction
    } else if (colDiff === 1) {
      // 左右走
      return rowDiff === 0
    }
  } else {
    // 未过河，只能向前走
    return toRow - fromRow === direction && toCol === fromCol
  }
  
  return false
}

// 炮的移动规则
const isValidCannonMove = (fromRow, fromCol, toRow, toCol) => {
  // 炮走直线
  if (fromRow !== toRow && fromCol !== toCol) return false
  
  // 检查路径上的棋子数量
  const stepRow = fromRow === toRow ? 0 : (toRow > fromRow ? 1 : -1)
  const stepCol = fromCol === toCol ? 0 : (toCol > fromCol ? 1 : -1)
  
  let currentRow = fromRow + stepRow
  let currentCol = fromCol + stepCol
  let pieceCount = 0
  
  while (currentRow !== toRow || currentCol !== toCol) {
    if (board.value[currentRow][currentCol]) pieceCount++
    currentRow += stepRow
    currentCol += stepCol
  }
  
  // 炮吃子时需要隔一个棋子
  const targetPiece = board.value[toRow][toCol]
  if (targetPiece) {
    return pieceCount === 1
  } else {
    return pieceCount === 0
  }
}

// 获取所有合法移动
const getValidMoves = (row, col) => {
  const moves = []
  const piece = board.value[row][col]
  
  if (!piece || piece.color !== currentPlayer.value) return moves
  
  // 检查所有可能的位置
  for (let i = 0; i < 10; i++) {
    for (let j = 0; j < 9; j++) {
      if (isValidMove(row, col, i, j)) {
        moves.push(`${i}-${j}`)
      }
    }
  }
  
  return moves
}

// 选择单元格
const selectCell = (row, col) => {
  if (gameStatus.value !== '游戏进行中') return
  
  const piece = board.value[row][col]
  
  // 如果点击的是当前玩家的棋子，显示合法移动
  if (piece && piece.color === currentPlayer.value) {
    selectedCell.value = `${row}-${col}`
    validMoves.value = getValidMoves(row, col)
  } 
  // 如果点击的是合法移动位置，移动棋子
  else if (selectedCell.value && validMoves.value.includes(`${row}-${col}`)) {
    const [fromRow, fromCol] = selectedCell.value.split('-').map(Number)
    movePiece(fromRow, fromCol, row, col)
  }
  // 否则取消选择
  else {
    selectedCell.value = ''
    validMoves.value = []
  }
}

// 移动棋子
const movePiece = (fromRow, fromCol, toRow, toCol) => {
  saveHistory()
  
  // 移动棋子
  board.value[toRow][toCol] = board.value[fromRow][fromCol]
  board.value[fromRow][fromCol] = null
  
  // 检查是否获胜
  // TODO: 实现获胜检查逻辑
  
  // 切换玩家
  currentPlayer.value = currentPlayer.value === 'red' ? 'black' : 'red'
  
  // 清除选择
  selectedCell.value = ''
  validMoves.value = []
  
  // AI移动
  if (aiMode.value && currentPlayer.value === 'black') {
    setTimeout(aiMove, 500)
  }
}

// 保存历史记录
const saveHistory = () => {
  history.value.push(JSON.parse(JSON.stringify(board.value)))
}

// AI移动
const aiMove = () => {
  // 简单的AI策略：随机选择一个合法移动
  const moves = []
  
  for (let i = 0; i < 10; i++) {
    for (let j = 0; j < 9; j++) {
      const piece = board.value[i][j]
      if (piece && piece.color === 'black') {
        const validMoves = getValidMoves(i, j)
        for (const move of validMoves) {
          moves.push({ from: `${i}-${j}`, to: move })
        }
      }
    }
  }
  
  if (moves.length === 0) return
  
  const randomMove = moves[Math.floor(Math.random() * moves.length)]
  const [fromRow, fromCol] = randomMove.from.split('-').map(Number)
  const [toRow, toCol] = randomMove.to.split('-').map(Number)
  
  movePiece(fromRow, fromCol, toRow, toCol)
}

// 重置游戏
const resetGame = () => {
  initBoard()
  currentPlayer.value = 'red'
  gameStatus.value = '游戏进行中'
  gameStatusClass.value = 'status-playing'
  selectedCell.value = ''
  validMoves.value = []
  history.value = []
  boardFlipped.value = false
}

// 切换AI模式
const toggleAIMode = () => {
  aiMode.value = !aiMode.value
  resetGame()
  ElMessage.info(aiMode.value ? 'AI模式已开启' : 'AI模式已关闭')
}

// 悔棋
const undoMove = () => {
  if (history.value.length === 0) {
    ElMessage.warning('没有可悔的棋步')
    return
  }
  
  board.value = history.value.pop()
  currentPlayer.value = currentPlayer.value === 'red' ? 'black' : 'red'
  gameStatus.value = '游戏进行中'
  gameStatusClass.value = 'status-playing'
  selectedCell.value = ''
  validMoves.value = []
  ElMessage.success('悔棋成功')
}

// 翻转棋盘
const flipBoard = () => {
  boardFlipped.value = !boardFlipped.value
}

// 返回主页
const goBack = () => {
  router.push('/')
}

// 拖拽开始
const handleDragStart = (event, row, col) => {
  event.dataTransfer.setData('text/plain', `${row}-${col}`)
}

// 页面挂载时初始化
onMounted(() => {
  initBoard()
  
  // 可以从本地存储加载游戏配置
  const gameConfig = localStorage.getItem('gameConfig')
  if (gameConfig) {
    const config = JSON.parse(gameConfig)
    // 根据配置设置游戏难度等
    console.log('游戏配置:', config)
  }
})
</script>

<style scoped>
.game-container {
  max-width: 1000px;
  margin: 20px auto;
  padding: 0 20px;
}

.game-card {
  border-radius: 12px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.game-icon {
  margin-right: 8px;
  font-size: 28px;
}

.back-button {
  margin-left: auto;
}

.game-content {
  text-align: center;
}

.game-info {
  margin-bottom: 24px;
}

.current-player {
  font-size: 20px;
  margin-bottom: 12px;
}

.player-mark {
  font-weight: bold;
  font-size: 24px;
  margin-left: 8px;
}

.player-mark.red {
  color: #f56c6c;
}

.player-mark.black {
  color: #303133;
}

.game-status {
  font-size: 18px;
  font-weight: bold;
  padding: 8px 16px;
  border-radius: 8px;
  display: inline-block;
}

.status-playing {
  background-color: #e6f7ff;
  color: #31708f;
}

.status-winning {
  background-color: #f0f9eb;
  color: #67c23a;
}

.status-draw {
  background-color: #f5f7fa;
  color: #909399;
}

.game-board {
  display: inline-block;
}

.coordinates {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.row-coordinates {
  display: flex;
  margin-bottom: 8px;
}

.row-coordinates span {
  width: 50px;
  text-align: center;
  font-weight: bold;
  color: #303133;
}

.board-container {
  display: flex;
}

.column-coordinates {
  margin-right: 8px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.column-coordinates span {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  color: #303133;
}

.chessboard {
  border: 2px solid #303133;
  border-radius: 8px;
  overflow: hidden;
}

.board-row {
  display: flex;
}

.board-cell {
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
}

.cell-dark {
  background-color: #deb887;
}

.cell-light {
  background-color: #f5deb3;
}

.cell-selected {
  background-color: #ffd700;
}

.cell-valid {
  position: relative;
}

.cell-valid::after {
  content: '';
  position: absolute;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background-color: rgba(0, 0, 0, 0.3);
}

.chess-piece {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s ease;
  user-select: none;
}

.chess-piece.red {
  background-color: #f56c6c;
  color: #ffffff;
}

.chess-piece.black {
  background-color: #303133;
  color: #ffffff;
}

.chess-piece:hover {
  transform: scale(1.1);
  box-shadow: 0 0 8px rgba(0, 0, 0, 0.5);
}

.game-controls {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin-top: 24px;
}
</style>