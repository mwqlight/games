<template>
  <div class="chinese-chess-container">
    <header class="game-header">
      <h1>中国象棋游戏</h1>
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
            <span>红方: {{ redCount }}</span>
          </div>
          <div class="stat-item">
            <span>黑方: {{ blackCount }}</span>
          </div>
          <div class="stat-item">
            <span>当前玩家: {{ currentPlayer === 1 ? '红方' : '黑方' }}</span>
          </div>
        </div>
      </div>
      <div class="game-board">
        <div 
          class="board-cell" 
          v-for="(row, rowIndex) in board" 
          :key="rowIndex"
          :class="{
            'red': (rowIndex + colIndex) % 2 === 0,
            'black': (rowIndex + colIndex) % 2 === 1,
            'selected': selectedCell && selectedCell.row === rowIndex && selectedCell.col === colIndex,
            'valid-move': cell.validMove
          }"
          @click="makeMove(rowIndex, colIndex)"
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
        <p>点击棋子进行移动，按照中国象棋规则进行游戏</p>
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
const redCount = ref(0)
const blackCount = ref(0)
const currentPlayer = ref(1)
const gameStarted = ref(false)
const gamePaused = ref(false)
const timer = ref(null)

// 游戏配置
const boardSize = ref(9)
const initialRedCount = ref(16)
const initialBlackCount = ref(16)
const initialPlayer = ref(1)

// 棋子类型
const pieceTypes = {
  rook: 1,
  knight: 2,
  bishop: 3,
  advisor: 4,
  king: 5,
  cannon: 6,
  pawn: 7
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
  redCount.value = initialRedCount.value
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
  board.value = Array(boardSize.value).fill(null).map(() => Array(boardSize.value).fill(null).map(() => ({
    value: 0,
    validMove: false
  })))
}

// 设置初始棋子
const setInitialPieces = () => {
  // 设置红方
  board.value[0][0].value = pieceTypes.rook + 10 // 红车
  board.value[0][8].value = pieceTypes.rook + 10 // 红车
  
  board.value[0][1].value = pieceTypes.knight + 10 // 红马
  board.value[0][7].value = pieceTypes.knight + 10 // 红马
  
  board.value[0][2].value = pieceTypes.bishop + 10 // 红象
  board.value[0][6].value = pieceTypes.bishop + 10 // 红象
  
  board.value[0][3].value = pieceTypes.advisor + 10 // 红士
  board.value[0][5].value = pieceTypes.advisor + 10 // 红士
  
  board.value[0][4].value = pieceTypes.king + 10 // 红帅
  
  board.value[2][1].value = pieceTypes.cannon + 10 // 红炮
  board.value[2][7].value = pieceTypes.cannon + 10 // 红炮
  
  board.value[3][0].value = pieceTypes.pawn + 10 // 红兵
  board.value[3][2].value = pieceTypes.pawn + 10 // 红兵
  board.value[3][4].value = pieceTypes.pawn + 10 // 红兵
  board.value[3][6].value = pieceTypes.pawn + 10 // 红兵
  board.value[3][8].value = pieceTypes.pawn + 10 // 红兵
  
  // 设置黑方
  board.value[8][0].value = pieceTypes.rook + 20 // 黑车
  board.value[8][8].value = pieceTypes.rook + 20 // 黑车
  
  board.value[8][1].value = pieceTypes.knight + 20 // 黑马
  board.value[8][7].value = pieceTypes.knight + 20 // 黑马
  
  board.value[8][2].value = pieceTypes.bishop + 20 // 黑象
  board.value[8][6].value = pieceTypes.bishop + 20 // 黑象
  
  board.value[8][3].value = pieceTypes.advisor + 20 // 黑士
  board.value[8][5].value = pieceTypes.advisor + 20 // 黑士
  
  board.value[8][4].value = pieceTypes.king + 20 // 黑将
  
  board.value[6][1].value = pieceTypes.cannon + 20 // 黑炮
  board.value[6][7].value = pieceTypes.cannon + 20 // 黑炮
  
  board.value[5][0].value = pieceTypes.pawn + 20 // 黑卒
  board.value[5][2].value = pieceTypes.pawn + 20 // 黑卒
  board.value[5][4].value = pieceTypes.pawn + 20 // 黑卒
  board.value[5][6].value = pieceTypes.pawn + 20 // 黑卒
  board.value[5][8].value = pieceTypes.pawn + 20 // 黑卒
}

