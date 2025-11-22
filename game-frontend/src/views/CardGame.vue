<template>
  <div class="card-game-container">
  <header class="game-header">
    <h1>抽乌龟游戏</h1>
    <div class="game-info">
      <el-button type="primary" @click="goBack">返回主页</el-button>
      <el-button type="success" @click="startNewGame">开始新游戏</el-button>
    </div>
  </header>
  <div v-if="showStartHint" class="start-hint">
    <div class="hint-content">
      <h3>游戏开始！</h3>
      <p>点击AI牌堆中的任意一张牌开始游戏</p>
    </div>
  </div>
  <main class="game-main">
      <div class="game-status">
        <h2 :class="{ 'player-turn': gameState?.currentTurn === 'PLAYER', 'ai-turn': gameState?.currentTurn === 'AI' }">
          {{ gameState?.currentTurn === 'PLAYER' ? '玩家回合' : 'AI回合' }}
          <span class="turn-indicator"></span>
        </h2>
        <p>{{ gameStatus }}</p>
        <el-dialog v-model="isGameOverDialogVisible" title="游戏结束" width="30%">
          <span>{{ gameState?.winner }}获胜！</span>
          <template #footer>
            <span class="dialog-footer">
              <el-button type="primary" @click="startNewGame">再来一局</el-button>
            </span>
          </template>
        </el-dialog>
      </div>
      <div class="game-board">
        <div class="player-area ai-area">
          <h3>AI</h3>
          <div class="hand-cards">
            <div 
              class="card" 
              v-for="(card, index) in gameState?.aiCards" 
              :key="index"
              :class="{ 'card-back': !card.isFaceUp, 'card-animating': isAnimating && drawnCardIndex === index }"
              @click="drawFromAI(index)"
              :disabled="gameState?.currentTurn !== 'PLAYER' || gameState?.gameStatus === 'FINISHED'"
            >
              <div v-if="card.isFaceUp" class="card-content">
                <div class="card-value">{{ getRankSymbol(card.rank) }}</div>
                <div class="card-suit">{{ getSuitSymbol(card.suit) }}</div>
              </div>
              <div v-else class="card-back-content">
                <div class="card-back-pattern"></div>
              </div>
            </div>
          </div>
          <div class="hand-count">手牌数量: {{ gameState?.aiCards?.length }}</div>
        </div>
        <div class="table-area">
          <h3>游戏信息</h3>
          <div class="game-info-panel">
            <p>当前回合: {{ gameState?.currentTurn === 'PLAYER' ? '玩家' : 'AI' }}</p>
            <p>玩家手牌: {{ gameState?.playerCards?.length }}张</p>
            <p>AI手牌: {{ gameState?.aiCards?.length }}张</p>
          </div>
        </div>
        <div class="player-area">
          <h3>玩家</h3>
          <div class="hand-cards">
            <div 
              class="card" 
              v-for="(card, index) in gameState?.playerCards" 
              :key="index"
              :class="{ 'selected': selectedCardIndex === index, 'card-removing': isCardBeingRemoved(card), 'card-ai-drawn': isAnimating && aiDrawnCardIndex === index }"
              @click="selectCard(index)"
            >
              <div class="card-content">
                <div class="card-value">{{ getRankSymbol(card.rank) }}</div>
                <div class="card-suit">{{ getSuitSymbol(card.suit) }}</div>
              </div>
            </div>
          </div>
          <div class="hand-count">手牌数量: {{ gameState?.playerCards?.length }}</div>
        </div>
      </div>
      <div class="game-controls">
        <el-button 
          type="primary" 
          @click="playerDraw" 
          :disabled="gameState?.currentTurn !== 'PLAYER' || gameState?.gameStatus === 'FINISHED'"
        >
          从AI手牌中抽牌
        </el-button>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import { ElMessage } from 'element-plus';

const router = useRouter()

// 初始化游戏状态
const gameState = reactive({
  playerCards: [],
  aiCards: [],
  currentTurn: 'PLAYER',
  gameStatus: 'IN_PROGRESS',
  turtleCard: null,
  winner: null
})

