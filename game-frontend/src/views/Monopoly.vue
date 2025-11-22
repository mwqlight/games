<template>
  <div class="monopoly-container">
    <header class="game-header">
      <h1>大富翁</h1>
      <div class="game-info">
        <span class="current-player">当前玩家: {{ currentPlayer.name }} ({{ currentPlayer.money }}元)</span>
        <span class="turn">回合: {{ turn }}</span>
        <el-button type="primary" @click="rollDice" :disabled="isRolling">掷骰子</el-button>
        <el-button type="success" @click="endTurn" :disabled="!canEndTurn">结束回合</el-button>
      </div>
    </header>
    <main class="game-main">
      <div class="game-board">
        <!-- 棋盘边缘的地块 -->
        <div class="board-edge top">
          <div 
            class="property" 
            v-for="(property, index) in properties.slice(0, 11)" 
            :key="index"
            :class="{
              'owned': property.owner,
              'current': currentPlayer.position === index
            }"
            @click="selectProperty(property)"
          >
            <div class="property-name">{{ property.name }}</div>
            <div class="property-price">{{ property.price }}元</div>
            <div class="property-rent" v-if="property.rent">租金: {{ property.rent }}元</div>
            <div class="property-owner" v-if="property.owner">{{ property.owner.name }}</div>
          </div>
        </div>
        <div class="board-edge right">
          <div 
            class="property" 
            v-for="(property, index) in properties.slice(11, 22)" 
            :key="index"
            :class="{
              'owned': property.owner,
              'current': currentPlayer.position === index
            }"
            @click="selectProperty(property)"
          >
            <div class="property-name">{{ property.name }}</div>
            <div class="property-price">{{ property.price }}元</div>
            <div class="property-rent" v-if="property.rent">租金: {{ property.rent }}元</div>
            <div class="property-owner" v-if="property.owner">{{ property.owner.name }}</div>
          </div>
        </div>
        <div class="board-edge bottom">
          <div 
            class="property" 
            v-for="(property, index) in properties.slice(22, 33).reverse()" 
            :key="index"
            :class="{
              'owned': property.owner,
              'current': currentPlayer.position === 32 - index
            }"
            @click="selectProperty(property)"
          >
            <div class="property-name">{{ property.name }}</div>
            <div class="property-price">{{ property.price }}元</div>
            <div class="property-rent" v-if="property.rent">租金: {{ property.rent }}元</div>
            <div class="property-owner" v-if="property.owner">{{ property.owner.name }}</div>
          </div>
        </div>
        <div class="board-edge left">
          <div 
            class="property" 
            v-for="(property, index) in properties.slice(33, 44).reverse()" 
            :key="index"
            :class="{
              'owned': property.owner,
              'current': currentPlayer.position === 43 - index
            }"
            @click="selectProperty(property)"
          >
            <div class="property-name">{{ property.name }}</div>
            <div class="property-price">{{ property.price }}元</div>
            <div class="property-rent" v-if="property.rent">租金: {{ property.rent }}元</div>
            <div class="property-owner" v-if="property.owner">{{ property.owner.name }}</div>
          </div>
        </div>
        <!-- 棋盘中心 -->
        <div class="board-center">
          <div class="player" 
            v-for="player in players" 
            :key="player.id"
            :style="{
              top: `${50 + player.position % 11 * 4.5}%`,
              left: player.position < 11 ? `${5 + player.position * 8}%` : 
                     player.position < 22 ? `90%` : 
                     player.position < 33 ? `${90 - (player.position - 22) * 8}%` : 
                     `5%`
            }"
            :class="`player-${player.id}`"
          >
            {{ player.name.charAt(0) }}
          </div>
        </div>
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
            <div class="player-money">{{ player.money }}元</div>
            <div class="player-properties">
              <span 
                class="property-tag"
                v-for="property in player.properties"
                :key="property.id"
              >
                {{ property.name }}
              </span>
            </div>
          </div>
        </div>
        <div class="sidebar-section" v-if="selectedProperty">
          <h3>地块信息</h3>
          <div class="property-details">
            <div class="detail-item">名称: {{ selectedProperty.name }}</div>
            <div class="detail-item">价格: {{ selectedProperty.price }}元</div>
            <div class="detail-item" v-if="selectedProperty.rent">租金: {{ selectedProperty.rent }}元</div>
            <div class="detail-item" v-if="selectedProperty.owner">所有者: {{ selectedProperty.owner.name }}</div>
            <div class="detail-actions" v-if="!selectedProperty.owner">
              <el-button type="primary" @click="buyProperty(selectedProperty)" :disabled="currentPlayer.money < selectedProperty.price">购买</el-button>
            </div>
            <div class="detail-actions" v-else-if="selectedProperty.owner.id === currentPlayer.id">
              <el-button type="success" @click="buildHouse(selectedProperty)">建造房屋</el-button>
            </div>
          </div>
        </div>
      </div>
    </main>
    <footer class="game-footer">
      <p v-if="message">{{ message }}</p>
      <p v-else>点击"掷骰子"开始你的回合</p>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