// 获取棋子符号
const getPieceSymbol = (value) => {
  const pieceType = value % 10
  const color = Math.floor(value / 10)
  
  switch (pieceType) {
    case pieceTypes.rook:
      return color === 1 ? '车' : '車'
    case pieceTypes.knight:
      return color === 1 ? '马' : '馬'
    case pieceTypes.bishop:
      return color === 1 ? '相' : '象'
    case pieceTypes.advisor:
      return color === 1 ? '仕' : '士'
    case pieceTypes.king:
      return color === 1 ? '帅' : '将'
    case pieceTypes.cannon:
      return color === 1 ? '炮' : '砲'
    case pieceTypes.pawn:
      return color === 1 ? '兵' : '卒'
    default:
      return ''
  }
}

// 检查有效移动
const checkValidMoves = () => {
  // 清除所有有效移动标记
  for (let row = 0; row < boardSize.value; row++) {
    for (let col = 0; col < boardSize.value; col++) {
      board.value[row][col].validMove = false
    }
  }
  
  // 检查所有棋子
  for (let row = 0; row < boardSize.value; row++) {
    for (let col = 0; col < boardSize.value; col++) {
      if (isCurrentPlayerPiece(board.value[row][col].value)) {
        // 检查是否有可以移动的位置
        checkPieceValidMoves(row, col)
      }
    }
  }
}

// 检查当前玩家的棋子
const isCurrentPlayerPiece = (value) => {
  if (value === 0) {
    return false
  }
  
  const color = Math.floor(value / 10)
  
  return color === currentPlayer.value
}

// 检查棋子的有效移动
const checkPieceValidMoves = (rowIndex, colIndex) => {
  // 获取棋子类型
  const pieceType = board.value[rowIndex][colIndex].value % 10
  
  // 根据棋子类型检查有效移动
  switch (pieceType) {
    case pieceTypes.rook:
      checkRookValidMoves(rowIndex, colIndex)
      break
    case pieceTypes.knight:
      checkKnightValidMoves(rowIndex, colIndex)
      break
    case pieceTypes.bishop:
      checkBishopValidMoves(rowIndex, colIndex)
      break
    case pieceTypes.advisor:
      checkAdvisorValidMoves(rowIndex, colIndex)
      break
    case pieceTypes.king:
      checkKingValidMoves(rowIndex, colIndex)
      break
    case pieceTypes.cannon:
      checkCannonValidMoves(rowIndex, colIndex)
      break
    case pieceTypes.pawn:
      checkPawnValidMoves(rowIndex, colIndex)
      break
  }
}

// 检查车的有效移动
const checkRookValidMoves = (rowIndex, colIndex) => {
  // 检查四个方向
  const directions = [
    { row: -1, col: 0 }, // 上
    { row: 1, col: 0 },  // 下
    { row: 0, col: -1 }, // 左
    { row: 0, col: 1 }   // 右
  ]
  
  for (let i = 0; i < directions.length; i++) {
    // 检查每个方向的单元格
    let newRow = rowIndex + directions[i].row
    let newCol = colIndex + directions[i].col
    
    while (newRow >= 0 && newRow < boardSize.value && newCol >= 0 && newCol < boardSize.value) {
      if (board.value[newRow][newCol].value === 0) {
        // 设置有效移动标记
        board.value[newRow][newCol].validMove = true
      } else if (isOpponentPiece(board.value[newRow][newCol].value)) {
        // 设置有效移动标记
        board.value[newRow][newCol].validMove = true
        break
      } else {
        // 遇到自己的棋子，停止检查
        break
      }
      
      newRow += directions[i].row
      newCol += directions[i].col
    }
  }
}

// 检查马的有效移动
const checkKnightValidMoves = (rowIndex, colIndex) => {
  // 检查八个方向
  const directions = [
    { row: -2, col: -1 },
    { row: -2, col: 1 },
    { row: -1, col: -2 },
    { row: -1, col: 2 },
    { row: 1, col: -2 },
    { row: 1, col: 2 },
    { row: 2, col: -1 },
    { row: 2, col: 1 }
  ]
  
  for (let i = 0; i < directions.length; i++) {
    // 计算新位置
    let newRow = rowIndex + directions[i].row
    let newCol = colIndex + directions[i].col
    
    if (newRow >= 0 && newRow < boardSize.value && newCol >= 0 && newCol < boardSize.value) {
      // 检查是否有棋子阻挡
      if (!isPieceBlocking(rowIndex, colIndex, newRow, newCol)) {
        if (board.value[newRow][newCol].value === 0 || isOpponentPiece(board.value[newRow][newCol].value)) {
          // 设置有效移动标记
          board.value[newRow][newCol].validMove = true
        }
      }
    }
  }
}

