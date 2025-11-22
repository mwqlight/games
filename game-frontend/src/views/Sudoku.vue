<template>
  <div class="sudoku-container">
    <header class="game-header">
      <h1>数独</h1>
      <div class="game-info">
        <span class="difficulty">难度: {{ difficulty }}</span>
        <el-button type="primary" @click="newGame">新游戏</el-button>
        <el-button type="success" @click="checkSolution">检查答案</el-button>
        <el-button type="warning" @click="hint">提示</el-button>
      </div>
    </header>
    <main class="game-main">
      <div class="sudoku-board">
        <div 
          class="board-row"
          v-for="(row, rowIndex) in board"
          :key="rowIndex"
        >
          <div 
            class="board-cell"
            v-for="(cell, colIndex) in row"
            :key="colIndex"
            :class="{
              'fixed': cell.fixed,
              'error': cell.error,
              'selected': selectedCell.row === rowIndex && selectedCell.col === colIndex,
              'border-right': (colIndex + 1) % 3 === 0 && colIndex !== 8,
              'border-bottom': (rowIndex + 1) % 3 === 0 && rowIndex !== 8
            }"
            @click="selectCell(rowIndex, colIndex)"
          >
            <input 
              v-if="!cell.fixed"
              type="number"
              min="1"
              max="9"
              v-model="cell.value"
              @input="validateCell(rowIndex, colIndex)"
              @keydown="handleKeyDown($event, rowIndex, colIndex)"
              class="cell-input"
            >
            <span v-else class="fixed-cell">{{ cell.value }}</span>
          </div>
        </div>
      </div>
    </main>
    <footer class="game-footer">
      <p v-if="isComplete">恭喜你完成了数独！</p>
      <p v-else-if="hasErrors">有一些错误，请检查红色标记的单元格</p>
      <p v-else>使用数字键1-9填写空格，按Tab键移动到下一个单元格</p>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

// 游戏状态
const board = ref([])
const solution = ref([])
const selectedCell = ref({ row: -1, col: -1 })
const difficulty = ref('简单')
const isComplete = ref(false)
const hasErrors = ref(false)

// 初始化游戏
onMounted(() => {
  newGame()
})

// 新游戏
function newGame() {
  // 创建完整的数独解决方案
  solution.value = generateSolution()
  
  // 根据难度移除一些数字
  board.value = generatePuzzle(solution.value, difficulty.value)
  
  selectedCell.value = { row: -1, col: -1 }
  isComplete.value = false
  hasErrors.value = false
}

// 生成数独解决方案
function generateSolution() {
  const board = Array(9).fill(null).map(() => Array(9).fill(0))
  fillBoard(board)
  return board
}

// 填充数独棋盘
function fillBoard(board) {
  const numbers = [1, 2, 3, 4, 5, 6, 7, 8, 9]
  
  for (let row = 0; row < 9; row++) {
    for (let col = 0; col < 9; col++) {
      if (board[row][col] === 0) {
        // 随机打乱数字顺序
        const shuffledNumbers = shuffle([...numbers])
        
        for (let num of shuffledNumbers) {
          if (isValidMove(board, row, col, num)) {
            board[row][col] = num
            
            if (fillBoard(board)) {
              return true
            }
            
            board[row][col] = 0
          }
        }
        
        return false
      }
    }
  }
  
  return true
}

// 检查移动是否有效
function isValidMove(board, row, col, num) {
  // 检查行
  for (let c = 0; c < 9; c++) {
    if (board[row][c] === num) return false
  }
  
  // 检查列
  for (let r = 0; r < 9; r++) {
    if (board[r][col] === num) return false
  }
  
  // 检查3x3宫格
  const boxRow = Math.floor(row / 3) * 3
  const boxCol = Math.floor(col / 3) * 3
  
  for (let r = boxRow; r < boxRow + 3; r++) {
    for (let c = boxCol; c < boxCol + 3; c++) {
      if (board[r][c] === num) return false
    }
  }
  
  return true
}

// 生成谜题
function generatePuzzle(solution, difficulty) {
  const puzzle = solution.map(row => row.map(num => ({ value: num, fixed: true, error: false })))
  
  // 根据难度移除不同数量的数字
  const cellsToRemove = {
    '简单': 40,
    '中等': 50,
    '困难': 60
  }[difficulty] || 40
  
  const positions = []
  for (let row = 0; row < 9; row++) {
    for (let col = 0; col < 9; col++) {
      positions.push({ row, col })
    }
  }
  
  // 随机打乱位置
  const shuffledPositions = shuffle(positions)
  
  // 移除数字
  for (let i = 0; i < cellsToRemove; i++) {
    const { row, col } = shuffledPositions[i]
    puzzle[row][col] = { value: '', fixed: false, error: false }
  }
  
  return puzzle
}

