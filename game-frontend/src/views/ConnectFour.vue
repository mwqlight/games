<template>
  <div class="game-container">
    <el-card class="game-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon class="game-icon"><Star /></el-icon>
          <span class="game-title">四子棋游戏</span>
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
            <span class="player-mark" :class="currentPlayer === 1 ? 'player-1' : 'player-2'">
              {{ currentPlayer === 1 ? '玩家' : 'AI' }}
            </span>
          </div>
          <div class="game-status" :class="gameStatusClass">
            {{ gameStatus }}
          </div>
        </div>

        <!-- AI难度选择和音效开关 -->
        <div class="game-settings" v-if="gameStatus === '游戏进行中'">
          <div class="ai-difficulty">
            <label for="ai-difficulty">AI难度:</label>
            <el-select 
              v-model="aiDifficulty" 
              placeholder="请选择AI难度" 
              size="small"
              @change="changeAIDifficulty"
            >
              <el-option label="简单" value="easy" />
              <el-option label="中等" value="medium" />
              <el-option label="困难" value="hard" />
            </el-select>
          </div>
          <div class="sound-toggle">
            <label for="sound-toggle">音效:</label>
            <el-switch 
              v-model="soundEnabled" 
              active-text="开启" 
              inactive-text="关闭"
              @change="toggleSound"
            />
          </div>
        </div>

        <!-- 游戏记录 -->
        <div class="game-records">
          <h3>游戏记录</h3>
          <el-table :data="gameRecords" size="small" style="width: 100%">
            <el-table-column prop="date" label="日期" width="180">
              <template #default="scope">
                {{ new Date(scope.row.date).toLocaleString() }}
              </template>
            </el-table-column>
            <el-table-column prop="result" label="结果" width="100">
              <template #default="scope">
                <span :class="scope.row.result === '玩家获胜' ? 'win' : scope.row.result === 'AI获胜' ? 'lose' : 'draw'">
                  {{ scope.row.result }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="difficulty" label="难度" width="100">
              <template #default="scope">
                {{ scope.row.difficulty === 'easy' ? '简单' : scope.row.difficulty === 'medium' ? '中等' : '困难' }}
              </template>
            </el-table-column>
          </el-table>
          <p v-if="gameRecords.length === 0" class="no-records">暂无游戏记录</p>
        </div>

        <!-- 游戏棋盘 -->
        <div class="game-board">
          <div 
            v-for="(column, colIndex) in 7" 
            :key="colIndex"
            class="board-column"
            :class="{ 'column-disabled': gameStatus !== '游戏进行中' || isColumnFull(colIndex) }"
          >
            <div 
              v-for="(cell, rowIndex) in 6" 
              :key="rowIndex"
              class="board-cell"
              :class="{
                'cell-empty': cell === 0,
                'cell-player-1': cell === 1,
                'cell-player-2': cell === 2,
                'cell-winning': winningCells.includes(`${rowIndex}-${colIndex}`)
              }"
              @click="makeMove(colIndex)"
            ></div>
          </div>
        </div>

        <!-- 游戏控制 -->
        <div class="game-controls">
          <el-button type="primary" @click="startNewGame">重新开始</el-button>
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
const gameId = ref(null)
const board = ref([[0, 0, 0, 0, 0, 0, 0],
                   [0, 0, 0, 0, 0, 0, 0],
                   [0, 0, 0, 0, 0, 0, 0],
                   [0, 0, 0, 0, 0, 0, 0],
                   [0, 0, 0, 0, 0, 0, 0],
                   [0, 0, 0, 0, 0, 0, 0]])
const currentPlayer = ref(1)
const gameStatus = ref('游戏进行中')
const gameStatusClass = ref('status-playing')
const winningCells = ref([])
const isLoading = ref(false)
const aiDifficulty = ref('medium') // 默认AI难度为中等
const soundEnabled = ref(true) // 默认开启音效

// 游戏记录
const gameRecords = ref([])

// 加载游戏记录
const loadGameRecords = () => {
  const savedRecords = localStorage.getItem('connectFourGameRecords')
  if (savedRecords) {
    try {
      gameRecords.value = JSON.parse(savedRecords)
    } catch (error) {
      console.error('Error parsing game records:', error)
      gameRecords.value = []
    }
  }
}

