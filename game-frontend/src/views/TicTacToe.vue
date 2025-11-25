<template>
  <div class="game-container">
    <el-card class="game-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon class="game-icon"><Star /></el-icon>
          <span class="game-title">井字棋游戏</span>
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
            <span class="player-mark" :class="currentPlayer">{{ currentPlayer }}</span>
          </div>
          <div class="game-status" :class="gameStatusClass">
            {{ gameStatus }}
          </div>
        </div>

        <!-- 游戏棋盘 -->
        <div class="game-board">
          <div 
            v-for="(cell, index) in board" 
            :key="index"
            class="board-cell"
            :class="{ 'cell-x': cell === 'X', 'cell-o': cell === 'O', 'cell-winning': winningCells.includes(index) }"
            @click="makeMove(index)"
          >
            {{ cell }}
          </div>
        </div>

        <!-- 游戏控制 -->
        <div class="game-controls">
          <el-button type="primary" @click="resetGame">重新开始</el-button>
          <el-button @click="toggleAIMode">
            {{ aiMode ? '关闭AI' : '开启AI' }}
          </el-button>
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
const board = ref(['', '', '', '', '', '', '', '', ''])
const currentPlayer = ref('X')
const gameStatus = ref('游戏进行中')
const gameStatusClass = ref('status-playing')
const winningCells = ref([])
const aiMode = ref(false)

// 获胜组合
const winningCombinations = [
  [0, 1, 2], [3, 4, 5], [6, 7, 8], // 横向
  [0, 3, 6], [1, 4, 7], [2, 5, 8], // 纵向
  [0, 4, 8], [2, 4, 6] // 对角线
]

// 检查游戏是否结束
const checkGameEnd = () => {
  // 检查是否有玩家获胜
  for (const combination of winningCombinations) {
    const [a, b, c] = combination
    if (board.value[a] && board.value[a] === board.value[b] && board.value[a] === board.value[c]) {
      winningCells.value = combination
      gameStatus.value = `${currentPlayer.value} 获胜！`
      gameStatusClass.value = 'status-winning'
      ElMessage.success(`${currentPlayer.value} 获胜！`)
      return true
    }
  }

  // 检查是否平局
  if (board.value.every(cell => cell !== '')) {
    gameStatus.value = '平局！'
    gameStatusClass.value = 'status-draw'
    ElMessage.info('平局！')
    return true
  }

  return false
}

// 玩家移动
const makeMove = (index) => {
  if (board.value[index] !== '' || gameStatus.value !== '游戏进行中') {
    return
  }

  board.value[index] = currentPlayer.value

  if (checkGameEnd()) {
    return
  }

  // 切换玩家
  currentPlayer.value = currentPlayer.value === 'X' ? 'O' : 'X'

  // AI移动
  if (aiMode.value && currentPlayer.value === 'O') {
    setTimeout(aiMove, 500)
  }
}

// AI移动
const aiMove = () => {
  // 使用极小极大算法选择最优移动
  const emptyCells = board.value.map((cell, index) => cell === '' ? index : null).filter(index => index !== null)
  if (emptyCells.length === 0) return

  let bestScore = -Infinity
  let bestMove = null

  for (const index of emptyCells) {
    // 模拟AI移动
    board.value[index] = 'O'
    // 计算分数
    const score = minimax(board.value, 0, false)
    // 撤销移动
    board.value[index] = ''
    // 更新最佳分数和最佳移动
    if (score > bestScore) {
      bestScore = score
      bestMove = index
    }
  }

  // 执行最佳移动
  board.value[bestMove] = 'O'

  checkGameEnd()
  currentPlayer.value = 'X'
}

// 极小极大算法
const minimax = (board, depth, isMaximizing) => {
  // 检查游戏是否结束
  const winner = checkWinner(board)
  if (winner === 'O') return 10 - depth
  if (winner === 'X') return depth - 10
  if (board.every(cell => cell !== '')) return 0

  if (isMaximizing) {
    let bestScore = -Infinity
    for (let i = 0; i < board.length; i++) {
      if (board[i] === '') {
        board[i] = 'O'
        const score = minimax(board, depth + 1, false)
        board[i] = ''
        bestScore = Math.max(score, bestScore)
      }
    }
    return bestScore
  } else {
    let bestScore = Infinity
    for (let i = 0; i < board.length; i++) {
      if (board[i] === '') {
        board[i] = 'X'
        const score = minimax(board, depth + 1, true)
        board[i] = ''
        bestScore = Math.min(score, bestScore)
      }
    }
    return bestScore
  }
}

// 检查获胜者
const checkWinner = (board) => {
  const winningCombinations = [
    [0, 1, 2], [3, 4, 5], [6, 7, 8], // 横向
    [0, 3, 6], [1, 4, 7], [2, 5, 8], // 纵向
    [0, 4, 8], [2, 4, 6] // 对角线
  ]

  for (const combination of winningCombinations) {
    const [a, b, c] = combination
    if (board[a] && board[a] === board[b] && board[a] === board[c]) {
      return board[a]
    }
  }

  return null
}

// 重置游戏
const resetGame = () => {
  board.value = ['', '', '', '', '', '', '', '', '']
  currentPlayer.value = 'X'
  gameStatus.value = '游戏进行中'
  gameStatusClass.value = 'status-playing'
  winningCells.value = []
}

// 切换AI模式
const toggleAIMode = () => {
  aiMode.value = !aiMode.value
  resetGame()
  ElMessage.info(aiMode.value ? 'AI模式已开启' : 'AI模式已关闭')
}

// 返回主页
const goBack = () => {
  router.push('/')
}

// 页面挂载时加载配置
onMounted(() => {
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
  max-width: 600px;
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

.player-mark.X {
  color: #409eff;
}

.player-mark.O {
  color: #67c23a;
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
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  grid-template-rows: repeat(3, 1fr);
  gap: 8px;
  max-width: 300px;
  margin: 0 auto 24px;
}

.board-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  font-weight: bold;
  background-color: #f5f7fa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  aspect-ratio: 1 / 1;
}

.board-cell:hover {
  background-color: #e4e7ed;
  transform: scale(1.05);
}

.cell-x {
  color: #409eff;
  background-color: #e6f7ff;
}

.cell-o {
  color: #67c23a;
  background-color: #f0f9eb;
}

.cell-winning {
  background-color: #ffd700;
  color: #fff;
  animation: pulse 1s infinite;
}

@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.1); }
  100% { transform: scale(1); }
}

.game-controls {
  display: flex;
  gap: 12px;
  justify-content: center;
}
</style>