const gameStatus = ref('点击"开始新游戏"开始游戏')
const selectedCardIndex = ref(null)
const isGameOverDialogVisible = ref(false)
const isAnimating = ref(false)
const drawnCardIndex = ref(null)
const cardsToRemove = ref([])
const aiDrawnCardIndex = ref(null)
const showStartHint = ref(true)

onMounted(() => {
  setTimeout(() => {
    showStartHint.value = false;
  }, 3000);
});

// 开始新游戏
const startNewGame = async () => {
  try {
    const response = await axios.post('/api/game/start')
    Object.assign(gameState, response.data)
    gameStatus.value = '游戏开始，玩家回合'
    selectedCardIndex.value = null
    isGameOverDialogVisible.value = false
  } catch (error) {
    console.error('开始游戏失败:', error)
    gameStatus.value = '开始游戏失败，请重试'
  }
}

// 玩家抽牌
const playerDraw = async () => {
  try {
    gameStatus.value = '玩家正在抽牌...'
    const response = await axios.post('/api/game/player/draw')
    Object.assign(gameState, response.data)
    
    if (gameState.gameStatus === 'FINISHED') {
      gameStatus.value = '游戏结束'
      isGameOverDialogVisible.value = true
    } else if (gameState.currentTurn === 'AI') {
      gameStatus.value = 'AI正在抽牌...'
      // 模拟AI思考时间
      setTimeout(async () => {
        // AI抽牌由后端自动处理，这里只需要获取最新状态
        const stateResponse = await axios.get('/api/game/state')
        Object.assign(gameState, stateResponse.data)
        
        if (gameState.gameStatus === 'FINISHED') {
          gameStatus.value = '游戏结束'
          isGameOverDialogVisible.value = true
        } else {
          gameStatus.value = '玩家回合'
        }
      }, 1000)
    }
  } catch (error) {
    console.error('抽牌失败:', error)
    gameStatus.value = '抽牌失败，请重试'
  }
}

// 从AI手牌中指定位置抽牌
const drawFromAI = async (index) => {
  if (gameState.currentTurn !== 'PLAYER' || gameState.gameStatus === 'FINISHED') {
    return
  }
  
  try {
    gameStatus.value = '玩家正在抽牌...'
    isAnimating.value = true
    drawnCardIndex.value = index
    
    // 等待动画结束后再发送请求
    setTimeout(async () => {
      const oldPlayerCards = [...gameState.playerCards]
      const response = await axios.post(`/api/game/player/draw/${index}`)
      
      // 找出被消除的牌
      const newPlayerCards = response.data.playerCards
      const removedCards = oldPlayerCards.filter(card => !newPlayerCards.some(newCard => newCard.suit === card.suit && newCard.rank === card.rank))
      
      if (removedCards.length > 0) {
        // 显示消除动画
        cardsToRemove.value = removedCards.map(card => ({ ...card, timestamp: Date.now() }))
        setTimeout(() => {
          Object.assign(gameState, response.data)
          cardsToRemove.value = []
        }, 500)
      } else {
        Object.assign(gameState, response.data)
      }
      
      if (gameState.gameStatus === 'FINISHED') {
        gameStatus.value = '游戏结束'
        isGameOverDialogVisible.value = true
      } else if (gameState.currentTurn === 'AI') {
        gameStatus.value = 'AI正在抽牌...'
        // 模拟AI思考时间
        setTimeout(async () => {
          // 显示AI抽牌动画
          const playerCardCount = gameState.playerCards.length
          if (playerCardCount > 0) {
            aiDrawnCardIndex.value = Math.floor(Math.random() * playerCardCount)
            isAnimating.value = true
            
            setTimeout(async () => {
              // AI抽牌由后端自动处理，这里只需要获取最新状态
              const stateResponse = await axios.get('/api/game/state')
              Object.assign(gameState, stateResponse.data)
              
              if (gameState.gameStatus === 'FINISHED') {
                gameStatus.value = '游戏结束'
                isGameOverDialogVisible.value = true
              } else {
                gameStatus.value = '玩家回合'
              }
              
              isAnimating.value = false
              aiDrawnCardIndex.value = null
            }, 500)
          } else {
            // 玩家手牌为空，AI获胜
            const stateResponse = await axios.get('/api/game/state')
            Object.assign(gameState, stateResponse.data)
            
            if (gameState.gameStatus === 'FINISHED') {
              gameStatus.value = '游戏结束'
              isGameOverDialogVisible.value = true
            } else {
              gameStatus.value = '玩家回合'
            }
          }
        }, 1000)
      }
      
      isAnimating.value = false
      drawnCardIndex.value = null
    }, 500)
  } catch (error) {
    console.error('抽牌失败:', error)
    gameStatus.value = '抽牌失败，请重试'
    isAnimating.value = false
    drawnCardIndex.value = null
  }
}

