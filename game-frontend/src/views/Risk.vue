<template>
  <div class="risk-container">
    <header class="game-header">
      <h1>Risk</h1>
      <div class="game-info">
        <span class="current-player">当前玩家: {{ currentPlayer.name }} ({{ currentPlayer.armies }}军队)</span>
        <span class="phase">阶段: {{ currentPhase }}</span>
        <el-button type="primary" @click="nextPhase" :disabled="!canNextPhase">下一阶段</el-button>
        <el-button type="success" @click="newGame">新游戏</el-button>
      </div>
    </header>
    <main class="game-main">
      <div class="game-board">
        <!-- 游戏棋盘 -->
        <svg width="800" height="600" viewBox="0 0 800 600" xmlns="http://www.w3.org/2000/svg">
          <!-- 绘制地图 -->
          <g id="map">
            <!-- 北美 -->
            <path d="M 100 100 L 200 100 L 250 150 L 200 200 L 150 250 L 100 200 Z" 
                  :fill="getTerritoryColor('Alaska')" 
                  :stroke="getTerritoryBorderColor('Alaska')"
                  stroke-width="2"
                  @click="selectTerritory('Alaska')"/>
            <path d="M 200 100 L 300 100 L 350 150 L 300 200 L 250 150 Z" 
                  :fill="getTerritoryColor('Canada')" 
                  :stroke="getTerritoryBorderColor('Canada')"
                  stroke-width="2"
                  @click="selectTerritory('Canada')"/>
            <path d="M 250 150 L 300 200 L 350 250 L 300 300 L 200 250 Z" 
                  :fill="getTerritoryColor('United States')" 
                  :stroke="getTerritoryBorderColor('United States')"
                  stroke-width="2"
                  @click="selectTerritory('United States')"/>
            <!-- 南美 -->
            <path d="M 200 300 L 300 300 L 350 350 L 300 400 L 200 400 Z" 
                  :fill="getTerritoryColor('Brazil')" 
                  :stroke="getTerritoryBorderColor('Brazil')"
                  stroke-width="2"
                  @click="selectTerritory('Brazil')"/>
            <path d="M 150 350 L 200 300 L 200 400 L 100 400 Z" 
                  :fill="getTerritoryColor('Argentina')" 
                  :stroke="getTerritoryBorderColor('Argentina')"
                  stroke-width="2"
                  @click="selectTerritory('Argentina')"/>
            <!-- 欧洲 -->
            <path d="M 400 100 L 500 100 L 550 150 L 500 200 L 450 150 Z" 
                  :fill="getTerritoryColor('United Kingdom')" 
                  :stroke="getTerritoryBorderColor('United Kingdom')"
                  stroke-width="2"
                  @click="selectTerritory('United Kingdom')"/>
            <path d="M 500 100 L 600 100 L 650 150 L 600 200 L 550 150 Z" 
                  :fill="getTerritoryColor('France')" 
                  :stroke="getTerritoryBorderColor('France')"
                  stroke-width="2"
                  @click="selectTerritory('France')"/>
            <path d="M 550 150 L 600 200 L 650 250 L 600 300 L 500 250 Z" 
                  :fill="getTerritoryColor('Germany')" 
                  :stroke="getTerritoryBorderColor('Germany')"
                  stroke-width="2"
                  @click="selectTerritory('Germany')"/>
            <path d="M 500 200 L 550 250 L 500 300 L 450 250 Z" 
                  :fill="getTerritoryColor('Italy')" 
                  :stroke="getTerritoryBorderColor('Italy')"
                  stroke-width="2"
                  @click="selectTerritory('Italy')"/>
            <!-- 亚洲 -->
            <path d="M 600 100 L 700 100 L 750 150 L 700 200 L 650 150 Z" 
                  :fill="getTerritoryColor('Russia')" 
                  :stroke="getTerritoryBorderColor('Russia')"
                  stroke-width="2"
                  @click="selectTerritory('Russia')"/>
            <path d="M 650 200 L 750 200 L 800 250 L 750 300 L 700 250 Z" 
                  :fill="getTerritoryColor('China')" 
                  :stroke="getTerritoryBorderColor('China')"
                  stroke-width="2"
                  @click="selectTerritory('China')"/>
            <path d="M 700 300 L 800 300 L 800 400 L 700 400 Z" 
                  :fill="getTerritoryColor('Japan')" 
                  :stroke="getTerritoryBorderColor('Japan')"
                  stroke-width="2"
                  @click="selectTerritory('Japan')"/>
            <!-- 非洲 -->
            <path d="M 400 250 L 500 250 L 550 300 L 500 350 L 400 350 Z" 
                  :fill="getTerritoryColor('Egypt')" 
                  :stroke="getTerritoryBorderColor('Egypt')"
                  stroke-width="2"
                  @click="selectTerritory('Egypt')"/>
            <path d="M 450 300 L 550 300 L 600 350 L 500 400 L 400 350 Z" 
                  :fill="getTerritoryColor('South Africa')" 
                  :stroke="getTerritoryBorderColor('South Africa')"
                  stroke-width="2"
                  @click="selectTerritory('South Africa')"/>
            <!-- 澳大利亚 -->
            <path d="M 500 450 L 600 450 L 650 500 L 600 550 L 500 550 Z" 
                  :fill="getTerritoryColor('Australia')" 
                  :stroke="getTerritoryBorderColor('Australia')"
                  stroke-width="2"
                  @click="selectTerritory('Australia')"/>
          </g>
          <!-- 绘制军队 -->
          <g id="armies">
            <text 
              v-for="territory in territories" 
              :key="territory.name"
              :x="territory.x"
              :y="territory.y"
              text-anchor="middle"
              dominant-baseline="middle"
              font-size="20"
              font-weight="bold"
              fill="white"
              stroke="black"
              stroke-width="1"
            >
              {{ territory.armies }}
            </text>
          </g>
        </svg>
      </div>
      <div class="game-sidebar">
        <div class="sidebar-section">
          <h3>玩家信息</h3>
          <div class="player-info" 
            v-for="player in players" 
            :key="player.id"
            :class="{
              'current': player.id === currentPlayer.id
            }"
          >
            <div class="player-name">{{ player.name }}</div>
            <div class="player-color" :style="{ backgroundColor: player.color }"></div>
            <div class="player-armies">军队: {{ player.armies }}</div>
            <div class="player-territories">领地: {{ player.territories.length }}</div>
          </div>
        </div>
        <div class="sidebar-section" v-if="selectedTerritory">
          <h3>领地信息</h3>
          <div class="territory-details">
            <div class="detail-item">名称: {{ selectedTerritory.name }}</div>
            <div class="detail-item">所有者: {{ selectedTerritory.owner.name }}</div>
            <div class="detail-item">军队: {{ selectedTerritory.armies }}</div>
            <div class="detail-item">相邻领地: {{ selectedTerritory.neighbors.join(', ') }}</div>
            <div class="detail-actions" v-if="selectedTerritory.owner.id === currentPlayer.id">
              <el-button type="primary" @click="addArmy" :disabled="currentPlayer.armies <= 0">增加军队</el-button>
              <el-button type="success" @click="attack" :disabled="currentPhase !== 'attack'">攻击</el-button>
              <el-button type="warning" @click="moveArmies" :disabled="currentPhase !== 'move'">移动军队</el-button>
            </div>
          </div>
        </div>
      </div>
    </main>
    <footer class="game-footer">
      <p v-if="message">{{ message }}</p>
      <p v-else>{{ getPhaseInstructions() }}</p>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