// 保存游戏记录
const saveGameRecord = (result) => {
  const record = {
    date: new Date().toISOString(),
    result: result,
    difficulty: aiDifficulty.value
  }
  gameRecords.value.unshift(record)
  // 只保存最近10条记录
  if (gameRecords.value.length > 10) {
    gameRecords.value = gameRecords.value.slice(0, 10)
  }
  // 保存到localStorage
  localStorage.setItem('connectFourGameRecords', JSON.stringify(gameRecords.value))
}

// 游戏音效
const dropSound = new Audio('https://assets.mixkit.co/sfx/preview/mixkit-coin-drop-1124.mp3')
const winSound = new Audio('https://assets.mixkit.co/sfx/preview/mixkit-winning-chimes-2015.mp3')
const loseSound = new Audio('https://assets.mixkit.co/sfx/preview/mixkit-losing-piano-2024.mp3')
const drawSound = new Audio('https://assets.mixkit.co/sfx/preview/mixkit-game-show-wrong-buzzer-950.mp3')

// 播放音效
const playSound = (sound) => {
  if (soundEnabled.value) {
    sound.currentTime = 0
    sound.play().catch(error => console.error('Error playing sound:', error))
  }
}

// 后端API地址
const API_BASE_URL = 'http://localhost:8081/api'

// 初始化游戏
const startNewGame = async () => {
  try {
    isLoading.value = true
    const response = await fetch(`${API_BASE_URL}/game/connect-four/start`, { 
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      }
    })
    const gameState = await response.json()
    updateGameState(gameState)
    // 保存游戏状态到localStorage
    localStorage.setItem('connectFourGameState', JSON.stringify(gameState))
    ElMessage.success('新游戏开始！')
  } catch (error) {
    ElMessage.error('无法开始新游戏，请稍后重试')
    console.error('Error starting new game:', error)
  } finally {
    isLoading.value = false
  }
}

// 处理玩家落子
const makeMove = async (column) => {
  if (gameStatus.value !== '游戏进行中' || isColumnFull(column) || isLoading.value) {
    return
  }

  try {
    isLoading.value = true
    // 玩家落子
    const playerResponse = await fetch(`${API_BASE_URL}/game/connect-four/move`, { 
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ 
        gameId: gameId.value, 
        column: column 
      })
    })
    let gameState = await playerResponse.json()
    updateGameState(gameState)
    // 播放落子音效
    playSound(dropSound)

    // 检查玩家落子后的游戏结果
    if (gameState.gameStatus === 'PLAYER_WON') {
      ElMessage.success('恭喜你获胜！')
      // 播放获胜音效
      playSound(winSound)
      // 保存游戏记录
      saveGameRecord('玩家获胜')
      // 游戏结束后清除localStorage中的游戏状态
      localStorage.removeItem('connectFourGameState')
      return
    } else if (gameState.gameStatus === 'DRAW') {
      ElMessage.info('平局！')
      // 播放平局音效
      playSound(drawSound)
      // 保存游戏记录
      saveGameRecord('平局')
      // 游戏结束后清除localStorage中的游戏状态
      localStorage.removeItem('connectFourGameState')
      return
    }

    // AI落子（如果游戏未结束）
    const aiResponse = await fetch(`${API_BASE_URL}/game/connect-four/ai-move`, { 
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ 
        gameId: gameId.value 
      })
    })
    gameState = await aiResponse.json()
    updateGameState(gameState)
    // 播放落子音效
    playSound(dropSound)

    // 检查AI落子后的游戏结果
    if (gameState.gameStatus === 'AI_WON') {
      ElMessage.error('AI获胜，再接再厉！')
      // 播放失败音效
      playSound(loseSound)
      // 保存游戏记录
      saveGameRecord('AI获胜')
      // 游戏结束后清除localStorage中的游戏状态
      localStorage.removeItem('connectFourGameState')
    } else if (gameState.gameStatus === 'DRAW') {
      ElMessage.info('平局！')
      // 播放平局音效
      playSound(drawSound)
      // 保存游戏记录
      saveGameRecord('平局')
      // 游戏结束后清除localStorage中的游戏状态
      localStorage.removeItem('connectFourGameState')
    }
  } catch (error) {
    ElMessage.error('落子失败，请稍后重试')
    console.error('Error making move:', error)
  } finally {
    isLoading.value = false
  }
}

