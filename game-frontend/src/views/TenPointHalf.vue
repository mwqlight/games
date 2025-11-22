<template>
  <div class="ten-point-half-container">
    <header class="game-header">
      <h1>十点半游戏</h1>
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
            <span>玩家点数: {{ playerScore }}</span>
          </div>
          <div class="stat-item">
            <span>庄家点数: {{ dealerScore }}</span>
          </div>
          <div class="stat-item">
            <span>当前回合: {{ currentTurn === 'player' ? '玩家' : currentTurn === 'dealer' ? '庄家' : '游戏结束' }}</span>
          </div>
        </div>
      </div>
      <div class="game-board">
        <div class="player-hand">
          <h3>玩家的牌</h3>
          <div class="hand-cards">
            <div 
              class="card" 
              v-for="(card, index) in playerCards" 
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
              v-for="(card, index) in dealerCards" 
              :key="index"
              :class="{
                'hidden': index === 0 && currentTurn === 'player'
              }"
            >
              <span>{{ index === 0 && currentTurn === 'player' ? '🂠' : getCardSymbol(card) }}</span>
            </div>
          </div>
        </div>
      </div>
      <div class="game-controls">
        <el-button type="primary" @click="startGame" :disabled="gameStarted">开始游戏</el-button>
        <el-button type="success" @click="hit" :disabled="!gameStarted || currentTurn !== 'player' || playerCards.length >= 5">要牌</el-button>
        <el-button type="info" @click="stand" :disabled="!gameStarted || currentTurn !== 'player'">停牌</el-button>
      </div>
      <div class="game-instructions">
        <p>目标：手牌点数之和尽量接近但不超过10.30</p>
        <p>点数计算：A计1点，数字牌按牌面，J/Q/K计0.5点</p>
        <p>特殊牌型：五小(5张<10.30,3倍)、人五小(5张人牌,4倍)、天王(5张=10.30,5倍)</p>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import axios from '../utils/axios'
import { ElMessage } from 'element-plus'

const router = useRouter()

// 初始化游戏状态
const playerCards = ref([])
const dealerCards = ref([])
const gameStatus = ref('准备开始')
const currentTurn = ref('')
const gameStarted = ref(false)
const playerBusted = ref(false)
const dealerBusted = ref(false)
const payoutMultiplier = ref(0.0)

// 计算玩家和庄家的点数
const playerScore = computed(() => {
  return calculateScore(playerCards.value)
})

const dealerScore = computed(() => {
  return calculateScore(dealerCards.value)
})

// 计算手牌点数
const calculateScore = (cards) => {
  if (!cards || cards.length === 0) return 0.0
  let score = 0.0
  cards.forEach(card => {
    switch (card.type) {
      case 'ACE':
        score += 1.0
        break
      case 'NUMBER':
        score += parseFloat(card.rank)
        break
      case 'FACE':
        score += 0.5
        break
    }
  })
  return parseFloat(score.toFixed(2))
}

// 获取牌的符号
const getCardSymbol = (card) => {
  if (!card) return ''
  return card.suit + card.rank
}

// 开始游戏
const startGame = async () => {
  try {
    const response = await axios.post('/api/game/start')
    const gameStatusData = response.data
    playerCards.value = gameStatusData.playerCards
    dealerCards.value = gameStatusData.dealerCards
    currentTurn.value = gameStatusData.currentTurn
    gameStatus.value = gameStatusData.result
    gameStarted.value = true
    playerBusted.value = gameStatusData.playerBusted
    dealerBusted.value = gameStatusData.dealerBusted
    payoutMultiplier.value = gameStatusData.payoutMultiplier
  } catch (error) {
    console.error('开始游戏失败:', error)
    gameStatus.value = '开始游戏失败，请重试'
  }
}

// 玩家要牌
const hit = async () => {
  try {
    const response = await axios.post('/api/game/hit')
    const gameStatusData = response.data
    playerCards.value = gameStatusData.playerCards
    dealerCards.value = gameStatusData.dealerCards
    currentTurn.value = gameStatusData.currentTurn
    gameStatus.value = gameStatusData.result
    playerBusted.value = gameStatusData.playerBusted
    dealerBusted.value = gameStatusData.dealerBusted
    payoutMultiplier.value = gameStatusData.payoutMultiplier
    
    if (currentTurn.value === 'gameOver') {
      gameStarted.value = false
      showResultMessage()
    }
  } catch (error) {
    console.error('要牌失败:', error)
    gameStatus.value = '要牌失败，请重试'
  }
}

