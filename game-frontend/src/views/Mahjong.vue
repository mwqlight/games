<template>
  <div class="mahjong-container">
    <header class="game-header">
      <h1>连连看游戏</h1>
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
            <span>得分: {{ score }}</span>
          </div>
          <div class="stat-item">
            <span>时间: {{ time }}</span>
          </div>
          <div class="stat-item">
            <span>剩余: {{ remainingPairs }}</span>
          </div>
        </div>
      </div>
      <div class="game-board">
        <div 
          class="board-cell" 
          v-for="(cell, index) in board" 
          :key="index"
          :class="{
            'hidden': cell.hidden,
            'selected': selectedCellIndex === index,
            'matched': cell.matched
          }"
          @click="selectCell(index)"
        >
          <div class="cell-content">
            {{ getCellSymbol(cell.value) }}
          </div>
        </div>
      </div>
      <div class="game-controls">
        <el-button type="primary" @click="startGame" :disabled="gameStarted">开始游戏</el-button>
        <el-button type="warning" @click="pauseGame" :disabled="!gameStarted || gamePaused">暂停游戏</el-button>
        <el-button type="danger" @click="stopGame" :disabled="!gameStarted">停止游戏</el-button>
      </div>
      <div class="game-instructions">
        <p>点击相同的图案进行消除</p>
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
const gameStatus = ref('准备开始')
const score = ref(0)
const time = ref(0)
const remainingPairs = ref(0)
const selectedCellIndex = ref(null)
const gameStarted = ref(false)
const gamePaused = ref(false)
const timer = ref(null)

// 游戏配置
const boardSize = 16
const symbols = ['🎈', '🎨', '🎭', '🎪', '🎯', '🎲', '🎸', '🎹', '🎺', '🎻', '🎼', '🎽', '🎾', '🎿', '🏀', '🏈']

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
  
  // 初始化得分
  score.value = 0
  
  // 初始化时间
  time.value = 0
  
  // 初始化选中的单元格
  selectedCellIndex.value = null
  
  // 生成棋盘
  generateBoard()
}

// 生成棋盘
const generateBoard = () => {
  // 创建一个包含所有符号的数组，每个符号出现两次
  const boardSymbols = [...symbols, ...symbols]
  
  // 洗牌
  shuffleArray(boardSymbols)
  
  // 生成棋盘
  board.value = boardSymbols.map((symbol, index) => ({
    value: symbol,
    hidden: false,
    matched: false
  }))
  
  // 初始化剩余配对数量
  remainingPairs.value = symbols.length
}

// 洗牌
const shuffleArray = (array) => {
  for (let i = array.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1))
    ;[array[i], array[j]] = [array[j], array[i]]
  }
}

// 获取单元格符号
const getCellSymbol = (value) => {
  return value
}

// 选中单元格
const selectCell = (index) => {
  // 检查游戏是否已经开始
  if (!gameStarted.value || gamePaused.value) {
    return
  }
  
  // 检查单元格是否已经隐藏或匹配
  if (board.value[index].hidden || board.value[index].matched) {
    return
  }
  
  // 检查是否已经选中了一个单元格
  if (selectedCellIndex.value === null) {
    // 选中第一个单元格
    selectedCellIndex.value = index
  } else {
    // 检查是否选中了同一个单元格
    if (selectedCellIndex.value === index) {
      // 取消选中
      selectedCellIndex.value = null
      return
    }
    
    // 检查两个单元格是否匹配
    if (board.value[selectedCellIndex.value].value === board.value[index].value) {
      // 匹配成功
      board.value[selectedCellIndex.value].matched = true
      board.value[index].matched = true
      
      // 增加得分
      score.value += 10
      
      // 减少剩余配对数量
      remainingPairs.value--
      
      // 取消选中
      selectedCellIndex.value = null
      
      // 检查游戏是否结束
      checkGameEnd()
    } else {
      // 匹配失败
      // 延迟隐藏单元格
      setTimeout(() => {
        board.value[selectedCellIndex.value].hidden = true
        board.value[index].hidden = true
        
        // 取消选中
        selectedCellIndex.value = null
      }, 500)
    }
  }
}

// 检查游戏是否结束
const checkGameEnd = () => {
  // 检查是否所有配对都已经完成
  if (remainingPairs.value === 0) {
    gameStatus.value = '恭喜获胜'
    clearInterval(timer.value)
  }
}

// 开始游戏
const startGame = () => {
  gameStatus.value = '游戏进行中'
  gameStarted.value = true
  gamePaused.value = false
  
  // 开始计时器
  timer.value = setInterval(() => {
    time.value++
  }, 1000)
}

// 暂停游戏
const pauseGame = () => {
  gameStatus.value = '游戏暂停'
  gamePaused.value = true
  
  // 清除计时器
  if (timer.value) {
    clearInterval(timer.value)
    timer.value = null
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
.mahjong-container {
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
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
  padding: 20px;
  border: 2px solid white;
  border-radius: 8px;
  background-color: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
}

.board-cell {
  width: 80px;
  height: 80px;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
  border: 2px solid white;
  border-radius: 8px;
  background-color: rgba(255, 255, 255, 0.2);
}

.board-cell.hidden {
  background-color: rgba(255, 255, 255, 0.1);
}

.board-cell.selected {
  transform: scale(1.1);
  box-shadow: 0 0 20px rgba(255, 255, 0, 0.8);
  border-color: yellow;
}

.board-cell.matched {
  background-color: rgba(0, 255, 0, 0.2);
  border-color: green;
}

.cell-content {
  font-size: 2rem;
}

.game-controls {
  display: flex;
  gap: 10px;
}

.game-instructions {
  font-size: 1.1rem;
}
</style>