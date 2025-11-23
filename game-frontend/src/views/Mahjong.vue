<template>
  <div class="mahjong-container">
    <header class="game-header">
      <h1>二人麻将</h1>
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
            <span>{{ playerName }}: {{ playerScore }}</span>
          </div>
          <div class="stat-item">
            <span>电脑: {{ aiScore }}</span>
          </div>
          <div class="stat-item">
            <span>当前回合: {{ currentTurn === 'player1' ? playerName : '电脑' }}</span>
          </div>
        </div>
      </div>
      <div class="game-table">
        <!-- 对手手牌区域 -->
        <div class="opponent-hand">
          <h3>电脑的牌</h3>
          <div class="hand-tiles">
            <div 
              class="tile opponent-tile"
              v-for="(tile, index) in opponentHandCount"
              :key="index"
            ></div>
          </div>
          <div class="exposed-tiles" v-if="aiExposedTiles.length > 0">
            <h4>电脑明牌</h4>
            <div class="tile-row">
              <div 
                class="tile exposed-tile"
                v-for="(tile, index) in aiExposedTiles"
                :key="index"
              >
                {{ tileText(tile) }}
              </div>
            </div>
          </div>
        </div>

        <!-- 牌池区域 -->
        <div class="discard-pool">
          <h3>牌池</h3>
          <div class="pool-tiles">
            <div 
              class="tile pool-tile"
              v-for="(tile, index) in discardPool"
              :key="index"
            >
              {{ tileText(tile) }}
            </div>
          </div>
        </div>

        <!-- 玩家手牌区域 -->
        <div class="player-hand">
          <h3>{{ playerName }}的牌</h3>
          <div class="hand-tiles">
            <div 
              class="tile player-tile"
              v-for="(tile, index) in playerHand"
              :key="index"
              :class="{ 'selected': selectedTileIndex === index }"
              @click="selectTile(index)"
            >
              {{ tileText(tile) }}
            </div>
          </div>
          <div class="exposed-tiles" v-if="playerExposedTiles.length > 0">
            <h4>你的明牌</h4>
            <div class="tile-row">
              <div 
                class="tile exposed-tile"
                v-for="(tile, index) in playerExposedTiles"
                :key="index"
              >
                {{ tileText(tile) }}
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="game-controls">
        <el-button 
          type="primary" 
          @click="chupai" 
          :disabled="selectedTileIndex === null || currentTurn !== 'player1'"
        >
          出牌
        </el-button>
        <el-button 
          type="success" 
          @click="chi" 
          :disabled="!canChi || currentTurn !== 'player1'"
        >
          吃
        </el-button>
        <el-button 
          type="warning" 
          @click="peng" 
          :disabled="!canPeng || currentTurn !== 'player1'"
        >
          碰
        </el-button>
        <el-button 
          type="danger" 
          @click="gang" 
          :disabled="!canGang || currentTurn !== 'player1'"
        >
          杠
        </el-button>
        <el-button 
          type="info" 
          @click="hu" 
          :disabled="!canHu || currentTurn !== 'player1'"
        >
          胡
        </el-button>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const router = useRouter()

// 游戏状态
const gameId = ref(null)
const playerName = ref('玩家')
const playerScore = ref(0)
const aiScore = ref(0)
const playerHand = ref([])
const opponentHandCount = ref(0)
const playerExposedTiles = ref([])
const aiExposedTiles = ref([])
const discardPool = ref([])
const currentTurn = ref('player1')
const gameStatus = ref('准备开始')
const selectedTileIndex = ref(null)
const canChi = ref(false)
const canPeng = ref(false)
const canGang = ref(false)
const canHu = ref(false)

// 初始化游戏
const initGame = async () => {
  try {
    const response = await axios.post('/api/mahjong/start', null, {
      params: { playerName: playerName.value }
    })
    const game = response.data
    updateGameState(game)
    gameStatus.value = '游戏进行中'
  } catch (error) {
    ElMessage.error('游戏初始化失败')
    console.error('游戏初始化失败:', error)
  }
}

