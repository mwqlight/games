<template>
  <div class="checkers-container">
    <header class="game-header">
      <h1>跳棋游戏</h1>
      <div class="game-info">
        <el-button type="primary" @click="goBack">返回主页</el-button>
        <el-button type="success" @click="restartGame" :disabled="gameStatus !== 'PLAYING'">重新开始</el-button>
      </div>
    </header>
    
    <!-- 房间管理界面 -->
    <div v-if="!roomId" class="room-management">
      <el-input v-model="playerName" placeholder="请输入你的名字" class="player-name-input" />
      <div class="room-buttons">
        <el-button type="primary" @click="createRoom" :disabled="!playerName">创建房间</el-button>
        <el-button type="success" @click="createAIGame" :disabled="!playerName">人机对战</el-button>
        <el-button type="success" @click="joinRoom" :disabled="!playerName || !roomIdInput">加入房间</el-button>
      </div>
      <el-input v-model="roomIdInput" placeholder="请输入房间ID" class="room-id-input" />
      <el-button type="info" @click="getActiveRooms">查看活跃房间</el-button>
      <div v-if="activeRooms.length > 0" class="active-rooms">
        <h3>活跃房间：</h3>
        <el-list>
          <el-list-item v-for="room in activeRooms" :key="room.id">
            <span>{{ room.id }} - {{ room.players.length }}/2 玩家</span>
            <el-button type="primary" size="small" @click="joinRoomById(room.id)">加入</el-button>
          </el-list-item>
        </el-list>
      </div>
    </div>
    
    <!-- 游戏界面 -->
    <main v-else class="game-main">
      <div class="game-board">
        <div class="board-row" v-for="(row, rowIndex) in board" :key="rowIndex">
          <div 
            class="board-cell" 
            v-for="(cell, colIndex) in row" 
            :key="colIndex"
            :class="{
              'black-cell': (rowIndex + colIndex) % 2 === 1,
              'white-cell': (rowIndex + colIndex) % 2 === 0,
              'valid-move': isValidMovePosition(rowIndex, colIndex),
              'selected-piece': selectedPiece && selectedPiece.row === rowIndex && selectedPiece.col === colIndex
            }"
            @click="handleCellClick(rowIndex, colIndex)"
          >
            <div 
              class="piece" 
              v-if="cell !== null"
              :class="{
                'red-piece': cell.color === 'RED',
                'black-piece': cell.color === 'BLACK',
                'king-piece': cell.isKing
              }"
            ></div>
          </div>
        </div>
      </div>
      <div class="game-status">
        <h2>房间ID: {{ roomId }}</h2>
        <h3>{{ currentPlayer === 'RED' ? '红方' : '黑方' }}回合</h3>
        <p>{{ gameStatusText }}</p>
        <div class="player-info">
          <p>红方: {{ redPlayerName || '等待玩家' }}</p>
          <p>黑方: {{ blackPlayerName || '等待玩家' }}</p>
        </div>
        <div class="piece-count">
          <p>红方棋子: {{ redPieceCount }}</p>
          <p>黑方棋子: {{ blackPieceCount }}</p>
        </div>
        <el-button type="danger" @click="leaveRoom">离开房间</el-button>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import SockJS from 'sockjs-client'
import Stomp from 'stompjs'

const router = useRouter()

// 房间管理相关
const playerName = ref('')
const roomId = ref(null)
const roomIdInput = ref('')
const activeRooms = ref([])

// 游戏状态相关
const board = ref([])
const currentPlayer = ref('RED')
const gameStatus = ref('WAITING_FOR_PLAYERS')
const redPlayerName = ref('')
const blackPlayerName = ref('')
const redPieceCount = ref(0)
const blackPieceCount = ref(0)

// 玩家相关
const selectedPiece = ref(null)
const validMoves = ref([])
let stompClient = null

// 计算属性
const gameStatusText = computed(() => {
  switch (gameStatus.value) {
    case 'WAITING_FOR_PLAYERS':
      return '等待玩家加入...'
    case 'PLAYING':
      return '游戏进行中'
    case 'PAUSED':
      return '游戏暂停'
    case 'FINISHED':
      return currentPlayer.value === 'RED' ? '黑方获胜' : '红方获胜'
    default:
      return '未知状态'
  }
})

// 初始化游戏
const initGame = () => {
  // 创建一个8x8的棋盘
  board.value = Array(8).fill(null).map(() => Array(8).fill(null))
}

