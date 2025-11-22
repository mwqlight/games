<template>
  <div class="scrabble-container">
    <header class="game-header">
      <h1>拼字游戏</h1>
      <div class="game-info">
        <span class="current-player">当前玩家: {{ currentPlayer }}</span>
        <span class="score">分数: {{ score }}</span>
        <el-button type="primary" @click="newGame">新游戏</el-button>
        <el-button type="success" @click="submitWord">提交单词</el-button>
      </div>
    </header>
    <main class="game-main">
      <div class="game-board">
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
              'double-word': cell.type === 'DW',
              'triple-word': cell.type === 'TW',
              'double-letter': cell.type === 'DL',
              'triple-letter': cell.type === 'TL',
              'center': cell.type === 'C',
              'has-tile': cell.tile,
              'selected': selectedCell.row === rowIndex && selectedCell.col === colIndex
            }"
            @click="selectCell(rowIndex, colIndex)"
          >
            <div v-if="cell.tile" class="tile">
              <span class="letter">{{ cell.tile.letter }}</span>
              <span class="value">{{ cell.tile.value }}</span>
            </div>
            <span v-else class="cell-type">{{ cell.type }}</span>
          </div>
        </div>
      </div>
      <div class="player-tiles">
        <h3>你的字母</h3>
        <div class="tiles-container">
          <div 
            class="tile hand-tile"
            v-for="(tile, index) in playerTiles"
            :key="index"
            :class="{ 'selected': selectedTileIndex === index }"
            @click="selectTile(index)"
          >
            <span class="letter">{{ tile.letter }}</span>
            <span class="value">{{ tile.value }}</span>
          </div>
        </div>
        <el-button type="warning" @click="swapTiles" :disabled="playerTiles.length === 0">交换字母</el-button>
      </div>
    </main>
    <footer class="game-footer">
      <p v-if="message">{{ message }}</p>
      <p v-else>点击棋盘放置字母，点击字母选择要放置的字母</p>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

// 游戏状态
const board = ref([])
const playerTiles = ref([])
const currentPlayer = ref('玩家1')
const score = ref(0)
const selectedCell = ref({ row: -1, col: -1 })
const selectedTileIndex = ref(-1)
const message = ref('')

// 字母分值
const letterValues = {
  'A': 1, 'B': 3, 'C': 3, 'D': 2, 'E': 1, 'F': 4, 'G': 2, 'H': 4, 'I': 1, 'J': 8,
  'K': 5, 'L': 1, 'M': 3, 'N': 1, 'O': 1, 'P': 3, 'Q': 10, 'R': 1, 'S': 1, 'T': 1,
  'U': 1, 'V': 4, 'W': 4, 'X': 8, 'Y': 4, 'Z': 10, ' ': 0
}

// 初始化游戏
onMounted(() => {
  newGame()
})

// 新游戏
function newGame() {
  // 初始化棋盘
  board.value = createBoard()
  
  // 初始化玩家字母
  playerTiles.value = drawTiles(7)
  
  currentPlayer.value = '玩家1'
  score.value = 0
  selectedCell.value = { row: -1, col: -1 }
  selectedTileIndex.value = -1
  message.value = ''
}

// 创建棋盘
function createBoard() {
  const board = Array(15).fill(null).map(() => Array(15).fill({ type: '', tile: null }))
  
  // 设置特殊格子
  // 三字母得分 (TL)
  const tlPositions = [
    [0, 0], [0, 7], [0, 14],
    [7, 0], [7, 14],
    [14, 0], [14, 7], [14, 14]
  ]
  
  // 双字母得分 (DL)
  const dlPositions = [
    [0, 3], [0, 11],
    [2, 6], [2, 8],
    [3, 0], [3, 7], [3, 14],
    [6, 2], [6, 6], [6, 8], [6, 12],
    [7, 3], [7, 11],
    [8, 2], [8, 6], [8, 8], [8, 12],
    [11, 0], [11, 7], [11, 14],
    [12, 6], [12, 8],
    [14, 3], [14, 11]
  ]
  
  // 三单词得分 (TW)
  const twPositions = [
    [1, 1], [1, 13],
    [2, 2], [2, 12],
    [3, 3], [3, 11],
    [4, 4], [4, 10],
    [10, 4], [10, 10],
    [11, 3], [11, 11],
    [12, 2], [12, 12],
    [13, 1], [13, 13]
  ]
  
  // 双单词得分 (DW)
  const dwPositions = [
    [1, 5], [1, 9],
    [5, 1], [5, 5], [5, 9], [5, 13],
    [9, 1], [9, 5], [9, 9], [9, 13],
    [13, 5], [13, 9]
  ]
  
  // 中心格子 (C)
  const centerPosition = [7, 7]
  
  // 设置特殊格子类型
  tlPositions.forEach(([row, col]) => board[row][col].type = 'TL')
  dlPositions.forEach(([row, col]) => board[row][col].type = 'DL')
  twPositions.forEach(([row, col]) => board[row][col].type = 'TW')
  dwPositions.forEach(([row, col]) => board[row][col].type = 'DW')
  board[centerPosition[0]][centerPosition[1]].type = 'C'
  
  return board
}

