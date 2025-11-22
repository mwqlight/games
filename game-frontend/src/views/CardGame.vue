<template>
  <div class="card-game-container">
    <header class="game-header">
      <h1>纸牌游戏</h1>
      <div class="game-info">
        <el-button type="primary" @click="goBack">返回主页</el-button>
        <el-button type="success" @click="restartGame">重新开始</el-button>
      </div>
    </header>
    <main class="game-main">
      <div class="game-status">
        <h2>{{ currentPlayer === 'player1' ? '玩家1' : '玩家2' }}回合</h2>
        <p>{{ gameStatus }}</p>
      </div>
      <div class="game-board">
        <div class="player-area">
          <h3>玩家1</h3>
          <div class="hand-cards">
            <div 
              class="card" 
              v-for="(card, index) in player1Hand" 
              :key="index"
              :class="{ 'selected': selectedCardIndex === index }"
              @click="selectCard(index)"
            >
              <div class="card-value">{{ card.value }}</div>
              <div class="card-suit">{{ getSuitSymbol(card.suit) }}</div>
            </div>
          </div>
          <div class="player-score">得分: {{ player1Score }}</div>
        </div>
        <div class="table-area">
          <h3>桌面</h3>
          <div class="table-cards">
            <div 
              class="card" 
              v-for="(card, index) in tableCards" 
              :key="index"
            >
              <div class="card-value">{{ card.value }}</div>
              <div class="card-suit">{{ getSuitSymbol(card.suit) }}</div>
            </div>
          </div>
        </div>
        <div class="player-area">
          <h3>玩家2</h3>
          <div class="hand-cards">
            <div 
              class="card" 
              v-for="(card, index) in player2Hand" 
              :key="index"
              :class="{ 'selected': selectedCardIndex === index + player1Hand.length }"
              @click="selectCard(index + player1Hand.length)"
            >
              <div class="card-value">{{ card.value }}</div>
              <div class="card-suit">{{ getSuitSymbol(card.suit) }}</div>
            </div>
          </div>
          <div class="player-score">得分: {{ player2Score }}</div>
        </div>
      </div>
      <div class="game-controls">
        <el-button type="primary" @click="playCard" :disabled="selectedCardIndex === null">出牌</el-button>
        <el-button type="warning" @click="drawCard" :disabled="deck.length === 0">摸牌</el-button>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 初始化游戏状态
const currentPlayer = ref('player1')
const gameStatus = ref('游戏进行中')
const player1Hand = ref([])
const player2Hand = ref([])
const tableCards = ref([])
const deck = ref([])
const player1Score = ref(0)
const player2Score = ref(0)
const selectedCardIndex = ref(null)

// 初始化游戏
const initGame = () => {
  // 创建一副牌
  deck.value = createDeck()
  
  // 洗牌
  shuffleDeck(deck.value)
  
  // 发牌
  player1Hand.value = deck.value.splice(0, 5)
  player2Hand.value = deck.value.splice(0, 5)
  
  // 初始化桌面
  tableCards.value = []
  
  // 初始化得分
  player1Score.value = 0
  player2Score.value = 0
  
  // 初始化当前玩家
  currentPlayer.value = 'player1'
  
  // 初始化游戏状态
  gameStatus.value = '游戏进行中'
  
  // 初始化选中的牌
  selectedCardIndex.value = null
}

// 创建一副牌
const createDeck = () => {
  const suits = ['hearts', 'diamonds', 'clubs', 'spades']
  const values = ['A', '2', '3', '4', '5', '6', '7', '8', '9', '10', 'J', 'Q', 'K']
  const deck = []
  
  for (const suit of suits) {
    for (const value of values) {
      deck.push({ suit, value })
    }
  }
  
  return deck
}

// 洗牌
const shuffleDeck = (deck) => {
  for (let i = deck.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1))
    ;[deck[i], deck[j]] = [deck[j], deck[i]]
  }
}

// 获取花色符号
const getSuitSymbol = (suit) => {
  switch (suit) {
    case 'hearts': return '♥'
    case 'diamonds': return '♦'
    case 'clubs': return '♣'
    case 'spades': return '♠'
    default: return ''
  }
}

// 选中牌
const selectCard = (index) => {
  // 检查是否是当前玩家的回合
  if ((currentPlayer.value === 'player1' && index >= player1Hand.value.length) || 
      (currentPlayer.value === 'player2' && index < player1Hand.value.length)) {
    return
  }
  
  // 检查是否已经选中了牌
  if (selectedCardIndex.value === index) {
    selectedCardIndex.value = null
  } else {
    selectedCardIndex.value = index
  }
}