// 连接WebSocket
const connectWebSocket = () => {
  const socket = new SockJS('http://localhost:8083/ws-game')
  stompClient = Stomp.over(socket)
  
  stompClient.connect({}, (frame) => {
    console.log('Connected: ' + frame)
    
    // 订阅游戏状态更新
    stompClient.subscribe(`/topic/game/${roomId.value}`, (gameState) => {
      updateGameState(JSON.parse(gameState.body))
    })
    
    // 加入房间
    joinGameRoom()
  }, (error) => {
    console.error('WebSocket connection error: ', error)
    ElMessage.error('连接失败，请重试')
  })
}

// 断开WebSocket
const disconnectWebSocket = () => {
  if (stompClient !== null) {
    stompClient.disconnect()
    console.log('Disconnected')
  }
}

// 创建房间
const createRoom = async () => {
  try {
    const response = await fetch('/api/game/room/create', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      }
    })
    
    if (response.ok) {
      const gameRoom = await response.json()
      roomId.value = gameRoom.id
      connectWebSocket()
      ElMessage.success('房间创建成功')
    } else {
      ElMessage.error('房间创建失败')
    }
  } catch (error) {
    console.error('Error creating room: ', error)
    ElMessage.error('网络错误，请重试')
  }
}

// 创建人机对战房间
const createAIGame = async () => {
  try {
    const response = await fetch('/api/game/room/create-ai', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      }
    })
    
    if (response.ok) {
      const gameRoom = await response.json()
      roomId.value = gameRoom.id
      connectWebSocket()
      ElMessage.success('人机对战房间创建成功')
    } else {
      ElMessage.error('人机对战房间创建失败')
    }
  } catch (error) {
    console.error('Error creating AI game room: ', error)
    ElMessage.error('网络错误，请重试')
  }
}

// 加入房间
const joinRoom = () => {
  if (roomIdInput.value) {
    roomId.value = roomIdInput.value
    connectWebSocket()
  }
}

// 通过ID加入房间
const joinRoomById = (roomId) => {
  roomIdInput.value = roomId
  joinRoom()
}

// 加入游戏房间
const joinGameRoom = () => {
  if (stompClient !== null && roomId.value && playerName.value) {
    stompClient.send(`/app/game/${roomId.value}/join`, {}, JSON.stringify({
      name: playerName.value
    }))
  }
}

// 离开房间
const leaveRoom = () => {
  if (stompClient !== null && roomId.value) {
    stompClient.send(`/app/game/${roomId.value}/leave`, {})
  }
  disconnectWebSocket()
  roomId.value = null
  selectedPiece.value = null
  validMoves.value = []
  ElMessage.info('已离开房间')
}

// 获取活跃房间
const getActiveRooms = async () => {
  try {
    const response = await fetch('/api/game/rooms')
    if (response.ok) {
      activeRooms.value = await response.json()
    } else {
      ElMessage.error('获取房间列表失败')
    }
  } catch (error) {
    console.error('Error getting active rooms: ', error)
    ElMessage.error('网络错误，请重试')
  }
}

// 处理单元格点击
const handleCellClick = (rowIndex, colIndex) => {
  if (gameStatus.value !== 'PLAYING') {
    ElMessage.warning('游戏未开始或已结束')
    return
  }
  
  const piece = board.value[rowIndex][colIndex]
  
  // 如果点击的是当前玩家的棋子，显示有效移动
  if (piece !== null && piece.color === currentPlayer.value) {
    selectedPiece.value = { row: rowIndex, col: colIndex }
    getValidMovesFromServer(rowIndex, colIndex)
  }
  // 如果点击的是有效移动位置，执行移动
  else if (selectedPiece.value && isValidMovePosition(rowIndex, colIndex)) {
    makeMove(selectedPiece.value.row, selectedPiece.value.col, rowIndex, colIndex)
    selectedPiece.value = null
    validMoves.value = []
  }
  // 否则取消选择
  else {
    selectedPiece.value = null
    validMoves.value = []
  }
}

// 从服务器获取有效移动
const getValidMovesFromServer = async (row, col) => {
  try {
    const response = await fetch('/api/game/moves', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        roomId: roomId.value,
        from: { row, col }
      })
    })
    
    if (response.ok) {
      const moves = await response.json()
      validMoves.value = moves.map(move => ({ row: move.to.row, col: move.to.col }))
    }
  } catch (error) {
    console.error('Error getting valid moves: ', error)
  }
}