// 随机打乱数组
function shuffle(array) {
  const shuffled = [...array]
  for (let i = shuffled.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1))
    ;[shuffled[i], shuffled[j]] = [shuffled[j], shuffled[i]]
  }
  return shuffled
}

// 选择单元格
function selectCell(row, col) {
  if (board.value[row][col].fixed) return
  selectedCell.value = { row, col }
}

// 验证单元格
function validateCell(row, col) {
  const value = parseInt(board.value[row][col].value)
  if (isNaN(value) || value < 1 || value > 9) {
    board.value[row][col].value = ''
    board.value[row][col].error = false
    return
  }
  
  // 检查是否与解决方案一致
  if (value !== solution.value[row][col]) {
    board.value[row][col].error = true
    hasErrors.value = true
  } else {
    board.value[row][col].error = false
    // 检查是否所有错误都已修复
    hasErrors.value = board.value.some(row => row.some(cell => cell.error))
  }
  
  // 检查是否完成
  checkComplete()
}

// 检查是否完成
function checkComplete() {
  for (let row = 0; row < 9; row++) {
    for (let col = 0; col < 9; col++) {
      const cell = board.value[row][col]
      if (cell.value === '' || parseInt(cell.value) !== solution.value[row][col]) {
        isComplete.value = false
        return
      }
    }
  }
  isComplete.value = true
}

// 检查答案
function checkSolution() {
  hasErrors.value = false
  
  for (let row = 0; row < 9; row++) {
    for (let col = 0; col < 9; col++) {
      const cell = board.value[row][col]
      if (cell.value !== '' && parseInt(cell.value) !== solution.value[row][col]) {
        cell.error = true
        hasErrors.value = true
      } else {
        cell.error = false
      }
    }
  }
  
  if (!hasErrors.value) {
    checkComplete()
  }
}

// 提示
function hint() {
  // 找到第一个空单元格
  for (let row = 0; row < 9; row++) {
    for (let col = 0; col < 9; col++) {
      const cell = board.value[row][col]
      if (cell.value === '') {
        cell.value = solution.value[row][col]
        cell.error = false
        selectedCell.value = { row, col }
        checkComplete()
        return
      }
    }
  }
}

// 键盘事件处理
function handleKeyDown(event, row, col) {
  if (event.key >= '1' && event.key <= '9') {
    board.value[row][col].value = event.key
    validateCell(row, col)
  } else if (event.key === 'Backspace' || event.key === 'Delete') {
    board.value[row][col].value = ''
    board.value[row][col].error = false
    hasErrors.value = board.value.some(row => row.some(cell => cell.error))
  } else if (event.key === 'Tab') {
    event.preventDefault()
    // 移动到下一个单元格
    let nextRow = row
    let nextCol = col + 1
    if (nextCol >= 9) {
      nextCol = 0
      nextRow = row + 1
      if (nextRow >= 9) {
        nextRow = 0
      }
    }
    
    // 跳过固定单元格
    while (nextRow < 9 && board.value[nextRow][nextCol].fixed) {
      nextCol++
      if (nextCol >= 9) {
        nextCol = 0
        nextRow++
      }
    }
    
    if (nextRow < 9) {
      selectedCell.value = { row: nextRow, col: nextCol }
    }
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
  text-align: center;
  padding: 1rem;
}

.game-header h1 {
  font-size: 2rem;
  margin-bottom: 1rem;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
}

.game-info {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  flex-wrap: wrap;
}

.difficulty {
  font-size: 1.2rem;
  font-weight: bold;
}

.game-main {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 1rem;
}

.sudoku-board {
  background: #2c3e50;
  padding: 1rem;
  border-radius: 12px;
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.3);
}

.board-row {
  display: flex;
}

.board-cell {
  width: 60px;
  height: 60px;
  background: #ecf0f1;
  border: 1px solid #bdc3c7;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  position: relative;
}

.board-cell.fixed {
  background: #bdc3c7;
  cursor: default;
}

.board-cell.error {
  background: #e74c3c;
  color: white;
}

.board-cell.selected {
  background: #3498db;
  color: white;
}

.board-cell.border-right {
  border-right: 3px solid #2c3e50;
}

.board-cell.border-bottom {
  border-bottom: 3px solid #2c3e50;
}

.cell-input {
  width: 100%;
  height: 100%;
  text-align: center;
  font-size: 1.5rem;
  font-weight: bold;
  border: none;
  background: transparent;
  outline: none;
  color: #2c3e50;
}

.fixed-cell {
  font-size: 1.5rem;
  font-weight: bold;
  color: #2c3e50;
}

.game-footer {
  text-align: center;
  padding: 1rem;
  font-size: 1.2rem;
  font-weight: bold;
}
</style>