<template>
  <div class="game-container">
    <el-card class="game-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon class="game-icon"><Star /></el-icon>
          <span class="game-title">井字游戏</span>
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
            <span class="player-mark" :class="currentPlayer">{{ currentPlayer === 'X' ? 'X' : 'O' }}</span>
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
              :class="{
                'cell-x': cell === 'X', 
                'cell-o': cell === 'O', 
                'cell-winning': isWinningCell(rowIndex, colIndex),
                'cell-disabled': gameStatus !== '游戏进行中'
              }"
              @click="makeMove(rowIndex, colIndex)"
            >
              <div class="cell-piece" :class="cell">{{ cell }}</div>
            </div>
          </div>
        </div>

        <!-- 游戏控制 -->
        <div class="game-controls">
          <el-button type="primary" @click="startNewGame">开始新游戏</el-button>
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

const router = useRouter()

// 游戏状态
const gameId = ref(null)
const board = ref([])
const currentPlayer = ref('X')
const gameStatus = ref('游戏进行中')
const gameStatusClass = ref('status-playing')
const winningLine = ref([])
const isLoading = ref(false)

// 初始化游戏
const startNewGame = async () => {
  try {
    isLoading.value = true
    const response = await axios.post('http://localhost:8080/api/game/start')
    const gameState = response.data
    gameId.value = gameState.gameId
    // 确保棋盘是二维数组
    if (gameState.board && gameState.board.length === 9) {
      // 如果是一维数组，转换为3x3的二维数组
      board.value = [
        gameState.board.slice(0, 3),
        gameState.board.slice(3, 6),
        gameState.board.slice(6, 9)
      ]
    } else {
      board.value = gameState.board
    }
    currentPlayer.value = gameState.currentPlayer
    gameStatus.value = mapGameStatus(gameState.gameStatus)
    winningLine.value = gameState.winningLine || []
    gameStatusClass.value = getStatusClass(gameState.gameStatus)
  } catch (error) {
    ElMessage.error('无法开始新游戏，请稍后重试')
    console.error('Error starting new game:', error)
  } finally {
    isLoading.value = false
  }
}

// 落子
const makeMove = async (rowIndex, colIndex) => {
  if (isLoading.value || gameStatus.value !== '游戏进行中' || board.value[rowIndex][colIndex] !== null) {
    return
  }

  try {
    isLoading.value = true
    const response = await axios.post('http://localhost:8080/api/game/move', {
      gameId: gameId.value,
      row: rowIndex,
      col: colIndex
    })
    const gameState = response.data
    // 确保棋盘是二维数组
    if (gameState.board && gameState.board.length === 9) {
      // 如果是一维数组，转换为3x3的二维数组
      board.value = [
        gameState.board.slice(0, 3),
        gameState.board.slice(3, 6),
        gameState.board.slice(6, 9)
      ]
    } else {
      board.value = gameState.board
    }
    currentPlayer.value = gameState.currentPlayer
    gameStatus.value = mapGameStatus(gameState.gameStatus)
    winningLine.value = gameState.winningLine || []
    gameStatusClass.value = getStatusClass(gameState.gameStatus)

    if (gameStatus.value !== '游戏进行中') {
      ElMessage.success(gameStatus.value)
    }
  } catch (error) {
    ElMessage.error('落子失败，请稍后重试')
    console.error('Error making move:', error)
  } finally {
    isLoading.value = false
  }
}

// 检查是否是获胜格子
const isWinningCell = (row, col) => {
  if (!winningLine.value || winningLine.value.length === 0) {
    return false
  }
  return winningLine.value.some(cell => cell[0] === row && cell[1] === col)
}

// 映射游戏状态
const mapGameStatus = (status) => {
  switch (status) {
    case 'PLAYING': return '游戏进行中'
    case 'X_WON': return 'X 获胜！'
    case 'O_WON': return 'O 获胜！'
    case 'DRAW': return '平局！'
    default: return '游戏进行中'
  }
}

// 获取状态类名
const getStatusClass = (status) => {
  switch (status) {
    case 'PLAYING': return 'status-playing'
    case 'X_WON': return 'status-winning'
    case 'O_WON': return 'status-winning'
    case 'DRAW': return 'status-draw'
    default: return 'status-playing'
  }
}

// 返回主页
const goBack = () => {
  router.push('/')
}

// 页面加载时初始化游戏
onMounted(() => {
  startNewGame()
})
</script>

<style scoped>
.game-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.game-card {
  width: 100%;
  max-width: 500px;
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px;
  border-bottom: 2px solid #f0f0f0;
}

.game-icon {
  font-size: 24px;
  color: #667eea;
  margin-right: 10px;
}

.game-title {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  flex: 1;
  text-align: center;
}

.back-button {
  padding: 6px 12px;
  font-size: 14px;
}

.game-content {
  padding: 30px;
}

.game-info {
  margin-bottom: 30px;
  text-align: center;
}

.current-player {
  font-size: 20px;
  margin-bottom: 10px;
  color: #333;
}

.player-mark {
  font-weight: bold;
  font-size: 24px;
  margin-left: 10px;
  padding: 4px 12px;
  border-radius: 8px;
  color: white;
  animation: pulse 1.5s infinite;
}

.player-mark.X {
  background: #667eea;
}

.player-mark.O {
  background: #f093fb;
}

.game-status {
  font-size: 22px;
  font-weight: bold;
  padding: 10px;
  border-radius: 8px;
  color: white;
  animation: pulse 1.5s infinite;
}

.status-playing {
  background: #4caf50;
}

.status-winning {
  background: #ff5722;
}

.status-draw {
  background: #ff9800;
}

.game-board {
  display: flex;
  justify-content: center;
  margin-bottom: 30px;
}

.board-row {
  display: flex;
}

.board-cell {
  width: 120px;
  height: 120px;
  border: 2px solid #ddd;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s ease;
  background: white;
}

.board-cell:hover:not(.cell-disabled) {
  background: #f5f5f5;
  transform: scale(1.05);
}

.board-cell.cell-x, .board-cell.cell-o {
  cursor: not-allowed;
}

.cell-piece {
  font-size: 60px;
  font-weight: bold;
  animation: fadeIn 0.5s ease;
}

.cell-x {
  color: #667eea;
}

.cell-o {
  color: #f093fb;
}

.cell-winning {
  background: #ffeb3b !important;
  animation: winningPulse 1s infinite;
}

.cell-disabled {
  cursor: not-allowed;
  opacity: 0.8;
}

.game-controls {
  display: flex;
  justify-content: center;
}

.game-controls button {
  padding: 12px 30px;
  font-size: 18px;
  border-radius: 8px;
}

@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.05); }
  100% { transform: scale(1); }
}

@keyframes fadeIn {
  from { opacity: 0; transform: scale(0.5); }
  to { opacity: 1; transform: scale(1); }
}

@keyframes winningPulse {
  0% { background: #ffeb3b; }
  50% { background: #ffc107; }
  100% { background: #ffeb3b; }
}

/* 响应式设计 */
@media (max-width: 600px) {
  .game-card {
    margin: 10px;
  }
  
  .card-header {
    padding: 15px;
  }
  
  .game-title {
    font-size: 20px;
  }
  
  .game-content {
    padding: 20px;
  }
  
  .board-cell {
    width: 90px;
    height: 90px;
  }
  
  .cell-piece {
    font-size: 45px;
  }
}
</style>