// 玩家停牌
const stand = async () => {
  try {
    const response = await axios.post('/api/game/stand')
    const gameStatusData = response.data
    playerCards.value = gameStatusData.playerCards
    dealerCards.value = gameStatusData.dealerCards
    currentTurn.value = gameStatusData.currentTurn
    gameStatus.value = gameStatusData.result
    playerBusted.value = gameStatusData.playerBusted
    dealerBusted.value = gameStatusData.dealerBusted
    payoutMultiplier.value = gameStatusData.payoutMultiplier
    
    gameStarted.value = false
    showResultMessage()
  } catch (error) {
    console.error('停牌失败:', error)
    gameStatus.value = '停牌失败，请重试'
  }
}

// 重新开始游戏
const restartGame = () => {
  playerCards.value = []
  dealerCards.value = []
  gameStatus.value = '准备开始'
  currentTurn.value = ''
  gameStarted.value = false
  playerBusted.value = false
  dealerBusted.value = false
  payoutMultiplier.value = 0.0
}

// 返回主页
const goBack = () => {
  router.push('/')
}

// 显示结果消息
const showResultMessage = () => {
  let message = gameStatus.value
  if (payoutMultiplier.value > 0) {
    message += `，赔率: ${payoutMultiplier.value}倍`
  }
  
  if (gameStatus.value.includes('玩家获胜')) {
    ElMessage.success(message)
  } else if (gameStatus.value.includes('庄家获胜')) {
    ElMessage.error(message)
  } else if (gameStatus.value.includes('平局')) {
    ElMessage.info(message)
  }
}
</script>

<style scoped>
.ten-point-half-container {
  width: 100%;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  box-sizing: border-box;
}

.game-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  max-width: 1200px;
  margin-bottom: 20px;
  color: white;
}

.game-header h1 {
  font-size: 2.5rem;
  margin: 0;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
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
  width: 100%;
  max-width: 1200px;
}

.game-status {
  display: flex;
  flex-direction: column;
  gap: 10px;
  text-align: center;
  color: white;
}

.game-status h2 {
  font-size: 1.8rem;
  margin: 0;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
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
  padding: 8px 16px;
  border-radius: 20px;
  backdrop-filter: blur(10px);
}

.game-board {
  display: flex;
  gap: 40px;
  justify-content: center;
  width: 100%;
  flex-wrap: wrap;
}

.player-hand, .dealer-hand {
  display: flex;
  flex-direction: column;
  gap: 10px;
  align-items: center;
  background: rgba(255, 255, 255, 0.1);
  padding: 20px;
  border-radius: 12px;
  backdrop-filter: blur(10px);
  min-width: 300px;
}

.player-hand h3, .dealer-hand h3 {
  color: white;
  font-size: 1.3rem;
  margin: 0;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
}

.hand-cards {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: center;
}

.card {
  width: 80px;
  height: 112px;
  background: white;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  font-weight: bold;
  box-shadow: 2px 2px 8px rgba(0, 0, 0, 0.3);
  transition: transform 0.3s ease;
}

.card:hover {
  transform: translateY(-5px);
}

.card.hidden {
  background: #333;
  color: transparent;
}

.card.hidden span {
  visibility: hidden;
}

.game-controls {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: center;
}

.game-instructions {
  color: white;
  text-align: center;
  background: rgba(255, 255, 255, 0.1);
  padding: 20px;
  border-radius: 12px;
  backdrop-filter: blur(10px);
  max-width: 800px;
}

.game-instructions p {
  margin: 5px 0;
  font-size: 1.1rem;
}

@media (max-width: 768px) {
  .game-header {
    flex-direction: column;
    gap: 10px;
    text-align: center;
  }
  
  .game-header h1 {
    font-size: 2rem;
  }
  
  .game-board {
    flex-direction: column;
    gap: 20px;
  }
  
  .player-hand, .dealer-hand {
    width: 100%;
    min-width: auto;
  }
  
  .card {
    width: 60px;
    height: 84px;
    font-size: 1.2rem;
  }
}
</style>