// 游戏状态
const players = ref([])
const currentPlayerIndex = ref(0)
const turn = ref(1)
const isRolling = ref(false)
const canEndTurn = ref(false)
const message = ref('')
const selectedProperty = ref(null)

// 地块数据
const properties = ref([
  { id: 0, name: '起点', price: 0, rent: 0, owner: null, type: 'start' },
  { id: 1, name: '地中海', price: 60, rent: 2, owner: null, type: 'property', color: 'brown' },
  { id: 2, name: '社区 Chest', price: 0, rent: 0, owner: null, type: 'chest' },
  { id: 3, name: '波罗的海', price: 60, rent: 4, owner: null, type: 'property', color: 'brown' },
  { id: 4, name: '所得税', price: 200, rent: 0, owner: null, type: 'tax' },
  { id: 5, name: 'Reading铁路', price: 200, rent: 25, owner: null, type: 'railroad' },
  { id: 6, name: '东方大道', price: 100, rent: 6, owner: null, type: 'property', color: 'light-blue' },
  { id: 7, name: '机会', price: 0, rent: 0, owner: null, type: 'chance' },
  { id: 8, name: '弗吉尼亚大道', price: 100, rent: 6, owner: null, type: 'property', color: 'light-blue' },
  { id: 9, name: '州立大道', price: 120, rent: 8, owner: null, type: 'property', color: 'light-blue' },
  { id: 10, name: '监狱', price: 0, rent: 0, owner: null, type: 'jail' },
  { id: 11, name: '圣詹姆斯', price: 140, rent: 10, owner: null, type: 'property', color: 'pink' },
  { id: 12, name: '电力公司', price: 150, rent: 0, owner: null, type: 'utility' },
  { id: 13, name: '田纳西大道', price: 140, rent: 10, owner: null, type: 'property', color: 'pink' },
  { id: 14, name: '纽约大道', price: 160, rent: 12, owner: null, type: 'property', color: 'pink' },
  { id: 15, name: '宾夕法尼亚铁路', price: 200, rent: 25, owner: null, type: 'railroad' },
  { id: 16, name: '肯塔基大道', price: 180, rent: 14, owner: null, type: 'property', color: 'orange' },
  { id: 17, name: '社区 Chest', price: 0, rent: 0, owner: null, type: 'chest' },
  { id: 18, name: '印第安纳大道', price: 180, rent: 14, owner: null, type: 'property', color: 'orange' },
  { id: 19, name: '伊利诺伊大道', price: 200, rent: 16, owner: null, type: 'property', color: 'orange' },
  { id: 20, name: '免费停车', price: 0, rent: 0, owner: null, type: 'free-parking' },
  { id: 21, name: '大西洋大道', price: 220, rent: 18, owner: null, type: 'property', color: 'red' },
  { id: 22, name: '机会', price: 0, rent: 0, owner: null, type: 'chance' },
  { id: 23, name: ' Ventnor大道', price: 220, rent: 18, owner: null, type: 'property', color: 'red' },
  { id: 24, name: ' Marvin花园', price: 240, rent: 20, owner: null, type: 'property', color: 'red' },
  { id: 25, name: 'B&O铁路', price: 200, rent: 25, owner: null, type: 'railroad' },
  { id: 26, name: '太平洋大道', price: 260, rent: 22, owner: null, type: 'property', color: 'yellow' },
  { id: 27, name: '北卡罗来纳大道', price: 260, rent: 22, owner: null, type: 'property', color: 'yellow' },
  { id: 28, name: '自来水公司', price: 150, rent: 0, owner: null, type: 'utility' },
  { id: 29, name: '宾夕法尼亚大道', price: 280, rent: 24, owner: null, type: 'property', color: 'yellow' },
  { id: 30, name: '去监狱', price: 0, rent: 0, owner: null, type: 'go-to-jail' },
  { id: 31, name: 'Park Place', price: 350, rent: 26, owner: null, type: 'property', color: 'green' },
  { id: 32, name: '社区 Chest', price: 0, rent: 0, owner: null, type: 'chest' },
  { id: 33, name: 'Boardwalk', price: 400, rent: 50, owner: null, type: 'property', color: 'dark-blue' },
  { id: 34, name: '奢侈品税', price: 100, rent: 0, owner: null, type: 'tax' },
  { id: 35, name: 'Short Line铁路', price: 200, rent: 25, owner: null, type: 'railroad' },
  { id: 36, name: '机会', price: 0, rent: 0, owner: null, type: 'chance' },
  { id: 37, name: 'Park Place', price: 350, rent: 26, owner: null, type: 'property', color: 'green' },
  { id: 38, name: '社区 Chest', price: 0, rent: 0, owner: null, type: 'chest' },
  { id: 39, name: 'Boardwalk', price: 400, rent: 50, owner: null, type: 'property', color: 'dark-blue' },
  { id: 40, name: '免费停车', price: 0, rent: 0, owner: null, type: 'free-parking' },
  { id: 41, name: '大西洋大道', price: 220, rent: 18, owner: null, type: 'property', color: 'red' },
  { id: 42, name: '机会', price: 0, rent: 0, owner: null, type: 'chance' },
  { id: 43, name: ' Ventnor大道', price: 220, rent: 18, owner: null, type: 'property', color: 'red' }
])

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
    { id: 1, name: '玩家1', money: 1500, position: 0, properties: [] },
    { id: 2, name: '玩家2', money: 1500, position: 0, properties: [] },
    { id: 3, name: '玩家3', money: 1500, position: 0, properties: [] },
    { id: 4, name: '玩家4', money: 1500, position: 0, properties: [] }
  ]
  
  currentPlayerIndex.value = 0
  turn.value = 1
  isRolling.value = false
  canEndTurn.value = false
  message.value = ''
  selectedProperty.value = null
  
  // 重置地块所有者
  properties.value.forEach(property => {
    property.owner = null
  })
}