// 更新游戏状态
const updateGameState = (gameState) => {
  gameId.value = gameState.gameId
  board.value = gameState.board
  currentPlayer.value = gameState.currentPlayer
  gameStatus.value = mapGameStatus(gameState.gameStatus)
  gameStatusClass.value = mapGameStatusClass(gameState.gameStatus)
  
  // 高亮获胜棋子
  if (gameState.gameStatus !== 'PLAYING' && gameState.lastMoveRow !== undefined && gameState.lastMoveColumn !== undefined) {
    highlightWinningCells(gameState.board, gameState.lastMoveRow, gameState.lastMoveColumn, 
      gameState.gameStatus === 'PLAYER_WON' ? 1 : 2)
  }
  
  // 保存游戏状态到localStorage
  localStorage.setItem('connectFourGameState', JSON.stringify(gameState))
}

// 映射游戏状态
const mapGameStatus = (status) => {
  switch (status) {
    case 'PLAYING': return '游戏进行中'
    case 'PLAYER_WON': return '你获胜了！'
    case 'AI_WON': return 'AI获胜了！'
    case 'DRAW': return '平局！'
    default: return '游戏进行中'
  }
}

// 映射游戏状态样式
const mapGameStatusClass = (status) => {
  switch (status) {
    case 'PLAYING': return 'status-playing'
    case 'PLAYER_WON': return 'status-winning'
    case 'AI_WON': return 'status-losing'
    case 'DRAW': return 'status-draw'
    default: return 'status-playing'
  }
}

// 检查列是否已满
const isColumnFull = (column) => {
  return board.value[0][column] !== 0
}

// 高亮获胜棋子
const highlightWinningCells = (board, row, column, player) => {
  const cells = []
  
  // 检查水平方向
  let left = column
  while (left >= 0 && board[row][left] === player) {
    left--
  }
  let right = column
  while (right < 7 && board[row][right] === player) {
    right++
  }
  if (right - left - 1 >= 4) {
    for (let c = left + 1; c < right; c++) {
      cells.push(`${row}-${c}`)
    }
  }
  
  // 检查垂直方向
  if (cells.length === 0) {
    let down = row
    while (down < 6 && board[down][column] === player) {
      down++
    }
    if (down - row >= 4) {
      for (let r = row; r < down; r++) {
        cells.push(`${r}-${column}`)
      }
    }
  }
  
  // 检查对角线1（左上-右下）
  if (cells.length === 0) {
    let up = row
    let left = column
    while (up >= 0 && left >= 0 && board[up][left] === player) {
      up--
      left--
    }
    let down = row
    let right = column
    while (down < 6 && right < 7 && board[down][right] === player) {
      down++
      right++
    }
    if (down - up - 1 >= 4) {
      for (let i = 0; i < down - up - 1; i++) {
        cells.push(`${up + 1 + i}-${left + 1 + i}`)
      }
    }
  }
  
  // 检查对角线2（右上-左下）
  if (cells.length === 0) {
    let up = row
    let right = column
    while (up >= 0 && right < 7 && board[up][right] === player) {
      up--
      right++
    }
    let down = row
    let left = column
    while (down < 6 && left >= 0 && board[down][left] === player) {
      down++
      left--
    }
    if (down - up - 1 >= 4) {
      for (let i = 0; i < down - up - 1; i++) {
        cells.push(`${up + 1 + i}-${right - 1 - i}`)
      }
    }
  }
  
  winningCells.value = cells
}

// 返回主页
const goBack = () => {
  router.push('/')
}

// 改变AI难度
const changeAIDifficulty = (difficulty) => {
  // 保存AI难度到localStorage
  localStorage.setItem('connectFourAIDifficulty', difficulty)
  ElMessage.info(`AI难度已设置为${difficulty === 'easy' ? '简单' : difficulty === 'medium' ? '中等' : '困难'}`)
}

// 切换音效
const toggleSound = (enabled) => {
  // 保存音效设置到localStorage
  localStorage.setItem('connectFourSoundEnabled', enabled)
  ElMessage.info(enabled ? '音效已开启' : '音效已关闭')
}