// 出牌
const playCard = () => {
  // 检查是否选中了牌
  if (selectedCardIndex.value === null) {
    return
  }
  
  // 获取当前玩家的手牌
  const currentHand = currentPlayer.value === 'player1' ? player1Hand.value : player2Hand.value
  
  // 获取选中的牌的索引
  const cardIndex = currentPlayer.value === 'player1' ? selectedCardIndex.value : selectedCardIndex.value - player1Hand.value.length
  
  // 获取选中的牌
  const card = currentHand[cardIndex]
  
  // 检查是否可以出牌
  if (!canPlayCard(card)) {
    return
  }
  
  // 将牌放到桌面
  tableCards.value.push(card)
  
  // 从手牌中移除牌
  currentHand.splice(cardIndex, 1)
  
  // 检查是否得分
  checkScore(card)
  
  // 切换玩家
  currentPlayer.value = currentPlayer.value === 'player1' ? 'player2' : 'player1'
  
  // 取消选中的牌
  selectedCardIndex.value = null
  
  // 检查游戏是否结束
  checkGameEnd()
}

// 检查是否可以出牌
const canPlayCard = (card) => {
  // 如果桌面没有牌，可以出任意牌
  if (tableCards.value.length === 0) {
    return true
  }
  
  // 获取桌面最后一张牌
  const lastCard = tableCards.value[tableCards.value.length - 1]
  
  // 检查是否是相同花色或相同点数
  return card.suit === lastCard.suit || card.value === lastCard.value
}

// 检查是否得分
const checkScore = (card) => {
  // 检查是否是J、Q、K、A
  if (['J', 'Q', 'K', 'A'].includes(card.value)) {
    // 给当前玩家加分
    if (currentPlayer.value === 'player1') {
      player1Score.value += 10
    } else {
      player2Score.value += 10
    }
  }
}

// 摸牌
const drawCard = () => {
  // 检查牌堆是否有牌
  if (deck.value.length === 0) {
    return
  }
  
  // 获取当前玩家的手牌
  const currentHand = currentPlayer.value === 'player1' ? player1Hand.value : player2Hand.value
  
  // 从牌堆摸一张牌
  currentHand.push(deck.value.pop())
  
  // 切换玩家
  currentPlayer.value = currentPlayer.value === 'player1' ? 'player2' : 'player1'
  
  // 取消选中的牌
  selectedCardIndex.value = null
  
  // 检查游戏是否结束
  checkGameEnd()
}

// 检查游戏是否结束
const checkGameEnd = () => {
  // 检查是否所有牌都已经出完
  if (player1Hand.value.length === 0 && player2Hand.value.length === 0 && deck.value.length === 0) {
    // 比较得分
    if (player1Score.value > player2Score.value) {
      gameStatus.value = '玩家1获胜'
    } else if (player2Score.value > player1Score.value) {
      gameStatus.value = '玩家2获胜'
    } else {
      gameStatus.value = '平局'
    }
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
</script>

<style scoped>
.card-game-container {
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

.game-status p {
  font-size: 1.2rem;
  margin: 0;
}

.game-board {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 50px;
  width: 100%;
  max-width: 1200px;
}

.player-area {
  display: flex;
  flex-direction: column;
  gap: 10px;
  text-align: center;
}

.player-area h3 {
  font-size: 1.2rem;
  margin: 0;
}

.hand-cards {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: center;
}

.card {
  width: 80px;
  height: 120px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
  background-color: white;
  color: black;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
  border: 2px solid transparent;
}

.card.selected {
  transform: scale(1.1);
  box-shadow: 0 0 20px rgba(255, 255, 0, 0.8);
  border-color: yellow;
}

.card:hover {
  transform: scale(1.05);
  box-shadow: 0 0 10px rgba(255, 255, 255, 0.8);
}

.card-value {
  font-size: 1.2rem;
  font-weight: bold;
}

.card-suit {
  font-size: 2rem;
}

.player-score {
  font-size: 1.1rem;
  font-weight: bold;
}

.table-area {
  display: flex;
  flex-direction: column;
  gap: 10px;
  text-align: center;
}

.table-area h3 {
  font-size: 1.2rem;
  margin: 0;
}

.table-cards {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: center;
}

.game-controls {
  display: flex;
  gap: 10px;
}
</style>