// 掷骰子
function rollDice() {
  if (isRolling.value || canEndTurn.value) return
  
  isRolling.value = true
  message.value = `${currentPlayer.value.name}正在掷骰子...`
  
  // 模拟掷骰子动画
  setTimeout(() => {
    const dice1 = Math.floor(Math.random() * 6) + 1
    const dice2 = Math.floor(Math.random() * 6) + 1
    const total = dice1 + dice2
    
    message.value = `${currentPlayer.value.name}掷出了 ${dice1} + ${dice2} = ${total}`
    
    // 移动玩家
    movePlayer(total)
    
    isRolling.value = false
    canEndTurn.value = true
  }, 1000)
}

// 移动玩家
function movePlayer(spaces) {
  const newPosition = (currentPlayer.value.position + spaces) % properties.value.length
  currentPlayer.value.position = newPosition
  
  // 检查是否经过起点
  if (newPosition < currentPlayer.value.position - spaces) {
    currentPlayer.value.money += 200
    message.value += `，经过起点获得200元`
  }
  
  // 处理当前地块
  const currentProperty = properties.value[newPosition]
  handleProperty(currentProperty)
}

// 处理地块
function handleProperty(property) {
  selectedProperty.value = property
  
  switch (property.type) {
    case 'start':
      message.value += `，你在起点`
      break
    case 'jail':
      message.value += `，你在监狱`
      break
    case 'free-parking':
      message.value += `，你在免费停车`
      break
    case 'go-to-jail':
      message.value += `，你被送到监狱`
      currentPlayer.value.position = 10
      break
    case 'tax':
      currentPlayer.value.money -= property.price
      message.value += `，你支付了${property.price}元税款`
      break
    case 'chest':
    case 'chance':
      // 简单处理社区 Chest 和机会卡
      const cardValue = Math.floor(Math.random() * 100) + 50
      currentPlayer.value.money += cardValue
      message.value += `，你获得了${cardValue}元`
      break
    case 'property':
      if (property.owner) {
        if (property.owner.id !== currentPlayer.value.id) {
          currentPlayer.value.money -= property.rent
          property.owner.money += property.rent
          message.value += `，你向${property.owner.name}支付了${property.rent}元租金`
        }
      } else {
        message.value += `，你可以购买${property.name}（${property.price}元）`
      }
      break
    case 'railroad':
    case 'utility':
      // 铁路和公共事业的处理逻辑
      if (property.owner) {
        if (property.owner.id !== currentPlayer.value.id) {
          // 计算租金
          const rent = property.type === 'railroad' ? 25 * Math.pow(2, property.owner.properties.filter(p => p.type === 'railroad').length - 1) : 10
          currentPlayer.value.money -= rent
          property.owner.money += rent
          message.value += `，你向${property.owner.name}支付了${rent}元租金`
        }
      } else {
        message.value += `，你可以购买${property.name}（${property.price}元）`
      }
      break
  }
}