// 检查是否是有效移动位置
const isValidMovePosition = (row, col) => {
  return validMoves.value.some(move => move.row === row && move.col === col)
}

// 执行移动
const makeMove = (fromRow, fromCol, toRow, toCol) => {
  if (stompClient !== null && roomId.value) {
    stompClient.send(`/app/game/${roomId.value}/move`, {}, JSON.stringify({
      from: { row: fromRow, col: fromCol },
      to: { row: toRow, col: toCol }
    }))
  }
}

// 重新开始游戏
const restartGame = () => {
  if (stompClient !== null && roomId.value) {
    stompClient.send(`/app/game/${roomId.value}/restart`, {})
    selectedPiece.value = null
    validMoves.value = []
    ElMessage.info('游戏已重新开始')
  }
}

// 更新游戏状态
const updateGameState = (gameState) => {
  // 更新棋盘状态
  board.value = gameState.boardState.map(row => 
    row.map(piece => piece ? { color: piece.color, isKing: piece.isKing } : null)
  )
  
  // 更新游戏状态
  currentPlayer.value = gameState.currentPlayer
  gameStatus.value = gameState.gameStatus
  redPieceCount.value = gameState.redPieceCount
  blackPieceCount.value = gameState.blackPieceCount
  
  // 更新玩家信息
  if (gameState.players) {
    gameState.players.forEach(player => {
      if (player.color === 'RED') {
        redPlayerName.value = player.name
      } else if (player.color === 'BLACK') {
        blackPlayerName.value = player.name
      }
    })
  }
  
  // 如果游戏结束，显示提示
  if (gameState.gameStatus === 'FINISHED') {
    ElMessage.success(gameStatusText.value)
  }
}

// 返回主页
const goBack = () => {
  if (roomId.value) {
    leaveRoom()
  }
  router.push('/')
}

// 组件挂载时初始化游戏
onMounted(() => {
  initGame()
})

// 组件卸载时断开WebSocket连接
onUnmounted(() => {
  disconnectWebSocket()
})
</script>

<style scoped>
.checkers-container {
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

/* 房间管理界面 */
.room-management {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 20px;
  max-width: 400px;
  margin: 0 auto;
  width: 100%;
}

.player-name-input,
.room-id-input {
  width: 100%;
  max-width: 300px;
}

.room-buttons {
  display: flex;
  gap: 10px;
}

.active-rooms {
  width: 100%;
  max-width: 300px;
  text-align: center;
}

.active-rooms h3 {
  margin-bottom: 10px;
}

/* 游戏界面 */
.game-main {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 50px;
}

.game-board {
  display: flex;
  flex-direction: column;
  border: 2px solid white;
  border-radius: 8px;
  overflow: hidden;
}

.board-row {
  display: flex;
}

.board-cell {
  width: 60px;
  height: 60px;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  transition: background-color 0.3s;
  position: relative;
}

.black-cell {
  background-color: #7B3F00;
}

.white-cell {
  background-color: #FFEBCD;
}

.valid-move {
  position: relative;
}

.valid-move::after {
  content: '';
  position: absolute;
  width: 20px;
  height: 20px;
  background-color: rgba(0, 255, 0, 0.7);
  border-radius: 50%;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.selected-piece {
  background-color: rgba(255, 255, 0, 0.3) !important;
}

.piece {
  width: 45px;
  height: 45px;
  border-radius: 50%;
  border: 2px solid white;
  transition: transform 0.3s;
  position: relative;
}

.red-piece {
  background-color: #FF4500;
}

.black-piece {
  background-color: #000000;
}

.king-piece::after {
  content: '♔';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: white;
  font-size: 20px;
  font-weight: bold;
}

.piece:hover {
  transform: scale(1.1);
}

.game-status {
  display: flex;
  flex-direction: column;
  gap: 15px;
  text-align: center;
  min-width: 200px;
}

.game-status h2 {
  font-size: 1.5rem;
  margin: 0;
}

.game-status h3 {
  font-size: 1.2rem;
  margin: 0;
}

.game-status p {
  font-size: 1rem;
  margin: 0;
}

.player-info {
  background: rgba(255, 255, 255, 0.1);
  padding: 10px;
  border-radius: 8px;
}

.piece-count {
  background: rgba(255, 255, 255, 0.1);
  padding: 10px;
  border-radius: 8px;
}
</style>