// 更新游戏状态
const updateGameState = (game) => {
  gameId.value = game.gameId
  playerHand.value = game.player1.handTiles
  opponentHandCount.value = game.player2.handTiles.length
  playerExposedTiles.value = game.player1.exposedTiles
  aiExposedTiles.value = game.player2.exposedTiles
  discardPool.value = game.discardPool
  currentTurn.value = game.currentTurnPlayer.id
  playerScore.value = game.player1.score
  aiScore.value = game.player2.score
  gameStatus.value = game.gameStatus === 'PLAYING' ? '游戏进行中' : 
                    game.gameStatus === 'HU' ? '游戏结束' : '流局'

  // 检查可执行的操作
  checkAvailableActions(game)
}

// 检查可执行的操作
const checkAvailableActions = (game) => {
  canChi.value = false
  canPeng.value = false
  canGang.value = false
  canHu.value = false

  if (game.gameStatus !== 'PLAYING' || game.currentTurnPlayer.id !== 'player1') {
    return
  }

  // 简单实现：这里应该根据游戏规则检查可执行的操作
  // 实际需要调用后端接口或在前端实现复杂的规则判断
  canHu.value = true // 暂时假设总是可以胡牌
}

// 选择牌
const selectTile = (index) => {
  if (currentTurn.value !== 'player1') {
    ElMessage.warning('不是你的回合')
    return
  }

  if (selectedTileIndex.value === index) {
    selectedTileIndex.value = null
  } else {
    selectedTileIndex.value = index
  }
}

// 出牌
const chupai = async () => {
  if (selectedTileIndex.value === null) {
    ElMessage.warning('请选择要出的牌')
    return
  }

  try {
    const tile = playerHand.value[selectedTileIndex.value]
    const response = await axios.post(`/api/mahjong/${gameId.value}/action`, {
      actionType: 'CHUPAI',
      tile: tile
    })
    updateGameState(response.data)
    selectedTileIndex.value = null

    if (response.data.gameStatus === 'HU') {
      ElMessage.success('你胡牌了！')
    }
  } catch (error) {
    ElMessage.error('出牌失败')
    console.error('出牌失败:', error)
  }
}

// 吃牌
const chi = async () => {
  try {
    const lastTile = discardPool.value[discardPool.value.length - 1]
    const response = await axios.post(`/api/mahjong/${gameId.value}/action`, {
      actionType: 'CHI',
      tile: lastTile
    })
    updateGameState(response.data)
    ElMessage.success('吃牌成功')
  } catch (error) {
    ElMessage.error('吃牌失败')
    console.error('吃牌失败:', error)
  }
}

// 碰牌
const peng = async () => {
  try {
    const lastTile = discardPool.value[discardPool.value.length - 1]
    const response = await axios.post(`/api/mahjong/${gameId.value}/action`, {
      actionType: 'PENG',
      tile: lastTile
    })
    updateGameState(response.data)
    ElMessage.success('碰牌成功')
  } catch (error) {
    ElMessage.error('碰牌失败')
    console.error('碰牌失败:', error)
  }
}

// 杠牌
const gang = async () => {
  try {
    // 找到玩家手牌中可以杠的牌
    const gangTile = findGangTile()
    if (!gangTile) {
      ElMessage.warning('没有可以杠的牌')
      return
    }

    const response = await axios.post(`/api/mahjong/${gameId.value}/action`, {
      actionType: 'GANG',
      tile: gangTile
    })
    updateGameState(response.data)
    ElMessage.success('杠牌成功')
  } catch (error) {
    ElMessage.error('杠牌失败')
    console.error('杠牌失败:', error)
  }
}

// 找到可以杠的牌
const findGangTile = () => {
  const countMap = new Map()
  for (const tile of playerHand.value) {
    const key = `${tile.type}-${tile.value}`
    countMap.set(key, (countMap.get(key) || 0) + 1)
  }

  for (const [key, count] of countMap.entries()) {
    if (count >= 3) {
      const [type, value] = key.split('-')
      return { type, value: parseInt(value) }
    }
  }

  return null
}

// 胡牌
const hu = async () => {
  try {
    const lastTile = discardPool.value.length > 0 ? 
      discardPool.value[discardPool.value.length - 1] : null
    
    const response = await axios.post(`/api/mahjong/${gameId.value}/action`, {
      actionType: 'HU',
      tile: lastTile
    })
    updateGameState(response.data)
    ElMessage.success('胡牌成功！')
  } catch (error) {
    ElMessage.error('胡牌失败')
    console.error('胡牌失败:', error)
  }
}

// 重新开始游戏
const restartGame = () => {
  selectedTileIndex.value = null
  initGame()
}

// 返回主页
const goBack = () => {
  router.push('/')
}

