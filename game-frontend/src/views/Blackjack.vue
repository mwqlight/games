<template>
  <div class="blackjack-container">
    <header class="game-header">
      <h1>二十一点游戏</h1>
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
            <span>玩家分数: {{ playerScore }}</span>
          </div>
          <div class="stat-item">
            <span>庄家分数: {{ dealerScore }}</span>
          </div>
          <div class="stat-item">
            <span>当前玩家: {{ currentPlayer === 1 ? '玩家' : '庄家' }}</span>
          </div>
        </div>
      </div>
      <div class="game-board">
        <div class="player-hand">
          <h3>玩家的牌</h3>
          <div class="hand-cards">
            <div 
              class="card" 
              v-for="(card, index) in playerHand" 
              :key="index"
            >
              <span>{{ getCardSymbol(card) }}</span>
            </div>
          </div>
        </div>
        <div class="dealer-hand">
          <h3>庄家的牌</h3>
          <div class="hand-cards">
            <div 
              class="card" 
              v-for="(card, index) in dealerHand" 
              :key="index"
              :class="{
                'hidden': index === 0 && gameStarted.value && currentPlayer.value === 1
              }"
            >
              <span>{{ index === 0 && gameStarted.value && currentPlayer.value === 1 ? '🂠' : getCardSymbol(card) }}</span>
            </div>
          </div>
        </div>
      </div>
      <div class="game-controls">
        <el-button type="primary" @click="startGame">开始游戏</el-button>
        <el-button type="warning" @click="pauseGame">暂停游戏</el-button>
        <el-button type="danger" @click="stopGame">停止游戏</el-button>
        <el-button type="success" @click="hit">要牌</el-button>
        <el-button type="info" @click="stand">停牌</el-button>
      </div>
      <div class="game-instructions">
        <p>点击要牌或停牌，按照二十一点规则进行游戏</p>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 初始化游戏状态
const playerHand = ref([])
const dealerHand = ref([])
const gameStatus = ref('准备开始')
const playerScore = ref(0)
const dealerScore = ref(0)
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
  playerScore.value = 0
  dealerScore.value = 0
  
  // 初始化当前玩家
  currentPlayer.value = initialPlayer.value
  
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
  playerHand.value = deck.slice(0, 2)
  dealerHand.value = deck.slice(2, 4)
  
  // 计算分数
  calculateScores()
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

// 计算分数
const calculateScores = () => {
  playerScore.value = calculateHandScore(playerHand.value)
  dealerScore.value = calculateHandScore(dealerHand.value)
}

// 计算手牌分数
const calculateHandScore = (hand) => {
  let score = 0
  let aceCount = 0
  
  for (let card of hand) {
    if (card.rank === 'A') {
      aceCount++
      score += 11
    } else if (['K', 'Q', 'J'].includes(card.rank)) {
      score += 10
    } else {
      score += parseInt(card.rank)
    }
  }
  
  // 调整A的分数
  while (score > 21 && aceCount > 0) {
    score -= 10
    aceCount--
  }
  
  return score
}

// 要牌
const hit = () => {
  if (!gameStarted.value || gamePaused.value || currentPlayer.value !== 1) {
    return
  }
  
  // 生成一副新牌
  const deck = []
  for (let suit of suits) {
    for (let rank of ranks) {
      deck.push({ suit, rank })
    }
  }
  
  // 洗牌
  shuffle(deck)
  
  // 发一张牌给玩家
  playerHand.value.push(deck[0])
  
  // 计算分数
  calculateScores()
  
  // 检查是否爆牌
  if (playerScore.value > 21) {
    gameStatus.value = '玩家爆牌，庄家赢'
    gameStarted.value = false
    clearInterval(timer.value)
  }
}

// 停牌
const stand = () => {
  if (!gameStarted.value || gamePaused.value || currentPlayer.value !== 1) {
    return
  }
  
  // 切换到庄家回合
  currentPlayer.value = 2
  
  // 庄家要牌直到分数达到17
  while (dealerScore.value < 17) {
    // 生成一副新牌
    const deck = []
    for (let suit of suits) {
      for (let rank of ranks) {
        deck.push({ suit, rank })
      }
    }
    
    // 洗牌
    shuffle(deck)
    
    // 发一张牌给庄家
    dealerHand.value.push(deck[0])
    
    // 计算分数
    calculateScores()
  }
  
  // 检查游戏结果
  checkGameResult()
}

// 检查游戏结果
const checkGameResult = () => {
  if (dealerScore.value > 21) {
    gameStatus.value = '庄家爆牌，玩家赢'
    playerScore.value += 10
  } else if (playerScore.value > dealerScore.value) {
    gameStatus.value = '玩家赢'
    playerScore.value += 10
  } else if (playerScore.value < dealerScore.value) {
    gameStatus.value = '庄家赢'
    dealerScore.value += 10
  } else {
    gameStatus.value = '平局'
  }
  
  gameStarted.value = false
  clearInterval(timer.value)
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
.blackjack-container {
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

.player-hand, .dealer-hand {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.player-hand h3, .dealer-hand h3 {
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

.card.hidden {
  background-color: rgba(0, 0, 0, 0.2);
}

.game-controls {
  display: flex;
  gap: 10px;
}

.game-instructions {
  font-size: 1.1rem;
}
</style>