// 页面加载时初始化游戏
onMounted(() => {
  // 尝试从localStorage中恢复AI难度
  const savedAIDifficulty = localStorage.getItem('connectFourAIDifficulty')
  if (savedAIDifficulty) {
    aiDifficulty.value = savedAIDifficulty
  }
  
  // 尝试从localStorage中恢复音效设置
  const savedSoundEnabled = localStorage.getItem('connectFourSoundEnabled')
  if (savedSoundEnabled !== null) {
    soundEnabled.value = savedSoundEnabled === 'true'
  }
  
  // 加载游戏记录
  loadGameRecords()
  
  // 尝试从localStorage中恢复游戏状态
  const savedGameState = localStorage.getItem('connectFourGameState')
  if (savedGameState) {
    try {
      const gameState = JSON.parse(savedGameState)
      updateGameState(gameState)
      ElMessage.info('游戏状态已恢复')
    } catch (error) {
      console.error('Error parsing saved game state:', error)
      startNewGame()
    }
  } else {
    startNewGame()
  }
})
</script>

<style scoped>
.game-container {
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 2rem;
}

.game-card {
  max-width: 800px;
  width: 100%;
  border-radius: 12px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 1.2rem;
  font-weight: bold;
}

.game-icon {
  margin-right: 0.5rem;
  color: #E6A23C;
}

.back-button {
  margin-left: auto;
}

.game-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 2rem;
}

.game-info {
  width: 100%;
  display: flex;
  justify-content: space-between;
  margin-bottom: 1rem;
  padding: 1rem;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.game-settings {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  padding: 1rem;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.ai-difficulty {
  display: flex;
  align-items: center;
}

.ai-difficulty label {
  margin-right: 1rem;
  font-weight: bold;
}

.sound-toggle {
  display: flex;
  align-items: center;
}

.sound-toggle label {
  margin-right: 1rem;
  font-weight: bold;
}

.game-records {
  width: 100%;
  margin-bottom: 2rem;
  padding: 1rem;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.game-records h3 {
  margin-top: 0;
  margin-bottom: 1rem;
  font-size: 1.2rem;
  font-weight: bold;
}

.no-records {
  text-align: center;
  color: #909399;
  margin: 0;
}

.win {
  color: #67c23a;
  font-weight: bold;
}

.lose {
  color: #f56c6c;
  font-weight: bold;
}

.draw {
  color: #e6a23c;
  font-weight: bold;
}

.current-player {
  font-size: 1.1rem;
  font-weight: bold;
}

.player-mark {
  padding: 0.2rem 0.8rem;
  border-radius: 4px;
  color: white;
  font-weight: bold;
  margin-left: 0.5rem;
}

.player-1 {
  background-color: #409EFF;
}

.player-2 {
  background-color: #F56C6C;
}

.game-status {
  font-size: 1.1rem;
  font-weight: bold;
}

.status-playing {
  color: #67C23A;
}

.status-winning {
  color: #67C23A;
}

.status-losing {
  color: #F56C6C;
}

.status-draw {
  color: #909399;
}

.game-board {
  display: flex;
  background-color: #0066cc;
  padding: 1rem;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.board-column {
  display: flex;
  flex-direction: column-reverse;
  margin: 0 0.5rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.board-column:hover:not(.column-disabled) {
  transform: translateY(-5px);
}

.column-disabled {
  cursor: not-allowed;
  opacity: 0.7;
}

.board-cell {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  margin: 0.5rem 0;
  transition: all 0.3s ease;
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.15);
  cursor: pointer;
  position: relative;
  z-index: 1;
}

.cell-empty {
  background-color: white;
}

.cell-player-1 {
  background-color: #409EFF;
  animation: dropIn 0.5s ease;
}

.cell-player-2 {
  background-color: #F56C6C;
  animation: dropIn 0.5s ease;
}

.cell-winning {
  animation: pulse 1s infinite;
  border: 3px solid #FFD700;
}

@keyframes dropIn {
  0% {
    transform: translateY(-300px);
    opacity: 0;
  }
  50% {
    opacity: 1;
  }
  80% {
    transform: translateY(10px);
  }
  100% {
    transform: translateY(0);
  }
}

@keyframes pulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
  100% {
    transform: scale(1);
  }
}

.game-controls {
  margin-top: 2rem;
  display: flex;
  gap: 1rem;
}

@media (max-width: 768px) {
  .board-cell {
    width: 40px;
    height: 40px;
  }
  
  .game-info {
    flex-direction: column;
    gap: 1rem;
    text-align: center;
  }
}
</style>