// 生成字母池
function createLetterPool() {
  const letterCounts = {
    'A': 9, 'B': 2, 'C': 2, 'D': 4, 'E': 12, 'F': 2, 'G': 3, 'H': 2, 'I': 9, 'J': 1,
    'K': 1, 'L': 4, 'M': 2, 'N': 6, 'O': 8, 'P': 2, 'Q': 1, 'R': 6, 'S': 4, 'T': 6,
    'U': 4, 'V': 2, 'W': 2, 'X': 1, 'Y': 2, 'Z': 1, ' ': 2
  }
  
  const pool = []
  for (const [letter, count] of Object.entries(letterCounts)) {
    for (let i = 0; i < count; i++) {
      pool.push({ letter, value: letterValues[letter] })
    }
  }
  
  return shuffle(pool)
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

// 抽取字母
function drawTiles(count) {
  const pool = createLetterPool()
  return pool.slice(0, count)
}

// 选择单元格
function selectCell(row, col) {
  selectedCell.value = { row, col }
  
  // 如果有选中的字母，尝试放置
  if (selectedTileIndex.value !== -1) {
    placeTile(row, col)
  }
}

// 选择字母
function selectTile(index) {
  selectedTileIndex.value = index
  
  // 如果有选中的单元格，尝试放置
  if (selectedCell.value.row !== -1) {
    placeTile(selectedCell.value.row, selectedCell.value.col)
  }
}

// 放置字母
function placeTile(row, col) {
  if (board.value[row][col].tile) return // 单元格已有字母
  
  const tile = playerTiles.value[selectedTileIndex.value]
  if (!tile) return
  
  // 放置字母
  board.value[row][col].tile = tile
  
  // 从玩家手中移除字母
  playerTiles.value.splice(selectedTileIndex.value, 1)
  
  // 重置选择
  selectedTileIndex.value = -1
  selectedCell.value = { row: -1, col: -1 }
  
  message.value = ''
}

// 提交单词
function submitWord() {
  // 简单的提交逻辑，实际游戏中需要验证单词是否有效
  // 这里只是计算当前分数
  let wordScore = 0
  let wordMultiplier = 1
  
  // 遍历棋盘计算分数
  for (let row = 0; row < 15; row++) {
    for (let col = 0; col < 15; col++) {
      const cell = board.value[row][col]
      if (cell.tile) {
        let letterScore = cell.tile.value
        
        // 应用字母得分倍数
        if (cell.type === 'DL') {
          letterScore *= 2
        } else if (cell.type === 'TL') {
          letterScore *= 3
        }
        
        // 应用单词得分倍数
        if (cell.type === 'DW') {
          wordMultiplier *= 2
        } else if (cell.type === 'TW') {
          wordMultiplier *= 3
        }
        
        wordScore += letterScore
      }
    }
  }
  
  wordScore *= wordMultiplier
  score.value += wordScore
  
  // 补充字母
  if (playerTiles.value.length < 7) {
    const newTiles = drawTiles(7 - playerTiles.value.length)
    playerTiles.value.push(...newTiles)
  }
  
  message.value = `提交成功！获得 ${wordScore} 分`
}

// 交换字母
function swapTiles() {
  if (playerTiles.value.length === 0) return
  
  // 简单的交换逻辑，实际游戏中需要放回字母池
  const swappedTiles = playerTiles.value.splice(selectedTileIndex.value || 0, 1)
  const newTiles = drawTiles(swappedTiles.length)
  playerTiles.value.push(...newTiles)
  
  selectedTileIndex.value = -1
  message.value = '交换成功！'
}
</script>

<style scoped>
.scrabble-container {
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

.current-player,
.score {
  font-size: 1.2rem;
  font-weight: bold;
}

.game-main {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  gap: 2rem;
  padding: 1rem;
  overflow-x: auto;
}

.game-board {
  background: #8B4513;
  padding: 1rem;
  border-radius: 12px;
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.3);
}

.board-row {
  display: flex;
}

.board-cell {
  width: 40px;
  height: 40px;
  background: #DEB887;
  border: 1px solid #8B4513;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  position: relative;
  font-size: 0.8rem;
  font-weight: bold;
}

.board-cell.double-word {
  background: #FFB6C1;
}

.board-cell.triple-word {
  background: #FF69B4;
}

.board-cell.double-letter {
  background: #98FB98;
}

.board-cell.triple-letter {
  background: #32CD32;
}

.board-cell.center {
  background: #98FB98;
}

.board-cell.has-tile {
  background: #F5DEB3;
}

.board-cell.selected {
  background: #3498db;
  color: white;
}

.cell-type {
  color: #8B4513;
}

.tile {
  width: 35px;
  height: 35px;
  background: #F5DEB3;
  border: 1px solid #8B4513;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  font-size: 1rem;
  font-weight: bold;
  color: #8B4513;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.tile .letter {
  font-size: 1.2rem;
}

.tile .value {
  font-size: 0.6rem;
  align-self: flex-end;
  margin-right: 3px;
  margin-bottom: 1px;
}

.player-tiles {
  background: #2c3e50;
  padding: 1rem;
  border-radius: 12px;
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.3);
  min-width: 300px;
}

.player-tiles h3 {
  text-align: center;
  margin-bottom: 1rem;
}

.tiles-container {
  display: flex;
  justify-content: center;
  gap: 0.5rem;
  margin-bottom: 1rem;
  flex-wrap: wrap;
}

.hand-tile {
  width: 50px;
  height: 50px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.hand-tile:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 10px rgba(0, 0, 0, 0.3);
}

.hand-tile.selected {
  background: #3498db;
  color: white;
}

.game-footer {
  text-align: center;
  padding: 1rem;
  font-size: 1.2rem;
  font-weight: bold;
}
</style>