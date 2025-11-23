<template>
  <div class="game-container">
    <el-card class="game-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon class="game-icon"><Star /></el-icon>
          <span class="game-title">五子棋游戏</span>
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
            <span class="player-mark" :class="currentPlayer">{{ currentPlayer === 'black' ? '黑棋' : '白棋' }}</span>
          </div>
          <div class="game-status" :class="gameStatusClass">
            {{ gameStatus }}
          </div>
        </div>

        <!-- 游戏棋盘 -->
        <div class="game-board">
          <div 
            v-for="(row, rowIndex) in board" 
            :key="rowIndex"
            class="board-row"
          >
            <div 
              v-for="(cell, colIndex) in row" 
              :key="colIndex"
              class="board-cell"
              :class="{ 'cell-black': cell === 'black', 'cell-white': cell === 'white', 'cell-winning': winningCells.includes(`${rowIndex}-${colIndex}`) }"
              @click="makeMove(rowIndex, colIndex)"
            >
              <div class="cell-piece" :class="cell"></div>
            </div>
          </div>
        </div>

        <!-- 游戏控制 -->
        <div class="game-controls">
          <el-button type="primary" @click="resetGame">重新开始</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Star } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

// 配置axios
const apiClient = axios.create({
  baseURL: 'http://localhost:8081/api/gobang',
  timeout: 10000,
})

const router = useRouter()

// 游戏状态
const boardSize = 15
const board = ref([])
const currentPlayer = ref('black')
const gameStatus = ref('游戏进行中')
const gameStatusClass = ref('status-playing')
const winningCells = ref([])
const isAIMoving = ref(false)

// 初始化棋盘
const initBoard = () => {
  const newBoard = []
  for (let i = 0; i < boardSize; i++) {
    newBoard.push(new Array(boardSize).fill(''))
  }
  board.value = newBoard
}

// 从后端更新棋盘状态
const updateBoardFromBackend = (backendBoard) => {
  for (let i = 0; i < boardSize; i++) {
    for (let j = 0; j < boardSize; j++) {
      switch (backendBoard[i][j]) {
        case 1:
          board.value[i][j] = 'black'
          break
        case 2:
          board.value[i][j] = 'white'
          break
        default:
          board.value[i][j] = ''
      }
    }
  }
}

// 更新游戏状态
const updateGameStatus = (status) => {
  switch (status) {
    case 'playing':
      gameStatus.value = '游戏进行中'
      gameStatusClass.value = 'status-playing'
      break
    case 'player_win':
      gameStatus.value = '黑棋获胜！'
      gameStatusClass.value = 'status-winning'
      ElMessage.success('黑棋获胜！')
      break
    case 'ai_win':
      gameStatus.value = '白棋获胜！'
      gameStatusClass.value = 'status-winning'
      ElMessage.success('白棋获胜！')
      break
    case 'draw':
      gameStatus.value = '平局！'
      gameStatusClass.value = 'status-draw'
      ElMessage.info('平局！')
      break
  }
}

// 更新当前玩家
const updateCurrentPlayer = (player) => {
  currentPlayer.value = player === 1 ? 'black' : 'white'
}

// 高亮获胜的棋子
const highlightWinningCells = (winningLine) => {
  if (winningLine) {
    const [row, col, dirX, dirY] = winningLine
    winningCells.value = []
    
    // 添加当前棋子
    winningCells.value.push(`${row}-${col}`)
    
    // 向正方向添加
    let r = row + dirX
    let c = col + dirY
    while (r >= 0 && r < boardSize && c >= 0 && c < boardSize && board.value[r][c] === currentPlayer.value) {
      winningCells.value.push(`${r}-${c}`)
      r += dirX
      c += dirY
    }
    
    // 向负方向添加
    r = row - dirX
    c = col - dirY
    while (r >= 0 && r < boardSize && c >= 0 && c < boardSize && board.value[r][c] === currentPlayer.value) {
      winningCells.value.push(`${r}-${c}`)
      r -= dirX
      c -= dirY
    }
  }
}





// 玩家移动
const makeMove = async (row, col) => {
  if (board.value[row][col] !== '' || gameStatus.value !== '游戏进行中' || isAIMoving.value) {
    return
  }

  try {
    isAIMoving.value = true
    const response = await apiClient.post('/move', { row, col })
    const gameState = response.data
    
    // 更新棋盘
    updateBoardFromBackend(gameState.board)
    
    // 更新游戏状态
    updateGameStatus(gameState.status)
    
    // 更新当前玩家
    updateCurrentPlayer(gameState.currentPlayer)
    
    // 高亮获胜的棋子
    highlightWinningCells(gameState.winningLine)
  } catch (error) {
    ElMessage.error('落子失败，请重试')
    console.error('落子失败:', error)
  } finally {
    isAIMoving.value = false
  }
}



// 重置游戏
const resetGame = async () => {
  try {
    const response = await apiClient.post('/reset')
    const gameState = response.data
    
    // 更新棋盘
    updateBoardFromBackend(gameState.board)
    
    // 更新游戏状态
    updateGameStatus(gameState.status)
    
    // 更新当前玩家
    updateCurrentPlayer(gameState.currentPlayer)
    
    // 清除获胜棋子高亮
    winningCells.value = []
  } catch (error) {
    ElMessage.error('重置游戏失败，请重试')
    console.error('重置游戏失败:', error)
  }
}





// 返回主页
const goBack = () => {
  router.push('/')
}

// 页面挂载时初始化
onMounted(async () => {
  initBoard()
  
  // 初始化游戏
  await resetGame()
  
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
  max-width: 800px;
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

.player-mark.black {
  color: #303133;
}

.player-mark.white {
  color: #ffffff;
  background-color: #303133;
  padding: 4px 12px;
  border-radius: 8px;
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
  border: 2px solid #303133;
  border-radius: 8px;
  padding: 8px;
  background-color: #deb887;
}

.board-row {
  display: flex;
}

.board-cell {
  width: 40px;
  height: 40px;
  border: 1px solid #8b7355;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  background-color: #deb887;
}

.board-cell:hover {
  background-color: #d2b48c;
}

.cell-piece {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.cell-black {
  background-color: #303133;
  box-shadow: inset 0 0 4px rgba(255, 255, 255, 0.3);
}

.cell-white {
  background-color: #ffffff;
  box-shadow: inset 0 0 4px rgba(0, 0, 0, 0.3);
}

.cell-winning {
  background-color: #ffd700;
}

.game-controls {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin-top: 24px;
}
</style>