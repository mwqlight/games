<template>
  <div class="sudoku-container">
    <header class="game-header">
      <h1>数独游戏</h1>
      <div class="game-info">
        <el-button type="primary" @click="newGame">新游戏</el-button>
        <el-button type="success" @click="checkSolution">检查答案</el-button>
        <el-button type="warning" @click="getHint">提示</el-button>
        <el-button type="danger" @click="clearBoard">清除</el-button>
      </div>
    </header>
    <main class="game-main">
      <div class="game-stats">
        <div class="stat-item">
          <span class="stat-label">难度:</span>
          <span class="stat-value">{{ difficulty }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">时间:</span>
          <span class="stat-value">{{ formatTime(elapsedTime) }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">错误:</span>
          <span class="stat-value">{{ errorCount }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">提示:</span>
          <span class="stat-value">{{ hintCount }}</span>
        </div>
      </div>
      <div class="sudoku-board" v-if="userBoard && puzzle && solution && userBoard.length > 0 && puzzle.length > 0 && solution.length > 0">
        <div 
          v-for="(row, rowIndex) in 9" 
          :key="rowIndex" 
          class="sudoku-row"
        >
          <div 
            v-for="(cell, colIndex) in 9" 
            :key="colIndex" 
            class="sudoku-cell"
            :class="{
              'prefilled': puzzle[rowIndex][colIndex] !== 0,
              'selected': selectedCell.row === rowIndex && selectedCell.col === colIndex,
              'error': userBoard[rowIndex][colIndex] !== 0 && userBoard[rowIndex][colIndex] !== solution[rowIndex][colIndex],
              'highlight-row': selectedCell.row === rowIndex,
              'highlight-col': selectedCell.col === colIndex,
              'highlight-box': isSameBox(selectedCell.row, selectedCell.col, rowIndex, colIndex)
            }"
            @click="selectCell(rowIndex, colIndex)"
            @keydown="handleKeyDown($event, rowIndex, colIndex)"
            tabindex="0"
          >
            {{ userBoard[rowIndex][colIndex] || '' }}
          </div>
        </div>
      </div>
      <div v-else class="loading">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>
      <div class="number-selector">
        <button 
          v-for="num in 9" 
          :key="num" 
          class="number-button"
          @click="inputNumber(num)"
        >
          {{ num }}
        </button>
        <button class="number-button clear-button" @click="inputNumber(0)">
          清除
        </button>
      </div>
      <div class="game-status" v-if="gameStatus !== 'PLAYING'">
        <h2>{{ gameStatus === 'SUCCESS' ? '恭喜你，完成了数独！' : '游戏失败，请重试。' }}</h2>
        <el-button type="primary" @click="newGame">再来一局</el-button>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const gameId = ref(null)
const puzzle = ref([])
const userBoard = ref([])
const solution = ref([])
const gameStatus = ref('PLAYING')
const difficulty = ref('MEDIUM')
const elapsedTime = ref(0)
const hintCount = ref(0)
const errorCount = ref(0)
const selectedCell = ref({ row: -1, col: -1 })
const timer = ref(null)

// 初始化游戏
const initGame = async () => {
  try {
    const response = await axios.get('/api/sudoku/new', {
      params: { difficulty: difficulty.value }
    })
    const game = response.data
    gameId.value = game.gameId
    puzzle.value = game.puzzle
    userBoard.value = game.userBoard
    solution.value = game.solution
    gameStatus.value = game.gameStatus
    elapsedTime.value = game.elapsedTime
    hintCount.value = game.hintCount
    errorCount.value = game.errorCount
    selectedCell.value = { row: -1, col: -1 }
    startTimer()
  } catch (error) {
    console.error('Failed to initialize game:', error)
    ElMessage.error('初始化游戏失败，请重试。')
  }
}

// 开始计时器
const startTimer = () => {
  if (timer.value) {
    clearInterval(timer.value)
  }
  timer.value = setInterval(() => {
    if (gameStatus.value === 'PLAYING') {
      elapsedTime.value++
    }
  }, 1000)
}

// 格式化时间
const formatTime = (seconds) => {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

// 选择单元格
const selectCell = (row, col) => {
  if (puzzle.value[row][col] !== 0) {
    return
  }
  selectedCell.value = { row, col }
  document.querySelectorAll('.sudoku-cell')[row * 9 + col].focus()
}

// 处理键盘输入
const handleKeyDown = (event, row, col) => {
  if (puzzle.value[row][col] !== 0) {
    return
  }
  if (event.key >= '1' && event.key <= '9') {
    inputNumber(parseInt(event.key))
  } else if (event.key === '0' || event.key === 'Delete' || event.key === 'Backspace') {
    inputNumber(0)
  } else if (event.key === 'ArrowUp' && row > 0) {
    selectCell(row - 1, col)
  } else if (event.key === 'ArrowDown' && row < 8) {
    selectCell(row + 1, col)
  } else if (event.key === 'ArrowLeft' && col > 0) {
    selectCell(row, col - 1)
  } else if (event.key === 'ArrowRight' && col < 8) {
    selectCell(row, col + 1)
  }
}

// 输入数字
const inputNumber = async (number) => {
  if (selectedCell.value.row === -1 || selectedCell.value.col === -1) {
    ElMessage.warning('请先选择一个单元格。')
    return
  }
  if (puzzle.value[selectedCell.value.row][selectedCell.value.col] !== 0) {
    ElMessage.warning('这个单元格是预填的，不能修改。')
    return
  }
  try {
    const response = await axios.post(`/api/sudoku/${gameId.value}/move`, {
      row: selectedCell.value.row,
      col: selectedCell.value.col,
      number: number
    })
    const game = response.data
    userBoard.value = game.userBoard
    gameStatus.value = game.gameStatus
    errorCount.value = game.errorCount
    if (gameStatus.value !== 'PLAYING') {
      clearInterval(timer.value)
      if (gameStatus.value === 'SUCCESS') {
        ElMessage.success('恭喜你，完成了数独！')
      } else {
        ElMessage.error('游戏失败，请重试。')
      }
    }
  } catch (error) {
    console.error('Failed to make move:', error)
    ElMessage.error('移动失败，请重试。')
  }
}

// 检查答案
const checkSolution = async () => {
  try {
    const response = await axios.get(`/api/sudoku/${gameId.value}/validate`)
    const isValid = response.data.valid
    if (isValid) {
      gameStatus.value = 'SUCCESS'
      clearInterval(timer.value)
      ElMessage.success('恭喜你，答案正确！')
    } else {
      ElMessage.warning('答案不正确，请检查错误。')
    }
  } catch (error) {
    console.error('Failed to validate solution:', error)
    ElMessage.error('检查答案失败，请重试。')
  }
}

// 获取提示
const getHint = async () => {
  try {
    const response = await axios.get(`/api/sudoku/${gameId.value}/hint`)
    const game = response.data
    userBoard.value = game.userBoard
    gameStatus.value = game.gameStatus
    hintCount.value = game.hintCount
    if (gameStatus.value !== 'PLAYING') {
      clearInterval(timer.value)
      ElMessage.success('恭喜你，完成了数独！')
    } else {
      ElMessage.info('已给出一个提示。')
    }
  } catch (error) {
    console.error('Failed to get hint:', error)
    ElMessage.error('获取提示失败，请重试。')
  }
}

// 清除棋盘
const clearBoard = () => {
  for (let row = 0; row < 9; row++) {
    for (let col = 0; col < 9; col++) {
      if (puzzle.value[row][col] === 0) {
        userBoard.value[row][col] = 0
      }
    }
  }
  selectedCell.value = { row: -1, col: -1 }
  errorCount.value = 0
  ElMessage.info('棋盘已清除。')
}

// 开始新游戏
const newGame = () => {
  ElMessageBox.prompt('请选择难度:', '新游戏', {
    confirmButtonText: '开始',
    cancelButtonText: '取消',
    inputValue: difficulty.value,
    inputPlaceholder: 'EASY, MEDIUM, HARD',
    inputValidator: (value) => {
      if (!['EASY', 'MEDIUM', 'HARD'].includes(value.toUpperCase())) {
        return '请输入正确的难度级别: EASY, MEDIUM, HARD'
      }
      return true
    }
  }).then(({ value }) => {
    difficulty.value = value.toUpperCase()
    initGame()
  }).catch(() => {
    // 用户取消
  })
}

// 检查是否在同一个3x3宫格内
const isSameBox = (row1, col1, row2, col2) => {
  if (row1 === -1 || col1 === -1) {
    return false
  }
  return Math.floor(row1 / 3) === Math.floor(row2 / 3) && Math.floor(col1 / 3) === Math.floor(col2 / 3)
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
.sudoku-container {
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

.game-stats {
  display: flex;
  gap: 20px;
  justify-content: center;
  flex-wrap: wrap;
}

.stat-item {
  font-size: 1.1rem;
  font-weight: bold;
  background: rgba(255, 255, 255, 0.1);
  padding: 10px 20px;
  border-radius: 8px;
  backdrop-filter: blur(10px);
}

.stat-label {
  margin-right: 5px;
}

.stat-value {
  color: #409EFF;
}

.sudoku-board {
  display: flex;
  flex-direction: column;
  border: 3px solid white;
  border-radius: 8px;
  overflow: hidden;
  background-color: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
}

.sudoku-row {
  display: flex;
}

.sudoku-cell {
  width: 50px;
  height: 50px;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid white;
  font-size: 1.2rem;
  font-weight: bold;
  user-select: none;
}

.sudoku-cell:hover {
  background-color: rgba(255, 255, 255, 0.2);
}

.sudoku-cell:focus {
  outline: none;
  box-shadow: 0 0 0 2px #409EFF;
}

.sudoku-cell.prefilled {
  color: #303133;
  background-color: rgba(255, 255, 255, 0.3);
  cursor: default;
}

.sudoku-cell.selected {
  background-color: rgba(64, 158, 255, 0.5);
  border-color: #409EFF;
}

.sudoku-cell.error {
  color: #F56C6C;
}

.sudoku-cell.highlight-row,
.sudoku-cell.highlight-col,
.sudoku-cell.highlight-box {
  background-color: rgba(255, 255, 0, 0.1);
}

/* 添加3x3宫格的粗边框 */
.sudoku-row:nth-child(3n) .sudoku-cell,
.sudoku-row:nth-child(6n) .sudoku-cell {
  border-bottom: 3px solid white;
}

.sudoku-cell:nth-child(3n) {
  border-right: 3px solid white;
}

.number-selector {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: center;
  max-width: 500px;
}

.number-button {
  width: 50px;
  height: 50px;
  font-size: 1.2rem;
  font-weight: bold;
  border: 2px solid white;
  border-radius: 8px;
  background-color: rgba(255, 255, 255, 0.1);
  color: white;
  cursor: pointer;
  transition: all 0.3s ease;
}

.number-button:hover {
  background-color: rgba(255, 255, 255, 0.2);
  transform: translateY(-2px);
}

.number-button.clear-button {
  width: auto;
  padding: 0 20px;
}

.game-status {
  text-align: center;
  background: rgba(255, 255, 255, 0.1);
  padding: 20px;
  border-radius: 8px;
  backdrop-filter: blur(10px);
}

.game-status h2 {
  margin-bottom: 20px;
  font-size: 1.5rem;
}

@media (max-width: 600px) {
  .sudoku-cell {
    width: 40px;
    height: 40px;
    font-size: 1rem;
  }
  
  .number-button {
    width: 40px;
    height: 40px;
    font-size: 1rem;
  }
  
  .game-stats {
    gap: 10px;
  }
  
  .stat-item {
    font-size: 1rem;
    padding: 8px 15px;
  }
}

/* 自定义加载动画 */
.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  gap: 20px;
}

.loading-spinner {
  width: 50px;
  height: 50px;
  border: 4px solid rgba(255, 255, 255, 0.3);
  border-top: 4px solid white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>
