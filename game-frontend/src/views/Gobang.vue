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
          <el-button @click="toggleAIMode">
            {{ aiMode ? '关闭AI' : '开启AI' }}
          </el-button>
          <el-button @click="undoMove" :disabled="history.length === 0">悔棋</el-button>
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

const router = useRouter()

// 游戏状态
const boardSize = 15
const board = ref([])
const currentPlayer = ref('black')
const gameStatus = ref('游戏进行中')
const gameStatusClass = ref('status-playing')
const winningCells = ref([])
const aiMode = ref(false)
const history = ref([])

// 初始化棋盘
const initBoard = () => {
  const newBoard = []
  for (let i = 0; i < boardSize; i++) {
    newBoard.push(new Array(boardSize).fill(''))
  }
  board.value = newBoard
}

// 检查游戏是否结束
const checkGameEnd = (row, col) => {
  const directions = [
    [0, 1], [1, 0], [1, 1], [1, -1] // 横向、纵向、主对角线、副对角线
  ]

  for (const [dx, dy] of directions) {
    let count = 1 // 当前棋子
    let winningPath = [`${row}-${col}`]

    // 向正方向检查
    for (let i = 1; i < 5; i++) {
      const newRow = row + dx * i
      const newCol = col + dy * i
      if (newRow >= 0 && newRow < boardSize && newCol >= 0 && newCol < boardSize && board.value[newRow][newCol] === currentPlayer.value) {
        count++
        winningPath.push(`${newRow}-${newCol}`)
      } else {
        break
      }
    }

    // 向负方向检查
    for (let i = 1; i < 5; i++) {
      const newRow = row - dx * i
      const newCol = col - dy * i
      if (newRow >= 0 && newRow < boardSize && newCol >= 0 && newCol < boardSize && board.value[newRow][newCol] === currentPlayer.value) {
        count++
        winningPath.unshift(`${newRow}-${newCol}`)
      } else {
        break
      }
    }

    // 检查是否获胜
    if (count >= 5) {
      winningCells.value = winningPath
      gameStatus.value = `${currentPlayer.value === 'black' ? '黑棋' : '白棋'} 获胜！`
      gameStatusClass.value = 'status-winning'
      ElMessage.success(`${currentPlayer.value === 'black' ? '黑棋' : '白棋'} 获胜！`)
      return true
    }
  }

  // 检查是否平局
  if (board.value.every(row => row.every(cell => cell !== ''))) {
    gameStatus.value = '平局！'
    gameStatusClass.value = 'status-draw'
    ElMessage.info('平局！')
    return true
  }

  return false
}

// 保存历史记录
const saveHistory = () => {
  history.value.push(JSON.parse(JSON.stringify(board.value)))
}

// 玩家移动
const makeMove = (row, col) => {
  if (board.value[row][col] !== '' || gameStatus.value !== '游戏进行中') {
    return
  }

  saveHistory()
  board.value[row][col] = currentPlayer.value

  if (checkGameEnd(row, col)) {
    return
  }

  // 切换玩家
  currentPlayer.value = currentPlayer.value === 'black' ? 'white' : 'black'

  // AI移动
  if (aiMode.value && currentPlayer.value === 'white') {
    setTimeout(aiMove, 500)
  }
}

// AI移动
const aiMove = () => {
  // 使用启发式算法选择最优移动
  const emptyCells = []
  for (let i = 0; i < boardSize; i++) {
    for (let j = 0; j < boardSize; j++) {
      if (board.value[i][j] === '') {
        emptyCells.push([i, j])
      }
    }
  }

  if (emptyCells.length === 0) return

  let bestScore = -Infinity
  let bestMove = null

  for (const [row, col] of emptyCells) {
    // 模拟AI移动
    board.value[row][col] = 'white'
    // 计算分数
    const score = evaluateBoard(board.value)
    // 撤销移动
    board.value[row][col] = ''
    // 更新最佳分数和最佳移动
    if (score > bestScore) {
      bestScore = score
      bestMove = [row, col]
    }
  }

  // 执行最佳移动
  const [bestRow, bestCol] = bestMove
  saveHistory()
  board.value[bestRow][bestCol] = 'white'

  checkGameEnd(bestRow, bestCol)
  currentPlayer.value = 'black'
}