// 游戏状态
const players = ref([])
const currentPlayerIndex = ref(0)
const currentPhase = ref('reinforce') // reinforce, attack, move
const territories = ref([])
const selectedTerritory = ref(null)
const canNextPhase = ref(false)
const message = ref('')

// 当前玩家
const currentPlayer = computed(() => players.value[currentPlayerIndex.value])

// 初始化游戏
onMounted(() => {
  newGame()
})

// 新游戏
function newGame() {
  // 初始化玩家
  players.value = [
    { id: 1, name: '玩家1', color: '#e74c3c', armies: 0, territories: [] },
    { id: 2, name: '玩家2', color: '#3498db', armies: 0, territories: [] },
    { id: 3, name: '玩家3', color: '#2ecc71', armies: 0, territories: [] },
    { id: 4, name: '玩家4', color: '#f39c12', armies: 0, territories: [] }
  ]
  
  // 初始化领地
  territories.value = [
    { name: 'Alaska', owner: null, armies: 0, x: 150, y: 150, neighbors: ['Canada'] },
    { name: 'Canada', owner: null, armies: 0, x: 250, y: 150, neighbors: ['Alaska', 'United States'] },
    { name: 'United States', owner: null, armies: 0, x: 250, y: 250, neighbors: ['Canada', 'Brazil'] },
    { name: 'Brazil', owner: null, armies: 0, x: 250, y: 350, neighbors: ['United States', 'Argentina'] },
    { name: 'Argentina', owner: null, armies: 0, x: 150, y: 375, neighbors: ['Brazil'] },
    { name: 'United Kingdom', owner: null, armies: 0, x: 450, y: 150, neighbors: ['France'] },
    { name: 'France', owner: null, armies: 0, x: 550, y: 150, neighbors: ['United Kingdom', 'Germany'] },
    { name: 'Germany', owner: null, armies: 0, x: 575, y: 250, neighbors: ['France', 'Italy', 'Russia'] },
    { name: 'Italy', owner: null, armies: 0, x: 475, y: 250, neighbors: ['Germany', 'Egypt'] },
    { name: 'Russia', owner: null, armies: 0, x: 650, y: 150, neighbors: ['Germany', 'China'] },
    { name: 'China', owner: null, armies: 0, x: 725, y: 275, neighbors: ['Russia', 'Japan'] },
    { name: 'Japan', owner: null, armies: 0, x: 750, y: 350, neighbors: ['China'] },
    { name: 'Egypt', owner: null, armies: 0, x: 450, y: 300, neighbors: ['Italy', 'South Africa'] },
    { name: 'South Africa', owner: null, armies: 0, x: 500, y: 375, neighbors: ['Egypt'] },
    { name: 'Australia', owner: null, armies: 0, x: 575, y: 500, neighbors: [] }
  ]
  
  // 随机分配领地
  const shuffledTerritories = shuffle([...territories.value])
  for (let i = 0; i < shuffledTerritories.length; i++) {
    const player = players.value[i % players.value.length]
    const territory = shuffledTerritories[i]
    territory.owner = player
    territory.armies = 1
    player.territories.push(territory)
    player.armies++
  }
  
  // 计算初始军队
  const initialArmies = Math.floor(territories.value.length / players.value.length) + 3
  players.value.forEach(player => {
    player.armies = initialArmies
  })
  
  currentPlayerIndex.value = 0
  currentPhase.value = 'reinforce'
  selectedTerritory.value = null
  canNextPhase.value = false
  message.value = ''
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

// 获取领地颜色
function getTerritoryColor(territoryName) {
  const territory = territories.value.find(t => t.name === territoryName)
  return territory?.owner?.color || '#bdc3c7'
}

// 获取领地边框颜色
function getTerritoryBorderColor(territoryName) {
  const territory = territories.value.find(t => t.name === territoryName)
  return territory?.owner ? 'white' : '#95a5a6'
}

// 选择领地
function selectTerritory(territoryName) {
  selectedTerritory.value = territories.value.find(t => t.name === territoryName)
}

// 增加军队
function addArmy() {
  if (!selectedTerritory.value || selectedTerritory.value.owner.id !== currentPlayer.id || currentPlayer.value.armies <= 0) return
  
  selectedTerritory.value.armies++
  currentPlayer.value.armies--
  
  // 如果玩家没有剩余军队，可以进入下一阶段
  if (currentPlayer.value.armies <= 0) {
    canNextPhase.value = true
  }
  
  message.value = `你在${selectedTerritory.value.name}增加了一支军队`
}

// 攻击
function attack() {
  if (!selectedTerritory.value || selectedTerritory.value.owner.id !== currentPlayer.id) return
  
  // 简单的攻击逻辑
  message.value = `你攻击了${selectedTerritory.value.name}`
}

// 移动军队
function moveArmies() {
  if (!selectedTerritory.value || selectedTerritory.value.owner.id !== currentPlayer.id) return
  
  // 简单的移动军队逻辑
  message.value = `你在${selectedTerritory.value.name}移动了军队`
}

// 下一阶段
function nextPhase() {
  if (!canNextPhase.value) return
  
  // 切换阶段
  if (currentPhase.value === 'reinforce') {
    currentPhase.value = 'attack'
    canNextPhase.value = false
    message.value = '现在是攻击阶段'
  } else if (currentPhase.value === 'attack') {
    currentPhase.value = 'move'
    canNextPhase.value = false
    message.value = '现在是移动阶段'
  } else if (currentPhase.value === 'move') {
    // 切换玩家
    currentPlayerIndex.value = (currentPlayerIndex.value + 1) % players.value.length
    currentPhase.value = 'reinforce'
    
    // 计算新的军队
    const newArmies = Math.max(3, Math.floor(currentPlayer.value.territories.length / 3))
    currentPlayer.value.armies = newArmies
    
    canNextPhase.value = false
    selectedTerritory.value = null
    message.value = `${currentPlayer.value.name}的回合，你有${newArmies}支军队可以部署`
  }
}

// 获取阶段说明
function getPhaseInstructions() {
  switch (currentPhase.value) {
    case 'reinforce':
      return `部署阶段: 在你的领地上部署军队，你有${currentPlayer.value.armies}支军队可以部署`
    case 'attack':
      return '攻击阶段: 攻击相邻的敌方领地'
    case 'move':
      return '移动阶段: 在你的领地之间移动军队'
    default:
      return '请点击"下一阶段"开始游戏'
  }
}
</script>

<style scoped>
.risk-container {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  overflow: hidden;
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
.phase {
  font-size: 1.2rem;
  font-weight: bold;
}

.game-main {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 2rem;
  padding: 1rem;
  overflow: auto;
}

.game-board {
  width: 800px;
  height: 600px;
  background: #2c3e50;
  border: 10px solid #2c3e50;
  border-radius: 12px;
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.3);
  display: flex;
  justify-content: center;
  align-items: center;
}

.game-board svg {
  width: 100%;
  height: 100%;
}

.game-board path {
  cursor: pointer;
  transition: all 0.3s ease;
}

.game-board path:hover {
  opacity: 0.8;
}

.game-sidebar {
  width: 300px;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.sidebar-section {
  background: #2c3e50;
  padding: 1rem;
  border-radius: 12px;
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.3);
}

.sidebar-section h3 {
  margin-bottom: 1rem;
  text-align: center;
}

.player-info {
  margin-bottom: 1rem;
  padding: 0.5rem;
  border-radius: 8px;
  background: #34495e;
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
}

.player-info.current {
  background: #3498db;
}

.player-name {
  font-weight: bold;
}

.player-color {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  border: 2px solid white;
}

.player-armies,
.player-territories {
  font-size: 0.9rem;
}

.territory-details {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.detail-item {
  padding: 0.3rem;
  border-radius: 4px;
  background: #34495e;
}

.detail-actions {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin-top: 0.5rem;
}

.game-footer {
  text-align: center;
  padding: 1rem;
  font-size: 1.2rem;
  font-weight: bold;
}
</style>