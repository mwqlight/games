<template>
  <div class="chess-view">
    <h1>国际象棋</h1>
    <div class="game-info">
      <div class="current-player">
        当前玩家: {{ currentPlayer === 'WHITE' ? '白方' : '黑方' }}
      </div>
      <div class="game-status">
        游戏状态: {{ getStatusText(gameStatus) }}
      </div>
      <button @click="startNewGame" class="new-game-btn">
        开始新游戏
      </button>
    </div>
    <div class="board-container">
      <ChessBoard />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue';
import { useChessStore } from '../stores/chessStore';
import ChessBoard from '../components/ChessBoard.vue';

const store = useChessStore();

const currentPlayer = computed(() => store.currentPlayer);
const gameStatus = computed(() => store.gameStatus);

const getStatusText = (status) => {
  switch (status) {
    case 'playing':
      return '游戏进行中';
    case 'check':
      return '将军';
    case 'checkmate':
      return '将死';
    case 'stalemate':
      return '和棋';
    default:
      return status;
  }
};

const startNewGame = () => {
  store.startGame();
};

onMounted(() => {
  store.startGame();
});
</script>

<style scoped>
.chess-view {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
}

h1 {
  font-size: 2.5rem;
  margin-bottom: 20px;
  color: #333;
}

.game-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 400px;
  margin-bottom: 20px;
  padding: 10px;
  background-color: #f5f5f5;
  border-radius: 8px;
}

.current-player {
  font-size: 1.2rem;
  font-weight: bold;
  color: #333;
}

.game-status {
  font-size: 1.2rem;
  font-weight: bold;
  color: #e74c3c;
}

.new-game-btn {
  padding: 8px 16px;
  font-size: 1rem;
  background-color: #3498db;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.new-game-btn:hover {
  background-color: #2980b9;
}

.board-container {
  border: 4px solid #333;
  border-radius: 8px;
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.3);
}
</style>