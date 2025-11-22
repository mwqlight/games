<template>
  <div class="poker-container">
    <header class="game-header">
      <h1>扑克游戏</h1>
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
            <span>玩家1: {{ player1Score }}</span>
          </div>
          <div class="stat-item">
            <span>玩家2: {{ player2Score }}</span>
          </div>
          <div class="stat-item">
            <span>当前玩家: {{ currentPlayer === 1 ? '玩家1' : '玩家2' }}</span>
          </div>
        </div>
      </div>
      <div class="game-board">
        <div class="player-hand">
          <h3>玩家1的牌</h3>
          <div class="hand-cards">
            <div 
              class="card" 
              v-for="(card, index) in player1Hand" 
              :key="index"
              :class="{
                'selected': selectedCard === index
              }"
              @click="selectCard(index)"
            >
              <span>{{ getCardSymbol(card) }}</span>
            </div>
          </div>
        </div>
        <div class="table-cards">
          <h3>桌面的牌</h3>
          <div class="hand-cards">
            <div 
              class="card" 
              v-for="(card, index) in tableCards" 
              :key="index"
            >
              <span>{{ getCardSymbol(card) }}</span>
            </div>
          </div>
        </div>
        <div class="player-hand">
          <h3>玩家2的牌</h3>
          <div class="hand-cards">
            <div 
              class="card" 
              v-for="(card, index) in player2Hand" 
              :key="index"
            >
              <span>{{ getCardSymbol(card) }}</span>
            </div>
          </div>
        </div>
      </div>
      <div class="game-controls">
        <el-button type="primary" @click="startGame">开始游戏</el-button>
        <el-button type="warning" @click="pauseGame">暂停游戏</el-button>
        <el-button type="danger" @click="stopGame">停止游戏</el-button>
      </div>
      <div class="game-instructions">
        <p>点击棋子进行移动，按照扑克规则进行游戏</p>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 初始化游戏状态
const player1Hand = ref([])
const player2Hand = ref([])
const tableCards = ref([])
const selectedCard = ref(null)
const gameStatus = ref('准备开始')
const player1Score = ref(0)
const player2Score = ref(0)
const currentPlayer = ref(1)
const gameStarted = ref(false)
const gamePaused = ref(false)
const timer = ref(null)
const time = ref(0)
const score = ref(0)

// 游戏配置
const initialPlayer = ref(1)

// 扑克牌类型
const suits = ['♠', '♥', '♦', '♣']
const ranks = ['A', '2', '3', '4', '5', '6', '7', '8', '9', '10', 'J', 'Q', 'K']

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
  time.value = 0
  score.value = 0
  
  // 初始化分数
  player1Score.value = 0
  player2Score.value = 0
  
  // 初始化当前玩家
  currentPlayer.value = initialPlayer.value
  
  // 初始化选中卡片
  selectedCard.value = null
  
  // 初始化牌
  initCards()
}

// 初始化牌
const initCards = () => {
  // 生成一副牌
  const deck = []
  for (let suit of suits) {
    for (let rank of ranks) {
      deck.push({ suit, rank })
    }
  }
  
  // 洗牌
  shuffle(deck)
  
  // 发牌
  player1Hand.value = deck.slice(0, 5)
  player2Hand.value = deck.slice(5, 10)
  tableCards.value = deck.slice(10, 15)
}

// 洗牌
const shuffle = (array) => {
  for (let i = array.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1))
    ;[array[i], array[j]] = [array[j], array[i]]
  }
}

// 获取牌符号
const getCardSymbol = (card) => {
  if (!card) {
    return ''
  }
  
  return `${card.rank}${card.suit}`
}

// 选择卡片
const selectCard = (index) => {
  if (!gameStarted.value || gamePaused.value) {
    return
  }
  
  // 检查是否是当前玩家的回合
  if (currentPlayer.value !== 1) {
    return
  }
  
  // 选中卡片
  selectedCard.value = index
}

// 检查游戏结束
const checkGameEnd = () => {
  // 检查是否有玩家的牌用完
  if (player1Hand.value.length === 0 || player2Hand.value.length === 0) {
    // 游戏结束
    gameStatus.value = '游戏结束'
    clearInterval(timer.value)
    
    // 计算得分
    if (player1Score.value > player2Score.value) {
      score.value = player1Score.value - player2Score.value
    } else if (player2Score.value > player1Score.value) {
      score.value = player2Score.value - player1Score.value
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
.poker-container {
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
  display: flex;
  flex-direction: column;
  width: 800px;
  height: 400px;
  border: 2px solid white;
  border-radius: 8px;
  overflow: hidden;
  background-color: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  padding: 20px;
  gap: 20px;
}

.player-hand {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.player-hand h3 {
  font-size: 1.2rem;
  margin: 0;
}

.hand-cards {
  flex: 1;
  display: flex;
  gap: 10px;
  justify-content: center;
  align-items: center;
}

.card {
  width: 80px;
  height: 120px;
  border: 2px solid white;
  border-radius: 8px;
  background-color: rgba(255, 255, 255, 0.2);
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
}

.card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.3);
}

.card.selected {
  background-color: rgba(255, 255, 0, 0.2);
  border-color: yellow;
}

.game-controls {
  display: flex;
  gap: 10px;
}

.game-instructions {
  font-size: 1.1rem;
}
</style>