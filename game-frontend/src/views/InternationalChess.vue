<template>
  <div class="international-chess-container">
    <header class="game-header">
      <h1>国际象棋游戏</h1>
      <div class="game-info">
        <el-button type="primary" @click="goBack">返回主页</el-button>
        <el-button type="success" @click="restartGame">重新开始</el-button>
      </div>
    </header>
    <main class="game-main">
      <div class="game-board">
        <div class="board-row" v-for="(row, rowIndex) in board" :key="rowIndex">
          <div 
            class="board-cell" 
            v-for="(cell, colIndex) in row" 
            :key="colIndex"
            :class="{ 'black-cell': (rowIndex + colIndex) % 2 === 1, 'white-cell': (rowIndex + colIndex) % 2 === 0 }"
            @click="makeMove(rowIndex, colIndex)"
          >
            <div 
              class="piece" 
              v-if="cell !== null"
              :class="{ 'white-piece': cell.color === 'white', 'black-piece': cell.color === 'black' }"
            >
              {{ getPieceSymbol(cell.type) }}
            </div>
          </div>
        </div>
      </div>
      <div class="game-status">
        <h2>{{ currentPlayer === 'white' ? '白方' : '黑方' }}回合</h2>
        <p>{{ gameStatus }}</p>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 初始化棋盘
const board = ref([])
const currentPlayer = ref('white')
const gameStatus = ref('游戏进行中')

// 初始化游戏
const initGame = () => {
  // 创建一个8x8的棋盘
  board.value = Array(8).fill(null).map(() => Array(8).fill(null))
  
  // 放置白方棋子
  board.value[0][0] = { color: 'white', type: 'rook' }
  board.value[0][1] = { color: 'white', type: 'knight' }
  board.value[0][2] = { color: 'white', type: 'bishop' }
  board.value[0][3] = { color: 'white', type: 'queen' }
  board.value[0][4] = { color: 'white', type: 'king' }
  board.value[0][5] = { color: 'white', type: 'bishop' }
  board.value[0][6] = { color: 'white', type: 'knight' }
  board.value[0][7] = { color: 'white', type: 'rook' }
  
  for (let col = 0; col < 8; col++) {
    board.value[1][col] = { color: 'white', type: 'pawn' }
  }
  
  // 放置黑方棋子
  board.value[7][0] = { color: 'black', type: 'rook' }
  board.value[7][1] = { color: 'black', type: 'knight' }
  board.value[7][2] = { color: 'black', type: 'bishop' }
  board.value[7][3] = { color: 'black', type: 'queen' }
  board.value[7][4] = { color: 'black', type: 'king' }
  board.value[7][5] = { color: 'black', type: 'bishop' }
  board.value[7][6] = { color: 'black', type: 'knight' }
  board.value[7][7] = { color: 'black', type: 'rook' }
  
  for (let col = 0; col < 8; col++) {
    board.value[6][col] = { color: 'black', type: 'pawn' }
  }
  
  currentPlayer.value = 'white'
  gameStatus.value = '游戏进行中'
}

// 获取棋子符号
const getPieceSymbol = (type) => {
  switch (type) {
    case 'rook': return '♖'
    case 'knight': return '♘'
    case 'bishop': return '♗'
    case 'queen': return '♕'
    case 'king': return '♔'
    case 'pawn': return '♙'
    default: return ''
  }
}

// 处理移动
const makeMove = (rowIndex, colIndex) => {
  // 检查当前位置是否有棋子
  const piece = board.value[rowIndex][colIndex]
  if (piece === null || piece.color !== currentPlayer.value) {
    return
  }
  
  // 检查是否可以移动
  const validMoves = getValidMoves(rowIndex, colIndex)
  if (validMoves.length === 0) {
    return
  }
  
  // 移动棋子
  board.value[rowIndex][colIndex] = null
  board.value[validMoves[0].row][validMoves[0].col] = piece
  
  // 切换玩家
  currentPlayer.value = currentPlayer.value === 'white' ? 'black' : 'white'
  
  // 检查游戏是否结束
  checkGameEnd()
}