// 检查象的有效移动
const checkBishopValidMoves = (rowIndex, colIndex) => {
  // 检查四个方向
  const directions = [
    { row: -2, col: -2 }, // 左上
    { row: -2, col: 2 },  // 右上
    { row: 2, col: -2 },  // 左下
    { row: 2, col: 2 }    // 右下
  ]
  
  for (let i = 0; i < directions.length; i++) {
    // 计算新位置
    let newRow = rowIndex + directions[i].row
    let newCol = colIndex + directions[i].col
    
    if (newRow >= 0 && newRow < boardSize.value && newCol >= 0 && newCol < boardSize.value) {
      // 检查是否有棋子阻挡
      if (!isPieceBlocking(rowIndex, colIndex, newRow, newCol)) {
        // 检查是否在自己的半场
        if ((currentPlayer.value === 1 && newRow <= 4) || (currentPlayer.value === 2 && newRow >= 5)) {
          if (board.value[newRow][newCol].value === 0 || isOpponentPiece(board.value[newRow][newCol].value)) {
            // 设置有效移动标记
            board.value[newRow][newCol].validMove = true
          }
        }
      }
    }
  }
}

// 检查士的有效移动
const checkAdvisorValidMoves = (rowIndex, colIndex) => {
  // 检查四个方向
  const directions = [
    { row: -1, col: -1 }, // 左上
    { row: -1, col: 1 },  // 右上
    { row: 1, col: -1 },  // 左下
    { row: 1, col: 1 }    // 右下
  ]
  
  for (let i = 0; i < directions.length; i++) {
    // 计算新位置
    let newRow = rowIndex + directions[i].row
    let newCol = colIndex + directions[i].col
    
    if (newRow >= 0 && newRow < boardSize.value && newCol >= 0 && newCol < boardSize.value) {
      // 检查是否在九宫格内
      if (isInPalace(newRow, newCol)) {
        if (board.value[newRow][newCol].value === 0 || isOpponentPiece(board.value[newRow][newCol].value)) {
          // 设置有效移动标记
          board.value[newRow][newCol].validMove = true
        }
      }
    }
  }
}

// 检查帅的有效移动
const checkKingValidMoves = (rowIndex, colIndex) => {
  // 检查四个方向
  const directions = [
    { row: -1, col: 0 }, // 上
    { row: 1, col: 0 },  // 下
    { row: 0, col: -1 }, // 左
    { row: 0, col: 1 }   // 右
  ]
  
  for (let i = 0; i < directions.length; i++) {
    // 计算新位置
    let newRow = rowIndex + directions[i].row
    let newCol = colIndex + directions[i].col
    
    if (newRow >= 0 && newRow < boardSize.value && newCol >= 0 && newCol < boardSize.value) {
      // 检查是否在九宫格内
      if (isInPalace(newRow, newCol)) {
        if (board.value[newRow][newCol].value === 0 || isOpponentPiece(board.value[newRow][newCol].value)) {
          // 设置有效移动标记
          board.value[newRow][newCol].validMove = true
        }
      }
    }
  }
}

// 检查炮的有效移动
const checkCannonValidMoves = (rowIndex, colIndex) => {
  // 检查四个方向
  const directions = [
    { row: -1, col: 0 }, // 上
    { row: 1, col: 0 },  // 下
    { row: 0, col: -1 }, // 左
    { row: 0, col: 1 }   // 右
  ]
  
  for (let i = 0; i < directions.length; i++) {
    // 检查每个方向的单元格
    let newRow = rowIndex + directions[i].row
    let newCol = colIndex + directions[i].col
    let pieceCount = 0
    
    while (newRow >= 0 && newRow < boardSize.value && newCol >= 0 && newCol < boardSize.value) {
      if (board.value[newRow][newCol].value === 0) {
        // 设置有效移动标记
        if (pieceCount === 0) {
          board.value[newRow][newCol].validMove = true
        }
      } else if (isOpponentPiece(board.value[newRow][newCol].value)) {
        // 设置有效移动标记
        if (pieceCount === 1) {
          board.value[newRow][newCol].validMove = true
        }
        break
      } else {
        // 遇到自己的棋子，停止检查
        if (pieceCount === 0) {
          pieceCount++
        } else {
          break
        }
      }
      
      newRow += directions[i].row
      newCol += directions[i].col
    }
  }
}