// 牌面文本
const tileText = (tile) => {
  if (!tile) return ''
  
  switch (tile.type) {
    case 'WAN':
      return tile.value + '万'
    case 'FENG':
      switch (tile.value) {
        case 1: return '东'
        case 2: return '南'
        case 3: return '西'
        case 4: return '北'
        default: return '风'
      }
    case 'JIAN':
      switch (tile.value) {
        case 1: return '中'
        case 2: return '发'
        case 3: return '白'
        default: return '箭'
      }
    case 'HUA':
      switch (tile.value) {
        case 1: return '春'
        case 2: return '夏'
        case 3: return '秋'
        case 4: return '冬'
        case 5: return '梅'
        case 6: return '兰'
        case 7: return '竹'
        case 8: return '菊'
        default: return '花'
      }
    default:
      return ''
  }
}

// 组件挂载时初始化游戏
onMounted(() => {
  initGame()
})
</script>

<style scoped>
.mahjong-container {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #8B4513 0%, #D2B48C 50%, #8B4513 100%);
  color: white;
}

.game-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(10px);
}

.game-header h1 {
  font-size: 2rem;
  margin: 0;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.5);
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
  padding: 20px;
}

.game-status {
  display: flex;
  flex-direction: column;
  gap: 10px;
  text-align: center;
  background: rgba(0, 0, 0, 0.1);
  padding: 15px;
  border-radius: 8px;
  backdrop-filter: blur(10px);
}

.game-status h2 {
  font-size: 1.5rem;
  margin: 0;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.5);
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
  padding: 5px 15px;
  border-radius: 20px;
}

.game-table {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 30px;
  background: rgba(0, 0, 0, 0.1);
  padding: 20px;
  border-radius: 15px;
  backdrop-filter: blur(10px);
  min-width: 800px;
  max-width: 1200px;
  width: 100%;
}

.opponent-hand, .player-hand {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  width: 100%;
}

.opponent-hand h3, .player-hand h3 {
  margin: 0;
  font-size: 1.2rem;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.5);
}

.hand-tiles {
  display: flex;
  gap: 5px;
  flex-wrap: wrap;
  justify-content: center;
  padding: 10px;
  background: rgba(0, 0, 0, 0.1);
  border-radius: 10px;
  min-height: 100px;
  align-items: center;
}

.tile {
  width: 60px;
  height: 84px;
  border-radius: 5px;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 1.2rem;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.opponent-tile {
  background: #8B4513;
  border-color: #654321;
}

.player-tile {
  background: white;
  color: #8B4513;
  border-color: #D2B48C;
}

.player-tile:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.3);
}

.player-tile.selected {
  border-color: #FF6B35;
  transform: scale(1.1);
  box-shadow: 0 0 20px rgba(255, 107, 53, 0.5);
}

.exposed-tiles {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
  width: 100%;
}

.exposed-tiles h4 {
  margin: 0;
  font-size: 1rem;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.5);
}

.tile-row {
  display: flex;
  gap: 5px;
  flex-wrap: wrap;
  justify-content: center;
  padding: 5px;
  background: rgba(0, 0, 0, 0.1);
  border-radius: 8px;
}

.exposed-tile {
  background: #FFEBCD;
  color: #8B4513;
  border-color: #DEB887;
}

.discard-pool {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  width: 100%;
}

.discard-pool h3 {
  margin: 0;
  font-size: 1.2rem;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.5);
}

.pool-tiles {
  display: flex;
  gap: 3px;
  flex-wrap: wrap;
  justify-content: center;
  padding: 10px;
  background: rgba(0, 0, 0, 0.1);
  border-radius: 10px;
  max-height: 200px;
  overflow-y: auto;
  width: 100%;
}

.pool-tile {
  background: #F5DEB3;
  color: #8B4513;
  border-color: #DEB887;
  width: 50px;
  height: 70px;
  font-size: 1rem;
}

.game-controls {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: center;
  padding: 10px;
  background: rgba(0, 0, 0, 0.1);
  border-radius: 10px;
  backdrop-filter: blur(10px);
}

/* 响应式设计 */
@media (max-width: 900px) {
  .game-table {
    min-width: 100%;
  }
  
  .tile {
    width: 50px;
    height: 70px;
    font-size: 1rem;
  }
  
  .pool-tile {
    width: 40px;
    height: 56px;
    font-size: 0.8rem;
  }
}
</style>