// 选中牌
const selectCard = (index) => {
  // 检查是否是玩家回合
  if (gameState.currentTurn !== 'PLAYER') {
    return
  }
  
  // 检查是否已经选中了牌
  if (selectedCardIndex.value === index) {
    selectedCardIndex.value = null
  } else {
    selectedCardIndex.value = index
  }
}

// 获取花色符号
const getSuitSymbol = (suit) => {
  switch (suit) {
    case 'HEARTS': return '♥'
    case 'DIAMONDS': return '♦'
    case 'CLUBS': return '♣'
    case 'SPADES': return '♠'
    default: return ''
  }
}

// 获取点数符号
const getRankSymbol = (rank) => {
  switch (rank) {
    case 'ACE': return 'A'
    case 'TWO': return '2'
    case 'THREE': return '3'
    case 'FOUR': return '4'
    case 'FIVE': return '5'
    case 'SIX': return '6'
    case 'SEVEN': return '7'
    case 'EIGHT': return '8'
    case 'NINE': return '9'
    case 'TEN': return '10'
    case 'JACK': return 'J'
    case 'QUEEN': return 'Q'
    case 'KING': return 'K'
    default: return ''
  }
}

// 检查牌是否正在被消除
const isCardBeingRemoved = (card) => {
  return cardsToRemove.value.some(removedCard => 
    removedCard.suit === card.suit && removedCard.rank === card.rank
  )
}

// 返回主页
const goBack = () => {
  router.push('/')
}

// 页面加载时初始化游戏
onMounted(() => {
  startNewGame()
})
</script>

<style scoped>
.card-game-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  background-color: #f5f5f5;
  min-height: 100vh;
  position: relative;
}

.game-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  max-width: 1200px;
  margin-bottom: 20px;
}

.game-header h1 {
  color: #333;
  margin: 0;
}

.game-main {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  max-width: 1200px;
}

.game-status {
  text-align: center;
  margin-bottom: 20px;
}

.game-status h2 {
  color: #333;
  margin: 0 0 10px 0;
  font-size: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  animation: pulse 1s infinite;
}

.game-status h2.player-turn {
  color: #409eff;
}

.game-status h2.ai-turn {
  color: #f56c6c;
}

.turn-indicator {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  animation: blink 1s infinite;
}

.game-status h2.player-turn .turn-indicator {
  background-color: #409eff;
}

.game-status h2.ai-turn .turn-indicator {
  background-color: #f56c6c;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

@keyframes blink {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0;
  }
}

.game-status p {
  color: #666;
  margin: 0;
}

.game-board {
  display: flex;
  justify-content: space-between;
  width: 100%;
  margin-bottom: 20px;
}

.player-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 35%;
}

.ai-area {
  transform: rotate(180deg);
}

.ai-area h3 {
  transform: rotate(180deg);
}

.player-area h3 {
  color: #333;
  margin: 0 0 10px 0;
}

.hand-cards {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 10px;
  margin-bottom: 10px;
}

.card {
  width: 80px;
  height: 112px;
  border-radius: 8px;
  border: 2px solid #ccc;
  background-color: white;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
  transition: all 0.3s ease;
}

.player-area .card:hover {
  transform: translateY(-10px) scale(1.05);
  box-shadow: 0 10px 25px rgba(64, 158, 255, 0.3);
}

.ai-area .card:hover {
  transform: rotate(180deg) translateY(-10px) scale(1.05);
  box-shadow: 0 10px 25px rgba(245, 108, 108, 0.3);
}