// 选择地块
function selectProperty(property) {
  selectedProperty.value = property
}

// 购买地块
function buyProperty(property) {
  if (!property.owner && currentPlayer.value.money >= property.price) {
    currentPlayer.value.money -= property.price
    property.owner = currentPlayer.value
    currentPlayer.value.properties.push(property)
    message.value = `你购买了${property.name}`
  }
}

// 建造房屋
function buildHouse(property) {
  // 简单的建造房屋逻辑
  message.value = `你在${property.name}建造了一栋房屋`
}

// 结束回合
function endTurn() {
  if (!canEndTurn.value) return
  
  // 切换玩家
  currentPlayerIndex.value = (currentPlayerIndex.value + 1) % players.value.length
  
  // 重置回合状态
  canEndTurn.value = false
  selectedProperty.value = null
  message.value = `${currentPlayer.value.name}的回合`
  
  // 增加回合数
  if (currentPlayerIndex.value === 0) {
    turn.value++
  }
}
</script>

<style scoped>
.monopoly-container {
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
.turn {
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
  width: 600px;
  height: 600px;
  background: #8B4513;
  border: 10px solid #8B4513;
  border-radius: 12px;
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.3);
  position: relative;
}

.board-edge {
  position: absolute;
  display: flex;
  gap: 1px;
  background: #8B4513;
}

.board-edge.top {
  top: 0;
  left: 0;
  right: 0;
  height: 100px;
}

.board-edge.right {
  top: 100px;
  right: 0;
  bottom: 100px;
  width: 100px;
  flex-direction: column;
}

.board-edge.bottom {
  bottom: 0;
  left: 0;
  right: 0;
  height: 100px;
}

.board-edge.left {
  top: 100px;
  left: 0;
  bottom: 100px;
  width: 100px;
  flex-direction: column;
}

.property {
  flex: 1;
  background: #DEB887;
  color: #8B4513;
  padding: 0.5rem;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  border: 1px solid #8B4513;
  font-size: 0.8rem;
  text-align: center;
  transition: all 0.3s ease;
}

.property:hover {
  background: #F5DEB3;
}

.property.owned {
  background: #98FB98;
}

.property.current {
  background: #3498db;
  color: white;
}

.property-name {
  font-weight: bold;
  margin-bottom: 0.2rem;
}

.property-price,
.property-rent,
.property-owner {
  font-size: 0.7rem;
  margin-bottom: 0.1rem;
}

.board-center {
  position: absolute;
  top: 100px;
  left: 100px;
  right: 100px;
  bottom: 100px;
  background: #F5DEB3;
  border-radius: 8px;
  position: relative;
}

.player {
  position: absolute;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  font-weight: bold;
  font-size: 1.2rem;
  color: white;
  border: 2px solid white;
  z-index: 10;
  transform: translate(-50%, -50%);
}

.player-1 {
  background: #e74c3c;
}

.player-2 {
  background: #3498db;
}

.player-3 {
  background: #2ecc71;
}

.player-4 {
  background: #f39c12;
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
}

.player-info.current {
  background: #3498db;
}

.player-name {
  font-weight: bold;
  margin-bottom: 0.2rem;
}

.player-money {
  font-size: 0.9rem;
  margin-bottom: 0.5rem;
}

.player-properties {
  display: flex;
  flex-wrap: wrap;
  gap: 0.3rem;
}

.property-tag {
  background: #98FB98;
  color: #8B4513;
  padding: 0.2rem 0.4rem;
  border-radius: 4px;
  font-size: 0.7rem;
  font-weight: bold;
}

.property-details {
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