// 获取有效移动
const getValidMoves = (rowIndex, colIndex) => {
  const moves = []
  const piece = board.value[rowIndex][colIndex]
  
  switch (piece.type) {
    case 'pawn':
      // 处理兵的移动
      const direction = piece.color === 'white' ? 1 : -1
      
      // 向前移动一格
      if (rowIndex + direction >= 0 && rowIndex + direction < 8 && 
          board.value[rowIndex + direction][colIndex] === null) {
        moves.push({ row: rowIndex + direction, col: colIndex })
        
        // 初始位置可以向前移动两格
        if ((piece.color === 'white' && rowIndex === 1) || (piece.color === 'black' && rowIndex === 6)) {
          if (board.value[rowIndex + direction * 2][colIndex] === null) {
            moves.push({ row: rowIndex + direction * 2, col: colIndex })
          }
        }
      }
      
      // 吃子
      if (rowIndex + direction >= 0 && rowIndex + direction < 8) {
        // 左前方吃子
        if (colIndex - 1 >= 0 && 
            board.value[rowIndex + direction][colIndex - 1] !== null && 
            board.value[rowIndex + direction][colIndex - 1].color !== piece.color) {
          moves.push({ row: rowIndex + direction, col: colIndex - 1 })
        }
        
        // 右前方吃子
        if (colIndex + 1 < 8 && 
            board.value[rowIndex + direction][colIndex + 1] !== null && 
            board.value[rowIndex + direction][colIndex + 1].color !== piece.color) {
          moves.push({ row: rowIndex + direction, col: colIndex + 1 })
        }
      }
      break
    
    case 'rook':
      // 处理车的移动
      // 向上移动
      for (let row = rowIndex - 1; row >= 0; row--) {
        if (board.value[row][colIndex] === null) {
          moves.push({ row, col: colIndex })
        } else if (board.value[row][colIndex].color !== piece.color) {
          moves.push({ row, col: colIndex })
          break
        } else {
          break
        }
      }
      
      // 向下移动
      for (let row = rowIndex + 1; row < 8; row++) {
        if (board.value[row][colIndex] === null) {
          moves.push({ row, col: colIndex })
        } else if (board.value[row][colIndex].color !== piece.color) {
          moves.push({ row, col: colIndex })
          break
        } else {
          break
        }
      }
      
      // 向左移动
      for (let col = colIndex - 1; col >= 0; col--) {
        if (board.value[rowIndex][col] === null) {
          moves.push({ row: rowIndex, col })
        } else if (board.value[rowIndex][col].color !== piece.color) {
          moves.push({ row: rowIndex, col })
          break
        } else {
          break
        }
      }
      
      // 向右移动
      for (let col = colIndex + 1; col < 8; col++) {
        if (board.value[rowIndex][col] === null) {
          moves.push({ row: rowIndex, col })
        } else if (board.value[rowIndex][col].color !== piece.color) {
          moves.push({ row: rowIndex, col })
          break
        } else {
          break
        }
      }
      break
    
    case 'knight':
      // 处理马的移动
      const knightMoves = [
        { row: -2, col: -1 },
        { row: -2, col: 1 },
        { row: -1, col: -2 },
        { row: -1, col: 2 },
        { row: 1, col: -2 },
        { row: 1, col: 2 },
        { row: 2, col: -1 },
        { row: 2, col: 1 }
      ]
      
      for (const move of knightMoves) {
        const newRow = rowIndex + move.row
        const newCol = colIndex + move.col
        
        if (newRow >= 0 && newRow < 8 && newCol >= 0 && newCol < 8) {
          if (board.value[newRow][newCol] === null || 
              board.value[newRow][newCol].color !== piece.color) {
            moves.push({ row: newRow, col: newCol })
          }
        }
      }
      break
    
    case 'bishop':
      // 处理象的移动
      // 左上移动
      for (let i = 1; i < 8; i++) {
        const newRow = rowIndex - i
        const newCol = colIndex - i
        
        if (newRow >= 0 && newCol >= 0) {
          if (board.value[newRow][newCol] === null) {
            moves.push({ row: newRow, col: newCol })
          } else if (board.value[newRow][newCol].color !== piece.color) {
            moves.push({ row: newRow, col: newCol })
            break
          } else {
            break
          }
        } else {
          break
        }
      }
      
      // 右上移动
      for (let i = 1; i < 8; i++) {
        const newRow = rowIndex - i
        const newCol = colIndex + i
        
        if (newRow >= 0 && newCol < 8) {
          if (board.value[newRow][newCol] === null) {
            moves.push({ row: newRow, col: newCol })
          } else if (board.value[newRow][newCol].color !== piece.color) {
            moves.push({ row: newRow, col: newCol })
            break
          } else {
            break
          }
        } else {
          break
        }
      }
      
      // 左下移动
      for (let i = 1; i < 8; i++) {
        const newRow = rowIndex + i
        const newCol = colIndex - i
        
        if (newRow < 8 && newCol >= 0) {
          if (board.value[newRow][newCol] === null) {
            moves.push({ row: newRow, col: newCol })
          } else if (board.value[newRow][newCol].color !== piece.color) {
            moves.push({ row: newRow, col: newCol })
            break
          } else {
            break
          }
        } else {
          break
        }
      }
      
      // 右下移动
      for (let i = 1; i < 8; i++) {
        const newRow = rowIndex + i
        const newCol = colIndex + i
        
        if (newRow < 8 && newCol < 8) {
          if (board.value[newRow][newCol] === null) {
            moves.push({ row: newRow, col: newCol })
          } else if (board.value[newRow][newCol].color !== piece.color) {
            moves.push({ row: newRow, col: newCol })
            break
          } else {
            break
          }
        } else {
          break
        }
      }
      break
    
    case 'queen':
      // 处理后的移动（结合车和象的移动）
      // 向上移动
      for (let row = rowIndex - 1; row >= 0; row--) {
        if (board.value[row][colIndex] === null) {
          moves.push({ row, col: colIndex })
        } else if (board.value[row][colIndex].color !== piece.color) {
          moves.push({ row, col: colIndex })
          break
        } else {
          break
        }
      }
      
      // 向下移动
      for (let row = rowIndex + 1; row < 8; row++) {
        if (board.value[row][colIndex] === null) {
          moves.push({ row, col: colIndex })
        } else if (board.value[row][colIndex].color !== piece.color) {
          moves.push({ row, col: colIndex })
          break
        } else {
          break
        }
      }
      
      // 向左移动
      for (let col = colIndex - 1; col >= 0; col--) {
        if (board.value[rowIndex][col] === null) {
          moves.push({ row: rowIndex, col })
        } else if (board.value[rowIndex][col].color !== piece.color) {
          moves.push({ row: rowIndex, col })
          break
        } else {
          break
        }
      }
      
      // 向右移动
      for (let col = colIndex + 1; col < 8; col++) {
        if (board.value[rowIndex][col] === null) {
          moves.push({ row: rowIndex, col })
        } else if (board.value[rowIndex][col].color !== piece.color) {
          moves.push({ row: rowIndex, col })
          break
        } else {
          break
        }
      }
      
      // 左上移动
      for (let i = 1; i < 8; i++) {
        const newRow = rowIndex - i
        const newCol = colIndex - i
        
        if (newRow >= 0 && newCol >= 0) {
          if (board.value[newRow][newCol] === null) {
            moves.push({ row: newRow, col: newCol })
          } else if (board.value[newRow][newCol].color !== piece.color) {
            moves.push({ row: newRow, col: newCol })
            break
          } else {
            break
          }
        } else {
          break
        }
      }
      
      // 右上移动
      for (let i = 1; i < 8; i++) {
        const newRow = rowIndex - i
        const newCol = colIndex + i
        
        if (newRow >= 0 && newCol < 8) {
          if (board.value[newRow][newCol] === null) {
            moves.push({ row: newRow, col: newCol })
          } else if (board.value[newRow][newCol].color !== piece.color) {
            moves.push({ row: newRow, col: newCol })
            break
          } else {
            break
          }
        } else {
          break
        }
      }
      
      // 左下移动
      for (let i = 1; i < 8; i++) {
        const newRow = rowIndex + i
        const newCol = colIndex - i
        
        if (newRow < 8 && newCol >= 0) {
          if (board.value[newRow][newCol] === null) {
            moves.push({ row: newRow, col: newCol })
          } else if (board.value[newRow][newCol].color !== piece.color) {
            moves.push({ row: newRow, col: newCol })
            break
          } else {
            break
          }
        } else {
          break
        }
      }
      
      // 右下移动
      for (let i = 1; i < 8; i++) {
        const newRow = rowIndex + i
        const newCol = colIndex + i
        
        if (newRow < 8 && newCol < 8) {
          if (board.value[newRow][newCol] === null) {
            moves.push({ row: newRow, col: newCol })
          } else if (board.value[newRow][newCol].color !== piece.color) {
            moves.push({ row: newRow, col: newCol })
            break
          } else {
            break
          }
        } else {
          break
        }
      }
      break
    
    case 'king':
      // 处理王的移动
      const kingMoves = [
        { row: -1, col: -1 },
        { row: -1, col: 0 },
        { row: -1, col: 1 },
        { row: 0, col: -1 },
        { row: 0, col: 1 },
        { row: 1, col: -1 },
        { row: 1, col: 0 },
        { row: 1, col: 1 }
      ]
      
      for (const move of kingMoves) {
        const newRow = rowIndex + move.row
        const newCol = colIndex + move.col
        
        if (newRow >= 0 && newRow < 8 && newCol >= 0 && newCol < 8) {
          if (board.value[newRow][newCol] === null || 
              board.value[newRow][newCol].color !== piece.color) {
            moves.push({ row: newRow, col: newCol })
          }
        }
      }
      break
  }
  
  return moves
}

// 检查游戏结束
const checkGameEnd = () => {
  // 检查是否有玩家没有王
  let whiteKingExists = false
  let blackKingExists = false
  
  for (let row = 0; row < 8; row++) {
    for (let col = 0; col < 8; col++) {
      if (board.value[row][col] !== null && 
          board.value[row][col].type === 'king') {
        if (board.value[row][col].color === 'white') {
          whiteKingExists = true
        } else {
          blackKingExists = true
        }
      }
    }
  }
  
  if (!whiteKingExists) {
    gameStatus.value = '黑方获胜'
  } else if (!blackKingExists) {
    gameStatus.value = '白方获胜'
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
.international-chess-container {
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
}

.black-cell {
  background-color: #7B3F00;
}

.white-cell {
  background-color: #FFEBCD;
}

.piece {
  width: 45px;
  height: 45px;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 2rem;
  transition: transform 0.3s;
}

.white-piece {
  color: white;
}

.black-piece {
  color: black;
}

.piece:hover {
  transform: scale(1.1);
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
</style>