.card.card-animating {
  animation: drawCard 0.8s cubic-bezier(0.25, 0.46, 0.45, 0.94) forwards;
  z-index: 100;
}

@keyframes drawCard {
  0% {
    transform: rotate(180deg) translateY(0) scale(1);
    opacity: 1;
  }
  25% {
    transform: rotate(180deg) translateY(-30px) scale(1.1);
    opacity: 0.9;
  }
  50% {
    transform: rotate(180deg) translateY(-60px) scale(1.2);
    opacity: 0.8;
  }
  75% {
    transform: rotate(180deg) translateY(-90px) scale(1.1);
    opacity: 0.4;
  }
  100% {
    transform: rotate(180deg) translateY(-120px) scale(0.8);
    opacity: 0;
  }
}

.card.card-removing {
  animation: removeCard 0.8s cubic-bezier(0.55, 0.055, 0.675, 0.19) forwards;
  z-index: 100;
}

@keyframes removeCard {
  0% {
    transform: scale(1) rotate(0deg);
    opacity: 1;
  }
  25% {
    transform: scale(1.3) rotate(5deg);
    opacity: 0.9;
  }
  50% {
    transform: scale(1.6) rotate(-5deg);
    opacity: 0.8;
  }
  75% {
    transform: scale(1.3) rotate(5deg);
    opacity: 0.4;
  }
  100% {
    transform: scale(0) rotate(-10deg);
    opacity: 0;
  }
}

.card.card-ai-drawn {
  animation: aiDrawCard 0.8s cubic-bezier(0.25, 0.46, 0.45, 0.94) forwards;
  z-index: 100;
}

@keyframes aiDrawCard {
  0% {
    transform: translateY(0) scale(1);
    opacity: 1;
  }
  25% {
    transform: translateY(30px) scale(1.1);
    opacity: 0.9;
  }
  50% {
    transform: translateY(60px) scale(1.2);
    opacity: 0.8;
  }
  75% {
    transform: translateY(90px) scale(1.1);
    opacity: 0.4;
  }
  100% {
    transform: translateY(120px) scale(0.8);
    opacity: 0;
  }
}

.card.selected {
  border-color: #409eff;
  box-shadow: 0 0 10px rgba(64, 158, 255, 0.5);
}

.card.card-back {
  background-color: #1976d2;
  border-color: #1565c0;
}

.card-content {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  height: 100%;
  padding: 5px;
}

.card-value {
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.card-suit {
  font-size: 24px;
  text-align: center;
}

.card-suit.HEARTS, .card-suit.DIAMONDS {
  color: #f44336;
}

.card-suit.CLUBS, .card-suit.SPADES {
  color: #333;
}

.card-back-content {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}

.card-back-pattern {
  width: 60px;
  height: 60px;
  background-color: white;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.card-back-pattern::before {
  content: '♠♥♦♣';
  font-size: 20px;
  color: #1976d2;
}

.hand-count {
  color: #666;
  font-size: 14px;
}

.table-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 25%;
}

.table-area h3 {
  color: #333;
  margin: 0 0 10px 0;
}

.game-info-panel {
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.game-info-panel p {
  color: #333;
  margin: 5px 0;
}

.game-controls {
  display: flex;
  gap: 10px;
}
.start-hint {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeOut 3s ease forwards;
}

.hint-content {
  background-color: white;
  padding: 40px;
  border-radius: 15px;
  text-align: center;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
  animation: bounceIn 1s ease;
}

.hint-content h3 {
  color: #333;
  margin-bottom: 15px;
  font-size: 28px;
}

.hint-content p {
  color: #666;
  font-size: 18px;
}

@keyframes fadeOut {
  0% {
    opacity: 1;
  }
  80% {
    opacity: 1;
  }
  100% {
    opacity: 0;
    pointer-events: none;
  }
}

@keyframes bounceIn {
  0% {
    transform: scale(0.3);
    opacity: 0;
  }
  50% {
    transform: scale(1.05);
    opacity: 1;
  }
  70% {
    transform: scale(0.9);
  }
  100% {
    transform: scale(1);
  }
}
</style>