// 检查兵的有效移动
const checkPawnValidMoves = (rowIndex, colIndex) => {
  // 获取棋子颜色
  const color = Math.floor(board.value[rowIndex][colIndex].value / 10)
  
  // 计算移动方向
  const direction = color === 1 ? 1 : -1
  
  // 检查前方单元格
  let newRow = rowIndex + direction
  let newCol = colIndex
  
  if (newRow >= 0 && newRow < boardSize.value && newCol >= 0 && newCol < boardSize.value) {
    if (board.value[newRow][newCol].value === 0 || isOpponentPiece(board.value[newRow][newCol].value)) {
      // 设置有效移动标记
      board.value[newRow][newCol].validMove = true
    }
  }
  
  // 检查是否过河
  if ((color === 1 && rowIndex >= 5) || (color === 2 && rowIndex <= 4)) {
    // 检查左右单元格
    newRow = rowIndex
    newCol = colIndex - 1
    
    if (newRow >= 0 && newRow < boardSize.value && newCol >= 0 && newCol < boardSize.value) {
      if (board.value[newRow][newCol].value === 0 || isOpponentPiece(board.value[newRow][newCol].value)) {
        // 设置有效移动标记
        board.value[newRow][newCol].validMove = true
      }
    }
    
    newRow = rowIndex
    newCol = colIndex + 1
    
    if (newRow >= 0 && newRow < boardSize.value && newCol >= 0 && newCol < boardSize.value) {
      if (board.value[newRow][newCol].value === 0 || isOpponentPiece(board.value[newRow][newCol].value)) {
        // 设置有效移动标记
        board.value[newRow][newCol].validMove = true
      }
    }
  }
}

// 检查是否有棋子阻挡
const isPieceBlocking = (fromRow, fromCol, toRow, toCol) => {
  // 计算中间位置
  const midRow = Math.floor((fromRow + toRow) / 2)
  const midCol = Math.floor((fromCol + toCol) / 2)
  
  // 检查中间位置是否有棋子
  return board.value[midRow][midCol].value !== 0
}

// 检查是否在九宫格内
const isInPalace = (rowIndex, colIndex) => {
  // 检查红方九宫格
  if (rowIndex >= 0 && rowIndex <= 2 && colIndex >= 3 && colIndex <= 5) {
    return true
  }
  
  // 检查黑方九宫格
  if (rowIndex >= 7 && rowIndex <= 9 && colIndex >= 3 && colIndex <= 5) {
    return true
  }
  
  return false
}

// 检查是否是对方的棋子
const isOpponentPiece = (value) => {
  if (value === 0) {
    return false
  }
  
  const color = Math.floor(value / 10)
  
  return color !== currentPlayer.value
}

// 下棋
const makeMove = (rowIndex, colIndex) => {
  if (!gameStarted.value || gamePaused.value) {
    return
  }
  
  // 检查是否已经选中棋子
  if (selectedCell.value) {
    // 检查是否是有效移动
    if (board.value[rowIndex][colIndex].validMove) {
      // 移动棋子
      movePiece(selectedCell.value.row, selectedCell.value.col, rowIndex, colIndex)
      
      // 清除选中状态
      selectedCell.value = null
      
      // 检查有效移动
      checkValidMoves()
      
      // 检查游戏是否结束
      checkGameEnd()
    } else {
      // 检查是否是当前玩家的棋子
      if (isCurrentPlayerPiece(board.value[rowIndex][colIndex].value)) {
        // 选中新的棋子
        selectedCell.value = { row: rowIndex, col: colIndex }
      } else {
        // 清除选中状态
        selectedCell.value = null
      }
    }
  } else {
    // 检查是否是当前玩家的棋子
    if (isCurrentPlayerPiece(board.value[rowIndex][colIndex].value)) {
      // 选中棋子
      selectedCell.value = { row: rowIndex, col: colIndex }
    }
  }
}

// 移动棋子
const movePiece = (fromRow, fromCol, toRow, toCol) => {
  // 获取棋子类型
  const pieceType = board.value[fromRow][fromCol].value
  
  // 检查是否吃掉对方棋子
  if (isOpponentPiece(board.value[toRow][toCol].value)) {
    // 更新棋子数量
    if (currentPlayer.value === 1) {
      blackCount.value--
    } else {
      redCount.value--
    }
  }
  
  // 移动棋子
  board.value[toRow][toCol].value = pieceType
  board.value[fromRow][fromCol].value = 0
  
  // 切换玩家
  currentPlayer.value = currentPlayer.value === 1 ? 2 : 1
}

// 检查游戏结束
const checkGameEnd = () => {
  // 检查是否有有效移动
  let hasValidMove = false
  
  for (let row = 0; row < boardSize.value; row++) {
    for (let col = 0; col < boardSize.value; col++) {
      if (board.value[row][col].validMove) {
        hasValidMove = true
        break
      }
    }
    
    if (hasValidMove) {
      break
    }
  }
  
  if (!hasValidMove) {
    // 游戏结束
    gameStatus.value = '游戏结束'
    clearInterval(timer.value)
    
    // 计算得分
    if (redCount.value > blackCount.value) {
      score.value = redCount.value - blackCount.value
    } else if (blackCount.value > redCount.value) {
      score.value = blackCount.value - redCount.value
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