// 评估棋盘
const evaluateBoard = (board) => {
  let score = 0
  
  // 评估每一行
  for (let i = 0; i < boardSize; i++) {
    score += evaluateLine(board[i])
  }
  
  // 评估每一列
  for (let j = 0; j < boardSize; j++) {
    const column = []
    for (let i = 0; i < boardSize; i++) {
      column.push(board[i][j])
    }
    score += evaluateLine(column)
  }
  
  // 评估主对角线
  for (let i = 0; i <= boardSize - 5; i++) {
    const diagonal1 = []
    const diagonal2 = []
    for (let j = 0; j < boardSize - i; j++) {
      diagonal1.push(board[i + j][j])
      diagonal2.push(board[j][i + j])
    }
    score += evaluateLine(diagonal1)
    score += evaluateLine(diagonal2)
  }
  
  // 评估副对角线
  for (let i = 1; i <= boardSize - 5; i++) {
    const diagonal1 = []
    const diagonal2 = []
    for (let j = 0; j < boardSize - i; j++) {
      diagonal1.push(board[i + j][boardSize - 1 - j])
      diagonal2.push(board[j][boardSize - 1 - i - j])
    }
    score += evaluateLine(diagonal1)
    score += evaluateLine(diagonal2)
  }
  
  return score
}

// 评估一条线（行、列或对角线）
const evaluateLine = (line) => {
  let score = 0
  const patterns = {
    'wwwww': 10000, // 五连
    'wwww_': 1000,  // 活四
    '_wwww': 1000,  // 活四
    'www__': 100,   // 活三
    '__www': 100,   // 活三
    'www_w': 100,   // 活三
    'w_www': 100,   // 活三
    'ww___': 10,    // 活二
    '___ww': 10,    // 活二
    'ww__w': 10,    // 活二
    'w__ww': 10,    // 活二
    'ww_w_': 10,    // 活二
    '_w_ww': 10,    // 活二
    'w_ww_': 10,    // 活二
    '_ww_w': 10,    // 活二
    'wwwww': -10000, // 对方五连
    'wwww_': -1000,  // 对方活四
    '_wwww': -1000,  // 对方活四
    'www__': -100,   // 对方活三
    '__www': -100,   // 对方活三
    'www_w': -100,   // 对方活三
    'w_www': -100,   // 对方活三
    'ww___': -10,    // 对方活二
    '___ww': -10,    // 对方活二
    'ww__w': -10,    // 对方活二
    'w__ww': -10,    // 对方活二
    'ww_w_': -10,    // 对方活二
    '_w_ww': -10,    // 对方活二
    'w_ww_': -10,    // 对方活二
    '_ww_w': -10     // 对方活二
  }
  
  // 将线转换为字符串
  const lineStr = line.join('')
  
  // 检查所有模式
  for (const pattern in patterns) {
    const regex = new RegExp(pattern.replace(/w/g, 'white').replace(/_/g, '').replace(/b/g, 'black'))
    if (regex.test(lineStr)) {
      score += patterns[pattern]
    }
  }
  
  return score
}

// 重置游戏
const resetGame = () => {
  initBoard()
  currentPlayer.value = 'black'
  gameStatus.value = '游戏进行中'
  gameStatusClass.value = 'status-playing'
  winningCells.value = []
  history.value = []
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
  currentPlayer.value = currentPlayer.value === 'black' ? 'white' : 'black'
  gameStatus.value = '游戏进行中'
  gameStatusClass.value = 'status-playing'
  winningCells.value = []
  ElMessage.success('悔棋成功')
}

// 返回主页
const goBack = () => {
  router.push('/')
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